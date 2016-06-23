package edu.albany.ir.cran;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.PerFieldAnalyzerWrapper;
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
import edu.albany.ir.lucene.MyAnalyzerPure;

public class IndexCran {

	/*
	 * .I 1 .T experimental investigation of the aerodynamics of a wing in a
	 * slipstream . .A brenckman,m. .B j. ae. scs. 25, 1958, 324. .W
	 * experimental investigation of the aerodynamics of a wing in a slipstream
	 * . an experimental study of a wing in a propeller slipstream was made in
	 * order to determine the spanwise distribution of the lift increase due to
	 * slipstream at different angles of attack of the wing and at different
	 * free stream to slipstream velocity ratios . the results were intended in
	 * part as an evaluation basis for different theoretical treatments of this
	 * problem . the comparative span loading curves, together with supporting
	 * evidence, showed that a substantial part of the lift increment produced
	 * by the slipstream was due to a /destalling/ or boundary-layer-control
	 * effect . the integrated remaining lift increment, after subtracting this
	 * destalling lift, was found to agree well with a potential flow theory .
	 * an empirical evaluation of the destalling effects was made for the
	 * specific configuration of the experiment .
	 */

	public static void main(String[] args) {

		String indexPath = "/home/jak/smart/cran/lucene_index";
		String docsPath = "/home/jak/smart/cran/cran.all";

		final File docDir = new File(docsPath);

		Date start = new Date();
		try {
			System.out.println("Indexing file '" + indexPath + "'...");

			Directory dir = FSDirectory.open(new File(indexPath));
			// Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_31);
//			 Analyzer analyzer = new MyAnalyzerNGram();
//			 Analyzer analyzer = new MyAnalyzerPure();
			// Analyzer analyzer = new
			// ShingleAnalyzerWrapper(Version.LUCENE_31,2,5);
			// Analyzer analyzer = new ShingleAnalyzerWrapper(new
			// SnowballAnalyzer(Version.LUCENE_31,"English"), 2, 5);
			// Analyzer analyzer = new
			// SnowballAnalyzer(Version.LUCENE_31,"English");

			PerFieldAnalyzerWrapper analyzer = new PerFieldAnalyzerWrapper(new MyAnalyzerPure());
			analyzer.addAnalyzer(Constants.CONTENT_INDEX_NGRAM, new MyAnalyzerNGram());
			analyzer.addAnalyzer(Constants.TITLE_INDEX_NGRAM, new MyAnalyzerNGram());

			IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_33, analyzer);

			// Create a new index in the directory, removing any
			// previously indexed documents:
			iwc.setOpenMode(OpenMode.CREATE);

			IndexWriter writer = new IndexWriter(dir, iwc);
			indexDocs(writer, docDir);

			writer.close();

			Date end = new Date();
			System.out.println(end.getTime() - start.getTime() + " total milliseconds");

		} catch (IOException e) {
			System.out.println(" caught a " + e.getClass() + "\n with message: " + e.getMessage());
		}
	}

	static void indexDocs(IndexWriter writer, File file) throws IOException {
		// do not try to index files that cannot be read
		if (file.canRead()) {
			FileInputStream fis;
			try {
				fis = new FileInputStream(file);
			} catch (FileNotFoundException fnfe) {
				// at least on windows, some temporary files raise this
				// exception with an "access denied" message
				// checking if the file can be read doesn't help
				fnfe.printStackTrace();
				return;
			}

			try {
				BufferedReader br = new BufferedReader(new InputStreamReader(fis));
				String line = null;

				// 0 = id
				// 1 = title
				// 2 = Author
				// 3 = Date
				// 4 = Content
				int key = 0;

				String id = "";
				String title = "";
				String author = "";
				String date = "";
				String content = "";

				while ((line = br.readLine()) != null) {
					if (line.startsWith(Constants.ID_DOC)) {
						if (!content.equals("")) {
							content = content + " " +title;
							Document document = new Document();
							document.add(new Field(Constants.ID_INDEX, id, Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS));
							document.add(new Field(Constants.TITLE_INDEX, title, Field.Store.YES, Field.Index.ANALYZED,
									Field.TermVector.YES));
							document.add(new Field(Constants.TITLE_INDEX_NGRAM, title, Field.Store.YES, Field.Index.ANALYZED,
									Field.TermVector.YES));
							document.add(new Field(Constants.AUTHOR_INDEX, author, Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS));
							document.add(new Field(Constants.DATE_INDEX, date, Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS));
							document.add(new Field(Constants.CONTENT_INDEX, content, Field.Store.YES, Field.Index.ANALYZED,
									Field.TermVector.YES));
							document.add(new Field(Constants.CONTENT_INDEX_NGRAM, content, Field.Store.YES, Field.Index.ANALYZED,
									Field.TermVector.YES));
							
							System.out.println("Content: " + id + "\n" + title + "\n" + author + "\n" + date + "\n" + content + "\n");
							System.out.println("Writing document: " + id);
							writer.addDocument(document);

						}
						id = line.split(" ")[1];
						key = 0;

						title = "";
						author = "";
						date = "";
						content = "";
					} else if (line.startsWith(Constants.TITLE_DOC)) {
						key = 1;
					} else if (line.startsWith(Constants.AUTHOR_DOC)) {
						key = 2;
					} else if (line.startsWith(Constants.DATE_DOC)) {
						key = 3;
					} else if (line.startsWith(Constants.CONTENT_DOC)) {
						key = 4;
					} else {
						if (key == 0) {

						} else if (key == 1) {
							title = title + " " + line;
						} else if (key == 2) {
							author = author + " " + line;
						} else if (key == 3) {
							date = date + " " + line;
						} else if (key == 4) {
							content = content + " " + line;
						}
					}

				}

				// LAST document --- sorry for bad practice
				Document document = new Document();
				document.add(new Field(Constants.ID_INDEX, id, Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS));
				document.add(new Field(Constants.TITLE_INDEX, title, Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.YES));
				document.add(new Field(Constants.TITLE_INDEX_NGRAM, title, Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.YES));
				document.add(new Field(Constants.AUTHOR_INDEX, author, Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS));
				document.add(new Field(Constants.DATE_INDEX, date, Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS));
				document.add(new Field(Constants.CONTENT_INDEX, content, Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.YES));
				document.add(new Field(Constants.CONTENT_INDEX_NGRAM, content, Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.YES));
				writer.addDocument(document);

				System.out.println("Content: " + content);
				System.out.println("Writing document: " + id);

			} finally {
				fis.close();
			}
		}
	}
}
