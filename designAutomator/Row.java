package designAutomator;

import java.util.Vector;

public class Row {
	double ypos;
	static double width;

	// Multiply by Config.binWidth to get the actual width of overlap
	// This is just the number of cells overlapping in a bin.
	int totOverlap = 0;
	Bin overflowBin;
	Vector<Bin> bins;
	
	static int numBins;
	public Row(double d) {
		// set the y position of the Row
		this.ypos = d;
				
		bins = new Vector<Bin>();
		for (int i = 0; i < numBins; i++) {
			bins.add(new Bin());
		}
		overflowBin = new Bin();
	}
	
	public void addCell(Module m){
		int baseBin = (int) Math.floor(m.xPos/Config.binWidth);
		
		// Basebin can never be an overflow bin.
		// Starting position is always same for two cells swapped.
		// None start at overflow.
		// By induction :P Q.E.D.
		bins.get(baseBin).addToBin();
		m.binInRow = baseBin;	// Module contains only first bin.
		int extraBins = m.numBins - 1;
		for(int i = 1; i <= extraBins;i++){
			if(baseBin+i < numBins){
				bins.get(baseBin+i).addToBin();
			}
			else {				
				overflowBin.addManyToBin(extraBins-i+1); 
				break;
			}
		}
		// Add element to bin iff the element actually extends to more
		// than half of it. Else don't.
		// Too costly each time. Just drop it.
		// if(extraBins*m.width > Config.binWidth/2){
		//	bins.get(baseBin+extraBins+1).addToBin();
		// }		
	}
	
	
	public int initialOverlap(){
		int totOverlap = 0;
		for(Bin curBin : bins){
			if (curBin.numCells > 0)
				totOverlap += curBin.numCells;
		}
		if (overflowBin.numCells > 0)
			totOverlap += overflowBin.numCells;
		
		this.totOverlap = totOverlap;
		return totOverlap;
 	}
	
	// Calculates overlap. But does not actually swap the modules.
	public static int incrementalOverlapPartial(Module oldM, Module newM){		
		int oldOverlap = 0;
		int newOverlap = 0;
		
		for(int i = oldM.binInRow; i < oldM.binInRow+oldM.numBins; i++){
			if(i < numBins){
				if (oldM.row.bins.get(i).numCells > 0)
					oldOverlap += oldM.row.bins.get(i).numCells;
			}
			else {
				if (oldM.row.overflowBin.numCells > 0)
					oldOverlap += oldM.row.overflowBin.numCells;
				break;
			}			
		}
		
		int diffNumBins = newM.numBins - oldM.numBins;
		if(diffNumBins > 0 ){
			//newM has more cells.
			newOverlap = oldOverlap;
			for (int i = oldM.binInRow + oldM.numBins; i < oldM.binInRow + newM.numBins; i++) {
				if (i < numBins) {
						newOverlap += oldM.row.bins.get(i).numCells + 1;
				}
				else {
						newOverlap += oldM.row.overflowBin.numCells + 1;
					break;
				}		
			}
			
		}
		else {
			// oldm has more cells than newM. So
			// for newM cells, computing overlap is enough
			newOverlap = oldOverlap;
			for (int i = oldM.binInRow + newM.numBins; i < oldM.binInRow + oldM.numBins; i++) {
				if (i < numBins) {
					if (oldM.row.bins.get(i).numCells > 1)
						newOverlap -= oldM.row.bins.get(i).numCells - 1;
				}
				else {
					if (oldM.row.overflowBin.numCells > 1)
						newOverlap -= oldM.row.overflowBin.numCells - 1;
					break;
				}
			}
		}
		return (newOverlap - oldOverlap); 
	}
	
	public static int swapWithFreeBin(Module m, Bin b){
		// Swap a free bin with the module and
		// also compute the total overlap
		return 0;
	}
	
	static void _swap(Module m1, Module m2){
		int  tempBinInRow; double tempDouble;
		Row tempRow;
		tempRow = m1.row; m1.row = m2.row; m2.row = tempRow;		
		tempDouble = m1.xPos; m1.xPos = m2.xPos; m2.xPos = tempDouble;
		tempDouble = m1.yPos; m1.yPos = m2.yPos; m2.yPos = tempDouble;
		tempBinInRow = m1.binInRow; m1.binInRow = m2.binInRow; m2.binInRow = tempBinInRow;
	}
	
	public static void swap(Module m1, Module m2){
		
		// have last bin count for ease
		int m2LastBin = m2.binInRow+m2.numBins;
		int m1LastBin = m1.binInRow+m1.numBins;
		
		// overflows for each module
		int m1OverflowBinCount = 0, m2OverflowBinCount = 0;
		
		// fix the last bin counts for each module
		if (m1LastBin >= Row.numBins) {
			m1OverflowBinCount = m1LastBin - Row.numBins + 1;
			m1LastBin = Row.numBins - 1;
		}
	
		if (m2LastBin >= Row.numBins) {
			m2OverflowBinCount = m2LastBin - Row.numBins + 1;
			m2LastBin = Row.numBins - 1;
		}
		
		
		int diffNumBins = m2LastBin - m1LastBin;
		if(diffNumBins == 0){
			_swap(m1,m2);
			return;
		}else  if (diffNumBins < 0) {
			// m1 has more than m2
			for (int i = 0; i < -diffNumBins; i++) {
				if (m2LastBin + i + 1 < Row.width) {
					m2.row.bins.get(m2LastBin + i + 1).addToBin();
				} else {
					assert(false);
				}
				assert(m1LastBin - i > 0);
				m1.row.bins.get(m1LastBin - i).removeFromBin();
				
			}
		}
		if (diffNumBins > 0) {
			// m2 has more than m1
			for (int i = 0; i < diffNumBins; i++) {
				if (m1LastBin + i + 1 < Row.width) {
					m1.row.bins.get(m1LastBin + i + 1).addToBin();
				} else {
					assert(false);
				}
				assert(m2LastBin -i > 0);				
				m2.row.bins.get(m2LastBin - i).removeFromBin();
				
			}
		}
		
		// Set the overflow bins
		if (m1OverflowBinCount > 0) {
			m1.row.overflowBin.addManyToBin(-m1OverflowBinCount);
			m2.row.overflowBin.addManyToBin(m1OverflowBinCount);
		}
		if (m2OverflowBinCount > 0) {
			m2.row.overflowBin.addManyToBin(-m2OverflowBinCount);
			m1.row.overflowBin.addManyToBin(m2OverflowBinCount);
		}
		
		_swap(m1, m2);
	}
}
