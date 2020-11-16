package imagerecognition;


public class TimeHandler extends Thread{

	private Main main;
	private int tickcount = 0;
	public float tickrate = 120f;
	
	public TimeHandler(Main main) {
		this.main = main;
	}
	public void run(){
		double unprocessedSeconds = 0;
		long previousTime = System.nanoTime();
		float secondsPerTick = 1f / tickrate;
		
		while (true) {
			secondsPerTick = 1f / tickrate;
			long currentTime = System.nanoTime();
			long passedTime = currentTime - previousTime;
			previousTime = currentTime;
			unprocessedSeconds += passedTime / 1000000000.0;
			while (unprocessedSeconds > secondsPerTick) {
				unprocessedSeconds -= secondsPerTick;
				tickcount++;
				main.tick(tickcount);
			}
		}
	}
}
