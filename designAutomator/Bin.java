package designAutomator;

public class Bin {
	// On adding one element, the overlap is 0. So before that,
	// overlap is -1.
	int numCells = -1;
	int xPos;
	
	public void addToBin(){
		numCells++;		
	}
	public void addManyToBin(int n){
		numCells += n;
	}
	public boolean isFree(){
		return (numCells == -1);
	}
	public void removeFromBin(){
		numCells--;
	}
}
