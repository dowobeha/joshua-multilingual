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

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Lane Schwartz
 */
public class BilingualRuleCollection {

	final List<String> lhs;
	final List<List<String>> rightHandSides;
	final List<Float>  scores;
	
	public BilingualRuleCollection(List<String> lhs) {
		this.lhs = lhs;
		this.rightHandSides = new ArrayList<List<String>>();
		this.scores = new ArrayList<Float>();
	}
	
	public void add(final String rhs, Float score) {
		this.rightHandSides.add(new AbstractList<String>(){
			
			final String[] array = rhs.split("\\s+");
			
			@Override
			public String get(int index) {
				return array[index];
			}

			@Override
			public int size() {
				return array.length;
			}
			
		});
		this.scores.add(score);
	}
	
	public int size() {
		return rightHandSides.size();
	}
}
