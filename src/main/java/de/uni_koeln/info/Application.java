package de.uni_koeln.info;

import java.io.File;
import java.io.IOException;

import org.apache.tika.exception.TikaException;
import org.apache.xmlbeans.XmlException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.xml.sax.SAXException;

import de.uni_koeln.info.lucene.Indexer;

@SpringBootApplication
public class Application extends SpringBootServletInitializer {

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(Application.class);
	}

	public static void main(String[] args) throws IOException, SAXException, TikaException, XmlException {
		
		// Delete index on start up...
		File[] listFiles = new File("index").listFiles();
		for (File file : listFiles) {
			file.delete();
		}
		// ...and reindex data
		Indexer indexer = new Indexer();
		indexer.index(new File("lucene/lia_rg_sents.txt"));
		indexer.index(new File("lucene/ALLEGRA_corpus_rg_sents.txt"));
		
		SpringApplication.run(Application.class, args);
	}

}
