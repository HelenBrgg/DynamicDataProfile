package de.ddm.profiler;

import lombok.AllArgsConstructor;


public class Value {
    private String s;

    public static Value fromOriginal(String original){}
    public static Value fromInt(int i){}
    public static Value fromString(String s){}

    public int compare(Value a, Value b);
    public byte[] getLongHash(){};
    public String toString(){};
    
}
