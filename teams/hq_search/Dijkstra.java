package hq_search;

import battlecode.common.Clock;


public class Dijkstra {
    static int[][]     tentativeDistances = null;
    static int         width              = 0;
    static int         height             = 0;
    static boolean[][] visited            = null;
    static int[][]     previous           = null;
    static boolean     finished           = false;
    static final int   INFINITY           = 999999999;
    static final int   UNSET              = 999999;
    static int[][]     grid               = null;
    static boolean[][] set                = null;
    static FibHeap     distFibHeap        = null;

    public static void setupDijkstra(int[][] _grid, int start_x, int start_y) {
        height = _grid.length;
        width = _grid[0].length;
        tentativeDistances = new int[height][width];
        visited = new boolean[height][width];
        previous = new int[height][width];
        set = new boolean[height][width];
        finished = false;
        grid = _grid;
        distFibHeap = new FibHeap(height * width);

        // Initialize tentative distances to infinity except zero at source
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < width; y++) {
                tentativeDistances[y][x] = INFINITY;
                previous[y][x] = UNSET;
                set[y][x] = false;
            }
        }
        tentativeDistances[start_y][start_x] = 0;
        return;
    }

    public static void doDijkstra() {
        boolean done = false;
        int bytes = Clock.getBytecodesLeft();

        while (!done) {
            if (Clock.getBytecodesLeft() < 1000 || Clock.getBytecodesLeft() < bytes / 2) {
                return;
            }
            bytes = Clock.getBytecodesLeft();

            // TODO: Use a better priority queue for distance
            // Find the position with minimum distance
            int best = INFINITY;
            int bestX = 0;
            int bestY = 0;
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    if ((tentativeDistances[y][x] <= best) && !set[y][x]) {
                        best = tentativeDistances[y][x];
                        bestX = x;
                        bestY = y;
                    }
                }
            }
            set[bestY][bestX] = true;

            if (best == INFINITY) {
                // This is the case where it is not one connected component
                // Should not happen
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
                    int alt = tentativeDistances[bestY][bestX] + grid[y][x];
                    if (alt < tentativeDistances[y][x]) {
                        // Need to update
                        tentativeDistances[y][x] = alt;
                        previous[y][x] = toDir(bestX, bestY, x, y);
                    }

                }
            }

            // Detect if we are done
            done = true;
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    if (set[y][x] == false)
                        done = false;
                }
            }
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
