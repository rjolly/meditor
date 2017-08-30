mkdir("build");
mkdir("build/web");
mkdir("build/web/WEB-INF");
mkdir("build/web/WEB-INF/classes");
mkdir("build/web/WEB-INF/lib");

javac("src/java", "build/web/WEB-INF/classes");
cp("../txt2xhtml/dist/txt2xhtml.jar", "build/web/WEB-INF/lib/txt2xhtml.jar");
copy("web", "build/web");
