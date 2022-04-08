package de.ddm;

import de.ddm.profiler.*;

public class DummyMain {
	public static void main(String[] args) {
		Table table = Table.parseCSV(
             "ID,Origin,Destination,Day\n" +
             "1,Mercury,Venus,2\n" +
             "2,Venus,Earth,3\n" +
             "3,Earth,Earth,4\n" +
             "4,Moon,Earth,2\n" +
             "5,Mars,Moon,1"
        );

        Source source = new DummySource(List.of(table));



        List<Candidate> candidates = /* */

        System.out.println("# Candidates");
        candidates.forEach(|cand| System.out.println(cand));


        System.out.println("# Validation");
	}
}
