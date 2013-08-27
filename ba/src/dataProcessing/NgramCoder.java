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
    
    
    public static long packAlsoNegativ(int[] a) {
        long out = 0;
        for (int i = 0; i < Math.min(8, a.length); i++) {
            out += (long) (a[i] & 0xff) << 8 * (7 - i);
        }
        return out;
    }
    
    public static long[] packChords(String b) {
        String[] ab = chordsToArray(b);
        int longCount = (int) Math.ceil(ab.length/14.0);
        long[] ngrams = new long[longCount];
    	
        for (int j = 0; j < longCount; j++){
        	long out = 0;
            byte underscores = 0;
            int count = 0;
            int underscoreCount = 0;
	        String[] a = Arrays.copyOfRange(ab, j*14, j+1 != longCount ? (j+1)*14 : ab.length);
	        for (int i = 0; i < Math.min(14, j+1 == longCount ? ab.length % 14 :14); i++) {  
	        	if (a[i].equals("_")) {
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

	        underscores *= Math.pow(2, 7-underscoreCount);
	        ngrams[j] = out | underscores;
        }

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
        for (int j = 0; j < longCount; j++){
        	long out = 0;
            int count = 0;
	        String[] a = Arrays.copyOfRange(ab, j*8, j+1 != longCount ? (j+1)*8 : ab.length);
	        for (int i = 0; i < Math.min(8, (j+1 == longCount && ab.length != 8) ? ab.length % 8 : 8); i++) {  
	        	out += Long.parseLong(a[i]) << 7 * (8 - count);
	            count++;	            
	        }
	        if (longCount > 1 && j != longCount-1) out = out + 1; 
	        ngrams[j] = out;

        }
        return ngrams;
    }
    
    /**
     * Packs only one Chord of PN Data
     * @param chord as String
     * @return
     */
    public static long[] packPN(String b) {
        String[] ab = chordToArray(b);
        int longCount = (int) Math.ceil(ab.length/7.0);
        long[] ngrams = new long[longCount];
        for (int j = 0; j < longCount; j++){
        	long out = 0;
            int count = 0;
	        String[] a = Arrays.copyOfRange(ab, j*7, j+1 != longCount ? (j+1)*7 : ab.length);
	        for (int i = 0; i < Math.min(7, (j+1 == longCount && ab.length != 7) ? ab.length % 7 : 7); i++) {  
	        	out += (Long.parseLong(a[i]) & 0xff) << 8 * (7 - count);
	            count++;	            
	        }
	        if (longCount > 1 && j != longCount-1) out = out + 1; 
	        ngrams[j] = out;

        }
        return ngrams;
    }    
    
    
	private static String[] chordToArray(String nextNgram) {
		String[] chord = new String[nextNgram.length()];
        String key = "";
        int count = 0;
        for (int i = 0; i < nextNgram.length(); i++) {
        	if (nextNgram.charAt(i) == '_' || nextNgram.charAt(i) == ' ') {
        		chord[count] = key;
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
    
    public static byte[] unpackLong(long l) {
        byte[] a = new byte[8];
        int nonNulLIndex = -1;
        for (int i = 0; i < 8; i++) {
            a[i] = (byte) ((l >>> 8 * (7 - i)) & 0xff);
            if (a[i] != 0) nonNulLIndex = i;
        }
        return Arrays.copyOfRange(a, 0, nonNulLIndex + 1);
    }
    
    public static String unpackS(long[] l) {
        String a = "";
        for (int j = 0; j < l.length; j++) {
            boolean end = false;
            boolean onlyOne = true;
        	if ((l[j] & 1) == 1) onlyOne = false;
        	
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
    
    public static String unpackSPN(long[] l) {
        String a = "";
        for (int j = 0; j < l.length; j++) {
            boolean end = false;
            boolean onlyOne = true;
        	if ((l[j] & 1) == 1) onlyOne = false;
        	
	        for (int i = 0; i < 7; i++) {
	        	long key = ((l[j] >>> 8 * (7 - i)) & 0xff);
	        	if (((l[j] >>> 8 * (7 - i - 1)) & 0xff) == 0) end = true;

	        	if (key != 0 || i==0) {
	        		if ((byte)key == -128) a += "0";
	        		else a += Byte.toString((byte)key);
	        		if (!onlyOne || !end) {
	        			a += "_";
	        		}
	        		else if (onlyOne && end && j != l.length-1) a += " ";		        		
	        	}
	        }
        }
        return a;
    }
    
    public static String unpackChords(long[] l) {
        String a = "";
        for (int j = 0; j < l.length; j++) {

	        for (int i = 0; i < 8; i++) {
	        	long key = ((l[j] >>> 7 * (8 - i)) & 0x7f);
	        	if (key != 0) {
	        		a += Long.toString(key);
	        	}
	            if (((l[j] & 0x7f) >> 7-i-1 & 1) == 1) {
	            	a += "_";
	            } else if (key != 0){
	            	a += " ";
	            }
	        }
        }
        return a;
    }



}

