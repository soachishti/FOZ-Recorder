// http://stackoverflow.com/questions/16046824/making-a-java-swing-frame-movable-and-setundecorated


package FOZReader;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;
import com.github.sarxos.webcam.WebcamResolution;

public class WebcamHandle {
	Webcam webcam;
	JFrame window;
	WebcamPanel panel;
	Dimension d;
	
	public WebcamHandle() {
		d = new Dimension(176, 144); 
	}
	
	public void size(String type) {
		// [176x144] [320x240] [640x480]
		
		System.out.println(type);
		
		if (type.equals("small"))
			d = new Dimension(176, 144);
		else if (type.equals("medium"))
			d = new Dimension(320, 240);
		else if (type.equals("large"))
			d = new Dimension(640, 480);
	}
	
	public void start() {
		
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException e1) {
			e1.printStackTrace();
		}
	
		webcam = Webcam.getDefault();	
		webcam.setViewSize(d);
		
		try {
		panel = new WebcamPanel(webcam);
		}
		catch(com.github.sarxos.webcam.WebcamLockException e) {
			JOptionPane.showMessageDialog(null, "Webcam already in use.");
		}
		
		panel.setFPSDisplayed(false);
		panel.setDisplayDebugInfo(false);
		panel.setImageSizeDisplayed(false);
		panel.setMirrored(false);

		window = new JFrame("FOZ Webcam");
		window.setType(javax.swing.JFrame.Type.UTILITY);
		window.setAlwaysOnTop(true);
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice defaultScreen = ge.getDefaultScreenDevice();
        Rectangle rect = defaultScreen.getDefaultConfiguration().getBounds();
        int x = (int) ((int) rect.getMaxX() - d.getWidth()) - 10;
        int y = (int) ((int) rect.getMaxY() - d.getHeight()) - 30;
        window.setLocation(x, y);
		
        // remove title bar
        window.setUndecorated(true);
		
        FrameDragListener frameDragListener = new FrameDragListener(window);
        window.addMouseListener(frameDragListener);
        window.addMouseMotionListener(frameDragListener);
		
		window.add(panel);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.pack();
		window.setVisible(true);
		window.setResizable(false);

	}
	
	public void stopWebcam() {
		webcam.close();
	}
	
	public void close() {
		webcam.close();
		window.dispose();
	}
	
	public void hide() {
		window.setVisible(false);
	}
	
	public void show() {
		window.setVisible(true);
	}
	
	public static class FrameDragListener extends MouseAdapter {
        private final JFrame frame;
        private Point mouseDownCompCoords = null;

        public FrameDragListener(JFrame frame) {
            this.frame = frame;
        }

        public void mouseReleased(MouseEvent e) {
            mouseDownCompCoords = null;
        }

        public void mousePressed(MouseEvent e) {
            mouseDownCompCoords = e.getPoint();
        }

        public void mouseDragged(MouseEvent e) {
            Point currCoords = e.getLocationOnScreen();
            frame.setLocation(currCoords.x - mouseDownCompCoords.x, currCoords.y - mouseDownCompCoords.y);
        }
  	}
}
