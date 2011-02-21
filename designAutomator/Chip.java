package designAutomator;

import java.util.Map;
import java.util.Vector;

public class Chip {
	double area;
	double height;
	double width;
	Vector<Row> rows;
	
	
	public double getArea() {
		return area;
	}

	public void setArea(double area) {
		this.area = area;
		System.out.println("Chip area = " + area);
		this.height =  this.width = 1.25*Math.sqrt(area);
		rows = new Vector<Row>( (int) (height/40.0));
		placePads();
	}
	public void placePads(){
		// Interval is the gap between two pads. 
		// This is (Perimeter of Chip)/(No. of Pads)
		// The thing is first and last may not be placed at the same interval
		// We can solve that by adding an extra pad on each side so that the first and the last
		// are separated by a little more than all others...
		double padInterval = (4 * height)/(Module.padList.size()+4);
		double xPos = 0;
		double yPos = 0;
		/*
		 * placingIn 0 => Base
		 * placingIn 1 => Right edge
		 * placingIn 2 => Top
		 * placingIn 3 => Left edge
		 * */
		int placingIn = 0;
		int padsPlaced = 0;
		// Place Pads in Periphery
		for(Map.Entry<String, Module> pad : Module.padList.entrySet()){
			pad.getValue().xPos = xPos;
			pad.getValue().yPos = yPos;
			padsPlaced += 1;
			// Update x and y positions
			switch(placingIn){ 
			case 0:
				if(xPos+padInterval > width-padInterval){					
					System.out.println(padsPlaced + " placed at base");
					padsPlaced = 0;
					placingIn = 1;
					xPos = width;
					yPos = 0;
				}
				else {
					xPos += padInterval;					
				}
				break;
			case 1:
				if(yPos+padInterval > height-padInterval){
					System.out.println(padsPlaced + " placed at right edge");
					padsPlaced = 0;
					placingIn = 2;
					xPos = width;
					yPos = height;
				}
				else {
					yPos += padInterval;
				}
				break;
			case 2:
				if(xPos - padInterval < padInterval) {
					System.out.println(padsPlaced + " placed at top");
					padsPlaced = 0;
					placingIn = 3;
					yPos = height;
					xPos = 0;
				}
				else {
					xPos -= padInterval;
				}
				break;
			case 3:
				if(yPos - padInterval < padInterval){
					System.out.println(padsPlaced + " placed at left edge");
					
					padsPlaced = 0;
					placingIn = -1;
					// Put all extras in impossible positions;
					xPos = -1;
					yPos = -1;					
				}
				else { 
					yPos -= padInterval;					
				}
				break;
			
			case -1:				
				break;
			}	
		}
		if(placingIn == -1){
			if( padsPlaced != 0){
				System.err.println(padsPlaced + " pads possibly not placed.");
			}
		}
		else {
			System.out.println(padsPlaced + " placed at left edge");
		}
	}
	public double getHeight(){
		return this.height;
	}
	public double getWidth(){
		return this.width;
	}
	
}
