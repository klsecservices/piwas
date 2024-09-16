package pwn.websocket;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.math.BigInteger;
import java.nio.channels.AsynchronousSocketChannel;

public class Handler  {
    private static final byte RES_GRANTED = 0x5A;
    private static final byte RES_FAILLED = 0x5B;
    private Session session;
    private AsynchronousSocketChannel connection; 

    public ByteBuffer reply(byte replyCode, short port, int addr){
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer
            .put((byte)0)
            .put(replyCode)
            .putShort( port)
            .putInt(addr).rewind();
        return buffer;
    }

    public Handler(Session session) {
        this.session = session;
    }
    
    public void close() {
        if (this.connection != null) {
            try {
                if (this.connection.isOpen())
                    this.connection.close();
                if (this.session.isOpen())
                    this.session.close();
            } catch (IOException e) {
            }
        }
    }

    public void onMessage(ByteBuffer message) {
        try {
            if (this.connection != null) {
                this.connection.write(message);
                return;
            }

            byte ver = message.get();
            byte command = message.get();
            short port = message.getShort();
            int addr = message.getInt();

            if (command != 0x01) {
                // We support only CONNECT method
                session.send(reply(RES_FAILLED, port, addr));
                session.close();
                return;
            }
            byte[] bytes = BigInteger.valueOf(addr).toByteArray();
            InetAddress inetAddress = InetAddress.getByAddress(bytes);

            try {
                this.connection = AsynchronousSocketChannel.open();
                this.connection.connect(new InetSocketAddress(inetAddress, port)).get();
                session.send(reply(RES_GRANTED, port, addr));
                new Reader(this.connection, session).read();
            } catch (Exception e) {
                session.send(reply(RES_FAILLED, port, addr));
                session.close();
                return;
            }
        } catch (Exception e) {
        }
    }
}
