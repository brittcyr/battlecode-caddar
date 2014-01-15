package new_navigator;

public class AStar {
    static int         gridWidth   = 0;
    static int         gridHeight  = 0;
    static int         myHeight;
    static int         myWidth;
    static int         topLeftX;
    static int         topLeftY;
    static int[][]     previous    = null;
    static boolean     finished    = false;
    static final int   UNSET       = 9;
    static int[][]     grid        = null;
    static boolean[][] visited     = null;
    static FibHeap     distFibHeap = null;
    static final int   MAX_VERTS   = FibHeap.MAX_VERTS;
    static MapLocation target;

    public static void setupAStar(int[][] _grid, MapLocation start, MapLocation _target,
            int _height, int _width, int _topLeftX, int _topLeftY) {
        gridHeight = _grid.length;
        gridWidth = _grid[0].length;
        myHeight = _height;
        myWidth = _width;
        topLeftX = _topLeftX;
        topLeftY = _topLeftY;
        target = _target;
        previous = new int[gridHeight][gridWidth];
        visited = new boolean[gridHeight][gridWidth];
        finished = false;
        grid = _grid;
        distFibHeap = new FibHeap(_height * _width);

        // TODO: get the index of the target some other way
        distFibHeap.decreaseKey(to_index(start.x, start.y), 0);
    }

    // TODO: Implement heuristic function

    public static void doAStar() {
        while (true) {
            // Find the position with minimum distance
            int val_index = distFibHeap.extractMin();
            int index = val_index % MAX_VERTS;
            int val = val_index / MAX_VERTS;

            // We keep in fib heap as a relative value, but need to get true value
            int relativeX = val_index % myWidth;
            int relativeY = val_index / myWidth;
            int bestX = relativeX + topLeftX;
            int bestY = relativeY + topLeftY;
            visited[bestY][bestX] = true;

            // TODO: make the break condition if we hit the target

            // Iterated over all neighbors
            int left = bestX - 1;
            int right = bestX + 1;
            int up = bestY - 1;
            int down = bestY + 1;

            // TODO: Introduce a conversion because grid and visited are not same
            // as the key in the fib heap
            if (left >= 0) {
                if (up >= 0 && !visited[up][left]) {
                    int alt = val + (int) ((double) grid[up][left] * 1.4);
                    int i = up * width + left;
                    // TODO: Convert i to a relative index
                    if (alt < distFibHeap.getVal(i)) {
                        distFibHeap.decreaseKey(i, alt);
                        previous[up][left] = 3;
                    }
                }
                if (!visited[bestY][left]) {
                    int alt = val + grid[bestY][left];
                    int i = bestY * width + left;
                    if (alt < distFibHeap.getVal(i)) {
                        distFibHeap.decreaseKey(i, alt);
                        previous[bestY][left] = 2;
                    }
                }
                if (down < height && !visited[down][left]) {
                    int alt = val + (int) ((double) grid[down][left] * 1.4);
                    int i = down * width + left;
                    if (alt < distFibHeap.getVal(i)) {
                        distFibHeap.decreaseKey(i, alt);
                        previous[down][left] = 1;
                    }
                }
            }

            if (up >= 0 && !visited[up][bestX]) {
                int alt = val + grid[up][bestX];
                int i = up * width + bestX;
                if (alt < distFibHeap.getVal(i)) {
                    distFibHeap.decreaseKey(i, alt);
                    previous[up][bestX] = 4;
                }
            }
            if (down < height && !visited[down][bestX]) {
                int alt = val + grid[down][bestX];
                int i = down * width + bestX;
                if (alt < distFibHeap.getVal(i)) {
                    distFibHeap.decreaseKey(i, alt);
                    previous[down][bestX] = 0;
                }
            }

            if (right < width) {
                if (up >= 0 && !visited[up][right]) {
                    int alt = val + (int) ((double) grid[up][right] * 1.4);
                    int i = up * width + right;
                    if (alt < distFibHeap.getVal(i)) { // Need to update
                        distFibHeap.decreaseKey(i, alt);
                        previous[up][right] = 5;
                    }
                }
                if (!visited[bestY][right]) {
                    int alt = val + grid[bestY][right];
                    int i = bestY * width + right;
                    if (alt < distFibHeap.getVal(i)) {
                        distFibHeap.decreaseKey(i, alt);
                        previous[bestY][right] = 6;
                    }
                }
                if (down < height && !visited[down][right]) {
                    int alt = val + (int) ((double) grid[down][right] * 1.4);
                    int i = down * width + right;
                    if (alt < distFibHeap.getVal(i)) {
                        distFibHeap.decreaseKey(i, alt);
                        previous[down][right] = 7;
                    }
                }
            }
        }
        AStar.finished = true;
    }
}