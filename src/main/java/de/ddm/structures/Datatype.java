package de.ddm.structures;

import lombok.NoArgsConstructor;

// TODO should this be associated with a value?
public interface Datatype {
    boolean isSubtype(Datatype other);

    @NoArgsConstructor
    static class String implements Datatype {
        @Override
        public boolean isSubtype(Datatype other) {
            return true;
        }
    }

    @NoArgsConstructor
    static class Number extends String {
        @Override
        public boolean isSubtype(Datatype other) {
            return other instanceof Number;
        }
    }

    @NoArgsConstructor
    static class Integer extends Number {
        @Override
        public boolean isSubtype(Datatype other) {
            return other instanceof Integer;
        }
    }

    @NoArgsConstructor
    static class Timestamp extends String {
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
        // TODO infer timestamps
        return new String();
    }
}
