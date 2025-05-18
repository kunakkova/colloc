package ru.leti;

import ru.leti.wise.task.plugin.graph.GraphCharacteristic;
import ru.leti.wise.task.graph.model.Graph;
import ru.leti.wise.task.graph.model.Vertex;
import ru.leti.wise.task.graph.model.Edge;

import java.util.*;

public class MaxVertexDisjointPaths implements GraphCharacteristic {

    private static final int INF = 1_000_000;

    @Override
    public int run(Graph graph) {
        List<Vertex> vertices = graph.getVertexList();
        int maxPaths = 0;

        for (Vertex source : vertices) {
            for (Vertex sink : vertices) {
                if (source.getId() != sink.getId()) {
                    int currentFlow = calculateMaxFlowForPair(graph, source.getId(), sink.getId());
                    maxPaths = Math.max(maxPaths, currentFlow);
                }
            }
        }

        return maxPaths;
    }

    private int calculateMaxFlowForPair(Graph graph, int sourceId, int sinkId) {
        List<Vertex> vertices = graph.getVertexList();
        int maxId = vertices.stream().mapToInt(Vertex::getId).max().orElse(0);

        List<List<Dinic.Edge>> adjacency = new ArrayList<>(2 * maxId + 2);
        for (int i = 0; i <= 2 * maxId + 1; i++) {
            adjacency.add(new ArrayList<>());
        }

        for (Vertex v : vertices) {
            int in = 2 * v.getId();
            int out = in + 1;
            int capacity = (v.getId() == sourceId || v.getId() == sinkId) ? INF : 1;
            addEdge(adjacency, in, out, capacity);
        }

        for (Edge e : graph.getEdgeList()) {
            int uOut = 2 * e.getSource() + 1;
            int vIn = 2 * e.getTarget();
            addEdge(adjacency, uOut, vIn, 1);
            if (!graph.isDirect()) {
                int vOut = 2 * e.getTarget() + 1;
                int uIn = 2 * e.getSource();
                addEdge(adjacency, vOut, uIn, 1);
            }
        }

        int sourceOut = 2 * sourceId + 1;
        int sinkIn = 2 * sinkId;

        Dinic dinic = new Dinic(adjacency, sourceOut, sinkIn);
        return dinic.calculateMaxFlow();
    }

    private void addEdge(List<List<Dinic.Edge>> adjacency, int from, int to, int capacity) {
        Dinic.Edge forward = new Dinic.Edge(to, adjacency.get(to).size(), capacity);
        Dinic.Edge backward = new Dinic.Edge(from, adjacency.get(from).size(), 0);
        adjacency.get(from).add(forward);
        adjacency.get(to).add(backward);
    }

    static class Dinic {
        private final List<List<Edge>> graph;
        private final int source;
        private final int sink;
        private final int[] level;

        public Dinic(List<List<Edge>> graph, int source, int sink) {
            this.graph = graph;
            this.source = source;
            this.sink = sink;
            this.level = new int[graph.size()];
        }

        public int calculateMaxFlow() {
            int maxFlow = 0;
            while (bfs()) {
                int[] ptr = new int[graph.size()];
                while (true) {
                    int pushed = dfs(source, Integer.MAX_VALUE, ptr);
                    if (pushed == 0) break;
                    maxFlow += pushed;
                }
            }
            return maxFlow;
        }

        private boolean bfs() {
            Arrays.fill(level, -1);
            level[source] = 0;
            Queue<Integer> queue = new LinkedList<>();
            queue.add(source);
            while (!queue.isEmpty()) {
                int u = queue.poll();
                for (Edge e : graph.get(u)) {
                    if (level[e.to] == -1 && e.flow < e.capacity) {
                        level[e.to] = level[u] + 1;
                        queue.add(e.to);
                    }
                }
            }
            return level[sink] != -1;
        }

        private int dfs(int u, int flow, int[] ptr) {
            if (u == sink) return flow;
            for (; ptr[u] < graph.get(u).size(); ptr[u]++) {
                Edge e = graph.get(u).get(ptr[u]);
                if (level[e.to] == level[u] + 1 && e.flow < e.capacity) {
                    int minFlow = Math.min(flow, e.capacity - e.flow);
                    int pushed = dfs(e.to, minFlow, ptr);
                    if (pushed > 0) {
                        e.flow += pushed;
                        graph.get(e.to).get(e.rev).flow -= pushed;
                        return pushed;
                    }
                }
            }
            return 0;
        }

        static class Edge {
            int to;
            int rev;
            int flow;
            int capacity;

            Edge(int to, int rev, int capacity) {
                this.to = to;
                this.rev = rev;
                this.capacity = capacity;
                this.flow = 0;
            }
        }
    }
}
