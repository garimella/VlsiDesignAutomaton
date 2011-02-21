package designAutomator;

public class Main {
	public static void main(String args[]) {
		Circuit ckt = new Circuit();
		Chip chip = new Chip();
		try {
			// The filename of the test-bench
//			final String filename = "input/test1";
			final String netListFile = "input/ibm01.net";
			final String areaListFile = "input/ibm01.are";

			// parsing and printing the netlist statistics for verification
			
			ckt.parseNetList(netListFile);
			ckt.parseAreaList(areaListFile);
			chip.setArea(ckt.getTotalArea());
			
			/*if (p.circuit.getVertexCount() < 100) {
				p.viewNetList();
			}*/

			// next run the simulated annealing algorithm
			// YALLGOOOO
		} catch (Exception e) {
			System.err.println(e.getCause());
			e.printStackTrace();
		}
	}
}
