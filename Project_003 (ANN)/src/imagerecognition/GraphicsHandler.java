package imagerecognition;
import java.awt.Graphics2D;
import java.awt.image.BufferStrategy;

public class GraphicsHandler extends Thread{

	private BufferStrategy bs;
	private Graphics2D g = null;
	private float fps = 0;
	private Main main;
	
	public GraphicsHandler(Main main, BufferStrategy bs) {
		this.bs = bs;
		this.main = main;
	}
	public void run(){
		while (true) {
			long start = System.nanoTime();
			drawGraphics();
			fps = 1e9f/(System.nanoTime() - start);
		}
	}
	private void drawGraphics() {
		g = (Graphics2D) bs.getDrawGraphics();
		main.render(g);
		
		if (g != null) {
			g.dispose();
		}
			
		bs.show();
	}
}
