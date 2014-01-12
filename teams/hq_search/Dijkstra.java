package hq_search;

import battlecode.common.Clock;

public class Dijkstra {
    static int[][]     tentativeDistances = null;
    static int         width              = 0;
    static int         height             = 0;
    static boolean[][] visited            = null;
    static int[][]     previous           = null;
    static boolean     finished           = false;

    public static void doDijkstra(int[][] grid, int start_x, int start_y) {
        width = grid.length;
        height = grid[0].length;
        tentativeDistances = new int[width][height];
        visited = new boolean[width][height];
        previous = new int[width][height];

        // 1. Initialize tentative distances to infinity except zero at source
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < width; y++) {
                tentativeDistances[x][y] = 9999999;
                previous[x][y] = 99999;
            }
        }
        tentativeDistances[start_x][start_y] = 0;
        previous[start_x][start_y] = 0;

        boolean done = false;
        while (!done) {
            if (Clock.getBytecodesLeft() < 9000) {
                return;
            }
            // TODO: Use a better priority queue for distance
            // Find the position with minimum distance
            int best = 999999999;
            int bestX = 0;
            int bestY = 0;
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    if ((tentativeDistances[x][y] < best) && previous[x][y] == 99999) {
                        best = tentativeDistances[x][y];
                        bestX = x;
                        bestY = y;
                    }
                }
            }

            if (best > 9999999) {
                // This is the case where it is not one connected component
                return;
            }

            // Iterated over all neighbors
            for (int x = bestX - 1; x <= bestX + 1; x++) {
                for (int y = bestY - 1; y <= bestY + 1; y++) {
                    if (x == bestX && y == bestY) {
                        continue;
                    }
                    if (x < 0 || y < 0 || x >= width || y >= height) {
                        continue;
                    }

                    // Here is where we are iterating over all neighbors
                    int alt = tentativeDistances[bestX][bestY] + grid[x][y];
                    if (alt < tentativeDistances[x][y]) {
                        // Need to update
                        tentativeDistances[x][y] = alt;
                        previous[x][y] = bestX * 100 + bestY;
                    }

                }
            }

            // Detect if we are done
            done = true;
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    if (tentativeDistances[x][y] == 9999999)
                        done = false;
                }
            }
            System.out.println("ITER");
        }

        Dijkstra.finished = true;
        return;
    }
}
