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
package joshua.sarray;

import joshua.util.sentence.Span;
import joshua.util.sentence.alignment.AbstractAlignments;

import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * AlignmentArray is an auxiliary class which stores alignment
 * information for a parallel corpus.  For each source word it
 * stores the minimum and maximum index of aligned words in the
 * target corpus, and for each target word it stores the min and
 * max indexed of aligned words in the source corpus.  The intent
 * is to increase the speed of the phrase extraction.
 *
 * This class was inspired by a conversation with Adam Lopez. 
 *
 * @author Chris Callison-Burch
 * @since  13 May 2008
 * @author Lane Schwartz
 * @version $LastChangedDate:2008-07-30 17:15:52 -0400 (Wed, 30 Jul 2008) $
 */

public class AlignmentArray extends AbstractAlignments {
	
//===============================================================
// Member variables
//===============================================================

	//TODO Probably can drop lowestAlignedTargetIndex and related variables...
	
	/**
	 * Stores the lowest index of aligned target words for each
	 * word in the source corpus.
	 */
	//protected final int[] lowestAlignedTargetIndex;
	
	
	/**
	 * Stores the highest index of aligned target words for
	 * each word in the source corpus.
	 */
	//protected final int[] highestAlignedTargetIndex;
	
	/**
	 * Stores the indices of all aligned target words for
	 * each word in the source corpus.
	 */
	protected final int[][] alignedTargetIndices;
	
	
	/**
	 * Stores the lowest index of aligned source words for each
	 * word in the target corpus.
	 */
	//protected final int[] lowestAlignedSourceIndex;
	
	
	/**
	 * Stores the highest index of aligned source words for
	 * each word in the target corpus.
	 */
	//protected final int[] highestAlignedSourceIndex;
	
	/**
	 * Stores the indices of all aligned source words for
	 * each word in the target corpus.
	 */
	protected final int[][] alignedSourceIndices;
	
	/** Logger for this class. */
	private static final Logger logger = Logger.getLogger(AlignmentArray.class.getName());
	

//===============================================================
// Constructor(s)
//===============================================================

	/**
	 * This protected constructor is used by the
	 * SuffixArrayFactory.loadAlignmentArray and
	 * SuffixArrayFactory.createAlignmentArray methods.
	 */
	protected AlignmentArray(int[][] alignedTargetIndices, int[][] alignedSourceIndices) {
		this.alignedTargetIndices = alignedTargetIndices;
		this.alignedSourceIndices = alignedSourceIndices;
	}
	
	/**
	 * This protected constructor is used by the
	 * SuffixArrayFactory.loadAlignmentArray and
	 * SuffixArrayFactory.createAlignmentArray methods.
	 */
//	protected AlignmentArray(int[] lowestAlignedTargetIndex, int[] highestAlignedTargetIndex,
//							 int[] lowestAlignedSourceIndex, int[] highestAlignedSourceIndex,
//							 int[][] alignedTargetIndices,   int[][] alignedSourceIndices) {
//		this.lowestAlignedTargetIndex = lowestAlignedTargetIndex;
//		this.highestAlignedTargetIndex = highestAlignedTargetIndex;
//		this.lowestAlignedSourceIndex = lowestAlignedSourceIndex;
//		this.highestAlignedSourceIndex = highestAlignedSourceIndex;
//		this.alignedTargetIndices = alignedTargetIndices;
//		this.alignedSourceIndices = alignedSourceIndices;
//	}
	

//===============================================================
// Public
//===============================================================
	
	//===========================================================
	// Accessor methods (set/get)
	//===========================================================
	
	/**
	 * This method looks up target span for the given source
	 * span
	 * 
	 * @param startSourceIndex the staring position in the
	 *                         source corpus (inclusive)
	 * @param endSourceIndex   the end position in the source
	 *                         corpus (exclusive)
	 * @return a tuple containing the min and max indices in
	 *         the target corpus, if the span is unaligned the
	 *         value will be <UNALIGNED, undefined>
	 */
	public Span getAlignedTargetSpan(int startSourceIndex, int endSourceIndex) {
		//return getAlignedSpan(startSourceIndex, endSourceIndex, lowestAlignedTargetIndex, highestAlignedTargetIndex);
		return getAlignedSpan(startSourceIndex, endSourceIndex, alignedTargetIndices);
	}
	

