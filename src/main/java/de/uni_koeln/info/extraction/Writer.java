package de.uni_koeln.info.extraction;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Writer {

	/**
	 * This routine writes the extracted sentences into a plain text file. Each
	 * line represents a sentence.
	 */
	public static void writeSentenceFile(File file, List<String> data) throws IOException {
		BufferedWriter bw = new BufferedWriter(new FileWriter(file));
		for (int i = 0; i < data.size(); i++) {
			bw.write(data.get(i));
			bw.newLine();
		}
		bw.close();
		
		System.out.println(Writer.class.getName() + "		|	ouput: '" + file + "'");
	
	}
	
	public static void main(String[] args) {
		
		Pattern chfRgx = Pattern.compile("CHF ", Pattern.CASE_INSENSITIVE);
		Set<String> finalVers = new HashSet<>();
		try {
			List<String> readFile = Reader.readFile(new File("preFinal_lucene_input.txt"));
			for (String sent : readFile) {
				Matcher matcher = chfRgx.matcher(sent);
				if(!matcher.find()) {
					finalVers.add(sent);
				}
			}
			for (String string : finalVers) {
				System.out.println(string);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
