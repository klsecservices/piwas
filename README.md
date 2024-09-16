# PIWAS - Process Injected Webshell And SOCKS 

**The project is still in proof-of-concept state**

PIWAS is a set of tools allowing you to inject some neat features to a running process. It uses builtin debugging protocols in order to inject source code, meaning that it doesnt require PTRACE and thus can work in container environment (or any other enviroment with SYS_PTRACE capability disabled). Currently PIWAS injects payload supporting following features:

- webshell - you probably know what it is. Currently the webshell endpoint is `/{SHA1}_sh` where SHA1 is sha1sum of the PIWAS injector.
- socks - socks4 server which uses websockets as a transport. Currently the SOCKS endpoint is `/{SHA1}_socks` where SHA1 is sha1um of the PIWAS injector. In order to use the SOCKS proxy it is recommended to use piwas client which will which will create a listening SOCKS proxy and will transfer all traffic through a target injected websocket endpoint. Here is the example of usage:

```
./piwas-client -url "ws://localhost:8080/$(sha1sum piwas.jar| cut -d ' ' -f 1)_socks"
```

## Usage

By default all versions of PIWAS follow the same protocol. In order to inject the code to a running process you need to run the corresponding version of piwas with the target process ID as an argument.

Here is the example for Java applications:

```
java -jar piwas.jar $(pidof java)
```

For more information please refer to the [documentation](./DOCS.md)
