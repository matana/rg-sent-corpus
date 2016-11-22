package de.uni_koeln.info.lucene;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.UUID;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.hunspell.Dictionary;
import org.apache.lucene.analysis.hunspell.HunspellStemFilter;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SimpleFSDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class Indexer {

	@Autowired private Searcher searcher;
	
	private Logger logger = LoggerFactory.getLogger(getClass());
	private IndexWriter writer;
	
	public int index(File txtFile) {
		try {
			getAnalyzer();
			
			// Ignore possible duplicates
			Set<String> lines = new HashSet<>(readLines(txtFile));
			
			List<Document> docs = new ArrayList<>();
			logger.info("indexing... " + txtFile.getName());
			for (String line : lines) {
				
				FieldType fieldType = new FieldType();
				fieldType.setStoreTermVectorOffsets(true);
				fieldType.setStoreTermVectorPositions(true);
				fieldType.setStoreTermVectors(true);
				fieldType.setStored(true);
				fieldType.setTokenized(true);
				fieldType.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS);
				
				Document document = new Document();
				document.add(new Field("content", line, fieldType));
				document.add(new StringField("id", UUID.randomUUID().toString() + line.hashCode(), Field.Store.YES));
				document.add(new StringField("keywords", "", Field.Store.YES));
				document.add(new StringField("source", txtFile.getName(), Field.Store.YES));
				
				SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
				document.add(new StringField("created", dateFormat.format(new Date()), Field.Store.YES));
				
				logger.info("adding doc : " + document.toString());
				docs.add(document);
			}
			writer.addDocuments(docs);
			int numDocs = writer.numDocs();
			writer.close();
			logger.info("index has " + numDocs + " items");
			return numDocs;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return -1;
	}

	private List<String> readLines(File txtFile) throws FileNotFoundException {
		Scanner scanner = new Scanner(txtFile);
		List<String> lines = new ArrayList<>();
		while(scanner.hasNextLine()) {
			lines.add(scanner.nextLine());
		}
		scanner.close();
		return lines;
	}

	private StandardAnalyzer getAnalyzer() throws IOException {
		Directory dir = new SimpleFSDirectory(new File("index").toPath());
		StandardAnalyzer analyzer = new StandardAnalyzer(new CharArraySet(new ArrayList<String>(), true)); // Ignore default stopWord list
		IndexWriterConfig writerConfig = new IndexWriterConfig(analyzer);
		this.writer = new IndexWriter(dir, writerConfig);
		return analyzer;
	}

	String getStemmedContent(Analyzer analyzer, String contents) throws IOException, ParseException {
		
		BufferedInputStream aff = new BufferedInputStream(new FileInputStream(new File("hunspell-rg/clean_out.aff")));
		BufferedInputStream dic = new BufferedInputStream(new FileInputStream(new File("hunspell-rg/clean_out.dic")));
		Dictionary dictionary = new Dictionary(aff, dic);
		
		TokenStream tokenStream = analyzer.tokenStream("contents", contents);
		tokenStream.reset();

		HunspellStemFilter filter = new HunspellStemFilter(tokenStream, dictionary);
		CharTermAttribute term = filter.getAttribute(CharTermAttribute.class);

		StringBuilder sb = new StringBuilder();
		while (filter.incrementToken()) {
			sb.append(term.toString()).append(" ");
		}
		filter.close();
		tokenStream.close();
		
		return sb.toString();
	}

	/**
	 * @return the number of indexed documents.
	 */
	public int getNumDocs() {
		return writer.numDocs();
	}

	public void deleteIndex() {
		try {
			this.writer.deleteAll();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public boolean isAvailable() {
		return writer.isOpen();
	}

	public boolean containsHash(String hash) throws IOException {
		return searcher.getHashes().contains(hash);
	}

}
