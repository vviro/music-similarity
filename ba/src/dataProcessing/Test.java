package dataProcessing;

import gnu.trove.map.hash.TObjectIntHashMap;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class Test {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
			DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		   //get current date time with Date()
		   Date date = new Date();
		   System.out.println(dateFormat.format(date));
	 
		   //get current date time with Calendar()
		   Calendar cal = Calendar.getInstance();
		   int year = cal.get(Calendar.YEAR);
		   int month = cal.get(Calendar.MONTH);
		   int day = cal.get(Calendar.DAY_OF_MONTH);
		   System.out.println(String.valueOf(year) + " " + String.valueOf(month)+ " " +String.valueOf(day) );
		   System.out.println(dateFormat.format(cal.getTime()));
		
//		String[] array = {"55_66", "3_4" ,"22_5" ,"3_4" ,"5"};
//		for (int i = 0; i <= array.length; i++) {
//			String keys ="";
//			for (int len = 0; len < (array.length-i < 3 ? array.length-i: 3); len++) {
//				keys = keys + array[i + len] + " ";
//				System.out.println(keys);
//			}
//		}
		
		
//		String[] a = {"a","b"};
//		String[] b = {"c", "d"};
//		String[] elements = new String[a.length + b.length];
//		System.arraycopy(a, 0, elements, 0, a.length);
//		System.arraycopy(b, 0, elements, a.length, b.length);
//		for(String s: elements) System.out.println(s);
		
//		Integer[] a = {55,56,88};
//		Ngram n = new Ngram(NgramCoder.pack(a),5);
//		
//		for(short s: NgramCoder.unpack(n.slice(2,3))){
//			System.out.println(s);
//		}
//		Composer i = new Composer(3,3);
//		int[] a = {55,56,88,66,77,88,44,33,77};
//		long l = NgramCoder.pack(a);
//		for (short s: NgramCoder.unpack(l)) {
//			System.out.print(s);
//		}
//		long[] a = {1,2,3};
//		long[] b = {6,7,8};
//		long[] c = {1,2,3};
//		
//		TObjectIntHashMap<Container> map = new TObjectIntHashMap<Container>();
//		map.put(new Container(a), 1);
//		map.put(new Container(b), 2);
//		map.put(new Container(c), 3);
//		System.out.println("a: " + map.get(new Container(a)));
//		System.out.println("b: " + map.get(new Container(b)));
//		System.out.println("c: " + map.get(new Container(c)));
//		

		
		
//		int[] a = {22,33,44,55,66,77,88,99};
//		Composer s = new Composer(4, 4);
//		long[] l = s.getNgrams(a, 4);
//		for (long k: l){
//			for (short u: NgramCoder.unpack(k)) {
//				System.out.print(u);
//			}
//			System.out.println();
//		}
		
//		StringBuffer b = new StringBuffer();
//		b.append("60_44" + " ");
//		b.append("55_54");
//		System.out.println(b.toString() =="60_44 55_54");
//		String s = "60_65_69_72 51_58_63_67_79 51_58_63_67_70_79";
//		System.out.println(s.substring(0, s.lastIndexOf(" ")));
//		byte by = '_';
//		for (byte c: s.getBytes())
//		System.out.println((char)c);
	}
	

	
	public static class Container {
		public long[] value;
		
		public Container(long[] v) {
			this.value = v;
		}
		
		@Override
		public int hashCode() {
			return Arrays.hashCode(value);
		}
		
		@Override
		public boolean equals(Object obj) {
			if(obj instanceof Container) {
				return Arrays.equals(value, ((Container)obj).value);
			}
			else {
				return false;
			}
		}
	}

}
