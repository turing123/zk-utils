package zkutils.masterslaves;

public class TestService implements Service {
	
	public String getServiceName() {
		return "TestService";
	}

	public void startup() {
		System.out.println("Service started.");
		
	}
	
	public void run() {
		while (true) {
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			System.out.println("I am handling job.");
		}
		
	}
}
