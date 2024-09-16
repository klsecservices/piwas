package pwn.websocket;

import java.util.HashMap;
import java.nio.ByteBuffer;


public class Endpoint {
    public static HashMap<String, Handler> handlers = new HashMap<String, Handler>();
    public static HashMap<String, Handler> sessions = new HashMap<String, Handler>();
    public static boolean registered = false;

    public void onOpen(Session session) {
        Handler handler = new Handler(session);
        handlers.put(session.getId(), handler);
    }

    public void onMessage(ByteBuffer msg, Session session) {
        if (handlers.containsKey(session.getId())) {
            handlers.get(session.getId()).onMessage(msg);
        }
    }

    public void onClose(Session session) {
        if (handlers.containsKey(session.getId())) {
            handlers.get(session.getId()).close();
            handlers.remove(session.getId());
        }
    }
}
