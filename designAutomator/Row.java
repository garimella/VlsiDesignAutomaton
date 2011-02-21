package designAutomator;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;


import designAutomator.Row.Head.HeadType;

public class Row {
	
	int ypos;
	static double width;
	int overlap = 0;
	
	List<Head> headsList;
	PriorityQueue<Module> tempPrioQueue;
	
	public Row(int ypos) {
		// set the y position of the Row
		this.ypos = ypos;
		
		headsList = new LinkedList<Head>();
		tempPrioQueue = new PriorityQueue<Module>(10, new CompareXPos());
		headsList.add(new Head(HeadType.FREE_HEAD, 0, Row.width, null));		
	}
	
	public void addCellWithoutUpdate(Module m){
		tempPrioQueue.add(m);
	}
	
	public void generateInitialHeadsList(){
		double done = 0;
		double currModuleEnd, currModuleStart;
		
		
		while(!tempPrioQueue.isEmpty()){		
			Module m = tempPrioQueue.poll();
			currModuleStart = m.xPos;
			currModuleEnd = m.xPos + m.width;
			
			if(done < currModuleEnd){
				if(currModuleStart - done > 0){
					// There is free space
					headsList.add(new Head(Head.HeadType.FREE_HEAD, done, currModuleStart-done, null));
				}			
				done = currModuleEnd;
			}		
		}
	}
	public static void setWidth(double width) {
		Row.width = width;
	}
	
	public static class Head {
		HeadType type;
		double xpos;
		double length;
		Module m;
		public Head(HeadType type, double xpos, double length, Module m) {
			this.type = type;
			this.xpos = xpos;
			this.length = length;
			m=null;
		}
		
		static enum HeadType {
			MODULE_HEAD, FREE_HEAD
		}
	}
	public class CompareXPos implements Comparator<Module>{
		@Override
		public int compare(Module arg0, Module arg1) {		
			return (int) (arg0.xPos - arg1.xPos);
		}
	}
}
