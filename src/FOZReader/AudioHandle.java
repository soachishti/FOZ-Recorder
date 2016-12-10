package FOZReader;

import javax.sound.sampled.*;
import java.io.*;

public class AudioHandle {

	File wavFile;
    AudioFileFormat.Type fileType = AudioFileFormat.Type.WAVE;
    TargetDataLine line;
	
    
    public void setFilename(String absoluteFilename) {
    	wavFile = new File(absoluteFilename);
    }
    
	AudioFormat getAudioFormat() {
		float sampleRate = 64000;
		int sampleSizeInBits = 8;
		int channels = 2;
		boolean signed = true;
		boolean bigEndian = true;
		AudioFormat format = new AudioFormat(sampleRate, sampleSizeInBits,
	                                             channels, signed, bigEndian);
	    return format;
	}

	public void record() {
		new Thread() {
			public void run() {
				AudioFormat format = getAudioFormat();
		        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
		
		        // checks if system supports the data line
		        if (!AudioSystem.isLineSupported(info)) {
		            System.out.println("Line not supported");
		            System.exit(0);
		        }
		        try {
					line = (TargetDataLine) AudioSystem.getLine(info);
					line.open(format);        
			        line.start();   // start capturing
			
			        AudioInputStream ais = new AudioInputStream(line);
			
			        System.out.println("Start recording...");
			
			        // start recording
		        	AudioSystem.write(ais, fileType, wavFile);
		        	System.out.println("Recording stopped...");
		        	
		        } catch (LineUnavailableException | IOException e) {
					e.printStackTrace();
				}
			}
		}.start();
	}
	
	public void stop() {
		line.stop();
		line.close();
	}
}
