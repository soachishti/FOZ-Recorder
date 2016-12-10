package FOZReader;

import java.awt.Component;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

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
		fileChooser.setFileFilter(new FileFilter() {
			public String getDescription() {
					return "MPEG-4 Container (*.mov)";
			}

			@Override
			public boolean accept(File f) {
				// TODO Auto-generated method stub
				return false;
			}
		});

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
