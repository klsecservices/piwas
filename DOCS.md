# Documentation

[[_TOC_]]

## NodeJS  

### Usage

```
node piwas.js $(pidof node)
```

### Injection protocol

The loader uses Node.JS debugger protocol. It sends SIGUSR1 signal to a target process in order to turn the NodeJS debugger on. After that PIWAS checks for the new listening ports (or tries all ports listened by the app in case if debugger was already enabled) and checks if the port provides a NodeJS debugger API.

### Injected code technical  details

In order to find the Expess.js Application instance object, the application changes the prototype of `Router` class, modifying the `handle` method. The modified method calls the original method, finds the Application and socket server instance objects in the methods context, registers both webshell and websocket SOCKS endpoints and return the `Router.handle` method to the original state. **Note that due to the Router.handle is called only to process users request we need to make at least one request in order to make all the endpoints registered**


### References

- https://nodejs.org/en/learn/getting-started/debugging

## Java

### Usage 

```
java -jar piwas.jar $(pidof java)
```

### Injection protocol

The idea is to use Java Attach API to inject the payload. The Java attach API protocol is following:

1. Loader process creates the `.attach$(pid)` empty file in the target process working dir (**IoC!**)
2. Loader process sends SIGQUIT signal to the target process
3. Target process will create `/tmp/.java_pid$(pid)` UNIX socket file (**IoC!**)
4. Loader process sends a `load` command through the UNIX socket
5. Target process will load arbitrary JAR or SO file from the filesystem (**IoC!**)

### Injected code technical details

Current implementaion requires Java 8+, and tested on tomcat based environments. Other environments will probably require some modifications, feel free to open an issue. Pull requests are welcome!

The injected payload tries to find `javax.servlet.http.HttpServlet` (or `jakarta.servlet.http.HttpServlet` for the newer versions of environment) class in the memory and then tries to modify the `service` method, adding the arbitrary code at the beggining of the method. The added arbitrary code will be executed on every HTTP request to the application and basically makes two things:

1. It checks if we already have registered an websocket endpoint. Otherwise it registers an websocket endpoint which will provide a SOCKS tunnel. **Note that due to the code is executed only during the HTTP request processing we need to make at least one request in order to make webocket endpoint registered**

2. Checks if the request path contains `{SHA1}_sh` string and reroutes execution flow to the webshell code.

### References

- https://docs.oracle.com/javase/8/docs/technotes/guides/attach/
- https://github.com/paul-axe/slides/blob/master/spbctf%202021%20-%20JVMyacni%20Stories.pdf
