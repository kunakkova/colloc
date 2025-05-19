package ru.leti;

import ru.leti.wise.task.plugin.graph.GraphCharacteristic;
import ru.leti.wise.task.graph.model.Graph;
import ru.leti.wise.task.graph.model.Vertex;
import ru.leti.wise.task.graph.model.Edge;

import java.util.*;

/**
 * Модуль для нахождения максимального числа вершинно-непересекающихся путей между всеми парами вершин в графе.
 *
 * Вершинно-непересекающиеся пути — пути, не имеющие общих вершин, за исключением начальной и конечной (начальная и конечная вершины не совпадают).
 *
 * По теореме Менгера задача сводится к поиску max потока в графе, где вершины разделены на вход/выход (ограничение capacity=1 на использование вершины). 
 * Алгоритм Диница эффективно работает с такими сетями, находя max поток, который соответствует числу вершинно-непересекающихся путей.
 *
 * Алгоритм преобразует исходный граф в сеть, где каждая вершина разделена на две (вход и выход), соединенные ребром с пропускной способностью 1.
 * Для исходных вершин S и T устанавливается бесконечная пропускная способность. Для нахождения максимального потока используется алгоритм Диница.
 */
public class MaxVertexDisjointPaths implements GraphCharacteristic {

    private static final int INF = 1_000_000; // Значение "бесконечности" для S и T

    @Override
    public int run(Graph graph) {
        List<Vertex> vertices = graph.getVertexList();
        int maxPaths = 0;

        // Перебираем все возможные пары вершин (источник, сток)
        for (Vertex source : vertices) {
            for (Vertex sink : vertices) {
                if (source.getId() != sink.getId()) {
                    // Вычисляем макс. поток для текущей пары и обновляем максимум
                    int currentFlow = calculateMaxFlowForPair(graph, source.getId(), sink.getId());
                    maxPaths = Math.max(maxPaths, currentFlow);
                }
            }
        }

        return maxPaths;
    }

    // Преобразует граф в сеть и вычисляет макс. поток между sourceId и sinkId
    private int calculateMaxFlowForPair(Graph graph, int sourceId, int sinkId) {
        List<Vertex> vertices = graph.getVertexList();
        int maxId = vertices.stream().mapToInt(Vertex::getId).max().orElse(0);

        // Создаем списки смежности с учетом разделения вершин на входы и выходы
        List<List<Dinic.Edge>> adjacency = new ArrayList<>(2 * maxId + 2);
        for (int i = 0; i <= 2 * maxId + 1; i++) {
            adjacency.add(new ArrayList<>());
        }

        // Разделяем каждую вершину на вход (in) и выход (out)
        for (Vertex v : vertices) {
            int in = 2 * v.getId();       // Вход вершины
            int out = in + 1;             // Выход вершины
            // Для S и T пропускная способность "бесконечна"
            int capacity = (v.getId() == sourceId || v.getId() == sinkId) ? INF : 1;
            addEdge(adjacency, in, out, capacity);
        }

        // Добавляем ребра исходного графа между выходами и входами вершин
        for (Edge e : graph.getEdgeList()) {
            int uOut = 2 * e.getSource() + 1; // Выход вершины-источника
            int vIn = 2 * e.getTarget();       // Вход вершины-назначения
            addEdge(adjacency, uOut, vIn, 1);

            // Для неориентированного графа добавляем обратное ребро
            if (!graph.isDirect()) {
                int vOut = 2 * e.getTarget() + 1;
                int uIn = 2 * e.getSource();
                addEdge(adjacency, vOut, uIn, 1);
            }
        }

        // Определяем исток (выход S) и сток (вход T) в преобразованной сети
        int sourceOut = 2 * sourceId + 1;
        int sinkIn = 2 * sinkId;

        Dinic dinic = new Dinic(adjacency, sourceOut, sinkIn);
        return dinic.calculateMaxFlow();
    }

    // Добавляет прямое и обратное (для алгоритма Диница) ребро в сеть
    private void addEdge(List<List<Dinic.Edge>> adjacency, int from, int to, int capacity) {
        Dinic.Edge forward = new Dinic.Edge(to, adjacency.get(to).size(), capacity);
        Dinic.Edge backward = new Dinic.Edge(from, adjacency.get(from).size(), 0);
        adjacency.get(from).add(forward);
        adjacency.get(to).add(backward);
    }

    // Реализация алгоритма Диница для поиска максимального потока
    static class Dinic {
        private final List<List<Edge>> graph;
        private final int source;     // Исток в преобразованной сети
        private final int sink;       // Сток в преобразованной сети
        private final int[] level;   // Уровни вершин для BFS

        public Dinic(List<List<Edge>> graph, int source, int sink) {
            this.graph = graph;
            this.source = source;
            this.sink = sink;
            this.level = new int[graph.size()];
        }

        public int calculateMaxFlow() {
            int maxFlow = 0;
            // Пока существует увеличивающий путь (BFS)
            while (bfs()) {
                int[] ptr = new int[graph.size()]; // Указатели для DFS
                while (true) {
                    int pushed = dfs(source, Integer.MAX_VALUE, ptr);
                    if (pushed == 0) break;
                    maxFlow += pushed;
                }
            }
            return maxFlow;
        }

        // Построение слоистой сети (по уровням)
        private boolean bfs() {
            Arrays.fill(level, -1);
            level[source] = 0;
            Queue<Integer> queue = new LinkedList<>();
            queue.add(source);

            while (!queue.isEmpty()) {
                int u = queue.poll();
                // Обход всех ребер вершины
                for (Edge e : graph.get(u)) {
                    // Если вершина не посещена и есть остаточная пропускная способность
                    if (level[e.to] == -1 && e.flow < e.capacity) {
                        level[e.to] = level[u] + 1;
                        queue.add(e.to);
                    }
                }
            }
            return level[sink] != -1; // Возвращаем достижим ли сток
        }

        // Поиск блокирующего потока с использованием указателей
        private int dfs(int u, int flow, int[] ptr) {
            if (u == sink) return flow;

            for (; ptr[u] < graph.get(u).size(); ptr[u]++) {
                Edge e = graph.get(u).get(ptr[u]);
                // Проверка уровня и остаточной пропускной способности
                if (level[e.to] == level[u] + 1 && e.flow < e.capacity) {
                    int minFlow = Math.min(flow, e.capacity - e.flow);
                    int pushed = dfs(e.to, minFlow, ptr);

                    if (pushed > 0) {
                        // Обновляем поток в прямом и обратном ребрах
                        e.flow += pushed;
                        graph.get(e.to).get(e.rev).flow -= pushed;
                        return pushed;
                    }
                }
            }
            return 0;
        }

        // Представление ребра в алгоритме Диница
        static class Edge {
            int to;        // Куда ведет ребро
            int rev;       // Индекс обратного ребра в списке смежности
            int flow;      // Текущий поток
            int capacity;  // Пропускная способность

            Edge(int to, int rev, int capacity) {
                this.to = to;
                this.rev = rev;
                this.capacity = capacity;
                this.flow = 0;
            }
        }
    }
}
