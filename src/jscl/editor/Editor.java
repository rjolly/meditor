package jscl.editor;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.Reader;
import java.io.Writer;
import java.io.FileWriter;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.SwingWorker;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultEditorKit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;
import linoleum.application.FileChooser;
import linoleum.application.ScriptSupport;
import org.jdesktop.swingx.JXGraph;

public class Editor extends ScriptSupport {
	private final Icon newIcon = new ImageIcon(getClass().getResource("/toolbarButtonGraphics/general/New24.gif"));
	private final Icon newIcon16 = new ImageIcon(getClass().getResource("/toolbarButtonGraphics/general/New16.gif"));
	private final Icon openIcon = new ImageIcon(getClass().getResource("/toolbarButtonGraphics/general/Open24.gif"));
	private final Icon openIcon16 = new ImageIcon(getClass().getResource("/toolbarButtonGraphics/general/Open16.gif"));
	private final Icon saveIcon = new ImageIcon(getClass().getResource("/toolbarButtonGraphics/general/Save24.gif"));
	private final Icon saveIcon16 = new ImageIcon(getClass().getResource("/toolbarButtonGraphics/general/Save16.gif"));
	private final Icon saveAsIcon = new ImageIcon(getClass().getResource("/toolbarButtonGraphics/general/SaveAs24.gif"));
	private final Icon saveAsIcon16 = new ImageIcon(getClass().getResource("/toolbarButtonGraphics/general/SaveAs16.gif"));
	private final Icon cutIcon = new ImageIcon(getClass().getResource("/toolbarButtonGraphics/general/Cut24.gif"));
	private final Icon cutIcon16 = new ImageIcon(getClass().getResource("/toolbarButtonGraphics/general/Cut16.gif"));
	private final Icon copyIcon = new ImageIcon(getClass().getResource("/toolbarButtonGraphics/general/Copy24.gif"));
	private final Icon copyIcon16 = new ImageIcon(getClass().getResource("/toolbarButtonGraphics/general/Copy16.gif"));
	private final Icon pasteIcon = new ImageIcon(getClass().getResource("/toolbarButtonGraphics/general/Paste24.gif"));
	private final Icon pasteIcon16 = new ImageIcon(getClass().getResource("/toolbarButtonGraphics/general/Paste16.gif"));
	private final Icon undoIcon = new ImageIcon(getClass().getResource("/toolbarButtonGraphics/general/Undo24.gif"));
	private final Icon undoIcon16 = new ImageIcon(getClass().getResource("/toolbarButtonGraphics/general/Undo16.gif"));
	private final Icon redoIcon = new ImageIcon(getClass().getResource("/toolbarButtonGraphics/general/Redo24.gif"));
	private final Icon redoIcon16 = new ImageIcon(getClass().getResource("/toolbarButtonGraphics/general/Redo16.gif"));
	private final Icon findIcon = new ImageIcon(getClass().getResource("find24.gif"));
	private final Icon findIcon16 = new ImageIcon(getClass().getResource("find.gif"));
	private final Icon runIcon = new ImageIcon(getClass().getResource("runProject24.png"));
	private final Icon runIcon16 = new ImageIcon(getClass().getResource("runProject.png"));
	private final Action cutAction = new DefaultEditorKit.CutAction();
	private final Action copyAction = new DefaultEditorKit.CopyAction();
	private final Action pasteAction = new DefaultEditorKit.PasteAction();
	private final Action findAction = new FindAction();
	private final Action replaceAction = new ReplaceAction();
	private final Action runAction = new RunAction();
	private final Action evalAction = new EvalAction();
	private final Action copyToWikiAction = new CopyToWikiAction();
	private final Action pasteFromWikiAction = new PasteFromWikiAction();
	private final Action copyToCodeAction = new CopyToCodeAction();
	private final Action exportAction = new ExportAction();
	private final Action preferenceAction = new PreferenceAction();
	private final Action newAction = new NewAction();
	private final Action openAction = new OpenAction();
	private final Action saveAction = new SaveAction();
	private final Action saveAsAction = new SaveAsAction();
	private final Action closeAction = new CloseAction();
	private final UndoAction undoAction = new UndoAction();
	private final RedoAction redoAction = new RedoAction();
	private final UndoManager undo = new UndoManager();
	private final UndoableEditListener undoHandler = new UndoableEditListener() {
		public void undoableEditHappened(final UndoableEditEvent e) {
			undo.addEdit(e.getEdit());
			if (modified >= 0) modified += 1;
			undoAction.update();
			redoAction.update();
		}
	};
	private final FileChooser exportChooser = new FileChooser();
	private final FileChooser chooser = new FileChooser();
	private ScriptEngine engine;
	private boolean found;
	private int modified;
	private String name;
	private Path file;
	private Path prev;

