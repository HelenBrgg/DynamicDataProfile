package de.ddm.structures;

import lombok.NoArgsConstructor;

// TODO should this be associated with a value?
public interface Datatype {
    boolean isSubtype(Datatype other);

    @NoArgsConstructor
    static class String implements Datatype {
        @Override
        public java.lang.String toString() {
            return "string";
        }
        @Override
        public boolean isSubtype(Datatype other) {
            return true;
        }
    }

    @NoArgsConstructor
    static class Number extends String {
        @Override
        public java.lang.String toString() {
            return "number";
        }
        @Override
        public boolean isSubtype(Datatype other) {
            return other instanceof Number;
        }
    }

    @NoArgsConstructor
    static class Integer extends Number {
        @Override
        public java.lang.String toString() {
            return "integer";
        }
        @Override
        public boolean isSubtype(Datatype other) {
            return other instanceof Integer;
        }
    }

    @NoArgsConstructor
    static class Timestamp extends String {
        @Override
        public java.lang.String toString() {
            return "timestamp";
        }
        @Override
        public boolean isSubtype(Datatype other) {
            return other instanceof Timestamp;
        }
    }

    static Datatype inferType(Value val) {
        if (val.toString().matches("\\d+")) {
            return new Integer();
        }
        if (val.toString().matches("\\d+[.]\\d*") || val.toString().matches("\\d*[.]\\d+")) {
            return new Number();
        }
        if (val.toString().matches("\\d{4}-\\d{2}-\\d{2}((-|\\s)\\d{2}:\\d{2}(:\\d{2})?)?")) {
            return new Timestamp();
        }
        return new String();
    }
}
