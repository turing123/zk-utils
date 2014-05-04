package zkutils.masterslaves;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.apache.zookeeper.KeeperException;

public class PointServiceRunner {

	public static void main(String[] args) {
		String configPath = "";
		if (args.length >= 1) {
			configPath = args[0];
		} else {
			// TODO: read configuration from the default 
		}
		
		Properties config = new Properties();
		try {
			config.load(new FileInputStream(new File(configPath)));
			PointServiceManager manager = new PointServiceManager(config);
			manager.init();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		} catch (KeeperException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}

}
