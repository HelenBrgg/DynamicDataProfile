package de.ddm.profiler;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public class Value implements Comparable<Value> {
    private String s;

    private Value(String s){
        this.s = s;
    }

    public static Value fromOriginal(String original){
        if(original.length() > 64){
           return new Value( "$" +original.length()+"$"+ Integer.toString(original.hashCode()));
        }
        return new Value(original);
    }
    public static Value fromInt(int i){
        return new Value(Integer.toString(i));
    }
    public static Value fromString(String s){
        return new Value(s);
    }

    @Override
    public int compareTo(Value b){
        return this.s.compareTo(b.s);
    };
    //public byte[] getLongHash(){};
    public String toString(){
    return s;
    }
}
