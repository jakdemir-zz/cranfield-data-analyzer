package edu.albany.ir.cran;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.PerFieldAnalyzerWrapper;
import org.apache.lucene.document.Document;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import edu.albany.ir.cran.util.GenericUtil;
import edu.albany.ir.lucene.MyAnalyzerNGram;
import edu.albany.ir.lucene.MyAnalyzerPure;

public class SearchCran {
	
//	public static void main(String[] args) throws Exception {
//		ArrayList<String> queryList = getAllQueries();
//		for (String query: queryList) {
//			Analyzer analyzer = new MyAnalyzer();
//			GenericUtil.getShingleBooleanQuery(analyzer, query, "content");
//			
//		}
//	}

	public static void main(String[] args) throws Exception {

		// Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_31);
		// Analyzer analyzer = new
		// ShingleAnalyzerWrapper(Version.LUCENE_31,2,5);
		// Analyzer analyzer = new ShingleAnalyzerWrapper(new
		// SnowballAnalyzer(Version.LUCENE_31,"English"), 2, 5);
		// Analyzer analyzer = new
		// SnowballAnalyzer(Version.LUCENE_31,"English");

		IndexSearcher searcher = new IndexSearcher(FSDirectory.open(new File("/home/jak/smart/cran/lucene_index")));
//		Analyzer analyzer = new MyAnalyzerNGram();
//		Analyzer analyzer = new MyAnalyzerPure();

		PerFieldAnalyzerWrapper analyzer = new PerFieldAnalyzerWrapper(new MyAnalyzerPure());
		analyzer.addAnalyzer(Constants.CONTENT_INDEX_NGRAM, new MyAnalyzerNGram());
		analyzer.addAnalyzer(Constants.TITLE_INDEX_NGRAM, new MyAnalyzerNGram());
		
//		QueryParser parser = new QueryParser(Version.LUCENE_31, Constants.CONTENT_INDEX, analyzer);
//		String q0 = "jak her havuzun dibi aynidir";
//		String q1 = "what similarity laws must be obeyed when constructing aeroelastic models of heated high speed aircraft";
		
		ArrayList<String> queryList = getAllQueries();
		BufferedWriter out = new BufferedWriter(new FileWriter("/home/jak/smart/cran/lucene_searching_results.txt", false));

		for (int i = 0; i < queryList.size(); i++) {
			// Query query = parser.parse(queryList.get(i));
			Query queryContent = GenericUtil.getShingleBooleanQuery(analyzer, queryList.get(i), Constants.CONTENT_INDEX);
//			queryContent.setBoost(0.75f);
//			Query queryTitle = GenericUtil.getShingleBooleanQuery(analyzer, queryList.get(i), Constants.TITLE_INDEX);
//			queryTitle.setBoost(0.25f);
//			
//			Query queryContentNGram = GenericUtil.getShingleBooleanQuery(analyzer, queryList.get(i), Constants.CONTENT_INDEX_NGRAM);
//			queryContentNGram.setBoost(10f);
//			Query queryTitleNGram = GenericUtil.getShingleBooleanQuery(analyzer, queryList.get(i), Constants.TITLE_INDEX_NGRAM);
//			queryTitleNGram.setBoost(1f);

			
			BooleanQuery booleanQuery = new BooleanQuery();
			booleanQuery.add(queryContent, Occur.SHOULD);
//			booleanQuery.add(queryTitle, Occur.SHOULD);
//			booleanQuery.add(queryContentNGram, Occur.SHOULD);
//			booleanQuery.add(queryTitleNGram, Occur.SHOULD);

			
			// TopDocs results = searcher.search(query, 30);
			TopDocs results = searcher.search(booleanQuery, 500);
			ScoreDoc[] hits = results.scoreDocs;

			int numTotalHits = results.totalHits;
			System.out.println(numTotalHits + " total matching documents");

			for (int j = 0; j < hits.length; j++) {
				Document doc = searcher.doc(hits[j].doc);
				float score = hits[j].score;
				String path = doc.get(Constants.ID_INDEX);
				out.write((i + 1) + " 0  " + path + "  " + j + " " + score + "  jak_CSI550\n");
				System.out.println((i + 1) + " 0  " + path + "  " + j + " " + score + "	jak_CSI550");
			}

		}

		out.close();
		
	}

	/*
	 * .I 001.Wwhat similarity laws must be obeyed when constructing aeroelastic
	 * modelsof heated high speed aircraft .
	 */

	private static ArrayList<String> getAllQueries() throws IOException {
		FileInputStream fis = new FileInputStream(new File("/home/jak/smart/cran/query.text"));
		BufferedReader br = new BufferedReader(new InputStreamReader(fis));

		String query = "";
		String line = "";
		ArrayList<String> queryList = new ArrayList<String>();
		while ((line = br.readLine()) != null) {
			if (line.startsWith(Constants.ID_DOC)) {
				if (!query.equals("")) {
					query = query.replace("*", "");
					query = query.replace("?", "");
					queryList.add(query);
					query = "";
				}

			} else if (line.startsWith(Constants.CONTENT_DOC)) {
				// do nothing
			} else {
				query = query +" "+ line;
			}
		}
		query = query + line;
		queryList.add(query);
		return queryList;
	}

}
