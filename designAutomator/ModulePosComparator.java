package designAutomator;

import java.util.Comparator;

public class ModulePosComparator implements Comparator<XPosIndexPair> {
	@Override
	public int compare(XPosIndexPair arg0, XPosIndexPair arg1) {
		return ((int)Math.signum(arg0.xPos - arg1.xPos));
	}	
}
