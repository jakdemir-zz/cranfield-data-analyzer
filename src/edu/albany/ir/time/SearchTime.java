package edu.albany.ir.time;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;
import java.io.BufferedWriter;
import java.io.FileWriter;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

public class SearchTime {

	private SearchTime() {
	}

	public static void main(String[] args) throws CorruptIndexException, IOException, ParseException {
		IndexSearcher searcher = new IndexSearcher(FSDirectory.open(new File("/home/jak/smart/time/lucene_index")));
		Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_31, new File("/home/jak/smart/time/stopword"));
		QueryParser parser = new QueryParser(Version.LUCENE_31, Constants.CONTENT, analyzer);

		String q0 = "jak her havuzun dibi aynidir";
		String q1 = "KENNEDY ADMINISTRATION PRESSURE ON NGO DINH DIEM TO STOP SUPPRESSING THE BUDDHISTS";
		String q2 = "EFFORTS OF AMBASSADOR HENRY CABOT LODGE TO GET VIET NAM'S PRESIDENT DIEM TO CHANGE HIS POLICIES OF POLITICAL REPRESSION";
		String q3 = "NUMBER OF TROOPS THE UNITED STATES HAS STATIONED IN SOUTH VIET NAM AS COMPARED WITH THE NUMBER OF TROOPS IT HAS STATIONED IN WEST GERMANY";
		String q7="REJECTION BY PRINCE NORODOM SIHANOUK, AN ASIAN NEUTRALIST LEADER, OF ALL FURTHER U.S . AID TO HIS NATION";
		Query query = parser.parse(q7);

		int q_id = 1;
		TopDocs results = searcher.search(query, 500);
		ScoreDoc[] hits = results.scoreDocs;
		BufferedWriter out = new BufferedWriter(new FileWriter("/home/jak/smart/time/lucene_searching_results.txt", false));

		int numTotalHits = results.totalHits;
		System.out.println(numTotalHits + " total matching documents");

		for (int i = 0; i < hits.length; i++) {
			Document doc = searcher.doc(hits[i].doc);
			float score = hits[i].score;
			String path = doc.get(Constants.NATURAL_ID);
			out.write(q_id + " 0  " + path + "  " + i + " " + score + "  jak_CSI550\n");
			System.out.println(q_id + " 0  " + path + "  " + i + " " + score + "	jak_CSI550");
		}

