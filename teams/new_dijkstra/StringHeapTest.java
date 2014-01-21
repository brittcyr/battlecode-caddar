package new_dijkstra;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class StringHeapTest {
    public StringHeap h;

    @Before
    public void setUp() throws Exception {
        h = new StringHeap(50, 50);
    }

    @Test
    public void testEmpty() {
        int minVal = h.getMinVal();
        assertEquals(minVal, 999);
    }

    @Test
    public void testFirstDecreaseKey() {
        h.decreaseKey(0, 0);
        int minVal = h.getMinVal();
        int minKey = h.extractMin();
        assertEquals(minVal, 0);
        assertEquals(minKey, 0);
    }

    @Test
    public void testSecondDecreaseSameKey() {
        h.decreaseKey(0, 10);
        int minVal = h.getMinVal();
        assertEquals(minVal, 10);
        h.decreaseKey(0, 0);
        minVal = h.getMinVal();
        int minKey = h.extractMin();
        assertEquals(minVal, 0);
        assertEquals(minKey, 0);
    }

    @Test
    public void testGetBigVal() {
        int minVal = h.getVal(123);
        assertEquals(minVal, 999);
    }

    @Test
    public void testGetBigValAfterDecrease() {
        h.decreaseKey(0, 0);
        int minVal = h.getVal(123);
        assertEquals(minVal, 999);
    }

    @Test
    public void testOutOfBounds() {
        try {
            h.getVal(10000);
            fail();
        }
        catch (Exception e) {
            assertEquals(e.getClass(), java.lang.NumberFormatException.class);
        }
    }

    @Test
    public void testManyDecrease() {
        for (int x = 999; x > 0; x--) {
            h.decreaseKey(0, x);
            int minVal = h.getMinVal();
            assertEquals(minVal, x);
        }
        h.extractMin();
        h.decreaseKey(1, 0);
        for (int x = 999; x > 0; x--) {
            h.decreaseKey(2, x);
            int minVal = h.getMinVal();
            assertEquals(minVal, 0);
        }
    }

    @Test
    public void testOverlapManyDecrease() {
        for (int x = 995; x > 0; x--) {
            h.decreaseKey(0, x);
            h.decreaseKey(1, x + 1);
            h.decreaseKey(2, x + 2);
            h.decreaseKey(3, x + 3);
            h.decreaseKey(4, x + 3);
            int minVal = h.getMinVal();
            assertEquals(minVal, x);
            int oneVal = h.getVal(1);
            int twoVal = h.getVal(2);
            int threeVal = h.getVal(3);
            int fourVal = h.getVal(4);
            assertEquals(oneVal, x + 1);
            assertEquals(twoVal, x + 2);
            assertEquals(threeVal, x + 3);
            assertEquals(fourVal, x + 3);
        }
    }

    @Test
    public void testIndexConversion() {
        int index = h.xyToInt((char) 10, (char) 10);
        assertEquals(index, 909);
        h.decreaseKey(index, 0);
        assertEquals(index, h.extractMin());
        String key = h.keyToString(index);
        assertEquals(key, "Z" + (char) 10 + (char) 10);
    }

    @Test
    public void testIndexConversion2() {
        int index = h.xyToInt((char) 1, (char) 25);
        assertEquals(index, 24);
        String key = h.keyToString(index);
        assertEquals(key, "Z" + (char) 1 + (char) 25);
    }

}
