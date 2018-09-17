package android.rms.iaware;

import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.net.LocalSocketAddress.Namespace;
import java.io.IOException;
import java.io.OutputStream;

public class IAwaredConnection {
    private static final String LOCAL_SOCKET_NAME = "iawared";
    private static final String TAG = "IAwaredConnection";
    private static IAwaredConnection instance;
    private OutputStream outStream;
    private LocalSocket sock;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.rms.iaware.IAwaredConnection.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.rms.iaware.IAwaredConnection.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.rms.iaware.IAwaredConnection.<clinit>():void");
    }

    private IAwaredConnection() {
        this.sock = null;
        this.outStream = null;
    }

    public static IAwaredConnection getInstance() {
        IAwaredConnection iAwaredConnection;
        synchronized (IAwaredConnection.class) {
            if (instance == null) {
                instance = new IAwaredConnection();
            }
            iAwaredConnection = instance;
        }
        return iAwaredConnection;
    }

    public synchronized void create() {
        if (!createImpl()) {
            AwareLog.e(TAG, "Failed to create connection");
        }
    }

    public synchronized void destroy() {
        destroyImpl();
    }

    public boolean sendPacket(byte[] msg) {
        if (msg != null && msg.length != 0) {
            return sendPacket(msg, 0, msg.length);
        }
        AwareLog.e(TAG, "Parameter check failed");
        return false;
    }

    public synchronized boolean sendPacket(byte[] msg, int offset, int count) {
        if (msg != null && offset >= 0 && count > 0) {
            if (offset <= msg.length - count) {
                if (createImpl()) {
                    try {
                        this.outStream.write(msg, offset, count);
                        this.outStream.flush();
                        return true;
                    } catch (IOException e) {
                        AwareLog.e(TAG, "Failed to write output stream, IOException");
                        destroyImpl();
                        return false;
                    }
                }
                AwareLog.e(TAG, "Failed to create connection");
                return false;
            }
        }
        AwareLog.e(TAG, "Parameter check failed");
        return false;
    }

    private boolean createImpl() {
        if (this.sock != null) {
            return true;
        }
        try {
            this.sock = new LocalSocket(3);
            this.sock.connect(new LocalSocketAddress(LOCAL_SOCKET_NAME, Namespace.RESERVED));
            this.outStream = this.sock.getOutputStream();
            return true;
        } catch (IOException e) {
            AwareLog.e(TAG, "Failed to create connection, IOException");
            destroyImpl();
            return false;
        } catch (SecurityException e2) {
            AwareLog.e(TAG, "Failed to create connection, SecurityException");
            destroyImpl();
            return false;
        }
    }

    private void destroyImpl() {
        if (this.outStream != null) {
            try {
                this.outStream.close();
            } catch (IOException e) {
                AwareLog.e(TAG, "Failed to close output stream, IOException");
            } catch (Throwable th) {
                this.outStream = null;
            }
            this.outStream = null;
        }
        if (this.sock != null) {
            try {
                this.sock.close();
            } catch (IOException e2) {
                AwareLog.e(TAG, "Failed to close local socket, IOException");
            } catch (Throwable th2) {
                this.sock = null;
            }
            this.sock = null;
        }
    }
}