		out.close();

	}

	public static void main1(String[] args) throws Exception {

		String index = "/home/jak/smart/time/lucene_index";
		String field = Constants.CONTENT;
		String queries = null;
		int repeat = 0;
		boolean raw = true;
		String queryString = "/home/jak/smart/time/query_1.text";
		int hitsPerPage = 1000; // was 10
		String out = "/home/jak/smart/time/lucene_searching_results.txt";

		BufferedWriter out_f = new BufferedWriter(new FileWriter(out, false));
		out_f.close();
		// System.out.println("queries: " + queries);
		IndexSearcher searcher = new IndexSearcher(FSDirectory.open(new File(index)));
		Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_31);

		BufferedReader in = null;
		if (queries != null) {
			in = new BufferedReader(new InputStreamReader(new FileInputStream(queries), "UTF-8"));
		} else {
			in = new BufferedReader(new InputStreamReader(System.in, "UTF-8"));
		}
		QueryParser parser = new QueryParser(Version.LUCENE_31, field, analyzer);
		while (true) {
			if (queries == null && queryString == null) { // prompt the user
				System.out.println("Enter query: ");
			}

			String line = queryString != null ? queryString : in.readLine();

			if (line == null || line.length() == -1) {
				break;
			}

			line = line.trim();
			StringBuffer q = new StringBuffer();
			String q_id = null;
			if (queries != null) {
				if (line.length() == 0 && queries != null) {
					continue;
				} else if (line.length() == 0)
					break;
				if (line.trim().equals("<DOCNO>")) {
					line = in.readLine();
					if (line != null) {
						q_id = line;
					}
					in.readLine(); // skip </DOCNO>
				}
				while ((line = in.readLine()) != null && line.trim().length() != 0) {
					// get question
					q.append(line + " ");
				}
			}
			if (q.length() > 0)
				line = q.toString().trim();
			Query query = parser.parse(line);
			System.out.println("Searching for: " + query.toString(field));

			if (repeat > 0) { // repeat & time as benchmark
				Date start = new Date();
				for (int i = 0; i < repeat; i++) {
					searcher.search(query, null, 100);
				}
				Date end = new Date();
				System.out.println("Time: " + (end.getTime() - start.getTime()) + "ms");
			}

			doPagingSearch(in, out, searcher, query, hitsPerPage, raw, queries == null && queryString == null, q_id);

			if (queryString != null) {
				break;
			}
		}
		searcher.close();
	}

	/**
	 * This demonstrates a typical paging search scenario, where the search
	 * engine presents pages of size n to the user. The user can then go to the
	 * next page if interested in the next hits.
	 * 
	 * When the query is executed for the first time, then only enough results
	 * are collected to fill 5 result pages. If the user wants to page beyond
	 * this limit, then the query is executed another time and all hits are
	 * collected.
	 * 
	 */
	public static void doPagingSearch(BufferedReader in, String out_f, IndexSearcher searcher, Query query, int hitsPerPage, boolean raw,
			boolean interactive, String q_id) throws IOException {

		// Collect enough docs to show 5 pages
		raw = true; // only show raw
		TopDocs results = searcher.search(query, 5 * hitsPerPage);
		ScoreDoc[] hits = results.scoreDocs;
		BufferedWriter out = new BufferedWriter(new FileWriter(out_f, true));

		int numTotalHits = results.totalHits;
		System.out.println(numTotalHits + " total matching documents");

		int start = 0;
		int end = Math.min(numTotalHits, hitsPerPage);

		while (true) {
			/*
			 * if (end > hits.length) { System.out.println("Only results 1 - " +
			 * hits.length +" of " + numTotalHits +
			 * " total matching documents collected.");
			 * System.out.println("Collect more (y/n) ?"); String line =
			 * in.readLine(); if (line.length() == 0 || line.charAt(0) == 'n') {
			 * break; }
			 * 
			 * hits = searcher.search(query, numTotalHits).scoreDocs; }
			 */

			end = Math.min(hits.length, start + hitsPerPage);

			for (int i = start; i < end; i++) {

				Document doc = searcher.doc(hits[i].doc);
				float score = hits[i].score;
				String path = doc.get("path");
				if (path != null) {
					path = path.substring(path.lastIndexOf("\\") + 1);
					out.write(q_id + " 0  " + path + "  " + i + " " + score + "  CSI550\n");
					System.out.println(q_id + " 0  " + path + "  " + i + " " + score + "  CSI550");
				} else {
					String url = doc.get("url");
					if (url != null) {
						System.out.println(i + ". " + url);
						System.out.println("   - " + doc.get("title"));
					} else {
						System.out.println(i + ". " + "No path nor URL for this document");
					}
				}

				if (true)
					continue;
				if (raw) { // output raw format
					System.out.println("doc=" + hits[i].doc + " score=" + hits[i].score);
					continue;
				}

				doc = searcher.doc(hits[i].doc);
				path = doc.get("path");
				if (path != null) {
					System.out.println((i + 1) + ". " + path);
					String title = doc.get("title");
					if (title != null) {
						System.out.println("   Title: " + doc.get("title"));
					}
				} else {
					System.out.println((i + 1) + ". " + "No path for this document");
				}

			}
			out.close();

			if (!interactive || end == 0) {
				break;
			}

			if (numTotalHits >= end) {
				boolean quit = false;
				while (true) {
					System.out.print("Press ");
					if (start - hitsPerPage >= 0) {
						System.out.print("(p)revious page, ");
					}
					if (start + hitsPerPage < numTotalHits) {
						System.out.print("(n)ext page, ");
					}
					System.out.println("(q)uit or enter number to jump to a page.");

					String line = in.readLine();
					if (line.length() == 0 || line.charAt(0) == 'q') {
						quit = true;
						break;
					}
					if (line.charAt(0) == 'p') {
						start = Math.max(0, start - hitsPerPage);
						break;
					} else if (line.charAt(0) == 'n') {
						if (start + hitsPerPage < numTotalHits) {
							start += hitsPerPage;
						}
						break;
					} else {
						int page = Integer.parseInt(line);
						if ((page - 1) * hitsPerPage < numTotalHits) {
							start = (page - 1) * hitsPerPage;
							break;
						} else {
							System.out.println("No such page");
						}
					}
				}
				if (quit)
					break;
				end = Math.min(numTotalHits, start + hitsPerPage);
			}
		}
	}
}
