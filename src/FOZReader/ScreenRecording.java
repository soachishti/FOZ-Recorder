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

class ScreenRecording extends JFrame implements ActionListener {
    JButton btCap;
    Robot rb;
    boolean started;
    StateHelper sh = null;
    static String mFileName;
    static String aFileName;
    ScreenRecording() throws AWTException {
        setTitle("Screen Recorder");
        setIconImage(getToolkit().getImage("icon.png"));
        setPreferredSize(new Dimension(300, 60));

        btCap = new JButton("Start Recording");
        btCap.addActionListener(this);
        btCap.setBackground(Color.BLACK);
        btCap.setForeground(Color.LIGHT_GRAY);
        add(btCap, BorderLayout.CENTER);

        setResizable(false);
        setVisible(true);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        pack();
        rb = new Robot();

    }


    public void actionPerformed(ActionEvent e) {
        try {
            if (btCap.getText().equals("Start Recording")) {
                setMess(btCap, "Stop Recording");
                setState(ICONIFIED);
                startRecording();
            } else if (btCap.getText().equals("Stop Recording")) {
                stopRecording();
                VideoGenerator vg = new VideoGenerator();
                vg.start();

            }

        } catch (Exception ee) {}



    }

    public void startRecording() {
        //this thread is for recording audio file
        Thread tAudio = new Thread() {
            public void run() {

                Processor p = null; //initialize the processor
                //find device that support the specified audio format
                CaptureDeviceManager cdm = new CaptureDeviceManager();
                AudioFormat af = new AudioFormat("linear", 44100, 16, 2);
                Vector cds = cdm.getDeviceList(af);
                CaptureDeviceInfo cdi = null;
                if (cds.size() > 0) { //the device is found
                    cdi = (CaptureDeviceInfo) cds.firstElement();
                } else //no supported device
                    System.exit(-1);
                try {
                    p = Manager.createProcessor(cdi.getLocator()); //create the processor for the device
                    sh = new StateHelper(p); //create Statehelper object
                } catch (IOException e) {
                    System.exit(-1);
                } catch (NoProcessorException e) {
                    System.exit(-1);
                }
                // Configure the processor
                if (!sh.configure(10000))
                    System.exit(-1);
                // Set the output content type and realize the processor
                p.setContentDescriptor(new FileTypeDescriptor(FileTypeDescriptor.WAVE));

                // Get the track control objects
                TrackControl track[] = p.getTrackControls();
                boolean encodingPossible = false;
                //search for a possible format and set it to the audio track for encoding
                for (int i = 0; i < track.length; i++) {
                    try {
                        track[i].setFormat(new AudioFormat(AudioFormat.IMA4_MS));
                        encodingPossible = true;
                    } catch (Exception e) {
                        // cannot convert to ima4
                        track[i].setEnabled(false);
                    }
                }
                if (!encodingPossible) {
                    sh.close();
                    System.exit(-1);
                }
                ////

                if (!sh.realize(10000))
                    System.exit(-1);
                //get the output of the processor
                DataSource source = p.getDataOutput();
                //create MediaLocator object to point to the output file locaiton
                aFileName = System.currentTimeMillis() + ".wav";
                File fout = new File(aFileName);
                MediaLocator oml = null;
                try {
                    oml = new MediaLocator(fout.toURI().toURL());
                } catch (MalformedURLException e) {}
                // create a datasink and open it so writing data is possible
                DataSink filewriter = null;
                try {
                    filewriter = Manager.createDataSink(source, oml);
                    filewriter.open();
                } catch (NoDataSinkException e) {
                    System.exit(-1);
                } catch (IOException e) {
                    System.exit(-1);
                } catch (SecurityException e) {
                    System.exit(-1);
                }
                //start the filewriter and processor
                try {
                    p.start();
                    filewriter.start();
                } catch (IOException e) {
                    System.exit(-1);
                }

                //wait for end of media stream
                //in every 5 seconds there is a check to see whether the end of stream is met
                //the recording stops when the user clicks Stop Recording
                sh.waitToEndOfMedia(5000);
                sh.close(); //close the processor
                filewriter.close(); //close the writer 

            }
        };
        //This thread is for capturing screen
        Thread tScreenCapture = new Thread() {
            Dimension ds = getToolkit().getScreenSize(); //get the screen dimension-width and height
            public void run() {
                started = true; //set started variable to true
                File tempDir = new File("tempDir"); //create a file object to point to the tempDir folder  
                if (!tempDir.exists()) //create the folder if it deos not exist
                    tempDir.mkdir();
                try {
                    while (started) {
                        BufferedImage bi = rb.createScreenCapture(new Rectangle(ds)); //capture the screen
                        makeCompression(bi, tempDir); //compress the images so the every size of the image is smaller
                        Thread.sleep(200); //capture interval
                    }

                } catch (Exception ie) {}
            }
        };

        tAudio.start(); //start sound recording
        tScreenCapture.start(); //start screen capture


    }
    public void stopRecording() { //stop recording
        started = false;
        sh.turnOffRecording();
    }

