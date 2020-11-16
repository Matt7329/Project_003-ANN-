package net;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Random;

public class ArtificialNeuralNetwork {
	
	int seed = 0;
	
	private int width;
	private int depth;
	private int input_width;
	private int output_width;
	private int total;
	
	private float[] weights; //[w2 + (width * w) + (width * width * d)] gives weight from the node [w2, d-1] to [w, d]	
	private float[] bias; //[w + d * width] gives bias for the node [w, d]
	private float[] weightsdiff;
	private float[] biasdiff;
	
	private float modifier;
	private float momentum = 0.01f;
	private float learning_rate = 0.01f;
	
	public ArtificialNeuralNetwork(int input_width, int output_width, int width, int depth) {
		this.width = width;
		this.depth = depth;
		this.input_width = input_width;
		this.output_width = output_width;
		
		total = output_width + (width * depth);
		modifier = (float) (1.0 / Math.sqrt(total));
		weights = new float[total * width];
		bias = new float[total];
		weightsdiff = new float[total * width];
		biasdiff = new float[total];
		
		Random r = new Random();
		for(int d = 0; d <= depth; d++) {
			for(int w = 0; w < ((d == depth)? output_width : width); w++) {
				int wi = (d == 0)? input_width : width;
				int node = w + d * width;
				for(int w2 = 0; w2 < wi; w2++) {
					int idx = w2 + node * width;
					weights[idx] = r.nextFloat() * (r.nextBoolean()? modifier : -modifier);
				}		
			}
		}
	}
	
	public float[] forwardPass (float[] input) {	
		float[] output = new float[output_width];
		float[] nodes = new float[total];	
		
		for(int d = 0; d < depth + 1; d++) {
			for(int w = 0; w < ((d == depth)? output_width : width); w++) {
				int wi = (d == 0)? input_width : width;
				int node = w + d * width;		
				for(int w2 = 0; w2 < wi; w2++) {
					int target_node = w2 + (d-1) * width;
					int idx = w2 + node * width;
					nodes[node] += ((d == 0) ? input[w2] : nodes[target_node]) * weights[idx];
				}
				nodes[node] = ActivationFunction(nodes[node] + bias[node], false);
				if(d == depth) output[w] = nodes[node];
			}
		}	
		return output;
	}
	
	public void train(float[] input, float[] ExpectedOutput) {
		float[] output = new float[output_width];
		float[] nodes = new float[total];
		for(int d = 0; d < depth + 1; d++) {
			for(int w = 0; w < ((d == depth)? output_width : width); w++) {
				int wi = (d == 0)? input_width : width;
				int node = w + d * width;		
				for(int w2 = 0; w2 < wi; w2++) {
					int target_node = w2 + (d-1) * width;
					int idx = w2 + node * width;
					nodes[node] += ((d == 0) ? input[w2] : nodes[target_node]) * weights[idx];
				}
				nodes[node] = ActivationFunction(nodes[node] + bias[node], false);
				if(d == depth) output[w] = nodes[node];
			}
		}
		float[] OutputSignalError = new float[output_width];
		float[] SignalError = new float[width * depth];		
		for (int out = 0; out < output_width; out++) {
			OutputSignalError[out] = (ExpectedOutput[out] - output[out]) * ActivationFunction(output[out], true);
		}
		for (int d = depth - 1; d >= 0; d--) {
			for (int w = 0; w < width; w++) {
				int node = w + d * width;
				float Sum = 0;
				for (int w2 = 0; w2 < ((d == depth - 1)? output_width : width); w2++) {
					int target_node = w2 + (d + 1) * width;
					int idx = w + target_node * width;
					Sum += weights[idx] * ((d == depth-1)? OutputSignalError[w2] : SignalError[target_node]);
				}
				SignalError[node] = ActivationFunction(nodes[node], true) * Sum;
			}
		}
		for (int d = depth; d >= 0; d--) {
			for (int w = 0; w < ((d == depth)? output_width : width); w++) {
				int node = w + d * width;
				biasdiff[node] = learning_rate * ((d == depth)? OutputSignalError[w] : SignalError[node]) + (momentum * biasdiff[node]);
				bias[node] += biasdiff[node];
				for (int w2 = 0; w2 < ((d == 0)? input_width : width); w2++) {
					int target_node = w2 + (d - 1) * width;
					int idx = w2 + node * width;
					float err = (d == depth)? OutputSignalError[w] : SignalError[node];
					float val = (d == 0)? input[w2] : nodes[target_node];	
					weightsdiff[idx] = learning_rate * err * val + (momentum * weightsdiff[idx]);
					weights[idx] += weightsdiff[idx];
				}
			}
		}
	}
	
	private float ActivationFunction(float x, boolean derivative) {
		return ReLU(x, derivative);
	}
	
	private float ReLU(float x, boolean derivative) {
		if(derivative)
			return (x > 0)? 1 : 0;
		else
			return (x > 0)? x : 0;
	}
	
	public void save() {
		write(weights, "res/data/weights.txt");
		write(bias, "res/data/bias.txt");
		write(weightsdiff, "res/data/weightsdiff.txt");
		write(biasdiff, "res/data/biasdiff.txt");
	}
	
	public void load() {
		weights = read("res/data/weights.txt");
		bias = read("res/data/bias.txt");
		weightsdiff = read("res/data/weightsdiff.txt");
		biasdiff = read("res/data/biasdiff.txt");
	}
	
	public void write(float[] data, String filepath) {
		try {
			ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(filepath));
			outputStream.writeObject(data);
			outputStream.flush();  
		    outputStream.close();    
			System.out.println("Saved "+data+" to "+filepath);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public float[] read(String filepath) {
		try { 
			ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(filepath));
			float[] out = (float[]) inputStream.readObject();	
			inputStream.close();
			System.out.println("Loaded "+filepath);
			return out;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}
	
}
