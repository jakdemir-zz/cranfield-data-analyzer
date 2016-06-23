package edu.albany.ir.time;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.NumericField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

public class IndexTime {
	public static void main(String[] args) {

		String indexPath = "/home/jak/smart/time/lucene_index";
		String docsPath = "/home/jak/smart/time/doc.text";

		final File docDir = new File(docsPath);

		Date start = new Date();
		try {
			System.out.println("Indexing to directory '" + indexPath + "'...");

			Directory dir = FSDirectory.open(new File(indexPath));
			Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_31,new File("/home/jak/smart/time/stopword"));
			IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_31, analyzer);

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

	/**
	 * Indexes the given file using the given writer
	 * 
	 * NOTE: This method indexes one document per input file. This is slow. For
	 * good throughput, put multiple documents into your input file(s). An
	 * example of this is in the benchmark module, which can create "line doc"
	 * files, one document per line, using the <a href=
	 * "../../../../../contrib-benchmark/org/apache/lucene/benchmark/byTask/tasks/WriteLineDocTask.html"
	 * >WriteLineDocTask</a>.
	 * 
	 * @param writer
	 *            Writer to the index where the given file/dir info will be
	 *            stored
	 * @param file
	 *            The file to index, or the directory to recurse into to find
	 *            files to index
	 * @throws IOException
	 */
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
				String content = "";
				int id=0;

				Document document = new Document();

				while (!(line = br.readLine()).equals(Constants.STOP)) {
					if (line.startsWith(Constants.HEAD)) {
						//First write old content collected line by line
						if (!content.equals("")) {
							document.add(new Field(Constants.CONTENT, content, Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.YES));
							
							System.out.println("Content " + content);
							System.out.println("Writing document: " + id);
							writer.addDocument(document);
							content="";
						}
						// *TEXT 017 01/04/63 PAGE 020
						String[] headOfContent = line.split(" ");

						System.out.println("Creating document : "+id +" : "+ line);

						//Create a new document head
						document = new Document();
						document.add(new Field(Constants.ID, String.valueOf(id), Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS));
						document.add(new Field(Constants.NATURAL_ID, headOfContent[1], Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS));
						document.add(new Field(Constants.DATE, headOfContent[2], Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS));
						document.add(new Field(Constants.PAGE, headOfContent[3], Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS));

						id++;
					} else {
						content = content + line;
					}
				}
				//LAST document --- sorry for bad practice
				document.add(new Field(Constants.CONTENT, content, Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.YES));
				
				System.out.println("Content " + content);
				System.out.println("Writing document: " + id);
				writer.addDocument(document);
				
			} finally {
				fis.close();
			}
		}
	}
}
