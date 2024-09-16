lib = ${JAVA_HOME}/lib/tools.jar
javassisturl = https://github.com/jboss-javassist/javassist/releases/download/rel_3_28_0_ga/javassist.jar
websocketurl = https://repo1.maven.org/maven2/javax/websocket/javax.websocket-api/1.1/javax.websocket-api-1.1.jar
beanshellurl = https://repo1.maven.org/maven2/org/beanshell/bsh/1.3.0/bsh-1.3.0.jar
jakartaurl = https://repo1.maven.org/maven2/jakarta/websocket/jakarta.websocket-api/2.0.0/jakarta.websocket-api-2.0.0.jar
builddir = dist
srcdir = src
outfile = piwas.jar


all: libs build

prepare:
	mkdir -p $(builddir)
	mkdir -p $(builddir)/classes
	mkdir -p $(builddir)/lib

build: prepare
	javac -d $(builddir)/classes -cp $(srcdir):$(lib) $(srcdir)/Loader.java
	javac -d $(builddir)/classes -cp $(builddir)/lib/javassist.jar:$(srcdir) $(srcdir)/Agent.java $(srcdir)/DebugAgent.java
	javac -d $(builddir)/classes -cp $(builddir)/lib/javassist.jar:$(srcdir) $(srcdir)/pwn/websocket/*.java
	jar -cfm $(builddir)/$(outfile) Manifest.txt \
		-C $(srcdir) source.java \
		-C $(builddir)/lib/ javassist \
		-C $(builddir)/classes pwn \
		-C $(builddir)/lib/ bsh \
		-C $(builddir)/classes Agent.class \
		-C $(builddir)/classes DebugAgent.class \
		-C $(builddir)/classes Loader.class


libs: prepare
ifeq (,$(wildcard $(builddir)/lib/javassist.jar))
	wget $(javassisturl) -O $(builddir)/lib/javassist.jar
	cd $(builddir)/lib ; jar xvf javassist.jar 
endif
ifeq (,$(wildcard $(builddir)/lib/bsh-1.3.0.jar))
	wget $(beanshellurl) -O $(builddir)/lib/bsh-1.3.0.jar
	cd $(builddir)/lib ; jar xvf bsh-1.3.0.jar
endif
ifeq (,$(wildcard $(builddir)/lib/websocket-api.jar))
	wget $(websocketurl) -O $(builddir)/lib/websocket-api.jar
	cd $(builddir)/lib ; jar xvf websocket-api.jar 
endif
ifeq (,$(wildcard $(builddir)/lib/jakarta-api.jar))
	wget $(jakartaurl) -O $(builddir)/lib/jakarta-api.jar
	cd $(builddir)/lib ; jar xvf jakarta-api.jar
endif

clean:
	cd $(builddir)
	rm -rf *
