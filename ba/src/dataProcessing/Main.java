package dataProcessing;

import gnu.trove.map.hash.TLongIntHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import java.nio.charset.Charset;
import java.nio.file.*;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.TimeUnit;


import dataProcessing.LongContainer;


public class Main {

	public static String[] directory;

	public static String currentComposer;
	private static InfoGetterForMelody in;
	private static InfoGetterForChords inChords;
	private static boolean chords = true;
	
	private static final int NGRAM_LENGTH = 10;
	private static final int LOWER_BOUND = 7;
	private static final double WEIGHT_TRESHOLD = .7;
	private static final double PROB_TRESHOLD = .4;
	private static final double SUPPORT_TRESHOLD = 2;

	
	public static void main(String[] args) {

		File f = new File("data");
		File[] files = f.listFiles();
//		directory = new String[files.length];
		directory = new String[]{"test"};
//		for (int i = 0; i < files.length; i++) {
//			directory[i] = files[i].getName();
//		}
		
		
		long startTime = System.nanoTime();    
		
		for (int i = 0; i < directory.length ; i++) {
			currentComposer = directory[i];
			System.out.println(directory[i]);
			if (!chords) {
//				getSimForMelody();
			} else {
				getSimForChords(i);
			}
		}
//		long estimatedTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
//		System.out.println("Time passed: "+ estimatedTime);
	}
	
	private static void getSimForChords(int i) {
		inChords = new InfoGetterForChords(NGRAM_LENGTH, LOWER_BOUND, WEIGHT_TRESHOLD, PROB_TRESHOLD);
		
//		long startTime2n = System.nanoTime();  
		System.out.println("Normal");
		readFiles(readFilesinDirectory("CSV/chords/"+currentComposer));
//		long estimatedTime2n = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime2n);
//		System.out.println("Time passed converting: "+ estimatedTime2n);
		
//		System.out.println("Nio");
//		long startTime2 = System.nanoTime();  
//		readFilesNio(readFilesinDirectory("CSV/chords/"+currentComposer));
//		long estimatedTime2 = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime2);
//		System.out.println("Time passed converting: "+ estimatedTime2);
		
//		System.out.println("Done converting");
		
//		List<Result<LongContainer>> list2 = inChords.getDescriptivePhrases(i);
//		DBWriter db = new DBWriter(list2, currentComposer, inChords.getCompositions());

//		inChords.getDescriptivePhrases(i);
		inChords.getMotivPhrases(i);

//		long startTimedb = System.nanoTime();    
//		DBWriter db = new DBWriter();
//		db.loadData();
//		System.out.println("Time passed DB: "+ TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTimedb));
//		System.out.println("done");		
	}

//	private static void getSimForMelody() {
//		in = new InfoGetterForMelody(NGRAM_LENGTH, LOWER_BOUND, WEIGHT_TRESHOLD, PROB_TRESHOLD);
//		readFiles(readFilesinDirectory("CSV/melody/"+currentComposer));
//		System.out.println("Done converting");
//		
//		long startTime3 = System.nanoTime();    
//		List<Result<Long>> list = in.getDescriptivePhrases();
//		long estimatedTime3 = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime3);
//		System.out.println("Time passed descPhrases: "+ estimatedTime3);
//		
//		System.out.println("done getting descPhrases");
////		DBWriter db = new DBWriter(list, currentComposer, in.getCompositions());
////		db.writeDesPhrases();
//		System.out.println("done");		
//	}

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
			TLongIntHashMap out = new TLongIntHashMap();
			TObjectIntHashMap<LongContainer> map = new TObjectIntHashMap<LongContainer>();

			try {
				br = new BufferedReader(new FileReader(csvFile));
				while ((line = br.readLine()) != null) {

			        // use comma as separator
					String[] ngramcount = line.split(",");
					if (!chords) {
						long ngram = Long.parseLong(ngramcount[0]);
						int length = NgramCoder.unpack(ngram).length;
						if ((length >= LOWER_BOUND-1 && length <= NGRAM_LENGTH) || length == 1) {
							out.put(ngram, Integer.parseInt(ngramcount[1]));
						}
					} else {
						long[] l = new long[ngramcount.length-1];
						for (int i1 = 0; i1 < l.length; i1++) {
							l[i1] = Long.parseLong(ngramcount[i1]);
						}
						int length = InfoGetterForChords.getNgramElementsCount(l);
						if (Integer.parseInt(ngramcount[ngramcount.length-1]) > SUPPORT_TRESHOLD &&((length >= LOWER_BOUND-1 && length <= NGRAM_LENGTH) || length == 1)) {
							map.put(new LongContainer(l), Integer.parseInt(ngramcount[ngramcount.length-1]));
						}
					}

				}

			 
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

			if (!chords) {
				long startTime4 = System.nanoTime();  
				in.computeAssociationRules(out, fileName);
				long estimatedTime4 = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime4);
				System.out.println("Time passed assoc: "+ estimatedTime4);
			} else {
				inChords.computeAssociationRules(map, fileName);				
			}

		}
	}
	
	public static void readFilesNio(File[] files) {
		for (int i=0; i < files.length; i++) {
			String fileName = files[i].getName();
			String csvFile = files[i].getPath();
			TLongIntHashMap out = new TLongIntHashMap();
			TObjectIntHashMap<LongContainer> map = new TObjectIntHashMap<LongContainer>();
			Path file = Paths.get(csvFile);;
	    	List<String> fileLines = null;
	    	try {
				fileLines = Files.readAllLines(file, Charset.defaultCharset());
			} catch (IOException e) {
				e.printStackTrace();
			}
	    	for (String line: fileLines) {

				String[] ngramcount = line.split(",");
				if (!chords) {
					long ngram = Long.parseLong(ngramcount[0]);
					int length = NgramCoder.unpack(ngram).length;
					if ((length >= LOWER_BOUND-1 && length <= NGRAM_LENGTH)|| length == 1) {
						out.put(ngram, Integer.parseInt(ngramcount[1]));
					}
				} else {
					long[] l = new long[ngramcount.length-1];
					for (int i1 = 0; i1 < l.length; i1++) {
						l[i1] = Long.parseLong(ngramcount[i1]);
					}
					int length = InfoGetterForChords.getNgramElementsCount(l);
					if ((length >= LOWER_BOUND-1 && length <= NGRAM_LENGTH) || length == 1) {
						map.put(new LongContainer(l), Integer.parseInt(ngramcount[ngramcount.length-1]));
					}
				}

				
			}
			if (!chords) {			
				in.computeAssociationRules(out, fileName);				
			} else {
				inChords.computeAssociationRules(map, fileName);				
			}


		}
	}
}
