/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jscl.webdav;

import java.io.IOException;
import java.net.URL;
import java.util.logging.Logger;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.URLMapper;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author raphael
 */
@ServiceProvider(service=URLMapper.class)
public class DavURLMapper extends URLMapper {
    private static final Logger LOG = Logger.getLogger(DavURLMapper.class.getName());
    private final DavManager manager = DavManager.getDefault();

    @Override
    public FileObject[] getFileObjects(URL url) {
        if ("http".equals(url.getProtocol()) || "https".equals(url.getProtocol())) {
            FileSystem fs = manager.get(url);
            if (fs != null) return new FileObject[]{fs.getRoot().getFileObject(url.toString().substring(fs.getDisplayName().length()))};
        }
        return null;
    }

    @Override
    public URL getURL(FileObject fo, int type) {
        try {
            FileSystem fs = fo.getFileSystem();
            return manager.contains(fs)?new URL(fs.getDisplayName() + "/" + fo.getPath()):null;
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
