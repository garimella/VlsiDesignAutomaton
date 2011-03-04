package designAutomator;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;

public class Chip {
	double area;
	double height;
	double width;
	Vector<Row> rows;
	
	public double getArea() {
		return area;
	}

	/**
	 * Sets the area of the chip and creates the data structure for
	 * rows.
	 * @param area area of the chip
	 */
	public void setArea(double area) {
		this.area = area;
		System.out.println("Chip area = " + area);
		this.height =  this.width = 1.25*Math.sqrt(area);
		
		// Create the rows and set the parameters of the rows
		Row.width = width;
		Row.totalBinsInRow = (int) Math.ceil(width/Config.binWidth) + Config.numExtraBins;
		rows = new Vector<Row>();
		for (int i = 0;i < (int) (height/40.0);i++) {			
			rows.add(new Row(40.0*i));
		}
		System.out.println("height = " + height+ "row size = " + rows.size());
	}
	
	/**
	 * Place pads uniformly on the periphery of the chip.
	 * Can be highly refactored ;). Will do it in the end. (Kashyap)
	 * @author:ScriptDevil
	 */
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
	
	public void placeCellsRandomly(){
		for (Map.Entry<String, Module> cellEntryList  : Module.cellList.entrySet()) {
			// choose a random row
			int row = (int)Math.round((Math.random() * (rows.size() - 1)));
			Module module = cellEntryList.getValue();
			module.numBins = (int)Math.ceil(module.width / Config.binWidth);
			// choose a random bin in that
			int randBin = (int)Math.round(Math.random() * (Row.totalBinsInRow - 1));
			while(Row.totalBinsInRow -randBin < module.numBins){
				randBin = (int)Math.round(Math.random() * (Row.totalBinsInRow - 1));
			}			
			module.setPosition(rows.get(row), randBin);
			rows.get(row).addCell(module);
		}
		for(Row row : rows){
			row.initialOverlap();
		}
	}
	
	public double getWidth(){
		return this.width;
	}
	public double getHeight(){
		return this.height;
	}

	public static void padSwap(Module p1, Module p2) {
		double t;
		t = p1.xPos; p1.xPos = p2.xPos; p2.xPos = t;
		t = p1.yPos; p1.yPos = p2.yPos; p2.yPos = t;		
	}
	public void dumpChipPlacements(String fileName){
		try
		{
			FileWriter fstream = new FileWriter(fileName);
			BufferedWriter out = new BufferedWriter(fstream);			
			out.write(Double.toString(this.width) + "\n");
			out.write(Double.toString(this.height) + "\n");
			out.write(Integer.toString(Module.cellKeyList.length+Module.padKeyList.length) +"\n");
			for(Entry<String, Module> modEntry: Module.cellList.entrySet()){
				out.write(Double.toString(modEntry.getValue().width) + " " 
					+ Double.toString(Module.HEIGHT) + " " + modEntry.getValue().name + "\n");
			}
			for(Entry<String, Module> modEntry: Module.padList.entrySet()){
				out.write(Double.toString(modEntry.getValue().width) + " " 
					+ Double.toString(Module.HEIGHT) + " " + modEntry.getValue().name + "\n");
			}
			out.write("\n");
			for(Entry<String, Module> modEntry: Module.cellList.entrySet()){
				out.write(Double.toString(modEntry.getValue().xPos) + " " 
					+ Double.toString(modEntry.getValue().yPos) + "\n");
			}
			for(Entry<String, Module> modEntry: Module.padList.entrySet()){
				out.write(Double.toString(modEntry.getValue().xPos) + " " 
					+ Double.toString(modEntry.getValue().yPos) + "\n");
			}
			out.close();
	    }
		catch (Exception e){//Catch exception if any
	      System.err.println("Error: " + e.getMessage());
	    }
	}
}
