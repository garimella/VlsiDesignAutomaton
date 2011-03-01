package designAutomator;

import java.util.Vector;

import org.apache.commons.collections15.set.ListOrderedSet;

import designAutomator.Module.ModuleType;

public class Row {
	/**
	 * The width of the row, fixed in the beginning; after parsing 
	 * the circuit
	 */
	static double width;

	/**
	 * The y position of the chip
	 */
	double yPos;
	
	/**
	 * totalRowOverlap is in terms of bins (Check Config.binWidth)
	 */
	int totalRowOverlap = 0;
	
	/**
	 * The bins present in this row
	 */
	Vector<Bin> bins;
	
	/**
	 * An overflow bin which takes care of bins going out of chip
	 */
	Bin overflowBin;
	
	/**
	 * An index of the available freeBins in the row
	 */
	ListOrderedSet<Integer> freeBins;
	
	/**
	 * Total number of bins in a row
	 */
	static int totalBinsInRow;
	
	/**
	 * The constructor of the Row 
	 * @param yPos The y position of the row
	 */
	public Row(double yPos) {
		// initialize the y position
		this.yPos = yPos;
		
		// create all the data structures required - bins, 
		// freebin index, 
		bins = new Vector<Bin>();
		freeBins = new ListOrderedSet<Integer>();
		for (int i = 0; i < totalBinsInRow; i++) {
			Bin b = new Bin();
			bins.add(b);
			freeBins.add(i);
		}
		overflowBin = new Bin();		
	}
	
	/**
	 * Add a cell to a bin
	 * @param binPos the bin number to which the cell is to added
	 */
	void addCellToBinAndUpdateFreeBins(int binPos){
		int overlapAmount = bins.get(binPos).addToBin();
		if(overlapAmount >= 0){
			if(freeBins.contains(binPos)){
				freeBins.remove(freeBins.indexOf(binPos));
			}
		}
	}
	
	/**
	 * Remove a cell from a bin
	 * @param binPos the bin number to which the cell belongs
	 */
	void removeFromBinAndUpdateFreeBins(int binPos){
		int numCells = bins.get(binPos).removeFromBin();
		if(numCells == -1){
			freeBins.add(binPos);
		}
	}
	
	/**
	 * Add a cell to this row
	 * @param m - cell to be added
	 */
	public void addCell(Module m){
		int startBin = m.binInRow;
		
		// Starting bin can never be an overflow bin.
		addCellToBinAndUpdateFreeBins(startBin);
		
		int endBin = m.numBins - 1;
		for(int i = 1; i <= endBin;i++){
			if(startBin+i < totalBinsInRow){
				addCellToBinAndUpdateFreeBins(startBin + i);
			}
			else {				
				overflowBin.addManyToBin(endBin - i + 1); 
				break;
			}
		}
	}
	
	/**
	 * Completely screwed up! Multiply by (n C 2) 
	 * and somehow fix the overlaps for the overflow bin.
	 * @return initial overlap of the row
	 */
	public int initialOverlap(){
		int overlap = 0;
		for(Bin curBin : bins) {
			if (curBin.overlapAmount > 0)
				overlap += 
					(curBin.overlapAmount 
					* (curBin.overlapAmount + 1)) /2;
		}
	
		// This is not right, but for time sake, we are doing this.
		if (overflowBin.overlapAmount > 0)
			overlap += overflowBin.overlapAmount *
				(overflowBin.overlapAmount + 1) / 2;
	
		this.totalRowOverlap = overlap;
		return overlap;
 	}

	/**
	 * Returns the incremental overlap when module1 is replace by
	 * module2
	 * Note: This routine does not swap the modules.
	 * @param module1 the original module
	 * @param module2 the new module that replaces original
	 * @return the incremental overlap
	 */
	public static int incrementalOverlapPartial(Module module1, 
			Module module2){		
		int oldOverlap = 0;
		for(int i = module1.binInRow;
			i < module1.binInRow+module1.numBins; i++){
			if(i < totalBinsInRow){
				if (module1.row.bins.get(i).overlapAmount > 0)
					oldOverlap 
						+= module1.row.bins.get(i).overlapAmount;
			}
			else {
//				if (module1.row.overflowBin.overlapAmount > 0)
//					oldOverlap 
//						+= module1.row.overflowBin.overlapAmount;
				break;
			}			
		}
		
		int newOverlap = oldOverlap;
		int diffNumBins = module2.numBins - module1.numBins;
		if(diffNumBins > 0 ) {
			// module2 has more cells than module1
			for (int i = module1.binInRow + module1.numBins;
				i < module1.binInRow + module2.numBins; i++) {
				if (i < totalBinsInRow) {
						newOverlap += 
							module1.row.bins.get(i).overlapAmount + 1;
				}
				else {
//					newOverlap += 
//						module1.row.overflowBin.overlapAmount + 1
//						+ module2.numBins + module1.binInRow - i;
					break;
				}		
			}
		} else {
			// module1 has more cells than module2
			for (int i = module1.binInRow + module2.numBins; 
				i < module1.binInRow + module1.numBins; i++) {
				if (i < totalBinsInRow) {
					if (module1.row.bins.get(i).overlapAmount > 1)
						newOverlap -= 
							module1.row.bins.get(i).overlapAmount - 1;
				}
				else {
//					if (module1.row.overflowBin.overlapAmount > 1)
//						newOverlap -= 
//						(module1.row.overflowBin.overlapAmount - 
//							(module1.numBins + module1.binInRow - i));
					break;
				}
			}
		}
		return (newOverlap - oldOverlap); 
	}
	