	class UndoAction extends AbstractAction {
		public UndoAction() {
			super("Undo");
			putValue(Action.SMALL_ICON, undoIcon16);
			putValue(Action.LARGE_ICON_KEY, undoIcon);
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_MASK));
			setEnabled(false);
		}

		public void actionPerformed(final ActionEvent e) {
			try {
				undo.undo();
				modified -= 1;
			} catch (final CannotUndoException ex) {
				Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Unable to undo", ex);
			}
			update();
			redoAction.update();
		}

		void update() {
			if (undo.canUndo()) {
				setEnabled(true);
				putValue(Action.NAME, undo.getUndoPresentationName());
			} else {
				setEnabled(false);
				putValue(Action.NAME, "Undo");
			}
			Editor.this.update();
		}
	}

	class RedoAction extends AbstractAction {
		public RedoAction() {
			super("Redo");
			putValue(Action.SMALL_ICON, redoIcon16);
			putValue(Action.LARGE_ICON_KEY, redoIcon);
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_MASK));
			setEnabled(false);
		}

		public void actionPerformed(final ActionEvent e) {
			try {
				undo.redo();
				modified += 1;
			} catch (final CannotRedoException ex) {
				Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Unable to redo", ex);
			}
			update();
			undoAction.update();
		}

		void update() {
			if (undo.canRedo()) {
				setEnabled(true);
				putValue(Action.NAME, undo.getRedoPresentationName());
			} else {
				setEnabled(false);
				putValue(Action.NAME, "Redo");
			}
		}
	}

	private class FindAction extends AbstractAction {
		private FindAction() {
			super("Find");
			putValue(Action.SMALL_ICON, findIcon16);
			putValue(Action.LARGE_ICON_KEY, findIcon);
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_MASK));
		}

		@Override
		public void actionPerformed(final ActionEvent e) {
			toggleDialog();
			jTextField2.setEnabled(false);
			jButton2.setEnabled(false);
			jButton3.setEnabled(false);
		}
	}

	private class ReplaceAction extends AbstractAction {
		private ReplaceAction() {
			super("Replace");
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_MASK));
		}

		@Override
		public void actionPerformed(final ActionEvent e) {
			toggleDialog();
			jTextField2.setEnabled(true);
			jButton2.setEnabled(true);
			jButton3.setEnabled(true);
		}
	}

	private void toggleDialog() {
		if (jPanel1.getComponentCount() < 2) {
			jPanel1.add(jPanel2, BorderLayout.SOUTH);
		} else {
			jPanel1.remove(jPanel2);
		}
		revalidate();
	}

	private class RunAction extends AbstractAction {
		private RunAction() {
			super("Run");
			putValue(Action.SMALL_ICON, runIcon16);
			putValue(Action.LARGE_ICON_KEY, runIcon);
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_MASK));
		}

		@Override
		public void actionPerformed(final ActionEvent e) {
			new EvalWorker(false).execute();
		}
	}

	private class EvalAction extends AbstractAction {
		private EvalAction() {
			super("Evaluate");
			putValue(Action.SMALL_ICON, findIcon16);
			putValue(Action.LARGE_ICON_KEY, findIcon);
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_MASK));
		}

		@Override
		public void actionPerformed(final ActionEvent e) {
			new EvalWorker(true).execute();
		}
	}

	private class CopyToWikiAction extends AbstractAction {
		private CopyToWikiAction() {
			super("Copy to wiki");
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_K, InputEvent.CTRL_MASK));
		}

		@Override
		public void actionPerformed(final ActionEvent e) {
			final String selected = mathTextPane1.getSelectedText();
			final String data = selected == null?"":selected;
			final int n = data.length() - 1;
			if (n < 0);
			else try {
				getClipboard().setContents(new StringSelection(Wiki.instance.copyToWiki(data)), null);
			} catch (final IOException ex) {
				ex.printStackTrace();
			}
		}
	}

	private class PasteFromWikiAction extends AbstractAction {
		private PasteFromWikiAction() {
			super("Paste from wiki");
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.CTRL_MASK));
		}

		@Override
		public void actionPerformed(final ActionEvent e) {
			try {
				mathTextPane1.replaceSelection(Wiki.instance.pasteFromWiki((String) getClipboard().getContents(null).getTransferData(DataFlavor.stringFlavor)));
			} catch (final UnsupportedFlavorException ex) {
				ex.printStackTrace();
			} catch (final IOException ex) {
				ex.printStackTrace();
			}
		}
	}

	private class CopyToCodeAction extends AbstractAction {
		private CopyToCodeAction() {
			super("Copy to code");
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_J, InputEvent.CTRL_MASK));
		}

		@Override
		public void actionPerformed(final ActionEvent e) {
			final String selected = mathTextPane1.getSelectedText();
			final String data = selected == null?"":selected;
			final int n = data.length() - 1;
			if (n < 0);
			else try {
				getClipboard().setContents(new StringSelection(code(data)), null);
			} catch (final IOException ex) {
				ex.printStackTrace();
			}
		}
	}

	private Clipboard getClipboard() {
		return Toolkit.getDefaultToolkit().getSystemClipboard();
	}

	private class ExportAction extends AbstractAction {
		private ExportAction() {
			super("Export...");
		}

		@Override
		public void actionPerformed(final ActionEvent e) {
			final FileChooser chooser = getOwner().exportChooser;
			final String name = file == null?null:stripExtension(file.getFileName().toString());
			chooser.setSelectedFile(name == null?null:new File(name));
			if (chooser.showInternalSaveDialog(jPanel1) != JFileChooser.APPROVE_OPTION) return;
			final String extension = ((MathFileFilter) chooser.getFileFilter()).getExtension();
			final File f = putExtension(chooser.getSelectedFile(), extension);
			if (f.exists() && !proceed("File exists. Overwrite ?")) return;
			new FileExporter(f, name, extension).execute();
		}
	}

	private class PreferenceAction extends AbstractAction {
		private PreferenceAction() {
			super("Preferences");
		}

		@Override
		public void actionPerformed(final ActionEvent e) {
			try {
				getApplicationManager().open(new URI("prefs", getName(), null));
			} catch (final URISyntaxException ex) {
				ex.printStackTrace();
			}
		}
	}

	private String stripExtension(final String name) {
		final int n = name.lastIndexOf(".");
		if (n < 0) {
			return name;
		}
		return name.substring(0, n);
	}

	private File putExtension(final File f, final String extension) {
		final String name = f.getPath();
		if (extension == null || name.endsWith("." + extension)) {
			return f;
		}
		return new File(name + "." + extension);
	}

	private class NewAction extends AbstractAction {	
		public NewAction() {
			super("New");
			putValue(Action.SMALL_ICON, newIcon16);
			putValue(Action.LARGE_ICON_KEY, newIcon);
			putValue(Action.SHORT_DESCRIPTION, "Create a new file");
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_MASK));
		}

		@Override
		public void actionPerformed(final ActionEvent e) {
			setFile(null);
			open();
		}
	}

	class OpenAction extends AbstractAction {
		OpenAction() {
			super("Open");
			putValue(Action.SMALL_ICON, openIcon16);
			putValue(Action.LARGE_ICON_KEY, openIcon);
			putValue(Action.SHORT_DESCRIPTION, "Open a file");
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_MASK));
		}

		@Override
		public void actionPerformed(final ActionEvent e) {
			getApplicationManager().get("Files").open(file == null?prev == null?null:prev.toUri():file.toUri(), getDesktopPane());
		}
	}

	class SaveAction extends AbstractAction {
		SaveAction() {
			super("Save");
			putValue(Action.SMALL_ICON, saveIcon16);
			putValue(Action.LARGE_ICON_KEY, saveIcon);
			putValue(Action.SHORT_DESCRIPTION, "Save to a file");
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK));
		}

		public void actionPerformed(final ActionEvent e) {
			doSave();
		}
	}

	class SaveAsAction extends AbstractAction {
		SaveAsAction() {
			super("Save as...");
			putValue(Action.SMALL_ICON, saveAsIcon16);
			putValue(Action.LARGE_ICON_KEY, saveAsIcon);
		}

		public void actionPerformed(final ActionEvent e) {
			final FileChooser chooser = getOwner().chooser;
			switch (chooser.showInternalSaveDialog(jPanel1)) {
			case JFileChooser.APPROVE_OPTION:
				save(chooser.getSelectedFile().toPath());
				break;
			default:
			}
		}
	}

	private class CloseAction extends AbstractAction {	
		public CloseAction() {
			super("Close");
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_W, InputEvent.CTRL_MASK));
		}

		@Override
		public void actionPerformed(final ActionEvent e) {
			doDefaultCloseAction();
		}
	}

	private void setFile(final Path file) {
		prev = this.file;
		this.file = file;
	}

	@Override
	public void open() {
		if (proceed()) {
			final MathDocument doc = mathTextPane1.getEditorKit().createDefaultDocument();
			mathTextPane1.getDocument().removeUndoableEditListener(undoHandler);
			mathTextPane1.setDocument(doc);
			if (file != null && Files.exists(file)) {
				try {
					getOwner().chooser.setSelectedFile(file.toFile());
				} catch (final UnsupportedOperationException e) {}
				new FileLoader().execute();
			} else {
				reset();
			}
		} else {
			file = prev;
		}
	}

	private void reset() {
		mathTextPane1.getDocument().addUndoableEditListener(undoHandler);
		resetUndoManager();
	}

	private void resetUndoManager() {
		undo.discardAllEdits();
		modified = 0;
		undoAction.update();
		redoAction.update();
	}

	private void save(final Path file) {
		if (!Files.exists(file) || file.equals(this.file) || proceed("File exists. Overwrite ?")) {
			this.file = file;
			doSave();
		}
	}

	private void doSave() {
		new FileSaver().execute();
	}

	private void update() {
		String title = file == null?getName():file.getFileName().toString();
		if (modified != 0) {
			title += " (modified)";
		}
		setTitle(title);
		saveAction.setEnabled(file != null && modified != 0);
	}

	private boolean proceed() {
		return modified == 0 || proceed("Changes not saved. Proceed ?");
	}

	private boolean proceed(final String msg) {
		switch (JOptionPane.showInternalConfirmDialog(this, msg, "Warning", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE)) {
		case JOptionPane.OK_OPTION:
			return true;
		default:
		}
		return false;
	}

	abstract class AbstractWorker extends FileWorker {
		final Cursor waitCursor = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
		final CardLayout layout = (CardLayout) jPanel3.getLayout();
		final MathDocument doc = mathTextPane1.getDocument();
		final Cursor cursor = mathTextPane1.getCursor();

		AbstractWorker() {
			addPropertyChangeListener(new PropertyChangeListener() {
				public  void propertyChange(final PropertyChangeEvent evt) {
					if ("progress".equals(evt.getPropertyName())) {
						jProgressBar1.setValue((Integer) evt.getNewValue());
					}
				}
			});
			layout.show(jPanel3, "card2");
			mathTextPane1.setCursor(waitCursor);
		}

		@Override
		public void done() {
			try {
				get();
				doDone();
			} catch (final InterruptedException e) {
				e.printStackTrace();
			} catch (final ExecutionException e) {
				e.printStackTrace();
			}
			mathTextPane1.requestFocus();
			mathTextPane1.setCursor(cursor);
			layout.show(jPanel3, "card1");
			jProgressBar1.setValue(0);
		}

		protected void doDone() {
		}
	}

	class FileLoader extends AbstractWorker {
		final MathEditorKit kit = mathTextPane1.getEditorKit();

		FileLoader() {
			try {
				setLength((int) Files.size(file));
			} catch (final IOException ex) {
				ex.printStackTrace();
			}
			kit.setWorker(this);
		}

		@Override
		public Boolean doInBackground() throws IOException, BadLocationException  {
			try (final Reader in = Files.newBufferedReader(file, Charset.defaultCharset())) {
				// try to start reading
				kit.read(in, null, 0);
			}
			return true;
		}

		@Override
		public void doDone() {
			reset();
		}
	}

	class FileSaver extends AbstractWorker {
		final MathEditorKit kit = mathTextPane1.getEditorKit();

		FileSaver() {
			try {
				setLength(doc.getText().length());
			} catch (final BadLocationException ex) {
				ex.printStackTrace();
			}
			kit.setWorker(this);
		}

		@Override
		public Boolean doInBackground() throws IOException, BadLocationException {
			try (final Writer out = Files.newBufferedWriter(file, Charset.defaultCharset())) {
				// start writing
				kit.write(out, null, 0, 0);
				out.flush();
			}
			return true;
		}

		@Override
		public void doDone() {
			modified = 0;
			update();
		}
	}

	class FileExporter extends AbstractWorker {
		final String formatting = getPref("stylesheet.formatting");
		final String stylesheet = getPref("stylesheet");
		final String feed = getPref("feed");
		final String icon = getPref("icon");
		final String extension;
		final String name;
		final File f;

		FileExporter(final File f, final String name, final String extension) {
			this.f = f;
			this.name = name;
			this.extension = extension;
		}

		@Override
		public Boolean doInBackground() throws IOException, BadLocationException {
			if("pdf".equals(extension)) try (final OutputStream out = new FileOutputStream(f)) {
				out.write(MathML.instance.exportToPDF(new StringReader(doc.getText()), formatting));
			} else try (final Writer out = new FileWriter(f)) {
				out.write(MathML.instance.exportToXHTML(new StringReader(doc.getText()), stylesheet, name, feed, icon));
			}
			return true;
		}
	}

	class EvalWorker extends SwingWorker<Object, Object> {
		final Cursor cursor = mathTextPane1.getCursor();
		final String selected = mathTextPane1.getSelectedText();
		final Cursor waitCursor = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
		final boolean newline;
		final boolean eval;
		final String data;

		EvalWorker(final boolean eval) {
			mathTextPane1.setCursor(waitCursor);
			data = selected == null?eval?"":mathTextPane1.getText():selected;
			final int n = data.length();
			newline = n > 0 && "\n".equals(data.substring(n - 1));
			this.eval = eval;
		}

		@Override
		public Object doInBackground() throws ScriptException, IOException  {
			return getEngine().eval(code(data));
		}

		@Override
		public void done() {
			try {
				final Object result = get();
				if (eval) {
					if (newline) {
						unselect();
					}
					replace(result);
				} else {
					unselect();
				}
			} catch (final InterruptedException e) {
				e.printStackTrace();
			} catch (final ExecutionException e) {
				e.printStackTrace();
			}
			mathTextPane1.setCursor(cursor);
		}

		private void replace(final Object result) {
			mathTextPane1.replaceSelection(render(result));
		}

		private void unselect() {
			int n = mathTextPane1.getCaretPosition();
			mathTextPane1.setSelectionStart(n);
			mathTextPane1.setSelectionEnd(n);
		}
	}

	private String render(final Object obj) {
		if(obj == null) return "null";
		if(obj instanceof Component) switch (JOptionPane.showInternalConfirmDialog(this, obj, obj.getClass().getSimpleName(), JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE)) {
		case JOptionPane.OK_OPTION:
			return SVG.instance.print((Component) obj);
		default:
		}
		if(obj instanceof JXGraph.Plot) {
			return render(new Graph((JXGraph.Plot) obj));
		}
		if(obj instanceof JXGraph.Plot[]) {
			return render(new Graph((JXGraph.Plot[]) obj));
		}
		if (isRendering()) {
			if(obj instanceof MathObject) {
				return "<math>" + ((MathObject) obj).toMathML() + "</math>";
			}
			try {
				final Method m = obj.getClass().getMethod("toMathML");
				return render(new MathObject() {
					public String toMathML() {
						try {
							return (String) m.invoke(obj);
						} catch (final ReflectiveOperationException ex) {}
						return null;
					}
				});
			} catch (final NoSuchMethodException ex) {}
		}
		return obj.toString();
	}

	private String code(final String str) throws IOException {
		final String name = getEngine().getFactory().getNames().get(0);
		final String stylesheet = getPref(getKey(name, "stylesheet"));
		if (!stylesheet.isEmpty()) {
			return Code.instance(stylesheet).apply(new StringReader(str));
		}
		return str;
	}

	@Override
	public ScriptEngine getEngine() {
		if (engine == null) try {
			engine = super.getEngine();
			final String name = engine.getFactory().getNames().get(0);
			final String init = getPref(getKey(name, "init")).trim();
			if (init.length() > 0) {
				engine.eval(init);
			}
		} catch (final ScriptException e) {
			e.printStackTrace();
		}
		return engine;
	}

	private boolean isRendering() {
		final String name = getEngine().getFactory().getNames().get(0);
		return getBooleanPref(getKey(name, "rendering"));
	}

	private String getKey(final String name, final String str) {
		return name + "." + str;
	}

	public Editor() {
		initComponents();
		setDescription("Java symbolic computing library and mathematical editor");
		setIcon(new ImageIcon(getClass().getResource("meditor24.png")));
		setMimeType("text/plain:text/*");
		chooser.setFileFilter(new FileNameExtensionFilter("Text", "txt"));
		cutAction.putValue(Action.NAME, "Cut");
		cutAction.putValue(Action.SMALL_ICON, cutIcon16);
		cutAction.putValue(Action.LARGE_ICON_KEY, cutIcon);
		cutAction.putValue(Action.SHORT_DESCRIPTION, "Move selection to clipboard");
		cutAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.CTRL_MASK));
		copyAction.putValue(Action.NAME, "Copy");
		copyAction.putValue(Action.SMALL_ICON, copyIcon16);
		copyAction.putValue(Action.LARGE_ICON_KEY, copyIcon);
		copyAction.putValue(Action.SHORT_DESCRIPTION, "Copy selection to clipboard");
		copyAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_MASK));
		pasteAction.putValue(Action.NAME, "Paste");
		pasteAction.putValue(Action.SMALL_ICON, pasteIcon16);
		pasteAction.putValue(Action.LARGE_ICON_KEY, pasteIcon);
		pasteAction.putValue(Action.SHORT_DESCRIPTION, "Paste clipboard to selection");
		pasteAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_MASK));
		exportChooser.removeChoosableFileFilter(exportChooser.getAcceptAllFileFilter());
		exportChooser.addChoosableFileFilter(new MathFileFilter("pdf"));
		exportChooser.addChoosableFileFilter(new MathFileFilter("xhtml"));
	}

	@Override
	public Component getFocusOwner() {
		return mathTextPane1;
	}

	@Override
	public Editor getOwner() {
		return (Editor) super.getOwner();
	}

	@Override
	public void setURI(final URI uri) {
		setFile(getPath(uri));
	}

	@Override
	public URI getURI() {
		return file == null?null:file.toUri();
	}

	@Override
	public Editor getFrame() {
		return new Editor();
	}

	@Override
	public void load() {
		super.load();
		loadEngine();
		jTextField4.setText(getPref("stylesheet"));
		jTextField5.setText(getPref("feed"));
		jTextField6.setText(getPref("icon"));
		jTextField7.setText(getPref("stylesheet.formatting"));
	}

	private void loadEngine() {
		name = getSelectedLanguage();
		jTextArea1.setText(getPref(getKey(name, "init")));
		jTextField3.setText(getPref(getKey(name, "stylesheet")));
		jCheckBox1.setSelected(getBooleanPref(getKey(name, "rendering")));
	}

	@Override
	public void save() {
		super.save();
		saveEngine();
		putPref("stylesheet", jTextField4.getText());
		putPref("feed", jTextField5.getText());
		putPref("icon", jTextField6.getText());
		putPref("stylesheet.formatting", jTextField7.getText());
	}

	private void saveEngine() {
		if (name != null) {
			putPref(getKey(name, "init"), jTextArea1.getText());
			putPref(getKey(name, "stylesheet"), jTextField3.getText());
			putBooleanPref(getKey(name, "rendering"), jCheckBox1.isSelected());
		}
	}

	@SuppressWarnings("unchecked")
        // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
        private void initComponents() {

                jPanel2 = new javax.swing.JPanel();
                jLabel1 = new javax.swing.JLabel();
                jTextField1 = new javax.swing.JTextField();
                jLabel2 = new javax.swing.JLabel();
                jTextField2 = new javax.swing.JTextField();
                jButton1 = new javax.swing.JButton();
                jButton2 = new javax.swing.JButton();
                jButton3 = new javax.swing.JButton();
                jButton4 = new javax.swing.JButton();
                optionPanel1 = new linoleum.application.OptionPanel();
                jTabbedPane1 = new javax.swing.JTabbedPane();
                jPanel4 = new javax.swing.JPanel();
                jLabel4 = new javax.swing.JLabel();
                jComboBox1 = new javax.swing.JComboBox();
                jLabel5 = new javax.swing.JLabel();
                jScrollPane2 = new javax.swing.JScrollPane();
                jTextArea1 = new javax.swing.JTextArea();
                jLabel6 = new javax.swing.JLabel();
                jTextField3 = new javax.swing.JTextField();
                jCheckBox1 = new javax.swing.JCheckBox();
                jPanel5 = new javax.swing.JPanel();
                jLabel7 = new javax.swing.JLabel();
                jTextField4 = new javax.swing.JTextField();
                jLabel8 = new javax.swing.JLabel();
                jTextField5 = new javax.swing.JTextField();
                jTextField6 = new javax.swing.JTextField();
                jLabel9 = new javax.swing.JLabel();
                jPanel6 = new javax.swing.JPanel();
                jLabel10 = new javax.swing.JLabel();
                jTextField7 = new javax.swing.JTextField();
                jToolBar1 = new javax.swing.JToolBar();
                jButton5 = new javax.swing.JButton();
                jButton6 = new javax.swing.JButton();
                jButton7 = new javax.swing.JButton();
                filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(5, 0), new java.awt.Dimension(5, 0), new java.awt.Dimension(5, 32767));
                jButton8 = new javax.swing.JButton();
                jButton9 = new javax.swing.JButton();
                jButton10 = new javax.swing.JButton();
                filler2 = new javax.swing.Box.Filler(new java.awt.Dimension(5, 0), new java.awt.Dimension(5, 0), new java.awt.Dimension(5, 32767));
                jButton11 = new javax.swing.JButton();
                jButton12 = new javax.swing.JButton();
                jPanel1 = new javax.swing.JPanel();
                jScrollPane1 = new javax.swing.JScrollPane();
                mathTextPane1 = new jscl.editor.MathTextPane();
                jPanel3 = new javax.swing.JPanel();
                jLabel3 = new javax.swing.JLabel();
                jProgressBar1 = new javax.swing.JProgressBar();
                jMenuBar1 = new javax.swing.JMenuBar();
                jMenu1 = new javax.swing.JMenu();
                jMenuItem9 = new javax.swing.JMenuItem();
                jMenuItem10 = new javax.swing.JMenuItem();
                jMenuItem11 = new javax.swing.JMenuItem();
                jMenuItem12 = new javax.swing.JMenuItem();
                jSeparator3 = new javax.swing.JPopupMenu.Separator();
                jMenuItem1 = new javax.swing.JMenuItem();
                jMenu2 = new javax.swing.JMenu();
                jMenuItem2 = new javax.swing.JMenuItem();
                jMenuItem3 = new javax.swing.JMenuItem();
                jMenuItem4 = new javax.swing.JMenuItem();
                jSeparator1 = new javax.swing.JPopupMenu.Separator();
                jMenuItem5 = new javax.swing.JMenuItem();
                jMenuItem6 = new javax.swing.JMenuItem();
                jSeparator2 = new javax.swing.JPopupMenu.Separator();
                jMenuItem7 = new javax.swing.JMenuItem();
                jMenuItem8 = new javax.swing.JMenuItem();
                jMenu3 = new javax.swing.JMenu();
                jMenuItem13 = new javax.swing.JMenuItem();
                jMenuItem14 = new javax.swing.JMenuItem();
                jSeparator4 = new javax.swing.JPopupMenu.Separator();
                jMenuItem16 = new javax.swing.JMenuItem();
                jMenuItem17 = new javax.swing.JMenuItem();
                jSeparator6 = new javax.swing.JPopupMenu.Separator();
                jMenuItem18 = new javax.swing.JMenuItem();
                jSeparator7 = new javax.swing.JPopupMenu.Separator();
                jMenuItem19 = new javax.swing.JMenuItem();
                jSeparator8 = new javax.swing.JPopupMenu.Separator();
                jMenuItem20 = new javax.swing.JMenuItem();

                jLabel1.setText("Find :");

                jLabel2.setText("Replace with :");

                jButton1.setText("Next");
                jButton1.addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                                jButton1ActionPerformed(evt);
                        }
                });

                jButton2.setText("Replace");
                jButton2.addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                                jButton2ActionPerformed(evt);
                        }
                });

                jButton3.setText("Replace all");
                jButton3.addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                                jButton3ActionPerformed(evt);
                        }
                });

                jButton4.setText("Done");
                jButton4.addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                                jButton4ActionPerformed(evt);
                        }
                });

                javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
                jPanel2.setLayout(jPanel2Layout);
                jPanel2Layout.setHorizontalGroup(
                        jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel2Layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(jPanel2Layout.createSequentialGroup()
                                                .addComponent(jLabel1)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(jTextField1)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(jButton1)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(jButton3))
                                        .addGroup(jPanel2Layout.createSequentialGroup()
                                                .addComponent(jLabel2)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(jTextField2, javax.swing.GroupLayout.DEFAULT_SIZE, 153, Short.MAX_VALUE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(jButton2)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(jButton4)))
                                .addContainerGap())
                );
                jPanel2Layout.setVerticalGroup(
                        jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel2Layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel1)
                                        .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jButton1)
                                        .addComponent(jButton3))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel2)
                                        .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jButton2)
                                        .addComponent(jButton4))
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                );

                jLabel4.setText("Language :");

                jComboBox1.setModel(getModel());
                jComboBox1.addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                                jComboBox1ActionPerformed(evt);
                        }
                });

                jLabel5.setText("Init :");

                jTextArea1.setColumns(20);
                jTextArea1.setRows(5);
                jScrollPane2.setViewportView(jTextArea1);

                jLabel6.setText("Stylesheet :");

                jCheckBox1.setText("Rendering");
                jCheckBox1.addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                                jCheckBox1ActionPerformed(evt);
                        }
                });

                javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
                jPanel4.setLayout(jPanel4Layout);
                jPanel4Layout.setHorizontalGroup(
                        jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel4Layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                .addComponent(jLabel6)
                                                .addComponent(jLabel4, javax.swing.GroupLayout.Alignment.TRAILING))
                                        .addComponent(jLabel5))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(jPanel4Layout.createSequentialGroup()
                                                .addComponent(jCheckBox1)
                                                .addGap(0, 0, Short.MAX_VALUE))
                                        .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 279, Short.MAX_VALUE)
                                        .addComponent(jTextField3)
                                        .addComponent(jComboBox1, 0, 279, Short.MAX_VALUE))
                                .addContainerGap())
                );
                jPanel4Layout.setVerticalGroup(
                        jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel4Layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel4)
                                        .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(jPanel4Layout.createSequentialGroup()
                                                .addComponent(jLabel5)
                                                .addGap(0, 0, Short.MAX_VALUE))
                                        .addComponent(jScrollPane2))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel6)
                                        .addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jCheckBox1)
                                .addContainerGap())
                );

                jTabbedPane1.addTab("Engine", jPanel4);

                jLabel7.setText("Stylesheet :");

                jLabel8.setText("Feed :");

                jLabel9.setText("Icon :");

                javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
                jPanel5.setLayout(jPanel5Layout);
                jPanel5Layout.setHorizontalGroup(
                        jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel5Layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addComponent(jLabel8)
                                        .addComponent(jLabel7)
                                        .addComponent(jLabel9))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jTextField4, javax.swing.GroupLayout.DEFAULT_SIZE, 273, Short.MAX_VALUE)
                                        .addComponent(jTextField5)
                                        .addComponent(jTextField6))
                                .addContainerGap())
                );
                jPanel5Layout.setVerticalGroup(
                        jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel5Layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel7)
                                        .addComponent(jTextField4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jTextField5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jLabel8))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jTextField6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jLabel9))
                                .addContainerGap(115, Short.MAX_VALUE))
                );

                jTabbedPane1.addTab("XHTML", jPanel5);

                jLabel10.setText("Stylesheet :");

                javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
                jPanel6.setLayout(jPanel6Layout);
                jPanel6Layout.setHorizontalGroup(
                        jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel6Layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(jLabel10)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jTextField7, javax.swing.GroupLayout.DEFAULT_SIZE, 273, Short.MAX_VALUE)
                                .addContainerGap())
                );
                jPanel6Layout.setVerticalGroup(
                        jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel6Layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel10)
                                        .addComponent(jTextField7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addContainerGap(201, Short.MAX_VALUE))
                );

                jTabbedPane1.addTab("PDF", jPanel6);

                javax.swing.GroupLayout optionPanel1Layout = new javax.swing.GroupLayout(optionPanel1);
                optionPanel1.setLayout(optionPanel1Layout);
                optionPanel1Layout.setHorizontalGroup(
                        optionPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jTabbedPane1, javax.swing.GroupLayout.Alignment.TRAILING)
                );
                optionPanel1Layout.setVerticalGroup(
                        optionPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jTabbedPane1)
                );

                setClosable(true);
                setIconifiable(true);
                setMaximizable(true);
                setResizable(true);
                setTitle("meditor");
                setFrameIcon(new javax.swing.ImageIcon(getClass().getResource("/jscl/editor/meditor.png"))); // NOI18N
                setName("meditor"); // NOI18N
                setOptionPanel(optionPanel1);
                addVetoableChangeListener(new java.beans.VetoableChangeListener() {
                        public void vetoableChange(java.beans.PropertyChangeEvent evt)throws java.beans.PropertyVetoException {
                                formVetoableChange(evt);
                        }
                });

                jToolBar1.setRollover(true);

                jButton5.setAction(newAction);
                jButton5.setFocusable(false);
                jButton5.setHideActionText(true);
                jToolBar1.add(jButton5);

                jButton6.setAction(openAction);
                jButton6.setFocusable(false);
                jButton6.setHideActionText(true);
                jToolBar1.add(jButton6);

                jButton7.setAction(saveAction);
                jButton7.setFocusable(false);
                jButton7.setHideActionText(true);
                jToolBar1.add(jButton7);
                jToolBar1.add(filler1);

                jButton8.setAction(cutAction);
                jButton8.setFocusable(false);
                jButton8.setHideActionText(true);
                jToolBar1.add(jButton8);

                jButton9.setAction(copyAction);
                jButton9.setFocusable(false);
                jButton9.setHideActionText(true);
                jToolBar1.add(jButton9);

                jButton10.setAction(pasteAction);
                jButton10.setFocusable(false);
                jButton10.setHideActionText(true);
                jToolBar1.add(jButton10);
                jToolBar1.add(filler2);

                jButton11.setAction(runAction);
                jButton11.setFocusable(false);
                jButton11.setHideActionText(true);
                jToolBar1.add(jButton11);

                jButton12.setAction(evalAction);
                jButton12.setFocusable(false);
                jButton12.setHideActionText(true);
                jToolBar1.add(jButton12);

                jPanel1.setLayout(new java.awt.BorderLayout());

                mathTextPane1.addCaretListener(new javax.swing.event.CaretListener() {
                        public void caretUpdate(javax.swing.event.CaretEvent evt) {
                                mathTextPane1CaretUpdate(evt);
                        }
                });
                jScrollPane1.setViewportView(mathTextPane1);

                jPanel1.add(jScrollPane1, java.awt.BorderLayout.CENTER);

                jPanel3.setLayout(new java.awt.CardLayout());
                jPanel3.add(jLabel3, "card1");
                jPanel3.add(jProgressBar1, "card2");

                jMenu1.setText("File");

                jMenuItem9.setAction(newAction);
                jMenu1.add(jMenuItem9);

                jMenuItem10.setAction(openAction);
                jMenu1.add(jMenuItem10);

                jMenuItem11.setAction(saveAction);
                jMenu1.add(jMenuItem11);

                jMenuItem12.setAction(saveAsAction);
                jMenu1.add(jMenuItem12);
                jMenu1.add(jSeparator7);

                jMenuItem19.setAction(exportAction);
                jMenu1.add(jMenuItem19);
                jMenu1.add(jSeparator3);

                jMenuItem1.setAction(closeAction);
                jMenu1.add(jMenuItem1);

                jMenuBar1.add(jMenu1);

                jMenu2.setText("Edit");

                jMenuItem2.setAction(cutAction);
                jMenu2.add(jMenuItem2);

                jMenuItem3.setAction(copyAction);
                jMenu2.add(jMenuItem3);

                jMenuItem4.setAction(pasteAction);
                jMenu2.add(jMenuItem4);
                jMenu2.add(jSeparator1);

                jMenuItem5.setAction(undoAction);
                jMenu2.add(jMenuItem5);

                jMenuItem6.setAction(redoAction);
                jMenu2.add(jMenuItem6);
                jMenu2.add(jSeparator2);

                jMenuItem7.setAction(findAction);
                jMenu2.add(jMenuItem7);

                jMenuItem8.setAction(replaceAction);
                jMenu2.add(jMenuItem8);

                jMenuBar1.add(jMenu2);

                jMenu3.setText("Math");

                jMenuItem13.setAction(runAction);
                jMenu3.add(jMenuItem13);

                jMenuItem14.setAction(evalAction);
                jMenu3.add(jMenuItem14);
                jMenu3.add(jSeparator4);

                jMenuItem16.setAction(copyToWikiAction);
                jMenu3.add(jMenuItem16);

                jMenuItem17.setAction(pasteFromWikiAction);
                jMenu3.add(jMenuItem17);
                jMenu3.add(jSeparator6);

                jMenuItem18.setAction(copyToCodeAction);
                jMenu3.add(jMenuItem18);
                jMenu3.add(jSeparator8);

                jMenuItem20.setAction(preferenceAction);
                jMenu3.add(jMenuItem20);

                jMenuBar1.add(jMenu3);

                setJMenuBar(jMenuBar1);

                javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
                getContentPane().setLayout(layout);
                layout.setHorizontalGroup(
                        layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jToolBar1, javax.swing.GroupLayout.DEFAULT_SIZE, 384, Short.MAX_VALUE)
                        .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                );
                layout.setVerticalGroup(
                        layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addComponent(jToolBar1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 252, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                );

                pack();
        }// </editor-fold>//GEN-END:initComponents

        private void jComboBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox1ActionPerformed
		saveEngine();
		loadEngine();
		optionPanel1.setDirty(true);
        }//GEN-LAST:event_jComboBox1ActionPerformed

        private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
		found = mathTextPane1.findNext(jTextField1.getText(), true);
		mathTextPane1.requestFocus();
        }//GEN-LAST:event_jButton1ActionPerformed

        private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
		if (found) {
			found = mathTextPane1.replace(jTextField1.getText(), jTextField2.getText());
		}
		mathTextPane1.requestFocus();
        }//GEN-LAST:event_jButton2ActionPerformed

        private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
		mathTextPane1.replaceAll(jTextField1.getText(), jTextField2.getText());
		mathTextPane1.requestFocus();
        }//GEN-LAST:event_jButton3ActionPerformed

        private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
		toggleDialog();
		mathTextPane1.requestFocus();
        }//GEN-LAST:event_jButton4ActionPerformed

        private void formVetoableChange(java.beans.PropertyChangeEvent evt)throws java.beans.PropertyVetoException {//GEN-FIRST:event_formVetoableChange
		if (IS_CLOSED_PROPERTY.equals(evt.getPropertyName()) && (Boolean) evt.getNewValue()) {
			setFile(null);
			open();
			if (modified != 0) {
				throw new PropertyVetoException("aborted", evt);
			}
		}
        }//GEN-LAST:event_formVetoableChange

        private void mathTextPane1CaretUpdate(javax.swing.event.CaretEvent evt) {//GEN-FIRST:event_mathTextPane1CaretUpdate
		try {
			final int pos = mathTextPane1.getCaretPosition();
			final int line = mathTextPane1.getLineOfOffset(pos);
			final int column = pos - mathTextPane1.getLineStartOffset(line);
			jLabel3.setText((line + 1) + "," + (column + 1));
		} catch (final BadLocationException ex) {
			ex.printStackTrace();
		}
        }//GEN-LAST:event_mathTextPane1CaretUpdate

        private void jCheckBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox1ActionPerformed
		optionPanel1.setDirty(true);
        }//GEN-LAST:event_jCheckBox1ActionPerformed

        // Variables declaration - do not modify//GEN-BEGIN:variables
        private javax.swing.Box.Filler filler1;
        private javax.swing.Box.Filler filler2;
        private javax.swing.JButton jButton1;
        private javax.swing.JButton jButton10;
        private javax.swing.JButton jButton11;
        private javax.swing.JButton jButton12;
        private javax.swing.JButton jButton2;
        private javax.swing.JButton jButton3;
        private javax.swing.JButton jButton4;
        private javax.swing.JButton jButton5;
        private javax.swing.JButton jButton6;
        private javax.swing.JButton jButton7;
        private javax.swing.JButton jButton8;
        private javax.swing.JButton jButton9;
        private javax.swing.JCheckBox jCheckBox1;
        private javax.swing.JComboBox jComboBox1;
        private javax.swing.JLabel jLabel1;
        private javax.swing.JLabel jLabel10;
        private javax.swing.JLabel jLabel2;
        private javax.swing.JLabel jLabel3;
        private javax.swing.JLabel jLabel4;
        private javax.swing.JLabel jLabel5;
        private javax.swing.JLabel jLabel6;
        private javax.swing.JLabel jLabel7;
        private javax.swing.JLabel jLabel8;
        private javax.swing.JLabel jLabel9;
        private javax.swing.JMenu jMenu1;
        private javax.swing.JMenu jMenu2;
        private javax.swing.JMenu jMenu3;
        private javax.swing.JMenuBar jMenuBar1;
        private javax.swing.JMenuItem jMenuItem1;
        private javax.swing.JMenuItem jMenuItem10;
        private javax.swing.JMenuItem jMenuItem11;
        private javax.swing.JMenuItem jMenuItem12;
        private javax.swing.JMenuItem jMenuItem13;
        private javax.swing.JMenuItem jMenuItem14;
        private javax.swing.JMenuItem jMenuItem16;
        private javax.swing.JMenuItem jMenuItem17;
        private javax.swing.JMenuItem jMenuItem18;
        private javax.swing.JMenuItem jMenuItem19;
        private javax.swing.JMenuItem jMenuItem20;
        private javax.swing.JMenuItem jMenuItem2;
        private javax.swing.JMenuItem jMenuItem3;
        private javax.swing.JMenuItem jMenuItem4;
        private javax.swing.JMenuItem jMenuItem5;
        private javax.swing.JMenuItem jMenuItem6;
        private javax.swing.JMenuItem jMenuItem7;
        private javax.swing.JMenuItem jMenuItem8;
        private javax.swing.JMenuItem jMenuItem9;
        private javax.swing.JPanel jPanel1;
        private javax.swing.JPanel jPanel2;
        private javax.swing.JPanel jPanel3;
        private javax.swing.JPanel jPanel4;
        private javax.swing.JPanel jPanel5;
        private javax.swing.JPanel jPanel6;
        private javax.swing.JProgressBar jProgressBar1;
        private javax.swing.JScrollPane jScrollPane1;
        private javax.swing.JScrollPane jScrollPane2;
        private javax.swing.JPopupMenu.Separator jSeparator1;
        private javax.swing.JPopupMenu.Separator jSeparator2;
        private javax.swing.JPopupMenu.Separator jSeparator3;
        private javax.swing.JPopupMenu.Separator jSeparator4;
        private javax.swing.JPopupMenu.Separator jSeparator6;
        private javax.swing.JPopupMenu.Separator jSeparator7;
        private javax.swing.JPopupMenu.Separator jSeparator8;
        private javax.swing.JTabbedPane jTabbedPane1;
        private javax.swing.JTextArea jTextArea1;
        private javax.swing.JTextField jTextField1;
        private javax.swing.JTextField jTextField2;
        private javax.swing.JTextField jTextField3;
        private javax.swing.JTextField jTextField4;
        private javax.swing.JTextField jTextField5;
        private javax.swing.JTextField jTextField6;
        private javax.swing.JTextField jTextField7;
        private javax.swing.JToolBar jToolBar1;
        private jscl.editor.MathTextPane mathTextPane1;
        private linoleum.application.OptionPanel optionPanel1;
        // End of variables declaration//GEN-END:variables
}
