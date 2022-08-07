package de.ddm.profiler;

import static org.junit.Assert.assertEquals;

import java.util.*;

import org.junit.Test;

import de.ddm.structures.HeapColumnArray;
import de.ddm.structures.Value;

public class HeapColumnArrayTest {

    @Test
    public void testSetValue() {
        HeapColumnArray testArray = new HeapColumnArray();
        List<Value.WithPosition> testList = Arrays.asList(new Value.WithPosition(Value.fromInt(1), 0),
                new Value.WithPosition(Value.fromInt(2), 1));
        List<Value> expectedArray = Arrays.asList((Value) Value.EMPTY, Value.EMPTY);
        assertEquals(expectedArray, testArray.setValues(testList.stream()));
        List<Value> secondExpectedArray = Arrays.asList(Value.fromInt(1), Value.fromInt(2));
        ;
        assertEquals(secondExpectedArray, testArray.setValues(testList.stream()));
    }
}
