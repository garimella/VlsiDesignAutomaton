package designAutomator;

public class Config {
	// TODO: tStart = number of blocks
	
	static double tStart(int numBlocks){
		return numBlocks;
	}	
	static double tEnd = .001f;
	static double binWidth = 5;
	// TODO: make it a good function
	static double alpha(double t, double tS){
		if(t > 2*t/3){
			return 0.95;
		}
		else if(t > t/5){
			return 0.99;
		}
		else{
			return 0.95;
		}
	}
	static double penaltyWeight(Chip c, double acceptRatio){
		return 1;
	}
	// based on temperature
	//static double beta = 0.8f;
	static double beta(double t, double tS){
		if(t > 0.1*tS){
			return 0.9;
		}
		else {
			return 0.5;
		}
	}
	static int innerConditionUpdate = 1;
	static int M;
	static void setM(int m) {
		M = m;
	}
	static int maxRetries = 200;
	static double freeToCellMoveRatio = 5;
	static double netToOverlapCostFact = 200;
	public static int numExtraBins = 8;
	
	static int max(int i, int j) {
		return i > j ? i : j;
	}
	
	static int min(int i, int j) {
		return i < j ? i : j;
	}
}
