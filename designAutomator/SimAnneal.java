package designAutomator;

public class SimAnneal {
	Chip c;
	Circuit ckt;
	double totalNetCost;
	int totalOverlapCost;
	double tStart;
	double t;
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
	double initialCost, avgBinsPerRow;
	int totBins;
	void initialRowWidthCost() {
		initialCost= 0;
		totBins = 0;
		for (Module m: Module.cellList.values()){
			totBins += m.numBins;
		}
		avgBinsPerRow = ((double) totBins)/c.rows.size();
		for(Row r: c.rows){
			r.numBinsUsed = 0;
			for (Module m: r.moduleList){
				r.numBinsUsed += m.numBins;
			}
			initialCost += Math.abs(avgBinsPerRow - r.numBinsUsed);
		}		
	}
		
	private double costNet(Net n) {
		double minXPos=c.width, minYPos = c.height, maxXPos=0, maxYPos=0;
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

	double penaltyFunction(double netCost, double overlapCost, double rowWidthCost){
		// Reweight overlapCost and netCost to be in comparable order.
		double scaledNetCost = netCost/(c.height+c.width);
		double scaledOverlapCost = overlapCost/c.maxModuleLen; 
		double scaledRowWidthCost = rowWidthCost/c.width;
		double penalty = Config.beta1*(scaledNetCost) 
			+ Config.beta2*(scaledOverlapCost) + Config.beta3*(scaledRowWidthCost);
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

		double rowWidthCost = Row.diffRowWidth(moveSource, moveDest, avgBinsPerRow);
		return penaltyFunction(currDiffNetCost, currDiffOverlapCost, rowWidthCost);
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
		double rowWidthCost = Row.diffRowWidthFree(moveSource, row, freeBinIndex, avgBinsPerRow);
		return penaltyFunction(currDiffNetCost, currDiffOverlapCost, rowWidthCost);
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
		return penaltyFunction(currDiffNetCost, 0, 0);
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
		initialRowWidthCost(); 
		//System.out.println("Total net cost = " + totalNetCost + " total Overlap cost =" + totalOverlapCost);
	}
	
	public void simAnneal(){
		tStart = Config.tStart(ckt.circuit.getVertexCount());
		t = tStart;
		int acceptCount = 0;
		int rejectCount = 0;
		
		Config.setM((int)Math.pow(ckt.circuit.getVertexCount(), 1.0));
		
		// TODO: make it a function of t
		 //double windowHeight = (c.rows.size())*40;
		
		double windowHeight = (c.rows.size() / 4) * 40 * Math.log10(t);
		//double windowHeight = 40 * 10 ;
//		double windowWidth = 0;
		// Fix stopping and innerLoop conditions
		System.out.println("BEFORE: Net Cost = " +  totalNetCost + "; Overlap Cost = " + totalOverlapCost);
		while(true){
			acceptCount=0;
			rejectCount=0;
			innerLoop:
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

					if (selectPadOrCell < cellRatio / 5) {
						// cell to cell move
						double randRowIndex = chosen1.row.yPos - windowHeight/2 + Math.random() * windowHeight;
						if (randRowIndex >= c.rows.size() * 40)
							randRowIndex = (c.rows.size() - 1) * 40.0;
						if (randRowIndex < 0)
							randRowIndex = 0;
						randRow = c.rows.get((int)(randRowIndex/40.0));
						
						if (randRow.moduleList.isEmpty())
							continue;
						chosen2 = randRow.moduleList.get((int)Math.round((randRow.moduleList.size() - 1)* Math.random()));
						if ((Row.totalBinsInRow - chosen2.binInRow < chosen1.numBins)
								|| (Row.totalBinsInRow - chosen1.binInRow < chosen2.numBins)) {
							continue; // Don't allow overflows
						} else {
							diffCost = calcCellMoveCost(chosen1, chosen2);
							wasCellToCellMove = true;
						}
					} else {
						// cell to free bin
						wasCellToCellMove = false;
						int randRowIndex = (int) Math.round((Math.random() * (c.rows
								.size() - 1)));
						randRow = c.rows.get(randRowIndex);
						int retryCount = 0;
						while ((randRow.freeBins.size() < 1) || (chosen1.row == randRow)) {
							if(retryCount > Config.maxRetries ) {
								continue innerLoop;
							}
							else {
								randRowIndex = (int) Math.round(Math.random() * (c.rows
										.size() - 1));
								randRow = c.rows.get(randRowIndex);
								retryCount++;
							}
						}
						freeBinIndex = (int) Math.round(Math.random() * (randRow.freeBins
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
				} else {
					rejectCount++;
				}
			}
			acceptRatio = ((double)acceptCount/(acceptCount+rejectCount));
			System.out.println("Acceptance Ratio:" + acceptRatio + "; t = " + t +
					" prob = " + probAccepts + " deterministic = " + realAccepts);
			probAccepts = 0;
			realAccepts = 0;
			if(acceptRatio<0.05){
				break;
			}
			
			t = update();
			//windowHeight = (c.rows.size()) * 40 * (Math.log(1 + acceptRatio)/Math.log(2));
			windowHeight = (c.rows.size() / 4) * 40;
		}
		
		System.out.println("Total Net Cost = " + totalNetCost
				+ " total overlap cost = " + totalOverlapCost);
		System.out.println("Computed After: Cost Net = " + initialNetCost()
				+ "Overlap = " + initialOverlapCost());
		System.out.println("AFTER: Net Cost = " + totalNetCost
				+ "; Overlap Cost = " + totalOverlapCost + "Percentage Overlap = " + (double)totalOverlapCost/(double)totBins);
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

	private double update() {
		return Config.alpha(t, tStart)*t; // Update value of alpha instead of using a function...
	}
	
	int realAccepts = 0, probAccepts = 0;
	static double acceptRatio=1;
	private boolean accept(double diffCost, double t) {		
		if(diffCost <= 0){
			
			realAccepts ++;
			return true;
		}
		
		double y = min(Math.sqrt(t/tStart),Math.pow(Math.E, -(Config.penaltyWeight(c, acceptRatio)*diffCost)/t));
		//double y = min(1, Math.pow(Math.E, -((ckt.circuit.getVertexCount()) * diffCost)/(10* t)));
		//double y = min(1,Math.pow(Math.E, -(diffCost)/t));
		
		double r = Math.random();
//		System.out.println("Choosing between " + y + "\t" + r + "for "
//				+ "given diff=" + diffCost);
		if(r < y){
			//if(y < 0.75) System.out.println("Wow! : "+y);
			probAccepts++;
			return true;
		}
		return false;
//			System.out.println("; diffCost = " + diffCost); 
			
			
		
	}
	private double min(double d1, double d2) {
		return (d1 < d2)?d1:d2;
	}
}
