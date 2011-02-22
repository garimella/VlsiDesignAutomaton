package designAutomator;

import java.util.Comparator;
import java.util.PriorityQueue;

public class Row {
	
	int ypos;
	static double width;
	double overlap = 0;
	
	DoublyLinkedList<Head> headsList;
	PriorityQueue<Module> tempPrioQueue;
	
	public Row(int ypos) {
		// set the y position of the Row
		this.ypos = ypos;
		
		headsList = new DoublyLinkedList<Head>();
		tempPrioQueue = new PriorityQueue<Module>(10, new CompareXPos());		
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
			// Update Module object to contain h
			if(done < currModuleEnd) {
				if(currModuleStart - done > 0){
					// There is free space
					headsList.addToEnd(new Head(Head.HeadType.FREE_HEAD, done, currModuleStart-done, null));
				}
				headsList.addToEnd(new Head(Head.HeadType.MODULE_HEAD,
						currModuleStart, currModuleEnd - currModuleStart, m));
				m.rowHead = headsList.tail;
				done = currModuleEnd;
			} else {
				headsList.addToEnd(new Head(Head.HeadType.MODULE_HEAD, 
						currModuleStart, currModuleEnd - currModuleStart, m));
				m.rowHead = headsList.tail;
			}
		}
	}
	
	public void initialOverlap(){
		DoublyLinkedListNode<Row.Head> iter = headsList.head;
		while(iter != null){
			Row.Head currHead = iter.data;
			DoublyLinkedListNode<Row.Head> innerIter = iter.next;
			while(innerIter != null){
				Row.Head nextHead = innerIter.data;
				if(nextHead.xpos > currHead.xpos + currHead.length){
					break;
				}
				else {
					double end = min ((currHead.xpos + currHead.length), (nextHead.xpos+ nextHead.length));
					overlap += end - nextHead.xpos;
				}
				innerIter = innerIter.next;
			}
			iter = iter.next;
		}
 	}
	
	/* 
	 * Stupid, Complex, Meaningless yet useful[if ever done]
	 * replaces oldHead by newHead and returns overlap difference.
	 */
	public static double swap(Row row1, DoublyLinkedListNode<Row.Head> head1, Row row2, DoublyLinkedListNode<Row.Head> replacementHead){
		/*
		 * Forward 
		 */
	}
	private double min(double a, double b) {
		return (a > b) ? b : a;
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
