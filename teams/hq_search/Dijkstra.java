package hq_search;

import java.util.Arrays;

import battlecode.common.Clock;

public class Dijkstra {
    static int         iters;
    static int         width       = 0;
    static int         height      = 0;
    static int[][]     previous    = null;
    static boolean     finished    = false;
    static final int   UNSET       = 9;
    static int[][]     grid        = null;
    static boolean[][] visited     = null;
    static FibHeap     distFibHeap = null;
    static int         MAX_VERTS   = FibHeap.MAX_VERTS;

    public static void setupDijkstra(int[][] _grid, int start_x, int start_y) {
        height = _grid.length;
        width = _grid[0].length;
        previous = new int[height][width];
        visited = new boolean[height][width];
        finished = false;
        grid = _grid;
        distFibHeap = new FibHeap(height * width);
        iters = 0;

        // Initialize tentative distances to infinity except zero at source
        for (int[] p : previous) {
            // Saves a few hundred bytecode to use Array.fill
            Arrays.fill(p, UNSET);
        }

        distFibHeap.decreaseKey(to_index(start_y, start_x), 0);
    }

    private static int to_index(int start_y, int start_x) {
        return start_y * width + start_x;
    }

    public static void doDijkstra() {
        int bytes = Clock.getBytecodesLeft();

        while (true) {
            int bc = Clock.getBytecodesLeft();
            if (bc < 1000 || bc < bytes / 2) {
                return;
            }
            bytes = bc;

            // Find the position with minimum distance
            int val_index = distFibHeap.extractMin();
            int index = val_index % MAX_VERTS;
            int val = val_index / MAX_VERTS;
            int bestX = index % width;
            int bestY = index / width;
            visited[bestY][bestX] = true;

            // Iterated over all neighbors
            int left = bestX - 1;
            int right = bestX + 1;
            int up = bestY - 1;
            int down = bestY + 1;

            if (left >= 0) {
                if (up >= 0 && !visited[up][left]) {
                    int alt = val + grid[up][left];
                    int i = up * width + left;
                    if (alt < distFibHeap.getVal(i)) {
                        distFibHeap.decreaseKey(i, alt);
                        previous[up][left] = toDir(bestX, bestY, left, up);
                    }
                }
                if (!visited[bestY][left]) {
                    int alt = val + grid[bestY][left];
                    int i = bestY * width + left;
                    if (alt < distFibHeap.getVal(i)) {
                        distFibHeap.decreaseKey(i, alt);
                        previous[bestY][left] = toDir(bestX, bestY, left, bestY);
                    }
                }
                if (down < height && !visited[down][left]) {
                    int alt = val + grid[down][left];
                    int i = down * width + left;
                    if (alt < distFibHeap.getVal(i)) {
                        distFibHeap.decreaseKey(i, alt);
                        previous[down][left] = toDir(bestX, bestY, left, down);
                    }
                }
            }

            if (up >= 0 && !visited[up][bestX]) {
                int alt = val + grid[up][bestX];
                int i = up * width + bestX;
                if (alt < distFibHeap.getVal(i)) {
                    distFibHeap.decreaseKey(i, alt);
                    previous[up][bestX] = toDir(bestX, bestY, bestX, up);
                }
            }
            if (down < height && !visited[down][bestX]) {
                int alt = val + grid[down][bestX];
                int i = down * width + bestX;
                if (alt < distFibHeap.getVal(i)) {
                    distFibHeap.decreaseKey(i, alt);
                    previous[down][bestX] = toDir(bestX, bestY, bestX, down);
                }
            }

            if (right < width) {
                if (up >= 0 && !visited[up][right]) {
                    int alt = val + grid[up][right];
                    int i = up * width + right;
                    if (alt < distFibHeap.getVal(i)) { // Need to update
                        distFibHeap.decreaseKey(i, alt);
                        previous[up][right] = toDir(bestX, bestY, right, up);
                    }
                }
                if (!visited[bestY][right]) {
                    int alt = val + grid[bestY][right];
                    int i = bestY * width + right;
                    if (alt < distFibHeap.getVal(i)) {
                        distFibHeap.decreaseKey(i, alt);
                        previous[bestY][right] = toDir(bestX, bestY, right, bestY);
                    }
                }
                if (down < height && !visited[down][right]) {
                    int alt = val + grid[down][right];
                    int i = down * width + right;
                    if (alt < distFibHeap.getVal(i)) {
                        distFibHeap.decreaseKey(i, alt);
                        previous[down][right] = toDir(bestX, bestY, right, down);
                    }
                }
            }

            iters++;
            if (iters >= height * width) {
                break;
            }
        }

        Dijkstra.finished = true;
    }

    private static int toDir(int endX, int endY, int startX, int startY) {
        int diffX = startX - endX;
        int diffY = startY - endY;

        // Match directions which starts at north and goes clockwise

        switch (diffX) {
            case 1:
                switch (diffY) {
                    case -1:
                        return 5;
                    case 0:
                        return 6;
                    case 1:
                        return 7;
                }
            case 0:
                switch (diffY) {
                    case -1:
                        return 4;
                    case 1:
                        return 0;
                }
            case -1:
                switch (diffY) {
                    case -1:
                        return 3;
                    case 0:
                        return 2;
                    case 1:
                        return 1;
                }
        }

        return UNSET;
    }
}
