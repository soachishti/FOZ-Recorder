package test;

import FOZReader.ScreenShot;

public class testScreenShot {

	public static void main(String[] args) throws InterruptedException {
		ScreenShot s = new ScreenShot();
		
		s.multiCapture("tmp/", 200);
		
		Thread.sleep(10000);
		s.stopMultiCapture();
		
		//s.capture();
		//s.save("screenshot.png", "png");
		//System.out.println("Saved");
	}

}
