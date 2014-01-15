package hq_search;

public class AStar {
    static int         width       = 0;
    static int         height      = 0;
    static int         targetX     = 0;
    static int         targetY     = 0;
    static int[][]     previous    = null;
    static boolean     finished    = false;
    static final int   UNSET       = 9;
    static int[][]     grid        = null;
    static boolean[][] visited     = null;
    static FibHeap     distFibHeap = null;
    static final int   MAX_VERTS   = FibHeap.MAX_VERTS;

    public static void setupAStar(int[][] _grid, int start_x, int start_y, int target_x,
            int target_y) {
        height = _grid.length;
        width = _grid[0].length;
        previous = new int[height][width];
        visited = new boolean[height][width];
        finished = false;
        grid = _grid;
        distFibHeap = new FibHeap(height * width);
        targetX = target_x;
        targetY = target_y;

        distFibHeap.decreaseKey(to_index(start_y, start_x), 0);
    }

    private static int to_index(int start_y, int start_x) {
        return start_y * width + start_x;
    }

    private static int heuristic(int x, int y) {
        int deltaX = Math.abs(x - targetX);
        int deltaY = Math.abs(y - targetY);
        return Math.max(deltaX, deltaY);
    }

    public static void doAStar() {
        while (true) {

            // Find the position with minimum distance
            int val_index = distFibHeap.extractMin();
            int index = val_index % MAX_VERTS;
            int val = val_index / MAX_VERTS;
            int bestX = index % width;
            int bestY = index / width;
            visited[bestY][bestX] = true;
            if (bestX == targetX && bestY == targetY) {
                break;
            }

            // Iterated over all neighbors
            int left = bestX - 1;
            int right = bestX + 1;
            int up = bestY - 1;
            int down = bestY + 1;

            if (left >= 0) {
                if (up >= 0 && !visited[up][left]) {
                    int alt = val + (int) ((double) grid[up][left] * 1.4) + heuristic(up, left);
                    int i = up * width + left;
                    if (alt < distFibHeap.getVal(i)) {
                        distFibHeap.decreaseKey(i, alt);
                        previous[up][left] = 3;
                    }
                }
                if (!visited[bestY][left]) {
                    int alt = val + grid[bestY][left] + heuristic(bestY, left);
                    int i = bestY * width + left;
                    if (alt < distFibHeap.getVal(i)) {
                        distFibHeap.decreaseKey(i, alt);
                        previous[bestY][left] = 2;
                    }
                }
                if (down < height && !visited[down][left]) {
                    int alt = val + (int) ((double) grid[down][left] * 1.4) + heuristic(down, left);
                    int i = down * width + left;
                    if (alt < distFibHeap.getVal(i)) {
                        distFibHeap.decreaseKey(i, alt);
                        previous[down][left] = 1;
                    }
                }
            }

            if (up >= 0 && !visited[up][bestX]) {
                int alt = val + grid[up][bestX] + heuristic(up, bestX);
                int i = up * width + bestX;
                if (alt < distFibHeap.getVal(i)) {
                    distFibHeap.decreaseKey(i, alt);
                    previous[up][bestX] = 4;
                }
            }
            if (down < height && !visited[down][bestX]) {
                int alt = val + grid[down][bestX] + heuristic(down, bestX);
                int i = down * width + bestX;
                if (alt < distFibHeap.getVal(i)) {
                    distFibHeap.decreaseKey(i, alt);
                    previous[down][bestX] = 0;
                }
            }

            if (right < width) {
                if (up >= 0 && !visited[up][right]) {
                    int alt = val + (int) ((double) grid[up][right] * 1.4) + heuristic(up, right);
                    int i = up * width + right;
                    if (alt < distFibHeap.getVal(i)) {
                        distFibHeap.decreaseKey(i, alt);
                        previous[up][right] = 5;
                    }
                }
                if (!visited[bestY][right]) {
                    int alt = val + grid[bestY][right] + heuristic(bestY, right);
                    int i = bestY * width + right;
                    if (alt < distFibHeap.getVal(i)) {
                        distFibHeap.decreaseKey(i, alt);
                        previous[bestY][right] = 6;
                    }
                }
                if (down < height && !visited[down][right]) {
                    int alt = val + (int) ((double) grid[down][right] * 1.4)
                            + heuristic(down, right);
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