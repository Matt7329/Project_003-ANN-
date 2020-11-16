package imagerecognition;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

public class InputHandler implements KeyListener, MouseMotionListener, MouseListener {

	public boolean[] key = new boolean[68836];
	public int mousex = 0;
	public int mousey = 0;
	public int mousebutton = 0;
	public boolean mousepressed = false;
	
	private Main main;

	public InputHandler(Main main) {
		this.main = main;
	}

	public void mouseClicked(MouseEvent e) {
		
	}

	public void mouseEntered(MouseEvent e) {
		
	}

	public void mouseExited(MouseEvent e) {
		
	}

	public void mousePressed(MouseEvent e) {
		mousex = e.getX();
		mousey = e.getY();	
		mousebutton  = e.getButton();
		mousepressed = true;
	}

	public void mouseReleased(MouseEvent e) {
		mousex = e.getX();
		mousey = e.getY();
		mousebutton = e.getButton();
		mousepressed = false;
		
		main.guess();
	}

	public void mouseDragged(MouseEvent e) {
		mousex = e.getX();
		mousey = e.getY();
	}

	public void mouseMoved(MouseEvent e) {
		mousex = e.getX();
		mousey = e.getY();
	}

	public void keyPressed(KeyEvent e) {
		int keyCode = e.getKeyCode();

		if (keyCode > 0 && keyCode < key.length) {
			key[keyCode] = true;
		}
		
		if (key[KeyEvent.VK_F2]) {
			main.train ^= true;
		}
		if (key[KeyEvent.VK_F7]) {
			main.ANN.load();
		}
		if (key[KeyEvent.VK_F8]) {
			main.ANN.save();
		}
	}

	public void keyReleased(KeyEvent e) {
		int keyCode = e.getKeyCode();

		if (keyCode > 0 && keyCode < key.length) {
			key[keyCode] = false;
		}
	}

	public void keyTyped(KeyEvent e) {
		
	}

	public void tick() {
		if (key[KeyEvent.VK_F1]) {
			System.exit(0);
		}
		if (key[KeyEvent.VK_0]) {
			main.answer = 0;
		}
		if (key[KeyEvent.VK_1]) {
			main.answer = 1;
		}
		if (key[KeyEvent.VK_2]) {
			main.answer = 2;
		}
		if (key[KeyEvent.VK_3]) {
			main.answer = 3;
		}
		if (key[KeyEvent.VK_4]) {
			main.answer = 4;
		}
		if (key[KeyEvent.VK_5]) {
			main.answer = 5;
		}
		if (key[KeyEvent.VK_6]) {
			main.answer = 6;
		}
		if (key[KeyEvent.VK_7]) {
			main.answer = 7;
		}
		if (key[KeyEvent.VK_8]) {
			main.answer = 8;
		}
		if (key[KeyEvent.VK_9]) {
			main.answer = 9;
		}
	}
}
