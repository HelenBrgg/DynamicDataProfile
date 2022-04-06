package de.ddm.profiler;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;

/**
    * implements the interface of the value of a table
    * Value is stored as a text
    * @see value
    */

@AllArgsConstructor @EqualsAndHashCode
public class SimpleValue implements Value {
    public String text;
    public SimpleValue(int i){
        text=Integer.toString(i);
    }
    /**
    * returns the value as a hashvalue 
    * @return the hashvalue
    */

    @Override
    public byte[] getLongHash() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * returns the text of the Value
    * @return text
    */

    @Override
    public String getText() {
        // TODO Auto-generated method stub
        return null;
    }
    
}
