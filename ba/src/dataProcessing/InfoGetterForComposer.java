package dataProcessing;

import gnu.trove.list.array.TDoubleArrayList;
import gnu.trove.map.hash.TObjectIntHashMap;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;


import dataPreparation.MIDIParser;
import dataProcessing.LongContainer;

public class InfoGetterForComposer extends InfoGetter{

	
	public InfoGetterForComposer(int ngramLength, int lowerBound, double weightTreshold, double probTreshold) {
		NGRAM_LENGTH = ngramLength;
		LOWER_BOUND = lowerBound;
		WEIGHT_TRESHOLD = weightTreshold;
		PROB_TRESHOLD = probTreshold;
		documents = new ArrayList<HashMap<LongContainer, Double[]>>();
		max = new TDoubleArrayList();
		compositions = new ArrayList<String>();
		ngramInDocCount = new TObjectIntHashMap<LongContainer>();
	}


	
	public HashMap<LongContainer, Double[]> computeAssociationRules(TObjectIntHashMap<LongContainer> map, String fileName) {
		Collection<LongContainer> entries = map.keySet();
		Iterator<LongContainer> it = entries.iterator();
		HashMap<LongContainer, Double[]> aMap = new HashMap<LongContainer, Double[]>();
		double docmax = 0;
		
		while (it.hasNext()) {
			LongContainer next = it.next();
			
			int length = getNgramElementsCount(next.getLongArray());

			if (length >= LOWER_BOUND && length <= NGRAM_LENGTH) {
				long[] left = getNgramUpTo(next.getLongArray());
				long[] right = getLastElement(next.getLongArray());
				double support = map.get(next);
				double leftSupport = map.get(new LongContainer(left));
				double rightSupport = map.get(new LongContainer(right));
				double probability = support / leftSupport;

				if (probability >= PROB_TRESHOLD && support > 2) {
//					System.out.println(probability);

					if (support > docmax) {
						docmax = support;
					}

					if (!ngramInDocCount.containsKey(next)) {
						ngramInDocCount.put(next, 1);
					} else {
						int count = ngramInDocCount.get(next) + 1;
						ngramInDocCount.put(next, count);
					}
					aMap.put(next, new Double[]{support, leftSupport, rightSupport});
				}
			}
		}
		max.add(docmax);
		compositions.add(fileName);
		documents.add(aMap);

		return aMap;
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
	
				double left = doc.get(rule)[0] / doc.get(rule)[1];
				double right = doc.get(rule)[0] / doc.get(rule)[2];
//				System.out.println(left +" r: "+ right);

				double m = (left + right) / 2.0;
//				System.out.println(m);

				double w = tf_idf * (m + l) / 2;

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

	
	public List<Result<LongContainer>> cutResults(List<List<Result<LongContainer>>> results, TDoubleArrayList maxWeights, int composerIndex) {

		List<Result<LongContainer>> phrases = new ArrayList<Result<LongContainer>>();
		ArrayList<String> compositionList = new ArrayList<String>();
    	
		for (List<Result<LongContainer>> resultList: results) {
			List<Result<LongContainer>> phrasesList = new ArrayList<Result<LongContainer>>();

			for (Result<LongContainer> result: resultList) {
				if (phrasesList.size() >= MAX_PHRASE_COUNT) {
					break;
				}
				
				boolean isSubstring = false;
				for (Result<LongContainer> r: phrasesList){
					isSubstring = isSubstring(NgramCoder.unpackS(r.getNgram().getLongArray()), NgramCoder.unpackS(result.getNgram().getLongArray()));
					if (isSubstring) {
						break;
					}
				}
				

				if (!isSubstring && result.getWeight()> WEIGHT_TRESHOLD * maxWeights.get(result.getCompositionId())) {
					phrasesList.add(result);
					
					System.out.println("Composition: "+ compositions.get(result.getCompositionId())+
							", Ngram: " + NgramCoder.unpackS(result.getNgram().getLongArray()) +
							", Weight: " + result.getWeight());
					
					String compString = compositions.get(result.getCompositionId());
					if (!compositionList.contains(compString)) {
						compositionList.add(compString);
					}
					result.setCompositionId(compositionList.indexOf(compString));
	 
				} 
			}
			phrases.addAll(phrasesList);
		}
		
		compositions = compositionList;
		writeResults(phrases, compositions);
		
		return phrases;
 	}
	



	private void writeResults(List<Result<LongContainer>> phrases, ArrayList<String> compositions) {
		try {

			File composition = new File("DBInput/Comp/composers.csv");
	
	    	FileWriter writer4 = new FileWriter(composition);
	
	    	for (String compTitle: compositions) {
	    		if (Main.pn) {
	    			writer4.append("pn," + compositionId + "," + compTitle + "\r\n");
	    		} else {
					writer4.append("kdf," + compositionId + "," + compTitle + "\r\n");
	    		}
				compositionId++;
	    	}
		
	    	writer4.close();
			
			
			File output = new File("DBInput/Comp/results.csv");
			File ngrams = new File("DBInput/Comp/ngrams.csv");
		

	        FileWriter writer = new FileWriter(output);
	        FileWriter writer2 = new FileWriter(ngrams);
	        int ngramId = 0;
	        for (Result<LongContainer> r: phrases) {
				int id = compositionId - (compositions.size() - r.getCompositionId());
				if (!results.contains(r.getNgram())) {
					ngramId = results.size();
					results.add(r.getNgram());
					if (Main.pn) {
						writer2.append("pn," + ngramId + "," + NgramCoder.unpackS(r.getNgram().getLongArray()) + "\r\n");
					} else {
						writer2.append("kdf," + ngramId + "," + NgramCoder.unpackS(r.getNgram().getLongArray()) + "\r\n");						
					}
				} else {
					ngramId = results.indexOf(r.getNgram());
				}
				if (Main.pn) {
					writer.append("pn," + id + "," + ngramId + "," + r.getWeight()+"\r\n");
				} else {
					writer.append("kdf," + id + "," + ngramId + "," + r.getWeight()+"\r\n");
				}
			}
        	writer.close();
        	writer2.close();
        	

		} catch (IOException e) {
			e.printStackTrace();
		}	
	 		
		
	}

}
