package designAutomator;

public class Config {
	// TODO: tStart = number of blocks
	static float tStart = 10000;
	static float tEnd = 1;
	static double binWidth = 5;
	// TODO: make it a good function
	static float alpha(double temperature){
		return 0.9f;
	}
	// based on temperature
	static float beta = 0.7f;
	static int innerConditionUpdate = 1;
	static int M = 500;
	static double freeToCellMoveRatio = 5;
	static double netToOverlapCostFact = 100;
}
