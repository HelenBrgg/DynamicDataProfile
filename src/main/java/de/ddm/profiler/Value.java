package de.ddm.profiler;


public interface Value {
    String getText();
    byte[] getLongHash();

    default int toInt(){
        return Integer.parseInt(getText());
    }
    
}
