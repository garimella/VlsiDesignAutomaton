package designAutomator;

public class SimAnneal {
	Chip c;
	Circuit ckt;
	
	double initialTotalCost(){
		double totalCost = 0;
		for(Net n : ckt.circuit.getEdges()){
			totalCost += costNet(n);
		}
		return totalCost;
	}
	
	private double costNet(Net n) {
		// TODO Auto-generated method stub
		return 0;
	}

	Module moveSource, moveDest; // Allows unmaking moves
	double makeMove(){
		// Make one of the valid moves
		// and compute change in cost
		moveSource = null;
		moveDest = null;
		double oldPartialCost = cost(moveSource) + cost(moveDest);
		swap();
		double newPartialCost = cost(moveSource) + cost(moveDest);
		if(moveSource.width != moveDest.width){
			//Compute Overlap cost too.
		}
		// Weight all the costs to get penalty function
		return newPartialCost - oldPartialCost;
	}
	private void swap() {
		double tXPos, tYPos;
		tXPos = moveSource.xPos;
		tYPos = moveSource.yPos;
		moveSource.xPos = moveDest.xPos;
		moveSource.yPos = moveDest.yPos;
		moveDest.xPos = tXPos;
		moveDest.yPos = tYPos;
	}

	void unmakeMove(){
		swap();
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
	
	double cost(Module m1){
		double totalCost = 0;
		try {
			for(String v : ckt.circuit.getNeighbors(m1.name)){
				Module neighbour;
				neighbour = Module.cellList.get(v);
				if(neighbour == null){
					neighbour = Module.padList.get(v);
				}
				if(neighbour != null){
					totalCost += distance(m1, neighbour);
				}
				else {
					System.err.println("[SimAnneal.java] : Impossible Event - Module is there, but isn't");
					throw new Exception();
				}
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
	}
	public void simAnneal(){
		double t = Config.tStart;
		double totalCost = initialTotalCost();
		// Fix stopping and innerLoop conditions
		boolean stoppingCondition = false;
		boolean innerLoopCondition = false; 
		while(!stoppingCondition){
			while(!innerLoopCondition){				
				double diffCost = makeMove();				
				if(!accept(diffCost, t)){
					unmakeMove();
				}
				else {
					totalCost = totalCost + diffCost;
				}
			}
			t = update(t);
		}
	}
	private double update(double t) {
		return Config.alpha(t)*t; // Update value of alpha instead of using a function...
	}
	private boolean accept(double diffCost, double t) {
		double y = min(1,Math.pow(Math.E, -diffCost/t));
		double r = Math.random();
		if(r < y){
			return true;
		}
		else {
			return false;
		}
	}
	private double min(double d1, double d2) {
		return (d1 < d2)?d1:d2;
	}
}
