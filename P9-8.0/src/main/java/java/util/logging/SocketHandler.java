package java.util.logging;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import libcore.net.NetworkSecurityPolicy;

public class SocketHandler extends StreamHandler {
    private String host;
    private int port;
    private Socket sock;

    private void configure() {
        LogManager manager = LogManager.getLogManager();
        String cname = getClass().getName();
        setLevel(manager.getLevelProperty(cname + ".level", Level.ALL));
        setFilter(manager.getFilterProperty(cname + ".filter", null));
        setFormatter(manager.getFormatterProperty(cname + ".formatter", new XMLFormatter()));
        try {
            setEncoding(manager.getStringProperty(cname + ".encoding", null));
        } catch (Exception e) {
            try {
                setEncoding(null);
            } catch (Exception e2) {
            }
        }
        this.port = manager.getIntProperty(cname + ".port", 0);
        this.host = manager.getStringProperty(cname + ".host", null);
    }

    public SocketHandler() throws IOException {
        this.sealed = false;
        configure();
        try {
            connect();
            this.sealed = true;
        } catch (IOException ix) {
            System.err.println("SocketHandler: connect failed to " + this.host + ":" + this.port);
            throw ix;
        }
    }

    public SocketHandler(String host, int port) throws IOException {
        this.sealed = false;
        configure();
        this.sealed = true;
        this.port = port;
        this.host = host;
        connect();
    }

    private void connect() throws IOException {
        if (this.port == 0) {
            throw new IllegalArgumentException("Bad port: " + this.port);
        } else if (this.host == null) {
            throw new IllegalArgumentException("Null host name: " + this.host);
        } else if (NetworkSecurityPolicy.getInstance().isCleartextTrafficPermitted()) {
            this.sock = new Socket(this.host, this.port);
            setOutputStream(new BufferedOutputStream(this.sock.getOutputStream()));
        } else {
            throw new IOException("Cleartext traffic not permitted");
        }
    }

    public synchronized void close() throws SecurityException {
        super.close();
        if (this.sock != null) {
            try {
                this.sock.close();
            } catch (IOException e) {
            }
        }
        this.sock = null;
    }

    public synchronized void publish(LogRecord record) {
        if (isLoggable(record)) {
            super.publish(record);
            flush();
        }
    }
}
