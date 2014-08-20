/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jscl.editor;

import java.awt.Component;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.io.FileWriter;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Collection;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import javax.swing.event.UndoableEditEvent;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.BadLocationException;
import javax.swing.text.EditorKit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import jscl.mathml.MathML;
import jscl.mathml.SVG;
import jscl.mathml.Wiki;
import jscl.textpane.MathDocument;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.NotifyDescriptor.Confirmation;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.UndoRedo;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.ProxyLookup;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.WindowManager;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(dtd = "-//jscl.editor//Math//EN",
autostore = false)
@TopComponent.Description(preferredID = "MathTopComponent",
iconBase = "jscl/editor/meditor.png",
persistenceType = TopComponent.PERSISTENCE_ALWAYS)
@TopComponent.Registration(mode = "editor", openAtStartup = true)
@ActionID(category = "Window", id = "jscl.editor.MathTopComponent")
@ActionReference(path = "Menu/Window" /*, position = 333 */)
@TopComponent.OpenActionRegistration(displayName = "#CTL_MathAction",
preferredID = "MathTopComponent")
public final class MathTopComponent extends TopComponent implements LookupListener {

	private MathDocument doc;
	private FileObject file;
	private boolean modified;
	private boolean editable;
	private boolean found;
	private Lookup.Result result;
	private int count = 0;
	private boolean branch = false;
	private UndoRedo.Manager manager = new UndoRedo.Manager() {

		@Override
		public void undoableEditHappened(UndoableEditEvent ue) {
			super.undoableEditHappened(ue);
			if(count < 0) branch = true;
			count++;
			fire(true);
		}

		@Override
		public void redo() throws CannotRedoException {
			super.redo();
			count++;
			fire(count != 0 || branch);
		}

		@Override
		public void undo() throws CannotUndoException {
			super.undo();
			count--;
			fire(count != 0 || branch);
		}
	};
	private final InstanceContent content;
	private final SaveCookie impl;
	private final Lookup lookup;
	private final EditorKit kit = new MathEditorKit();
	private final JFileChooser chooser = new JFileChooser();
	private FileChangeAdapter fca = new FileChangeAdapter() {

		@Override
		public void fileChanged(FileEvent fe) {
			MathTopComponent.this.fileChanged(fe);
		}
	};
	private final Charset cs = Charset.defaultCharset();
	private InputOutput io = IOProvider.getDefault().getIO("Standard output", false);

	public MathTopComponent() {
		initComponents();
		setName(NbBundle.getMessage(MathTopComponent.class, "CTL_MathTopComponent"));
		setToolTipText(NbBundle.getMessage(MathTopComponent.class, "HINT_MathTopComponent"));

		jDialog1.pack();
		jDialog1.setLocationRelativeTo(this);

		plot.pack();
		plot.setLocationRelativeTo(this);

		doc = (MathDocument)kit.createDefaultDocument();

		mathTextPane1.setEditorKit(kit);
		mathTextPane1.setMathDocument(doc);
		mathTextPane1.setText(org.openide.util.NbBundle.getMessage(MathTopComponent.class, "MathTopComponent.mathTextPane1.text"));

		chooser.removeChoosableFileFilter(chooser.getAcceptAllFileFilter());
		chooser.addChoosableFileFilter(new MathFileFilter("pdf"));
		chooser.addChoosableFileFilter(new MathFileFilter("xhtml"));

		impl = new FileObjectSaveCapability();
		content = new InstanceContent();
		lookup = new AbstractLookup(content);
		content.add(new MathEditorCapability());

		System.setOut(new PrintStream(new BufferedOutputStream(getOutputStream(io.getOut()), 128), true));
        }

	public OutputStream getOutputStream(final Writer writer) {
		return new OutputStream() {

			@Override
			public void write(int b) throws IOException {
				write(new byte[] {(byte)b});
			}

			@Override
			public void write(byte[] b, int off, int len) throws IOException {
				writer.write(cs.decode(ByteBuffer.wrap(b, off, len)).array());
			}
		};
	}

