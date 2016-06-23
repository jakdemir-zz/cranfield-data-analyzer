package edu.albany.ir.cran.util;

import java.io.StringReader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.BooleanClause.Occur;

public class GenericUtil {
	public static BooleanQuery getShingleBooleanQuery(Analyzer analyzer, String qs, String fieldToSearch) throws Exception {

		BooleanQuery q = new BooleanQuery();
		TokenStream ts = analyzer.tokenStream(fieldToSearch, new StringReader(qs));
		CharTermAttribute termAtt = ts.addAttribute(CharTermAttribute.class);
		ts.reset();

		while (ts.incrementToken()) {
			String termText = termAtt.toString();
			q.add(new TermQuery(new Term(fieldToSearch, termText)), Occur.SHOULD);
		}
		System.out.println("... parsed query: " + q);
		return q;
	}

}
