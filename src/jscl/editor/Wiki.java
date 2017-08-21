package jscl.editor;

import java.util.regex.Pattern;
import java.util.regex.Matcher;
import jscl.converter.Converter;

public class Wiki {
	public static final Wiki instance = new Wiki();
	private final Pattern pattern = Pattern.compile("([$][ ].*?[$])|(\n\\\\\\[\n\t.*?\n\\\\\\]\n)");

	private Wiki() {
	}

	public String copyToWiki(final String document) throws Exception {
		return math(Converter.instance("/xsltml/mmltex.xsl").apply(document));
	}

	private String math(final String str) {
		final StringBuffer b = new StringBuffer();
		final Matcher pm = pattern.matcher(str);
		int n = 0;
		while (pm.find()) {
			final int m = pm.start();
			final String s = pm.group();
			final String t = s.startsWith("$")?s.substring(2, s.lastIndexOf("$")):s.substring(5, s.lastIndexOf("\n\\]"));
			b.append(str.substring(n,m));
			b.append("<math>" + t + "</math>");
			n=pm.end();
		}
		b.append(str.substring(n));
		return b.toString();
	}
}
