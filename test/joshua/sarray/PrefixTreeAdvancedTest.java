package joshua.sarray;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import joshua.sarray.AlignmentArray;
import joshua.sarray.BasicPhrase;
import joshua.sarray.CorpusArray;
import joshua.sarray.PrefixTree;
import joshua.sarray.SampledLexProbs;
import joshua.sarray.SuffixArray;
import joshua.sarray.PrefixTree.Node;
import joshua.util.lexprob.LexicalProbabilities;
import joshua.util.sentence.Vocabulary;


import org.testng.Assert;
import org.testng.annotations.Test;



public class PrefixTreeAdvancedTest {

	String corpusString, targetCorpusString;
	Vocabulary sourceVocab, targetVocab;
	Map<Integer,String> ntVocab;
	BasicPhrase corpusSentence;
	SuffixArray suffixArray;
	AlignmentArray alignments;
	CorpusArray targetCorpusArray;
	LexicalProbabilities lexProbs;

	@Test
	public void setup() {
		
		// Adam Lopez's example...
		corpusString = "it makes him and it mars him , it sets him on and it takes him off .";
		
		Set<String> sourceWords = new HashSet<String>();
		for (String word : corpusString.split("\\s+")) {
			sourceWords.add(word);
		}

		sourceVocab = new Vocabulary(sourceWords);
		

		corpusSentence = new BasicPhrase(corpusString, sourceVocab);
		
		targetCorpusString = "das macht ihn und es beschädigt ihn , es setzt ihn auf und es führt ihn aus .";
		Set<String> targetWords = new HashSet<String>();
		for (String targetWord : targetCorpusString.split("\\s+")) {
			targetWords.add(targetWord);
		}
		
		targetVocab = new Vocabulary(targetWords);
		
		ntVocab = new HashMap<Integer,String>();
		ntVocab.put(-1, "X");
		
		{
			// create the suffix array...
			int[] sentenceStartPositions = {0};
			
			Assert.assertEquals(corpusSentence.size(), 18);
			
			int[] corpus = new int[corpusSentence.size()];
			for(int i = 0; i < corpusSentence.size(); i++) {
				corpus[i] = corpusSentence.getWordID(i);
			}
			
			CorpusArray corpusArray = new CorpusArray(corpus, sentenceStartPositions, sourceVocab);
			suffixArray = new SuffixArray(corpusArray);
			


			int[] targetSentenceStartPositions = {0};
			
			BasicPhrase targetCorpusSentence = new BasicPhrase(targetCorpusString, targetVocab);
			Assert.assertEquals(targetCorpusSentence.size(), 18);
			
			int[] targetCorpus = new int[targetCorpusSentence.size()];
			for(int i = 0; i < targetCorpusSentence.size(); i++) {
				targetCorpus[i] = targetCorpusSentence.getWordID(i);
			}
			

			

			
			targetCorpusArray = new CorpusArray(targetCorpus, targetSentenceStartPositions, targetVocab);
			
			int[] lowestAlignedTargetIndex = new int[corpusSentence.size()];
			int[] highestAlignedTargetIndex = new int[corpusSentence.size()];
			int[] lowestAlignedSourceIndex = new int[targetCorpusSentence.size()];
			int[] highestAlignedSourceIndex = new int[targetCorpusSentence.size()];
			
			int[][] alignedTargetIndices = new int[corpusSentence.size()][];
			int[][] alignedSourceIndices = new int[targetCorpusSentence.size()][];
			
			
			
			{
				for (int i=0; i<18; i++) {
					lowestAlignedTargetIndex[i] = i;
					highestAlignedTargetIndex[i] = i;
					lowestAlignedSourceIndex[i] = i;
					highestAlignedSourceIndex[i] = i;
					
					alignedTargetIndices[i] = new int[1];
					alignedTargetIndices[i][0] = i;
					
					alignedSourceIndices[i] = new int[1];
					alignedSourceIndices[i][0] = i;
				}
			}
			
			
			//alignments = new AlignmentArray(lowestAlignedTargetIndex, highestAlignedTargetIndex, lowestAlignedSourceIndex, highestAlignedSourceIndex, alignedTargetIndices, alignedSourceIndices);
			alignments = new AlignmentArray(alignedTargetIndices, alignedSourceIndices);
			
			
		}
		
		{
			/*
			String targetGivenSourceCounts =
				"   1 , ," + "\n" +
				"   1 . ." + "\n" +
				"   2 and und" + "\n" +
				"   4 him ihn" + "\n" +
				"   1 it das" + "\n" +
				"   3 it es" + "\n" +
				"   1 makes macht" + "\n" +
				"   1 mars beschädigt" + "\n" +
				"   1 off aus" + "\n" +
				"   1 on auf" + "\n" +
				"   1 sets setzt" + "\n" +
				"   1 takes führt" + "\n";
			
			String sourceGivenTargetCounts =
				"   1 , ," + "\n" +
				"   1 . ." + "\n" +
				"   1 auf on" + "\n" +
				"   1 aus off" + "\n" +
				"   1 beschädigt mars" + "\n" +
				"   1 das it" + "\n" +
				"   3 es it" + "\n" +
				"   1 führt takes" + "\n" +
				"   4 ihn him" + "\n" +
				"   1 macht makes" + "\n" +
				"   1 setzt sets" + "\n" +
				"   2 und and" + "\n";
			
			Scanner sourceGivenTarget = new Scanner(sourceGivenTargetCounts);
			Scanner targetGivenSource = new Scanner(targetGivenSourceCounts);
			*/
			String alignmentString = "0-0 1-1 2-2 3-3 4-4 5-5 6-6 7-7 8-8 9-9 10-10 11-11 12-12 13-13 14-14 15-15 16-16 17-17";
		
			try {
				lexProbs = SampledLexProbs.getSampledLexProbs(corpusString, targetCorpusString, alignmentString);
			} catch (IOException e) {
				Assert.fail("Unable to initialize lexprobs");
			}
		//	lexProbs = new LexProbs(sourceGivenTarget, targetGivenSource, sourceVocab, targetVocab);
		}
	}
	