	public Span getAlignedTargetSpan(Span sourceSpan) {
		//return getAlignedSpan(sourceSpan.start, sourceSpan.end, lowestAlignedTargetIndex, highestAlignedTargetIndex);
		return getAlignedSpan(sourceSpan.start, sourceSpan.end, alignedTargetIndices);
	}
	
	/**
	 * Gets the indices of all source words aligned with 
	 * a particular location in the target corpus.
	 * 
	 * @param index Index into the target corpus
	 * @return The indices of all source words aligned with 
	 *         the given location in the target corpus.
	 */
	public int[] getAlignedSourceIndices(int targetIndex) {
		return alignedSourceIndices[targetIndex];
	}

	/**
	 * Gets the indices of all target words aligned with 
	 * a particular location in the source corpus.
	 * 
	 * @param index Index into the source corpus
	 * @return The indices of all target words aligned with 
	 *         the given location in the source corpus.
	 */
	public int[] getAlignedTargetIndices(int sourceIndex) {
		return alignedTargetIndices[sourceIndex];
	}
	
	/**
	 * This method looks up source span for the given target span
	 * 
	 * @param startTargetIndex the staring position in the
	 *                         target corpus (inclusive)
	 * @param endTargetIndex   the end position in the target
	 *                         corpus (exclusive)
	 * @return a tuple containing the min and max indices in
	 *         the source corpus, if the span is unaligned the
	 *         value will
	 be <UNALIGNED, undefined>
	 */
	public Span getAlignedSourceSpan(int startTargetIndex, int endTargetIndex) {
		//return getAlignedSpan(startTargetIndex, endTargetIndex, lowestAlignedSourceIndex, highestAlignedSourceIndex);
		return getAlignedSpan(startTargetIndex, endTargetIndex, alignedSourceIndices);
	}
	
	
	/**
	 * This method determines whether there is a consistent
	 * word alignment for the specified source phrase.
	 * ccb - debugging
	 */
	public boolean hasConsistentAlignment(int startSourceIndex, int endSourceIndex) {
		Span targetSpan = getAlignedTargetSpan(startSourceIndex, endSourceIndex);
		//if(targetSpan[0] == UNALIGNED) return false;
		if (targetSpan.start == UNALIGNED) return false;
		// check back to see what sourceSpan the targetSpan
		// aligns back to, so that we can check that it's
		// within bounds
		Span sourceSpan = getAlignedSourceSpan(targetSpan.start, targetSpan.end);
		//int[] sourceSpan = getAlignedSourceSpan(targetSpan[0], targetSpan[1]);
		
		//if(sourceSpan[0] < startSourceIndex || sourceSpan[1] > endSourceIndex) {
		if (sourceSpan.start < startSourceIndex
		|| sourceSpan.end > endSourceIndex) {
			return false;
		} else {
			return true;
		}
	}
	
	/**
	 * Determines if any terminal in the source phrase aligns with the provided index into the target corpus.
	 * 
	 * @param targetIndex
	 * @param sourcePhrase
	 * @return
	 */
	public boolean hasAlignedTerminal(int targetIndex, HierarchicalPhrase sourcePhrase) {
		
		if (alignedSourceIndices[targetIndex]!=null) {
			for (int alignedSourceIndex : alignedSourceIndices[targetIndex]) {
				for (int i=0; i<sourcePhrase.terminalSequenceStartIndices.length; i++) {
					if (alignedSourceIndex >= sourcePhrase.terminalSequenceStartIndices[i] &&
							alignedSourceIndex < sourcePhrase.terminalSequenceEndIndices[i]) {
						if (logger.isLoggable(Level.FINEST)) logger.finest("Target index " + targetIndex + ", source index " + alignedSourceIndex + " is in source phrase at range ["+sourcePhrase.terminalSequenceStartIndices[i] + "-" + sourcePhrase.terminalSequenceEndIndices[i] + ")");
						return true;
					}
				}
			}
		}
		
		if (logger.isLoggable(Level.FINEST)) logger.warning("No aligned point");
		return false;
	}
	
