package jscl.editor;

import java.io.File;
import javax.swing.filechooser.FileFilter;

public class MathFileFilter extends FileFilter {
	private final String extension;

	public MathFileFilter(final String extension) {
		this.extension = extension;
	}

	public String getExtension() {
		return extension;
	}

	public boolean accept(final File f) {
		return f.isDirectory() || f.getName().endsWith("." + extension);
	}

	public String getDescription() {
		return extension.toUpperCase() + " Files";
	}
}
