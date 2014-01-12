package hq_search;

import battlecode.common.Clock;

public class Dijkstra {
    static int         width              = 0;
    static int         height             = 0;
    static int         iters       = 0;
    static int[][]     previous           = null;
    static boolean     finished           = false;
    static final int   INFINITY           = 999999999;
    static final int   UNSET       = 9;
    static int[][]     grid               = null;
    static boolean[][] set                = null;
    static FibHeap     distFibHeap        = null;

    public static void setupDijkstra(int[][] _grid, int start_x, int start_y) {
        iters = 0;
        height = _grid.length;
        width = _grid[0].length;
        previous = new int[height][width];
        set = new boolean[height][width];
        finished = false;
        grid = _grid;
        distFibHeap = new FibHeap(height * width);

        // Initialize tentative distances to infinity except zero at source
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                previous[y][x] = UNSET;
                set[y][x] = false;
            }
        }
        distFibHeap.decreaseKey(to_index(start_y, start_x), 0);
        return;
    }

    private static int to_index(int start_y, int start_x) {
        return start_y * width + start_x;
    }

    private static int index_to_x(int index) {
        return index % width;
    }

    private static int index_to_y(int index) {
        return index / width;
    }

    public static void doDijkstra() {
        boolean done = false;
        int bytes = Clock.getBytecodesLeft();

        while (!done) {
            int bc = Clock.getBytecodesLeft();
            if (bc < 1000 || bc < bytes / 2) {
                return;
            }
            bytes = Clock.getBytecodesLeft();

            // Find the position with minimum distance
            int val_index = distFibHeap.extractMin();
            int index = val_index % FibHeap.MAX_VERTS;
            int val = val_index / FibHeap.MAX_VERTS;
            int bestX = index_to_x(index);
            int bestY = index_to_y(index);
            set[bestY][bestX] = true;

            // Iterated over all neighbors
            for (int x = bestX - 1; x <= bestX + 1; x++) {
                for (int y = bestY - 1; y <= bestY + 1; y++) {
                    if (x < 0 || y < 0 || x >= width || y >= height || set[y][x]
                            || (x == bestX && y == bestY)) {
                        continue;
                    }

                    // Here is where we are iterating over all neighbors
                    int alt = val + grid[y][x];
                    if (alt < distFibHeap.getVal(to_index(y, x))) {
                        // Need to update
                        distFibHeap.decreaseKey(to_index(y, x), alt);
                        previous[y][x] = toDir(bestX, bestY, x, y);
                    }

                }
            }
            if (iters >= width * height - 1) {
                done = true;
            }
            iters++;
        }

        Dijkstra.finished = true;
        return;
    }

    private static int toDir(int endX, int endY, int startX, int startY) {
        int diffX = startX - endX;
        int diffY = startY - endY;

        // Match directions which starts at north and goes clockwise
        
        switch (diffX)  {
            case 1:
                switch (diffY) {
                    case -1:
                        return 7;
                    case 0:
                        return 6;
                    case 1:
                        return 5;
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
