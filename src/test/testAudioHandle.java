package test;

import FOZReader.AudioHandle;

public class testAudioHandle {

	public static void main(String[] args) throws InterruptedException {
		// TODO Auto-generated method stub
		AudioHandle a = new AudioHandle();
		a.record2();
		Thread.sleep(10000);
		//a.stop();
	}

}
