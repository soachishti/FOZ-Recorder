package FOZReader;

import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

public class ScreenShot {
	boolean running;
	BufferedImage image;
	Robot rb;
	Thread tScreenCapture;
	
	public ScreenShot() {
		running = true;
		try {
			rb = new Robot();
		} catch (AWTException e) {
			e.printStackTrace();
		}
	}
	
	public void multiCapture(String tmp, int interval) {
		if (interval == 0)
			interval = 200;
		Thread tScreenCapture = new Thread(){
			public void run() {
				Dimension ds = Toolkit.getDefaultToolkit().getScreenSize(); 				
		    	File tempDir=new File(tmp);  
				if(!tempDir.exists()) 
					tempDir.mkdir();
				try{
					while(running){
						BufferedImage img = rb.createScreenCapture(new Rectangle(ds)); 
						compressImage(img, tempDir); 
						Thread.sleep(200);
					}
				} catch(Exception ie){}
			}
		};
		tScreenCapture.start();
	}
	
	public void stopMultiCapture() {
		running = false;
	}
	
	public void capture() {
		Rectangle size = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
		image = rb.createScreenCapture(size);
	}
	
	public void save(String file, String type) {
		// type = "jpg", "png"
		try {
			ImageIO.write(this.image, type, new File(file));
		} catch (IOException e) {
		
		}
	}
	
	public BufferedImage getImage() {
		return image;
	}
	
	private void compressImage(BufferedImage img, File dir) {
		// Source http://java.worldbestlearningcenter.com/2013/06/screen-recording.html
		
		try {
			// Current image
			ImageWriter imgWriterC =(ImageWriter) ImageIO.getImageWritersByFormatName("jpg").next();
			File fileC = new File(dir.getAbsolutePath()+"/current.jpg");
			ImageOutputStream imgOutStrmC = ImageIO.createImageOutputStream(fileC);
			imgWriterC.setOutput(imgOutStrmC);
			
			// Save Images
			ImageWriter imgWriter =(ImageWriter) ImageIO.getImageWritersByFormatName("jpg").next();
			File file = new File(dir.getAbsolutePath()+"/all/"+System.currentTimeMillis()+".jpg");
			ImageOutputStream imgOutStrm = ImageIO.createImageOutputStream(file);
			imgWriter.setOutput(imgOutStrm);
			
			IIOImage iioImg = new IIOImage(img, null,null);	
			ImageWriteParam jpgWriterParam = imgWriter.getDefaultWriteParam();
			jpgWriterParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
			jpgWriterParam.setCompressionQuality(0.7f);
			
			imgWriter.write(null, iioImg, jpgWriterParam);
			imgWriterC.write(null, iioImg, jpgWriterParam);
			
			imgOutStrm.close();
			imgWriter.dispose();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
