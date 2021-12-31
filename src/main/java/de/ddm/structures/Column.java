package de.ddm.structures;
import de.ddm.serialization.AkkaSerializable;

import java.util.*;
import java.util.stream.Stream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

// Table column which uses optimized skip-list storage
public class Column implements AkkaSerializable {

    // This is the column data saved as indicies into valuesByPosition instead of their actual values.
    private List<Integer> data = new ArrayList<>();
    private Set<Integer> uniqueValueHashes = new HashSet<>();

    public Column(){}

    public void add(String value){
        int valueHash = value.hashCode();
        data.add(valueHash);
        uniqueValueHashes.add(valueHash);
    }

    public int size() {
        return this.data.size();
    }

    public int getMemorySize(){
        return this.data.size() * 4;
    }

    public Set<Integer> getUniqueValueHashes() {
        return this.uniqueValueHashes;
    }

}
