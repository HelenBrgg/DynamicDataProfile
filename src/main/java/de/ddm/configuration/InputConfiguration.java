package de.ddm.configuration;

import lombok.Data;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

@Data
public class InputConfiguration {
	private String inputPath = "data" + File.separator + "TPCH";
	private boolean fileHasHeader = true;
	private Charset charset = StandardCharsets.UTF_8;
	private char valueSeparator = ';';
	private char valueQuote = '"';
	private char valueEscape = '\\';
	private boolean valueStrictQuotes = false;
	private boolean valueIgnoreLeadingWhitespace = false;
	private String dataGeneratorArgs = "10000 18096 0.0";

	public void update(CommandMaster commandMaster) {
		this.fileHasHeader = commandMaster.fileHasHeader;
		this.charset = commandMaster.charset;
		this.valueSeparator = commandMaster.attributeSeparator;
		this.valueQuote = commandMaster.attributeQuote;
		this.valueEscape = commandMaster.attributeEscape;
		this.valueStrictQuotes = commandMaster.attributeStrictQuotes;
		this.valueIgnoreLeadingWhitespace = commandMaster.attributeIgnoreLeadingWhitespace;
		this.dataGeneratorArgs = commandMaster.dataGeneratorArgs;
	}

	public List<String> getInputFilePaths() {
		return Stream.of(new File(this.inputPath).listFiles())
			.map(file -> file.getAbsolutePath())
			.collect(Collectors.toList());
	}

	public String[] getDataGeneratorEnv() {
		return new String[]{ String.format("CSV_SEPARATOR='%s'", this.valueSeparator) };
	}
	public List<String[]> getDataGeneratorCommands() {
		return this.getInputFilePaths().stream()
			.map((String filepath) -> {
				ArrayList<String> arr = new ArrayList<>(List.of("scripts/datagenerator.py", filepath));
				arr.addAll(List.of(this.dataGeneratorArgs.split(" ")));
				return arr.toArray(new String[arr.size()]);
			})
			.collect(Collectors.toList());
	}
}
