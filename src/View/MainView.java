package View;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.Image;
import java.awt.Toolkit;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import FOZReader.AudioHandle;
import FOZReader.FileHandle;
import FOZReader.ScreenBroadcast;
import FOZReader.ScreenShot;
import FOZReader.WebcamHandle;

import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.Box;
import javax.swing.SwingConstants;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.MalformedURLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.awt.event.ActionEvent;

public class MainView extends JFrame {
	JCheckBox chckbxShowWebcam;
	JCheckBox chckbxLocalBroadcast;
	JCheckBox chckbxRecordAudio;
	JComboBox comboBoxVideo;
	JLabel lblAddress;
	JComboBox cmbWebcam;
	JButton btnStopRecording;
	JButton btnStartRecording;
	JLabel lblStatus;
	Thread tStatus;
	boolean tTimer = true;
	String tmpDir = System.getProperty("java.io.tmpdir");
	ScreenShot screen;
	WebcamHandle webcam;
	AudioHandle audio;
	ScreenBroadcast broadcast;		
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MainView frame = new MainView();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public MainView() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException e1) {
			e1.printStackTrace();
		}
		setIconImage(Toolkit.getDefaultToolkit().getImage("icon.png"));
		setTitle("FOZ Recorder");
		
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 383, 265);
		
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		
		JMenu mnFile = new JMenu("File");
		menuBar.add(mnFile);
		
		JMenuItem mntmExit = new JMenuItem("Exit");
		mntmExit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				System.exit(0);
			}
		});
		mnFile.add(mntmExit);
		
		JMenu mnHelp = new JMenu("Help");
		menuBar.add(mnHelp);
		
		JMenuItem mntmAbout = new JMenuItem("About");
		mntmAbout.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				About a = new About();
				a.setVisible(true);
			}
		});
		mnHelp.add(mntmAbout);
		getContentPane().setLayout(null);
				
		btnStartRecording = new JButton("Start Recording");
		btnStartRecording.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				chckbxLocalBroadcast.setEnabled(false);
				chckbxRecordAudio.setEnabled(false);
				chckbxShowWebcam.setEnabled(false);
				cmbWebcam.setEnabled(false);
				comboBoxVideo.setEnabled(false);
								
				boolean chkLocal = chckbxLocalBroadcast.isSelected();
				boolean chkWebcam = chckbxShowWebcam.isSelected();
				boolean chkAudio = chckbxRecordAudio.isSelected();				
				
				tmpDir = "tmp/foz-" + System.currentTimeMillis() + "/";
				System.out.println("TmpDir: " + tmpDir);
				
				new Thread() {
					public void run() {
						screen = new ScreenShot();
						webcam = new WebcamHandle();
						audio = new AudioHandle();
						broadcast = new ScreenBroadcast();		
						
						lblStatus.setText("Starting server...");
						lblStatus.repaint();
						if (chkLocal == true) {
							broadcast.setTmpDir(tmpDir);
							broadcast.start();
							System.out.println("Server started");
						}
						
						lblStatus.setText("Starting webcam...");
						lblStatus.repaint();
						if (chkWebcam == true) {
							String size = cmbWebcam.getSelectedItem().toString().toLowerCase();
							webcam.size(size);
							webcam.start();
							System.out.println("Webcam initiated.");
						}
										
						lblStatus.setText("Starting audio device...");
						lblStatus.repaint();
						if (chkAudio == true) {
							audio.setFilename(tmpDir + "/audio.wav");
							audio.record();
						}
						
						lblStatus.setText("Starting screen capture...");
						lblStatus.repaint();
						String mode = comboBoxVideo.getSelectedItem().toString().toLowerCase();
						if (mode.equals("standard")) {
							screen.setInterval(200); // 5 frame per second
						}
						else {
							screen.setInterval(6000); // Hyperlapse every 6 second
						}
						
						screen.setMultiDirectoy(tmpDir);
						screen.multiCapture();
						
						btnStartRecording.setEnabled(false);
						btnStopRecording.setEnabled(true);
						tTimer = true;
						tStatus = new Thread() {
							public void run(){
								int min = 0;
								int sec = 0;
								while (tTimer) {
									
									sec++;
									if (sec == 60) {
										sec = 0;
										min++;
									}
									
									lblStatus.setText(String.format("%02d:%02d", min, sec));
									
									try {
										Thread.sleep(1000); 
									} catch (InterruptedException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
									
								}
							}
						};
						tStatus.start();
					}
				}.start();
			}
		});
		btnStartRecording.setBounds(250, 167, 107, 23);
		getContentPane().add(btnStartRecording);
		
		btnStopRecording = new JButton("Stop Recording");
		btnStopRecording.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				tTimer = false;
				try {
					tStatus.join();
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				lblStatus.setText("Stopped!");
				
				boolean chkLocal = chckbxLocalBroadcast.isSelected();
				boolean chkWebcam = chckbxShowWebcam.isSelected();
				boolean chkAudio = chckbxRecordAudio.isSelected();
				
				if (chkLocal == true) {
					broadcast.stop();
				}
				
				if (chkAudio == true) {
					audio.stop();
				}

				if (chkWebcam == true) {
					webcam.close();
				}

				screen.stopMultiCapture();
				
				lblStatus.setText("Save video?");
				
				new Thread() {
					public void run() {
						FileHandle f = new FileHandle();
						
						String videoFileName;
						videoFileName = f.getSaveFilePath(getContentPane());
						
						while (true) {
							
							if (videoFileName == null) {
								JOptionPane.showMessageDialog(getContentPane(), "No file selected!");
							} 
							else if (videoFileName.toString().toLowerCase().endsWith(".mov") == false) {
								videoFileName = videoFileName + ".mov";
								break;
							}
							else {
								File file = new File(videoFileName);
								if (file.exists()) {
									JOptionPane.showMessageDialog(getContentPane(), "File already exists!");
								}
								else {
									break;
								}
							}
							videoFileName = f.getSaveFilePath(getContentPane());
						}
						System.out.println(videoFileName);
						
						lblStatus.setText("Processing video...");
						try {
							if (chkAudio == true) {
								
								screen.makeMovie(tmpDir + "video.mov");
								lblStatus.setText("Merging video...");
								screen.merging(videoFileName);
							}
							else {
								screen.makeMovie(videoFileName);
							}
						} catch (MalformedURLException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						
						deleteDirectory(new File(tmpDir));
						
						chckbxLocalBroadcast.setEnabled(true);
						chckbxRecordAudio.setEnabled(true);
						chckbxShowWebcam.setEnabled(true);
						cmbWebcam.setEnabled(true);
						comboBoxVideo.setEnabled(true);
						
						btnStartRecording.setEnabled(true);
						btnStopRecording.setEnabled(false);
						lblStatus.setText("Done");
	
						screen = new ScreenShot();
						webcam = new WebcamHandle();
						audio = new AudioHandle();
						broadcast = new ScreenBroadcast();		
					}
				}.start();
			}
		});
		btnStopRecording.setEnabled(false);
		btnStopRecording.setBounds(133, 167, 107, 23);
		getContentPane().add(btnStopRecording);
		
		chckbxShowWebcam = new JCheckBox("Show Webcam");
		chckbxShowWebcam.setBounds(10, 43, 97, 23);
		getContentPane().add(chckbxShowWebcam);
		
		chckbxLocalBroadcast = new JCheckBox("Local Broadcast");
		chckbxLocalBroadcast.setBounds(10, 84, 125, 23);
		getContentPane().add(chckbxLocalBroadcast);
		
		chckbxRecordAudio = new JCheckBox("Record Audio");
		chckbxRecordAudio.setBounds(10, 124, 125, 23);
		getContentPane().add(chckbxRecordAudio);
		
		JLabel lblVideoMode = new JLabel("Video Mode:");
		lblVideoMode.setBounds(10, 11, 86, 14);
		getContentPane().add(lblVideoMode);
		
		comboBoxVideo = new JComboBox();
		comboBoxVideo.setModel(new DefaultComboBoxModel(new String[] {"Standard", "Hyperlapse"}));
		comboBoxVideo.setBounds(224, 8, 125, 20);
		getContentPane().add(comboBoxVideo);
		
		lblAddress = new JLabel("http://localhost:7881/");
		lblAddress.setHorizontalAlignment(SwingConstants.RIGHT);
		lblAddress.setBounds(211, 88, 138, 14);
		getContentPane().add(lblAddress);
		
		// Update address
		new Thread() {
			public void run() {
				lblAddress.setText(new ScreenBroadcast().getIPAddress());
			}
		}.start();
		
		JLabel lblIp = new JLabel("IP:");
		lblIp.setHorizontalAlignment(SwingConstants.RIGHT);
		lblIp.setBounds(206, 84, 28, 22);
		getContentPane().add(lblIp);
		
		JSeparator separator = new JSeparator();
		separator.setBounds(10, 114, 347, 2);
		getContentPane().add(separator);
		
		JSeparator separator_1 = new JSeparator();
		separator_1.setBounds(10, 32, 347, 2);
		getContentPane().add(separator_1);
		
		JSeparator separator_2 = new JSeparator();
		separator_2.setBounds(10, 73, 347, 2);
		getContentPane().add(separator_2);
		
		JSeparator separator_3 = new JSeparator();
		separator_3.setBounds(10, 154, 347, 2);
		getContentPane().add(separator_3);
		
		cmbWebcam = new JComboBox();
		cmbWebcam.setModel(new DefaultComboBoxModel(new String[] {"Small", "Medium"}));
		cmbWebcam.setBounds(224, 44, 125, 20);
		getContentPane().add(cmbWebcam);
		
		lblStatus = new JLabel("");
		lblStatus.setBounds(10, 171, 113, 14);
		getContentPane().add(lblStatus);
		
	}

	public void finalize() {
		deleteDirectory(new File(tmpDir));
	}
	
	public boolean deleteDirectory(File directory) {
	    if(directory.exists()){
	        File[] files = directory.listFiles();
	        if(null!=files){
	            for(int i=0; i<files.length; i++) {
	                if(files[i].isDirectory()) {
	                    deleteDirectory(files[i]);
	                }
	                else {
	                    files[i].delete();
	                }
	            }
	        }
	    }
	    return(directory.delete());
	}
}
