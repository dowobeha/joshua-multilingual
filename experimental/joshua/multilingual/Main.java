/* This file is part of the Joshua Machine Translation System.
 * 
 * Joshua is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free
 * Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 * MA 02111-1307 USA
 */
package joshua.multilingual;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import joshua.util.ArrayBackedList;
import joshua.util.ArrayBackedStringList;

/**
 * @author Lane Schwartz
 */
public class Main {

	private static final Logger logger = Logger.getLogger(Main.class.getName());
	
	static int maxPhraseLength = 5;
	static Settings settings = new Settings(maxPhraseLength);
	
	/**
	 * @param args
	 * @throws FileNotFoundException 
	 */
	public static void main(String[] args) throws FileNotFoundException {
				
		// Read phrase tables
		PhraseTable table1 = new PhraseTable(args[1], 0);
		PhraseTable table2 = new PhraseTable(args[3], Direction.Reverse, 1);
		logger.finer("Phrase table has " + table1.size() + " entries.");
		
		// Read input sentences
		Scanner sentences1 = new Scanner(new File(args[0]));
		Scanner sentences2 = new Scanner(new File(args[2]));
		
		// For each input language
		while (sentences1.hasNextLine() && sentences2.hasNextLine()) {
			
			// Construct translation options for this input->output
			final String inputSentence1 = sentences1.nextLine();
			final String inputSentence2 = sentences2.nextLine();
			
			// Store each input sentence as an list of words
			List<String> words = new ArrayBackedStringList(inputSentence1);
			List<String> words2 = new ArrayBackedStringList(inputSentence2);

			int n1 = words.size();
			//int n2 = words2.size();
			
			logger.info("Translating \"" + inputSentence1 + "\" with " + n1 + " words");
			logger.info(" as well as \"" + inputSentence2 + "\"");
		
			// Construct n+1 empty stacks, where n = input1.length
			List<HypothesisStack> stacks = new ArrayList<HypothesisStack>(n1 + 1);
			for (int stackNumber=0; stackNumber<=n1; stackNumber++) {
				stacks.add(new HypothesisStack());
			}
			
			// Place empty hypothesis into stack 0
			stacks.get(0).add(new Hypothesis(2));
		
			// For each stack from 0..n
			for (int stackNumber=0; stackNumber<=n1; stackNumber++) {
				
				HypothesisStack stack = stacks.get(stackNumber);
				logger.finer("Current stack is stack " + stackNumber);
				
				// For each hypothesis in this stack
				for (Hypothesis hypothesis : stack) {
				
					// for each translation option for lang1 that is applicable
					for (BilingualRuleCollection translationOptions : table1.getApplicableTranslationOptions(words, hypothesis)) {
				
						// for each translation option for lang2 that pairs with translation option from lang1
							// if translation option for lang2 is applicable
						
						// Create new hypotheses and place in the appropriate stack(s)
						// (combine hypotheses if possible)
						// (prune stacks if needed)
						addNewHypotheses(hypothesis, translationOptions, stacks, words, words2, table2);
					}
				}
			}

			// Trace best translation
			float bestScore = Float.NEGATIVE_INFINITY;
			Hypothesis bestHypothesis = null;
			int lastStackNumber = stacks.size()-1;
			
			if (logger.isLoggable(Level.FINE)) {
				for (int i=0; i<=lastStackNumber; i++) {
					logger.fine(stacks.get(i).size() + " hypotheses in stack " + i);
				}
			}
			
			//logger.info(""+lastStackNumber);
			for (Hypothesis hypothesis : stacks.get(lastStackNumber)) {
				if (logger.isLoggable(Level.FINEST)) logger.finest("Looking at final hypothesis: " + hypothesis);
				if (hypothesis.cumulativeScore > bestScore) {
					bestHypothesis = hypothesis;
					bestScore = hypothesis.cumulativeScore;
				}
			}
			
			if (bestHypothesis == null) {
				logger.warning("No best translation found");
			} else {
				for (Hypothesis current = bestHypothesis; current != null; current = current.previous) {
					logger.info(current.toString());
				}
				logger.info("Found translation: \"" + bestHypothesis.getTranslation() + "\"");
			}
		}
		
	}

