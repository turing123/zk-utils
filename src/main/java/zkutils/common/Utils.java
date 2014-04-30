package zkutils.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Properties;

public class Utils {
	public static Properties readProperties(String classpath) throws IOException {		
		Properties properties = new Properties();
		FileInputStream fileInputStream = null;
		try {
			fileInputStream = new FileInputStream(classpathToFile(classpath));
			properties.load(fileInputStream);
			return properties;
		} catch (FileNotFoundException e) {
		    throw e;
		} catch (IOException e) {
			throw e;
		} finally {
			if (fileInputStream != null) {
				try {
					fileInputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public static File classpathToFile(String path) {
		URL url = Utils.class.getClassLoader()
				.getResource(path);
		try {
			return new File(url.toURI());
		} catch (URISyntaxException e) {
			throw new IllegalArgumentException("Invalid class path: " + path);
		}
	}

}
