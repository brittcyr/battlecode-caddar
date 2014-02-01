package team050;

import java.util.Arrays;

import battlecode.common.Clock;

public class Dijkstra {
    public static int         iters;
    public static int         width          = 0;
    public static int         height         = 0;
    public static int[][]     previous       = null;
    public static boolean     finished       = false;
    public static final int   UNSET          = 9;
    public static int[][]     grid           = null;
    public static boolean[][] visited        = null;
    public static StringHeap  distStringHeap = null;
    public static int[]       vals;
    public static int         byteCodeThresh = 1000;

    public static void setupDijkstra(int[][] _grid, int start_x, int start_y) {
        height = _grid.length;
        width = _grid[0].length;
        previous = new int[height][width];
        visited = new boolean[height][width];
        finished = false;
        grid = _grid;
        distStringHeap = new StringHeap(width, height);
        iters = 0;
        vals = new int[100 * Math.max(height, width)];

        Arrays.fill(vals, 999);

        // Initialize tentative distances to infinity except zero at source
        for (int[] p : previous) {
            // Saves a few hundred bytecode to use Array.fill
            Arrays.fill(p, UNSET);
        }

        distStringHeap.decreaseKey(start_y + 100 * start_x, 0);
        vals[start_y + 100 * start_x] = 0;
    }

    public static void doDijkstra() {
        while (true) {
            if (Clock.getBytecodesLeft() < byteCodeThresh) {
                return;
            }

            // Find the position with minimum distance
            int val = distStringHeap.getMinVal();
            int index = distStringHeap.extractMin();
            if (val > 950) {
                break;
            }
            int bestX = index / 100;
            int bestY = index % 100;
            visited[bestY][bestX] = true;

            // Iterated over all neighbors
            int left = bestX - 1;
            int right = bestX + 1;
            int up = bestY - 1;
            int down = bestY + 1;

            if (left >= 0) {
                if (up >= 0 && !visited[up][left]) {
                    int alt = val + (int) ((double) grid[up][left] * 1.4);
                    int i = up + 100 * left;
                    if (alt < vals[i]) {
                        distStringHeap.decreaseKey(i, alt);
                        vals[i] = alt;
                        previous[up][left] = 3;
                    }
                }
                if (!visited[bestY][left]) {
                    int alt = val + grid[bestY][left];
                    int i = bestY + 100 * left;
                    if (alt < vals[i]) {
                        distStringHeap.decreaseKey(i, alt);
                        vals[i] = alt;
                        previous[bestY][left] = 2;
                    }
                }
                if (down < height && !visited[down][left]) {
                    int alt = val + (int) ((double) grid[down][left] * 1.4);
                    int i = down + 100 * left;
                    if (alt < vals[i]) {
                        distStringHeap.decreaseKey(i, alt);
                        vals[i] = alt;
                        previous[down][left] = 1;
                    }
                }
            }

            if (up >= 0 && !visited[up][bestX]) {
                int alt = val + grid[up][bestX];
                int i = up + 100 * bestX;
                if (alt < vals[i]) {
                    distStringHeap.decreaseKey(i, alt);
                    vals[i] = alt;
                    previous[up][bestX] = 4;
                }
            }
            if (down < height && !visited[down][bestX]) {
                int alt = val + grid[down][bestX];
                int i = down + 100 * bestX;
                if (alt < vals[i]) {
                    distStringHeap.decreaseKey(i, alt);
                    vals[i] = alt;
                    previous[down][bestX] = 0;
                }
            }

            if (right < width) {
                if (up >= 0 && !visited[up][right]) {
                    int alt = val + (int) ((double) grid[up][right] * 1.4);
                    int i = up + 100 * right;
                    if (alt < vals[i]) {
                        distStringHeap.decreaseKey(i, alt);
                        vals[i] = alt;
                        previous[up][right] = 5;
                    }
                }
                if (!visited[bestY][right]) {
                    int alt = val + grid[bestY][right];
                    int i = bestY + 100 * right;
                    if (alt < vals[i]) {
                        distStringHeap.decreaseKey(i, alt);
                        vals[i] = alt;
                        previous[bestY][right] = 6;
                    }
                }
                if (down < height && !visited[down][right]) {
                    int alt = val + (int) ((double) grid[down][right] * 1.4);
                    int i = down + 100 * right;
                    if (alt < vals[i]) {
                        distStringHeap.decreaseKey(i, alt);
                        vals[i] = alt;
                        previous[down][right] = 7;
                    }
                }
            }

            if (++iters >= height * width) {
                break;
            }
        }

        Dijkstra.finished = true;
    }
}
