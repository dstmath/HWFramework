package tmsdkobf;

import android.content.Context;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import tmsdk.common.ErrorCode;
import tmsdk.common.TMServiceFactory;
import tmsdk.common.utils.d;
import tmsdk.common.utils.f;
import tmsdk.fg.module.spacemanager.SpaceManager;
import tmsdk.fg.module.urlcheck.IUrlMonitorCallback;
import tmsdkobf.pr.b;

/* compiled from: Unknown */
public class pv {
    static final /* synthetic */ boolean fJ = false;
    private byte IJ;
    private boolean IK;
    private boolean IL;
    private Thread IM;
    private Object IN;
    private Socket IO;
    private DataOutputStream IP;
    private DataInputStream IQ;
    protected pp IR;
    private b IS;
    private a IT;
    private boolean IU;
    private Handler IV;
    private Context mContext;

    /* compiled from: Unknown */
    public interface a {
        void a(int i, Object obj);

        void b(int i, byte[] bArr);
    }

    /* compiled from: Unknown */
    /* renamed from: tmsdkobf.pv.1 */
    class AnonymousClass1 extends Handler {
        final /* synthetic */ pv IW;

        AnonymousClass1(pv pvVar, Looper looper) {
            this.IW = pvVar;
            super(looper);
        }

        public void handleMessage(Message message) {
            switch (message.what) {
                case IUrlMonitorCallback.DES_ID_HOBBYHORSE /*1*/:
                    this.IW.IU = false;
                default:
            }
        }
    }

    /* compiled from: Unknown */
    /* renamed from: tmsdkobf.pv.2 */
    class AnonymousClass2 extends Thread {
        final /* synthetic */ pv IW;

        AnonymousClass2(pv pvVar, String str) {
            this.IW = pvVar;
            super(str);
        }

        public void run() {
            d.e("TcpNetwork", "RcvThread start...");
            this.IW.hp();
            d.e("TcpNetwork", "RcvThread stop...");
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdkobf.pv.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdkobf.pv.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: tmsdkobf.pv.<clinit>():void");
    }

    public pv() {
        this((byte) 0, false);
    }

    public pv(byte b, boolean z) {
        this.IJ = (byte) 0;
        this.IK = true;
        this.IL = true;
        this.IN = new Object();
        this.IU = false;
        this.IV = new AnonymousClass1(this, Looper.getMainLooper());
        this.IJ = (byte) b;
        this.IK = z;
    }

    private synchronized int a(Context context, pp ppVar, boolean z) {
        d.e("TcpNetwork", "start() isRestart " + z);
        if (isStarted()) {
            d.e("TcpNetwork", "start() isStarted() " + isStarted());
            return 0;
        } else if (ppVar != null) {
            this.mContext = context;
            if (hv()) {
                this.IR = ppVar;
                if (this.IT != null) {
                    this.IT.a(3, null);
                }
                int b = b(this.IR);
                if (b == 0) {
                    this.IL = false;
                    if (this.IJ == null) {
                        d.e("TcpNetwork", "start() startRcvThread()");
                        ho();
                    }
                    if (this.IT != null) {
                        if (z) {
                            this.IT.a(5, null);
                        } else {
                            this.IT.a(4, null);
                        }
                    }
                    return 0;
                }
                d.e("TcpNetwork", "start() checkSocket() !ret");
                return b;
            }
            d.e("TcpNetwork", "start() !NetworkUtil.isNetworkConnected()");
            return -220000;
        } else {
            d.e("TcpNetwork", "start() null == ipPlot");
            return -240000;
        }
    }

    private boolean a(b bVar) throws IOException {
        if (!hr()) {
            hq();
        }
        this.IS = bVar;
        this.IO = a(InetAddress.getByName(bVar.fZ()), bVar.getPort());
        if (this.IO == null) {
            return false;
        }
        switch (this.IJ) {
            case SpaceManager.ERROR_CODE_OK /*0*/:
                this.IP = new DataOutputStream(this.IO.getOutputStream());
                this.IQ = new DataInputStream(this.IO.getInputStream());
                break;
            case IUrlMonitorCallback.DES_ID_HOBBYHORSE /*1*/:
                this.IO.setSoTimeout(60000);
                break;
        }
        return hs();
    }

