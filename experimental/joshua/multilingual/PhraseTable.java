package joshua.multilingual;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

import joshua.util.sentence.Span;

public class PhraseTable extends HashMap<List<String>,BilingualRuleCollection> {

	int size;
	
	public PhraseTable(String filename) throws FileNotFoundException {
		this(filename, Direction.Normal);
	}
	
	public PhraseTable(String filename, Direction direction) throws FileNotFoundException {
		Scanner scanner = new Scanner(new File(filename));
		
		this.size = 0;
		
		while (scanner.hasNext()) {
			String line = scanner.nextLine();
			
			String[] parts = line.split("\\|\\|\\|");
			final String lhs = (direction==Direction.Normal) ? parts[0].trim() : parts[1].trim();
			String rhs = (direction==Direction.Normal) ? parts[1].trim() : parts[0].trim();

			String[] scoreParts = parts[parts.length-1].split(" ");
			Float score = Float.valueOf(scoreParts[scoreParts.length-5]);
			
			List<String> lhsList = new AbstractList<String>() {

				final String[] array = lhs.split("\\s+");
				
				@Override
				public String get(int index) {
					return array[index];
				}

				@Override
				public int size() {
					return array.length;
				}
				
			};
			
			if (!this.containsKey(lhsList)) {
				this.put(lhsList, new BilingualRuleCollection(lhsList));
			}
			
			this.size++;
			this.get(lhsList).add(rhs, score);
		}
	}
	
	public Collection<BilingualRuleCollection> getApplicableTranslationOptions(List<String> inputWords, Hypothesis hypothesisToExtend) {
		
		ArrayList<BilingualRuleCollection> result = new ArrayList<BilingualRuleCollection>();
		
		for (Span span : hypothesisToExtend.getExtendableSourceSpans(Main.settings.MAX_PHRASE_LENGTH)) {
			BilingualRuleCollection collection = this.get(inputWords.subList(span.start, span.end));
			
			if (collection != null) {
				result.add(collection);
			}
		}
		
		return result;
	}
	
	public int size() {
		return this.size;
	}
	
}

enum Direction {
	Normal,
	Reverse
}
