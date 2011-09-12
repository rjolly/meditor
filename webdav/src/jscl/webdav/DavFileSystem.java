package jscl.webdav;

import com.googlecode.sardine.DavResource;
import com.googlecode.sardine.Sardine;
import com.googlecode.sardine.SardineFactory;
import com.googlecode.sardine.impl.SardineImpl;
import java.beans.PropertyVetoException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;
import org.apache.http.entity.ByteArrayEntity;
import org.openide.filesystems.AbstractFileSystem;

final class DavFileSystem extends AbstractFileSystem implements AbstractFileSystem.Info, AbstractFileSystem.Change, AbstractFileSystem.List, AbstractFileSystem.Attr {
    private static final Logger ERR = Logger.getLogger(DavFileSystem.class.getName());
    
    private final Sardine sardine;
    private final boolean readOnly;
    private final URL url;
    
    @SuppressWarnings("deprecation") // need to set it for compat
    private void _setSystemName(String s) throws PropertyVetoException {
        setSystemName(s);
    }

    public DavFileSystem(URL url, String username, String password, boolean readOnly) {
        attr = this;
        list = this;
        change = this;
        info = this;
        this.url = url;
        this.readOnly = readOnly;
        ClassLoader orig = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(Object.class.getClassLoader());
        try {
            sardine = readOnly?SardineFactory.begin():SardineFactory.begin(username, password);
            _setSystemName(url.toString());
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        } finally {
            Thread.currentThread().setContextClassLoader(orig);
        }
    }

    public URL getURL() {
        return url;
    }

    public String getProtocol() {
        return url.getProtocol();
    }

    public String getHost() {
        return url.getHost();
    }

    public int getPort() {
        return url.getPort();
    }

    public String getPath() {
        return url.getPath();
    }

    public String getDisplayName() {
        return url.toString();
    }

    public boolean isReadOnly() {
        return this.readOnly;
    }

    public Enumeration<String> attributes(String name) {
        return org.openide.util.Enumerations.empty();
    }

    public String[] children(String f) {
        if ((f.length() > 0) && (f.charAt(0) == '/')) {
            f = f.substring(1);
        }

        if ((f.length() > 0) && !f.endsWith("/")) {
            f = f + "/";
        }

        Set<String> l = new HashSet<String>();

        //System.out.println("Folder: " + f);
        try {
            java.util.List<DavResource> list = sardine.list(url + "/" + f);
            for (DavResource res : list.subList(1, list.size())) {
                l.add(res.getName());
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }

        return l.toArray(new String[0]);
    }

    public void createData(String name) throws IOException {
        if (sardine.exists(url + "/" + name)) {
	    StringBuffer message = new StringBuffer();
	    message.append("File already exists: ").append(name);
            throw new IOException(message.toString());//NOI18N
        }
 	((SardineImpl)sardine).put(url + "/" + name, new ByteArrayEntity(new byte[] {}), (String) null, false);
    }

    public void createFolder(String name) throws java.io.IOException {
        if (sardine.exists(url + "/" + name)) {
	    StringBuffer message = new StringBuffer();
	    message.append("Folder already exists: ").append(name);
            throw new IOException(message.toString());//NOI18N
        }
        sardine.createDirectory(url+"/"+name);
    }

    public void delete(String name) throws IOException {
        sardine.delete(url+"/"+name);
    }

    public void deleteAttributes(String name) {
    }

    public boolean folder(String name) {
        try {
            return sardine.list(url + "/" + name).get(0).isDirectory();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public InputStream inputStream(String name) throws java.io.FileNotFoundException {
        try {
            return sardine.get(url + "/" + name);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public java.util.Date lastModified(String name) {
        ClassLoader orig = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(Object.class.getClassLoader());
        try {
            return sardine.list(url + "/" + name).get(0).getModified();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        } finally {
            Thread.currentThread().setContextClassLoader(orig);
        }
    }

    public void lock(String name) throws IOException {
    }

    public void markUnimportant(String name) {
    }

    public String mimeType(String name) {
        try {
            return sardine.list(url + "/" + name).get(0).getContentType();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public OutputStream outputStream(final String name) throws java.io.IOException {
        class Out extends ByteArrayOutputStream {
            public void close() throws IOException {
                super.close();

		((SardineImpl)sardine).put(url + "/" + name, new ByteArrayEntity(toByteArray()), (String) null, false);
            }
        }

        return new Out();
    }

    public Object readAttribute(String name, String attrName) {
        return null;
    }

    public boolean readOnly(String name) {
        return readOnly;
    }

    public void rename(String oldName, String newName) throws IOException {
        if (!sardine.exists(url + "/" + oldName)) {
            throw new IOException("The file to rename does not exist.");
        }

        if (sardine.exists(url + "/" + newName)) {
            throw new IOException("Cannot rename to existing file");
        }

        if ((newName.length() > 0) && (newName.charAt(0) == '/')) {
            newName = newName.substring(1);
        }

        sardine.move(url + "/" + oldName, url + "/" +newName);
    }

    public void renameAttributes(String oldName, String newName) {
    }

    public long size(String name) {
        try {
            return sardine.list(url + "/" + name).get(0).getContentLength();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void unlock(String name) {
    }

    public void writeAttribute(String name, String attrName, Object value) throws IOException {
    }
}
