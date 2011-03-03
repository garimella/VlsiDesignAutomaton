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
	 * The bins present in this row
	 */
	Vector<Bin> bins;
	
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
	}
	
	/**
	 * Add a cell to a bin
	 * @param binPos the bin number to which the cell is to added
	 */
	void addCellToBinAndUpdateFreeBins(int binPos){
		int overlapAmount = bins.get(binPos).addToBin();
		if(overlapAmount >= 0 && freeBins.contains(binPos)){
			freeBins.remove(freeBins.indexOf(binPos));
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

		addCellToBinAndUpdateFreeBins(startBin);
		
		int endBin = m.numBins - 1;
		for(int i = 1; i <= endBin;i++){
			if(startBin+i < totalBinsInRow){
				addCellToBinAndUpdateFreeBins(startBin + i);
			}			
		}
	}
	
	/**
	 *  
	 * and somehow fix the overlaps for the overflow bin.
	 * @return initial overlap of the row
	 */
	public int initialOverlap(){
		int overlap = 0;
		for(Bin curBin : bins) {
			if (curBin.overlapAmount > 0)
				overlap += 
					curBin.overlapAmount; 					
		}	
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
	public static int incrementalOverlap(Module module1, Module module2){
		if (module1.row.yPos == module2.row.yPos) {
			int start = Config.min(module1.binInRow, module2.binInRow);
			int end = Config.max(module1.binInRow, module2.binInRow);
			int maxlength = Config.max(
					module1.numBins, module2.numBins);
			
			int initialOverlap = 0;
			
			for (int i = start; i < end + maxlength; i++) {
				int binOverlap = module1.row.bins.get(i).overlapAmount ;
				initialOverlap += binOverlap > 0 ? binOverlap : 0;
			}
			swapCellWithCell(module1, module2);
			
			int finalOverlap = 0;
			for (int i = start; i < end; i++) {
				int binOverlap = module1.row.bins.get(i).overlapAmount ;
				finalOverlap += binOverlap > 0 ? binOverlap : 0;
			}
			swapCellWithCell(module1, module2);
			
			return finalOverlap - initialOverlap;
		}
		
		int increaseInModule1RowOverlap = 0;
		int reductionInModule1RowOverlap = 0;
		int increaseInModule2RowOverlap = 0;
		int reductionInModule2RowOverlap = 0;
		for(int b=module1.binInRow; b < module1.binInRow+module1.numBins; b++){
			if(b < module1.binInRow + module2.numBins){
				// module2 will also contribute here. So overlap doesn't get decreased.
				// We can re-write the loop now as
				// for(int b=module1.binInRow+module2.numBins; b < module1.binInRow+module1.numBins; b++){
			} else {
				int ovl = module1.row.bins.get(b).overlapAmount;
				reductionInModule1RowOverlap += (ovl>=1)?1:0;
			}
		}
		
		// If module2 is bigger
		for(int b=module1.binInRow+module1.numBins;
				b < module1.binInRow+module2.numBins; b++){
			int ovl = module1.row.bins.get(b).overlapAmount;
			increaseInModule1RowOverlap += (ovl >= 0)?1:0;
		}
		
		for(int b=module2.binInRow; b < module2.binInRow+module2.numBins; b++){
			if(b < module2.binInRow + module1.numBins){
			}
			else {
				int ovl = module2.row.bins.get(b).overlapAmount;
				reductionInModule2RowOverlap += (ovl>=1)?1:0;
			}
		}
		// If module1 is bigger
		for(int b=module2.binInRow+module2.numBins;
				b < module2.binInRow+module1.numBins; b++){
			int ovl = module2.row.bins.get(b).overlapAmount;
			increaseInModule2RowOverlap += (ovl >= 0)?1:0;
		}
		
		return(increaseInModule1RowOverlap+increaseInModule2RowOverlap
				-reductionInModule1RowOverlap-reductionInModule2RowOverlap);	
				
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
		int overlap = 0;
		int fb = row.freeBins.get(freeBinIndex);
		int reductionInModuleRowOverlap = 0;
		int increaseInFreeCellRowOverlap = 0;
		for(int b=module.binInRow; b < module.binInRow+module.numBins; b++){
			int ovl = module.row.bins.get(b).overlapAmount;
			// An overlap reduction will happen only if originally there was
			// some cell ther other than the current cell. Else overlap will
			// remain 0;
			reductionInModuleRowOverlap += (ovl>=1)?1:0;
		}		
		
		for(int b=fb; b < fb+module.numBins; b++){
			int ovl = row.bins.get(b).overlapAmount;
			//Increase in overlap will happen only if there is atleast 1 cell
			// originally in that.
			increaseInFreeCellRowOverlap += (ovl>=0)?1:0;
		}
		overlap = increaseInFreeCellRowOverlap-reductionInModuleRowOverlap;
		return overlap;
	}
	
	public static void _swapCellWithCell(Module m1, Module m2){
		int m2LastBin = m2.binInRow+m2.numBins - 1;
		int m1LastBin = m1.binInRow+m1.numBins - 1;
		
		for(int b = m1.binInRow;b < m1LastBin; b++){
			m1.row.bins.get(b).removeFromBin();
		}
		for(int b = m2.binInRow;b < m2LastBin; b++){
			m2.row.bins.get(b).removeFromBin();
		}
		for(int b = m2.binInRow;b < m2.binInRow+m1.numBins-1 ; b++){
			m2.row.bins.get(b).addToBin();
		}
		for(int b = m1.binInRow;b < m1.binInRow+m2.numBins-1 ; b++){
			m1.row.bins.get(b).addToBin();
		}
		// Don't handle freeBins now.
	}
	
	/**
	 * Swaps a cell with another cell.
	 * @param m1 cell 1
	 * @param m2 cell 2
	 */
	public static void swapCellWithCell(Module m1, Module m2) {				
		// Move all extras added by m2 away
		for(int b = m1.binInRow+m1.numBins; b < m1.binInRow+m2.numBins;b++){
			m1.row.addCellToBinAndUpdateFreeBins(b);
		}
		
		// Move all extras added by m1 away
		for(int b = m2.binInRow+m2.numBins; b < m2.binInRow+m1.numBins;b++){
			m2.row.addCellToBinAndUpdateFreeBins(b);
		}
		
		// Remove all orphans of m1 away
		for(int b = m1.binInRow+m2.numBins; b < m1.binInRow+m1.numBins;b++){
			m1.row.removeFromBinAndUpdateFreeBins(b);
		}
		
		// Remove all orphans of m2 away
		for(int b = m2.binInRow+m1.numBins; b < m2.binInRow+m2.numBins;b++){
			m2.row.removeFromBinAndUpdateFreeBins(b);
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
			}			
			else 
				assert(false);
			
			if (freeBinIndexInRow  + i < totalBinsInRow) {
				row.addCellToBinAndUpdateFreeBins(
						freeBinIndexInRow  + i);
			}
			else 
				assert(false);
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
