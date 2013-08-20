package dataProcessing;

import gnu.trove.list.array.TByteArrayList;
import gnu.trove.map.hash.TLongIntHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.util.List;
import java.util.concurrent.TimeUnit;
 
public class ReadFileWithFileSizedBuffer
{
    public static void main(String args[])
    {
//		File fi = new File("bach_art_of_fugue_1080_1_(c)harfesoft.mid.csv");
//
//		String csvFile0 = fi.getPath();
//
//    	long startTime0 = System.nanoTime();    
//		Path fil = Paths.get(csvFile0);
//    	List<String> fileLines = null;
//    	try {
//			fileLines = Files.readAllLines(fil, Charset.defaultCharset());
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//    	
//    	for (String eins: fileLines) {
//    		String[] einst = eins.split(",");
//    	}
//    	long estimatedTime0 = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime0);
//		System.out.println("Time passed reading: "+ estimatedTime0);
//    	
    	
		long startTime5 = System.nanoTime();    

//    	Path file = Paths.get("beethoven_fantasy_for_piano_77_(c)hisamori.mid.csv");
		File file = new File("beethoven_fantasy_for_piano_77_(c)hisamori.mid.csv");
		
//    	List<String> fileArray;
//    	Charset charset = Charset.forName("US-ASCII");
		TObjectIntHashMap<LongContainer> map = new TObjectIntHashMap<LongContainer>();

    	try {
//    		BufferedReader reader = Files.newBufferedReader(file, Charset.defaultCharset());
    		FileInputStream f = new FileInputStream(file);
    		FileChannel ch = f.getChannel( );
    		MappedByteBuffer mb = ch.map(FileChannel.MapMode.READ_ONLY, 0L, ch.size( ) );
    		TByteArrayList barray = new TByteArrayList();
//    		long checkSum = 0L;
    		int nGet;
    		String test = null;
    		int oldPos = 0;
    		while( mb.hasRemaining( ) )
    		{
//    			nGet = Math.min( mb.remaining( ), 8192 );
//    		    mb.get( barray, 0, nGet );
//    		    test = new String(barray);
    			
    			byte get = mb.get();
    		    if (get == '\n') {
    		    	String s = new String(barray.toArray());
    		    	String[] ngramcount = s.split(",");
    		    	long[] l = new long[ngramcount.length-1];
					for (int i1 = 0; i1 < l.length; i1++) {
						l[i1] = Long.parseLong(ngramcount[i1]);
					}
					int length = InfoGetterForChords.getNgramElementsCount(l);
					if ((length >= 3-1 && length <= 3) || length == 1) {
//						System.out.println(s);

						map.put(new LongContainer(l), Integer.parseInt(ngramcount[ngramcount.length-1].trim()));
//						System.out.println(ngram);
					}
					
    		    	barray.reset();
//    		    	oldPos = mb.position();
//        		    test = new String(barray);
////    		    	System.out.print(test);
//
    		    } else {
        			barray.add(get);

    		    }
    		}
//		    System.out.println(test);

    		ch.close();

    	} catch (IOException x) {
			x.printStackTrace();
    	}
    	long estimatedTime5 = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime5);
		System.out.println("Time passed reading: "+ estimatedTime5);
			
		
    	
//		String csvFile = new File("beethoven_fantasy_for_piano_77_(c)hisamori.mid.csv").getPath();
//		BufferedReader br = null;
//		String line = "";
//		
//		long startTime = System.nanoTime();    
//		TObjectIntHashMap<LongContainer> map2 = new TObjectIntHashMap<LongContainer>();
//
//		try {
////			byte[] byteArray = Files.toByteArray(fi);
//			
//			br = new BufferedReader(new FileReader(csvFile));
////			br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(byteArray)));
//			while ((line = br.readLine()) != null) {
//				String[] ngramcount = line.split(",");
//				long[] l = new long[ngramcount.length-1];
//				for (int i1 = 0; i1 < l.length; i1++) {
//					l[i1] = Long.parseLong(ngramcount[i1]);
//				}
//				int length = InfoGetterForChords.getNgramElementsCount(NgramCoder.unpackS(l));
//				if ((length >= 3-1 && length <= 3) || length == 1) {
////					
//					map2.put(new LongContainer(l), Integer.parseInt(ngramcount[ngramcount.length-1]));
//				}	
//			}
//			long estimatedTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
//			System.out.println("Time passed reading: "+ estimatedTime);
//		 
//		} catch (Exception e) {
//			e.printStackTrace();
//		} finally {
//			if (br != null) {
//				try {
//					br.close();
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//			}
//		}
//            
    }
}
