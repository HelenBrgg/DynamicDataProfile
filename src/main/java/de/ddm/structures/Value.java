package de.ddm.structures;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@EqualsAndHashCode
public class Value implements Comparable<Value> {
    public static Value EMPTY = new Value("");

    /**
    * Contains: 
    * - a value
    * - the position of the value in a column 
    * @see  Value
    */
    @Getter
    @AllArgsConstructor
    public static class WithPosition {
        public Value value;
        public int position;
    }

    private String shortstr;

    private Value(String shortstr) {
        this.shortstr = shortstr;
    }

    public static Value fromString(String unstrippedStr) {
        String str = unstrippedStr.strip();
        if (str.isEmpty()) return EMPTY;
        if (str.charAt(0) != '$' && str.length() > 64) {
            return new Value("$" + str.length() + "$" + Integer.toString(str.hashCode()));
        }
        return new Value(str);
    }

    public static Value fromInt(int i) {
        return new Value(Integer.toString(i));
    }

    @Override
    public int compareTo(Value b) {
        return this.shortstr.compareTo(b.shortstr); // TODO compare object refs?
    };

    public boolean isLessThan(Value b){
        return this.compareTo(b) < 0;
    }
    public boolean isGreaterThan(Value b){
        return this.compareTo(b) > 0;
    }

    public String toString() {
        return this.shortstr;
    }
}
