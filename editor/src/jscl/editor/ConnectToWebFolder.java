/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jscl.editor;

import jscl.webdav.DavManager;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.URL;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

@ActionID(category = "Math",
id = "jscl.editor.ConnectToWebFolder")
@ActionRegistration(displayName = "#CTL_ConnectToWebFolder")
@ActionReferences({
	@ActionReference(path = "Menu/Math", position = 0)
})
public final class ConnectToWebFolder extends SystemAction {
	static DataObject docs;

	public void actionPerformed(ActionEvent e) {
		final URL location = MathManager.getDefault().getLocation();
		final DataFolder f = getFolder();
		try {
			if (docs == null) {
				if (DavManager.getDefault().add(location)) {
					final FileObject fo = URLMapper.findFileObject(location);
					docs = DataObject.find(fo.getFileObject("/")).createShadow(f);
					String name = name(location);
					if (name.length() > 0) {
						name = collision(f, name);
						docs.rename(name);
					}
					openTab();
				}
			} else {
				docs.delete();
				docs = null;
				DavManager.getDefault().remove(location);
			}
			putValue(NAME, getName());
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	String name(URL location) {
		String name = location.getPath();
		int n = name.lastIndexOf('/');
		return name.substring(n + 1);
	}

	String collision(DataFolder f, String name) {
		int sequence = 0;
		boolean flag = true;
		DataObject child[] = f.getChildren();
		while (flag) {
			flag = false;
			for (DataObject obj : child) {
				if (name.equals(obj.getName())) {
					sequence++;
					name = "docs_" + sequence;
					flag = true;
					break;
				}
			}
		}
		return name;
	}

	public static String getIconBase() {
		return "jscl/editor/webProjectIcon.png";
	}

	@Override
	protected String iconResource() {
		return getIconBase();
	}

	private static DataFolder getFolder() {
		try {
			final FileObject fo = FileUtil.createFolder(FileUtil.getConfigRoot(), "Favorites"); // NOI18N
			final DataFolder folder = DataFolder.findFolder(fo);
			return folder;
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	private static void openTab() {
		final TopComponent tc = WindowManager.getDefault().findTopComponent("favorites"); // NOI18N
		tc.open();
		tc.requestActive();
	}

	public static String getDisplayName() {
		return NbBundle.getMessage(ConnectToWebFolder.class, docs == null ? "CTL_ConnectToWebFolder" : "CTL_DisconnectFromWebFolder"); // NOI18N
	}

	@Override
	public String getName() {
		return getDisplayName();
	}

	@Override
	public HelpCtx getHelpCtx() {
		return new HelpCtx(ConnectToWebFolder.class);
	}
}
