package dataProcessing;

import gnu.trove.map.hash.TObjectIntHashMap;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import dataProcessing.LongContainer;


public class Main {

	public static String[] directory;

	public static String currentComposer;
	private static InfoGetter inChords;

	private static boolean leitmotive = true;
	public static boolean pn = true;

	
	private static final int NGRAM_LENGTH = 10;
	private static final int LOWER_BOUND = 2;
	private static final double WEIGHT_TRESHOLD = .5;
	private static final double PROB_TRESHOLD = .4;
	private static final double SUPPORT_TRESHOLD = 2;

	
	public static void main(String[] args) {
		long startTime = System.nanoTime();    

		if (!leitmotive) {
			getSimForChords(0);
		} else {
			File f = new File("data");
			File[] files = f.listFiles();
			directory = new String[files.length];
	//		directory = new String[]{"test"};
			for (int i = 0; i < files.length; i++) {
				directory[i] = files[i].getName();
			}
						
			for (int i = 0; i < directory.length ; i++) {
				currentComposer = directory[i];
				System.out.println(directory[i]);
				getSimForChords(i);
			}
		}
		long estimatedTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
		System.out.println("Time passed: "+ estimatedTime);
	}
	
	private static void getSimForChords(int i) {
		if (!leitmotive) {
			System.out.println("Normal");
			if (pn){
				inChords = new InfoGetterPN(NGRAM_LENGTH, LOWER_BOUND, WEIGHT_TRESHOLD, PROB_TRESHOLD);
				readFiles(readFilesinDirectory("CSV/pn"));
			} else {
				inChords = new InfoGetterForComposer(NGRAM_LENGTH, LOWER_BOUND, WEIGHT_TRESHOLD, PROB_TRESHOLD);
				readFiles(readFilesinDirectory("CSV/chordsComp"));
			}
		} else {
			inChords = new InfoGetterForLeimotifs(NGRAM_LENGTH, LOWER_BOUND, WEIGHT_TRESHOLD, PROB_TRESHOLD);
			System.out.println("Normal");
			readFiles(readFilesinDirectory("CSV/leitmotive/"+currentComposer));
		}

		inChords.getMotivPhrases(i);

//		DBWriter db = new DBWriter(leitmotive);
//		db.loadData();
		
	}


	public static File[] readFilesinDirectory(String directory) {
		File f = new File(directory);
		return f.listFiles();
	}
	
	public static void readFiles(File[] files) {
		for (int i=0; i < files.length; i++) {
			String fileName = files[i].getName().split("\\.")[0];
//			String fileName = fileNames[0];
			String csvFile = files[i].getPath();
			BufferedReader br = null;
			String line = "";
			TObjectIntHashMap<LongContainer> map = new TObjectIntHashMap<LongContainer>();

			try {
				br = new BufferedReader(new FileReader(csvFile));

				System.out.println(fileName);

				while ((line = br.readLine()) != null) {

			        // use comma as separator
					String[] ngramcount = line.split(",");
					if (!leitmotive) {

							long[] l = new long[ngramcount.length-1];
							for (int i1 = 0; i1 < l.length; i1++) {
								l[i1] = Long.parseLong(ngramcount[i1]);
							}
							int length = InfoGetter.getNgramElementsCount(l);
							if ((length >= LOWER_BOUND-1 && length <= NGRAM_LENGTH) || length == 1) {
								map.put(new LongContainer(l), Integer.parseInt(ngramcount[ngramcount.length-1]));
							}
						
					} else {
						long[] l = new long[ngramcount.length-1];
						for (int i1 = 0; i1 < l.length; i1++) {
							l[i1] = Long.parseLong(ngramcount[i1]);
						}
						int length = InfoGetterForLeimotifs.getNgramElementsCount(l);
						if (Integer.parseInt(ngramcount[ngramcount.length-1]) > SUPPORT_TRESHOLD &&((length >= LOWER_BOUND-1 && length <= NGRAM_LENGTH) || length == 1)) {
							map.put(new LongContainer(l), Integer.parseInt(ngramcount[ngramcount.length-1]));
						}
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

			if (!leitmotive) {
				inChords.computeAssociationRules(map, fileName);				
			} else {
				inChords.computeAssociationRules(map, fileName);				
			}

		}
	}
	

}
