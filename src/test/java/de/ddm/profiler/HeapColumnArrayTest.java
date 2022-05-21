package de.ddm.profiler;

import static org.junit.Assert.assertEquals;

import java.util.*;

import org.junit.Test;

public class HeapColumnArrayTest {

    @Test
    public void testSetValue() {
        HeapColumnArray testArray = new HeapColumnArray();
        List<ValueWithPosition> testList = Arrays.asList(new ValueWithPosition(Value.fromInt(1), 0),
                new ValueWithPosition(Value.fromInt(2), 1));
        List<Value> expectedArray = Arrays.asList((Value) Value.NULL, Value.NULL);
        assertEquals(expectedArray, testArray.setValue(testList));
        List<Value> secondExpectedArray = Arrays.asList(Value.fromInt(1), Value.fromInt(2));
        ;
        assertEquals(secondExpectedArray, testArray.setValue(testList));
    }
}