	/**
	 * Determines if any terminal in the source phrase aligns with the provided index into the target corpus.
	 * 
	 * @param targetIndex
	 * @param sourcePhrase
	 * @return
	 */
	public boolean hasAlignedTerminal(int targetIndex, HierarchicalPhrases sourcePhrases, int sourcePhraseIndex) {
		
		int phraseLength = sourcePhrases.getNumberOfTerminalSequences();
		
		if (alignedSourceIndices[targetIndex]!=null) {
			for (int alignedSourceIndex : alignedSourceIndices[targetIndex]) {
//				for (int i=0; i<sourcePhrase.terminalSequenceStartIndices.length; i++) {
				for (int i=0; i<phraseLength; i++) {
					int sourceStart = sourcePhrases.getStartPosition(sourcePhraseIndex, i);
					int sourceEnd = sourcePhrases.getEndPosition(sourcePhraseIndex, i);
//					if (alignedSourceIndex >= sourcePhrase.terminalSequenceStartIndices[i] &&
					if (alignedSourceIndex >= sourceStart &&
//							alignedSourceIndex < sourcePhrase.terminalSequenceEndIndices[i]) {
							alignedSourceIndex < sourceEnd) {
//						if (logger.isLoggable(Level.FINEST)) logger.finest("Target index " + targetIndex + ", source index " + alignedSourceIndex + " is in source phrase at range ["+sourcePhrase.terminalSequenceStartIndices[i] + "-" + sourcePhrase.terminalSequenceEndIndices[i] + ")");
						if (logger.isLoggable(Level.FINEST)) logger.finest("Target index " + targetIndex + ", source index " + alignedSourceIndex + " is in source phrase at range ["+sourceStart + "-" + sourceEnd + ")");
						return true;
					}
				}
			}
		}
		
		if (logger.isLoggable(Level.FINEST)) logger.warning("No aligned point");
		return false;
	}
	
	
	
	//===========================================================
	// Methods
	//===========================================================


//===============================================================
// Protected 
//===============================================================
	
	//===============================================================
	// Methods
	//===============================================================



//===============================================================
// Private 
//===============================================================
	
	//===============================================================
	// Methods
	//===============================================================

	
	

	/*
	private Span getAlignedSpan(int startIndex, int endIndex, int[] lowestAlignedIndex, int[] highestAlignedIndex) {
		int lowestHighestMin = UNALIGNED;
		int lowestHighestMax = -1;
		
		//int[] lowestHighest = new int[2];
		//lowestHighest[0] = UNALIGNED;
		//lowestHighest[1] = -1;
		
		for(int i = startIndex; i < endIndex; i++) {
			if(lowestAlignedIndex[i] != UNALIGNED) {
				lowestHighestMin = ( lowestAlignedIndex[i] < lowestHighestMin) ?  lowestAlignedIndex[i] : lowestHighestMin; //Math.min(lowestAlignedIndex[i], lowestHighestMin);
				lowestHighestMax = (highestAlignedIndex[i] > lowestHighestMax) ? highestAlignedIndex[i] : lowestHighestMax; //Math.max(highestAlignedIndex[i], lowestHighestMax);
				//lowestHighest[0] = Math.min(lowestAlignedIndex[i], lowestHighest[0]);
				//lowestHighest[1] = Math.max(highestAlignedIndex[i], lowestHighest[1]);
			} else if (requireTightSpans && (i==startIndex || i==endIndex-1)) { //XXX Is this the correct way to ensure tight spans?
				// If requiring tight spans
				return new Span(UNALIGNED, UNALIGNED);
			}
		}
		
		//lowestHighest[1]++;
		lowestHighestMax++;
		
		//return lowestHighest;
		return new Span(lowestHighestMin,lowestHighestMax);
	}
	*/
	
