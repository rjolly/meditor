mkdir("dist");
var name = "base";
cp("pom.xml", "dist/" + name + ".pom")

publish("dist")