	@Override
	public Lookup getLookup() {
		return new ProxyLookup(super.getLookup(), lookup);
	}

	/** This method is called from within the constructor to
	 * initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the Form Editor.
	 */
	// <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
	private void initComponents() {

		jDialog1 = new javax.swing.JDialog();
		jLabel1 = new javax.swing.JLabel();
		jTextField1 = new javax.swing.JTextField();
		jLabel2 = new javax.swing.JLabel();
		jTextField2 = new javax.swing.JTextField();
		jButton1 = new javax.swing.JButton();
		jButton2 = new javax.swing.JButton();
		jButton3 = new javax.swing.JButton();
		jButton4 = new javax.swing.JButton();
		plot = new javax.swing.JDialog();
		jScrollPane1 = new javax.swing.JScrollPane();
		mathTextPane1 = new jscl.textpane.MathTextPane();

		jDialog1.setTitle(org.openide.util.NbBundle.getMessage(MathTopComponent.class, "MathTopComponent.jDialog1.title")); // NOI18N
		jDialog1.setAlwaysOnTop(true);
		jDialog1.setResizable(false);

		org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(MathTopComponent.class, "MathTopComponent.jLabel1.text")); // NOI18N

		org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(MathTopComponent.class, "MathTopComponent.jLabel2.text")); // NOI18N

		org.openide.awt.Mnemonics.setLocalizedText(jButton1, org.openide.util.NbBundle.getMessage(MathTopComponent.class, "MathTopComponent.jButton1.text")); // NOI18N
		jButton1.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jButton1ActionPerformed(evt);
			}
		});

		org.openide.awt.Mnemonics.setLocalizedText(jButton2, org.openide.util.NbBundle.getMessage(MathTopComponent.class, "MathTopComponent.jButton2.text")); // NOI18N
		jButton2.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jButton2ActionPerformed(evt);
			}
		});

		org.openide.awt.Mnemonics.setLocalizedText(jButton3, org.openide.util.NbBundle.getMessage(MathTopComponent.class, "MathTopComponent.jButton3.text")); // NOI18N
		jButton3.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jButton3ActionPerformed(evt);
			}
		});

		org.openide.awt.Mnemonics.setLocalizedText(jButton4, org.openide.util.NbBundle.getMessage(MathTopComponent.class, "MathTopComponent.jButton4.text")); // NOI18N
		jButton4.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jButton4ActionPerformed(evt);
			}
		});

		javax.swing.GroupLayout jDialog1Layout = new javax.swing.GroupLayout(jDialog1.getContentPane());
		jDialog1.getContentPane().setLayout(jDialog1Layout);
		jDialog1Layout.setHorizontalGroup(
			jDialog1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
			.addGroup(jDialog1Layout.createSequentialGroup()
				.addContainerGap()
				.addGroup(jDialog1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
					.addComponent(jTextField1, javax.swing.GroupLayout.DEFAULT_SIZE, 376, Short.MAX_VALUE)
					.addComponent(jLabel1)
					.addComponent(jLabel2)
					.addComponent(jTextField2, javax.swing.GroupLayout.DEFAULT_SIZE, 376, Short.MAX_VALUE)
					.addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jDialog1Layout.createSequentialGroup()
						.addComponent(jButton4)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(jButton3)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(jButton2)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(jButton1)))
				.addContainerGap())
		);
		jDialog1Layout.setVerticalGroup(
			jDialog1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
			.addGroup(jDialog1Layout.createSequentialGroup()
				.addContainerGap()
				.addComponent(jLabel1)
				.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
				.addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
				.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
				.addComponent(jLabel2)
				.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
				.addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
				.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
				.addGroup(jDialog1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
					.addComponent(jButton1)
					.addComponent(jButton2)
					.addComponent(jButton3)
					.addComponent(jButton4))
				.addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
		);

		plot.setModal(true);
		plot.setPreferredSize(new java.awt.Dimension(400, 400));

		mathTextPane1.setEditable(false);
		mathTextPane1.setFont(new java.awt.Font("Monospaced", 0, 14)); // NOI18N
		jScrollPane1.setViewportView(mathTextPane1);

		javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
		this.setLayout(layout);
		layout.setHorizontalGroup(
			layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
			.addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
		);
		layout.setVerticalGroup(
			layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
			.addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE)
		);
	}// </editor-fold>//GEN-END:initComponents

