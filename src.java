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
        int n = vertices.size();

        // Строим вспомогательный граф для потоков, сделаем метод для каждого s,t
        int maxPathsOverall = 0;

        for (int i = 0; i < n; i++) {
            int s = vertices.get(i).getId();
            for (int j = 0; j < n; j++) {
                if (i == j) continue;
                int t = vertices.get(j).getId();

                int maxFlow = maxVertexDisjointPaths(graph, s, t);
                if (maxFlow > maxPathsOverall) {
                    maxPathsOverall = maxFlow;
                }
            }
        }

        return maxPathsOverall;
    }

    private int maxVertexDisjointPaths(Graph graph, int s, int t) {
        List<Vertex> vertices = graph.getVertexList();
        Map<Integer, Integer> vertexIdToIndex = new HashMap<>();
        for (int i = 0; i < vertices.size(); i++) {
            vertexIdToIndex.put(vertices.get(i).getId(), i);
        }

        int n = vertices.size();
        int size = n * 2; // количество вершин в преобразованном графе

        List<EdgeFlow>[] flowGraph = createFlowGraph(size);

        // Преобразуем s и t в индексы в списке
        Integer sIndex = vertexIdToIndex.get(s);
        Integer tIndex = vertexIdToIndex.get(t);
        if (sIndex == null || tIndex == null) {
            return 0; // если такие вершины не найдены — 0 путей
        }

        // Добавляем ребра v_in -> v_out
        for (int v = 0; v < n; v++) {
            int v_in = v * 2;
            int v_out = v * 2 + 1;
            int capacity = (v == sIndex || v == tIndex) ? INF : 1;
            addEdge(flowGraph, v_in, v_out, capacity);
        }

        // Добавляем ребра из исходного графа (используем индексы)
        for (Edge e : graph.getEdgeList()) {
            Integer uIndex = vertexIdToIndex.get(e.getSource());
            Integer vIndex = vertexIdToIndex.get(e.getTarget());
            if (uIndex == null || vIndex == null) continue;

            if (!graph.isDirect()) {
                addEdge(flowGraph, uIndex * 2 + 1, vIndex * 2, INF);
                addEdge(flowGraph, vIndex * 2 + 1, uIndex * 2, INF);
            } else {
                addEdge(flowGraph, uIndex * 2 + 1, vIndex * 2, INF);
            }
        }

        int source = sIndex * 2 + 1;
        int sink = tIndex * 2;

        return dinicMaxFlow(flowGraph, source, sink);
    }

    private static class EdgeFlow {
        int to, rev;
        int capacity;
        EdgeFlow(int to, int rev, int capacity) {
            this.to = to;
            this.rev = rev;
            this.capacity = capacity;
        }
    }

    private List<EdgeFlow>[] createFlowGraph(int size) {
        List<EdgeFlow>[] graph = new List[size];
        for (int i = 0; i < size; i++) {
            graph[i] = new ArrayList<>();
        }
        return graph;
    }

    private void addEdge(List<EdgeFlow>[] graph, int from, int to, int capacity) {
        graph[from].add(new EdgeFlow(to, graph[to].size(), capacity));
        graph[to].add(new EdgeFlow(from, graph[from].size() - 1, 0)); // обратное ребро с capacity 0
    }

    private int dinicMaxFlow(List<EdgeFlow>[] graph, int source, int sink) {
        int flow = 0;
        int[] level = new int[graph.length];

        while (bfs(graph, source, sink, level)) {
            int[] iter = new int[graph.length];
            int f;
            while ((f = dfs(graph, source, sink, INF, iter, level)) > 0) {
                flow += f;
            }
        }
        return flow;
    }

    private boolean bfs(List<EdgeFlow>[] graph, int source, int sink, int[] level) {
        Arrays.fill(level, -1);
        Queue<Integer> queue = new ArrayDeque<>();
        level[source] = 0;
        queue.add(source);

        while (!queue.isEmpty()) {
            int v = queue.poll();
            for (EdgeFlow e : graph[v]) {
                if (e.capacity > 0 && level[e.to] < 0) {
                    level[e.to] = level[v] + 1;
                    queue.add(e.to);
                }
            }
        }
        return level[sink] >= 0;
    }

    private int dfs(List<EdgeFlow>[] graph, int v, int sink, int flow, int[] iter, int[] level) {
        if (v == sink) return flow;
        for (; iter[v] < graph[v].size(); iter[v]++) {
            EdgeFlow e = graph[v].get(iter[v]);
            if (e.capacity > 0 && level[v] < level[e.to]) {
                int d = dfs(graph, e.to, sink, Math.min(flow, e.capacity), iter, level);
                if (d > 0) {
                    e.capacity -= d;
                    graph[e.to].get(e.rev).capacity += d;
                    return d;
                }
            }
        }
        return 0;
    }
}
