package edu.albany.ir.test;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.shingle.ShingleAnalyzerWrapper;
import org.apache.lucene.analysis.snowball.SnowballAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import edu.albany.ir.lucene.MyAnalyzerNGram;

public class IndexTest {

	public static void main(String[] args) {

		String indexPath = "/home/jak/smart/cran/lucene_index";

		Date start = new Date();
		try {
			System.out.println("Indexing file '" + indexPath + "'...");

			Directory dir = FSDirectory.open(new File(indexPath));
			// Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_31);
			Analyzer analyzer = new MyAnalyzerNGram();
			//Analyzer analyzer = new ShingleAnalyzerWrapper(Version.LUCENE_31, 3, 5);
			//Analyzer analyzer = new ShingleAnalyzerWrapper(new SnowballAnalyzer(Version.LUCENE_31,"English"), 2, 5);
			//Analyzer analyzer = new SnowballAnalyzer(Version.LUCENE_31,"English");

			IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_31, analyzer);
			iwc.setOpenMode(OpenMode.CREATE);

			IndexWriter writer = new IndexWriter(dir, iwc);

			String id = "1";
			String content = "jak akdemir benim adim senin adin ne?";
			Document document = new Document();
			document.add(new Field("id", id, Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS));			
			document.add(new Field("content", content, Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.YES));
			writer.addDocument(document);

			System.out.println("Content: " + content);
			System.out.println("Writing document: " + id);
			writer.close();

			Date end = new Date();
			System.out.println(end.getTime() - start.getTime() + " total milliseconds");

		} catch (IOException e) {
			System.out.println(" caught a " + e.getClass() + "\n with message: " + e.getMessage());
		}
	}

}
