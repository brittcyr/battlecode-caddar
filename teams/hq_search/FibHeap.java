package hq_search;

public class FibHeap {
    int   size;
    int[] heap;
    int[] locations;
    static final int INFINITY = 999999999;

    public FibHeap(int _size) {
        int guess = 1;
        while (guess < _size) {
            guess *= 2;
        }
        size = guess;
        heap = new int[size];
        locations = new int[size];
        for (int x = 0; x < size; x++) {
            heap[x] = INFINITY * 10000 + x;
            locations[x] = x;
        }
    }

    public void decreaseKey(int ID, int newVal) {
        int location = locations[ID];
        int oldVal = heap[location];

        // Update the value in the heap
        int newItem = (oldVal % 10000) + 10000 * newVal;
        heap[location] = newItem;

        // Heapify by going up the tree
        while (location > 0) {
            if (newItem < heap[(location - 1) / 2]) {
                // Update the location table and do swap
                int otherID = heap[(location - 1) / 2] % 10000;
                locations[ID] = (location - 1) / 2;
                locations[otherID] = location;
                heap[location] = heap[(location - 1) / 2];
                heap[(location - 1) / 2] = newItem;

                // Recurse
                location = (location - 1) / 2;
            }
            else {
                break;
            }
        }
    }

    public int extractMin() {
        int minVal = heap[0] / 10000;

        // Once we remove the min, we must recurse down to reheapify
        int location = 0;
        while (location < size / 2) {
            int leftVal = heap[(location + 1) * 2 - 1];
            int rightVal = heap[(location + 1) * 2];
            int locToPull = (location + 1) * 2 - 1;
            if (leftVal > rightVal) {
                locToPull++;
            }
            int IDToPull = heap[locToPull] % 10000;

            // Do the move
            heap[location] = heap[locToPull];
            locations[IDToPull] = location;

            // Recurse
            location = locToPull;
        }
        // Make it bigger than anything else since at bottom
        heap[location] = INFINITY * 10000 * 2;
        return minVal;
    }

}
