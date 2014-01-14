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
            /*
             * TODO: change to manually iterate over all directions int left = bestX - 1; int right
             * = bestX + 1; int up = bestY - 1; int down = bestY + 1;
             * 
             * if (left > 0) { if (up > 0 && !visited[left][up]) { int alt = val + grid[up][left];
             * int i = up * width + left; if (alt < distFibHeap.getVal(i)) { // Need to update
             * distFibHeap.decreaseKey(i, alt); previous[up][left] = toDir(bestX, bestY, left, up);
             * } } }
             */

            for (int x = bestX - 1; x <= bestX + 1; x++) {
                if (x < 0 || x >= width) {
                    continue;
                }
                for (int y = bestY - 1; y <= bestY + 1; y++) {
                    if (y < 0 || y >= height || visited[y][x] || (x == bestX && y == bestY)) {
                        continue;
                    }

                    // TODO: Add 1.4 penalty for diagonal moves

                    // Here is where we are iterating over all neighbors
                    int alt = val + grid[y][x];
                    int i = y * width + x;
                    if (alt < distFibHeap.getVal(i)) {
                        // Need to update
                        distFibHeap.decreaseKey(i, alt);
                        previous[y][x] = toDir(bestX, bestY, x, y);
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
