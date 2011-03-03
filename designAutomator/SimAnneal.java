package designAutomator;

public class SimAnneal {
	Chip c;
	Circuit ckt;
	double totalNetCost;
	int totalOverlapCost;
	
	double initialNetCost(){
		double totalCost = 0;
		for(Net n : ckt.circuit.getEdges()){
			totalCost += costNet(n);
		}
		return totalCost;
	}
	
	int initialOverlapCost() {
		int initialOverlap = 0;
		for (Row row : c.rows) {
			initialOverlap += row.initialOverlap();
		}
		return initialOverlap;
	}
	
	private double costNet(Net n) {
		double minXPos=0, minYPos=0, maxXPos=0, maxYPos=0;
		for(String nameOfModule: ckt.circuit.getIncidentVertices(n)){
			Module m;
			m = Module.cellList.get(nameOfModule);
			if(m == null) m = Module.padList.get(nameOfModule);
			
			assert(m != null);
			double mXPos = m.xPos+m.width/2;
			double mYPos = m.yPos+Module.HEIGHT/2;
			
			if(mXPos > maxXPos) maxXPos = mXPos;			
			if(mYPos > maxYPos) maxYPos = mYPos; 
			if(mXPos < minXPos) minXPos = mXPos;			
			if(mYPos < minYPos) minYPos = mYPos;
		}
		return ((maxXPos-minXPos) + (maxYPos - minYPos));
	}

	double penaltyFunction(double netCost, double overlapCost){
		// Reweight overlapCost and netCost to be in comparable order.
		double changedNetCost = netCost;
		double changedOverlapCost = overlapCost * Config.binWidth; 
		 
		double penalty = Config.beta*(changedNetCost/Config.netToOverlapCostFact) 
			+ (1-Config.beta)*(changedOverlapCost);
		//System.out.println("The penalty = " + penalty + " total overlap = " + totalOverlapCost);
		return penalty;
	}
	
	
	// set during the constructor
	double cellRatio;
	int currDiffOverlapCost = 0;
	double currDiffNetCost = 0;
	
	double calcCellMoveCost(Module moveSource, Module moveDest) {
		currDiffOverlapCost = Row.incrementalOverlap(moveSource, moveDest);
		double oldPartialCost = cost(moveSource) + cost(moveDest);

		_swap(moveSource, moveDest);
		double newPartialCost = cost(moveSource) + cost(moveDest);
		_swap(moveDest, moveSource);
		currDiffNetCost = newPartialCost - oldPartialCost;


		return penaltyFunction(currDiffNetCost, currDiffOverlapCost);
	}
	
	double calcCellToFreeMoveCost(Module moveSource, Row row, int freeBinIndex) {
		currDiffOverlapCost = Row.incrementalOverlapSwapFree(moveSource, row, freeBinIndex);
		Module freeTempModule = new Module("tempFreeModule");
		freeTempModule.xPos = row.freeBins.get(freeBinIndex) * Config.binWidth;
		freeTempModule.yPos = row.yPos;
		
		double oldNetCost = cost(moveSource);
		_swap(moveSource, freeTempModule);
		double newNetCost = cost(moveSource);
		_swap(moveSource, freeTempModule);
		currDiffNetCost = newNetCost - oldNetCost;
//		System.out.println("net diff = " + currDiffNetCost + 
//				"overlap diff = " + currDiffOverlapCost);
		return penaltyFunction(currDiffNetCost, currDiffOverlapCost);
	}
	
	private void _swap(Module m1, Module m2) {
		double tempDouble;				
		tempDouble = m1.xPos; m1.xPos = m2.xPos; m2.xPos = tempDouble;
		tempDouble = m1.yPos; m1.yPos = m2.yPos; m2.yPos = tempDouble;		
	}

	double calcPadMoveCost(Module moveSource, Module moveDest) {
		double oldPartialCost = cost(moveSource) + cost(moveDest);

		_swap(moveSource, moveDest);
		double newPartialCost = cost(moveSource) + cost(moveDest);
		_swap(moveDest, moveSource);
		currDiffNetCost = newPartialCost - oldPartialCost;
		// Due to pad moves, ovelap will never change! (Kashyap)
		// This is the bug which is causing random increase in overlap
		currDiffOverlapCost = 0;
		return penaltyFunction(currDiffNetCost, 0);
	}
	
