package test;

import java.net.MalformedURLException;

import FOZReader.ScreenShot;

public class testScreenShot {

	public static void main(String[] args) throws InterruptedException, MalformedURLException {
		ScreenShot s = new ScreenShot();
		
		s.setMultiDirectoy("tmp/foz-1481393482646/");
		//s.multiCapture();
		
		//Thread.sleep(10000);
		//s.stopMultiCapture();
		//s.deleteMultiImage();
		
		System.out.println("Making video");
		s.makeMovie("video.mov");
		
		//s.merging("video.mov", "audio.wav", "final.mov");
		
		//s.capture("screenshot.png", "png");
		//System.out.println("Saved");
	}

}