	/**
	 * This method looks up the minimum and maximum aligned
	 * indices for the span.
	 * 
	 * @param startIndex the staring word (inclusive)
	 * @param endIndex the end word (exclusive) 
	 * @return a tuple containing the min (inclusive) and max
	 *         (exclusive) aligned indices, if the span is
	 *         unaligned the value will be <UNALIGNED, ?>
	 */
	private Span getAlignedSpan(int startIndex, int endIndex, int[][] alignedIndices) {
		int lowestHighestMin = UNALIGNED;
		int lowestHighestMax = -1;
		
		for(int i = startIndex; i < endIndex; i++) {
			if (alignedIndices[i] != null) {
				lowestHighestMin = ( alignedIndices[i][0] < lowestHighestMin) ?  alignedIndices[i][0] : lowestHighestMin; //Math.min(lowestAlignedIndex[i], lowestHighestMin);
				lowestHighestMax = (alignedIndices[i][alignedIndices[i].length-1] > lowestHighestMax) ? alignedIndices[i][alignedIndices[i].length-1] : lowestHighestMax; //Math.max(highestAlignedIndex[i], lowestHighestMax);
			} else if (requireTightSpans && (i==startIndex || i==endIndex-1)) { //XXX Is this the correct way to ensure tight spans?
				// If requiring tight spans
				return new Span(UNALIGNED, UNALIGNED);
			}
		}
		
		lowestHighestMax++;
		return new Span(lowestHighestMin,lowestHighestMax);	
	}
	
/*
	private Span getAlignedSpan(int startIndex, int endIndex, int[][] alignedIndices) {
		int lowestHighestMin = UNALIGNED;
		int lowestHighestMax = -1;

		for(int i = startIndex; i < endIndex; i++) {
			if (alignedIndices[i].length > 0) {
				lowestHighestMin = (alignedIndices[i][0] < lowestHighestMin) ?  alignedIndices[i][0] : lowestHighestMin;
				lowestHighestMax = (alignedIndices[i][alignedIndices[i].length-1] > lowestHighestMax) ? alignedIndices[i][alignedIndices[i].length-1] : lowestHighestMax;
			} else if (requireTightSpans) {
				return new Span(UNALIGNED, UNALIGNED);
			}
		}
		
		lowestHighestMax++;
		
		return new Span(lowestHighestMin,lowestHighestMax);
	}
*/	
	
//===============================================================
// Static
//===============================================================


//===============================================================
// Main 
//===============================================================
/*
	public static void main(String[] args) throws IOException {
		
		
		if (args.length != 5) {
			System.out.println("Usage: java AlignmentArray sourceLang targetLang corpusName dir alignmentsFilename");
			System.exit(0);
		}
		
		String sourceLang = args[0];
		String targetLang = args[1];
		String corpusName = args[2];
		String directory = args[3];
		//String alignmentsFilename = args[4];
		
		SuffixArray sourceCorpus = SuffixArrayFactory.loadSuffixArray(sourceLang, corpusName, directory);
		SuffixArray targetCorpus = SuffixArrayFactory.loadSuffixArray(targetLang, corpusName, directory);
		AlignmentArray alignmentArray = SuffixArrayFactory.loadAlignmentArray(sourceLang, targetLang, corpusName, directory);
		
		// ccb - debugging
		for (int i = 0; i < sourceCorpus.getNumWords(); i++) {
			for (int j = i+1; j <= sourceCorpus.getNumWords(); j++) {
				//int[] alignedTargetWords = alignmentArray.getAlignedTargetSpan(i, j);
				Span alignedTargetWords = alignmentArray.getAlignedTargetSpan(i, j);
				if (alignmentArray.hasConsistentAlignment(i, j)) {
					//System.out.println(sourceCorpus.getPhrase(i, j) + " [" + i + "," + j + "]\t" + targetCorpus.getPhrase(alignedTargetWords[0], alignedTargetWords[1])  + " [" + alignedTargetWords[0] + "," + (alignedTargetWords[1]) + "]");
					System.out.println(sourceCorpus.getPhrase(i, j) + " [" + i + "," + j + "]\t" + targetCorpus.getPhrase(alignedTargetWords.start, alignedTargetWords.end)  + " [" + alignedTargetWords.start + "," + (alignedTargetWords.end) + "]");
				} else {
//					System.out.println(sourceCorpus.getPhrase(i, j) + ": NO CONSISTENT ALIGNMENT");
				}
			}
		}
	}
	*/
}

