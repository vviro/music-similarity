package dataPreparation;

import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TObjectIntHashMap;

import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.TimeUnit;



import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.google.common.collect.TreeMultimap;
import com.google.common.io.Files;

import dataProcessing.InfoGetterForMelody;
import dataProcessing.InfoGetterForLeimotifs;
import dataProcessing.LongContainer;
import dataProcessing.NgramCoder;

public class MIDIParser 
{
	private static final int NOTE_ON = 0x90;
	private static final int NOTE_OFF = 0x80;
	
	public static String[] DIRECTORY;

	public static String currentComposer;
//	private static InfoGetterForChords inChords;
	private static final int NGRAM_LENGTH = 10;
	private static final int SUPPORT_TRESHOLD = 2;

    private static TObjectIntHashMap<LongContainer> countMap  = new TObjectIntHashMap<LongContainer>();

//	private static final int LOWER_BOUND = 8;
	static boolean leitmotive = true;


	public static void main(String[] args) {
		File f = new File("data");
		File[] files = f.listFiles();
//		DIRECTORY = new String[files.length];
		DIRECTORY = new String[]{"verdi"};

//		for (int i = 0; i < files.length; i++) {
//			DIRECTORY[i] = files[i].getName();
//		}
		
		long startTime = System.nanoTime();    
		
		for (String s: DIRECTORY) {
			currentComposer = s;
			System.out.println(s);
			if (!leitmotive){
				countMap  = new TObjectIntHashMap<LongContainer>();
				MIDIParser.convertFiles(readFilesinDirectory("data/"+currentComposer));
				System.out.println("Done converting");
			} else {
//				inChords = new InfoGetterForChords(NGRAM_LENGTH, 0, 0, 0);
				MIDIParser.convertFiles(readFilesinDirectory("data/"+currentComposer));
				System.out.println("Done converting");
			}
		}
		long estimatedTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
		System.out.println("Time passed: "+ estimatedTime);
	}
	
	public static File[] readFilesinDirectory(String directory) {
		File f = new File(directory);
		return f.listFiles();
	}
	
	public static void convertFiles(File[] files) {
		for (int i=0; i < files.length; i++) {
			try {
				String fileName = files[i].getName();
				byte[] byteArray = Files.toByteArray(files[i]);
				if (!leitmotive) {
					ArrayList<String> out2 = parseChords(byteArray, i);
					getFrequencies(out2, fileName);

				} else {
					ArrayList<String> out2 = parseChords(byteArray, i);
					writeNgramsToFile("CSV/leitmotive/" + MIDIParser.currentComposer + ".csv", getFrequencies(out2, fileName));

				}
				
			} catch (Exception e) {
//				e.printStackTrace();
				System.out.println("Cannot read File: " + files[i]);
			}
		}
		if (!leitmotive) {
			writeNgramsToFile("CSV/chordsComp/" + MIDIParser.currentComposer + ".csv", countMap);

		}
	}
	
