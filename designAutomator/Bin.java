package designAutomator;

public class Bin {
	// On adding one element, the overlap is 0. So before that,
	// overlap is -1.
	int overlapAmount = -1;
	int xPos;
	
	public int addToBin(){		
		overlapAmount++;	
		return overlapAmount;
	}
	public int addManyToBin(int n){		
		overlapAmount += n;
		return overlapAmount;
	}
	public boolean isFree(){
		return (overlapAmount == -1);
	}
	public int removeFromBin(){
		overlapAmount--;
		return overlapAmount;
	}
}
