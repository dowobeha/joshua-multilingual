package joshua.multilingual;

public class BilingualRule {

	final String rhs;
	final float score;
	
	public BilingualRule(String line) {
		String[] parts = line.split("\\|\\|\\|");
//		this.lhs = parts[0];
		this.rhs = parts[1];

		String[] scoreParts = parts[parts.length-1].split(" ");
		this.score = Float.valueOf(scoreParts[scoreParts.length-5]);
	}
}