	static void addNewHypotheses(Hypothesis hypothesisToExtend, 
			BilingualRuleCollection translationOptions1, List<HypothesisStack> stacks, 
			List<String> source1, List<String> source2, PhraseTable reverseTable2) {
		
		//int coverage1Arity = hypothesisToExtend.coverageCardinality(0);
		BitSet coverageVector2 = hypothesisToExtend.coverageVector.get(1);
		
		for (int i1=0; i1<translationOptions1.size(); i1++) {
			
			// Get a partial translation of a source1 phrase, and its score
			List<String> targetWords = translationOptions1.rightHandSides.get(i1);
			float score1 = translationOptions1.scores.get(i1);
			
			// Make sure the partial translation can be generated by lang2
			if (reverseTable2.containsKey(targetWords)) {
				logger.fine("Phrase table for source " + reverseTable2.sourceID + " contains " + targetWords);

				// Get the set of source2 reverse rules that match the partial translation
				BilingualRuleCollection reverseRules2 = reverseTable2.get(targetWords);
				
				List<BitSet> source1SubLists = new ArrayList<BitSet>();
				
				// Find all locations in source1 where sourcePhrase1 is applicable
				List<String> sourcePhrase1 = translationOptions1.lhs;
				List<Integer> sourcePhrase1Matches = indicesOfSubList(source1, sourcePhrase1);
				if (logger.isLoggable(Level.FINE)) logger.fine("Source 1 phrase " + sourcePhrase1 + " matches input 1 in " + sourcePhrase1Matches.size() + " places.");
				for (int j1 : sourcePhrase1Matches) {
					BitSet newCoverageVector1 = new BitSet();
					newCoverageVector1.set(j1, j1+targetWords.size());
					newCoverageVector1.or(hypothesisToExtend.coverageVector.get(0));
					
					source1SubLists.add(newCoverageVector1);
				}

				// For each source2 phrase that corresponds with the partial translation targetWords...
				for (int i2=0; i2<reverseRules2.size(); i2++) {
				
					// Get the source2 phrase that corresponds with the partial translation, and its score
					List<String> sourcePhrase2 = reverseRules2.rightHandSides.get(i2);
					float score2 = reverseRules2.scores.get(i2);
					
					logger.fine("Considering source 2 phrase " + sourcePhrase2);
					
					// Find all locations in source2 where sourcePhrase2 might be applicable
					for (int j2 : indicesOfSubList(source2, sourcePhrase2)) {
						BitSet newCoverageVector2 = new BitSet();
						newCoverageVector2.set(j2, j2+sourcePhrase2.size());
						
						if (! coverageVector2.intersects(newCoverageVector2)) {
							
							if (logger.isLoggable(Level.FINE)) logger.fine("Source 2 phrase " + sourcePhrase2 + " matches input 2 at " + newCoverageVector2.nextSetBit(0));
							newCoverageVector2.or(coverageVector2);
							
							
							for (BitSet newCoverageVector1 : source1SubLists) {
								
								List<BitSet> newCoverageVectors = new ArrayBackedList<BitSet>(newCoverageVector1, newCoverageVector2);
								List<Float> newScores = new ArrayBackedList<Float>(score1, score2);
								
								logger.finest("Extending hypothesis: " + hypothesisToExtend + "   (coverage vectors==" + newCoverageVectors + ", scores==" + newScores + ")");
								Hypothesis hypothesis = new Hypothesis(hypothesisToExtend, targetWords, newCoverageVectors, newScores);
								
								int stackNumber = hypothesis.coverageCardinality(0);
								logger.finest("Adding to stack " + stackNumber + " : " + hypothesis);
								stacks.get(stackNumber).add(hypothesis);
							}
							
						} else if (logger.isLoggable(Level.FINE)) {
							logger.fine("Source 2 phrase " + sourcePhrase2 + " does NOT match input 2.");
						}
						
					}
					
				}
				
				//for (BilingualRuleCollection reverseRules2 : reverseTable2.getApplicableTranslationOptions(source2, hypothesisToExtend, hypothesisToExtend.constraintVector2)) {

//					int newCoverageArity1 = targetWords.size() + coverage1Arity;

//					for (int j : indicesOfSubList(source1, translationOptions1.lhs)) {
//
//						BitSet newCoverageVector = new BitSet();
//						newCoverageVector.set(j, j+targetWords.size());
//						newCoverageVector.or(hypothesisToExtend.coverageVector.get(0));

//						Hypothesis hypothesis = new Hypothesis(hypothesisToExtend, targetWords, newCoverageVector, newCoverage, score1);
//						logger.fine("Extending hypothesis: " + hypothesisToExtend + "   (coverage==" + coverage1Arity + ")");
//						logger.fine("Adding to stack " + newCoverage + " : " + hypothesis);
//						stacks.get(newCoverage).add(hypothesis);
//					}
				//}

			} else {
				logger.fine("Phrase table for source " + reverseTable2.sourceID + " does NOT contain " + targetWords);
			}
		}
		
	}
	
	// Based on algorithm from Collections.indexOfSubList
	static List<Integer> indicesOfSubList(List<?> source, List<?> target) {
		
		ArrayList<Integer> result = new ArrayList<Integer>();
		
		int targetSize = target.size();
		int lastValidIndex = source.size() - targetSize;
		
		loop:
			for (int index=0; index<=lastValidIndex; index++) {
				for (int i=index, j=0; j<targetSize; i++, j++) {
					if (!source.get(i).equals(target.get(j))) 
						continue loop;
				}
				logger.finer("Adding index " + index);
				result.add(index);
			}
		
		return result;
	}
	
	
}