	PrefixTree simplePrefixTree;
	
	@Test(dependsOnMethods={"setup"})
	public void verifyNodesShort() {
		int maxPhraseSpan = 10;
		int maxPhraseLength = 10;
		int maxNonterminals = 2;
		int sampleSize = 300;
		int minNonterminalSpan = 2;
		RuleExtractor ruleExtractor = new HierarchicalRuleExtractor(suffixArray, targetCorpusArray, alignments, lexProbs, sampleSize, maxPhraseSpan, maxPhraseLength, maxNonterminals, minNonterminalSpan);
		
		
		BasicPhrase query = new BasicPhrase("it makes him", sourceVocab);
		//BasicPhrase query = new BasicPhrase("it makes him and it mars him", sourceVocab);
		//BasicPhrase query = new BasicPhrase("it makes him and it mars him , it sets him on and it takes him off .", sourceVocab);
		simplePrefixTree = new PrefixTree(suffixArray, targetCorpusArray, alignments, lexProbs, ruleExtractor, query.getWordIDs(), maxPhraseSpan, maxPhraseLength, maxNonterminals, minNonterminalSpan);

		//System.out.println(simplePrefixTree.toString());
	
		//int[] sentence = {sourceVocab.getID("it"), sourceVocab.getID("makes"), sourceVocab.getID("him"), sourceVocab.getID("and"), sourceVocab.getID("it"), sourceVocab.getID("mars"), sourceVocab.getID("him"), sourceVocab.getID(","), sourceVocab.getID("it"), sourceVocab.getID("sets"), sourceVocab.getID("him"), sourceVocab.getID("on"), sourceVocab.getID("and"), sourceVocab.getID("it"), sourceVocab.getID("takes"), sourceVocab.getID("him"), sourceVocab.getID("off"), sourceVocab.getID(".")};
		//PrefixTree dumbTree = new PrefixTree(sentence, maxPhraseSpan, maxPhraseLength, maxNonterminals, spanLimit);
		//System.err.println(dumbTree.toString(sourceVocab));
		
		Assert.assertNotNull(simplePrefixTree.root);
		Assert.assertNotNull(simplePrefixTree.root.children);
		
		/////////////////////////////
		
		Assert.assertEquals(simplePrefixTree.root.children.size(), 4);
		
		Assert.assertTrue(simplePrefixTree.root.children.containsKey(sourceVocab.getID("it")));
		Assert.assertTrue(simplePrefixTree.root.children.containsKey(sourceVocab.getID("makes")));
		Assert.assertTrue(simplePrefixTree.root.children.containsKey(sourceVocab.getID("him")));
		Assert.assertTrue(simplePrefixTree.root.children.containsKey(PrefixTree.X));
		
		Node root_it = simplePrefixTree.root.children.get(sourceVocab.getID("it"));
		Node root_makes = simplePrefixTree.root.children.get(sourceVocab.getID("makes"));
		Node root_him = simplePrefixTree.root.children.get(sourceVocab.getID("him"));
		Node root_X = simplePrefixTree.root.children.get(PrefixTree.X);
		
		Assert.assertNotNull(root_it);
		Assert.assertNotNull(root_makes);
		Assert.assertNotNull(root_him);
		Assert.assertNotNull(root_X);
		
		/////////////////////////////
		
		Assert.assertEquals(root_it.children.size(), 2);
		
		Assert.assertTrue(root_it.children.containsKey(sourceVocab.getID("makes")));
		Assert.assertTrue(root_it.children.containsKey(PrefixTree.X));
		
		Node root_it_makes = root_it.children.get(sourceVocab.getID("makes"));
		Node root_it_X = root_it.children.get(PrefixTree.X);
				
		Assert.assertNotNull(root_it_makes);
		Assert.assertNotNull(root_it_X);

		/////////////////////////////
		
		Assert.assertEquals(root_makes.children.size(), 2);
		
		Assert.assertTrue(root_makes.children.containsKey(sourceVocab.getID("him")));
		Assert.assertTrue(root_makes.children.containsKey(PrefixTree.X));
		
		Node root_makes_him = root_makes.children.get(sourceVocab.getID("him"));
		Node root_makes_X = root_makes.children.get(PrefixTree.X);
				
		Assert.assertNotNull(root_makes_him);
		Assert.assertNotNull(root_makes_X);
		
		/////////////////////////////
		
		Assert.assertEquals(root_X.children.size(), 2);

		Assert.assertTrue(root_X.children.containsKey(sourceVocab.getID("makes")));
		Assert.assertTrue(root_X.children.containsKey(sourceVocab.getID("him")));
		
		Node root_X_makes = root_X.children.get(sourceVocab.getID("makes"));
		Node root_X_him = root_X.children.get(sourceVocab.getID("him"));
				
		Assert.assertNotNull(root_X_makes);
		Assert.assertNotNull(root_X_him);
		
		/////////////////////////////
		
		Assert.assertEquals(root_him.children.size(), 0);
				
		/////////////////////////////
		// Level 3 in the tree
		/////////////////////////////		
		
		Assert.assertEquals(root_it_makes.children.size(), 2);
		
		Assert.assertTrue(root_it_makes.children.containsKey(sourceVocab.getID("him")));
		Assert.assertTrue(root_it_makes.children.containsKey(PrefixTree.X));
		
		Node root_it_makes_him = root_it_makes.children.get(sourceVocab.getID("him"));
		Node root_it_makes_X = root_it_makes.children.get(PrefixTree.X);
				
		Assert.assertNotNull(root_it_makes_him);
		Assert.assertNotNull(root_it_makes_X);

		/////////////////////////////
		
		Assert.assertEquals(root_it_X.children.size(), 1);
		
		Assert.assertTrue(root_it_X.children.containsKey(sourceVocab.getID("him")));
		
		Node root_it_X_him = root_it_X.children.get(sourceVocab.getID("him"));
				
		Assert.assertNotNull(root_it_X_him);

		/////////////////////////////
		
		Assert.assertEquals(root_makes_him.children.size(), 0);
		
		/////////////////////////////
		
		Assert.assertEquals(root_makes_X.children.size(), 0);
		
		/////////////////////////////
		
		Assert.assertEquals(root_X_him.children.size(), 0);
		
		/////////////////////////////
		
		Assert.assertEquals(root_X_makes.children.size(), 2);
		
		Assert.assertTrue(root_X_makes.children.containsKey(sourceVocab.getID("him")));
		Assert.assertTrue(root_X_makes.children.containsKey(PrefixTree.X));
		
		Node root_X_makes_him = root_makes.children.get(sourceVocab.getID("him"));
		Node root_X_makes_X = root_makes.children.get(PrefixTree.X);
				
		Assert.assertNotNull(root_X_makes_him);
		Assert.assertNotNull(root_X_makes_X);
		
		/////////////////////////////
		
		Assert.assertEquals(root_X_makes_him.children.size(), 0);
		
		/////////////////////////////

		Assert.assertEquals(root_X_makes_X.children.size(), 0);
		
		/////////////////////////////

	}
	
	
	@Test(dependsOnMethods={"setup"})
	public void verifyNodes() {
		int maxPhraseSpan = 10;
		int maxPhraseLength = 10;
		int maxNonterminals = 2;
		int sampleSize = 300;
		int minNonterminalSpan = 2;
		RuleExtractor ruleExtractor = new HierarchicalRuleExtractor(suffixArray, targetCorpusArray, alignments, lexProbs, sampleSize, maxPhraseSpan, maxPhraseLength, maxNonterminals, minNonterminalSpan);
		
		
		//BasicPhrase query = new BasicPhrase("it makes him", sourceVocab);
		//BasicPhrase query = new BasicPhrase("it makes him and it mars him", sourceVocab);
		BasicPhrase query = new BasicPhrase("it makes him and it mars him , it sets him on and it takes him off .", sourceVocab);
		PrefixTree prefixTree = new PrefixTree(suffixArray, targetCorpusArray, alignments, lexProbs, ruleExtractor, query.getWordIDs(), maxPhraseSpan, maxPhraseLength, maxNonterminals, minNonterminalSpan);

		//System.out.println(prefixTree.toString());
	
		//int[] sentence = {sourceVocab.getID("it"), sourceVocab.getID("makes"), sourceVocab.getID("him"), sourceVocab.getID("and"), sourceVocab.getID("it"), sourceVocab.getID("mars"), sourceVocab.getID("him"), sourceVocab.getID(","), sourceVocab.getID("it"), sourceVocab.getID("sets"), sourceVocab.getID("him"), sourceVocab.getID("on"), sourceVocab.getID("and"), sourceVocab.getID("it"), sourceVocab.getID("takes"), sourceVocab.getID("him"), sourceVocab.getID("off"), sourceVocab.getID(".")};
		//PrefixTree dumbTree = new PrefixTree(sentence, maxPhraseSpan, maxPhraseLength, maxNonterminals, spanLimit);
		//System.err.println(dumbTree.toString(sourceVocab));
		
		Assert.assertNotNull(prefixTree.root);
		Assert.assertNotNull(prefixTree.root.children);
		
		/////////////////////////////
		
		Assert.assertEquals(prefixTree.root.children.size(), 12);
		
		Assert.assertTrue(prefixTree.root.children.containsKey(sourceVocab.getID("it")));
		Assert.assertTrue(prefixTree.root.children.containsKey(sourceVocab.getID("makes")));
		Assert.assertTrue(prefixTree.root.children.containsKey(sourceVocab.getID("him")));
		Assert.assertTrue(prefixTree.root.children.containsKey(sourceVocab.getID("and")));
		Assert.assertTrue(prefixTree.root.children.containsKey(sourceVocab.getID("mars")));
		Assert.assertTrue(prefixTree.root.children.containsKey(sourceVocab.getID(",")));
		Assert.assertTrue(prefixTree.root.children.containsKey(sourceVocab.getID("sets")));
		Assert.assertTrue(prefixTree.root.children.containsKey(sourceVocab.getID("on")));
		Assert.assertTrue(prefixTree.root.children.containsKey(sourceVocab.getID("takes")));
		Assert.assertTrue(prefixTree.root.children.containsKey(sourceVocab.getID("off")));
		Assert.assertTrue(prefixTree.root.children.containsKey(sourceVocab.getID(".")));
		Assert.assertTrue(prefixTree.root.children.containsKey(PrefixTree.X));
		
		Node root_it = prefixTree.root.children.get(sourceVocab.getID("it"));
		Node root_makes = prefixTree.root.children.get(sourceVocab.getID("makes"));
		Node root_him = prefixTree.root.children.get(sourceVocab.getID("him"));
		Node root_and = prefixTree.root.children.get(sourceVocab.getID("and"));
		Node root_mars = prefixTree.root.children.get(sourceVocab.getID("mars"));
		Node root_comma = prefixTree.root.children.get(sourceVocab.getID(","));
		Node root_sets = prefixTree.root.children.get(sourceVocab.getID("sets"));
		Node root_on = prefixTree.root.children.get(sourceVocab.getID("on"));
		Node root_takes = prefixTree.root.children.get(sourceVocab.getID("takes"));
		Node root_off = prefixTree.root.children.get(sourceVocab.getID("off"));
		Node root_period = prefixTree.root.children.get(sourceVocab.getID("."));
		Node root_X = prefixTree.root.children.get(PrefixTree.X);
		
		Assert.assertNotNull(root_it);
		Assert.assertNotNull(root_makes);
		Assert.assertNotNull(root_him);
		Assert.assertNotNull(root_and);
		Assert.assertNotNull(root_mars);
		Assert.assertNotNull(root_comma);
		Assert.assertNotNull(root_sets);
		Assert.assertNotNull(root_on);
		Assert.assertNotNull(root_takes);
		Assert.assertNotNull(root_off);
		Assert.assertNotNull(root_period);
		Assert.assertNotNull(root_X);
		
		/////////////////////////////
		
		Assert.assertEquals(root_it.children.size(), 5);
		
		Assert.assertTrue(root_it.children.containsKey(sourceVocab.getID("makes")));
		Assert.assertTrue(root_it.children.containsKey(PrefixTree.X));
		
		Node root_it_makes = root_it.children.get(sourceVocab.getID("makes"));
		Node root_it_mars = root_it.children.get(sourceVocab.getID("mars"));
		Node root_it_sets = root_it.children.get(sourceVocab.getID("sets"));
		Node root_it_takes = root_it.children.get(sourceVocab.getID("takes"));
		Node root_it_X = root_it.children.get(PrefixTree.X);
				
		Assert.assertNotNull(root_it_makes);
		Assert.assertNotNull(root_it_mars);
		Assert.assertNotNull(root_it_sets);
		Assert.assertNotNull(root_it_takes);
		Assert.assertNotNull(root_it_X);

		/////////////////////////////
		
		Assert.assertEquals(root_makes.children.size(), 2);
		
		Assert.assertTrue(root_makes.children.containsKey(sourceVocab.getID("him")));
		Assert.assertTrue(root_makes.children.containsKey(PrefixTree.X));
		
		Node root_makes_him = root_makes.children.get(sourceVocab.getID("him"));
		Node root_makes_X = root_makes.children.get(PrefixTree.X);
				
		Assert.assertNotNull(root_makes_him);
		Assert.assertNotNull(root_makes_X);
		
		/////////////////////////////
		
		Assert.assertEquals(root_X.children.size(), 11);

		Assert.assertTrue(root_X.children.containsKey(sourceVocab.getID("it")));
		Assert.assertTrue(root_X.children.containsKey(sourceVocab.getID("makes")));
		Assert.assertTrue(root_X.children.containsKey(sourceVocab.getID("him")));
		Assert.assertTrue(root_X.children.containsKey(sourceVocab.getID("and")));
		Assert.assertTrue(root_X.children.containsKey(sourceVocab.getID("mars")));
		Assert.assertTrue(root_X.children.containsKey(sourceVocab.getID(",")));
		Assert.assertTrue(root_X.children.containsKey(sourceVocab.getID("sets")));
		Assert.assertTrue(root_X.children.containsKey(sourceVocab.getID("on")));
		Assert.assertTrue(root_X.children.containsKey(sourceVocab.getID("takes")));
		Assert.assertTrue(root_X.children.containsKey(sourceVocab.getID("off")));
		Assert.assertTrue(root_X.children.containsKey(sourceVocab.getID(".")));
		
		Node root_X_it = root_X.children.get(sourceVocab.getID("it"));
		Node root_X_makes = root_X.children.get(sourceVocab.getID("makes"));
		Node root_X_him = root_X.children.get(sourceVocab.getID("him"));
		Node root_X_and = root_X.children.get(sourceVocab.getID("and"));
		Node root_X_mars = root_X.children.get(sourceVocab.getID("mars"));
		Node root_X_comma = root_X.children.get(sourceVocab.getID(","));
		Node root_X_sets = root_X.children.get(sourceVocab.getID("sets"));
		Node root_X_on = root_X.children.get(sourceVocab.getID("on"));
		Node root_X_takes = root_X.children.get(sourceVocab.getID("takes"));
		Node root_X_off = root_X.children.get(sourceVocab.getID("off"));
		Node root_X_period = root_X.children.get(sourceVocab.getID("."));
		
		Assert.assertNotNull(root_X_it);		
		Assert.assertNotNull(root_X_makes);
		Assert.assertNotNull(root_X_him);
		Assert.assertNotNull(root_X_and);
		Assert.assertNotNull(root_X_mars);
		Assert.assertNotNull(root_X_comma);
		Assert.assertNotNull(root_X_sets);
		Assert.assertNotNull(root_X_on);
		Assert.assertNotNull(root_X_takes);
		Assert.assertNotNull(root_X_off);
		Assert.assertNotNull(root_X_period);
		
		//TODO Finish implementing this test!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
		
		/////////////////////////////
		/*
		Assert.assertEquals(root_him.children.size(), 0);
				
		/////////////////////////////
		// Level 3 in the tree
		/////////////////////////////		
		
		Assert.assertEquals(root_it_makes.children.size(), 2);
		
		Assert.assertTrue(root_it_makes.children.containsKey(sourceVocab.getID("him")));
		Assert.assertTrue(root_it_makes.children.containsKey(PrefixTree.X));
		
		Node root_it_makes_him = root_it_makes.children.get(sourceVocab.getID("him"));
		Node root_it_makes_X = root_it_makes.children.get(PrefixTree.X);
				
		Assert.assertNotNull(root_it_makes_him);
		Assert.assertNotNull(root_it_makes_X);

		/////////////////////////////
		
		Assert.assertEquals(root_it_X.children.size(), 1);
		
		Assert.assertTrue(root_it_X.children.containsKey(sourceVocab.getID("him")));
		
		Node root_it_X_him = root_it_X.children.get(sourceVocab.getID("him"));
				
		Assert.assertNotNull(root_it_X_him);

		/////////////////////////////
		
		Assert.assertEquals(root_makes_him.children.size(), 0);
		
		/////////////////////////////
		
		Assert.assertEquals(root_makes_X.children.size(), 0);
		
		/////////////////////////////
		
		Assert.assertEquals(root_X_him.children.size(), 0);
		
		/////////////////////////////
		
		Assert.assertEquals(root_X_makes.children.size(), 2);
		
		Assert.assertTrue(root_X_makes.children.containsKey(sourceVocab.getID("him")));
		Assert.assertTrue(root_X_makes.children.containsKey(PrefixTree.X));
		
		Node root_X_makes_him = root_makes.children.get(sourceVocab.getID("him"));
		Node root_X_makes_X = root_makes.children.get(PrefixTree.X);
				
		Assert.assertNotNull(root_X_makes_him);
		Assert.assertNotNull(root_X_makes_X);
		
		/////////////////////////////
		
		Assert.assertEquals(root_X_makes_him.children.size(), 0);
		
		/////////////////////////////

		Assert.assertEquals(root_X_makes_X.children.size(), 0);
		
		/////////////////////////////
		 
		 */

		/*
		java.util.Collection<joshua.decoder.ff.tm.Rule> allRules = prefixTree.getAllRules();
		
		for (joshua.decoder.ff.tm.Rule rule : allRules) {
			System.out.println(rule.toString(ntVocab, sourceVocab, targetVocab));
		}
		
		System.out.println(allRules.size() + " total rules");
		*/
	}
	
	
	//@Test(dependsOnMethods={"verifyNodesShort"})
	//@Test
	public void testPrint() throws UnsupportedEncodingException, IOException {
		//System.err.println(prefixTree.size());
		//System.err.println(prefixTree);
		simplePrefixTree.print(System.out);
	}
	
