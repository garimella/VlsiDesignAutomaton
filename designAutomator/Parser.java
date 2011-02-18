package designAutomator;

import java.awt.Dimension;
import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.JFrame;

import designAutomator.Module.ModuleType;

import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.algorithms.layout.HypergraphLayout;
import edu.uci.ics.jung.graph.*;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.visualization.*;
import edu.uci.ics.jung.visualization.renderers.*;


public class Parser {
	Hypergraph<String, PinnedWeightedHyperedge> netList;
	
	Parser() {
		netList = new SetHypergraph<String, PinnedWeightedHyperedge>();
	}
	
	void parseNetList(String filename) {
		try {
			File file = new File(filename);
			Scanner scanner = new Scanner(file);
			
			// The first line is '0'
			scanner.nextLong();
			// total_pins 
			scanner.nextLong();
			// total nets 
			scanner.nextLong();
			// total modules 
			scanner.nextLong();
			// pad offset
			scanner.nextLong();
			
			Module sourceNode = null;
			Collection<String> destNodes = new LinkedList<String>(); 
			Map<String, Integer> node_to_pin_map = new HashMap<String, Integer>();
			
			while (scanner.hasNextLine()) {
				String s = scanner.nextLine();
				if (s.isEmpty())
					continue;
				
				StringTokenizer st = new StringTokenizer(s);
				String node = st.nextToken();
				Module m = Module.moduleList.get(node);
				if (m == null) {
					m = new Module(node);
					if (node.toCharArray()[0] == 'p') {
						m.type = ModuleType.PAD;
					} else {
						m.type = ModuleType.CELL;
					}
					Module.moduleList.put(node, m);
					netList.addVertex(m.name);
				}
				
				if (st.countTokens() > 1) {
					if (sourceNode != null) {
						PinnedWeightedHyperedge e = new PinnedWeightedHyperedge(node_to_pin_map);
						netList.addEdge(e, destNodes, EdgeType.UNDIRECTED);
						destNodes.clear();
						node_to_pin_map.clear();
					}
					sourceNode = m;
					sourceNode.output_pin_count++;
					node_to_pin_map.put(m.name, m.output_pin_count);
					destNodes.add(m.name);
				} else if (st.countTokens() == 1){
					m.input_pin_count++;
					destNodes.add(m.name);
					node_to_pin_map.put(m.name, m.input_pin_count);
				} else {
						System.out.println("Something wrong! - line:" + s);
						throw new Exception();
				}
			}
			if (sourceNode != null) {
				PinnedWeightedHyperedge e = new PinnedWeightedHyperedge(node_to_pin_map);
				netList.addEdge(e, destNodes, EdgeType.UNDIRECTED);
			} else {
				System.out.println("Something wrong!");
				throw new Exception();
			}
		}
		catch (Exception e) {
			System.err.println(e.getCause());
			e.printStackTrace();
		}
	}
	
	void parseAreaList(String filename) {
		try {
			// open the file
			File file = new File(filename);
			Scanner scanner = new Scanner(file);
			
			// parse it and update the module data structures
			while (scanner.hasNextLine()) {
				String s = scanner.nextLine();
				if (s.isEmpty())
					continue;
				
				StringTokenizer st = new StringTokenizer(s);
				String node = st.nextToken();
				Module m = Module.moduleList.get(node);
				if (m == null) {
					System.out.println("Module in .are file, " +
							"not present in the original .net file");
					throw new Exception("module not found");
				}
				int area = new Integer(st.nextToken());
				m.height = ((area > 8192) ? 8192 : area)/Module.WIDTH;
			}
		} catch (Exception e) {
			System.err.println(e.getCause());
			e.printStackTrace();
		}
	}
	
	void printNetListStatistics() {
		System.out.println("\n== Net List Stats (No of vertices, etc.) ==\n");
		System.out.println("The number of vertices = " + netList.getVertexCount());	
		System.out.println("The number of edges = " + netList.getEdgeCount());	
	}
	
	void printPinStatistics() {
			System.out.println("\n== Pin Stats ==\n");
			Vector<Integer> module_stats = new Vector<Integer>();
			module_stats.setSize(100);
			
			for (int i = 0; i < 100; i++) {
				module_stats.set(i, new Integer(0));
			}
			
			for(String mname : Module.moduleList.keySet()) {
				int index = Module.moduleList.get(mname).input_pin_count
								+ Module.moduleList.get(mname).output_pin_count;
				int element = 0;
				try {
					element = module_stats.get(index);
				} catch (ArrayIndexOutOfBoundsException e) {
					// Outch! caught an exception.. recover!
					// System.out.println("Outch! caught an exception.. recovering!\n");
					
					// Increment the array size by another 100
					int old_size = module_stats.size();
					module_stats.setSize(index + 101);
					for (int i = old_size; i < (index + 101); i++) {
						module_stats.set(i, new Integer(0));
					}
					
					// Re-execute the instruction
					element = module_stats.get(index);
				}
				module_stats.set(index, element+1);
			}
			
			Vector<Integer> net_stats = new Vector<Integer>();
			net_stats.setSize(100);
			
			for (int i = 0; i < 100; i++) {
				net_stats.set(i, new Integer(0));
			}
			
			for(PinnedWeightedHyperedge e : netList.getEdges()) {
				int index = e.node_to_pin_map.size();
				int element = 0;
				try {
					element = net_stats.get(index);
				} catch (ArrayIndexOutOfBoundsException exp) {
					// Outch! caught an exception.. recover!
					// System.out.println("Ouch! caught an exception.. recovering!\n index = " + index);
					
					// Increment the array size by another 100
					int old_size = net_stats.size();
					net_stats.setSize(index + 101);
					for (int i = old_size; i < (index + 101); i++) {
						net_stats.set(i, new Integer(0));
					}
					
					// Re-execute the instruction
					element = net_stats.get(index);
				}
				net_stats.set(index, element+1);
			}
			
			int max = (net_stats.size() > module_stats.size()) ? net_stats.size() : module_stats.size();
			System.out.println("#pcnt #nets #modules");
			for (int i = 0; i < max; i ++) {
				int ns = (i < net_stats.size()) ? net_stats.get(i) : 0;
				int ms = (i < module_stats.size()) ? module_stats.get(i) : 0;
				if ((ns + ms) != 0)
					System.out.println(i + "\t" + ns + "\t" + ms);
			}
	}
	
	/*
	 * Works only for fewer than 100 vertices
	 */
	void viewNetList() {
		// create hypergraph layout
		HypergraphLayout<String, PinnedWeightedHyperedge> l 
				= new HypergraphLayout<String, PinnedWeightedHyperedge>(netList, FRLayout.class);		
		
		// create visualization viewer
		VisualizationViewer<String, PinnedWeightedHyperedge> v 
				= new VisualizationViewer<String, PinnedWeightedHyperedge>(l, new Dimension(600, 600));
		
		// replace standard renderer with hypergraph renderer
		v.setRenderer(new BasicHypergraphRenderer<String, PinnedWeightedHyperedge>());
		
		// create JFrame and add visualization viewer to content pane
		JFrame frame = new JFrame("NetList");
		frame.setSize(new Dimension(700, 700));
		frame.getContentPane().add(v);		
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);		
		frame.setVisible(true);	
	}
}