	/**
	 * Incremental overlap when a module is swapped with a 
	 * free space.
	 * Note: This routine does not really swap the module.
	 * @param module
	 * @param row
	 * @param freeBinIndex
	 * @return
	 */
	public static int incrementalOverlapSwapFree(Module module, 
			Row row, int freeBinIndex) {
		Module freeTempModule = new Module("tempFreeModule");
		freeTempModule.row = row;
		freeTempModule.binInRow = row.freeBins.get(freeBinIndex);
		freeTempModule.numBins = 0;
		freeTempModule.xPos = 
			row.freeBins.get(freeBinIndex) * Config.binWidth;
		return  incrementalOverlapPartial(freeTempModule, module)
		 + incrementalOverlapPartial(module, freeTempModule);
	}
	
	/**
	 * Swaps a cell with another cell.
	 * @param m1 cell 1
	 * @param m2 cell 2
	 */
	public static void swapCellWithCell(Module m1, Module m2) {
		// have last bin count for ease
		int m2LastBin = m2.binInRow+m2.numBins - 1;
		int m1LastBin = m1.binInRow+m1.numBins - 1;
		
		// overflows for each module
		int m1OverflowBinCount = 0, m2OverflowBinCount = 0;
		
		// fix the last bin counts for each module
		if (m1LastBin >= Row.totalBinsInRow) {
			m1OverflowBinCount = m1LastBin - Row.totalBinsInRow + 1;
			m1LastBin = Row.totalBinsInRow - 1;
		}
	
		if (m2LastBin >= Row.totalBinsInRow) {
			m2OverflowBinCount = m2LastBin - Row.totalBinsInRow + 1;
			m2LastBin = Row.totalBinsInRow - 1;
		}
		
		
		int diffNumBins = m2LastBin - m1LastBin;
		if(diffNumBins == 0){
			_swap(m1,m2);
			return;
		} else  if (diffNumBins < 0) {
			// m1 has more than m2
			for (int i = 0; i < -diffNumBins; i++) {
				if (m2LastBin + i + 1 < Row.width) {
					m2.row.addCellToBinAndUpdateFreeBins(
							m2LastBin + i + 1);
				} else {
					assert(false);
				}
				assert(m1LastBin - i > 0);
				m1.row.removeFromBinAndUpdateFreeBins(m1LastBin - i);
			}
		} else { // diffNumBins > 0
			// m2 has more than m1
			for (int i = 0; i < diffNumBins; i++) {
				if (m1LastBin + i + 1 < Row.width) {
					m1.row.addCellToBinAndUpdateFreeBins(
							m1LastBin + i + 1);
				} else {
					assert(false);
				}
				assert(m2LastBin -i > 0);				
				m2.row.removeFromBinAndUpdateFreeBins(m2LastBin - i);
				
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
	
	/**
	 * Swap a cell with a free bin
	 * @param module the module to be swapped
	 * @param row the row which contains the free bin
	 * @param freeBinIndex the index of the free bin in the row
	 */
	static void swapWithFreeBin(Module module, Row row, 
			int freeBinIndex){
		// The module should be a cell
		assert(module.type == ModuleType.CELL);
		
		// fix all the bin values
		int freeBinIndexInRow = row.freeBins.get(freeBinIndex);
		for (int i = 0; i < module.numBins; i ++) {
			if (module.binInRow + i < totalBinsInRow) {
				module.row.removeFromBinAndUpdateFreeBins(
						module.binInRow + i);
			} else {
				module.row.overflowBin.removeFromBin();
			}
			
			if (freeBinIndexInRow  + i < totalBinsInRow) {
				row.addCellToBinAndUpdateFreeBins(
						freeBinIndexInRow  + i);
			} else {
				row.overflowBin.addToBin();
			}
		}
		
		// set the module's position to the new row's free bin
		module.setPosition(row, freeBinIndexInRow);
	}
	
	/**
	 * Internal routine which does swapping of some crucial fields
	 * @param m1
	 * @param m2
	 */
	static void _swap(Module m1, Module m2){
		Row tempRow;
		tempRow = m1.row; 
		m1.row = m2.row; 
		m2.row = tempRow;		
		
		double tempDouble;
		tempDouble = m1.xPos; 
		m1.xPos = m2.xPos; 
		m2.xPos = tempDouble;
		tempDouble = m1.yPos; 
		m1.yPos = m2.yPos; 
		m2.yPos = tempDouble;
		
		int  tempBinInRow;
		tempBinInRow = m1.binInRow; 
		m1.binInRow = m2.binInRow; 
		m2.binInRow = tempBinInRow;
	}
}