	//@Test(dependsOnMethods={"setup"})
	public void test() throws UnsupportedEncodingException, IOException {
		
		int maxPhraseSpan = 10;
		int maxPhraseLength = 10;
		int maxNonterminals = 2;
		
		int minNonterminalSpan = 2;
		RuleExtractor ruleExtractor = new HierarchicalRuleExtractor(suffixArray, targetCorpusArray, alignments, lexProbs, Integer.MAX_VALUE, maxPhraseSpan, maxPhraseLength, maxNonterminals, minNonterminalSpan);
		
		
		String queryString = "it persuades him and it disheartens him";
		
		BasicPhrase querySentence = new BasicPhrase(queryString, sourceVocab);
		
		Assert.assertEquals(querySentence.toString(), "it UNK him and it UNK him");
		Assert.assertEquals(corpusSentence.toString(), corpusString);
		
		PrefixTree prefixTree = new PrefixTree(suffixArray, targetCorpusArray, alignments, lexProbs, ruleExtractor, querySentence.getWordIDs(), maxPhraseSpan, maxPhraseLength, maxNonterminals, minNonterminalSpan);
		

		
		//System.out.println(prefixTree.toString());
		//System.out.println();
		//System.out.println(prefixTree.size());
		
		
		Assert.assertTrue(prefixTree.root.children.containsKey(PrefixTree.X));
		Assert.assertTrue(prefixTree.root.children.containsKey(sourceVocab.getID("it")));
		Assert.assertTrue(prefixTree.root.children.containsKey(sourceVocab.getID("him")));
		Assert.assertTrue(prefixTree.root.children.containsKey(sourceVocab.getID("and")));
		Assert.assertTrue(prefixTree.root.children.containsKey(sourceVocab.getID(Vocabulary.UNKNOWN_WORD_STRING)));

		Assert.assertEquals(prefixTree.root.children.size(), 5);
		
		Assert.assertFalse(prefixTree.root.getChild(PrefixTree.X).children.containsKey(PrefixTree.X));
		Assert.assertTrue(prefixTree.root.getChild(PrefixTree.X).children.containsKey(sourceVocab.getID("it")));
		Assert.assertTrue(prefixTree.root.getChild(PrefixTree.X).children.containsKey(sourceVocab.getID("him")));
		Assert.assertTrue(prefixTree.root.getChild(PrefixTree.X).children.containsKey(sourceVocab.getID("and")));
		Assert.assertTrue(prefixTree.root.getChild(PrefixTree.X).children.containsKey(sourceVocab.getID(Vocabulary.UNKNOWN_WORD_STRING)));
		
		Assert.assertEquals(prefixTree.root.getChild(PrefixTree.X).children.size(), 4);
		
		//////
		
		Assert.assertNotNull(prefixTree.root.getChild(sourceVocab.getID("it")).sourceHierarchicalPhrases);
		
		for (Node node : prefixTree.root.children.values()) {
			
			Assert.assertNotNull(node.sourceHierarchicalPhrases);
			Assert.assertNotNull(node.results);
			//System.out.println(node.hierarchicalPhrases.size());
			
			/*
			for (Rule r : node.results) {
				
				System.out.println(r.toString(ntVocab, sourceVocab, targetVocab));
			}
			*/
			
		}
		
		//System.out.println("Children of 'him' (" + vocab.getID("him") + ") :\n\n");
		//System.out.println(prefixTree.root.getChild(vocab.getID("him")).toString(vocab));
		//System.out.println(prefixTree.root.getChild(vocab.getID("him")).children.size() + " children");
		
		for (Node node : prefixTree.root.getChild(sourceVocab.getID("him")).children.values()) {
			
			Assert.assertNotNull(node.sourceHierarchicalPhrases);
			Assert.assertNotNull(node.results);
			
			//System.out.println("Current node: " + node.toString(vocab));

			/*
			for (Rule r : node.results) {

				System.out.println(r.toString(ntVocab, sourceVocab, targetVocab));
			}
			*/
			
		}
		
		//prefixTree.print(System.out);
	}
	
}