    public static byte[] a(InputStream inputStream, int i, int i2, tmsdkobf.pr.a aVar) throws IOException {
        byte[] bArr = new byte[i2];
        int i3 = 0;
        int i4 = i2;
        while (i3 < i2 && i4 > 0) {
            int read = inputStream.read(bArr, i, i4);
            if (read >= 0) {
                i3 += read;
                i += read;
                i4 -= read;
                if (aVar != null) {
                    aVar.a(true, i3, i2);
                }
            } else if (aVar != null) {
                aVar.a(true, i3, i2);
            }
        }
        return i3 == i2 ? bArr : null;
    }

    private int b(pp ppVar) {
        boolean z = false;
        d.e("TcpNetwork", "checkSocketWithRetry()");
        ppVar.gN();
        int gO = ppVar.gO() * ppVar.gP();
        int i = 0;
        int i2 = 0;
        while (i < gO) {
            b gM = ppVar.gM();
            if (gM != null) {
                pu.a(new pu());
                long currentTimeMillis = System.currentTimeMillis();
                i2 = b(gM);
                pu.hm().IF = System.currentTimeMillis() - currentTimeMillis;
                pu.hm().AR = i2;
                pu.hm().v = f.A(this.mContext);
                pu.hm().ja = gM.fZ();
                pu.hm().port = gM.getPort();
                if (i != 0) {
                    pu.hm().IH = true;
                }
                pu.hm().hn();
                pu.release();
                pa.a("TcpNetwork", "checkSocketWithRetry() ipPoint " + gM.toString() + " localIp " + ht() + " localPort " + hu() + " success ? " + i2, null, null);
                if (i2 == 0) {
                    break;
                }
                ppVar.gL();
                i++;
            } else {
                d.e("TcpNetwork", "checkSocketWithRetry() getPlotIPPoint() is null");
                return -240000;
            }
        }
        if (i2 == 0) {
            z = true;
        }
        ppVar.K(z);
        return i2;
    }

    private int b(b bVar) {
        int i = -900000;
        d.e("TcpNetwork", "checkSocket()");
        if (bVar == null) {
            return -240000;
        }
        if (hs()) {
            return 0;
        }
        try {
            if (a(bVar)) {
                i = 0;
            }
        } catch (UnknownHostException e) {
            i = -70000;
            mj.bB(-10010);
            d.e("TcpNetwork", "checkSocket() UnknownHostException " + e.toString());
            if (this.IT != null) {
                this.IT.a(7, bVar);
            }
        } catch (SocketTimeoutException e2) {
            i = -130000;
            mj.bB(-10011);
            d.e("TcpNetwork", "checkSocket() SocketTimeoutException " + e2.toString());
            if (this.IT != null) {
                this.IT.a(8, bVar);
            }
        } catch (Throwable th) {
            pu.hm().IG = th.toString();
            mj.bB(-10012);
            d.e("TcpNetwork", "checkSocket() Throwable " + th.toString());
            if (this.IT != null) {
                this.IT.a(9, bVar);
            }
        }
        return i;
    }

    private void b(int i, byte[] bArr) {
        if (this.IT != null) {
            try {
                this.IT.b(i, bArr);
            } catch (Throwable th) {
                mj.bB(-10016);
                pa.c("ocean", "[ocean]ERR: " + th.toString(), null, null);
                d.c("TcpNetwork", "recv() handleData() Throwable " + th.toString());
                this.IT.a(6, null);
            }
        }
    }

    private synchronized boolean d(boolean z, boolean z2) {
        d.e("TcpNetwork", "stop() bySvr " + z + " isRestart " + z2);
        if (!z) {
            this.IU = true;
        }
        this.IL = true;
        if (!hq()) {
            return false;
        }
        if (this.IT != null) {
            if (z) {
                this.IT.a(0, null);
            } else if (z2) {
                this.IT.a(2, null);
            } else {
                this.IT.a(1, null);
            }
        }
        d.e("TcpNetwork", "stop() bySvr " + z + " isRestart " + z2 + " stop() done");
        return true;
    }

    private void ho() {
        this.IM = new AnonymousClass2(this, "RcvThread");
        this.IM.setPriority(10);
        this.IM.start();
    }

