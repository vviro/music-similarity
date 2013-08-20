package dataProcessing;

/* --------------------- NgramCoder.java -------------------------*/

import java.util.Arrays;


/**
* Created with IntelliJ IDEA.
* User: Vladimir Viro
* Date: 19.03.13
* Time: 12:35
*/
public class NgramCoder {
	
	//akkorde

//    public static long pack(Integer[] a) {
//        long out = 0;
//        for (int i = 0; i < Math.min(9, a.length); i++) {
//            out += (long) a[i] << 7 * (8 - i);
//        }
//        return out;
//    }

    public static long pack(int[] a) {
        long out = 0;
        for (int i = 0; i < Math.min(9, a.length); i++) {
            out += (long) a[i] << 7 * (8 - i);
        }
        return out;
    }
    
    public static long[] packChords(String b) {
        String[] ab = chordsToArray(b);
//    	if (a.length > 14) throw new IllegalArgumentException("cannot pack longer array than 14");
        int longCount = (int) Math.ceil(ab.length/14.0);
        long[] ngrams = new long[longCount];
//        System.out.println(Math.ceil(ab.length/14.0));
    	
        for (int j = 0; j < longCount; j++){
        	long out = 0;
            byte underscores = 0;
            int count = 0;
            int underscoreCount = 0;
	        String[] a = Arrays.copyOfRange(ab, j*14, j+1 != longCount ? (j+1)*14 : ab.length);
//        	System.out.println("a"+a.length);
	        for (int i = 0; i < Math.min(14, j+1 == longCount ? ab.length % 14 :14); i++) {  
//        		System.out.println(i);
	        	if (a[i].equals("_")) {
//	        		if (i == 0) throw new IllegalArgumentException("Ngram cannot start with _");
	        		underscores = (byte) (underscores << 1);
	            	underscores += 1;
	            	underscoreCount++;
	            } else if (i != 0 && a[i].equals(" ")){
	                underscores = (byte) (underscores << 1);
	                underscoreCount++;
	            } else {
	                out += Long.parseLong(a[i]) << 7 * (8 - count);
	                count++;
	            }
	            
	        }
//	        System.out.println("s"+underscoreCount);
//	        System.out.println(ab.length % 14);
	        underscores *= Math.pow(2, 7-underscoreCount);
	        ngrams[j] = out | underscores;
//	        System.out.println(underscores);
        }
//        System.out.println(underscoreCount);
//        System.out.println("a"+ab.length % 14);
        return ngrams;
    }
    
    /**
     * Packs only one Chord
     * @param chord as String
     * @return
     */
    public static long[] pack(String b) {
        String[] ab = chordToArray(b);
        int longCount = (int) Math.ceil(ab.length/8.0);
        long[] ngrams = new long[longCount];
//        System.out.println(longCount);
        for (int j = 0; j < longCount; j++){
        	long out = 0;
            int count = 0;
	        String[] a = Arrays.copyOfRange(ab, j*8, j+1 != longCount ? (j+1)*8 : ab.length);
	        for (int i = 0; i < Math.min(8, (j+1 == longCount && ab.length != 8) ? ab.length % 8 : 8); i++) {  
//	        	System.out.println(i);
	        	out += Long.parseLong(a[i]) << 7 * (8 - count);
	            count++;	            
	        }
//	        System.out.println(out);
	        if (longCount > 1 && j != longCount-1) out = out + 1; 
	        ngrams[j] = out;
//	        System.out.println(out);

//	        System.out.println(ngrams[j]);
        }
        return ngrams;
    }
    
//    private static String[] chordToArray(String nextNgram) {
//		List<String> chordsAsArray = new ArrayList<String>();
//    	String[] chords = nextNgram.split(" ");
//		for (String chord: chords) {
//			String[] keys = chord.split("_");
//			for (int i = 0; i < keys.length; i++) {
//				chordsAsArray.add(keys[i]);
//				if (i != keys.length - 1) chordsAsArray.add("_");
//
//			}
//			chordsAsArray.add(" ");
//		}
//		return chordsAsArray.toArray(new String[chordsAsArray.size()]);
//	}
    
	private static String[] chordToArray(String nextNgram) {
		String[] chord = new String[nextNgram.length()];
        String key = "";
        int count = 0;
        for (int i = 0; i < nextNgram.length(); i++) {
        	if (nextNgram.charAt(i) == '_' || nextNgram.charAt(i) == ' ') {
        		chord[count] = key;
//        		chord[count+1] = Character.toString(nextNgram.charAt(i));
        		count++;
        		key = "";
        		continue;
        	}
        	key += nextNgram.charAt(i);
        	if (i == nextNgram.length() - 1) chord[count] = key;
        	
		}
        return Arrays.copyOfRange(chord, 0, count + 1);
	}
    
