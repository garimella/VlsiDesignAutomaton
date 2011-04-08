package designAutomator;

public class Config {
	// TODO: tStart = number of blocks
	
	static double tStart = 0;
	static double tStart(int numBlocks){
		tStart = Math.pow(numBlocks, 0.8);
		return tStart;
	}	
	
	static double tStart() {
		return tStart;
	}
	
	static double tEnd = .001f;
	static double binWidth = 3;
	// TODO: make it a good function
	static double alpha(double t, double tS){
		if (SimAnneal.acceptRatio > 0.6) {
			return 0.99;
		} else if (SimAnneal.acceptRatio > 0.3) {
			return 0.995;
		}
		else
			return 0.99;
//		if(t > 0.6 * tS){
//			return 0.90;
//		}
//		else if(t > 0.2 * tS){
//			return 0.99;
//		}
//		else{
//			return 0.90;
//		}
	}
	
	// netcost
	static double beta1 = 0.4;
	// overlaps
	static double beta2 = 0.2;
	// Row-width
	static double beta3 = 0.4;
	
	static double penaltyWeight(Chip c, double acceptRatio){
		return 1;
	}
	// based on temperature
	//static double beta = 0.8f;
	static double beta(double t, double tS){
		if(t > 0.1 * tS){
			return 0.7;
		}
		else {
			return 0.6;
		}
	}
	static int innerConditionUpdate = 1;
	static int M;
	static void setM(int m) {
		//M = m*3;
		M = (int)Math.pow(m,1.0);
	}
	static int maxRetries = 200;
	static double freeToCellMoveRatio = 5;
	static double netToOverlapCostFact = 100;
	public static int numExtraBins = 0;
	
	static int max(int i, int j) {
		return i > j ? i : j;
	}
	
	static int min(int i, int j) {
		return i < j ? i : j;
	}
}
