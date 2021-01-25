package jcifs.util.transport;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import jcifs.util.LogStream;

public abstract class Transport implements Runnable {
    static int id = 0;
    static LogStream log = LogStream.getInstance();
    String name;
    protected HashMap response_map;
    int state = 0;
    TransportException te;
    Thread thread;

    /* access modifiers changed from: protected */
    public abstract void doConnect() throws Exception;

    /* access modifiers changed from: protected */
    public abstract void doDisconnect(boolean z) throws IOException;

    /* access modifiers changed from: protected */
    public abstract void doRecv(Response response) throws IOException;

    /* access modifiers changed from: protected */
    public abstract void doSend(Request request) throws IOException;

    /* access modifiers changed from: protected */
    public abstract void doSkip() throws IOException;

    /* access modifiers changed from: protected */
    public abstract void makeKey(Request request) throws IOException;

    /* access modifiers changed from: protected */
    public abstract Request peekKey() throws IOException;

    public Transport() {
        StringBuilder append = new StringBuilder().append("Transport");
        int i = id;
        id = i + 1;
        this.name = append.append(i).toString();
        this.response_map = new HashMap(4);
    }

    public static int readn(InputStream in, byte[] b, int off, int len) throws IOException {
        int i = 0;
        while (i < len) {
            int n = in.read(b, off + i, len - i);
            if (n <= 0) {
                break;
            }
            i += n;
        }
        return i;
    }

    public synchronized void sendrecv(Request request, Response response, long timeout) throws IOException {
        makeKey(request);
        response.isReceived = false;
        try {
            this.response_map.put(request, response);
            doSend(request);
            response.expiration = System.currentTimeMillis() + timeout;
            while (!response.isReceived) {
                wait(timeout);
                timeout = response.expiration - System.currentTimeMillis();
                if (timeout <= 0) {
                    throw new TransportException(this.name + " timedout waiting for response to " + request);
                }
            }
            this.response_map.remove(request);
        } catch (IOException ioe) {
            LogStream logStream = log;
            if (LogStream.level > 2) {
                ioe.printStackTrace(log);
            }
            try {
                disconnect(true);
            } catch (IOException ioe2) {
                ioe2.printStackTrace(log);
            }
            throw ioe;
        } catch (InterruptedException ie) {
            throw new TransportException(ie);
        } catch (Throwable th) {
            this.response_map.remove(request);
            throw th;
        }
    }

    private void loop() {
        boolean timeout;
        boolean hard;
        while (this.thread == Thread.currentThread()) {
            try {
                Request key = peekKey();
                if (key == null) {
                    throw new IOException("end of stream");
                }
                synchronized (this) {
                    Response response = (Response) this.response_map.get(key);
                    if (response == null) {
                        LogStream logStream = log;
                        if (LogStream.level >= 4) {
                            log.println("Invalid key, skipping message");
                        }
                        doSkip();
                    } else {
                        doRecv(response);
                        response.isReceived = true;
                        notifyAll();
                    }
                }
            } catch (Exception ex) {
                String msg = ex.getMessage();
                if (msg == null || !msg.equals("Read timed out")) {
                    timeout = false;
                } else {
                    timeout = true;
                }
                if (!timeout) {
                    hard = true;
                } else {
                    hard = false;
                }
                if (!timeout) {
                    LogStream logStream2 = log;
                    if (LogStream.level >= 3) {
                        ex.printStackTrace(log);
                    }
                }
                try {
                    disconnect(hard);
                } catch (IOException ioe) {
                    ioe.printStackTrace(log);
                }
            }
        }
    }

