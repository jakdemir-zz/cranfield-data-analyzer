package edu.albany.ir.cran.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class FindTopTerms {
	public static void main(String[] pArgs) throws Exception {

		String indexPath = "/home/jak/smart/cran/lucene_index";
		Directory dir = FSDirectory.open(new File(indexPath));

		Integer threshold = new Integer(10);
		IndexReader reader = IndexReader.open(dir);
		TermEnum termEnum = reader.terms();
		List termList = new ArrayList();
		while (termEnum.next()) {
			if (termEnum.docFreq() >= threshold.intValue() && termEnum.term().field().equals("title")) {
				Freq freq = new Freq(termEnum.term().text(), termEnum.docFreq());
				termList.add(freq);
			}
		}
		Collections.sort(termList);
		Collections.reverse(termList);
		System.out.println("Frequency | Term");
		Iterator iterator = termList.iterator();
		while (iterator.hasNext()) {
			Freq freq = (Freq) iterator.next();
			System.out.print(freq.frequency);
			System.out.println(" | " + freq.term);
		}
	}

	public static class Freq implements Comparable {
		String term;
		int frequency;

		public Freq(String term, int frequency) {
			this.term = term;
			this.frequency = frequency;
		}

		public int compareTo(Object o) {
			if (o instanceof Freq) {
				Freq oFreq = (Freq) o;
				return new CompareToBuilder().append(frequency, oFreq.frequency).append(term, oFreq.term).toComparison();
			} else {
				return 0;
			}
		}
	}
}