package designAutomator;

import java.util.HashMap;
import java.util.Map;

public class PinnedWeightedHyperedge {
	
	int weight = 0; // Default Weight
	
	Map<String, Integer> node_to_pin_map;
	
	// Constructor
	public PinnedWeightedHyperedge(Map<String, Integer> node_to_pin_map) {
		this.node_to_pin_map = new HashMap<String, Integer> (node_to_pin_map);
	}
}
