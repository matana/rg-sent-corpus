package de.uni_koeln.info.misc;

import java.io.File;
import java.io.IOException;
import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.uni_koeln.info.extraction.Reader;



public class AllegraCorpusReader {
	
	private static Pattern phoneRgx = Pattern.compile("((tel.{4}|fax|mobil|service|numer|gratuit)(\\.|:|\\.:)?)?\\s?\\+?[0-9()\\s]{11,18}", Pattern.CASE_INSENSITIVE);
	private static Pattern emailRgx = Pattern.compile("[a-z0-9._-]+@[a-z0-9.-]+\\.[a-z]{2,6}", Pattern.CASE_INSENSITIVE);
	private static Pattern urlRgx = Pattern.compile("(https?)://[a-z.]*[a-z0-9+&#/%=\\.\\-~_]", Pattern.CASE_INSENSITIVE);
	private static Pattern dataDoubleDashRgx = Pattern.compile("Data:", Pattern.CASE_INSENSITIVE);
	private static Pattern wwwRgx = Pattern.compile("www.", Pattern.CASE_INSENSITIVE);
	
	public static void main(String[] args) throws IOException {
		File file = new File("ALLEGRA_corpus/rm");
		File[] listFiles = file.listFiles();
		System.out.println(listFiles.length + " documents in ALLEGRA corpus");
		List<String> list = new ArrayList<>();
		Pattern pattern = Pattern.compile("\\.[A-Z]\\w+");
		for (File f : listFiles) {
			String content = Reader.getContent(f);
			BreakIterator sentenceIterator = BreakIterator.getSentenceInstance();
			sentenceIterator.setText(content);
			int start = sentenceIterator.first();
			for (int end = sentenceIterator.next(); end != BreakIterator.DONE; start = end, end = sentenceIterator.next()) {
				// System.out.printf("[%s]: %s \n",  count++, content.substring(start, end));
				String substring = content.substring(start, end);
				
				Matcher matcher = pattern.matcher(substring);
				
				int left = 0;
				int subCount = 0;
				int right = 0;
				while(matcher.find()) {
					right =  matcher.start() + 1;
					String substring2 = substring.substring(left, right);
//					System.out.println("subSent: " + substring2);
					list.add(substring2);
					left = right;
					subCount++;
				}
				if(subCount == 1) {
					String substring2 = substring.substring(left, substring.length());
//					System.out.println("subSent: " + substring2);
					list.add(substring2);
				} else if(subCount == 0) {
					list.add(substring);
				}
			}
		}
		
		List<String> toReturn = new ArrayList<>();
		
		for (String sentence : list) {
			
			Matcher email = emailRgx.matcher(sentence);
			Matcher url = urlRgx.matcher(sentence);
			Matcher phone = phoneRgx.matcher(sentence);
			Matcher dataDoubleDash = dataDoubleDashRgx.matcher(sentence);
			Matcher www = wwwRgx.matcher(sentence);
			
			if (email.find() || url.find() || phone.find() || dataDoubleDash.find() || www.find()) {
//				 System.out.println(sentence);
				continue;
			}
			toReturn.add(sentence);
		}
		int total = 0;
		for (String string : toReturn) {
			
			if(total % 10 == 0)
				System.out.println();
			// System.out.println("[" + total + "] " + string);
			System.out.println(string);
			total++;
		}
	}

}
