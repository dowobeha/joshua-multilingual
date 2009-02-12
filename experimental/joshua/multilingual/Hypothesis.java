package joshua.multilingual;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import joshua.util.sentence.Span;

public class Hypothesis {

	final Hypothesis previous;
	final BitSet coverageVector;
	//final BitSet constraintoverageVector;
	
	final int coverageSize;
	
	final float score;
	final float cumulativeScore;
	
	final List<String> target;
	
	public Hypothesis(int coverageSize) {
		this.coverageSize = coverageSize;
		this.previous = null;
		this.coverageVector = new BitSet();
		this.cumulativeScore = 0.0f;
		this.score = 0.0f;
		this.target = Collections.emptyList();
	}
	
	public Hypothesis(Hypothesis previous, List<String> target, BitSet coverageVector, int coverageSize, float score) {
		this.coverageSize = coverageSize;
		this.previous = previous;
		this.coverageVector = coverageVector;
		this.cumulativeScore = previous.cumulativeScore + score;
		this.score = score;
		this.target = target;
	}	
	
	public Collection<Span> getExtendableSourceSpans(int maxPhraseLength) {
		
		ArrayList<Span> spans = new ArrayList<Span>();
		
		for (int i=coverageVector.nextClearBit(0); i>=0 && i<coverageSize; i=coverageVector.nextClearBit(i+1)) {
			
			int last = coverageVector.nextSetBit(i);
			if (last < 0) last = coverageSize;
			if (last > i + Main.settings.MAX_PHRASE_LENGTH) last = i + Main.settings.MAX_PHRASE_LENGTH;
			
			for (int j=i+1; j<=last; j++) {
				spans.add(new Span(i,j));
				//System.out.println("[" + i + "-" + j + ")");
				//if (coverageVector.get(j)==true) break;
			}
		}
		
//		for(int i=coverageVector.nextClearBit(0), j=coverageVector.nextSetBit(i+1); i>=0; i=j, j=coverageVector.nextSetBit(j+1)) {
//			
//		}
//		
		//throw new RuntimeException();
		return spans;
	}
	
	public int coverage() {
		return coverageVector.cardinality();
	}
	
	public String getTranslation() {
		StringBuilder s = new StringBuilder();
		
		if (target != null && target.size() > 0) {
			for (int i=0; i<target.size()-1; i++) {
				s.append(target.get(i));
				s.append(' ');
			}
			s.append(target.get(target.size()-1));
			
			if (previous != null) {
				s.append(' ');
			}
		}
		
		if (previous != null) {
			s.append(previous.getTranslation());
		}
		
		return s.toString();
	}
	
	public String toString() {
		StringBuilder s = new StringBuilder();
		
		//s.append(target.size());
		
		s.append('\'');
		
		for (String targetWord : target) {
			s.append(targetWord);
			s.append(' ');
		}
		
		s.append("||| ");
		s.append(score);
		s.append('\'');
		
		
		return s.toString();
	}
}
