package test;

import FOZReader.WebcamHandle;

public class testWebcam {

	public static void main(String[] args) {
		WebcamHandle w = new WebcamHandle();
		w.start();
		
		try {
			Thread.sleep(20000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		w.close();
	}

}
