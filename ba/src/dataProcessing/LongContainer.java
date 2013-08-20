package dataProcessing;

import java.util.Arrays;

public class LongContainer {
	private long[] ngramLong;
	
	public LongContainer(long[] ngramLong) {
		this.ngramLong = ngramLong;
	}
	
	public long[] getLongArray() {
		return ngramLong;
	}
	
	@Override
	public int hashCode() {
		return Arrays.hashCode(ngramLong);
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof LongContainer) {
			return Arrays.equals(ngramLong, ((LongContainer)obj).ngramLong);
		}
		else {
			return false;
		}
	}
}
