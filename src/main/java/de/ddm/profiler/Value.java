package de.ddm.profiler;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public class Value implements Comparable<Value> {
    public static Value NULL = Value.fromString("");

    private String shortstr;

    private Value(String shortstr) {
        this.shortstr = shortstr;
    }

    public static Value fromString(String str) {
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

    // public byte[] getLongHash(){};
    public String toString() {
        return this.shortstr;
    }
}
