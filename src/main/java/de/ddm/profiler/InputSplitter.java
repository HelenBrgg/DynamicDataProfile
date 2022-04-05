package de.ddm.profiler;
import java.util.ArrayList;
import java.util.List;


public class InputSplitter {
    public Source source;
    public int numWorker;
    public InputSplitter(Source source, int numWorker){
        this.source = source;
        this.numWorker= numWorker;
   }

    public List<SetCommand> splitTable(Table table){
        List<SetCommand> setCommands = new ArrayList<>();
            for(int i = 1; i<=table.attributes.size(); i++){
                SetCommand setCommand = new SetCommand();
                setCommand.values= new ArrayList<>();
                setCommand.attribute = table.attributes.get(i);
                Column column = table.columns.get(i);
                for(int j = 0; j < column.values.size(); j++){
                    Value value = column.values.get(i);
                    int position = table.columns.get(0).values.get(j).toInt();
                    setCommand.values.add(new ValueWithPosition(value,position));
                }
                setCommand.workerID =(i-1) % numWorker;
                setCommands.add(setCommand);
            }
        return setCommands;
    }


    
}
