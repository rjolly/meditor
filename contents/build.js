mkdir("build");
mkdir("build/classes");

copy("res", "build/classes");

mkdir("dist");
var name = "contents";
jar("dist/" + name + ".jar", "build/classes");
cp("pom.xml", "dist/" + name + ".pom")

publish("dist")
