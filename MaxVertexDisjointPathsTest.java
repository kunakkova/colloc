import ru.leti.MaxVertexDisjointPaths;
import ru.leti.wise.task.graph.util.FileLoader;
import ru.leti.wise.task.graph.model.Graph;

import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class MaxVertexDisjointPathsTest {

    @Test
    public void testNullGraph() throws FileNotFoundException {
        MaxVertexDisjointPaths plugin = new MaxVertexDisjointPaths();
        Graph graph1 = FileLoader.loadGraphFromJson("src/test/resources/null_graph.json");

        // В пустом графе нет вершин, значит не может быть и путей
        int result = plugin.run(graph1);

        // Ожидаем 0 путей
        assertThat(result).isEqualTo(0);
    }

    @Test
    public void testSingleVert() throws FileNotFoundException {
        MaxVertexDisjointPaths plugin = new MaxVertexDisjointPaths();
        Graph graph1 = FileLoader.loadGraphFromJson("src/test/resources/single_vert.json");

        // В графе с одной вершиной нет пар вершин для поиска путей
        int result = plugin.run(graph1);

        // Ожидаем 0 путей
        assertThat(result).isEqualTo(0);
    }

    @Test
    public void test1Vert1Loop() throws FileNotFoundException {
        MaxVertexDisjointPaths plugin = new MaxVertexDisjointPaths();
        Graph graph1 = FileLoader.loadGraphFromJson("src/test/resources/1_vert_1_loop.json");

        // В графе с одной вершиной и петлей нет пар вершин для поиска путей
        int result = plugin.run(graph1);

        // Ожидаем 0 путей
        assertThat(result).isEqualTo(0);
    }

    @Test
    public void test2Vert0Edge() throws FileNotFoundException {
        MaxVertexDisjointPaths plugin = new MaxVertexDisjointPaths();
        Graph graph1 = FileLoader.loadGraphFromJson("src/test/resources/2_vert_0_edge.json");

        // В графе с двумя вершинами без ребер нет путей между ними
        int result = plugin.run(graph1);

        // Ожидаем 0 путей
        assertThat(result).isEqualTo(0);
    }

    @Test
    public void test2Vert1Edge() throws FileNotFoundException {
        MaxVertexDisjointPaths plugin = new MaxVertexDisjointPaths();
        Graph graph1 = FileLoader.loadGraphFromJson("src/test/resources/2_vert_1_edge.json");

        // В графе с двумя вершинами и одним ребром есть 1 путь между ними
        int result = plugin.run(graph1);

        // Ожидаем 1 путь
        assertThat(result).isEqualTo(1);
    }

    @Test
    public void test2Chains() throws FileNotFoundException {
        MaxVertexDisjointPaths plugin = new MaxVertexDisjointPaths();
        Graph graph1 = FileLoader.loadGraphFromJson("src/test/resources/2_chains.json");

        // В графе с двумя независимыми цепочками между парой вершин
        int result = plugin.run(graph1);

        // Ожидаем 2 вершинно-непересекающихся пути
        assertThat(result).isEqualTo(2);
    }

    @Test
    public void test7VertNondirectional() throws FileNotFoundException {
        MaxVertexDisjointPaths plugin = new MaxVertexDisjointPaths();
        Graph graph1 = FileLoader.loadGraphFromJson("src/test/resources/7_vert_nondirectional.json");

        // В неориентированном графе из 7 вершин
        int result = plugin.run(graph1);

        // Ожидаем 3 вершинно-непересекающихся пути для некоторой пары вершин
        assertThat(result).isEqualTo(3);
    }

    @Test
    public void test5VertDirected() throws FileNotFoundException {
        MaxVertexDisjointPaths plugin = new MaxVertexDisjointPaths();
        Graph graph1 = FileLoader.loadGraphFromJson("src/test/resources/5_vert_directed.json");

        // В ориентированном графе из 5 вершин
        int result = plugin.run(graph1);

        // Ожидаем 2 вершинно-непересекающихся пути для некоторой пары вершин
        assertThat(result).isEqualTo(2);
    }

    @Test
    public void testK5() throws FileNotFoundException {
        MaxVertexDisjointPaths plugin = new MaxVertexDisjointPaths();
        Graph graph1 = FileLoader.loadGraphFromJson("src/test/resources/k5.json");

        // В полном графе K5
        int result = plugin.run(graph1);

        // Ожидаем 4 вершинно-непересекающихся пути для любой пары вершин
        assertThat(result).isEqualTo(4);
    }

    @Test
    public void testK5K3Disconnected() throws FileNotFoundException {
        MaxVertexDisjointPaths plugin = new MaxVertexDisjointPaths();
        Graph graph1 = FileLoader.loadGraphFromJson("src/test/resources/k5_k3_disconnected.json");

        // В несвязном графе из K5 и K3 компонент
        int result = plugin.run(graph1);

        // Ожидаем 4 пути (максимум из компоненты K5)
        assertThat(result).isEqualTo(4);
    }

    @Test
    public void testBigCountOfPaths() throws FileNotFoundException {
        MaxVertexDisjointPaths plugin = new MaxVertexDisjointPaths();
        Graph graph1 = FileLoader.loadGraphFromJson("src/test/resources/big_count_of_paths.json");

        // В графе с большим количеством возможных путей
        int result = plugin.run(graph1);

        // Ожидаем 8 вершинно-непересекающихся путей для некоторой пары вершин
        assertThat(result).isEqualTo(8);
    }
}