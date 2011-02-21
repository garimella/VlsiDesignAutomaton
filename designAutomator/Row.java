package designAutomator;

import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Vector;

public class Row {
	
	int ypos;
	static double width;
	int overlap = 0;
	
	/**
	 * A sorted list of heads in the row.
	 */
	Vector<Head> head_list;
	
	/**
	 * Set of indices in head_list vector, which are free heads.
	 */
	SortedSet<Head> free_heads_index;
	
	public Row(int ypos) {
		// set the y position of the Row
		this.ypos = ypos;
		
		// initialize the head list and the index
		head_list = new Vector<Head>();
		free_heads_index = new TreeSet<Head>();
		
		head_list.insertElementAt(
				new Head(Head.HeadType.FREE_HEAD, 0, width), 0);
		free_heads_index.add(head_list.get(0));
	}
	
	public static void setWidth(double width) {
		Row.width = width;
	}
	
	public static class Head {
		HeadType type;
		double xpos;
		double length;
		
		public Head(HeadType type, double xpos, double length) {
			this.type = type;
			this.xpos = xpos;
			this.length = length;
		}
		
		static enum HeadType {
			MODULE_HEAD, FREE_HEAD
		}
	}
}
