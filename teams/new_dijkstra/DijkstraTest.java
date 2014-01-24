package new_dijkstra;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class DijkstraTest {

    @Test
    public void test3x3() {
        int[][] grid = { { 1, 2, 3 }, { 1, 3, 3 }, { 1, 4, 3 } };

        Dijkstra.setupDijkstra(grid, 0, 1); // grid, x, y
        Dijkstra.doDijkstra();

        int[][] expected = { { 4, 5, 6 }, { 9, 6, 7 }, { 0, 7, 7 } };

        assertTrue(java.util.Arrays.deepEquals(expected, Dijkstra.previous));
    }

    @Test
    // This test concerns the 1.4 multiplicative factor for diagonal movement
    public void test3x3BiggerNumbers() {
        int[][] grid = { { 10, 20, 30 }, { 10, 30, 30 }, { 10, 40, 30 } };

        Dijkstra.setupDijkstra(grid, 0, 1); // grid, x, y
        Dijkstra.doDijkstra();

        int[][] expected = { { 4, 5, 6 }, { 9, 6, 6 }, { 0, 6, 7 } };

        assertTrue(java.util.Arrays.deepEquals(expected, Dijkstra.previous));
    }
}
