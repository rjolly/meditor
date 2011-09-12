/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jscl.webdav;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import org.openide.filesystems.FileSystem;
import org.openide.windows.WindowManager;

/**
 *
 * @author raphael
 */
public class DavManager {
    private static final DavManager manager = new DavManager();
    private final Map<String, FileSystem> map = new HashMap<String, FileSystem>();

    public static DavManager getDefault() {
        return manager;
    }

    public FileSystem get(URL url) {
        String s = url.toString();
        for (String fs : map.keySet()) {
            if (s.startsWith(fs)) {
                return map.get(fs);
            }
        }
        return null;
    }

    public boolean contains(FileSystem fs) {
        return map.values().contains(fs);
    }

    public boolean add(URL url) {
        Login auth = new Login(WindowManager.getDefault().getMainWindow(), true, url);
        if (auth.login()) {
            map.put(url.toString(), new DavFileSystem(url, auth.getUsername(), auth.getPassword(), auth.isReadOnly()));
            return true;
        }
        return false;
    }

    public void remove(URL url) {
        map.remove(url.toString());
    }
}
