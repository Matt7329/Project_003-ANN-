package train;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.util.Random;

import javax.swing.ImageIcon;
import javax.swing.JFrame;

import net.ArtificialNeuralNetwork;

public class Main {
	
	public static final Dimension ss = Toolkit.getDefaultToolkit().getScreenSize();
	public static final int ssw = (int) ss.getWidth();
	public static final int ssh = (int) ss.getHeight();
	
	private JFrame f;
	private BufferStrategy bs;	
	private ArtificialNeuralNetwork ANN;
	private GraphicsHandler gh;
	private TimeHandler th;
	private Random r = new Random();

	private int MODE = 1;
	private int gap = 2;
	private int scale_x = 28;
	private int scale_y = 28;
	
	private int current_answer; //the correct answer for the current selected image
	private int current_selection; //the selected image from the set

	private float[][][] training_data;//[answer][selection][x + y * width]
	private float[][][] testing_data;//[answer][selection][x + y * width]
	
	private float[] input;
	private float[] output;
	
	private int iterations = 0;
	private int max_iterations = 100000;
	
	private int answer; //the answer given by the neural net
	private int correct_answers = 0;
	private int total_forward_passes = 0;
	
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
		
		load_data();
		
		int input_width = (scale_x - (gap*2)) * (scale_y - (gap*2));
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
	
	private void load_data() {
		training_data = new float[10][][];
		testing_data = new float[10][][];
		
		for(int i = 0; i < 10; i++) {
			BufferedImage current_image = getImage("res/train/images/mnist_train"+i+".jpg");
			int max_x = current_image.getWidth()/scale_x;
			int max_y = current_image.getHeight()/scale_y;	
			training_data[i] = new float[max_x * max_y][(scale_x - (gap * 2)) * (scale_y - (gap * 2))];	
			for (int x = 0; x < max_x; x++) {
				for (int y = 0; y < max_y; y++) {	
					BufferedImage training_image = current_image.getSubimage(x*scale_x, y*scale_y, scale_x, scale_y);		
					for(int x2 = gap; x2 < scale_x-gap; x2++) {
						for(int y2 = gap; y2 < scale_y-gap; y2++) {	
							Color c = new Color(training_image.getRGB(x2, y2));
							int r = c.getRed();
							int g = c.getGreen();
							int b = c.getBlue();
							float tot = (float)(r + g + b) / (765f);
							training_data[i][x + y * max_x][(x2 - gap) + (y2 - gap) * (scale_x - (gap * 2))] = tot;
						}
					}
				}
			}	
			current_image = getImage("res/test/images/mnist_test"+i+".jpg");
			max_x = current_image.getWidth()/scale_x;
			max_y = current_image.getWidth()/scale_y;	
			testing_data[i] = new float[max_x * max_y][(scale_x - (gap * 2)) * (scale_y - (gap * 2))];	
			for (int x = 0; x < max_x; x++) {
				for (int y = 0; y < max_y; y++) {
					BufferedImage testing_image = current_image.getSubimage(x*scale_x, y*scale_y, scale_x, scale_y);
					for(int x2 = gap; x2 < scale_x-gap; x2++) {
						for(int y2 = gap; y2 < scale_y-gap; y2++) {	
							Color c = new Color(testing_image.getRGB(x2, y2));
							int r = c.getRed();
							int g = c.getGreen();
							int b = c.getBlue();
							float tot = (float)(r + g + b) / (765f);
							testing_data[i][x + y * max_x][(x2 - gap) + (y2 - gap) * (scale_x - (gap * 2))] = tot;
						}
					}
				}
			}
		}	
	}
	public void render(Graphics2D g) {
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, ssw/3, ssh/3);
		
		if(output != null) {
			for(int i = 0; i < output.length; i++) {
				g.setColor((answer == i)? ((answer == current_answer)? Color.GREEN : Color.RED) : Color.WHITE);
				g.drawString("["+i+"] "+output[i], 500, 50 + 20*(i+1));
			}
			g.setColor((answer == current_answer)? Color.GREEN : Color.RED);
			g.drawString("Output: "+answer, 500, 50);
		}
		if(input != null)
		for(int x = gap; x < scale_x - gap; x++) {
			for(int y = gap; y < scale_y - gap; y++) {
				int in = (int) (input[(x - gap) + (y - gap) * (scale_x - (gap * 2))] * 255f);
				g.setColor(new Color(in, in, in));
				g.fillRect(x*10 + 150, y*10 + 50, 9, 9);
			}
		}	
		
		g.setColor(Color.WHITE);
		if(total_forward_passes != 0) {
			float accuracy = (float)(correct_answers*100f) / (float)total_forward_passes;
			g.drawString("Accuracy: "+accuracy+"%", 500, 270);
		}else {
			g.drawString("Progress: "+(iterations*100.0)/max_iterations+"%", 500, 270);
		}
		g.drawString("Tickrate: "+th.tickrate+"/s", 500, 285);
	}
	
	public void tick(int tickcount) {
		if(MODE == 0) {
			long start = System.currentTimeMillis();	
			for(iterations = 0; iterations < max_iterations; iterations++) {			
				train();
			}
			ANN.save();
			System.out.println("Time: "+(System.currentTimeMillis() - start) / 1000.0);
		}else {
			test();
		}
	}
	
	public void test() {
		current_answer = r.nextInt(10);
		int max_selection = testing_data[current_answer].length;
		current_selection = r.nextInt(max_selection);
		input = testing_data[current_answer][current_selection];	
		
		output = ANN.forwardPass(input);
		answer = -1;
		float max = 0;
		for(int i = 0; i < output.length; i++) {
			if(output[i] > max) {
				max = output[i];
				answer = i;
			}
		}
		if(answer == current_answer) correct_answers++;
		total_forward_passes++;
	}
	
	public void train() {
		current_answer = r.nextInt(10);
		int max_selection = training_data[current_answer].length;
		current_selection = r.nextInt(max_selection);
		input = training_data[current_answer][current_selection];
		
		output = new float[10];
		output[current_answer] = 1;	
		answer = current_answer;
		ANN.train(input, output);
	}
	
	private BufferedImage getImage(String filename) {
		ImageIcon in = new ImageIcon(filename);
		Image img = in.getImage();
		BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);
	    Graphics2D bGr = bimage.createGraphics();
	    bGr.drawImage(img, 0, 0, null);
	    bGr.dispose();
	    return bimage;
    }

	public static void main(String args[]) {
		new Main();
	}
}
