package dataProcessing;

public class Note implements Comparable<Note> {

	private long start;
	private long end;
	private int key;
	private int rhythm;

	//set time of on-event and key value
	public Note(long start, int key) {
		this.start = start;
		this.key = key;
		this.rhythm = 0;
	}
	
	
	public int getRhythm() {
		return rhythm;
	}
	
	//get time of on-event
	public long getStart() {
		return start;
	}
	
	//get time of off-event
	public long getEnd() {
		return end;
	}
	
	//get key of note
	public int getKey() {
		return key;
	}
	
	//set time of off-event
	public void setEnd(long end) {
		this.end = end;
		rhythm = (int) (this.end-start);
	}
	

	//notes are ordered by the time they were played
	public int compareTo(Note note) {
		if (start < note.getStart()) return -1;
		else if (start > note.getStart()) return 1;
		else return 0;
	}
}
