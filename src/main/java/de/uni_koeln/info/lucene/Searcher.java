package de.uni_koeln.info.lucene;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.highlight.Fragmenter;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.search.highlight.SimpleSpanFragmenter;
import org.apache.lucene.search.highlight.TokenSources;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SimpleFSDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import de.uni_koeln.info.lucene.json.ContextResponse;

@Service
public class Searcher {

	private int totalHits;
	
	private Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 * @return totalHits in current search
	 */
	public int getTotalHits() {
		return totalHits;
	}

	/**
	 * @param totalHits
	 */
	public void setTotalHits(int totalHits) {
		this.totalHits = totalHits;
	}
	
	/**
	 * 
	 * @param key
	 * @return
	 * @throws IOException
	 * @throws ParseException
	 * @throws InvalidTokenOffsetsException
	 */
	public List<ContextResponse> searchAndHighlight(String key) throws IOException, ParseException, InvalidTokenOffsetsException {
		
		List<ContextResponse> toReturn = new ArrayList<>();
		Directory dir = new SimpleFSDirectory(new File("index").toPath());
		DirectoryReader dirReader = DirectoryReader.open(dir);
		IndexSearcher searcher =  new IndexSearcher(dirReader);
		
		QueryParser parser = new QueryParser("content", new StandardAnalyzer());
		
		// Retriev exact hits and record the ids...
		Query queryExact = parser.parse(key);
		
		ScoreDoc[] scoreDocs = searcher.search(queryExact, 10).scoreDocs;
		List<Integer> exactIds = new ArrayList<>();
		for (ScoreDoc scoreDoc : scoreDocs) {
			exactIds.add(scoreDoc.doc);
		}

		// Fuzzy query
		String[] split = key.split(" ");
		String fuzzy = "";
		
		if(split.length > 0) {
			for (int i = 0; i < split.length; i++) {
				fuzzy += (i < split.length -1) ? split[i] + "~ AND " : split[i] + "~";
			}
		} else
			fuzzy = key + "~0.7";
			
		logger.debug("q: " + fuzzy);
		Query queryFuzzy = parser.parse(fuzzy);
		scoreDocs = searcher.search(queryFuzzy, 10).scoreDocs;
		
		//  ...then boost exact matches
		for (Integer id : exactIds) {
			for (ScoreDoc scoreDoc : scoreDocs) {
				if(scoreDoc.doc == id)
					scoreDoc.score += 1; 
			}
		}
		
		QueryScorer queryScorer = new QueryScorer(queryFuzzy, "content");
		Fragmenter fragmenter = new SimpleSpanFragmenter(queryScorer);
		SimpleHTMLFormatter htmlFormatter = new SimpleHTMLFormatter("<span class=\"key\">", "</span>");
		Highlighter highlighter = new Highlighter(htmlFormatter, queryScorer);
		highlighter.setTextFragmenter(fragmenter);
		
        for (ScoreDoc scoreDoc : scoreDocs) {
        	Document document = searcher.doc(scoreDoc.doc);
            String content = document.get("content");
			TokenStream tokenStream = TokenSources.getTokenStream("content", dirReader.getTermVectors(scoreDoc.doc), content, new StandardAnalyzer(), -1);
			String context = highlighter.getBestFragment(tokenStream, content);
			toReturn.add(new ContextResponse(document.get("id"), context, scoreDoc.score, document.get("keywords"), 
					document.get("source"), document.get("created")));
        }
		return toReturn;
	}
	
	List<String> getHashes() throws IOException {
		Directory dir = new SimpleFSDirectory(new File("index").toPath());
		DirectoryReader dirReader = DirectoryReader.open(dir);
		int  numDocs = dirReader.numDocs();
		List<String> toReturn = new ArrayList<String>();
		while(numDocs > -1) {
			Document document = dirReader.document(numDocs);
			toReturn.add(document.get("hash"));
			numDocs--;
		}
		return toReturn;
	}

}
