package musuGPS.main;

public class UpdateThread implements Runnable {
	private SharedGPSActivity sga;
	private boolean run = true;
	public UpdateThread(SharedGPSActivity sga){
		this.sga = sga;
	}
	public void run() {
		while(run){
			try {
				Thread.sleep(10000);
				sga.updateFeed();
			} catch (InterruptedException e) {
				
			}
			
		}

	}
	public void stop(){
		run = false;
	}
}
