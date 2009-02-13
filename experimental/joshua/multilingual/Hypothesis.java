package joshua.multilingual;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import joshua.util.sentence.Span;

public class Hypothesis {

	final Hypothesis previous;
	final List<String> target;
	
	final List<BitSet> coverageVector;
//	final BitSet coverageVector1;
//	final BitSet constraintVector2;
	
	//final List<Integer> coverageLength;
//	final int coverageSize1;
//	final int coverageSize2;
	
	final List<Float> score;
	final List<Float> distortionCost;
//	final float score1;
//	final float score2;
	
	final float cumulativeScore;
	
	public Hypothesis(int n) {
	//public Hypothesis(List<Integer> coverageLength) { //int coverageSize, int constraintCoverageSize) {
		//this.coverageLength = coverageLength;
//		this.coverageSize1 = coverageSize;
//		this.coverageSize2 = constraintCoverageSize;
		this.previous = null;
		
		//int n = coverageLength.size();
		this.coverageVector = new ArrayList<BitSet>(n);
		this.score = new ArrayList<Float>(n);
		this.distortionCost = new ArrayList<Float>(n);
		
		for (int i=0; i<n; i++) {
			this.coverageVector.add(new BitSet());
			this.score.add(0.0f);
			this.distortionCost.add(0.0f);
		}
		
//		this.coverageVector1 = new BitSet();
//		this.constraintVector2 = new BitSet();
		this.cumulativeScore = 0.0f;
//		this.score1 = 0.0f;
//		this.score2 = 0.0f;
		this.target = Collections.emptyList();
	}
	
	public Hypothesis(Hypothesis previous, List<String> target, List<BitSet> coverageVectors, List<Float> scores, List<Float> distortionCosts) {
//	public Hypothesis(Hypothesis previous, List<String> target, BitSet coverageVector, BitSet constraintCoverageVector, int coverageSize, int constraintCoverageSize, float score, float constraintScore) {
//		this.coverageSize1 = coverageSize;
//		this.coverageSize2 = constraintCoverageSize;
		this.previous = previous;
		this.coverageVector = coverageVectors;
//		this.coverageLength = coverageLengths;
		this.score = scores;
		this.distortionCost = distortionCosts;
		
		float cumulativeScore = 0.0f;
		for (float score : this.score) {
			cumulativeScore += score;
		}
		for (float distortionCost : this.distortionCost) {
			cumulativeScore += distortionCost;
		}
		this.cumulativeScore = cumulativeScore;
		
//		this.constraintVector2 = constraintCoverageVector;
//		this.coverageVector1 = coverageVector;
//		this.cumulativeScore = previous.cumulativeScore + score;
//		this.score1 = score;
//		this.score2 = constraintScore;
		this.target = target;
	}	
	
	public Collection<Span> getExtendableSourceSpans(int sourceID, int maxCoverageLength, int maxPhraseLength) {
		
		BitSet coverageVector = this.coverageVector.get(sourceID);
		int coverageLength = maxCoverageLength;//this.coverageLength.get(sourceID); 
		
		ArrayList<Span> spans = new ArrayList<Span>();
		
		for (int i=coverageVector.nextClearBit(0); i>=0 && i<coverageLength; i=coverageVector.nextClearBit(i+1)) {
			
			int last = coverageVector.nextSetBit(i);
			if (last < 0) last = coverageLength;
			if (last > i + Main.settings.MAX_PHRASE_LENGTH) last = i + Main.settings.MAX_PHRASE_LENGTH;
			
			for (int j=i+1; j<=last; j++) {
				spans.add(new Span(i,j));
				//System.out.println("[" + i + "-" + j + ")");
				//if (coverageVector.get(j)==true) break;
			}
		}
		
		return spans;
	}
	
	public int coverageCardinality(int sourceID) {
		return coverageVector.get(sourceID).cardinality();
	}
	
	public String getTranslation() {
		StringBuilder s = new StringBuilder();
		
		if (previous != null) {
			String prevTranslation = previous.getTranslation();
			if (!"".equals(prevTranslation)) {
				s.append(prevTranslation);
				s.append(' ');
			}
		}
		
		if (target != null && target.size() > 0) {
			for (int i=0; i<target.size()-1; i++) {
				s.append(target.get(i));
				s.append(' ');
			}
			s.append(target.get(target.size()-1));
			
//			if (previous != null) {
//				s.append(' ');
//			}
		}
		

		
		return s.toString();
	}
	
	public String toString() {
		StringBuilder s = new StringBuilder();
		
		s.append('\'');
		
		for (String targetWord : target) {
			s.append(targetWord);
			s.append(' ');
		}
		
		for (float score : this.score) {
			s.append(" ||| ");
			s.append(score);
		}

		for (float cost : this.distortionCost) {
			s.append(" ||| ");
			s.append(cost);
		}
		
		s.append('\'');
		
		
		return s.toString();
	}
}
