package de.ddm.profiler;

    /**
    * interface of the value of a table
    */
public interface Value {
    String getText();
    byte[] getLongHash();

    /**
     * default function
     *  returns the text of the value as an integer
    * @return the integer
    */

    default int toInt(){
        return Integer.parseInt(getText());
    }
    
}