    public void setMess(JButton btCap, String mess) { //set text of the button
            btCap.setText(mess);

        }
        //
    public void makeCompression(BufferedImage bi, File tempDir) throws Exception {

            //Create image writer object
            ImageWriter imgWriter = (ImageWriter) ImageIO.getImageWritersByFormatName("jpg").next();

            //Create image output stream object from the image file
            ImageOutputStream imgOutStrm = ImageIO.createImageOutputStream(new File(tempDir.getAbsolutePath() + "/" + System.currentTimeMillis() + ".jpg"));

            //Set the image output stream object to image writer
            imgWriter.setOutput(imgOutStrm);

            //wrap the captured image data in IIOImage object
            IIOImage iioImg = new IIOImage(bi, null, null);

            //Create parameter for image writer
            ImageWriteParam jpgWriterParam = imgWriter.getDefaultWriteParam();
            //Set compresson mode
            jpgWriterParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            //Set compression quality
            jpgWriterParam.setCompressionQuality(0.7 f);
            //Write the image with specified parameters to the file
            imgWriter.write(null, iioImg, jpgWriterParam);
            //clean objects
            imgOutStrm.close();
            imgWriter.dispose();

        }
        //


    //This class makes a video file and merge audio and video files to produce a final video file
    class VideoGenerator extends Thread {
        ArrayList < String > imgList;
        File tempDir;
        Dimension ds;
        String vFileName;
        String aFileName;
        VideoGenerator() {
            imgList = new ArrayList < String > (); //create ArrayList object to store the image file paths
            tempDir = new File("tempDir"); //create file object to point to the tempDir folder
            readImageFiles(); //read the paths of the image files to store in the ArrayList object
            ds = getToolkit().getScreenSize(); //get the screen dimension-width and height
        }
        public void run() {
            try {
                setMess(btCap, "Generating video file..."); //Changing the text on the button to Generating video file...
                makeMovie((int) ds.getWidth(), (int) ds.getHeight(), 2.2 f, imgList); //generating the video file
                merging(ScreenRecording.mFileName, ScreenRecording.aFileName); //merging sound and video
                setMess(btCap, "Start Recording"); //Changing the text on the button to Start Recording 
                deleteImageFiles(); //remove the temporary image files after the final video file is generated

            } catch (Exception ie) {}

        }


        public void readImageFiles() { //read the image files to place in the list for later use
            File[] fileLst = tempDir.listFiles();
            for (int i = 0; i < fileLst.length; i++) {
                imgList.add(fileLst[i].getAbsolutePath());

            }
        }

        public void deleteImageFiles() { //delete the image files which are no longer use
            File[] fileLst = tempDir.listFiles();
            for (int i = 0; i < fileLst.length; i++) {
                File f = new File(fileLst[i].getAbsolutePath());
                f.delete();
            }
        }

