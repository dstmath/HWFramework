package tmsdkobf;

import android.content.Context;
import android.net.NetworkInfo;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.Iterator;
import tmsdk.common.TMServiceFactory;
import tmsdk.common.creator.ManagerCreatorC;
import tmsdkobf.nw.f;
import tmsdkobf.on.b;

public class oq {
    protected om CU;
    private byte ID;
    private boolean IE;
    private String IF;
    private volatile boolean IG;
    private Thread IH;
    private final Object II;
    private Socket IJ;
    private DataOutputStream IK;
    private DataInputStream IL;
    private a IM;
    private boolean IN;
    private Context mContext;

    public interface a {
        void a(int i, Object obj);

        void bE(int i);

        void d(int i, byte[] bArr);
    }

    public oq(Context context, byte b, boolean z, a aVar, om omVar) {
        this.ID = (byte) 0;
        this.IE = true;
        this.IF = "";
        this.IG = true;
        this.II = new Object();
        this.IN = false;
        this.mContext = context;
        this.ID = (byte) b;
        this.IE = z;
        this.IM = aVar;
        this.CU = omVar;
    }

    public oq(Context context, a aVar, om omVar) {
        this(context, (byte) 0, false, aVar, omVar);
    }

    /* JADX WARNING: Missing block: B:16:0x0040, code:
            return 0;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private synchronized int a(Context context, boolean z) {
        mb.d("TcpNetwork", "[tcp_control]start() isRestart " + z);
        if (isStarted()) {
            mb.d("TcpNetwork", "start() already started");
            return 0;
        } else if (hm()) {
            if (this.IM != null) {
                this.IM.bE(3);
            }
            int a = a(this.CU);
            if (a == 0) {
                this.IG = false;
                if (this.ID == (byte) 0) {
                    mb.d("TcpNetwork", "[tcp_control]connect succ, startRcvThread()");
                    hh();
                }
                if (this.IM != null) {
                    if (z) {
                        this.IM.bE(5);
                    } else {
                        this.IM.bE(4);
                    }
                }
            } else {
                mb.s("TcpNetwork", "[tcp_control]connect failed, donot startRcvThread()");
                return a;
            }
        } else {
            mb.d("TcpNetwork", "start(), no connect");
            return -220000;
        }
    }

    private int a(om omVar) {
        mb.d("TcpNetwork", "[tcp_control] checkSocketWithRetry()");
        long currentTimeMillis = System.currentTimeMillis();
        int i = 0;
        omVar.D(true);
        int G = omVar.G(true);
        b bVar = null;
        long j = 0;
        int i2 = 0;
        while (i2 < G) {
            bVar = omVar.B(true);
            if (bVar != null) {
                long currentTimeMillis2 = System.currentTimeMillis();
                i = b(bVar);
                j = System.currentTimeMillis() - currentTimeMillis2;
                mb.n("TcpNetwork", "checkSocketWithRetry(), ipPoint " + bVar.toString() + " localIp " + ho() + " localPort " + hp() + " ret: " + i);
                if (i == 0 || !ne.bk(i)) {
                    break;
                } else if (i2 == 0 && nu.ch("tcp connect")) {
                    i = -160000;
                    break;
                } else {
                    omVar.C(true);
                }
            }
            i2++;
        }
        omVar.E(i == 0);
        if (bVar != null) {
            final oe oeVar = new oe();
            oeVar.HB = bVar.hd();
            oeVar.HC = String.valueOf(bVar.getPort());
            oeVar.HE = String.valueOf(nh.w(this.mContext));
            oeVar.HG = j;
            oeVar.errorCode = i;
            oeVar.HH = this.IF;
            oeVar.HD = i2 >= G ? G : i2 + 1;
            oeVar.u(omVar.F(true));
            if ((i2 != G ? null : 1) == null) {
                oeVar.HL = false;
                oeVar.HI = "false";
                nz nzVar = (nz) ManagerCreatorC.getManager(nz.class);
                if (nzVar != null) {
                    oeVar.d(nzVar.gl());
                }
            } else {
                ((ki) fj.D(4)).addTask(new Runnable() {
                    public void run() {
                        oeVar.HL = true;
                        oeVar.HM = nu.ci("tcp connect");
                        oeVar.HI = "true";
                        nz nzVar = (nz) ManagerCreatorC.getManager(nz.class);
                        if (nzVar != null) {
                            oeVar.d(nzVar.gl());
                        }
                    }
                }, "uploadConnectInfo");
            }
        }
        mb.n("TcpNetwork", "[tcp_control] checkSocketWithRetry(), ret: " + i + " time: " + (System.currentTimeMillis() - currentTimeMillis));
        return i;
    }

    private Socket a(InetAddress inetAddress, int i) throws IOException {
        mb.n("TcpNetwork", "acquireSocketWithTimeOut, addr: " + inetAddress + ", port: " + i);
        Socket socket = new Socket();
        socket.setSoLinger(false, 0);
        socket.connect(new InetSocketAddress(inetAddress, i), 15000);
        mb.n("TcpNetwork", "acquireSocketWithTimeOut end");
        return socket;
    }

    private boolean a(b bVar) throws IOException {
        mb.d("TcpNetwork", "[tcp_control]startSocket()");
        if (!hk()) {
            mb.n("TcpNetwork", "startSocket() 1");
            hj();
        }
        mb.n("TcpNetwork", "startSocket() 2");
        InetAddress byName = InetAddress.getByName(bVar.hd());
        mb.n("TcpNetwork", "startSocket() 3");
        this.IJ = a(byName, bVar.getPort());
        mb.n("TcpNetwork", "startSocket() 4");
        switch (this.ID) {
            case (byte) 0:
                this.IK = new DataOutputStream(this.IJ.getOutputStream());
                mb.n("TcpNetwork", "startSocket() 5");
                this.IL = new DataInputStream(this.IJ.getInputStream());
                break;
            case (byte) 1:
                this.IJ.setSoTimeout(15000);
                break;
        }
        mb.n("TcpNetwork", "startSocket() 6");
        return hl();
    }

    /* JADX WARNING: Missing block: B:10:?, code:
            r17.IF = "";
            tmsdkobf.mb.n("TcpNetwork", "sendDataInAsync() succ");
     */
    /* JADX WARNING: Missing block: B:12:0x0075, code:
            return 0;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private int b(f fVar, byte[] bArr) {
        try {
            synchronized (this.IJ) {
                if (hl()) {
                    OutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);
                    dataOutputStream.writeInt(bArr.length);
                    dataOutputStream.write(bArr);
                    byte[] toByteArray = byteArrayOutputStream.toByteArray();
                    mb.n("TcpNetwork", "[tcp_control]sendDataInAsync(), bf [tcp send] bytes: " + toByteArray.length);
                    this.IK.write(toByteArray);
                    mb.d("TcpNetwork", "[flow_control][tcp_control]sendDataInAsync(), [tcp send] bytes: " + toByteArray.length);
                    if (!(fVar == null || fVar.Ft == null || fVar.Ft.size() <= 0)) {
                        int size = fVar.Ft.size();
                        Iterator it = fVar.Ft.iterator();
                        while (it.hasNext()) {
                            bw bwVar = (bw) it.next();
                            if (bwVar != null) {
                                nt.ga().a("TcpNetwork", bwVar.bz, bwVar.ey, bwVar, 12, 0, String.format("%d/%d", new Object[]{Integer.valueOf(toByteArray.length), Integer.valueOf(size)}));
                                if (bwVar.ez == 0) {
                                    oe.a(new oe(), bwVar.ey);
                                }
                            }
                        }
                    }
                } else {
                    return -180000;
                }
            }
        } catch (SocketException e) {
            this.IF = e.toString();
            mb.o("TcpNetwork", "sendDataInAsync() SocketException: " + e.toString());
            return -330000;
        } catch (Throwable th) {
            this.IF = th.toString();
            mb.o("TcpNetwork", "sendDataInAsync() Throwable: " + th.toString());
            return -320000;
        }
    }

    private int b(b bVar) {
        int i;
        String unknownHostException;
        mb.d("TcpNetwork", "[tcp_control]checkSocket()");
        if (bVar == null) {
            return -10;
        }
        if (hl()) {
            mb.s("TcpNetwork", "[tcp_control]checkSocket(), already contected");
            return 0;
        }
        try {
            if (a(bVar)) {
                i = 0;
                mb.r("TcpNetwork", "[tcp_control]checkSocket(), startSocket succ, set: mIsIgnoreStopExption = false");
                this.IN = false;
            } else {
                i = -340000;
            }
            this.IF = "";
        } catch (Throwable e) {
            i = -70000;
            mb.b("TcpNetwork", "checkSocket(), UnknownHostException: ", e);
            if (this.IM != null) {
                this.IM.a(7, bVar);
            }
            unknownHostException = e.toString();
        } catch (Throwable e2) {
            i = -130000;
            mb.b("TcpNetwork", "checkSocket(), SocketTimeoutException: ", e2);
            if (this.IM != null) {
                this.IM.a(8, bVar);
            }
            unknownHostException = e2.toString();
        } catch (Throwable e22) {
            i = ne.f(e22.toString(), -500000);
            mb.b("TcpNetwork", "checkSocket(), ConnectException: ", e22);
            if (this.IM != null) {
                this.IM.a(9, bVar);
            }
            unknownHostException = e22.toString();
        } catch (Throwable e222) {
            i = ne.f(e222.toString(), -420000);
            mb.b("TcpNetwork", "checkSocket(), SocketException: ", e222);
            if (this.IM != null) {
                this.IM.a(9, bVar);
            }
            unknownHostException = e222.toString();
        } catch (Throwable e2222) {
            i = ne.f(e2222.toString(), -440000);
            mb.b("TcpNetwork", "checkSocket(), SecurityException: ", e2222);
            if (this.IM != null) {
                this.IM.a(9, bVar);
            }
            unknownHostException = e2222.toString();
        } catch (Throwable e22222) {
            i = -900000;
            mb.b("TcpNetwork", "checkSocket(), Throwable: ", e22222);
            if (this.IM != null) {
                this.IM.a(9, bVar);
            }
            unknownHostException = e22222.toString();
        }
        return i;
        this.IF = unknownHostException;
        return i;
    }

    private synchronized int e(boolean z, boolean z2) {
        int hj;
        mb.n("TcpNetwork", "[tcp_control]stop(),  bySvr: " + z + " isRestart: " + z2);
        if (!z) {
            mb.d("TcpNetwork", "[tcp_control]stop(), !bySvr, set: mIsIgnoreStopExption = true");
            this.IN = true;
        }
        this.IG = true;
        hj = hj();
        if (hj == 0) {
            if (this.IM != null) {
                if (z) {
                    this.IM.bE(0);
                } else if (z2) {
                    this.IM.bE(2);
                } else {
                    this.IM.bE(1);
                }
            }
        } else if (this.IM != null) {
            this.IM.a(6, "stop socket failed: " + this.IF);
        }
        return hj;
    }

    private void e(final int i, final byte[] bArr) {
        if (this.IM != null) {
            ((ki) fj.D(4)).addTask(new Runnable() {
                public void run() {
                    try {
                        oq.this.IM.d(i, bArr);
                    } catch (Throwable th) {
                        mb.e("TcpNetwork", th);
                    }
                }
            }, "shark-onreceive-callback");
        }
    }

    private void hh() {
        this.IH = new Thread("RcvThread") {
            public void run() {
                mb.d("TcpNetwork", "[tcp_control]RcvThread start...");
                oq.this.hi();
                mb.d("TcpNetwork", "[tcp_control]RcvThread end!");
            }
        };
        this.IH.setPriority(10);
        this.IH.start();
    }

    private void hi() {
        mb.d("TcpNetwork", "[tcp_control]recv()...");
        while (!this.IG) {
            int i = 0;
            try {
                if (this.IE) {
                    i = this.IL.readInt();
                }
                int readInt = this.IL.readInt();
                if (readInt < 1000000) {
                    mb.d("TcpNetwork", "[flow_control][tcp_control]recv(), [tcp receive] bytes: " + (readInt + 4));
                    byte[] a = on.a(this.IL, 0, readInt, null);
                    if (a != null) {
                        mb.d("TcpNetwork", "[tcp_control]notifyOnReceiveData(), respData.length(): " + a.length);
                        e(i, a);
                    } else {
                        mb.o("TcpNetwork", "[tcp_control]recv(), respData == null");
                    }
                } else {
                    mb.o("TcpNetwork", "[flow_control][tcp_control]包有误，数据过大，size >= 1000000, [tcp receive] bytes: " + readInt);
                    return;
                }
            } catch (Throwable e) {
                mb.c("TcpNetwork", "[tcp_control]recv(), SocketException: " + e, e);
                if (this.IN) {
                    mb.d("TcpNetwork", "[tcp_control]ignore stop exption");
                    this.IG = true;
                } else {
                    e(true, false);
                    if (this.IM != null) {
                        this.IM.a(10, e);
                    }
                }
            } catch (Throwable e2) {
                mb.c("TcpNetwork", "[tcp_control]recv() EOFException: " + e2, e2);
                if (this.IN) {
                    mb.d("TcpNetwork", "[tcp_control]ignore stop exption");
                    this.IG = true;
                } else {
                    e(true, false);
                    if (this.IM != null) {
                        this.IM.a(11, e2);
                    }
                }
            } catch (Throwable e22) {
                mb.c("TcpNetwork", "[tcp_control]recv() Throwable: " + e22, e22);
                if (this.IN) {
                    mb.d("TcpNetwork", "[tcp_control]ignore stop exption");
                    this.IG = true;
                } else {
                    e(true, false);
                    if (this.IM != null) {
                        this.IM.a(12, e22);
                    }
                }
            }
        }
        if (!this.IN) {
            stop();
        }
        mb.d("TcpNetwork", "[tcp_control]recv(), recv thread is stopped, set: mIsIgnoreStopExption = false");
        this.IN = false;
        mb.d("TcpNetwork", "[tcp_control]recv(), end!!!");
    }

    /* JADX WARNING: Removed duplicated region for block: B:34:0x007d A:{SYNTHETIC} */
    /* JADX WARNING: Removed duplicated region for block: B:62:0x0115 A:{SYNTHETIC, Splitter: B:62:0x0115} */
    /* JADX WARNING: Removed duplicated region for block: B:34:0x007d A:{SYNTHETIC} */
    /* JADX WARNING: Removed duplicated region for block: B:34:0x007d A:{SYNTHETIC} */
    /* JADX WARNING: Missing block: B:6:0x0016, code:
            tmsdkobf.mb.n("TcpNetwork", "stopSocket() 1");
     */
    /* JADX WARNING: Missing block: B:7:0x0023, code:
            if (hk() != false) goto L_0x00db;
     */
    /* JADX WARNING: Missing block: B:8:0x0025, code:
            tmsdkobf.mb.n("TcpNetwork", "stopSocket() 2");
            r3 = r10.II;
     */
    /* JADX WARNING: Missing block: B:9:0x0030, code:
            monitor-enter(r3);
     */
    /* JADX WARNING: Missing block: B:11:?, code:
            tmsdkobf.mb.n("TcpNetwork", "stopSocket() 3");
     */
    /* JADX WARNING: Missing block: B:14:0x0040, code:
            if (r10.IJ.isInputShutdown() == false) goto L_0x00e5;
     */
    /* JADX WARNING: Missing block: B:49:0x00db, code:
            tmsdkobf.mb.s("TcpNetwork", "[tcp_control]stopSocket(), already closed");
     */
    /* JADX WARNING: Missing block: B:50:0x00e4, code:
            return 0;
     */
    /* JADX WARNING: Missing block: B:52:?, code:
            r10.IJ.shutdownInput();
     */
    /* JADX WARNING: Missing block: B:53:0x00ec, code:
            r4 = move-exception;
     */
    /* JADX WARNING: Missing block: B:55:?, code:
            tmsdkobf.mb.d("TcpNetwork", "stopSocket(), mSocket.shutdownInput() " + r4);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private int hj() {
        mb.n("TcpNetwork", "[tcp_control]stopSocket()");
        long currentTimeMillis = System.currentTimeMillis();
        synchronized (this.II) {
            if (this.IJ != null) {
            } else {
                mb.s("TcpNetwork", "[tcp_control]stopSocket(), mSocket is null");
                return 0;
            }
        }
        int i;
        String interruptedException;
        mb.n("TcpNetwork", "stopSocket() 5");
        try {
            if (!this.IJ.isOutputShutdown()) {
                this.IJ.shutdownOutput();
            }
        } catch (Throwable th) {
            mb.d("TcpNetwork", "stopSocket(), mSocket.shutdownOutput() " + th);
        }
        mb.n("TcpNetwork", "stopSocket() 6");
        try {
            this.IK.close();
        } catch (Throwable th2) {
            mb.d("TcpNetwork", "stopSocket(), mSocketWriter.close() " + th2);
        }
        i = 0;
        try {
            mb.n("TcpNetwork", "stopSocket() 7");
            synchronized (this.II) {
                mb.n("TcpNetwork", "stopSocket() 8");
                this.IJ.close();
                this.IJ = null;
                mb.n("TcpNetwork", "stopSocket() 9");
            }
            Thread.sleep(2000);
            this.IF = "";
        } catch (InterruptedException e) {
            i = -270000;
            mb.d("TcpNetwork", "stopSocket(), InterruptedException: " + e);
            interruptedException = e.toString();
        } catch (IOException e2) {
            i = -140000;
            mb.d("TcpNetwork", "stopSocket(), IOException: " + e2);
            interruptedException = e2.toString();
        } catch (Throwable th22) {
            i = -900000;
            mb.d("TcpNetwork", "stopSocket(), Throwable: " + th22);
            interruptedException = th22.toString();
        }
        mb.n("TcpNetwork", "[tcp_control]stopSocket(), ret: " + i + " stop action use(ms): " + (System.currentTimeMillis() - currentTimeMillis));
        return i;
        mb.n("TcpNetwork", "stopSocket() 6");
        this.IK.close();
        i = 0;
        mb.n("TcpNetwork", "stopSocket() 7");
        synchronized (this.II) {
        }
        Thread.sleep(2000);
        this.IF = "";
        mb.n("TcpNetwork", "[tcp_control]stopSocket(), ret: " + i + " stop action use(ms): " + (System.currentTimeMillis() - currentTimeMillis));
        return i;
        mb.n("TcpNetwork", "stopSocket() 4");
        try {
            this.IL.close();
        } catch (Object th3) {
            mb.d("TcpNetwork", th3);
        }
        mb.n("TcpNetwork", "stopSocket() 5");
        if (this.IJ.isOutputShutdown()) {
        }
        mb.n("TcpNetwork", "stopSocket() 6");
        this.IK.close();
        i = 0;
        mb.n("TcpNetwork", "stopSocket() 7");
        synchronized (this.II) {
        }
        Thread.sleep(2000);
        this.IF = "";
        mb.n("TcpNetwork", "[tcp_control]stopSocket(), ret: " + i + " stop action use(ms): " + (System.currentTimeMillis() - currentTimeMillis));
        return i;
        this.IF = interruptedException;
        mb.n("TcpNetwork", "[tcp_control]stopSocket(), ret: " + i + " stop action use(ms): " + (System.currentTimeMillis() - currentTimeMillis));
        return i;
        i = 0;
        mb.n("TcpNetwork", "stopSocket() 7");
        synchronized (this.II) {
        }
        Thread.sleep(2000);
        this.IF = "";
        mb.n("TcpNetwork", "[tcp_control]stopSocket(), ret: " + i + " stop action use(ms): " + (System.currentTimeMillis() - currentTimeMillis));
        return i;
    }

    private boolean hk() {
        mb.n("TcpNetwork", "isSocketClosed()");
        synchronized (this.II) {
            mb.n("TcpNetwork", "isSocketClosed() 1");
            if (this.IJ != null) {
                boolean isClosed = this.IJ.isClosed();
                mb.n("TcpNetwork", "isSocketClosed() 2");
                return isClosed;
            }
            return true;
        }
    }

    private NetworkInfo hn() {
        NetworkInfo networkInfo = null;
        try {
            return TMServiceFactory.getSystemInfoService().getActiveNetworkInfo();
        } catch (NullPointerException e) {
            mb.s("TcpNetwork", " getActiveNetworkInfo NullPointerException--- \n" + e.getMessage());
            return networkInfo;
        }
    }

    private String ho() {
        synchronized (this.II) {
            if (this.IJ != null) {
                String inetAddress = this.IJ.getLocalAddress().toString();
                return inetAddress;
            }
            return "null";
        }
    }

    private int hp() {
        synchronized (this.II) {
            if (this.IJ != null) {
                int localPort = this.IJ.getLocalPort();
                return localPort;
            }
            return 0;
        }
    }

    private boolean isStarted() {
        return !this.IG;
    }

    private int u(byte[] bArr) {
        try {
            this.IK.writeInt(bArr.length);
            this.IK.write(bArr);
            return 0;
        } catch (Throwable th) {
            mb.o("TcpNetwork", "sendDataInSync() Throwable: " + th.toString());
            return -310000;
        }
    }

    public int C(Context context) {
        return a(context, false);
    }

    public int a(f fVar, byte[] bArr) {
        if (hk()) {
            return -190000;
        }
        if (!hl()) {
            return -180000;
        }
        if (fVar.gp()) {
            mb.o("TcpNetwork", "[time_out]sendDataAsync(), send time out");
            return -17;
        }
        int i = -1;
        switch (this.ID) {
            case (byte) 0:
                i = b(fVar, bArr);
                break;
            case (byte) 1:
                i = u(bArr);
                break;
        }
        return i;
    }

    public om gQ() {
        return this.CU;
    }

    public String hf() {
        return this.IF;
    }

    protected int hg() {
        return e(false, true) == 0 ? a(this.mContext, true) : -210000;
    }

    protected boolean hl() {
        boolean z = false;
        mb.n("TcpNetwork", "isSocketConnected()");
        synchronized (this.II) {
            mb.n("TcpNetwork", "isSocketConnected() 1");
            if (this.IJ != null) {
                mb.n("TcpNetwork", "isSocketConnected() 2");
                if (!hk() && this.IJ.isConnected()) {
                    z = true;
                }
                mb.n("TcpNetwork", "isSocketConnected() 3");
                return z;
            }
            return false;
        }
    }

    public boolean hm() {
        NetworkInfo hn = hn();
        return hn != null ? hn.isConnected() : false;
    }

    public int stop() {
        return e(false, false);
    }
}