private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
	found=mathTextPane1.findNext(jTextField1.getText(), true);
}//GEN-LAST:event_jButton4ActionPerformed

private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
	if(found) found = mathTextPane1.replace(jTextField1.getText(),jTextField2.getText());
}//GEN-LAST:event_jButton3ActionPerformed

private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
	mathTextPane1.replaceAll(jTextField1.getText(),jTextField2.getText());
}//GEN-LAST:event_jButton2ActionPerformed

private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
	jDialog1.setVisible(false);
}//GEN-LAST:event_jButton1ActionPerformed

	// Variables declaration - do not modify//GEN-BEGIN:variables
	private javax.swing.JButton jButton1;
	private javax.swing.JButton jButton2;
	private javax.swing.JButton jButton3;
	private javax.swing.JButton jButton4;
	private javax.swing.JDialog jDialog1;
	private javax.swing.JLabel jLabel1;
	private javax.swing.JLabel jLabel2;
	private javax.swing.JScrollPane jScrollPane1;
	private javax.swing.JTextField jTextField1;
	private javax.swing.JTextField jTextField2;
	private jscl.textpane.MathTextPane mathTextPane1;
	private javax.swing.JDialog plot;
	// End of variables declaration//GEN-END:variables
	@Override
	public void componentOpened() {
		result = WindowManager.getDefault().findTopComponent("favorites").getLookup().lookupResult(FileObject.class);
		result.addLookupListener(this);
	}

	@Override
	public void componentClosed() {
		result.removeLookupListener(this);
		result = null;
	}

	@Override
	public UndoRedo getUndoRedo() {
		return manager;
	}

	@Override
	public void resultChanged(LookupEvent ev) {
		Lookup.Result r = (Lookup.Result) ev.getSource();
		Collection<FileObject> coll = r.allInstances();
		if (coll.isEmpty()) return;
		FileObject file = this.file;
		if (file != null) file.removeFileChangeListener(fca);
		for (FileObject obj : coll) {
			file = obj;
			if (this.file == file) return;
		}
		if (file.isFolder()) file = file.getFileObject("/index.txt");
		if (file == null || !"text/plain".equals(file.getMIMEType())) return;
		if (modified) if(!proceed()) return;
		file.addFileChangeListener(fca);
		this.file = file;
		load();
	}

	public void fileChanged(FileEvent fe) {
		if (file != fe.getFile()) return;
		if (modified) if(!reload()) return;
		load();
	}

	void load() {
		setName(file.getNameExt());
		try {
			editable = !file.getFileSystem().isReadOnly();
		} catch (FileStateInvalidException ex) {
			Exceptions.printStackTrace(ex);
		}
		doc = (MathDocument)kit.createDefaultDocument();
		mathTextPane1.setMathDocument(doc);
		try {
			InputStream in = file.getInputStream();
			kit.read(in, doc, 0);
			in.close();
		} catch (IOException ex) {
			Exceptions.printStackTrace(ex);
		} catch (BadLocationException ex) {
			Exceptions.printStackTrace(ex);
		}
		mathTextPane1.setEditable(editable);
		manager.discardAllEdits();
		doc.addUndoableEditListener(manager);
		count = 0;
		branch = false;
		fire(false);
	}

	public void fire(boolean modified) {
		this.modified = modified;
		if (modified) {
			content.add(impl);
		} else {
			content.remove(impl);
		}
	}

	public boolean proceed() {
		Confirmation message = new NotifyDescriptor.Confirmation("Changes to \"" + file.getNameExt() + "\" not saved. Proceed ?", NotifyDescriptor.YES_NO_OPTION, NotifyDescriptor.QUESTION_MESSAGE);
		Object result = DialogDisplayer.getDefault().notify(message);
		return NotifyDescriptor.YES_OPTION.equals(result);
	}

	public boolean reload() {
		Confirmation message = new NotifyDescriptor.Confirmation("\"" + file.getNameExt() + "\" was modified externally. Reload ?", NotifyDescriptor.YES_NO_OPTION, NotifyDescriptor.QUESTION_MESSAGE);
		Object result = DialogDisplayer.getDefault().notify(message);
		return NotifyDescriptor.YES_OPTION.equals(result);
	}

	public boolean overwrite() {
		Confirmation message = new NotifyDescriptor.Confirmation("The file already exists. Overwrite ?", NotifyDescriptor.YES_NO_OPTION, NotifyDescriptor.QUESTION_MESSAGE);
		Object result = DialogDisplayer.getDefault().notify(message);
		return NotifyDescriptor.YES_OPTION.equals(result);
	}

	private class FileObjectSaveCapability implements SaveCookie {

		@Override
		public void save() throws IOException {
			file.removeFileChangeListener(fca);
			try {
				OutputStream out = file.isData()?file.getOutputStream():file.createAndOpen("index.txt");
				kit.write(out, doc, 0, doc.getLength());
				out.close();
			} catch (BadLocationException ex) {
				Exceptions.printStackTrace(ex);
			}
			file.addFileChangeListener(fca);
			count = 0;
			branch = false;
			fire(false);
		}

		@Override
		public String toString() {
			return file.getNameExt();
		}
	}

	private class MathEditorCapability implements MathCookie {

		@Override
		public void find() {
			jDialog1.setTitle("Find");
			jTextField2.setEnabled(false);
			jButton2.setEnabled(false);
			jButton3.setEnabled(false);
			jDialog1.setVisible(true);
		}

		@Override
		public void replace() {
			jDialog1.setTitle("Replace");
			jTextField2.setEnabled(true);
			jButton2.setEnabled(true);
			jButton3.setEnabled(true);
			jDialog1.setVisible(true);
		}

		@Override
		public void run() throws Exception {
			final String selected = mathTextPane1.getSelectedText();
			final String data = selected == null?mathTextPane1.getText():selected;
			MathManager.getDefault().eval(code(data));
			unselect();
		}

		@Override
		public void evaluate() throws Exception {
			final String selected = mathTextPane1.getSelectedText();
			final String data = selected == null?"":selected;
			final Object result = MathManager.getDefault().eval(code(data));
			final int n = data.length() - 1;
			if (n < 0 || "\n".equals(data.substring(n))) {
				unselect();
			} else {
				final String str = render(result);
				mathTextPane1.replaceSelection(str);
			}
		}

		@Override
		public void copyToWiki() throws Exception {
			final String selected = mathTextPane1.getSelectedText();
			final String data = selected == null?"":selected;
			final int n = data.length() - 1;
			if (n < 0);
			else {
				String srcData = Wiki.copyToWiki(data);
				StringSelection contents = new StringSelection(srcData);
				getClipboard().setContents(contents, null);
			}
		}

		@Override
		public void pasteFromWiki() throws Exception {
			String srcData = (String)getClipboard().getContents(null).getTransferData(DataFlavor.stringFlavor);
			mathTextPane1.replaceSelection(Wiki.pasteFromWiki(srcData));
		}

		@Override
		public void copyToCode() throws Exception {
			final String selected = mathTextPane1.getSelectedText();
			final String data = selected == null?"":selected;
			final int n = data.length() - 1;
			if (n < 0);
			else {
				String srcData = code(data);
				StringSelection contents = new StringSelection(srcData);
				getClipboard().setContents(contents, null);
			}
		}

		@Override
		public void export() throws IOException {
			final MathManager manager = MathManager.getDefault();
			final String formattingStylesheet = manager.getFormattingStylesheet();
			final String stylesheet = manager.getStylesheet();
			final String feed = manager.getFeed();
			final String icon = manager.getIcon();
			final String name = file == null?"":file.getName();
			chooser.setSelectedFile(new File(name));
			if(chooser.showSaveDialog(MathTopComponent.this) != JFileChooser.APPROVE_OPTION) return;
			final String extension = ((MathFileFilter)chooser.getFileFilter()).extension;
			final File f = putExtension(chooser.getSelectedFile(), extension);
			if(f.exists()) if (!overwrite()) return;
			try {
				SwingUtilities.invokeLater(new Runnable() {

					@Override
					public void run() {
						try {
							if("pdf".equals(extension)) {
								byte b[] = MathML.exportToPDF(doc.getText(), orNull(formattingStylesheet));
								OutputStream out = new FileOutputStream(f);
								out.write(b);
								out.close();
							} else {
								String s=MathML.exportToXHTML(doc.getText(), orNull(stylesheet), orNull(name), orNull(feed), orNull(icon));
								Writer out=new FileWriter(f);
								out.write(s);
								out.close();

							}
						} catch (Exception ex) {
							Exceptions.printStackTrace(ex);
						}
					}
				});
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	private static String orNull(String str) {
		return "".equals(str)?null:str;
	}

	private static String code(final String str) throws Exception {
		final String stylesheet = MathManager.getDefault().getEngineStylesheet();
		return "".equals(stylesheet)?str:MathML.code(str, stylesheet);
	}

	private String render(Object obj) throws Exception {
		if(obj == null) return "null";
		if(obj instanceof Component) {
			Component comp = (Component)obj;
			plot.getContentPane().removeAll();
			plot.getContentPane().add(comp);
			plot.setVisible(true);
			return SVG.print(comp);
		}
		try {
			return render(new Graph(obj, obj.getClass().getMethod("apply", new Class[] {double.class})));
		} catch (NoSuchMethodException ex) {}
		if (MathManager.getDefault().isRendering()) {
			try {
				return "<math>" + obj.getClass().getMethod("toMathML", new Class[] {}).invoke(obj, new Object[] {}) + "</math>";
			} catch (NoSuchMethodException ex) {}
		}
		try {
			return obj.getClass().getMethod("toJava", new Class[] {}).invoke(obj, new Object[] {}).toString();
		} catch (NoSuchMethodException ex) {}
		return obj.toString();
	}

	void unselect() {
		int n = mathTextPane1.getCaretPosition();
		mathTextPane1.setSelectionStart(n);
		mathTextPane1.setSelectionEnd(n);
	}

	private static Clipboard getClipboard() {
		Clipboard c = Lookup.getDefault().lookup(Clipboard.class);

		if (c == null) {
			c = Toolkit.getDefaultToolkit().getSystemClipboard();
		}

		return c;
	}

	static File putExtension(File file, String extension) {
		String name=file.getPath();
		if(extension==null || name.endsWith("."+extension)) return file;
		return new File(name+"."+extension);
	}

	static class MathFileFilter extends FileFilter {
		String extension;

		public MathFileFilter(String extension) {
			this.extension=extension;
		}

		public boolean accept(File f) {
			return f.isDirectory() || f.getName().endsWith("."+extension);
		}

		public String getDescription() {
			return extension.toUpperCase()+" Files";
		}
	}

	void writeProperties(java.util.Properties p) {
		// better to version settings since initial version as advocated at
		// http://wiki.apidesign.org/wiki/PropertyFiles
		p.setProperty("version", "1.0");
		// TODO store your settings
	}

	void readProperties(java.util.Properties p) {
		String version = p.getProperty("version");
		// TODO read your settings according to their version
	}
}