	public static void writeNgramsToFile(String filename, TObjectIntHashMap<LongContainer> countMap) {
		//write how often a key appears in song 
		try {		
			File output = new File(filename);
		    FileWriter fw = new FileWriter(output);
				
		    BufferedWriter writer = new BufferedWriter(fw);
		        
			Collection<LongContainer> entrys = countMap.keySet();
			Iterator<LongContainer> iterator = entrys.iterator();
			while (iterator.hasNext()) {
				LongContainer next = iterator.next();
				if (countMap.get(next) > SUPPORT_TRESHOLD) {
					for (long l: next.getLongArray()) {
						writer.write(l + ",");
					}
					writer.write(countMap.get(next) + "\r\n");
				}
			}
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static ArrayList<String> parseChords(byte[] rawmidi, int fileNumber) throws Exception {
		ArrayList<String> list = new ArrayList<String>();
		InputStream is = new ByteArrayInputStream(rawmidi);
		Sequence sequence = MidiSystem.getSequence(is);

		// time x pitch
		Multimap<Long, Integer> onEvents  = ArrayListMultimap.create();
		Multimap<Long, Integer> offEvents = ArrayListMultimap.create();

		// accumulating note on and off events over all voices in a common timeline

		for (Track track : sequence.getTracks()) {
			for (int i = 0; i < track.size(); i++) {
				MidiEvent event = track.get(i);
				MidiMessage message = event.getMessage();
				if (message instanceof ShortMessage) {
					ShortMessage sm = (ShortMessage) message;
					int key = sm.getData1();
					long time = event.getTick();
					if (sm.getCommand() == NOTE_ON) {
						int velocity = sm.getData2();
						if (key > 0 && velocity > 0) {
							onEvents.put(time, key);
						}
						else if (key > 0 && velocity == 0) {
							offEvents.put(time, key);
						}
					} else if (sm.getCommand() == NOTE_OFF && key > 0) {
						offEvents.put(time, key);
					}
				}
			}
		}
		// time points at which notes are turned on or off - chord boundaries
		Set<Long> intervalBounds = ImmutableSortedSet.copyOf(
				Sets.union(onEvents.keySet(), offEvents.keySet())
				.immutableCopy());
		Multimap<Long, Integer> chords = TreeMultimap.create();
		ImmutableSet<Integer> thisChord;
		
		long prevTick = 0;
		for (Long tick : intervalBounds) {
			if (prevTick == 0) {
				thisChord = ImmutableSet.copyOf(onEvents.get(tick));
				chords.putAll(tick, onEvents.get(tick));
			} else {
				ImmutableSet<Integer> prevChord = ImmutableSet.copyOf(chords.get(prevTick));
				ImmutableSet<Integer> restNotes = Sets.difference(prevChord, Sets.newHashSet(offEvents.get(tick))).immutableCopy();
				thisChord = Sets.union(restNotes, Sets.newHashSet(onEvents.get(tick))).immutableCopy();
				chords.putAll(tick, thisChord);
			}
			
			if (offEvents.containsKey(tick)) {
				Integer[] a = new Integer[thisChord.size()];
				thisChord.toArray(a);
				Arrays.sort(a); 
	
				String chord = "";
				for (int i = 0; i < a.length; i++) {
					if (i == a.length -1) {
						chord += a[i];
					} else {
						chord += a[i] + "_";
					}
				}
				if (!chord.equals("")) {
					list.add(chord);
//					System.out.println(tick +" "+chord);
				}
			}
			prevTick = tick;
		}
//		System.out.println(chords.entries());
		return list;
	}    
	
	public static TIntList parseTimewise(byte[] rawmidi, int fileNumber) throws Exception {
		TIntList list = new TIntArrayList();

		TreeMultimap<Long, Integer> out = TreeMultimap.create();
		InputStream is = new ByteArrayInputStream(rawmidi);
		
		Sequence sequence = MidiSystem.getSequence(is);
		
//		File output = new File("txt/outputTimewise" + fileNumber + ".txt");

		long trackNumber = 0;

		int key = 0;
		long time = 0;
//		Files.write("",output,Charsets.UTF_8);
		
		for (Track track : sequence.getTracks()) {
			trackNumber++;
			//Files.append("Track: "+trackNumber+ "\r\n",output,Charsets.UTF_8); // write track number
			for (int i = 0; i < track.size(); i++) { 
				MidiEvent event = track.get(i);
				MidiMessage message = event.getMessage();
				if (message instanceof ShortMessage) {
					ShortMessage sm = (ShortMessage) message;

					if (sm.getCommand() == NOTE_ON) {
						key = sm.getData1();
						int velocity = sm.getData2();
						if (key > 0 && velocity > 0) {
							time = event.getTick();
							out.put(time, key);
							list.add(key);

						}
						
					}

				}
			}

		}
		//write results to file
//		Set<Map.Entry<Long, Integer>> entries = out.entries();
//		Iterator<Map.Entry<Long, Integer>> it = entries.iterator();
//		while (it.hasNext()) {
//			Map.Entry<Long ,Integer> next = it.next();
//			Files.append(next.getValue() + " ", output, Charsets.UTF_8);
//		}
		is.close();
		return list;
	}
	
	
	public static TObjectIntHashMap<LongContainer> getFrequencies(ArrayList<String> out, String fileName) throws IOException {


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
	    
		
		return countMap;
	}
	
//	public static ImmutableMultimap<Long, Integer> parseTrackwise(byte[] rawmidi, int fileNumber) throws Exception {
//
//		Multimap<Long, Integer> out = TreeMultimap.create();
//
//		InputStream is = new ByteArrayInputStream(rawmidi);
//		Sequence sequence = MidiSystem.getSequence(is);
//		File output = new File("txt/output" + fileNumber + ".txt");
//
//		long trackNumber = 0;
//		long noteNumber = 0;
//	    Files.write("", output, Charsets.UTF_8); //make new file
//
//		for (Track track : sequence.getTracks()) {
//			trackNumber++;
//		    Files.append("Track: "+ trackNumber +"\r\n", output, Charsets.UTF_8);
//			for (int i = 0; i < track.size(); i++) { 
//				MidiEvent event = track.get(i);
//				MidiMessage message = event.getMessage();
//				if (message instanceof ShortMessage) {
//					ShortMessage sm = (ShortMessage) message;
//					int key = sm.getData1();
//
//					if (sm.getCommand() == NOTE_ON) {
//						int velocity = sm.getData2();
//						if (key > 0 && velocity > 0) {				
//							//write infos(event command, key, tick on which event occurs)
////						    Files.append("Command: "+sm.getCommand()+" Note: "+key+ " DeltaTime: "+event.getTick()+ "\r\n", output, Charsets.UTF_8);
//							out.put(noteNumber, key);
//							Files.append("K" + key + " ", output, Charsets.UTF_8);
//							noteNumber++;
//						}
//						if (key > 0 && velocity == 0) { //write Note-Off
////						    Files.append("Command Note-OFF VEL: "+" Note: "+key+ " DeltaTime: "+event.getTick()+ "\r\n", output, Charsets.UTF_8);
//
//						}
//					}else if (key > 0 && sm.getCommand() == NOTE_OFF) {
////						    Files.append("Command Note-OFF : "+" Note: "+key+ " DeltaTime: "+event.getTick()+ "\r\n", output, Charsets.UTF_8);
//
//					}
//				}
//			}
//			Files.append("\r\n", output, Charsets.UTF_8);
//
//			noteNumber += 1000000000000l + 1;
//		}
//
//		return ImmutableMultimap.copyOf(out);
//	}
	

	
//	public static Multimap<Long, Integer> parseTimewisewithRythm(byte[] rawmidi, int fileNumber) throws Exception {
//		Multiset<Note> out = TreeMultiset.create();
//		Map<Integer, Note> onNotes = new HashMap<Integer ,Note>();
//		InputStream is = new ByteArrayInputStream(rawmidi);
//		Sequence sequence = MidiSystem.getSequence(is);
//		
//		File output = new File("txt/outputRythmTimewise" + fileNumber + ".txt");
//
//		long trackNumber = 0;
//		long time = 0;
//		
//		Files.write("", output, Charsets.UTF_8);
//		
//		for (Track track : sequence.getTracks()) {
//			trackNumber++;
//			//Files.append("Track: "+trackNumber+ "\r\n",output,Charsets.UTF_8); // write track number
//
//			for (int i = 0; i < track.size(); i++) { 
//				MidiEvent event = track.get(i);
//				MidiMessage message = event.getMessage();
//				if (message instanceof ShortMessage) {
//					ShortMessage sm = (ShortMessage) message;
//					int velocity = sm.getData2();
//					int key	= sm.getData1();
//
//					if (sm.getCommand() == NOTE_ON) {
//						if (key > 0 && velocity > 0) {
//							time= event.getTick();
//							Note note = new Note(time,key); // make new note
//							out.add(note);
//							onNotes.put(key, note);
//						} else if (velocity == 0) { //set time of note-off
//								onNotes.get(key).setEnd(event.getTick());
//								onNotes.remove(key);
//						}
//					} else if (sm.getCommand() == NOTE_OFF) {
//						try {
//							onNotes.get(key).setEnd(event.getTick());
//							onNotes.remove(key);
//						} catch(NullPointerException e) {
//							//toDo: Noten die mehrmals gleichzeitig gespielt werden
//						}
//					}
//				}
//			}
//
//		}
//		//ImmutableMultimap<String, Integer> finalOut= TreeMultimap.copyOf(out);
////		TreeMultimap<Long, Long> finalOut  = TreeMultimap.create();
////		ArrayList<Long> finalOut = new ArrayList<Long>();
//		Multimap<Long, Integer> finalOut= TreeMultimap.create();
//		Set<Multiset.Entry<Note>> entries = out.entrySet();
//		Iterator<Multiset.Entry<Note>> it = entries.iterator();
//		long noteNumber = 1000000000000l;
//		
//		while (it.hasNext()) {
//			Multiset.Entry<Note> next = it.next();
//			for (int i = 0; i < next.getCount(); i++) {
//				noteNumber++;
//				finalOut.put(next.getElement().getStart() + noteNumber, next.getElement().getRhythm());
//				Files.append(next.getElement().getRhythm() + " ", output, Charsets.UTF_8);
//			}
//			
//		}
////		System.out.println(out.entries());
////		System.out.println(finalOut.entries());
//
//		return finalOut;
//	}
	
//	public static Multimap<Long, Integer> parseTrackwisewithRythm(byte[] rawmidi, int fileNumber) throws Exception {
//
//		Multimap<Long, Note> out = TreeMultimap.create();
//		Map<Integer, Note> onNotes = new HashMap<Integer, Note>();
//
//		InputStream is = new ByteArrayInputStream(rawmidi);
//		Sequence sequence = MidiSystem.getSequence(is);
//		
//		File output = new File("txt/outputRythmTrackwise" + fileNumber + ".txt");
//
//		long trackNumber = 0;
//
//		int key = 0;
//		long time = 0;
//		boolean on = false;
//		
//		Files.write("", output, Charsets.UTF_8);
//		
//		for (Track track : sequence.getTracks()) {
//			trackNumber++;
//			//Files.append("Track: "+trackNumber+ "\r\n",output,Charsets.UTF_8); // write track number
//			for (int i = 0; i < track.size(); i++) { 
//				MidiEvent event = track.get(i);
//				MidiMessage message = event.getMessage();
//				if (message instanceof ShortMessage) {
//					ShortMessage sm = (ShortMessage) message;
//					int velocity = sm.getData2();
//					key = sm.getData1();
//
//					if (sm.getCommand() == NOTE_ON) {
//						if (key > 0 && velocity > 0) {
////							on=true;
//							time = event.getTick();
//							Note note = new Note(time, key);
//							out.put(trackNumber, note);
//							onNotes.put(key, note);
//						} else if (velocity == 0) { //write Note-Off
//							onNotes.get(key).setEnd(event.getTick());
//							onNotes.remove(key);
//						}
//					} else if (sm.getCommand() == NOTE_OFF) {
//								try {
//									onNotes.get(key).setEnd(event.getTick());
//									onNotes.remove(key);
//								} catch(NullPointerException e) {
//									//noten die mehrmals gleichzeitig gespielt werden???
//								}
//								//Files.append("K"+Integer.toString(key)+" ", output, Charsets.UTF_8);
//					}
//
//				}
//
//			}
//			
//		}
//		
//		//ImmutableMultimap<String, Integer> finalOut= TreeMultimap.copyOf(out);
//		Multimap<Long, Integer> finalOut  = TreeMultimap.create();
//		Collection<Entry<Long, Note>> entries = out.entries();
//		Iterator<Entry<Long, Note>> it = entries.iterator();
//		
//		while (it.hasNext()) {
//			Entry<Long, Note> next = it.next();
//			finalOut.put(next.getKey() * 1000000000000l + next.getValue().getStart(), next.getValue().getRhythm());
////			System.out.println(finalOut.entries());
//			Files.append(next.getValue().getRhythm() + " ", output, Charsets.UTF_8);
//
//		}
////		System.out.println(out.entries());
////		System.out.println(finalOut.entries());
//
//		return finalOut;
//	}
 
//
//
//
//	public static ImmutableMultimap<Long, Integer> parseChordsTrackwise(byte[] rawmidi) throws Exception {
//
//		InputStream is = new ByteArrayInputStream(rawmidi);
//		Sequence sequence = MidiSystem.getSequence(is);
//
//		// time x pitch
//		Multimap<Long, Integer> onEvents  = ArrayListMultimap.create();
//		Multimap<Long, Integer> offEvents = ArrayListMultimap.create();
//
//		// accumulating note on and off events over all voices in a common timeline
//
//		Multimap<Long, Integer> allChords = TreeMultimap.create();
//		long tickTrack = 0;
//
//		for (Track track : sequence.getTracks()) {
//
//			for (int i = 0; i < track.size(); i++) {
//				MidiEvent event = track.get(i);
//				MidiMessage message = event.getMessage();
//				if (message instanceof ShortMessage) {
//					ShortMessage sm = (ShortMessage) message;
//					int key = sm.getData1();
//					long time = event.getTick();
//					if (sm.getCommand() == NOTE_ON) {
//						int velocity = sm.getData2();
//						if (key > 0 && velocity > 0) {
//							onEvents.put(time, key);
//						}
//						else if (key > 0 && velocity == 0) {
//							offEvents.put(time, key);
//						}
//					} else if (sm.getCommand() == NOTE_OFF && key > 0) {
//						offEvents.put(time, key);
//					}
//				}
//			}
//			// time points at which notes are turned on or off - chord boundaries
//			Set<Long> intervalBounds = 
//					ImmutableSortedSet.copyOf(Sets.union(onEvents.keySet(), offEvents.keySet()).immutableCopy());
//			Multimap<Long, Integer> chords = TreeMultimap.create();
//			Multiset<Integer> activeNotes = HashMultiset.create();
//
//			long prevTick = 0;
//			for (Long tick : intervalBounds) {
//				if (prevTick == 0) {
//					chords.putAll(tick + tickTrack, onEvents.get(tick));
//				} else {
//					ImmutableSet<Integer> prevChord = ImmutableSet.copyOf(chords.get(prevTick));
//					ImmutableSet<Integer> restNotes = Sets.difference(prevChord, Sets.newHashSet(offEvents.get(tick))).immutableCopy();
//					ImmutableSet<Integer> thisChord = Sets.union(restNotes, Sets.newHashSet(onEvents.get(tick))).immutableCopy();
//					chords.putAll(tick + tickTrack, thisChord);
//				}
//				prevTick = tick;
//			}
//
//			allChords.putAll(chords);
//			//tickTrack += Collections.max(intervalBounds) + 1;
//			tickTrack += 1000000000000l + 1;
//		}
//
//		ImmutableMultimap<Long, Integer> out = ImmutableMultimap.copyOf(allChords);
//		return out;
//	}     
}