        //create a no-sound video file
        public void makeMovie(int width, int height, float frameRate, ArrayList imgfiles) throws MalformedURLException {
                mFileName = System.currentTimeMillis() + ".mov"; //assign the video file to number representing the current time   
                File fout = new File(mFileName); //create file object to point to the video file
                //create media locator object to point to the file object
                MediaLocator oml = new MediaLocator(fout.toURI().toURL());
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
            //merge the sound and video files
        public void merging(String videoFileName, String audioFileName) {
            try {

                //initialize the state helper and processor objects
                StateHelper shv = null;
                StateHelper sha = null;
                StateHelper shm = null;
                Processor videoProcessor = null;
                Processor audioProcessor = null;
                Processor mixProcessor = null;
                //create MediaLocator for video and audio
                File videoFile = new File(videoFileName);
                MediaLocator videoLocator = new MediaLocator(videoFile.toURI().toURL());

                File audioFile = new File(audioFileName);
                MediaLocator audioLocator = new MediaLocator(audioFile.toURI().toURL());

                //Create MediaLocator for outputing
                File outFile = new File(System.currentTimeMillis() + "final.mov");
                MediaLocator outLocator = new MediaLocator(outFile.toURI().toURL());

                //create datasources

                DataSource videoDataSource = Manager.createDataSource(videoLocator); //your video file
                DataSource audioDataSource = Manager.createDataSource(audioLocator); // your audio file
                DataSource mixedDataSource = null; // data source to combine video with audio
                DataSource arrayDataSource[] = new DataSource[2]; //data source array
                Format[] formats = new Format[2];
                formats[0] = new AudioFormat(AudioFormat.IMA4_MS);
                //formats[0]=new AudioFormat("linear",44100,16,2);
                formats[1] = new VideoFormat(VideoFormat.JPEG);
                FileTypeDescriptor outftd = new FileTypeDescriptor(FileTypeDescriptor.QUICKTIME);

                //create processors for video and audio
                try {
                    videoProcessor = Manager.createProcessor(videoDataSource);
                    shv = new StateHelper(videoProcessor);

                    audioProcessor = Manager.createProcessor(audioDataSource);
                    sha = new StateHelper(audioProcessor);
                } catch (IOException ie) {
                    System.exit(-1);
                } catch (NoProcessorException ne) {
                    System.exit(-1);
                }


                //Configure processors
                if (!shv.configure(10000))
                    System.exit(-1);
                if (!sha.configure(10000))
                    System.exit(-1);
                //Realize processors

                if (!shv.realize(10000))
                    System.exit(-1);
                if (!sha.realize(10000))
                    System.exit(-1);
                //return data sources from processors
                arrayDataSource[0] = videoProcessor.getDataOutput();
                arrayDataSource[1] = audioProcessor.getDataOutput();

                //start the processor
                videoProcessor.start();
                audioProcessor.start();

                //create mix data source, connect, and start it
                mixedDataSource = Manager.createMergingDataSource(arrayDataSource);
                mixedDataSource.connect();
                mixedDataSource.start();

                //processor for mix output
                mixProcessor = Manager.createRealizedProcessor(new ProcessorModel(mixedDataSource, formats, outftd));
                shm = new StateHelper(mixProcessor);
                //configure the mixProcessor
                if (!shm.configure(10000))
                    System.exit(100);
                //set output file content type
                mixProcessor.setContentDescriptor(new ContentDescriptor(FileTypeDescriptor.QUICKTIME));
                //query supported formats
                TrackControl tcs[] = mixProcessor.getTrackControls();
                Format f[] = tcs[0].getSupportedFormats();
                if (f == null || f.length <= 0)
                    System.exit(100);
                //set track format
                tcs[0].setFormat(f[0]);
                //realize the mixprocessor
                if (!shm.realize(10000))
                    System.exit(100);
                //get datasource from the mixProcessor so it is ready to write to a file by DataSink filewriter
                DataSource source = mixProcessor.getDataOutput();
                //create DataSink filewrite for writing
                DataSink filewriter = null;
                try {
                    filewriter = Manager.createDataSink(source, outLocator);
                    filewriter.open();
                } catch (NoDataSinkException e) {
                    System.exit(100);
                } catch (IOException e) {
                    System.exit(100);
                } catch (SecurityException e) {
                    System.exit(100);
                }

                // now start the filewriter and mixProcessor
                try {
                    mixProcessor.start();
                    filewriter.start();
                } catch (IOException e) {
                    System.exit(-1);
                }
                // wait 5 seconds for end of media stream merging
                shm.waitToEndOfMedia(5000, true);
                shm.close();
                filewriter.close();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }


    }


    //This class creates a datasource from the Buffer that feeds on the processor
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

    //This class reads data of each image in array of bytes form to Buffer
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




    public static void main(String args[]) throws Exception {

        new ScreenRecording();

    }


}

//The StateHelper class help you determine the states of the processors
class StateHelper implements javax.media.ControllerListener {
        Processor processor = null;
        boolean configured = false;
        boolean realized = false;
        boolean prefetched = false;
        boolean eom = false;
        boolean failed = false;
        boolean closed = false;
        public StateHelper(Processor p) {
            processor = p;
            processor.addControllerListener(this);
        }
        public void turnOffRecording() {
            eom = true;
        }
        public boolean configure(int timeOutMillis) {
            long startTime = System.currentTimeMillis(); //start time of waiting
            synchronized(this) {
                processor.configure(); //configure the processor

                while (!configured && !failed) {
                    try {
                        wait(timeOutMillis); //wait for processor configuration to complete
                    } catch (InterruptedException ie) {}
                    if (System.currentTimeMillis() - startTime > timeOutMillis) //exit the loop when exceeding waiting time limit
                        break;
                }

            }
            return configured;
        }
        public boolean realize(int timeOutMillis) {
            long startTime = System.currentTimeMillis();
            synchronized(this) {
                processor.realize(); //realize the processor
                while (!realized && !failed) {
                    try {
                        wait(timeOutMillis); //wait for processor realization to complete
                    } catch (InterruptedException ie) {}
                    if (System.currentTimeMillis() - startTime > timeOutMillis)
                        break;
                }
            }
            return realized;
        }

