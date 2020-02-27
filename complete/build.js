mkdir("dist");
var name = "complete";
cp("pom.xml", "dist/" + name + ".pom")

publish("dist")
