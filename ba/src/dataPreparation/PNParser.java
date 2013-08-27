package dataPreparation;

import gnu.trove.map.hash.TObjectIntHashMap;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;


import dataProcessing.InfoGetterPN;
import dataProcessing.LongContainer;
import dataProcessing.NgramCoder;

public class PNParser {

	
	public static String[] directory;

	public static String currentComposer;
	private static final double SUPPORT_TRESHOLD = 2;

	
	public static void main(String[] args) {
		long startTime = System.nanoTime();    

		getSimForChords(0);
		
		long estimatedTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
		System.out.println("Time passed: "+ estimatedTime);
	}
	
	private static void getSimForChords(int i) {
//			long startTime2n = System.nanoTime();  
			System.out.println("Normal");
			
			readFiles(readFilesinDirectory("CSV/input"));
					
	}


	public static File[] readFilesinDirectory(String directory) {
		File f = new File(directory);
		return f.listFiles();
	}
	
	public static void readFiles(File[] files) {
		for (int i=0; i < files.length; i++) {
//			String fileName = files[i].getName().split("\\.")[0];
//			String fileName = fileNames[0];
			String csvFile = files[i].getPath();
			BufferedReader br = null;
			String line = "";
			HashMap<String, TObjectIntHashMap<LongContainer>> all = new HashMap<String, TObjectIntHashMap<LongContainer>>();
//			TObjectIntHashMap<LongContainer> map = new TObjectIntHashMap<LongContainer>();

			try {
				br = new BufferedReader(new FileReader(csvFile));
				LongContainer lc = null;
				String lastComp = "";
//				System.out.println(fileName);

				while ((line = br.readLine()) != null) {

//					System.out.println(line);
							
					String[] s1 = line.split(":");
					if (Integer.parseInt(s1[3].split("	")[1]) > SUPPORT_TRESHOLD) {
						
						long[] ngrams = chordToLongArray(InfoGetterPN.tranformChords(s1[2]));
								
					    if (lastComp.equalsIgnoreCase(s1[1].replace(",", "")) && lc.equals(new LongContainer(ngrams))) {
							TObjectIntHashMap<LongContainer> comp = all.get(s1[1].replace(",", ""));
					    	comp.put(new LongContainer(ngrams),comp.get(new LongContainer(ngrams)) + Integer.parseInt(s1[3].split("	")[1]));
					    } else {
//							    	System.out.println(lastComp);
						    if (!all.containsKey(s1[1].replace(",", ""))) {
								TObjectIntHashMap<LongContainer> comp = new TObjectIntHashMap<LongContainer>();
								comp.put(new LongContainer(ngrams), Integer.parseInt(s1[3].split("	")[1]));
								all.put(s1[1].replace(",", ""), comp);
							} else {
								TObjectIntHashMap<LongContainer> comp = all.get(s1[1].replace(",", ""));
								if (!comp.containsKey(new LongContainer(ngrams))) {
									comp.put(new LongContainer(ngrams), Integer.parseInt(s1[3].split("	")[1]));
								} 
//								TObjectIntHashMap<LongContainer> lcomp = all.get(lastComp);
//								if (lcomp.get(lc) <= SUPPORT_TRESHOLD) {
//									lcomp.remove(lc);
////								System.out.println("removed");
//								}
							}
					    }
					    lc = new LongContainer(ngrams);
					    lastComp = s1[1].replace(",", "");
					}
						
					
				}

//				System.out.println("done reading");
 
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (br != null) {
					try {
						br.close();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}

				
			for (String comp: all.keySet()) {
				MIDIParser.writeNgramsToFile("CSV/pn/" + comp + ".csv", all.get(comp));
			}

		}
	}

	private static long[] chordToLongArray(String ngram) {
		ArrayList<LongContainer> a = new ArrayList<LongContainer>();
		for (String chord: ngram.split(" ")){
			a.add(new LongContainer(NgramCoder.pack(chord)));
		}
				
		long[] ngrams = new long[0];
	    for (LongContainer longNgram: a) {
	    	long[] l = longNgram.getLongArray();
	    	long[] old = ngrams;
	    	ngrams = new long[old.length + l.length];
	    	System.arraycopy(old, 0, ngrams, 0, old.length);
			System.arraycopy(l, 0, ngrams, old.length, l.length);
	    }
		return ngrams;
	}

}
