package jscl.mathml;

public class Code {
	public static String get(String document, String stylesheet) throws Exception {
		String s = MathML.code(document, stylesheet);
		return s.substring(3,s.length()-2).replaceAll(" \n", "\n");
	}
}
