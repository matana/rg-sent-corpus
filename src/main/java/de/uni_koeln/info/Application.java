package de.uni_koeln.info;

import java.io.File;
import java.io.IOException;

import org.apache.tika.exception.TikaException;
import org.apache.xmlbeans.XmlException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.core.io.ClassPathResource;
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
		File liaCorpus = new ClassPathResource("lia_rg_sents.txt").getFile();
		File allegraCorpus = new ClassPathResource("ALLEGRA_corpus_rg_sents.txt").getFile();
		indexer.index(liaCorpus);
		indexer.index(allegraCorpus);
		
		SpringApplication.run(Application.class, args);
	}

}
