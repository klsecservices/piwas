package pwn.websocket;

import java.io.IOException;
import java.nio.channels.CompletionHandler;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.ByteBuffer;


public class Reader implements CompletionHandler<Integer, ByteBuffer> {
    private Session session;
    private AsynchronousSocketChannel channel;
    private ByteBuffer buf;

    public Reader(AsynchronousSocketChannel channel, Session session) {
        this.channel = channel;
        this.session = session;
        this.buf = ByteBuffer.allocate(1024);
    }

    public void read() {
        this.channel.read(buf, null, this);
    }

    @Override
    public void completed(Integer size, ByteBuffer attachment) {   
        buf.flip();

        if (size < 0) {
            try {
                if (this.channel.isOpen())
                    this.channel.close();
                if (this.session.isOpen())
                    this.session.close();
            } catch (IOException e) {}
            return;
        }

        try {
            if (size > 0) 
                this.session.send(buf);

            if (this.channel.isOpen()) {
                this.channel.read(buf, null, this);
            }
        } catch (IOException e) {
        }
    }

    @Override
    public void failed(Throwable exc, ByteBuffer v) {
    }
}