	void makeMove(){
		
	}
			
	
	double distance(Module m1, Module m2){
		double m1CenterX, m1CenterY, m2CenterX, m2CenterY;
		m1CenterX = m1.xPos + m1.width/2;
		m2CenterX = m2.xPos + m1.width/2;
		m1CenterY = m1.yPos + Module.HEIGHT/2;
		m2CenterY = m2.yPos + Module.HEIGHT/2;
		
		double hpwl = Math.abs((m2CenterX-m1CenterX)) + Math.abs(m2CenterY - m1CenterY);
		return hpwl;
	}
	
	
	/*
	 * Cost of all nets connected to the current cell.
	 * */
	double cost(Module m1){
		double totalCost = 0;
		try {
			for(Net n : ckt.circuit.getIncidentEdges(m1.name)){
				totalCost+=costNet(n);
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return totalCost;
	}
	
	SimAnneal(Chip c, Circuit ckt){
		this.c = c;
		this.ckt = ckt;
		cellRatio = 
			(double)Module.cellList.size() / (double)(Module.cellList.size() + Module.padList.size());
		totalNetCost = initialNetCost();
		totalOverlapCost = initialOverlapCost();
		//System.out.println("Total net cost = " + totalNetCost + " total Overlap cost =" + totalOverlapCost);
	}
	
	public void simAnneal(){
		double t = Config.tStart;
		int acceptCount = 0;
		int rejectCount = 0;
		// Fix stopping and innerLoop conditions
		System.out.println("BEFORE: Net Cost = " +  totalNetCost + "; Overlap Cost = " + totalOverlapCost);
		while(t > Config.tEnd){
			acceptCount=0;
			rejectCount=0;
			for (int j = 0; j < Config.M; j = j + Config.innerConditionUpdate) {
				boolean wasPadMove;
				double diffCost;
				Module chosen1, chosen2 = null;
				Row randRow = null;
				int freeBinIndex = 0;
				double selectPadOrCell = Math.random();
				// selectPadOrCell = cellRatio/2;
				boolean wasCellToCellMove = false;
				if (selectPadOrCell < cellRatio) {
					wasPadMove = false;
					// choose with probability a cell with module
					// or free bin
					chosen1 = Module.cellList
							.get(Module.cellKeyList[(int) (Math.random() * (Module.cellKeyList.length - 1))]);

					if (selectPadOrCell < cellRatio / 3) {
						// cell to cell move
						chosen2 = Module.cellList
								.get(Module.cellKeyList[(int) (Math.random() * (Module.cellKeyList.length - 1))]);
						if ((Row.totalBinsInRow - chosen2.binInRow < chosen1.numBins)
								|| (Row.totalBinsInRow - chosen1.binInRow < chosen2.numBins)) {
							continue; // Don't allow overflows
						} else {
							diffCost = calcCellMoveCost(chosen1, chosen2);
							wasCellToCellMove = true;
						}
					} else {
						wasCellToCellMove = false;
						// cell to free bin
						int randRowIndex = (int) (Math.random() * (c.rows
								.size() - 1));
						randRow = c.rows.get(randRowIndex);
						while ((randRow.freeBins.size() < 1) || (chosen1.row == randRow)) {
							// while ((randRow.freeBins.size() < 1)) {
							randRowIndex = (int) (Math.random() * (c.rows
									.size() - 1));
							randRow = c.rows.get(randRowIndex);
						}
						freeBinIndex = (int) (Math.random() * (randRow.freeBins
								.size() - 1));
						if (chosen1.numBins > Row.totalBinsInRow
								- randRow.freeBins.get(freeBinIndex)) {
							continue;
						} else {
							diffCost = calcCellToFreeMoveCost(chosen1, randRow,
									freeBinIndex);
						}
					}
			}
			else {
					chosen1 = Module.padList.get(Module.padKeyList[(int) (Math
							.random() * Module.padKeyList.length)]);
					chosen2 = Module.padList.get(Module.padKeyList[(int) (Math
							.random() * Module.padKeyList.length)]);
					wasPadMove = true;
					diffCost = calcPadMoveCost(chosen1, chosen2);
				}

				if (accept(diffCost, t)) {
					acceptCount++;
					totalNetCost += currDiffNetCost;
					if (acceptCount % 100 == 0) {
						// System.out.println("[Internal] totOverlapCost=" +
						// totalOverlapCost);
						// System.out.println("[Internal] currDiffCost=" +
						// currDiffOverlapCost);
					}
					totalOverlapCost += currDiffOverlapCost;
					if (wasPadMove) {
						makePadMove(chosen1, chosen2);
					} else {
						if (wasCellToCellMove) {
							makeCellMove(chosen1, chosen2);
							if (acceptCount % 100 == 0) {

							}
						} else {
							makeCellToFreeMove(chosen1, randRow, freeBinIndex);
							;
						}
					}
					if (totalOverlapCost != initialOverlapCost()) {
						System.out.println("wasPadMove = " + wasPadMove
								+ " was cell to cell move = "
								+ wasCellToCellMove);
						System.out.println("Net Cost = " + totalNetCost
								+ "; Overlap Cost = " + totalOverlapCost);
						System.out.println("Calculated overlap cost ="
								+ initialOverlapCost());
						System.out.println("accept count = " + acceptCount);
						if (wasCellToCellMove)
							System.out.println(chosen1.row.yPos + " "
									+ chosen2.row.yPos);
						else
							System.out.println(chosen1.row.yPos + " "
									+ randRow.yPos + " - " + freeBinIndex);

						System.exit(1);
					}
				} else {
					rejectCount++;
				}
			}
			// System.out.println("Acceptance Ratio:" +
			// ((double)acceptCount/(acceptCount+rejectCount)));
			t = update(t);
		}
		System.out.println("Total Net Cost = " + totalNetCost
				+ " total overlap cost = " + totalOverlapCost);
		System.out.println("Computed After: Cost Net = " + initialNetCost()
				+ "Overlap = " + initialOverlapCost());
		System.out.println("AFTER: Net Cost = " + totalNetCost
				+ "; Overlap Cost = " + totalOverlapCost);
	}
	
	private void makeCellMove(Module m1, Module m2) {
		Row.swapCellWithCell(m1, m2);
	}
	
	private void makeCellToFreeMove(Module m, Row r, int freeBinIndex){
		Row.swapWithFreeBin(m, r, freeBinIndex);
	}

	private void makePadMove(Module p1, Module p2) {
		Chip.padSwap(p1,p2);
	}

	private double update(double t) {
		return Config.alpha(t)*t; // Update value of alpha instead of using a function...
	}
	private boolean accept(double diffCost, double t) {
		double y = min(1,Math.pow(Math.E, -diffCost/t));
		double r = Math.random();
//		System.out.println("Choosing between " + y + "\t" + r + "for "
//				+ "given diff=" + diffCost);
		if(r < y){			
			return true;
		}
		else {
			//System.out.println(" rejected");
			return false;
		}
	}
	private double min(double d1, double d2) {
		return (d1 < d2)?d1:d2;
	}
}
