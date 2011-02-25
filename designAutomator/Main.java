package designAutomator;

public class Main {
	public static void main(String args[]) {
		Circuit ckt = new Circuit();
		Chip chip = new Chip();
		try {
			// The filename of the test-bench
			//final String filename = "input/test1";
			final String netListFile = "input/ibm01.net";
			final String areaListFile = "input/ibm01.are";

			// parsing and printing the netlist statistics for verification
			ckt.parseNetList(netListFile);
			ckt.parseAreaList(areaListFile);
			
			Module.cellKeyList = Module.cellList.keySet().toArray(new String[0]);
			Module.padKeyList = Module.padList.keySet().toArray(new String[0]);
			
			chip.setArea(ckt.getTotalArea());
			chip.placePads();
			chip.placeCellsRandomly();
			chip.dumpChipPlacements("ibm01_orig.bbb");
//			if (chip.circuit.getVertexCount() < 100) {
//				chip.viewNetList();
//			}

			// next run the simulated annealing algorithm
			SimAnneal simAnneal = new SimAnneal(chip, ckt);
			simAnneal.simAnneal();
			chip.dumpChipPlacements("ibm01.bbb");
		} catch (Exception e) {
			System.err.println(e.getCause());
			e.printStackTrace();
		}
	}
}
