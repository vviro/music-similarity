package dataProcessing;

import gnu.trove.list.array.TDoubleArrayList;
import gnu.trove.map.hash.TObjectIntHashMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public abstract class InfoGetter {

	protected static double WEIGHT_TRESHOLD;
	protected static double PROB_TRESHOLD;
	protected static int NGRAM_LENGTH;
	protected static int LOWER_BOUND;
	protected static int compositionId = 0;
	protected static final int MAX_PHRASE_COUNT = 15;
	
	protected static List<LongContainer> results = new ArrayList<LongContainer>();
	
	protected ArrayList<HashMap<LongContainer,Double[]>> documents;
	protected TDoubleArrayList max;
	protected ArrayList<String> compositions;
	protected TObjectIntHashMap<LongContainer> ngramInDocCount;
	

	public ArrayList<String> getCompositions() {
		return compositions;
	}

	
	public static int getNgramElementsCount(long[] ngrams) {
		int counter = 0;
		for (long l: ngrams) {
			if ((l & 1) == 0) counter++;
		}
		return counter;
	}
	
	public static long[] getLastElement(long[] ngrams) {
		for (int i = ngrams.length-1; i > 0; i--){
			if ((ngrams[i-1] & 1) == 0) return Arrays.copyOfRange(ngrams, i, ngrams.length);
		}
		return ngrams;
	}
	
	public static long[] getNgramUpTo(long[] ngrams) {
		return Arrays.copyOfRange(ngrams, 0, ngrams.length - getLastElement(ngrams).length);
	}
	
	protected boolean isSubstring(String ngram, String subngram) {
		boolean isSubstring = false;
		if (subngram.split(" ").length > 2) {
			isSubstring = ngram.contains(subngram.substring(0, subngram.lastIndexOf(' ') - 1))
			|| ngram.contains(subngram.substring(subngram.indexOf(' ') + 1));
		}
		return ngram.contains(subngram) || subngram.contains(ngram) || isSubstring;
	}

	public abstract List<Result<LongContainer>> getMotivPhrases(int composerIndex);
	
	public abstract HashMap<LongContainer, Double[]> computeAssociationRules(TObjectIntHashMap<LongContainer> map, String fileName);

	
	public abstract List<Result<LongContainer>> cutResults(List<List<Result<LongContainer>>> results,
			TDoubleArrayList maxWeights, int composerIndex);

}
