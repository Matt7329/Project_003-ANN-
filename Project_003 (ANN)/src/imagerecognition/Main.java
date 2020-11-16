package imagerecognition;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.image.BufferStrategy;

import javax.swing.JFrame;

import net.ArtificialNeuralNetwork;

public class Main {
	
	public static final Dimension ss = Toolkit.getDefaultToolkit().getScreenSize();
	public static final int ssw = (int) ss.getWidth();
	public static final int ssh = (int) ss.getHeight();
	
	private JFrame f;
	private BufferStrategy bs;	
	private GraphicsHandler gh;
	private TimeHandler th;
	private InputHandler in;
	
	private int offset_x = 450;
	private int offset_y = 150;
	private int size_x = 30;
	private int size_y = 30;
	private int scale_x = 24;
	private int scale_y = 24;
	private int border = 3;
	
	private float[] input = new float[scale_x * scale_y];
	private float[] output = new float[10];
	
	public ArtificialNeuralNetwork ANN;
	public int answer = 0;
	public boolean train = false;
	
	public Main() {	
		f = new JFrame();
		f.setTitle("Matt");
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setSize(ssw, ssh);
		f.setLocationRelativeTo(null);
		f.setUndecorated(true);
		f.setVisible(true);
		f.createBufferStrategy(3);
		bs = f.getBufferStrategy();
		
		in = new InputHandler(this);
		f.addKeyListener(in);
		f.addMouseListener(in);
		f.addMouseMotionListener(in);
		
		int input_width = scale_x * scale_y;
		int output_width = 10;
		int width = 600;
		int depth = 2;
		
		System.out.println("Inputs: "+input_width);
		System.out.println("Outputs: "+output_width);
		System.out.println("Width: "+width);
		System.out.println("Depth: "+depth);
		
		ANN = new ArtificialNeuralNetwork(input_width, output_width, width, depth);
		ANN.load();
		
		th = new TimeHandler(this);
		th.start();
		gh = new GraphicsHandler(this, bs);
		gh.start();
	}
	
	public void render(Graphics2D g) {
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, ssw, ssh);
		g.setColor(Color.WHITE);
		g.fillRect(offset_x - border, offset_y - border , (border * 2) + scale_x * size_x, (border * 2) + scale_y * size_y);
		g.setColor(Color.BLACK);
		g.fillRect(offset_x, offset_y, scale_x * size_x, scale_y * size_y);
		
		for(int x = 0; x < scale_x; x++) {
			for(int y = 0; y < scale_y; y++) {
				float in = input[x + y * scale_x];
				g.setColor(new Color(in, in, in));
				g.fillRect(offset_x + x * size_x, offset_y + y * size_y, size_x - 3, size_y - 3);
			}
		}	
		
		g.setFont(new Font("Calibri", Font.BOLD, 48)); 
		if(output != null) {
			g.setColor(Color.WHITE);
			for(int i = 0; i < output.length; i++) {
				g.drawString("["+i+"] "+output[i], 1200, 200 + 50 * i);
			}
			g.drawString("Output: "+answer, 1200, 150);
		}
		g.drawString("Mode: "+(train? "Train" : "Test"), 1200, 750);
	}
	
	public void tick(int tickcount) {
		in.tick();
		
		if(in.mousepressed) {
			int mx = in.mousex;
			int my = in.mousey;
			if(mx > offset_x && mx < offset_x + scale_x * size_x) {
				if(my > offset_y && my < offset_y + scale_y * size_y) {
					brush(mx, my);
				}
			}
		}
	}

	public void guess() {
		if(train) {
			train();
		}else {
			test();
		}
	}
	
	private void train() {
		output = new float[10];
		output[answer] = 1;	
		ANN.train(input, output);
		input = new float[scale_x * scale_y];
	}

	private void test() {
		output = ANN.forwardPass(input);
		
		answer = -1;
		float max = 0;
		for(int i = 0; i < output.length; i++) {
			if(output[i] > max) {
				max = output[i];
				answer = i;
			}
		}
		
		input = new float[scale_x * scale_y];
	}

	public void brush(int mx, int my) {
		float x = (float)(mx - offset_x) / (float)size_x; 
		float y = (float)(my - offset_y) / (float)size_y; 
		float f = -((float) in.mousebutton - 2f) * 0.75f;	
		
		float min_x = (float) Math.floor(x);
		float min_y = (float) Math.floor(y);
		float max_x = (float) Math.ceil(x);
		float max_y = (float) Math.ceil(y);
		float c_x = Math.round(x);
		float c_y = Math.round(y);
		
		for(int i = (int) (min_x - (x - c_x)) - scale_x/2; i <= max_x - (x - c_x) + scale_x/2; i++) {
			for(int j = (int) (min_y - (y - c_y)) - scale_y/2; j <= max_y - (y - c_y) + scale_y/2; j++) {
				float d = (float) Math.hypot(c_x - i - 0.5f, c_y - j - 0.5f) * 1.5f;
				changeBrightness(i, j, f / (d * d * d * d));
			}
		}
	}
	
	public void changeBrightness(int x, int y, float dif) {
		if(x < 0 || x >= scale_x) return;
		if(y < 0 || y >= scale_y) return;
		
		float f = input[x + y * scale_x] + dif;	
		if(f < 0) f = 0;
		if(f > 1) f = 1;	
		input[x + y * scale_x] = f;
	}
	
	public static void main(String args[]) {
		new Main();
	}
}
