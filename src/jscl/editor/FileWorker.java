package jscl.editor;

import javax.swing.SwingWorker;

public abstract class FileWorker extends SwingWorker<Boolean, Object> {
	private int length;

	public final void setLength(final int length) {
		this.length = length;
	}

	public void setNumber(final int n) {
		if (length > 0) {
			setProgress(100 * n / length);
		}
	}
}
