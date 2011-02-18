package designAutomator;

import java.util.HashMap;
import java.util.Map;

public class Module {
	/**
	 * The global list of modules
	 */
	static Map<String, Module> moduleList = new HashMap<String, Module>();
	
	/**
	 * The name of the module. Starts with 'p' if it is a pin, 
	 * else starts with 'a' describing a cell.
	 */
	String name;
	
	/**
	 * The type of module - cell or pad.
	 */
	ModuleType type;
	
	/**
	 * The in pin count
	 */
	int input_pin_count = 0;
	
	/**
	 * The in pin count
	 */
	int output_pin_count = 0;
	
	/**
	 * The height of the module
	 */
	float height = 0;
	
	/**
	 * The width of the module is fixed at 32
	 */
	static final float WIDTH = 32;
	
	static enum ModuleType {
		PAD, CELL
	};
	
	Module(String name) {
		this.name = name;
	}
	
	@Override
	public String toString() {
		return name + ":" +  input_pin_count + output_pin_count;
	}
}
