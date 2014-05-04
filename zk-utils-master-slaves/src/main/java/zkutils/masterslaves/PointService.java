package zkutils.masterslaves;

public interface PointService {
	String getServiceName();
	void startup();
	void run();
}
