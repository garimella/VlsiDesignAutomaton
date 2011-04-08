package designAutomator;

import java.util.Arrays;
import java.util.Collections;
import java.util.Vector;

public class Legalizer {
	
	Chip c;
	Circuit ckt;
	
	Vector<Module> overflowedModules = new Vector<Module>();
	public Legalizer(Chip c, Circuit ckt) {
		this.c = c;
		this.ckt = ckt;
	}
	
	public int expandModules() {
		int totalOverflow = 0;
		
		overflowedModules.clear();
		for (Row row : c.rows) {
			Vector<XPosIndexPair> xPosIndexList = new Vector<XPosIndexPair>();
			for(int i = 0; i < row.moduleList.size(); i++){
				xPosIndexList.add(new XPosIndexPair(i, row.moduleList.get(i).xPos));
			}
			Collections.sort(xPosIndexList, new ModulePosComparator());
			int curBin = 0;
			for (XPosIndexPair x1 : xPosIndexList) {			
				Module m = row.moduleList.get(x1.index);
				m.binInRow = curBin;
				m.xPos = curBin * Config.binWidth;
				if(m.binInRow + m.numBins > (int)(0.9f * Row.totalBinsInRow)){
					overflowedModules.add(m);
					totalOverflow+=(m.binInRow + m.numBins - (int)(0.9f * Row.totalBinsInRow));
				}
				curBin += m.numBins;
			}
			row.numFreeBins = (int)(0.9f * row.bins.size()) - Config.numExtraBins - curBin;
			row.numBinsUsed = (int)(0.9f * Row.totalBinsInRow) -row.numFreeBins;
		}
		System.out.println(" The size of the overflowed modules =" + 
				overflowedModules.size());
		return totalOverflow;
	}
	
	public void legalizeAcrossRows() {
		for (Module m : overflowedModules) {
			int rowIndex = (int)m.row.yPos/40;
			mainFor:
			for(int i = 1; i <c.rows.size(); i++){
				if(rowIndex + i < c.rows.size()){
					if(c.rows.get(rowIndex + i).numFreeBins > m.numBins) {
						m.row = c.rows.get(rowIndex+i);
						m.yPos = m.row.yPos;
						m.xPos = m.row.numBinsUsed*Config.binWidth;
						m.row.numBinsUsed += m.numBins;
						m.row.numFreeBins -= m.numBins;
						c.rows.get(rowIndex).numFreeBins += m.numBins;
						m.row.moduleList.add(m);
						c.rows.get(rowIndex).moduleList.remove(m);
						break mainFor;
					}
				} else if(rowIndex - i -1 >= 0){
					if(c.rows.get(rowIndex - i -1).numFreeBins > m.numBins) {
						m.row = c.rows.get(rowIndex - i -1);
						m.yPos = m.row.yPos;
						m.xPos = m.row.numBinsUsed*Config.binWidth;
						m.row.numBinsUsed += m.numBins;
						m.row.numFreeBins -= m.numBins;
						c.rows.get(rowIndex).numFreeBins += m.numBins;
						m.row.moduleList.add(m);
						c.rows.get(rowIndex).moduleList.remove(m);
						break mainFor;
					}
				}
				else {
					System.out.println("Something wrong!");
					break;
				}
			}
		}
	}
	public void legalize() {
		int totOverflow =  expandModules();
		System.out.println("TotalOverflow = " + totOverflow);
		if(totOverflow > 0){
			legalizeAcrossRows();
		}
		totOverflow = expandModules();
		int avail = 0;
		for (Row row : c.rows){
			avail += row.numFreeBins;
		}
		System.out.println("TotalOverflow = " + totOverflow + 
				"; Available Space = " + avail);
	}
}
