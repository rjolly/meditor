mkdir("build");
mkdir("build/classes");
mkdir("build/sources");
mkdir("build/javadoc");

javac("src", "build/classes", "1.8");
copy("res", "build/classes");
copy("src", "build/sources");
copy("res/jscl", "build/sources/jscl");
javadoc("src", "build/javadoc");

mkdir("dist");
var name = "meditor";
jar("dist/" + name + ".jar", "build/classes");
jar("dist/" + name + "-source.jar", "build/sources");
jar("dist/" + name + "-javadoc.jar", "build/javadoc");
cp("pom.xml", "dist/" + name + ".pom")

publish("dist")
