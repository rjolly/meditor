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

public class Converter {
	public static final Pattern pattern = Pattern.compile("(?s:<math.*?/math\n?>)|(?s:<svg.*?/svg\n?>)");
	public static final String XML = "<?xml version=\"1.0\" encoding=\"utf-8\" standalone=\"no\"?>";
	private static final Pattern word = Pattern.compile("[\\w_\\-\\.]+");
	private static final Pattern http = Pattern.compile("https?://[\\w_\\-\\.:/~\\?=&#]*");
	private static final Pattern txt = Pattern.compile("(" + word.pattern() + ")(?:/(" + word.pattern() + "))*\\.txt");
	private static final Pattern mvn = Pattern.compile("(" + word.pattern() + ")#(" + word.pattern() + ");(" + word.pattern() + ")");
	private static final Pattern links = Pattern.compile("(" + http.pattern() + ")|(" + txt.pattern() + ")|(" + mvn.pattern() + ")");
	private static final Pattern newlines = Pattern.compile("\n");
	private static final Pattern spaces = Pattern.compile("(?m:^ +)|(  +)|(\\t)");
	private static final String mml = " xmlns=\"http://www.w3.org/1998/Math/MathML\"";
	private static final String svg = " xmlns=\"http://www.w3.org/2000/svg\"";
	private static final Map<String, Converter> cache = new HashMap<String, Converter>();
	public final Transformer transformer;

	private Converter(final Transformer transformer) {
		this.transformer = transformer;
	}

	public static Converter instance(String stylesheet) throws Exception {
		if (stylesheet.startsWith("/")) {
			stylesheet = stylesheet.substring(1);
		}
		if (!cache.containsKey(stylesheet)) {
			final URL resource = Thread.currentThread().getContextClassLoader().getResource(stylesheet);
			if (resource == null) throw new Exception("Stylesheet not found: \"" + stylesheet + "\"");
			cache.put(stylesheet, new Converter(TransformerFactory.newInstance().newTransformer(new StreamSource(resource.toString()))));
		}
 		return cache.get(stylesheet);
 	}

	public static String read(final Reader reader) throws IOException {
		final Writer w = new StringWriter();
		pipe(reader, w);
		w.close();
		return w.toString();
	}

	public static void write(final String str, final Writer writer) throws IOException {
		pipe(new StringReader(str), writer);
	}

	public String apply(final Reader reader) throws Exception {
		return apply(read(reader));
	}

	public String apply(final String str) throws TransformerException {
		final String s = apply(str, null);
		final Reader r = new StringReader(s);
		final Writer w = new StringWriter();
		transformer.transform(new StreamSource(r), new StreamResult(w));
		return w.toString().replaceAll("\r", "").replaceAll("\u00a0", " ").replaceAll(" +\n", "\n").replaceAll(" {8}", "\t").trim();
	}

	public static String apply(final String str, final String stylesheet) {
		return apply(str, stylesheet, null, null, null, null, false);
	}

	public static String apply(final String str, final String stylesheet, final String title, final String feed, final String icon, final String url, final boolean extension) {
		final Matcher pm = pattern.matcher(str);
		final StringBuffer b = new StringBuffer(XML);
		if (stylesheet != null && !stylesheet.isEmpty()) {
			b.append("<?xml-stylesheet href=\"").append(stylesheet).append("\" type=\"text/xsl\"?>");
		}
		b.append("<html xmlns=\"http://www.w3.org/1999/xhtml\">\n");
		b.append("<head>\n");
		if (title != null && !title.isEmpty()) {
			b.append("<title>").append(title).append("</title>\n");
		}
		if (feed != null && !feed.isEmpty()) {
			b.append("<link rel=\"alternate\" type=\"application/rss+xml\" title=\"rss\" href=\"").append(feed).append("\"/>\n");
		}
		if (icon != null && !icon.isEmpty()) {
			b.append("<link rel=\"shortcut icon\" href=\"").append(icon).append("\" type=\"image/x-icon\"/>\n");
		}
		b.append("</head>\n");
		b.append("<body>\n");
		b.append("<tt>\n");
		int n = 0;
		while (pm.find()) {
			final int m = pm.start();
			final String s = pm.group();
			b.append(links(str.substring(n, m), extension));
			b.append(insertNameSpace(s));
			n = pm.end();
		}
		b.append(links(str.substring(n), extension));
		b.append("</tt>\n");
		if (url != null) {
			b.append("<hr width=\"100%\"/>").append("<a href=\"" + url + "\">source</a>\n");
		}
		b.append("</body>\n");
		b.append("</html>\n");
		return b.toString();
	}

