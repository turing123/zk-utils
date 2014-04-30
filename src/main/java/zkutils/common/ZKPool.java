package zkutils.common;

import java.util.ArrayList;
import java.util.List;

public class ZKPool {
	private List<String> connectStringList;
	
	public ZKPool(String[] connectStrings) {
		connectStringList = new ArrayList<String>();
		for (int i=0; i<connectStrings.length; i++) {
			String connectString = connectStrings[i];
			// TODO: check validity of the connect string 		
			if (connectStringList.contains(connectStrings[i])) {
				throw new IllegalArgumentException("");
			}
			connectStringList.add(connectString);
		}
		
		if (connectStringList.size() == 0) {
			throw new IllegalArgumentException("The ZooKeeper pool cannot be empty.");
		}
	}
	
	public List<String> getConnectStrings() {
		return connectStringList;
	}
	
	public String getRandoemConnectString() {
		int index = (int)(Math.random() * (connectStringList.size()));
		
		return connectStringList.get(index);
	}
}
