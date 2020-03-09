package jscl.converter;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Converter {
	public final Pattern pattern = Pattern.compile("(?s:<math.*?/math\n?>)|(?s:<svg.*?/svg\n?>)");
	protected final String XML = "<?xml version=\"1.0\" encoding=\"utf-8\" standalone=\"no\"?>";
	private final Pattern word = Pattern.compile("[\\w_\\-\\.]+");
	private final Pattern http = Pattern.compile("https?://[\\w_\\-\\.:/~\\?=&#]*");
	private final Pattern txt = Pattern.compile("(" + word.pattern() + ")(?:/(" + word.pattern() + "))*\\.txt");
	private final Pattern mvn = Pattern.compile("(" + word.pattern() + ")#(" + word.pattern() + ");(" + word.pattern() + ")");
	private final Pattern mail = Pattern.compile(word.pattern() + "@" + word.pattern());
	private final Pattern links = Pattern.compile("(" + http.pattern() + ")|(" + txt.pattern() + ")|(" + mvn.pattern() + ")|(" + mail.pattern() + ")");
	private final Pattern newlines = Pattern.compile("\n");
	private final Pattern spaces = Pattern.compile("(?m:^ +)|(  +)|(\\t)");
	private final String mml = " xmlns=\"http://www.w3.org/1998/Math/MathML\"";
	private final String svg = " xmlns=\"http://www.w3.org/2000/svg\"";

	public String apply(final String str) {
		return fromString(str, null, null, null, null, null, false);
	}

	@Deprecated
	public String apply(final String str, final String stylesheet) {
		return fromString(str, stylesheet, null, null, null, null, false);
	}

	@Deprecated
	public String apply(final String str, final String stylesheet, final String title, final String feed, final String icon, final String url, final boolean extension) {
		return fromString(str, stylesheet, title, feed, icon, url, extension);
	}

	private String fromString(final String str, final String stylesheet, final String title, final String feed, final String icon, final String url, final boolean extension) {
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

	private String links(final String str, final boolean extension) {
		final StringBuffer buffer = new StringBuffer();
		final Matcher pm = links.matcher(str);
		int n = 0;
		while (pm.find()) {
			final int m = pm.start();
			final String s = pm.group();
			final Matcher pm0 = txt.matcher(s);
			final Matcher pm1 = mvn.matcher(s);
			final Matcher pm2 = mail.matcher(s);
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
			} else if (pm2.matches()) {
				final String ss = pm2.group();
				buffer.append("<a href=\"");
				buffer.append("mailto:");
				buffer.append(ss);
				buffer.append("\">");
				buffer.append(ss);
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

	private String newlines(final String str) {
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

	private String spaces(final String str) {
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

	private String special(final String str) {
		return str.replaceAll("&","&amp;").replaceAll("<","&lt;").replaceAll(">","&gt;").replaceAll("\"","&quot;");
	}

	protected String insertNameSpace(final String str) {
		final String m = str.indexOf(mml) > -1?"":mml;
		final String s = str.indexOf(svg) > -1?"":svg;
		return isSvg(str)?str.substring(0, 4) + s + str.substring(4):str.substring(0, 5) + m + str.substring(5);
	}

	protected String stripNameSpace(final String str) {
		final int n = str.indexOf(mml);
		return str.substring(0, n) + str.substring(n + mml.length());
	}

	protected boolean isSvg(final String str) {
		return str.substring(1, 4).compareTo("svg") == 0;
	}

	public String apply(final Reader reader, final String stylesheet, final String title, final String feed, final String icon, final String url, final boolean extension) throws IOException {
		return fromString(stringFromReader(reader), stylesheet, title, feed, icon, url, extension);
	}

	protected String stringFromReader(final Reader reader) throws IOException {
		final Writer writer = new StringWriter();
		pipe(reader, writer);
		writer.close();
		return writer.toString();
	}

	private char buffer[] = new char[8192];

	public void pipe(final Reader in, final Writer out) throws IOException {
		int n = in.read(buffer);
		while (n > -1) {
			out.write(buffer, 0, n);
			n = in.read(buffer);
		}
	}
}
