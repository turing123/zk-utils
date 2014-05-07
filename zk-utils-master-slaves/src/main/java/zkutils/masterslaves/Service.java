package zkutils.masterslaves;

public interface Service {
	String getServiceName();
	void startup();
	void run();
}