	private static String[] chordsToArray(String nextNgram) {
		String[] chord = new String[nextNgram.length()];
        String key = "";
        int count = 0;
        for (int i = 0; i < nextNgram.length(); i++) {
        	if (nextNgram.charAt(i) == '_' || nextNgram.charAt(i) == ' ') {
        		chord[count] = key;
        		chord[count+1] = Character.toString(nextNgram.charAt(i));
        		count = count + 2;
        		key = "";
        		continue;
        	}
        	key += nextNgram.charAt(i);
        	if (i == nextNgram.length() - 1) chord[count] = key;
        	
		}
        return Arrays.copyOfRange(chord, 0, count + 1);
	}
    
    public static short[] unpack(long l) {
        short[] a = new short[9];
        int nonNulLIndex = -1;
        for (int i = 0; i < 9; i++) {
            a[i] = (short) ((l >>> 7 * (8 - i)) & 0x7f);
            if (a[i] != 0) nonNulLIndex = i;
        }
        return Arrays.copyOfRange(a, 0, nonNulLIndex + 1);
    }
    
    public static String unpackS(long[] l) {
        String a = "";
        for (int j = 0; j < l.length; j++) {
            boolean end = false;
            boolean onlyOne = true;
//	        long underscores = (l[j] & 0x7f);
        	if ((l[j] & 1) == 1) onlyOne = false;
        	
//        	System.out.println(l[j]);
	        for (int i = 0; i < 8; i++) {
	        	long key = ((l[j] >>> 7 * (8 - i)) & 0x7f);   
	        	if (((l[j] >>> 7 * (8 - i-1)) & 0x7f) == 0) end = true;
	        	if (key != 0) {
	        		a += Long.toString(key);
	        		if (!onlyOne || !end) a += "_";
	        		else if (onlyOne && end && j != l.length-1) a += " ";	        		
	        	}
	        }
        }
        return a;
    }
    
    public static String unpackChords(long[] l) {
        String a = "";
        for (int j = 0; j < l.length; j++) {
//	        long underscores = (l[j] & 0x7f);
//        	System.out.println(l[j]);
	        for (int i = 0; i < 8; i++) {
	        	long key = ((l[j] >>> 7 * (8 - i)) & 0x7f);
	        	if (key != 0) {
	        		a += Long.toString(key);
	        	}
	//            System.out.println(underscores >> 7-i-1);
	            if (((l[j] & 0x7f) >> 7-i-1 & 1) == 1) {
	            	a += "_";
	            } else if (key != 0){
	            	a += " ";
	            }
//	            System.out.println(a+"j:"+j);
	        }
        }
        return a;
    }

    public static void main(String[] args) {
        String nextNgram = "45_49_52_57_79_85_88_91_66_88";
		long startTime5 = System.nanoTime();    

        long[] test = pack(nextNgram);
//        for (long n: test) {
//        	System.out.println("aaa"+n);
//        }
//        String[] test = chordToArrayN(nextNgram);
        long[] j = new long[test.length*3];
    	System.arraycopy(test, 0, j, 0, test.length);
    	System.arraycopy(test, 0, j, test.length, test.length);
    	System.arraycopy(test, 0, j, test.length*2, test.length);

    	System.out.println("elems: "+InfoGetterForChords.getNgramElementsCount(j));
    	System.out.println("last: "+ unpackS(InfoGetterForChords.getLastElement(j)));
    	System.out.println("left: "+ unpackS(InfoGetterForChords.getNgramUpTo(j)));

        long estimatedTime5 = System.nanoTime() - startTime5;
		System.out.println("Time passed reading: "+ estimatedTime5);
//        for(String n: test){
        	System.out.println(unpackS(j));
//    	System.out.print(n + " ");

//        }
        	
//        	763601
//        	882929
//        String first = nextNgram.substring(0, nextNgram.trim().lastIndexOf(" "));
//        String last = nextNgram.substring(nextNgram.trim().lastIndexOf(" ")+1);
//        System.out.println(first);
//        System.out.println(last);
//    	long[] f = NgramCoder.pack(first);
//    	long[] l = NgramCoder.pack(last);
//    	long[] all = new long[f.length + l.length];
//		System.arraycopy(f, 0, all, 0, f.length);
//		System.arraycopy(l, 0, all, f.length, l.length);
//		for (long s: f){
//    		System.out.println("first: "+ s);
//    	}
//    	for (long s: l){
//    		System.out.println("last: "+ s);
//    	}
//    	for (long s: all){
//    		System.out.println("all: "+ s);
//    	}
//        String u = NgramCoder.unpackS(all).trim();
//        for (String s: u){
//        	System.out.print(u);
//        	System.out.println();
//        long[] t = InfoGetterForChords.getNgramUpTo(u, 2);
//            System.out.println(NgramCoder.unpackS(t));
//        for (long r: t) {
//        	System.out.println("r: "+r);
//        }

//        }
//        int i = 2;
//        String[] elements = nextNgram.split(" ");
//		String newNgram = "";
//		for (int n = 0; n < i; n++){
//			newNgram += elements[n] + " ";
//		}
//		System.out.println(newNgram);
	    

    }


}

