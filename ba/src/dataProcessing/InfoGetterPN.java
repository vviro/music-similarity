package dataProcessing;

import gnu.trove.list.array.TDoubleArrayList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class InfoGetterPN extends InfoGetterForComposer{
	private static final String PITCH = "80";
	
	public InfoGetterPN(int ngramLength, int lowerBound, double weightTreshold, double probTreshold) {
		super(ngramLength, lowerBound, weightTreshold, probTreshold);
	}
	
	
	public List<Result<LongContainer>> getMotivPhrases(int composerIndex) {
		List<List<Result<LongContainer>>> results = new ArrayList<List<Result<LongContainer>>>();
		TDoubleArrayList maxWeights = new TDoubleArrayList();

		for (HashMap<LongContainer, Double[]> doc: documents) {
//	    	System.out.println(doc.size());
			double maxWeight = 0;

			List<Result<LongContainer>> weight = new ArrayList<Result<LongContainer>>();

	    	double maximum = max.get(documents.indexOf(doc));
	    	
	    	int composition = documents.indexOf(doc);
				    				
	    	for (LongContainer rule: doc.keySet()) {
				//compute inverse document frequency
				double docCount = documents.size();
				double phraseCount = ngramInDocCount.get(rule);

				double idf = Math.log(docCount / phraseCount);
				double tf_idf = (doc.get(rule)[0] / maximum) * idf;

				double l = getNgramElementsCount(rule.getLongArray()) / (double)NGRAM_LENGTH;	

				double w = tf_idf * l;

//				System.out.println(w);
				if (w > maxWeight) {
					maxWeight = w;
				}
				
				weight.add(new Result<LongContainer>(rule, w, composition));
			}
	    	Collections.sort(weight);
	    	results.add(weight);
	    	maxWeights.add(maxWeight);
	    }

		return cutResults(results, maxWeights, 0);

	}
	
	
	public static String tranformChords(String string) {
		String ngram = "" + PITCH;
//		System.out.println(string);
		if (string.startsWith("_")) ngram = ngram + "_";
		else if (!string.isEmpty()) ngram = ngram + " ";
		String[] chords = string.split(" ");
		
		int old = Integer.parseInt(PITCH);
		for (int i=0; i < chords.length; i++){
//			System.out.println(chords[i]);
			String[] keys = chords[i].split("_");
			for (int j=0; j < keys.length; j++) {
//				System.out.println(keys[j]);
				if (!keys[j].isEmpty()){
				if (j != keys.length-1) ngram = ngram + (old + Integer.parseInt(keys[j]))  + "_";
				else ngram = ngram + (old + Integer.parseInt(keys[j]));
				old = old + Integer.parseInt(keys[j]);
			
				}
			}
			if (i != chords.length - 1) ngram = ngram + " ";
		}
		
		return ngram;
	}
	

}
