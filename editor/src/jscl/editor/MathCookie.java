package jscl.editor;

import java.io.IOException;
import org.openide.nodes.Node;

public interface MathCookie extends Node.Cookie {
	public void find();
	public void replace();
	public void run() throws Exception;
	public void evaluate() throws Exception;
	public void copyToWiki() throws Exception;
	public void pasteFromWiki() throws Exception;
	public void copyToCode() throws Exception;
	public void export() throws IOException;
}
