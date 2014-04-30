package zkutils.backup;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;

import zkutils.common.ZKPool;

public class PointServiceManager {
	private Properties config;
	private ZKPool zkPool;
	private PointService pointService;
	private ZooKeeper zk;
	private String pointServicePath;
	private final ReentrantLock stateLock = new ReentrantLock();
	private boolean isMaster;
	
	public PointServiceManager(Properties config) {
		this.config = config;
	}
	
	public void init() throws IOException, KeeperException, InterruptedException {
		initZKPool();
		initService();
		if (pointService == null) {
			return;
		}
		
		initZK();
		runService();
	}
	
	private void initZKPool() {
		String zkPoolConfig = this.config.getProperty("zkPool");
		String[] connectStrings = zkPoolConfig.split(",");
		if (connectStrings == null || connectStrings.length == 0) {
			throw new IllegalArgumentException("ZK pool is not configured.");
		}
		
		zkPool = new ZKPool(connectStrings);
	}

	private void initService() {
		ClassLoader loader = this.getClass().getClassLoader();
		String serviceClassPath = this.config.getProperty("serviceClassPath");
		
		
		String serviceClass = this.config.getProperty("serviceClass");
		try {
			if (serviceClassPath != null && !serviceClassPath.equals("")) {
				URL url = new File(serviceClassPath).toURI().toURL();
				loader = new URLClassLoader(new URL[]{url}, loader);
			}
			
			Class<?> serviceClazz = Class.forName(serviceClass, true, loader);
			Constructor<?> constructor = serviceClazz.getConstructor();
			pointService = (PointService) constructor.newInstance();
			pointService.startup();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return;
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void initZK() throws IOException, KeeperException, InterruptedException {
		zk = new ZooKeeper(zkPool.getRandoemConnectString(),
				2000, null);
		String serviceDirPath = "/" + pointService.getServiceName();
		if (zk.exists(serviceDirPath, null) == null) {
			zk.create(serviceDirPath, "".getBytes(), Ids.OPEN_ACL_UNSAFE,
					CreateMode.PERSISTENT);
		} 
		
		String pointServicePathPrefix = serviceDirPath + "/" + pointService.getServiceName();
		pointServicePath = zk.create(pointServicePathPrefix, "".getBytes(), Ids.OPEN_ACL_UNSAFE,
				CreateMode.EPHEMERAL_SEQUENTIAL);
		System.out.println("My path: " + pointServicePath);	
	}
	
	private void runService() throws KeeperException, InterruptedException {
		String serviceDirPath = "/" + pointService.getServiceName();
		String pointServicePathPrefix = serviceDirPath + "/" + pointService.getServiceName();
		
		long mySequenceNumber = getSequenceNumber(pointServicePath, pointServicePathPrefix);		
		
		List<String> childrenPaths = null;
		childrenPaths = zk.getChildren(serviceDirPath, false);
		
		long[] sortedChildrenSequenceNumbers = getSortedSequenceNumbers(childrenPaths, pointService.getServiceName());
		if (mySequenceNumber == sortedChildrenSequenceNumbers[0]) {
			runAsMaster();
		} else {
			long sequenceNumberToWatch = 0;
			for (int i=0; i<sortedChildrenSequenceNumbers.length; i++) {
				if (sortedChildrenSequenceNumbers[i] == mySequenceNumber) {
					sequenceNumberToWatch = sortedChildrenSequenceNumbers[i-1];
					break;
				}
			}
			
			String pathToWatch = "";
			for (String path : childrenPaths) {
				if (getSequenceNumber(path, pointService.getServiceName()) == sequenceNumberToWatch) {
					pathToWatch = path;
					break;
				}
			}
			watchAsSlave(serviceDirPath + "/" + pathToWatch);
		}
	}
	
	private void runAsMaster() {
		System.out.println("Running as master.");
		isMaster = true;
		pointService.run();
	}
	
	private void watchAsSlave(final String pathToWatch) throws KeeperException, InterruptedException {
		System.out.println("Running as slave, and watching: " + pathToWatch);
		isMaster = false;
		
		zk.getData(pathToWatch, new Watcher() {
			public void process(WatchedEvent event) {
				if (  event.getPath().equals(pathToWatch) 
				   && event.getType().equals(Watcher.Event.EventType.NodeDeleted)) {					
					stateLock.lock();
					isMaster = true;
					stateLock.unlock();
				}
			}
		}, null);	
		
		while(true) {
			if (stateLock.tryLock()) {
				if (isMaster) {					
					stateLock.unlock();
					runService();
					break;
				}
				else {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					} finally {
						stateLock.unlock();
					}
				}
			}
		}
	}
	
	private long getSequenceNumber(String path, String prefix) {
		return Long.parseLong(path.substring(prefix.length()));
	}
	
	private long[] getSortedSequenceNumbers(List<String> paths, String prefix) {
		long[] sequenceNumbers = new long[paths.size()];
		for (int i=0; i<paths.size(); i++) {
			sequenceNumbers[i] = getSequenceNumber(paths.get(i), prefix);
		}
		Arrays.sort(sequenceNumbers);
		return sequenceNumbers;
	}
}
