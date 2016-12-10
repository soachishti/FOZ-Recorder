package FOZReader;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;
import javax.imageio.*;
import javax.imageio.stream.*;
import java.awt.image.*;
import java.util.*;
import javax.media.*;
import javax.media.control.TrackControl;
import javax.media.datasink.*;
import javax.media.format.*;
import javax.media.protocol.*;
import java.net.*;


import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Vector;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import javax.media.Buffer;
import javax.media.DataSink;
import javax.media.Format;
import javax.media.Manager;
import javax.media.MediaLocator;
import javax.media.NoDataSinkException;
import javax.media.NoProcessorException;
import javax.media.Processor;
import javax.media.ProcessorModel;
import javax.media.Time;
import javax.media.control.TrackControl;
import javax.media.format.AudioFormat;
import javax.media.format.VideoFormat;
import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.DataSource;
import javax.media.protocol.FileTypeDescriptor;
import javax.media.protocol.PullBufferDataSource;
import javax.media.protocol.PullBufferStream;
import javax.sound.sampled.AudioFileFormat.Type;

import FOZReader.ScreenRecording.ImageDataSource;
import FOZReader.ScreenRecording.ImageSourceStream;

public class ScreenShot {
	boolean running;
	BufferedImage image;
	Robot rb;
	File tempDir;
	File scrnShot;	
	Thread tScreenCapture;
	int captureInterval;
	
	public ScreenShot() {
		captureInterval = 200;
		running = true;
		try {
			rb = new Robot();
		} catch (AWTException e) {
			e.printStackTrace();
		}
	}
	
	public void setInterval(int interval) {
		captureInterval = interval;
	}
	
	public void setMultiDirectoy(String directory) {
		tempDir = new File(directory);  
		if(!tempDir.exists()) 
			tempDir.mkdir();
		
		scrnShot = new File(directory + "/screenshot/");
		if (!scrnShot.exists()) {
			scrnShot.mkdirs();
		}
		
	}
	
	public void multiCapture() {
		tScreenCapture = new Thread(){
			public void run() {
				Dimension ds = Toolkit.getDefaultToolkit().getScreenSize(); 				
		    	try{
					while(running){
						BufferedImage img = rb.createScreenCapture(new Rectangle(ds)); 
						compressImage(img); 
						Thread.sleep(captureInterval);
					}
				} catch(Exception ie){}
			}
		};
		tScreenCapture.start();
	}
	
