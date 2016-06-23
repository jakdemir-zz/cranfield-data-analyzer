package edu.albany.ir.example;
/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.NumericField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;

/**
 * Index all text files under a directory.
 * <p>
 * This is a command-line application demonstrating simple Lucene indexing. Run
 * it with no command-line arguments for usage information.
 */
public class IndexFiles {

	private IndexFiles() {
	}

	/** Index all text files under a directory. */
	public static void main(String[] args) {
		String usage = "java org.apache.lucene.demo.IndexFiles"
				+ " [-index INDEX_PATH] [-docs DOCS_PATH] [-update]\n\n"
				+ "This indexes the documents in DOCS_PATH, creating a Lucene index"
				+ "in INDEX_PATH that can be searched with SearchFiles";
		String indexPath = "index";
		String docsPath = "TREC";
		boolean create = true;
		for (int i = 0; i < args.length; i++) {
			if ("-index".equals(args[i])) {
				indexPath = args[i + 1];
				i++;
			} else if ("-docs".equals(args[i])) {
				docsPath = args[i + 1];
				i++;
			} else if ("-update".equals(args[i])) {
				create = false;
			}
		}

		if (docsPath == null) {
			System.err.println("Usage: " + usage);
			System.exit(1);
		}

		final File docDir = new File(docsPath);
		if (!docDir.exists() || !docDir.canRead()) {
			System.out
					.println("Document directory '"
							+ docDir.getAbsolutePath()
							+ "' does not exist or is not readable, please check the path");
			System.exit(1);
		}

		Date start = new Date();
		try {
			System.out.println("Indexing to directory '" + indexPath + "'...");

			Directory dir = FSDirectory.open(new File(indexPath));
			Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_31);
			IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_31,
					analyzer);
			create = true;

			if (create) {
				// Create a new index in the directory, removing any
				// previously indexed documents:
				iwc.setOpenMode(OpenMode.CREATE);
			} else {
				// Add new documents to an existing index:
				iwc.setOpenMode(OpenMode.CREATE_OR_APPEND);
			}

			// Optional: for better indexing performance, if you
			// are indexing many documents, increase the RAM
			// buffer. But if you do this, increase the max heap
			// size to the JVM (eg add -Xmx512m or -Xmx1g):
			//
			// iwc.setRAMBufferSizeMB(256.0);

			IndexWriter writer = new IndexWriter(dir, iwc);
			indexDocs(writer, docDir);

			// NOTE: if you want to maximize search performance,
			// you can optionally call optimize here. This can be
			// a costly operation, so generally it's only worth
			// it when your index is relatively static (ie you're
			// done adding documents to it):
			//
			// writer.optimize();

			writer.close();

			Date end = new Date();
			System.out.println(end.getTime() - start.getTime()
					+ " total milliseconds");

		} catch (IOException e) {
			System.out.println(" caught a " + e.getClass()
					+ "\n with message: " + e.getMessage());
		}
	}

	/**
	 * Indexes the given file using the given writer, or if a directory is
	 * given, recurses over files and directories found under the given
	 * directory.
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
			if (file.isDirectory()) {
				String[] files = file.list();
				// an IO error could occur
				if (files != null) {
					for (int i = 0; i < files.length; i++) {
						indexDocs(writer, new File(file, files[i]));
					}
				}
			} else {

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

					// our code
					// *********************************************************
					String record = null;
					int a, b, stringNum = 0, i = 0;
					// String[] docContents = new String[1000];
					// String[] docNos = new String[10000];
					String docName = null;
					// make a new, empty document
					Document doc = new Document();

					BufferedReader reader = new BufferedReader(
							new InputStreamReader(fis));

					record = new String();
					while ((record = reader.readLine()) != null) {
						a = record.lastIndexOf("<DOCNO>");
						b = record.indexOf("</DOCNO>");

						if (a >= 0 && b > 0) // if this line contains the DOCNO
						{
							stringNum++;
							// docNos[stringNum] = record.substring(a+7,b-1);
							docName = record.substring(a + 7, b).trim();
							// add a document
							if (stringNum >= 1) {
								// index previous document
								if (stringNum >= 2)
									writer.addDocument(doc);

								// start new document
								doc = new Document();
								// doc.add(new Field("path", file.getPath()+
								// "/"+docName,
								// Add the path of the file as a field named
								// "path". Use a
								// field that is indexed (i.e. searchable), but
								// don't tokenize
								// the field into separate words and don't index
								// term frequency
								// or positional information:
								Field pathField = new Field("path", docName,
										Field.Store.YES,
										Field.Index.NOT_ANALYZED_NO_NORMS);
								pathField.setOmitTermFreqAndPositions(true);
								doc.add(pathField);
								// doc.add(new Field("path", docName,
								// Field.Store.YES,
								// Field.Index.UN_TOKENIZED));
								// System.out.println("adding " +
								// file.getPath()+ "/"+docName);
								System.out.println("adding " + docName);

								// Add the last modified date of the file a
								// field named "modified".
								// Use a NumericField that is indexed (i.e.
								// efficiently filterable with
								// NumericRangeFilter). This indexes to
								// milli-second resolution, which
								// is often too fine. You could instead create a
								// number based on
								// year/month/day/hour/minutes/seconds, down the
								// resolution you require.
								// For example the long value 2011021714 would
								// mean
								// February 17, 2011, 2-3 PM.
								NumericField modifiedField = new NumericField(
										"modified");
								modifiedField.setLongValue(file.lastModified());
								doc.add(modifiedField);

								// doc.add(new Field("modified",
								// DateField.timeToString(file.lastModified()),
								// Field.Store.YES,
								// Field.Index.UN_TOKENIZED));
							}
						} else {

							doc.add(new Field("contents", record,
									Field.Store.YES, Field.Index.ANALYZED, // tokenized
									Field.TermVector.YES));
							// docContents[stringNum] = docContents[stringNum] +
							// record;
							// add contents to document
							// Add the contents of the file to a field named
							// "contents". Specify a Reader,
							// so that the text of the file is tokenized and
							// indexed, but not stored.
							// Note that FileReader expects the file to be in
							// UTF-8 encoding.
							// If that's not the case searching for special
							// characters will fail.
							// doc.add(new Field("contents", new
							// BufferedReader(new InputStreamReader(fis,
							// "UTF-8"))));
						}
						a = 0;
						b = 0;
					}

					if (writer.getConfig().getOpenMode() == OpenMode.CREATE) {
						// New index, so we just add the document (no old
						// document can be there):
						System.out.println("adding " + docName);
						writer.addDocument(doc);
					} else {
						// Existing index (an old copy of this document may have
						// been indexed) so
						// we use updateDocument instead to replace the old one
						// matching the exact
						// path, if present:
						System.out.println("updating " + file);
						writer.updateDocument(new Term("path", file.getPath()),
								doc);
					}

				} finally {
					fis.close();
				}
			}
		}
	}
}
