package edu.albany.ir.lucene;

import java.io.Reader;
import java.lang.reflect.Array;
import java.util.HashSet;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.ISOLatin1AccentFilter;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.StopAnalyzer;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.miscellaneous.EmptyTokenStream;
import org.apache.lucene.analysis.ngram.EdgeNGramTokenFilter;
import org.apache.lucene.analysis.ngram.EdgeNGramTokenFilter.Side;
import org.apache.lucene.analysis.shingle.ShingleAnalyzerWrapper;
import org.apache.lucene.analysis.shingle.ShingleFilter;
import org.apache.lucene.analysis.snowball.SnowballAnalyzer;
import org.apache.lucene.analysis.snowball.SnowballFilter;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.util.Version;

import edu.albany.ir.cran.util.CONSTANTS;

public class MyAnalyzerNGram extends Analyzer {

	@Override
	public TokenStream tokenStream(String fieldName, Reader reader) {		
		TokenStream result = new StandardTokenizer(Version.LUCENE_33, reader);

		result = new StandardFilter(Version.LUCENE_33, result);
		result = new LowerCaseFilter(Version.LUCENE_33, result);
		result = new ISOLatin1AccentFilter(result);
		result = new StopFilter(Version.LUCENE_33, result, StopAnalyzer.ENGLISH_STOP_WORDS_SET);
		//result = new StopFilter(Version.LUCENE_33, result, CONSTANTS.STOP_SET);

		// result = new EdgeNGramTokenFilter(result, Side.FRONT, 1, 20);
		result = new SnowballFilter(result, "English");

		ShingleFilter filter = new ShingleFilter(result, 7, 7);
		filter.setOutputUnigrams(false);
		result = filter;
		return result;
	}

}
