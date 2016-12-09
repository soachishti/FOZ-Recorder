package FOZReader;

import java.awt.Component;
import java.io.File;

import javax.swing.JFileChooser;

public class FileHandle {
	JFileChooser fileChooser;
	
	public FileHandle() {
		fileChooser = new JFileChooser();
		fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
	}
	
	public String getOpenFilePath(Component ui) {
		int result = fileChooser.showOpenDialog(ui);
		if (result == JFileChooser.APPROVE_OPTION) {
		    File selectedFile = fileChooser.getSelectedFile();
		    return selectedFile.getAbsolutePath();
		}
		return "";
	}
	
	public String getSaveFilePath(Component ui) {
		int result = fileChooser.showSaveDialog(ui);
		if (result == JFileChooser.APPROVE_OPTION) {
		    File selectedFile = fileChooser.getSelectedFile();
		    return selectedFile.getAbsolutePath();
		}
		return "";
	}

	public void setPath(String directory) {
		fileChooser.setCurrentDirectory(new File(directory));
	}
}
