package hq_search;

public class FibHeap {
    int              size;
    long[]           heap;
    int[]            locations;
    static final int INFINITY  = 2 ^ 10;
    static final int MAX_VERTS = 2 ^ 32;

    /*
     * Structure of heap: heap is implemented as an array of longs.
     * 
     * Each element of the heap contains the value and id. It is stored as VALUE * MAX_VERTS + ID
     * That way they are separated into high bits for VALUE and low bits for ID which enables
     * sorting
     * 
     * 
     * The location array is indexed by the ID of an element and the value is the location in the
     * heap. This provides O(1) access into the heap for get and set operations
     */

    public FibHeap(int _size) {

        // Make the size of the heap into the first power of 2 - 1 that is bigger than we need
        int guess = 1;
        while (guess - 1 <= _size) {
            guess *= 2;
        }
        size = guess - 1;

        // Instantiate the heap and locations array
        heap = new long[size];
        locations = new int[size];
        for (int x = size - 1; x >= 0; x--) {
            // Set all values in the heap to be INFINITY and locations are trivial
            heap[x] = INFINITY * MAX_VERTS + x;
            locations[x] = x;
        }
    }

    // Return the high bits containing the VALUE
    public int getVal(int ID) {
        int loc = locations[ID];
        return ((int) heap[loc]) / MAX_VERTS;
    }

    /*
     * To decrease a value in the heap, we use the location array to find the item Then, once we
     * have it, we can reduce and reheapify. To heapify, we just recurse up to the root or until we
     * can stop with heaping. Each of these is fast because O(1) access into heap. This gives an
     * O(log n) decreaseKey
     */

    public void decreaseKey(int ID, int newVal) {
        int location = locations[ID];

        // Update the value in the heap
        int newItem = ID + (MAX_VERTS * newVal);
        heap[location] = newItem;

        // Heapify by going up the tree
        while (location > 0 && newItem < heap[(location - 1) / 2]) {
            // Update the location table and do swap
            int otherID = ((int) heap[(location - 1) / 2]) % MAX_VERTS;
            locations[ID] = (location - 1) / 2;
            locations[otherID] = location;
            heap[location] = heap[(location - 1) / 2];
            heap[(location - 1) / 2] = newItem;

            // Recurse
            location = (location - 1) / 2;
        }
    }

    /*
     * To extract min, just pop the top off the heap and reheapify while maintaining the location
     * array. This runs in O(log n)
     */
    public int extractMin() {
        int minID = ((int) heap[0]) % MAX_VERTS;

        // Once we remove the min, we must recurse down to reheapify
        int location = 0;
        while (location < size / 2) {
            long leftVal = heap[(location + 1) * 2 - 1];
            long rightVal = heap[(location + 1) * 2];
            int locToPull = (location + 1) * 2;
            if (leftVal < rightVal) {
                locToPull--;
            }
            int IDToPull = ((int) heap[locToPull]) % MAX_VERTS;

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