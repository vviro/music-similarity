package dataProcessing;

import gnu.trove.iterator.TLongIterator;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TDoubleArrayList;
import gnu.trove.map.TLongIntMap;
import gnu.trove.map.hash.TLongIntHashMap;
import gnu.trove.set.TLongSet;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


import dataPreparation.MIDIParser;


public class InfoGetterForMelody {
	private static double WEIGHT_TRESHOLD;
	private static double PROB_TRESHOLD;
	private static int NGRAM_LENGTH;
	private static int LOWER_BOUND;
	
	private ArrayList<HashMap<Long, Double[]>> documents;
	private TDoubleArrayList max;
	private ArrayList<String> compositions;
	private TLongIntMap ngramInDocCount;
	
	public InfoGetterForMelody(int ngramLength, int lowerBound, double weightTreshold, double probTreshold) {
		NGRAM_LENGTH = ngramLength;
		LOWER_BOUND = lowerBound;
		WEIGHT_TRESHOLD = weightTreshold;
		PROB_TRESHOLD = probTreshold;
		documents = new ArrayList<HashMap<Long, Double[]>>();
		max = new TDoubleArrayList();
		compositions = new ArrayList<String>();
		ngramInDocCount = new TLongIntHashMap();
	}
	
	public ArrayList<String> getCompositions() {
		return compositions;
	}

	public TLongIntHashMap getFrequencies(TIntList out, String fileName) throws IOException {
		//write how often a key appears in song 
		File output = new File("CSV/melody/"+MIDIParser.currentComposer+"/" + fileName + ".csv");
        FileWriter writer = new FileWriter(output);
	    //get set with count
//		ImmutableMultiset<Long> set  = new ImmutableMultiset.Builder<Long>().build();
		int[] array = new int[out.size()];
		out.toArray(array);
		
//		long[] elements = getNgrams(array, NGRAM_LENGTH);
//		Long[] elements = new Long[0];
//		for (int i = 1; i <= NGRAM_LENGTH; i++) {
//			Long[] a = getNgrams(array, i);
//			Long[] oldArray = elements;
//			elements = new Long[oldArray.length + a.length];
//			System.arraycopy(oldArray, 0, elements, 0, oldArray.length);
//			System.arraycopy(a, 0, elements, oldArray.length, a.length);
//		}

	    TLongIntHashMap countMap  = new TLongIntHashMap();
		for (int i = 0; i <= array.length; i++) {
			int[] keys = new int[NGRAM_LENGTH];
			for (int len = 0; len < (array.length-i < NGRAM_LENGTH ? array.length-i: NGRAM_LENGTH) ; len++) {			
				keys[len] = array[i + len];
				if (!countMap.contains(NgramCoder.pack(keys))) {
					countMap.put(NgramCoder.pack(keys), 1);
				} else {
					int count = countMap.get(NgramCoder.pack(keys)) + 1;
					countMap.put(NgramCoder.pack(keys), count);
				}
			}
		}

//		set = ImmutableMultiset.copyOf(elements);
		//create multimap with (key,count(key))
		TLongSet entrys = countMap.keySet();
		TLongIterator iterator = entrys.iterator();
		while (iterator.hasNext()) {
			long next = iterator.next();
//			countMap.put(next.getElement(), next.getCount());
//		    Files.append(next.getElement()+","+next.getCount()+"\r\n", output, Charsets.UTF_8);
			writer.write(next+","+ countMap.get(next) +"\r\n");
		}
		writer.close();
//		System.out.println(set.entrySet());
		return countMap;
	}
	
	
	public HashMap<Long,Double[]> computeAssociationRules(TLongIntHashMap ngramMap, String fileName) {
		TLongSet entries = ngramMap.keySet();
		TLongIterator it = entries.iterator();
//		ArrayList<AssociationRule<Long>> aRList = new ArrayList<AssociationRule<Long>>();
		HashMap<Long, Double[]> aMap = new HashMap<Long, Double[]>();
		double docmax = 0;
		while (it.hasNext()) {
			long nextNgram = it.next();
//			long nextNgram = next.getKey();
			short[] unpacked = NgramCoder.unpack(nextNgram);
			
//			int[] bounds = {LOWER_BOUND, NGRAM_LENGTH};
			
			if (unpacked.length >= LOWER_BOUND && unpacked.length <= NGRAM_LENGTH) {
//				long l = nextNgram.getNgram();
				long left = getNgramUpTo(unpacked, unpacked.length - 1);
				long right = getLastElement(unpacked);
				double support = ngramMap.get(nextNgram);
				double leftSupport = ngramMap.get(left);
				double rightSupport = ngramMap.get(right);
				double probability = support / leftSupport;

				if (probability > PROB_TRESHOLD) {
					if (support > docmax) {
						docmax = support;
					}
//					AssociationRule<Long> aRule = new AssociationRule<Long>(nextNgram, support, leftSupport, rightSupport);

					if (!ngramInDocCount.containsKey(nextNgram)) {
						ngramInDocCount.put(nextNgram, 1);
					} else {
						int count = ngramInDocCount.get(nextNgram) + 1;
//						ngramInDocCount.remove(nextNgram);
						ngramInDocCount.put(nextNgram, count);
					}
//					aRList.add(aRule);
					aMap.put(nextNgram, new Double[]{support, leftSupport, rightSupport});
				}
			}
		}
		max.add(docmax);
		compositions.add(fileName);
		documents.add(aMap);
		return aMap;
	}