	public void stopMultiCapture() {
		running = false;
		try {
			tScreenCapture.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void capture(String file, String type) {
		Rectangle size = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
		image = rb.createScreenCapture(size);
		// type = "jpg", "png"
		try {
			ImageIO.write(this.image, type, new File(file));
		} catch (IOException e) {
			System.err.println(e);
		}
	}
	
	public BufferedImage getImage() {
		return image;
	}
	
	private void compressImage(BufferedImage img) {
		// Source http://java.worldbestlearningcenter.com/2013/06/screen-recording.html
		
		try {
			// Current image
			ImageWriter imgWriterC =(ImageWriter) ImageIO.getImageWritersByFormatName("jpg").next();
			File fileC = new File(tempDir.getAbsolutePath()+"/current.jpg");
			ImageOutputStream imgOutStrmC = ImageIO.createImageOutputStream(fileC);
			imgWriterC.setOutput(imgOutStrmC);
			
			// Save Images
			ImageWriter imgWriter =(ImageWriter) ImageIO.getImageWritersByFormatName("jpg").next();
			File file = new File(scrnShot.getAbsolutePath()+"/"+System.currentTimeMillis()+".jpg");
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
	
	public File[] getMultiImage() {
		File[] listOfFiles = scrnShot.listFiles();
		return listOfFiles;
	}
	
	public void deleteMultiImage() {
		for (File f: getMultiImage()) {
			f.delete();
		}
	}
	
	public void makeMovie(String filename) throws MalformedURLException {
		float frameRate = 2.2f;
		Dimension ds = Toolkit.getDefaultToolkit().getScreenSize(); 				
		int width = ds.width;
		int height = ds.height;
		
		ArrayList imgfiles = new ArrayList();
		for (File f: getMultiImage()) {
			imgfiles.add(f.getAbsolutePath());
		}
		
		String mFileName = filename; //assign the video file to number representing the current time   
        File fout = new File(mFileName); //create file object to point to the video file
        //create media locator object to point to the file object
        MediaLocator oml;
        oml = new MediaLocator(fout.toURI().toURL());
				
        //Initialize state helper and processor objects
        StateHelper statehelper = null;
        Processor processor = null;
        //create datasource object from the image files
        ImageDataSource ids = new ImageDataSource(width, height, frameRate, imgfiles);
        try {
            processor = Manager.createProcessor(ids); //create processor object
        } catch (Exception e) {}
        statehelper = new StateHelper(processor); //create state helper object
        if (!statehelper.configure(10000)) //configure the processor
            System.exit(100);
        //set the video file content type to QUICKTIME
        processor.setContentDescriptor(new ContentDescriptor(FileTypeDescriptor.QUICKTIME));
        //get the supported track formats
        TrackControl tcs[] = processor.getTrackControls();
        Format f[] = tcs[0].getSupportedFormats();
        if (f == null || f.length <= 0) //there is no supported track format
            System.exit(100);

        tcs[0].setFormat(f[0]); //set a supported format to the track control
        //realize the processor
        if (!statehelper.realize(10000))
            System.exit(100);
        //create datasource object from processor so it is ready to write out by the writer
        DataSource source = processor.getDataOutput();
        //initialize the writer
        DataSink filewriter = null;
        //create and open the writer object
        try {
            filewriter = Manager.createDataSink(source, oml);
            filewriter.open();
        } catch (NoDataSinkException e) {
            System.exit(100);
        } catch (IOException e) {
            System.exit(100);
        } catch (SecurityException e) {
            System.exit(100);
        }

        //start the filewriter and processor
        try {
            processor.start();
            filewriter.start();
        } catch (IOException e) {
            System.exit(-1);
        }
        // wait 5 seconds for end of video stream
        statehelper.waitToEndOfMedia(5000);  
        statehelper.close();
        filewriter.close();
    }

	
	public void finalize() {
		deleteMultiImage();
	}
	
    class ImageDataSource extends PullBufferDataSource {

        ImageSourceStream streams[];

        ImageDataSource(int width, int height, float frameRate, ArrayList images) {
            streams = new ImageSourceStream[1];
            streams[0] = new ImageSourceStream(width, height, frameRate, images);
        }

        public void setLocator(MediaLocator source) {}

        public MediaLocator getLocator() {
            return null;
        }


        public String getContentType() {
            return ContentDescriptor.RAW;
        }

        public void connect() {}

        public void disconnect() {}

        public void start() {}

        public void stop() {}

        public PullBufferStream[] getStreams() {
            return streams;
        }


        public Time getDuration() {
            return DURATION_UNKNOWN;
        }

        public Object[] getControls() {
            return new Object[0];
        }

        public Object getControl(String type) {
            return null;
        }
    }

    
    class ImageSourceStream implements PullBufferStream {

        ArrayList images;
        int width, height;
        VideoFormat format;

        int nextImage = 0;
        boolean ended = false;

        public ImageSourceStream(int width, int height, float frameRate, ArrayList images) {
            this.width = width;
            this.height = height;
            this.images = images;

            format = new VideoFormat(VideoFormat.JPEG, new Dimension(width,
                    height), Format.NOT_SPECIFIED, Format.byteArray,
                frameRate);
        }


        public boolean willReadBlock() {
            return false;
        }

        public void read(Buffer buf) throws IOException {

            if (nextImage >= images.size()) { //check whether all images are processed
                buf.setEOM(true);
                buf.setOffset(0);
                buf.setLength(0);
                ended = true;
                return;
            }

            //write the image data to the Buffer
            //the processor will take it one by one
            String imageFile = (String) images.get(nextImage);
            File fnew = new File(imageFile);
            BufferedImage originalImage = ImageIO.read(fnew);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(originalImage, "jpg", baos);
            byte[] imageInByte = baos.toByteArray();
            buf.setOffset(0);
            buf.setLength((int) imageInByte.length);
            buf.setFormat(format);
            buf.setFlags(buf.getFlags() | buf.FLAG_KEY_FRAME);
            buf.setData(imageInByte);
            //move to the next image for processing
            nextImage++;


        }
        public Format getFormat() {
            return format;
        }
        public ContentDescriptor getContentDescriptor() { //raw data
            return (new ContentDescriptor(ContentDescriptor.RAW));
        }

        public long getContentLength() {
            return 0;
        }

        public boolean endOfStream() {
            return true;
        }

        public Object[] getControls() {
            return null;
        }

        public Object getControl(String type) {
            return null;
        }
    }
    
    public boolean merging(String outFileName) {
    	System.out.println(System.getProperty("user.dir") + "/ffmpeg");
    	String[] exeCmd = new String[] {
    		 System.getProperty("user.dir") + "/ffmpeg",
             "-i",
             tempDir + "/audio.wav",
             "-i",
             tempDir + "/video.mov",
             "-acodec",
             "copy",
             "-vcodec",
             "copy",
             outFileName
         };

         ProcessBuilder pb = new ProcessBuilder(exeCmd);
         boolean exeCmdStatus = executeCMD(pb);
         System.out.println(exeCmdStatus);
         return exeCmdStatus;
    } 
    
    private boolean executeCMD(ProcessBuilder pb) {
         pb.redirectErrorStream(true);
         Process p = null;

         try {
             p = pb.start();

         } catch (Exception ex) {
             ex.printStackTrace();
             System.out.println("oops");
             p.destroy();
             return false;
         }
         try {
             p.waitFor();
         } catch (InterruptedException e) {
             e.printStackTrace();
             System.out.println("woopsy");
             p.destroy();
             return false;
         }
         return true;
    }
}
