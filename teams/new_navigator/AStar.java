package new_navigator;

import java.util.Arrays;

import battlecode.common.Clock;
import battlecode.common.MapLocation;

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

    /*
     * This is the heuristic which is the road cost times the L_inf norm with target
     */
    public static int heuristic(int y, int x) {
        int deltaX = Math.abs(x - target.x);
        int deltaY = Math.abs(y - target.y);
        return Math.max(deltaX, deltaY) * 7;
    }

    /*
     * Setup AStar algorithm
     */
    public static void setupAStar(int[][] _grid, MapLocation start, MapLocation _target,
            int _height, int _width, MapLocation topLeft) {
        gridHeight = _grid.length;
        gridWidth = _grid[0].length;
        myHeight = _height;
        myWidth = _width;
        topLeftX = topLeft.x;
        topLeftY = topLeft.y;
        target = _target;
        previous = new int[gridHeight][gridWidth];
        visited = new boolean[gridHeight][gridWidth];
        finished = false;
        grid = _grid;
        distFibHeap = new FibHeap(_height * _width);

        distFibHeap.decreaseKey(to_index(start.x, start.y), 0);
    }

    /*
     * This converts global coordinates to local index
     */
    private static int to_index(int x, int y) {
        int localX = x - topLeftX;
        int localY = y - topLeftY;
        int index = localY * myWidth + localX;
        return index;
    }

    public static void doAStar() {
        while (true) {
            // Find the position with minimum distance
            int val_index = distFibHeap.extractMin();
            int index = val_index % MAX_VERTS;
            int val = val_index / MAX_VERTS;

            // We keep in fib heap as a relative value, but need to get true value
            int relativeX = index % myWidth;
            int relativeY = index / myWidth;
            int bestX = relativeX + topLeftX;
            int bestY = relativeY + topLeftY;
            visited[bestY][bestX] = true;

            if (bestY == target.y && bestX == target.x) {
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
                    int i = to_index(left, up);
                    if (alt < distFibHeap.getVal(i)) {
                        distFibHeap.decreaseKey(i, alt);
                        previous[up][left] = 3;
                    }
                }
                if (!visited[bestY][left]) {
                    int alt = val + grid[bestY][left] + heuristic(bestY, left);
                    int i = to_index(left, bestY);
                    if (alt < distFibHeap.getVal(i)) {
                        distFibHeap.decreaseKey(i, alt);
                        previous[bestY][left] = 2;
                    }
                }
                if (down < gridHeight && !visited[down][left]) {
                    int alt = val + (int) ((double) grid[down][left] * 1.4) + heuristic(down, left);
                    int i = to_index(left, down);
                    if (alt < distFibHeap.getVal(i)) {
                        distFibHeap.decreaseKey(i, alt);
                        previous[down][left] = 1;
                    }
                }
            }

            if (up >= 0 && !visited[up][bestX]) {
                int alt = val + grid[up][bestX] + heuristic(up, bestX);
                int i = to_index(bestX, up);
                if (alt < distFibHeap.getVal(i)) {
                    distFibHeap.decreaseKey(i, alt);
                    previous[up][bestX] = 4;
                }
            }
            if (down < gridHeight && !visited[down][bestX]) {
                int alt = val + grid[down][bestX] + heuristic(down, bestX);
                int i = to_index(bestX, down);
                if (alt < distFibHeap.getVal(i)) {
                    distFibHeap.decreaseKey(i, alt);
                    previous[down][bestX] = 0;
                }
            }

            if (right < gridWidth) {
                if (up >= 0 && !visited[up][right]) {
                    int alt = val + (int) ((double) grid[up][right] * 1.4) + heuristic(up, right);
                    int i = to_index(right, up);
                    if (alt < distFibHeap.getVal(i)) {
                        distFibHeap.decreaseKey(i, alt);
                        previous[up][right] = 5;
                    }
                }
                if (!visited[bestY][right]) {
                    int alt = val + grid[bestY][right] + heuristic(bestY, right);
                    int i = to_index(right, bestY);
                    if (alt < distFibHeap.getVal(i)) {
                        distFibHeap.decreaseKey(i, alt);
                        previous[bestY][right] = 6;
                    }
                }
                if (down < gridHeight && !visited[down][right]) {
                    int alt = val + (int) ((double) grid[down][right] * 1.4)
                            + heuristic(down, right);
                    int i = to_index(right, down);
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