package designAutomator;

public class Main {
	public static void main(String args[]) {
		Circuit ckt = new Circuit();
		Chip chip = new Chip();
		try {
			String benchmarkname = "ibm01";
			// The filename of the test-bench
			//final String filename = "input/test1";
			final String netListFile = "input/" + benchmarkname + ".net";
			final String areaListFile = "input/" + benchmarkname + ".are";

			// parsing and printing the netlist statistics for verification
			ckt.parseNetList(netListFile);
			ckt.parseAreaList(areaListFile);
			
			Module.cellKeyList = Module.cellList.keySet().toArray(new String[0]);
			Module.padKeyList = Module.padList.keySet().toArray(new String[0]);
			
			chip.setArea(ckt.getTotalArea());
			chip.placePads();
			chip.placeCellsRandomly();
			chip.dumpChipPlacements("result/" + benchmarkname + "_orig.bbb");
//			if (chip.circuit.getVertexCount() < 100) {
//				chip.viewNetList();
//			}
			System.out.println("initial row width =" + Row.width);
			// next run the simulated annealing algorithm
			SimAnneal simAnneal = new SimAnneal(chip, ckt);
			
			simAnneal.simAnneal();
			int totalExtraRowWidth = 0;
			for(Row r: chip.rows) {
				totalExtraRowWidth += r.numFreeAtEnd();
			}
			System.out.println("average row width =" + (Row.width + ((double)totalExtraRowWidth/chip.rows.size())));
			
			chip.dumpChipPlacements("result/" + benchmarkname + ".bbb");
		} catch (Exception e) {
			System.err.println(e.getCause());
			e.printStackTrace();
		}
	}
}
