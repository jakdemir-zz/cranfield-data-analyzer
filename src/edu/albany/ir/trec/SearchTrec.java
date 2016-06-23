package edu.albany.ir.trec;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.apache.lucene.analysis.PerFieldAnalyzerWrapper;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;

import edu.albany.ir.cran.util.GenericUtil;
import edu.albany.ir.lucene.MyAnalyzerNGram;
import edu.albany.ir.lucene.MyAnalyzerPure;

public class SearchTrec {
	
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

		IndexSearcher searcher = new IndexSearcher(FSDirectory.open(new File("/usr/java/Training_TREC/lucene_index")));
//		Analyzer analyzer = new MyAnalyzerNGram();
//		Analyzer analyzer = new MyAnalyzerPure();

		PerFieldAnalyzerWrapper analyzer = new PerFieldAnalyzerWrapper(new MyAnalyzerPure());
		analyzer.addAnalyzer(Constants.CONTENT_INDEX_NGRAM, new MyAnalyzerNGram());
		analyzer.addAnalyzer(Constants.HEADLINE_INDEX_NGRAM, new MyAnalyzerNGram());
		
//		QueryParser parser = new QueryParser(Version.LUCENE_31, Constants.CONTENT_INDEX, analyzer);
//		String q0 = "jak her havuzun dibi aynidir";
//		String q1 = "what similarity laws must be obeyed when constructing aeroelastic models of heated high speed aircraft";
		
		ArrayList<TrecQuery> queryList = getAllQueries();
		BufferedWriter out = new BufferedWriter(new FileWriter("/usr/java/Training_TREC/lucene_searching_results.txt", false));

		for (int i = 0; i < queryList.size(); i++) {
			// Query query = parser.parse(queryList.get(i));
//			Query queryContent = GenericUtil.getShingleBooleanQuery(analyzer, queryList.get(i).getQuery(), Constants.CONTENT_INDEX);
//			queryContent.setBoost(0.75f);
//			Query queryHeadline = GenericUtil.getShingleBooleanQuery(analyzer, queryList.get(i).getQuery(), Constants.HEADLINE_INDEX);
//			queryHeadline.setBoost(0.25f);
			
			Query queryContentNGram = GenericUtil.getShingleBooleanQuery(analyzer, queryList.get(i).getQuery(), Constants.CONTENT_INDEX_NGRAM);
			queryContentNGram.setBoost(0.75f);
//			Query queryHeadlineNGram = GenericUtil.getShingleBooleanQuery(analyzer, queryList.get(i).getQuery(), Constants.HEADLINE_INDEX_NGRAM);
//			queryHeadlineNGram.setBoost(0.25f);

			
			BooleanQuery booleanQuery = new BooleanQuery();
//			booleanQuery.add(queryContent, Occur.SHOULD);
//			booleanQuery.add(queryHeadline, Occur.SHOULD);
			booleanQuery.add(queryContentNGram, Occur.SHOULD);
//			booleanQuery.add(queryHeadlineNGram, Occur.SHOULD);

			
			// TopDocs results = searcher.search(query, 30);
			TopDocs results = searcher.search(booleanQuery, 500);
			ScoreDoc[] hits = results.scoreDocs;

			int numTotalHits = results.totalHits;
			System.out.println(numTotalHits + " total matching documents");

			for (int j = 0; j < hits.length; j++) {
				Document doc = searcher.doc(hits[j].doc);
				float score = hits[j].score;
				String path = doc.get(Constants.PATH_INDEX);
				out.write(queryList.get(i).getId() + " 0  " + path + "  " + j + " " + score + "  jak_CSI550\n");
				System.out.println(queryList.get(i).getId() + " 0  " + path + "  " + j + " " + score + "	jak_CSI550");
			}

		}

		out.close();
		
	}

	/*
<DOCNO>
302
</DOCNO>
Is the disease of Poliomyelitis (polio) under control in the
world?





<DOCNO>
308
</DOCNO>
What are the advantages and/or disadvantages of tooth implants?

	 */

	private static ArrayList<TrecQuery> getAllQueries() throws IOException {
		FileInputStream fis = new FileInputStream(new File("/usr/java/Training_TREC/QUERIES_for_training.txt"));
		BufferedReader br = new BufferedReader(new InputStreamReader(fis));

		
		String query = "";
		int id=0;
		String line = "";
		
		int state = 0;

		ArrayList<TrecQuery> queryList = new ArrayList<TrecQuery>();

		while ((line = br.readLine()) != null) {
			if (line.equals(Constants.DOC_START_DOC)) {
				if (!query.equals("")) {
					TrecQuery trecQuery = new TrecQuery();
					trecQuery.setId(id);
					trecQuery.setQuery(query);
					queryList.add(trecQuery);
					id = 0;
					query= "";
				}
				state = 1;
			}else if(line.equals(Constants.DOC_END_DOC)){
				state =2;
			}else {
				if (state == 1) {
					id = Integer.valueOf(line.trim());
				}else if (state==2) {
					query = query +" "+ line;
				}
			}
		}
		TrecQuery trecQuery = new TrecQuery();
		trecQuery.setId(id);
		trecQuery.setQuery(query);
		queryList.add(trecQuery);
		
		return queryList;
	}

	
//	public static void main(String[] args) throws IOException {
//		ArrayList<TrecQuery> queries = getAllQueries();
//		
//		for (TrecQuery trecQuery : queries) {
//			System.out.println("trecQuery.getId() : "+trecQuery.getId());
//			System.out.println("trecQuery.getQuery() : "+trecQuery.getQuery());
//			
//		}
//	}
}
