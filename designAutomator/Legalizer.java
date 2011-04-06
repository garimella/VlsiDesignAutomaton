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
		
		for (Row row : c.rows) {
			Module sortedModules[] = {};
			row.moduleList.toArray(sortedModules);
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
				if(m.binInRow + m.numBins > Row.totalBinsInRow){
					overflowedModules.add(m);
					totalOverflow+=(m.binInRow + m.numBins - Row.totalBinsInRow);
				}
				curBin += m.numBins;
			}
			row.numFreeBins = row.bins.size() - Config.numExtraBins - curBin;
			row.numBinsUsed = Row.totalBinsInRow-row.numFreeBins;
		}
		return totalOverflow;
	}
	
	public void legalizeAcrossRows() {
		for (Module m : overflowedModules) {
			int rowIndex = (int)m.row.yPos/40;
			for(int i = 1; i <c.rows.size(); i++){
				if(rowIndex + i < c.rows.size()){
					if(c.rows.get(rowIndex).numFreeBins > m.numBins) {
						m.row = c.rows.get(rowIndex);
						m.yPos = m.row.yPos;
						m.xPos = m.row.numBinsUsed*Config.binWidth;
						m.row.numFreeBins -= m.numBins;
						c.rows.get(rowIndex).numFreeBins += m.numBins;
					}
				}
			}
		}
	}
	public void legalize() {
		int totOverflow =  expandModules();
		int avail = 0;
		for (Row row : c.rows){
			avail += row.numFreeBins;
		}
		System.out.println("TotalOverflow = " + totOverflow + 
				"; Available Space = " + avail);
	}
}
