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

public class InfoGetterForChords {

	private static double WEIGHT_TRESHOLD;
	private static double PROB_TRESHOLD;
	private static int NGRAM_LENGTH;
	private static int LOWER_BOUND;
	private static int compositionId = 0;
	private static final int MAX_PHRASE_COUNT = 15;
	
	private static List<LongContainer> results = new ArrayList<LongContainer>();
	
	private ArrayList<HashMap<LongContainer,Double[]>> documents;
	private TDoubleArrayList max;
	private ArrayList<String> compositions;
	private TObjectIntHashMap<LongContainer> ngramInDocCount;
	
	public InfoGetterForChords(int ngramLength, int lowerBound, double weightTreshold, double probTreshold) {
		NGRAM_LENGTH = ngramLength;
		LOWER_BOUND = lowerBound;
		WEIGHT_TRESHOLD = weightTreshold;
		PROB_TRESHOLD = probTreshold;
		documents = new ArrayList<HashMap<LongContainer, Double[]>>();
		max = new TDoubleArrayList();
		compositions = new ArrayList<String>();
		ngramInDocCount = new TObjectIntHashMap<LongContainer>();
	}

	public ArrayList<String> getCompositions() {
		return compositions;
	}

	public TObjectIntHashMap<LongContainer> getFrequencies(ArrayList<String> out, String fileName) throws IOException {


		String[] array = new String[out.size()];
		out.toArray(array);

	    
		//create multimap with (key,count(key))
	    TObjectIntHashMap<LongContainer> countMap  = new TObjectIntHashMap<LongContainer>();
		for (int i = 0; i <= array.length; i++) {
			String keys ="";
			ArrayList<LongContainer> all = new ArrayList<LongContainer>();
			for (int len = 0; len < (array.length-i < NGRAM_LENGTH ? array.length-i: NGRAM_LENGTH); len++) {
				keys = array[i + len];
			    long[] longKeys = NgramCoder.pack(keys);
			    all.add(new LongContainer(longKeys));
			    
			    long[] ngrams = new long[0];
			    for (LongContainer longNgram: all) {
			    	long[] l = longNgram.getLongArray();
			    	long[] old = ngrams;
			    	ngrams = new long[old.length + l.length];
			    	System.arraycopy(old, 0, ngrams, 0, old.length);
					System.arraycopy(l, 0, ngrams, old.length, l.length);
			    }
				
				if (!countMap.contains(new LongContainer(ngrams))) {
					countMap.put(new LongContainer(ngrams), 1);
				} else {
					int count = countMap.get(new LongContainer(ngrams)) + 1;
					countMap.put(new LongContainer(ngrams), count);
				}

			}
		}
	    
		//write how often a key appears in song 
		File output = new File("CSV/chords/"+MIDIParser.currentComposer+"/" + fileName + ".csv");
        FileWriter fw = new FileWriter(output);
        BufferedWriter writer = new BufferedWriter(fw);
        
		Collection<LongContainer> entrys = countMap.keySet();
		Iterator<LongContainer> iterator = entrys.iterator();
		while (iterator.hasNext()) {
			LongContainer next = iterator.next();
			for (long l: next.getLongArray()) {
				writer.write(l + ",");
			}
			writer.write(countMap.get(next) + "\r\n");
		}
		writer.close();
		return countMap;
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

				if (probability >= PROB_TRESHOLD) {
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

	public static int getNgramElementsCount(long[] ngrams) {
		int counter = 0;
		for (long l: ngrams) {
			if ((l & 1) == 0) counter++;
		}
		return counter;
	}
	
	public static long[] getLastElement(long[] ngrams) {
		for (int i = ngrams.length-1; i > 0; i--){
			if ((ngrams[i-1] & 1) == 0) return Arrays.copyOfRange(ngrams, i, ngrams.length);
		}
		return ngrams;
	}
	
	public static long[] getNgramUpTo(long[] ngrams) {
		return Arrays.copyOfRange(ngrams, 0, ngrams.length - getLastElement(ngrams).length);
	}

	public List<Result<LongContainer>> getMotivPhrases(int composerIndex) {
		List<List<Result<LongContainer>>> results = new ArrayList<List<Result<LongContainer>>>();
		TDoubleArrayList maxWeights = new TDoubleArrayList();
		double maxWeight = 0;

		for (HashMap<LongContainer, Double[]> doc: documents) {
//	    	System.out.println(documents.indexOf(doc));
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
				double m = (left + right) / 2.0;
				
				double w = tf_idf * (m + l) / 2;

				if (w > maxWeight) {
					maxWeight = w;
				}
				
				weight.add(new Result<LongContainer>(rule, w, composition));
			}
	    	Collections.sort(weight);
	    	results.add(weight);
	    	maxWeights.add(maxWeight);
	    }

		return cutResults(results, maxWeights, composerIndex);

	}
	
	private List<Result<LongContainer>> cutResults(List<List<Result<LongContainer>>> results, TDoubleArrayList maxWeights, int composerIndex) {
		
		List<Result<LongContainer>> phrases = new ArrayList<Result<LongContainer>>();
		ArrayList<String> compositionList = new ArrayList<String>();
    	
		for (List<Result<LongContainer>> resultList: results) {
			List<Result<LongContainer>> phrasesList = new ArrayList<Result<LongContainer>>();
//			int counter = 0;
			for (Result<LongContainer> result: resultList) {
				if (phrasesList.size() >= MAX_PHRASE_COUNT) {
					break;
				}
				boolean isSubstring = false;
				for (Result<LongContainer> r: phrasesList){
					if (NgramCoder.unpackS(r.getNgram().getLongArray()).contains(NgramCoder.unpackS(result.getNgram().getLongArray()))) {
						isSubstring = true;
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
		writeResults(phrases, compositions, composerIndex);
		
		return phrases;
 	}
	

//	public List<Result<LongContainer>> getDescriptivePhrases(int composerIndex) {
//		List<Result<LongContainer>> weight = new ArrayList<Result<LongContainer>>();
//		double maxWeight = 0;
//		
//		for (HashMap<LongContainer, Double[]> doc: documents) {
//	    	double maximum = max.get(documents.indexOf(doc));
////	    	System.out.println(documents.indexOf(doc));
//
//	    	int composition = documents.indexOf(doc);
//				    				
//	    	for (LongContainer rule: doc.keySet()) {
//				//compute inverse document frequency
//				double docCount = documents.size();
//				double phraseCount = ngramInDocCount.get(rule);
//				
//				double idf = Math.log(docCount / phraseCount);
//				double tf_idf = (doc.get(rule)[0] / maximum) * idf;
//				
//				double l = getNgramElementsCount(rule.getLongArray()) / (double)NGRAM_LENGTH;	
//	
//				double left = doc.get(rule)[0] / doc.get(rule)[1];
//				double right = doc.get(rule)[0] / doc.get(rule)[2];
//				double m = (left + right) / 2.0;
//				
//				double w = tf_idf * (m + l) / 2;
//
//				if (w > maxWeight) {
//					maxWeight = w;
//				}
//				
//				weight.add(new Result<LongContainer>(rule, w, composition));
//			}
//
//	    }
//		if (weight.size() >= MAX_PHRASE_COUNT) {
//			Collections.sort(weight);
//		}
//		return cutResults(weight, maxWeight, composerIndex);
//
//	}
//	
//
//
//	private List<Result<LongContainer>> cutResults(List<Result<LongContainer>> weight, double maxWeight, int composerIndex) {
//		List<Result<LongContainer>> phrases = new ArrayList<Result<LongContainer>>();
//
//		ArrayList<String> compositionList = new ArrayList<String>();
//
//    	System.out.println(maxWeight);
//    	
//		for (Result<LongContainer> result: weight) {
//			
//			if (result.getWeight()> WEIGHT_TRESHOLD * maxWeight) {
//				if (phrases.size() > MAX_PHRASE_COUNT) {
//					break;
//				}
//				phrases.add(result);
//				String compString = compositions.get(result.getCompositionId());
//				if (!compositionList.contains(compString)) {
//					compositionList.add(compString);
//				}
//				result.setCompositionId(compositionList.indexOf(compString));
// 
//			} 
//		}
//		
//		compositions = compositionList;
//		writeResults(phrases, compositions, composerIndex);
//		
//		return phrases;
// 	}

	private void writeResults(List<Result<LongContainer>> phrases, ArrayList<String> compositions, int composerIndex) {
		try {
			File composer = new File("DBInput/composer.csv");
			FileWriter writer5 = new FileWriter(composer);
			writer5.append(composerIndex + "," + Main.currentComposer + "\r\n");
			writer5.close();
	
			File comps = new File("DBInput/compcomp.csv");
			File composition = new File("DBInput/compositions.csv");
	
	
	    	FileWriter writer3 = new FileWriter(comps);
	    	FileWriter writer4 = new FileWriter(composition);
	
	    	for (String compTitle: compositions) {
				writer3.append(composerIndex + "," + compositionId + "\r\n");
				writer4.append(compositionId + "," + compTitle + "\r\n");
				compositionId++;
	    	}
		
	    	writer3.close();
	    	writer4.close();
			
			
			File output = new File("DBInput/results.csv");
			File ngrams = new File("DBInput/ngrams.csv");
		

	        FileWriter writer = new FileWriter(output);
	        FileWriter writer2 = new FileWriter(ngrams);
	        int ngramId = 0;
	        for (Result<LongContainer> r: phrases) {
				int id = compositionId - (compositions.size() - r.getCompositionId());
				if (!results.contains(r.getNgram())) {
					ngramId = results.size();
					results.add(r.getNgram());
					writer2.append(ngramId + "," + NgramCoder.unpackS(r.getNgram().getLongArray()) + "\r\n");

				} else {
					ngramId = results.indexOf(r.getNgram());
				}
	        	writer.append(id + "," + ngramId + "," + r.getWeight()+"\r\n");

			}
        	writer.close();
        	writer2.close();
        	

		} catch (IOException e) {
			e.printStackTrace();
		}	
	 		
		
	}

}
