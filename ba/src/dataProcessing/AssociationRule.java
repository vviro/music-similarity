package dataProcessing;

public class AssociationRule<T> {
	private double leftSupport;
	private double rightSupport;
//	private double probability;
	private T ngram;
	private double support;
	
	public AssociationRule(T ngram, double support, double left, double right) {
		this.leftSupport = left;
		this.rightSupport = right;
//		this.probability = probability;
		this.ngram = ngram;
		this.support = support;
	}
	
	public T getNgram() {
		return ngram;
	}
	
	public double getSupport() {
		return support;
	}
	
//	public double getProbability() {
//		return probability;
//	}
	
	public double getLeftSupport() {
		return leftSupport;
	}
	
	public double getRightSupport() {
		return rightSupport;
	}
	
//	public void printLeft() {
//		for (short s: left.getUnpackedNgram()) {
//			System.out.print(s);
//		}
//	}
//	
//	public void printRight(){
//		for (short s: right.getUnpackedNgram()) {
//			System.out.print(s);
//		}
//	}
}
