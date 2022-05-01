package de.ddm.profiler;

import lombok.NoArgsConstructor;

public interface Datatype {
    boolean isSubsetOf(Datatype other);

    @NoArgsConstructor
    public static class String implements Datatype {
        @Override
        boolean isSubsetOf(Datatype other){
            return true;
        }
    }

    @NoArgsConstructor
    public static class Number extends String {
        @Override
        boolean isSubsetOf(Datatype other){
            return other instanceof Number;
        }
    }

    @NoArgsConstructor
    public static class Integer extends Number {
        @Override
        boolean isSubsetOf(Datatype other){
            return other instanceof Integer;
        }
    }

    @NoArgsConstructor
    public static class Timestamp extends String {
        @Override
        boolean isSubsetOf(Datatype other){
            return other instanceof Timestamp;
        }
    }

    public static Datatype inferType(Value val){
        if val.matches("\d+"){
            return new Integer();
        }
        if val.matches("[\d+[.]\d*") || val.matches("\d*[.]\d+") {
            // TODO allow exponent-expressions
            return new Number();
        }
        // TODO infer timestamps
        return new String();
    }
}