    public synchronized void connect(long timeout) throws TransportException {
        try {
            switch (this.state) {
                case 0:
                    this.state = 1;
                    this.te = null;
                    this.thread = new Thread(this, this.name);
                    this.thread.setDaemon(true);
                    synchronized (this.thread) {
                        this.thread.start();
                        this.thread.wait(timeout);
                        switch (this.state) {
                            case 1:
                                this.state = 0;
                                this.thread = null;
                                throw new TransportException("Connection timeout");
                            case 2:
                                if (this.te == null) {
                                    this.state = 3;
                                    if (!(this.state == 0 || this.state == 3 || this.state == 4)) {
                                        LogStream logStream = log;
                                        if (LogStream.level >= 1) {
                                            log.println("Invalid state: " + this.state);
                                        }
                                        this.state = 0;
                                        this.thread = null;
                                        break;
                                    }
                                } else {
                                    this.state = 4;
                                    this.thread = null;
                                    throw this.te;
                                }
                            default:
                                if (!(this.state == 0 || this.state == 3 || this.state == 4)) {
                                    LogStream logStream2 = log;
                                    if (LogStream.level >= 1) {
                                        log.println("Invalid state: " + this.state);
                                    }
                                    this.state = 0;
                                    this.thread = null;
                                    break;
                                }
                        }
                    }
                case 1:
                case 2:
                default:
                    TransportException te2 = new TransportException("Invalid state: " + this.state);
                    this.state = 0;
                    throw te2;
                case 3:
                    if (!(this.state == 0 || this.state == 3 || this.state == 4)) {
                        LogStream logStream3 = log;
                        if (LogStream.level >= 1) {
                            log.println("Invalid state: " + this.state);
                        }
                        this.state = 0;
                        this.thread = null;
                        break;
                    }
                case 4:
                    this.state = 0;
                    throw new TransportException("Connection in error", this.te);
            }
        } catch (InterruptedException ie) {
            this.state = 0;
            this.thread = null;
            throw new TransportException(ie);
        } catch (Throwable th) {
            if (!(this.state == 0 || this.state == 3 || this.state == 4)) {
                LogStream logStream4 = log;
                if (LogStream.level >= 1) {
                    log.println("Invalid state: " + this.state);
                }
                this.state = 0;
                this.thread = null;
            }
            throw th;
        }
    }

    public synchronized void disconnect(boolean hard) throws IOException {
        IOException ioe = null;
        switch (this.state) {
            case 0:
                break;
            case 1:
            default:
                LogStream logStream = log;
                if (LogStream.level >= 1) {
                    log.println("Invalid state: " + this.state);
                }
                this.thread = null;
                this.state = 0;
                break;
            case 2:
                hard = true;
            case 3:
                if (this.response_map.size() == 0 || hard) {
                    try {
                        doDisconnect(hard);
                    } catch (IOException ioe0) {
                        ioe = ioe0;
                    }
                }
            case 4:
                this.thread = null;
                this.state = 0;
                break;
        }
        if (ioe != null) {
            throw ioe;
        }
    }

    @Override // java.lang.Runnable
    public void run() {
        Thread run_thread = Thread.currentThread();
        Exception ex0 = null;
        try {
            doConnect();
            synchronized (run_thread) {
                if (run_thread != this.thread) {
                    if (0 != 0) {
                        LogStream logStream = log;
                        if (LogStream.level >= 2) {
                            ex0.printStackTrace(log);
                        }
                    }
                    return;
                }
                if (0 != 0) {
                    this.te = new TransportException((Throwable) null);
                }
                this.state = 2;
                run_thread.notify();
                loop();
            }
        } catch (Exception ex) {
            synchronized (run_thread) {
                if (run_thread != this.thread) {
                    if (ex != null) {
                        LogStream logStream2 = log;
                        if (LogStream.level >= 2) {
                            ex.printStackTrace(log);
                        }
                    }
                    return;
                }
                if (ex != null) {
                    this.te = new TransportException(ex);
                }
                this.state = 2;
                run_thread.notify();
            }
        } catch (Throwable th) {
            synchronized (run_thread) {
                if (run_thread != this.thread) {
                    if (0 != 0) {
                        LogStream logStream3 = log;
                        if (LogStream.level >= 2) {
                            ex0.printStackTrace(log);
                        }
                    }
                    return;
                }
                if (0 != 0) {
                    this.te = new TransportException((Throwable) null);
                }
                this.state = 2;
                run_thread.notify();
                throw th;
            }
        }
    }

    @Override // java.lang.Object
    public String toString() {
        return this.name;
    }
}
