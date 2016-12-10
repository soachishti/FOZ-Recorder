package View;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.Toolkit;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import java.awt.Font;

public class About extends JFrame {

	private JPanel contentPane;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					About frame = new About();
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
	public About() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException e1) {
			e1.printStackTrace();
		}
		setIconImage(Toolkit.getDefaultToolkit().getImage("icon.png"));
		setTitle("About");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 297, 150);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JLabel lblFozRecorder = new JLabel("FOZ Recorder");
		lblFozRecorder.setFont(new Font("Tahoma", Font.BOLD, 18));
		lblFozRecorder.setHorizontalAlignment(SwingConstants.CENTER);
		lblFozRecorder.setBounds(10, 11, 261, 14);
		contentPane.add(lblFozRecorder);
		
		JLabel lblVAlpha = new JLabel("v0.1 alpha");
		lblVAlpha.setHorizontalAlignment(SwingConstants.CENTER);
		lblVAlpha.setBounds(10, 36, 261, 14);
		contentPane.add(lblVAlpha);
		
		JLabel lblDevelopedByOwais = new JLabel("Developed by Owais, Zerk and Faisal");
		lblDevelopedByOwais.setHorizontalAlignment(SwingConstants.CENTER);
		lblDevelopedByOwais.setBounds(10, 76, 261, 29);
		contentPane.add(lblDevelopedByOwais);
	}

}