        public boolean prefetch(int timeOutMillis) {
            long startTime = System.currentTimeMillis();
            synchronized(this) {
                processor.prefetch(); //prefetch the processor
                while (!prefetched && !failed) {
                    try {
                        wait(timeOutMillis); //wait for processor prefetch to complete
                    } catch (InterruptedException ie) {}
                    if (System.currentTimeMillis() - startTime > timeOutMillis)
                        break;
                }
            }
            return prefetched && !failed;
        }
        public boolean waitToEndOfMedia(int timeOutMillis) { //wait for end of media processing
            long startTime = System.currentTimeMillis();
            eom = false;
            synchronized(this) {
                while (!eom && !failed) {
                    try {
                        wait(timeOutMillis);
                    } catch (InterruptedException ie) {}
                }
            }
            return eom && !failed;
        }
        public boolean waitToEndOfMedia(int timeOutMillis, boolean isForMerge) { //wait for mergeing audio and video process to finish
            long startTime = System.currentTimeMillis();
            eom = false;
            synchronized(this) {
                while (!eom && !failed) {
                    try {
                        wait(timeOutMillis);
                    } catch (InterruptedException ie) {}
                    if (System.currentTimeMillis() - startTime > timeOutMillis)
                        break;
                }
            }
            return eom && !failed;
        }

        public void close() { //close the processor and wait 100 milliseconds for the close process
            synchronized(this) {
                processor.close();
                while (!closed) {
                    try {
                        wait(100);
                    } catch (InterruptedException ie) {}
                }
            }
            processor.removeControllerListener(this);
        }

        //Update the state of the processor
        public synchronized void controllerUpdate(ControllerEvent ce) {
            if (ce instanceof RealizeCompleteEvent) {
                realized = true;
            } else if (ce instanceof ConfigureCompleteEvent) {
                configured = true;
            } else if (ce instanceof PrefetchCompleteEvent) {
                prefetched = true;
            } else if (ce instanceof EndOfMediaEvent) {
                eom = true;
            } else if (ce instanceof ControllerErrorEvent) {
                failed = true;
            } else if (ce instanceof ControllerClosedEvent) {
                closed = true;
            } else {
                return;
            }
            notifyAll();
        }
}