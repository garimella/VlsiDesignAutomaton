package designAutomator;

public class Config {
	static float tStart = 100;
	static float tEnd = 0;
	static double binWidth = 5;
	static float alpha(double temperature){
		return 0.8f;
	}
	static float beta = 0.8f;
	static int M = 0; // Dunno what m is...
	static double freeToCellMoveRatio = 5;
	static double netToOverlapCostFact = 100;
}
