package jscl.mathml;

import java.util.regex.Pattern;
import java.util.regex.Matcher;
import jscl.converter.Converter;

public class Wiki {
	static Pattern pattern=Pattern.compile("([$][ ].*?[$]\n?)|(\n\\\\\\[\n\t.*?\n\\\\\\]\n?)");
	static Pattern math=Pattern.compile("(<math>.*?</math>)");

	public static String copyToWiki(String document) throws Exception {
		String s = MathML.tex(document);
		s = s.substring(3,s.length()-2);
		s = s.replaceAll("\u00a0"," ");
		return math(s);
	}

	public static String pasteFromWiki(String str) throws Exception {
		StringBuffer b=new StringBuffer();
		Matcher pm=math.matcher(str);
		int n=0;
		while(pm.find()) {
			int m=pm.start();
			String s=pm.group();
			String t=Converter.insertNameSpace(s);
			try {
				if(MathML.createImage(Converter.XML+t)==null) t=TeX.mml(s);
			} catch (Throwable ex) {}
			b.append(str.substring(n,m));
			b.append(Converter.stripNameSpace(t));
			n=pm.end();
		}
		b.append(str.substring(n));
		return b.toString();
	}

	static String math(String str) {
		StringBuffer b=new StringBuffer();
		Matcher pm=pattern.matcher(str);
		int n=0;
		while(pm.find()) {
			int m=pm.start();
			String s=pm.group();
			String t=s.startsWith("$")?s.substring(2, s.lastIndexOf("$")):s.substring(5, s.lastIndexOf("\n\\]"));
			b.append(str.substring(n,m));
			b.append("<math>"+t+"</math>");
			n=pm.end();
		}
		b.append(str.substring(n));
		return b.toString();
	}
}
