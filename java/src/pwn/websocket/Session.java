package pwn.websocket;

import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.io.IOException;
import java.util.HashMap;


public class Session {
    private Object sess;
    private Object remote;
    private String id;

    public static HashMap<String, Session> instances = new HashMap<String, Session>();

    public static Session getInstance(String id, Object sess, Object remote) {
        if (instances.containsKey(id)) {
            return instances.get(id);
        }
        Session instance = new Session(id, sess, remote);
        instances.put(id, instance);
        return instance;
    }

    private Session(String id, Object sess, Object remote) {
        this.id = id;
        this.sess = sess;
        this.remote = remote;
    }

    public String getId() {
        return this.id;
    }

    public boolean isOpen() {
        boolean result = false;
        try {
            Method method = this.sess.getClass().getMethod("isOpen");
            result = (boolean)method.invoke(this.sess);
        } finally {
            return result;
        }
    }

    public void close() {
        try {
            Method method = this.sess.getClass().getMethod("close");
            method.invoke(this.sess);
        } catch (Exception e) {
        } finally {
        }
    }

    public void send(ByteBuffer buf) throws IOException {
        try {
            Method method = this.remote.getClass().getMethod("sendBinary", ByteBuffer.class);
            method.invoke(this.remote, buf);
        } catch (Exception e) {
            throw new IOException();
        }
    }


}