	private long getLastElement(short[] unpacked) {
		int[] newUnpacked = new int[1];
		newUnpacked[0] = unpacked[unpacked.length-1];
		return NgramCoder.pack(newUnpacked);
	}

	private long getNgramUpTo(short[] nextNgram, int i) {
		int [] newNgram = new int[i];
		for (int n = 0; n < i; n++){
			newNgram[n] = nextNgram[n];
		}
		return NgramCoder.pack(newNgram);
	}
	

	public List<Result<Long>> getDescriptivePhrases() {
//		System.out.println(documents.size());
		List<Result<Long>> weight = new ArrayList<Result<Long>>();
		double maxWeight = 0;
		
		for (HashMap<Long, Double[]> doc: documents) {
	    	double maximum = max.get(documents.indexOf(doc));
//	    	System.out.println(maximum);^
	    	
	    	int composition = documents.indexOf(doc);
				    				
	    	for (Long rule: doc.keySet()) {
				//compute inverse document frequency
				double docCount = documents.size();
//				double phraseCount = 0;
				double phraseCount = ngramInDocCount.get(rule);

				short[] unpacked = NgramCoder.unpack(rule);
				
				double idf = Math.log(docCount / phraseCount);
				double tf_idf = (doc.get(rule)[0] / maximum) * idf;

				double l =  unpacked.length / (double) NGRAM_LENGTH;	

//				double l = 1.0;
//				double t; to Do 		
				double left = doc.get(rule)[0] / doc.get(rule)[1];
				double right = doc.get(rule)[0] / doc.get(rule)[2];
//			    System.out.println(rule.getLeftSupport());

				double m = (left + right) / 2;
//			    System.out.println(l);

				double w = tf_idf * (m + l) / 2;
				
				if (w > maxWeight) {
					maxWeight = w;
				}
				
//				Ngram r = new Ngram(rule.getNgram(), w, composition);
				weight.add(new Result<Long>(rule, w, composition));
			}

	    }
		return cutResults(weight, maxWeight);

	}

	private List<Result<Long>> cutResults(List<Result<Long>> weight, double maxWeight) {
		List<Result<Long>> phrases = new ArrayList<Result<Long>>();
//		File output = new File("CSV/descPhrases.csv");
//		String header = "phrase, weight, file";
//	    try {
//			Files.write(header+"\r\n", output, Charsets.UTF_8);
//		} catch (IOException e1) {
//			e1.printStackTrace();
//		}	

		
    	System.out.println(maxWeight);
    	
		for (Result<Long> result: weight) {
			if (result.getWeight()> WEIGHT_TRESHOLD * maxWeight) {
				phrases.add(result);			
//				String keys = "";
//				for (short key: NgramCoder.unpack(result.getNgram())) {
//					keys += " " + key;
//				}
//				try {
//					Files.append(keys + ","+ result.getSupport()+","+ result.getCompositionName() + "\r\n", output, Charsets.UTF_8);
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
 
			} 
		}
		File output = new File("CSV/melody/results/" + Main.currentComposer + "descPhrases" + LOWER_BOUND +"-" + NGRAM_LENGTH + ".csv");
		try {
	        FileWriter writer = new FileWriter(output);
		
		for (Result<Long> r: phrases) {
			short[] s = NgramCoder.unpack(r.getNgram());
			String ngram = "";
			for (short key: s) {
				ngram += key + " ";
			}
//			System.out.println(compositions.get(r.getCompositionId())+" k: " +r.getNgram() + " w:"+ r.getWeight());
			writer.write(compositions.get(r.getCompositionId()) + ", " + ngram + ","+ r.getWeight()+"\r\n");
		}
		
		writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	return phrases;
 	}

	
//	public long[] getNgrams(int[] array, int length) {
////		Long[] elements = new Long[array.length - length + 1];
//		TLongArrayList elements = new TLongArrayList();
//		for (int i = 0; i <= array.length; i++) {
//			int[] keys = new int[length];
////			System.out.println(array.length-i < length ? array.length-i: length);
//			for (int len = 0; len < (array.length-i < length ? array.length-i: length) ; len++) {			
//				keys[len] = array[i + len];
//				elements.add(NgramCoder.pack(keys));
//
//			}
//			//hier schon zählen
////			System.out.println(keys);
//		}
//		
//		return elements.toArray();
//	}
	
}
