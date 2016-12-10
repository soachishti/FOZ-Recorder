package test;

import FOZReader.ScreenBroadcast;
import FOZReader.ScreenShot;

public class testScreenBroadcast {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		ScreenShot s = new ScreenShot();
		s.setMultiDirectoy("tmp/");
		s.multiCapture();
		
		ScreenBroadcast sb = new ScreenBroadcast();
		String address = sb.getIPAddress();
		sb.setAddress(address);
		sb.start();
		
		System.out.println(address);
		//sb.stop();
		
	}

}