    private void hp() {
        d.e("TcpNetwork", "recv start...");
        while (!this.IL) {
            try {
                if (!fJ) {
                    if (this.IQ == null) {
                        throw new AssertionError("null != mSocketReader");
                    }
                }
                int readInt = !this.IK ? 0 : this.IQ.readInt();
                int readInt2 = this.IQ.readInt();
                if (!fJ && readInt2 < 0) {
                    throw new AssertionError("recv() size < 4");
                } else if (readInt2 < 1000000) {
                    byte[] a = a(this.IQ, 0, readInt2, null);
                    if (a == null) {
                        d.e("TcpNetwork", "recv(), respData == null");
                    }
                    b(readInt, a);
                } else {
                    d.c("TcpNetwork", "\u5305\u6709\u8bef\uff0c\u6570\u636e\u8fc7\u5927\uff0csize >= 1000000");
                    return;
                }
            } catch (SocketException e) {
                d.c("TcpNetwork", "recv() SocketException " + e.toString());
                if (this.IU) {
                    d.e("TcpNetwork", "ignore stop exption");
                    this.IL = true;
                } else {
                    d(true, false);
                    if (this.IT != null) {
                        this.IT.a(10, null);
                    }
                }
            } catch (EOFException e2) {
                d.c("TcpNetwork", "recv() EOFException " + e2.toString());
                if (this.IU) {
                    d.e("TcpNetwork", "ignore stop exption");
                    this.IL = true;
                } else {
                    d(true, false);
                    if (this.IT != null) {
                        this.IT.a(11, null);
                    }
                }
            } catch (Throwable th) {
                d.c("TcpNetwork", "recv() Throwable " + th.toString());
                if (this.IU) {
                    d.e("TcpNetwork", "ignore stop exption");
                    this.IL = true;
                } else {
                    d(true, false);
                    if (this.IT != null) {
                        this.IT.a(12, null);
                    }
                }
            }
        }
        if (!this.IU) {
            fH();
        }
        this.IU = false;
        d.e("TcpNetwork", "recv stop...");
    }

    private boolean hq() {
        boolean z;
        pa.h("TcpNetwork", "stop socket");
        if (hr()) {
            pa.h("TcpNetwork", "stop socket success:true");
            return true;
        }
        if (!this.IO.isInputShutdown()) {
            try {
                this.IO.shutdownInput();
            } catch (Throwable th) {
                mj.bB(ErrorCode.ERR_CORRECTION_PROFILE_UPLOAD_FAIL);
                d.e("TcpNetwork", "stopSocket() mSocket.shutdownInput() " + th);
            }
        }
        try {
            this.IQ.close();
        } catch (Throwable th2) {
            mj.bB(ErrorCode.ERR_CORRECTION_LOCAL_TEMPLATE_UNMATCH);
            d.e("TcpNetwork", th2);
        }
        if (!this.IO.isOutputShutdown()) {
            try {
                this.IO.shutdownOutput();
            } catch (Throwable th22) {
                mj.bB(ErrorCode.ERR_CORRECTION_LOCAL_NO_TEMPLATE);
                d.e("TcpNetwork", "stopSocket() mSocket.shutdownOutput() " + th22);
            }
        }
        try {
            this.IP.close();
        } catch (Throwable th222) {
            mj.bB(ErrorCode.ERR_CORRECTION_PROFILE_ILLEGAL);
            d.e("TcpNetwork", "stopSocket() mSocketWriter.close() " + th222);
        }
        try {
            this.IO.close();
            synchronized (this.IN) {
                this.IO = null;
            }
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            mj.bB(-10008);
            e.printStackTrace();
            d.e("TcpNetwork", "stopSocket() InterruptedException " + e);
        } catch (IOException e2) {
            mj.bB(-10009);
            d.e("TcpNetwork", "stopSocket() mSocket.close() " + e2);
            z = false;
            pa.h("TcpNetwork", "stop socket success:" + z);
            return z;
        } catch (Throwable th2222) {
            d.e("TcpNetwork", "stopSocket() mSocket.close() " + th2222);
            z = false;
            pa.h("TcpNetwork", "stop socket success:" + z);
            return z;
        }
        z = true;
        pa.h("TcpNetwork", "stop socket success:" + z);
        return z;
    }

