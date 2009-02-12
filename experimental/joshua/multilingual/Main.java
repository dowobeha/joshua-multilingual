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
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Logger;

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
		
//		Pattern p = Pattern.compile("^(.*?) \\|\\|\\| (.*?) \\|\\|\\| .* \\|\\|\\| .* \\|\\|\\| (.*)$");
//		Matcher m = p.matcher("ahora ||| now have ||| (0) ||| (0) () ||| 0.10396 0.468315 0.0020142 0.0085109 2.718");
//		//boolean b = m.matches();
//		if (m.matches()) {
//			System.out.println(m.group(1));
//			System.out.println(m.group(2));
//			System.out.println(m.group(3));
//		}
//		else
//			System.out.println("No match");
				
		// Read phrase tables
		PhraseTable table1 = new PhraseTable(args[1]);
		PhraseTable table2 = new PhraseTable(args[3], Direction.Reverse);
		
		logger.fine("Phrase table has " + table1.size() + " entries.");
		// Read input sentences
		Scanner sentences1 = new Scanner(new File(args[0]));
		Scanner sentences2 = new Scanner(new File(args[2]));
		
		// For each input language
		while (sentences1.hasNextLine() && sentences2.hasNextLine()) {
			
			// Construct translation options for this input->output
			final String inputSentence1 = sentences1.nextLine();
			final String inputSentence2 = sentences2.nextLine();
			
			List<String> words = new AbstractList<String>() {

				final String[] array = inputSentence1.split("\\s+");
				
				@Override
				public String get(int index) {
					return array[index];
				}

				@Override
				public int size() {
					return array.length;
				}
				
			};
			//String[] words = inputSentence.split("\\W+");
			int n = words.size();
			logger.info("Translating \"" + inputSentence1 + "\" with " + n + " words");
			logger.info(" as well as \"" + inputSentence2 + "\"");
		
			// Construct n+1 empty stacks, where n = input1.length
			List<HypothesisStack> stacks = new ArrayList<HypothesisStack>(n + 1);
			for (int stackNumber=0; stackNumber<=n; stackNumber++) {
				stacks.add(new HypothesisStack());
			}
			
			// Place empty hypothesis into stack 0
			stacks.get(0).add(new Hypothesis(n));
		
			// For each stack from 0..n
			for (int stackNumber=0; stackNumber<=n; stackNumber++) {
				
				// for each hypothesis in this stack
				HypothesisStack stack = stacks.get(stackNumber);
				logger.fine("Current stack is stack " + stackNumber);
				
				for (Hypothesis hypothesis : stack) {
				
					// for each translation option for lang1 that is applicable
					for (BilingualRuleCollection optionCollection : table1.getApplicableTranslationOptions(words, hypothesis)) {
				
						// for each translation option for lang2 that pairs with translation option from lang1
							// if translation option for lang2 is applicable
						
								// create new hypothesis
								// place hypothesis in the next stack
								// (combine hypothesis if possible)
								// (prune stack if needed)
						addNewHypotheses(hypothesis, optionCollection, stacks, words, table2);
					}
				}
			}

			// Trace best translation
			float bestScore = Float.NEGATIVE_INFINITY;
			Hypothesis bestHypothesis = null;
			int lastStackNumber = stacks.size()-1;
			logger.info(""+lastStackNumber);
			for (Hypothesis hypothesis : stacks.get(lastStackNumber)) {
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

	static void addNewHypotheses(Hypothesis hypothesisToExtend, BilingualRuleCollection rules, List<HypothesisStack> stacks, List<String> source, PhraseTable constraintTable) {
		
		int previousCoverage = hypothesisToExtend.coverage();
		
		for (int i=0; i<rules.size(); i++) {
			List<String> rhs = rules.rightHandSides.get(i);
			float score = rules.scores.get(i);
			
			if (constraintTable.containsKey(rhs)) {
				logger.info("Constraint can translate " + rhs);
			} else {
				logger.info("Constraint can NOT translate " + rhs);
			}
			
			int newCoverage = rhs.size() + previousCoverage;
			
			for (int j : indicesOfSubList(source, rules.lhs)) {

				BitSet newCoverageVector = new BitSet();
				newCoverageVector.set(j, j+rhs.size());
				newCoverageVector.or(hypothesisToExtend.coverageVector);
				
				Hypothesis hypothesis = new Hypothesis(hypothesisToExtend, rhs, newCoverageVector, newCoverage, score);
				logger.fine("Extending hypothesis: " + hypothesisToExtend + "   (coverage==" + previousCoverage + ")");
				logger.fine("Adding to stack " + newCoverage + " : " + hypothesis);
				stacks.get(newCoverage).add(hypothesis);
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
