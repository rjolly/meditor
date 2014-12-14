/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jscl.converter;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.util.Map;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

/**
 *
 * @author raphael
 */
public class Converter {
	public static final Pattern pattern=Pattern.compile("(?s:<math.*?/math\n?>)|(?s:<svg.*?/svg\n?>)");
	public static final String XML="<?xml version=\"1.0\" encoding=\"utf-8\" standalone=\"no\"?>";
	static final Pattern links=Pattern.compile("(https?://[\\w_\\-\\.:/~\\?=&#]*)|([\\w_\\-\\./]*\\.txt)");
	static final Pattern newlines=Pattern.compile("\n");
	static final Pattern spaces=Pattern.compile("(?m:^ +)|(  +)|(\\t)");
	static final String mml=" xmlns=\"http://www.w3.org/1998/Math/MathML\"";
	static final String svg=" xmlns=\"http://www.w3.org/2000/svg\"";
	static final Map<String, Converter> cache = new HashMap<String, Converter>();
	public final Transformer transformer;

	private Converter(final Transformer transformer) {
		this.transformer = transformer;
	}

	public static Converter instance(String stylesheet) throws Exception {
		if(stylesheet.startsWith("/")) stylesheet = stylesheet.substring(1);
		if(!cache.containsKey(stylesheet)) {
			URL resource = Thread.currentThread().getContextClassLoader().getResource(stylesheet);
			if (resource == null) throw new Exception("Stylesheet not found: \"" + stylesheet + "\"");
			cache.put(stylesheet, new Converter(TransformerFactory.newInstance().newTransformer(new StreamSource(resource.toString()))));
		}
 		return cache.get(stylesheet);
 	}

	public static String read(Reader reader) throws IOException {
		Writer w = new StringWriter();
		pipe(reader, w);
		w.close();
		return w.toString();
	}

	public static void write(String str, Writer writer) throws IOException {
		pipe(new StringReader(str), writer);
	}

	public String apply(Reader reader) throws Exception {
		return apply(read(reader));
	}

	public String apply(String str) throws TransformerException {
		String s = apply(str, null);
		Reader r=new StringReader(s);
		Writer w=new StringWriter();
		transformer.transform(new StreamSource(r), new StreamResult(w));
		return w.toString().replaceAll("\r", "").replaceAll("\u00a0", " ").replaceAll(" +\n", "\n").replaceAll(" {8}", "\t").trim();
	}

	public static String apply(String str, String stylesheet) {
		return apply(str, stylesheet, null, null, null, null, false);
	}

	public static String apply(String str, String stylesheet, String title, String feed, String icon, String url, boolean extension) {
		Matcher pm=pattern.matcher(str);
		StringBuffer b=new StringBuffer(XML);
		if (stylesheet != null) b.append("<?xml-stylesheet href=\"").append(stylesheet).append("\" type=\"text/xsl\"?>");
		b.append("<html xmlns=\"http://www.w3.org/1999/xhtml\">\n");
		b.append("<head>\n");
		if (title != null) b.append("<title>").append(title).append("</title>\n");
		if (feed != null) b.append("<link rel=\"alternate\" type=\"application/rss+xml\" title=\"rss\" href=\"").append(feed).append("\"/>\n");
		if (icon != null) b.append("<link rel=\"shortcut icon\" href=\"").append(icon).append("\" type=\"image/x-icon\"/>\n");
		b.append("</head>\n");
		b.append("<body>\n");
		b.append("<tt>\n");
		int n=0;
		while(pm.find()) {
			int m=pm.start();
			String s=pm.group();
			b.append(links(str.substring(n,m), extension));
			b.append(insertNameSpace(s));
			n=pm.end();
		}
		b.append(links(str.substring(n), extension));
		b.append("</tt>\n");
		if (url != null) b.append("<hr width=\"100%\"/>").append("<a href=\"" + url + "\">source</a>\n");
		b.append("</body>\n");
		b.append("</html>\n");
		return b.toString();
	}

	static String links(String str, boolean extension) {
		StringBuffer buffer = new StringBuffer();
		Matcher pm=links.matcher(str);
		int n=0;
		while(pm.find()) {
			int m=pm.start();
			String s=pm.group();
			buffer.append(newlines(str.substring(n,m)));
			if(s.endsWith(".txt")) {
				String ss = s.substring(0, s.length() - 4);
				buffer.append("<a href=\"" + ss + (extension?".xhtml":".txt") + "\">");
				buffer.append(ss.endsWith("/index")?ss.substring(0, ss.length() - 6):ss);
				buffer.append("</a>");
			} else {
				buffer.append("<a href=\"" + special(s) + "\">" + special(s) + "</a>");
			}
			n=pm.end();
		}
		buffer.append(newlines(str.substring(n)));
		return buffer.toString();
	}

	static String newlines(String str) {
		StringBuffer buffer = new StringBuffer();
		Matcher pm=newlines.matcher(str);
		int n=0;
		while(pm.find()) {
			int m=pm.start();
			String s=pm.group();
			String t = spaces(str.substring(n,m));
			buffer.append(t.matches("-+")?"<hr/>":(m == 0?" ":t) + "<br/>").append("\n");
			n=pm.end();
		}
		buffer.append(spaces(str.substring(n)));
		return buffer.toString();
	}

	static String spaces(String str) {
		StringBuffer buffer = new StringBuffer();
		Matcher pm=spaces.matcher(str);
		int n=0;
		while(pm.find()) {
			int m=pm.start();
			String s=pm.group();
			buffer.append(special(str.substring(n,m)));
			for(int i = 0; i < s.length(); i++) buffer.append(s.charAt(i) == '\t'?"\u00a0\u00a0\u00a0\u00a0\u00a0\u00a0\u00a0\u00a0":"\u00a0");
			n=pm.end();
		}
		buffer.append(special(str.substring(n)));
		return buffer.toString();
	}

	static String special(String str) {
		return str.replaceAll("&","&amp;").replaceAll("<","&lt;").replaceAll(">","&gt;").replaceAll("\"","&quot;");
	}

	public static String insertNameSpace(String str) {
		String m=str.indexOf(mml)>-1?"":mml;
		String s=str.indexOf(svg)>-1?"":svg;
		return isSvg(str)?str.substring(0, 4)+s+str.substring(4):str.substring(0, 5)+m+str.substring(5);
	}

	public static String stripNameSpace(String str) {
		int n=str.indexOf(mml);
		return str.substring(0, n)+str.substring(n+mml.length());
	}

	public static boolean isSvg(String str) {
		return str.substring(1,4).compareTo("svg")==0;
	}

	private static void pipe(Reader in, Writer out) throws IOException {
		int c = in.read();
		while(c != -1) {
			out.write(c);
			c = in.read();
		}
	}
}
