package hq_search;

public class FibHeap {
    int              size;
    int[]            heap;
    int[]            locations;
    static final int INFINITY  = 999;
    static final int MAX_VERTS = 100;

    public FibHeap(int _size) {
        int guess = 1;
        while (guess - 1 < _size) {
            guess *= 2;
        }
        size = guess - 1;
        heap = new int[size];
        locations = new int[size];
        for (int x = 0; x < size; x++) {
            heap[x] = INFINITY * MAX_VERTS + x;
            locations[x] = x;
        }
    }

    public int getVal(int ID) {
        int loc = locations[ID];
        return heap[loc];
    }

    public void decreaseKey(int ID, int newVal) {
        int location = locations[ID];

        // Update the value in the heap
        int newItem = ID + MAX_VERTS * newVal;
        heap[location] = newItem;

        // Heapify by going up the tree
        while (location > 0 && newItem < heap[(location - 1) / 2]) {
            // Update the location table and do swap
            int otherID = heap[(location - 1) / 2] % MAX_VERTS;
            locations[ID] = (location - 1) / 2;
            locations[otherID] = location;
            heap[location] = heap[(location - 1) / 2];
            heap[(location - 1) / 2] = newItem;

            // Recurse
            location = (location - 1) / 2;
        }
    }

    public int extractMin() {
        int minID = heap[0] % MAX_VERTS;

        // Once we remove the min, we must recurse down to reheapify
        int location = 0;
        while (location < size / 2) {
            int leftVal = heap[(location + 1) * 2 - 1];
            int rightVal = heap[(location + 1) * 2];
            int locToPull = (location + 1) * 2 - 1;
            if (leftVal > rightVal) {
                locToPull++;
            }
            int IDToPull = heap[locToPull] % MAX_VERTS;

            // Do the move
            heap[location] = heap[locToPull];
            locations[IDToPull] = location;

            // Recurse
            location = locToPull;
        }
        // Make it bigger than anything else since at bottom
        heap[location] = INFINITY * MAX_VERTS * 2;
        return minID;
    }

}