    private boolean hr() {
        boolean z = true;
        synchronized (this.IN) {
            if (this.IO != null) {
                if (this.IO != null) {
                    if (!this.IO.isClosed()) {
                    }
                    return z;
                }
                z = false;
                return z;
            }
            return true;
        }
    }

    private NetworkInfo hw() {
        try {
            return TMServiceFactory.getSystemInfoService().getActiveNetworkInfo();
        } catch (NullPointerException e) {
            mj.bB(-10017);
            d.f("getActiveNetworkInfo", " getActiveNetworkInfo NullPointerException--- \n" + e.getMessage());
            return null;
        }
    }

    private int y(byte[] bArr) {
        if (!fJ && this.IJ != null) {
            throw new AssertionError();
        } else if (!fJ && this.IP == null) {
            throw new AssertionError("mSocketWriter is null");
        } else {
            try {
                synchronized (this.IO) {
                    if (hs()) {
                        OutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                        DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);
                        dataOutputStream.writeInt(bArr.length);
                        dataOutputStream.write(bArr);
                        byte[] toByteArray = byteArrayOutputStream.toByteArray();
                        d.e("TcpNetwork", "sendDataInAsync() realSendData.lenght " + toByteArray.length);
                        this.IP.write(toByteArray);
                        this.IP.flush();
                        return 0;
                    }
                    return -180000;
                }
            } catch (SocketException e) {
                mj.bB(-10013);
                d.c("TcpNetwork", "sendDataInAsync() has a Throwable when sendDataInAsync() e " + e.toString());
                return -120000;
            } catch (Throwable th) {
                mj.bB(-10014);
                d.c("TcpNetwork", "sendDataInAsync() has a Throwable when sendDataInAsync() t " + th.toString());
                return -150000;
            }
        }
    }

    private int z(byte[] bArr) {
        if (fJ || 1 == this.IJ) {
            try {
                this.IP.writeInt(bArr.length);
                this.IP.write(bArr);
                this.IP.flush();
                return 0;
            } catch (Throwable th) {
                mj.bB(-10015);
                d.c("TcpNetwork", "sendDataInSync() has a Throwable when sendDataInsync() " + th.toString());
                return -150000;
            }
        }
        throw new AssertionError();
    }

    public int a(Context context, pp ppVar) {
        return a(context, ppVar, false);
    }

    protected int a(pp ppVar) {
        if (!d(false, true)) {
            return -210000;
        }
        if (this.mContext == null) {
            d.d("TmsTcpManager", "context == null\uff0c\u65e0\u6cd5start TcpNetwork");
        }
        return a(this.mContext, ppVar, true);
    }

    public Socket a(InetAddress inetAddress, int i) throws IOException {
        d.d("MMConnectionManager", "acquireSocketWithTimeOut, addr: " + inetAddress + ", port: " + i);
        Socket socket = new Socket();
        socket.setSoLinger(false, 0);
        socket.connect(new InetSocketAddress(inetAddress, i), 60000);
        return socket;
    }

    public void a(a aVar) {
        this.IT = aVar;
    }

    public boolean fH() {
        return d(false, false);
    }

    protected boolean hs() {
        boolean z = false;
        synchronized (this.IN) {
            if (this.IO != null) {
                if (!hr() && this.IO.isConnected()) {
                    z = true;
                }
                return z;
            }
            return false;
        }
    }

    public String ht() {
        return this.IO != null ? this.IO.getLocalAddress().toString() : "null";
    }

    public int hu() {
        return this.IO != null ? this.IO.getLocalPort() : 0;
    }

    public boolean hv() {
        NetworkInfo hw = hw();
        return hw != null ? hw.isConnected() : false;
    }

    public boolean isStarted() {
        return !this.IL;
    }

    public int x(byte[] bArr) {
        if (hr()) {
            return -190000;
        }
        if (!hs()) {
            return -180000;
        }
        int i = -900000;
        switch (this.IJ) {
            case SpaceManager.ERROR_CODE_OK /*0*/:
                i = y(bArr);
                break;
            case IUrlMonitorCallback.DES_ID_HOBBYHORSE /*1*/:
                i = z(bArr);
                break;
        }
        return i;
    }
}
