package zkutils.backup;

public interface PointService {
	String getServiceName();
	void startup();
	void run();
}
