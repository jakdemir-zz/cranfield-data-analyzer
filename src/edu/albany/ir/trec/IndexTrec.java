package edu.albany.ir.trec;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.PerFieldAnalyzerWrapper;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.NumericField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import edu.albany.ir.lucene.MyAnalyzerNGram;
import edu.albany.ir.lucene.MyAnalyzerPure;

public class IndexTrec {

	private IndexTrec() {
	}

	/** Index all text files under a directory. */
	public static void main(String[] args) {
		String indexPath = "/usr/java/Training_TREC/lucene_index/";
		String docsPath = "/usr/java/Training_TREC/Training_Data/";

		final File docDir = new File(docsPath);

		Date start = new Date();
		try {
			System.out.println("Indexing to directory '" + indexPath + "'...");

			Directory dir = FSDirectory.open(new File(indexPath));
			PerFieldAnalyzerWrapper analyzer = new PerFieldAnalyzerWrapper(new MyAnalyzerPure());
			analyzer.addAnalyzer(Constants.CONTENT_INDEX_NGRAM, new MyAnalyzerNGram());
			analyzer.addAnalyzer(Constants.HEADLINE_INDEX_NGRAM, new MyAnalyzerNGram());

			IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_33, analyzer);

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

	// <DOC>
	// <DOCNO>FT911-4016</DOCNO>
	// <PROFILE>_AN-BDVB1AB5FT</PROFILE>
	// <DATE>910422
	// </DATE>
	// <HEADLINE>
	// FT 22 APR 91 / The Queen's Awards For Export and Technology 1991: The
	// export experts line up - From marmalade to mites, Andrew Jack surveys
	// some
	// of the winners
	// </HEADLINE>
	// <BYLINE>
	// By ANDREW JACK
	// </BYLINE>
	// <TEXT>
	// asdasd
	// </TEXT>
	// <PUB>The Financial Times
	// </PUB>
	// <PAGE>
	// London Page 9 Illustration (Omitted). Illustration (Omitted).
	// Illustration
	// (Omitted).
	// </PAGE>
	// </DOC>

	static void indexDocs(IndexWriter writer, File file) throws IOException {
		// do not try to index files that cannot be read
		if (file.canRead()) {
			if (file.isDirectory()) {
				String[] files = file.list();
				// an IO error could occur
				if (files != null) {
					for (int i = 0; i < files.length; i++) {
						indexDocs(writer, new File(file, files[i]));
					}
				}
			} else {
				FileInputStream fis = new FileInputStream(file);

				try {
					BufferedReader br = new BufferedReader(new InputStreamReader(fis));
					String line = null;

					int state = 0;
					String id = "";
					String headline = "";
					String content = "";
					
					while ((line = br.readLine()) != null) {

						if (line.contains(Constants.DOC_START_DOC)) {
							
							if (!content.equals("")) {
								Document document = new Document();
								document.add(new Field(Constants.PATH_INDEX, id, Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS));
								document.add(new Field(Constants.HEADLINE_INDEX, headline, Field.Store.YES, Field.Index.ANALYZED,
										Field.TermVector.YES));
								document.add(new Field(Constants.CONTENT_INDEX, content, Field.Store.YES, Field.Index.ANALYZED,
										Field.TermVector.YES));
								document.add(new Field(Constants.HEADLINE_INDEX_NGRAM, headline, Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.YES));
								document.add(new Field(Constants.CONTENT_INDEX_NGRAM, content, Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.YES));

								//System.out.println("Content: " + id + "\n" + headline + "\n" + content + "\n");
								System.out.println("Content: " + id);
								writer.addDocument(document);

							}
							id = line.subSequence(line.indexOf(Constants.DOC_START_DOC)+7, line.indexOf(Constants.DOC_END_DOC)).toString();
							headline = "";
							content = "";

							state = 1;
						} else if (line.contains(Constants.DOC_END_DOC)) {
							state = 2;
						} else if (line.contains(Constants.HEADLINE_START_DOC)) {
							state = 3;
						} else if (line.contains(Constants.HEADLINE_END_DOC)) {
							state = 4;
						} else if (line.contains(Constants.BY_START_DOC)) {
							state = 5;
						} else if (line.contains(Constants.BY_END_DOC)) {
							state = 6;
						} else if (line.contains(Constants.TEXT_START_DOC)) {
							state = 7;
						} else if (line.contains(Constants.TEXT_END_DOC)) {
							state = 8;
						} else if (line.contains(Constants.PUB_START_DOC)) {
							state = 9;
						} else if (line.contains(Constants.PUB_END_DOC)) {
							state = 10;
						} else if (line.contains(Constants.PAGE_START_DOC)) {
							state = 11;
						} else if (line.contains(Constants.PAGE_END_DOC)) {
							state = 12;
						} else {
							if (state == 3) {
								headline = headline + " " + line;
							} else if (state == 5 || state == 7 || state == 9 || state == 11) {
								content = content + " " + line;
							}
						}
					}

					Document document = new Document();
					document.add(new Field(Constants.PATH_INDEX, id, Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS));
					document.add(new Field(Constants.HEADLINE_INDEX, headline, Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.YES));
					document.add(new Field(Constants.CONTENT_INDEX, content, Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.YES));
					document.add(new Field(Constants.HEADLINE_INDEX_NGRAM, headline, Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.YES));
					document.add(new Field(Constants.CONTENT_INDEX_NGRAM, content, Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.YES));

					//System.out.println("Content: " + id + "\n" + headline + "\n" + content + "\n");
					writer.addDocument(document);
				} finally {
					fis.close();
				}
			}
		}
	}
}
