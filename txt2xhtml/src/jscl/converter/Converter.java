/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jscl.converter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author raphael
 */
public class Converter {
	public static Pattern pattern=Pattern.compile("(?s:<math.*?/math\n?>)|(?s:<svg.*?/svg\n?>)");
	public static String XML="<?xml version=\"1.0\" encoding=\"utf-8\" standalone=\"no\"?>";
	static Pattern links=Pattern.compile("(https?://[\\w_\\-\\./~\\?=]*)|([\\w_\\-\\./]*\\.txt)");
	static Pattern newlines=Pattern.compile("\n");
	static Pattern spaces=Pattern.compile("( {2,}|\\t)");
	static String mml=" xmlns=\"http://www.w3.org/1998/Math/MathML\"";
	static String svg=" xmlns=\"http://www.w3.org/2000/svg\"";

	public static String convert(String str) {
		return convert(str, null, null, null, null, null, false);
	}

	public static String convert(String str, String stylesheet, String title, String feed, String icon, String url, boolean extension) {
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
			b.append(xhtmltext(str.substring(n,m), extension));
			b.append(insertNameSpace(s));
			n=pm.end();
		}
		b.append(xhtmltext(str.substring(n), extension));
		b.append("</tt>\n");
		if (url != null) b.append("<hr width=\"100%\"/>").append("<a href=\"" + url + "\">source</a>\n");
		b.append("</body>\n");
		b.append("</html>\n");
		return b.toString();
	}

	static String xhtmltext(String str, boolean extension) {
		return links(spaces(str).replaceAll("&","&amp;").replaceAll("<","&lt;").replaceAll(">","&gt;").replaceAll("\"","&quot;"), extension);
	}

	static String spaces(String line) {
		StringBuffer buffer = new StringBuffer();
		Matcher pm=spaces.matcher(line);
		int n=0;
		while(pm.find()) {
			int m=pm.start();
			String s=pm.group();
			buffer.append(line.substring(n,m));
			for(int i = 0; i < s.length(); i++) buffer.append(s.charAt(i) == '\t'?"\u00a0\u00a0":"\u00a0");
			n=pm.end();
		}
		buffer.append(line.substring(n));
		return buffer.toString();
	}

	static String links(String line, boolean extension) {
		StringBuffer buffer = new StringBuffer();
		Matcher pm=links.matcher(line);
		int n=0;
		while(pm.find()) {
			int m=pm.start();
			String s=pm.group();
			buffer.append(newlines(line.substring(n,m)));
			if(s.endsWith(".txt")) {
				String ss = s.substring(0, s.length() - 4);
				buffer.append("<a href=\"" + ss + (extension?".xhtml":".txt") + "\">" + ss + "</a>");
			} else {
				buffer.append("<a href=\"" + s + "\">" + s + "</a>");
			}
			n=pm.end();
		}
		buffer.append(newlines(line.substring(n)));
		return buffer.toString();
	}

	static String newlines(String line) {
		StringBuffer buffer = new StringBuffer();
		Matcher pm=newlines.matcher(line);
		int n=0;
		while(pm.find()) {
			int m=pm.start();
			String s=pm.group();
			String t = line.substring(n,m);
			buffer.append(t.matches("-+")?"<hr/>":(m == 0?" ":t) + "<br/>").append("\n");
			n=pm.end();
		}
		buffer.append(line.substring(n));
		return buffer.toString();
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
}
