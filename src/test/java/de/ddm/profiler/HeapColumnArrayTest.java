package de.ddm.profiler;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.util.*;

import org.junit.Test;

public class HeapColumnArrayTest {
  
    @Test
    public void testSetValue() {
        HeapColumnArray testArray = new HeapColumnArray();
        List<ValueWithPosition> testList = Arrays.asList(new ValueWithPosition(new SimpleValue(1),0),new ValueWithPosition(new SimpleValue(2),1));
        List<Value> expectedArray  = Arrays.asList( (Value)null,null);
        assertEquals(expectedArray, testArray.setValue(testList));
        List<Value> secondExpectedArray = Arrays.asList( new SimpleValue(1),new SimpleValue(2));;
        assertEquals(secondExpectedArray, testArray.setValue(testList));
    }
}
