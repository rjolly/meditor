mkdir("dist");
var name = "scas";
cp("pom.xml", "dist/" + name + ".pom")

publish("dist")
