package designAutomator;

public class Main {
	public static void main(String args[]) {
		long time1= System.currentTimeMillis();
		
		Circuit ckt = new Circuit();
		Chip chip = new Chip();
		try {
			String benchmarkname = args[0];
			//String benchmarkname = "ibm17";
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
			if (ckt.circuit.getVertexCount() < 100) {
				ckt.viewNetList();
			}
			
			System.out.println("initial row width =" + Row.width);
			SimAnneal simAnneal = new SimAnneal(chip, ckt);
			simAnneal.simAnneal();
			chip.dumpChipPlacementsSimple("result/" + benchmarkname + "_simple.bbb");
			
			// instead of simulated annealing, read the values from the dump file
//			chip.readChipPlacements("result/" + benchmarkname + "_simple.bbb");
			
			int totalExtraRowWidth = 0;
			for(Row r: chip.rows) {
				totalExtraRowWidth += r.numFreeAtEnd();
			}
			System.out.println("average row width =" + (Row.width + ((double)totalExtraRowWidth/chip.rows.size())));
			System.out.println("Before legalization net cost = " + simAnneal.initialNetCost());
			
			Legalizer legalizer = new Legalizer(chip, ckt);
			legalizer.legalize();
			
			System.out.println("Final net cost after legalization = " + simAnneal.initialNetCost());
			chip.dumpChipPlacements("result/" + benchmarkname + ".bbb");
		} catch (Exception e) {
			System.err.println(e.getCause());
			e.printStackTrace();
		}
		long time2= System.currentTimeMillis();
		System.out.println("millsecs elapsed:"+(time2-time1));
	}
}
