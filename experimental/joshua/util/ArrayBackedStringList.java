package joshua.util;

public class ArrayBackedStringList extends ArrayBackedList<String> {

	public ArrayBackedStringList(String string) {
		super(string.split("\\s+"));
	}

}
