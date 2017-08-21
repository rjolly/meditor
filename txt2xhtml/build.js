mkdir("build");
mkdir("build/classes");
mkdir("build/sources");
mkdir("build/javadoc");

javac("src", "build/classes");
copy("src", "build/sources");
javadoc("src", "build/javadoc");

mkdir("dist");
var name = "txt2xhtml";
jar("dist/" + name + ".jar", "build/classes");
jar("dist/" + name + "-source.jar", "build/sources");
jar("dist/" + name + "-javadoc.jar", "build/javadoc");
cp("pom.xml", "dist/" + name + ".pom")

publish("dist")
