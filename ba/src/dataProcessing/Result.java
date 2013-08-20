package dataProcessing;

public class Result<T> implements Comparable<Result<T>> {
	private T ngram;
	private double weight;
	private int compositionId;

	
	public Result(T ngram, double weight, int compositionName) {
		this.ngram = ngram;
		this.weight = weight;
		this.compositionId = compositionName;
	}
	
	

	public int getCompositionId() {
		return compositionId;
	}
	
	public double getWeight() {
		return weight;
	}
	
	
	public T getNgram() {
		return ngram;
	}



	public void setCompositionId(int index) {
		compositionId = index;
	}

	@Override
	public int compareTo(Result<T> o) {
		if (weight < o.weight) return 1;
		else if (weight == o.weight) return 0;
		else return -1;
	}
	
}
