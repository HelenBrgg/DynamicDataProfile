package de.ddm.profiler;

import lombok.NoArgsConstructor;

public interface Datatype {
    boolean isSubsetOf(Datatype other);

    @NoArgsConstructor
    public static class String implements Datatype {
        @Override
        public boolean isSubsetOf(Datatype other) {
            return true;
        }
    }

    @NoArgsConstructor
    public static class Number extends String {
        @Override
        public boolean isSubsetOf(Datatype other) {
            return other instanceof Number;
        }
    }

    @NoArgsConstructor
    public static class Integer extends Number {
        @Override
        public boolean isSubsetOf(Datatype other) {
            return other instanceof Integer;
        }
    }

    @NoArgsConstructor
    public static class Timestamp extends String {
        @Override
        public boolean isSubsetOf(Datatype other) {
            return other instanceof Timestamp;
        }
    }

    public static Datatype inferType(Value val) {
        if (val.toString().matches("\\d+")) {
            return new Integer();
        }
        if (val.toString().matches("\\d+[.]\\d*") || val.toString().matches("\\d*[.]\\d+")) {
            // TODO allow exponent-expressions
            return new Number();
        }
        // TODO infer timestamps
        return new String();
    }
}
