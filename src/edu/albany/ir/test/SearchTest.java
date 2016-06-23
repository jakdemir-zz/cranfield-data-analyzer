package edu.albany.ir.test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.shingle.ShingleAnalyzerWrapper;
import org.apache.lucene.analysis.snowball.SnowballAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import edu.albany.ir.cran.util.GenericUtil;
import edu.albany.ir.lucene.MyAnalyzerNGram;

public class SearchTest {

	private SearchTest() {
	}

	public static void main(String[] args) throws Exception {
		IndexSearcher searcher = new IndexSearcher(FSDirectory.open(new File("/home/jak/smart/cran/lucene_index")));
		// Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_31);
		Analyzer analyzer = new MyAnalyzerNGram();
		//Analyzer analyzer = new ShingleAnalyzerWrapper(Version.LUCENE_31, 3, 5);
		//Analyzer analyzer = new ShingleAnalyzerWrapper(new SnowballAnalyzer(Version.LUCENE_31,"English"), 2, 5);
		//Analyzer analyzer = new SnowballAnalyzer(Version.LUCENE_31,"English");

		QueryParser parser = new QueryParser(Version.LUCENE_31, "content", analyzer);
		String content = "jak akdemir benim adim";
//		Query query = parser.parse(content);
		Query query = GenericUtil.getShingleBooleanQuery(analyzer, content, "content");
		System.out.println("Query is : "+query.toString());
		
		int q_id = 1;
		TopDocs results = searcher.search(query, 30);
		ScoreDoc[] hits = results.scoreDocs;
		BufferedWriter out = new BufferedWriter(new FileWriter("/home/jak/smart/cran/lucene_searching_results.txt", false));

		int numTotalHits = results.totalHits;
		System.out.println(numTotalHits + " total matching documents");

		for (int i = 0; i < hits.length; i++) {
			Document doc = searcher.doc(hits[i].doc);
			float score = hits[i].score;
			String path = doc.get("id");
			out.write(q_id + " 0  " + path + "  " + i + " " + score + "  jak_CSI550\n");
			System.out.println(q_id + " 0  " + path + "  " + i + " " + score + "	jak_CSI550");
		}

		out.close();

	}

}