	private static String links(final String str, final boolean extension) {
		final StringBuffer buffer = new StringBuffer();
		final Matcher pm = links.matcher(str);
		int n = 0;
		while (pm.find()) {
			final int m = pm.start();
			final String s = pm.group();
			final Matcher pm0 = txt.matcher(s);
			final Matcher pm1 = mvn.matcher(s);
			buffer.append(newlines(str.substring(n, m)));
			if (pm0.matches()) {
				buffer.append("<a href=\"");
				for (int i = 1 ; i <= pm0.groupCount() ; i++) {
					final String ss = pm0.group(i);
					if (ss != null) {
						if (i > 1) {
							buffer.append("/");
						}
						buffer.append(ss);
					}
				}
				buffer.append(extension?".xhtml":".txt");
				buffer.append("\">");
				for (int i = 1 ; i <= pm0.groupCount() ; i++) {
					final String ss = pm0.group(i);
					if (ss != null && (!"index".equals(ss) || i < pm0.groupCount())) {
						if (i > 1) {
							buffer.append("/");
						}
						buffer.append(ss);
					}
				}
				buffer.append("</a>");
			} else if (pm1.matches()) {
				buffer.append("<a href=\"");
				buffer.append("mvn:");
				for (int i = 1 ; i <= pm1.groupCount() ; i++) {
					final String ss = pm1.group(i);
					if (i > 1) {
						buffer.append("/");
					}
					buffer.append(ss);
				}
				buffer.append("\">");
				for (int i = 1 ; i <= pm1.groupCount() ; i++) {
					final String ss = pm1.group(i);
					if (i > 1) {
						buffer.append(i == 2?"#":";");
					}
					buffer.append(ss);
				}
				buffer.append("</a>");
			} else {
				final String ss = special(s);
				buffer.append("<a href=\"");
				buffer.append(ss);
				buffer.append("\">");
				buffer.append(ss);
				buffer.append("</a>");
			}
			n = pm.end();
		}
		buffer.append(newlines(str.substring(n)));
		return buffer.toString();
	}

	private static String newlines(final String str) {
		final StringBuffer buffer = new StringBuffer();
		final Matcher pm = newlines.matcher(str);
		int n = 0;
		while (pm.find()) {
			final int m = pm.start();
			final String s = pm.group();
			final String t = spaces(str.substring(n, m));
			buffer.append(t.matches("-+")?"<hr/>":(m == 0?" ":t) + "<br/>").append("\n");
			n = pm.end();
		}
		buffer.append(spaces(str.substring(n)));
		return buffer.toString();
	}

	private static String spaces(final String str) {
		final StringBuffer buffer = new StringBuffer();
		final Matcher pm = spaces.matcher(str);
		int n = 0;
		while (pm.find()) {
			final int m = pm.start();
			final String s = pm.group();
			buffer.append(special(str.substring(n, m)));
			for (int i = 0; i < s.length(); i++) {
				buffer.append(s.charAt(i) == '\t'?"\u00a0\u00a0\u00a0\u00a0\u00a0\u00a0\u00a0\u00a0":"\u00a0");
			}
			n = pm.end();
		}
		buffer.append(special(str.substring(n)));
		return buffer.toString();
	}

	private static String special(final String str) {
		return str.replaceAll("&","&amp;").replaceAll("<","&lt;").replaceAll(">","&gt;").replaceAll("\"","&quot;");
	}

	public static String insertNameSpace(final String str) {
		final String m = str.indexOf(mml) > -1?"":mml;
		final String s = str.indexOf(svg) > -1?"":svg;
		return isSvg(str)?str.substring(0, 4) + s + str.substring(4):str.substring(0, 5) + m + str.substring(5);
	}

	public static String stripNameSpace(final String str) {
		final int n = str.indexOf(mml);
		return str.substring(0, n) + str.substring(n + mml.length());
	}

	public static boolean isSvg(final String str) {
		return str.substring(1, 4).compareTo("svg") == 0;
	}

	private static void pipe(final Reader in, final Writer out) throws IOException {
		int c = in.read();
		while (c != -1) {
			out.write(c);
			c = in.read();
		}
	}
}
