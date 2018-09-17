package jcifs.util.transport;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import jcifs.smb.SmbFile;
import jcifs.util.Encdec;
import jcifs.util.LogStream;

public abstract class Transport implements Runnable {
    static int id;
    static LogStream log;
    String name;
    protected HashMap response_map;
    int state;
    TransportException te;
    Thread thread;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: jcifs.util.transport.Transport.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: jcifs.util.transport.Transport.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: jcifs.util.transport.Transport.<clinit>():void");
    }

    protected abstract void doConnect() throws Exception;

    protected abstract void doDisconnect(boolean z) throws IOException;

    protected abstract void doRecv(Response response) throws IOException;

    protected abstract void doSend(Request request) throws IOException;

    protected abstract void doSkip() throws IOException;

    protected abstract void makeKey(Request request) throws IOException;

    protected abstract Request peekKey() throws IOException;

    public Transport() {
        this.state = 0;
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
        } catch (Throwable ie) {
            throw new TransportException(ie);
        } catch (Throwable th) {
            this.response_map.remove(request);
        }
    }

    private void loop() {
        LogStream logStream;
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
                        logStream = log;
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
                if (timeout) {
                    hard = false;
                } else {
                    hard = true;
                }
                if (!timeout) {
                    logStream = log;
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
            LogStream logStream;
            switch (this.state) {
                case SmbFile.FILE_NO_SHARE /*0*/:
                    this.state = 1;
                    this.te = null;
                    this.thread = new Thread(this, this.name);
                    this.thread.setDaemon(true);
                    synchronized (this.thread) {
                        this.thread.start();
                        this.thread.wait(timeout);
                        switch (this.state) {
                            case Encdec.TIME_1970_SEC_32BE /*1*/:
                                this.state = 0;
                                this.thread = null;
                                throw new TransportException("Connection timeout");
                            case Encdec.TIME_1970_SEC_32LE /*2*/:
                                if (this.te == null) {
                                    this.state = 3;
                                    if (!(this.state == 0 || this.state == 3 || this.state == 4)) {
                                        logStream = log;
                                        if (LogStream.level >= 1) {
                                            log.println("Invalid state: " + this.state);
                                        }
                                        this.state = 0;
                                        this.thread = null;
                                        break;
                                    }
                                    break;
                                }
                                this.state = 4;
                                this.thread = null;
                                throw this.te;
                            default:
                                if (!(this.state == 0 || this.state == 3 || this.state == 4)) {
                                    logStream = log;
                                    if (LogStream.level >= 1) {
                                        log.println("Invalid state: " + this.state);
                                    }
                                    this.state = 0;
                                    this.thread = null;
                                    break;
                                }
                                break;
                        }
                    }
                case Encdec.TIME_1904_SEC_32BE /*3*/:
                    if (!(this.state == 0 || this.state == 3 || this.state == 4)) {
                        logStream = log;
                        if (LogStream.level >= 1) {
                            log.println("Invalid state: " + this.state);
                        }
                        this.state = 0;
                        this.thread = null;
                        break;
                    }
                    break;
                case Encdec.TIME_1904_SEC_32LE /*4*/:
                    this.state = 0;
                    throw new TransportException("Connection in error", this.te);
                default:
                    TransportException te = new TransportException("Invalid state: " + this.state);
                    this.state = 0;
                    throw te;
            }
        } catch (Throwable ie) {
            this.state = 0;
            this.thread = null;
            throw new TransportException(ie);
        } catch (Throwable th) {
            if (!(this.state == 0 || this.state == 3 || this.state == 4)) {
                LogStream logStream2 = log;
                if (LogStream.level >= 1) {
                    log.println("Invalid state: " + this.state);
                }
                this.state = 0;
                this.thread = null;
            }
        }
    }

    public synchronized void disconnect(boolean hard) throws IOException {
        IOException ioe = null;
        switch (this.state) {
            case SmbFile.FILE_NO_SHARE /*0*/:
                break;
            case Encdec.TIME_1970_SEC_32LE /*2*/:
                hard = true;
                break;
            case Encdec.TIME_1904_SEC_32BE /*3*/:
                break;
            case Encdec.TIME_1904_SEC_32LE /*4*/:
                break;
            default:
                LogStream logStream = log;
                if (LogStream.level >= 1) {
                    log.println("Invalid state: " + this.state);
                }
                this.thread = null;
                this.state = 0;
                break;
        }
        if (this.response_map.size() == 0 || hard) {
            try {
                doDisconnect(hard);
            } catch (IOException ioe0) {
                ioe = ioe0;
            }
            this.thread = null;
            this.state = 0;
        }
        if (ioe != null) {
            throw ioe;
        }
    }

    public void run() {
        Thread run_thread = Thread.currentThread();
        Throwable ex0 = null;
        LogStream logStream;
        try {
            doConnect();
            synchronized (run_thread) {
                if (run_thread != this.thread) {
                    if (ex0 != null) {
                        logStream = log;
                        if (LogStream.level >= 2) {
                            ex0.printStackTrace(log);
                        }
                    }
                    return;
                }
                if (ex0 != null) {
                    this.te = new TransportException(ex0);
                }
                this.state = 2;
                run_thread.notify();
                loop();
            }
        } catch (Throwable ex) {
            ex0 = ex;
            synchronized (run_thread) {
            }
            if (run_thread != this.thread) {
                if (ex0 != null) {
                    logStream = log;
                    if (LogStream.level >= 2) {
                        ex0.printStackTrace(log);
                    }
                }
                return;
            }
            if (ex0 != null) {
                this.te = new TransportException(ex0);
            }
            this.state = 2;
            run_thread.notify();
        } catch (Throwable th) {
            synchronized (run_thread) {
            }
            if (run_thread != this.thread) {
                if (ex0 != null) {
                    logStream = log;
                    if (LogStream.level >= 2) {
                        ex0.printStackTrace(log);
                    }
                }
                return;
            }
            if (ex0 != null) {
                this.te = new TransportException(ex0);
            }
            this.state = 2;
            run_thread.notify();
        }
    }

    public String toString() {
        return this.name;
    }
}
