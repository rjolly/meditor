package jscl.editor;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class Wiki {
	public static final Wiki instance = new Wiki();
	private final Pattern pattern = Pattern.compile("([$][ ].*?[$])|(\n\\\\\\[\n\t.*?\n\\\\\\]\n)");
	private final Pattern math = Pattern.compile("(<math>.*?</math>)");

	private Wiki() {
	}

	public String copyToWiki(final String document) throws Exception {
		return math(Code.instance("/xsltml/mmltex.xsl").apply(document));
	}

	public String pasteFromWiki(final String str) throws Exception {
		final StringBuffer b = new StringBuffer();
		final Matcher pm = math.matcher(str);
		int n = 0;
		while (pm.find()) {
			final int m = pm.start();
			final String s = pm.group();
			final String t = MathML.instance.createImage(s) == null?MathML.instance.fromTeX(s):s;
			b.append(str.substring(n, m));
			b.append(t);
			n = pm.end();
		}
		b.append(str.substring(n));
		return b.toString();
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
