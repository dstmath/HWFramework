package tmsdkobf;

import android.os.HandlerThread;
import android.os.Looper;
import android.text.TextUtils;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import tmsdk.common.CallerIdent;
import tmsdk.common.exception.WifiApproveException;
import tmsdk.common.utils.i;
import tmsdk.common.utils.u;
import tmsdk.common.utils.u.a;

public class nu {
    private static nl CT = null;
    private static boolean EA = false;
    private static boolean EB = false;
    private static boolean EC = false;
    private static oa ED = null;
    public static boolean Eu = false;
    public static boolean Ev = false;
    private static Looper Ew = null;
    private static boolean Ex = false;
    private static String Ey = null;
    private static boolean Ez = false;
    private static Looper sLooper = null;

    public static void H(boolean z) {
        Ex = z;
    }

    public static void I(boolean z) {
        Ez = z;
    }

    public static void J(boolean z) {
        EA = z;
    }

    public static void K(boolean z) {
        EB = z;
    }

    public static void L(boolean z) {
        EC = z;
    }

    public static void a(oa oaVar) {
        mb.d("SharkHelper", "[shark_init]initSharkQueueInstance(), sharkQueue: " + oaVar);
        ED = oaVar;
    }

    public static boolean aB() {
        return Ez;
    }

    public static boolean aC() {
        return EA;
    }

    public static boolean aD() {
        return EB;
    }

    private static long bL() {
        return CallerIdent.getIdent(3, 4294967296L);
    }

    public static boolean br(int i) {
        return i == 152 || i == 1;
    }

    public static boolean bs(int i) {
        return i == 997 || i == 999;
    }

    public static boolean ch(String str) {
        boolean z = false;
        if (eb.iJ != i.iG()) {
            return false;
        }
        mb.n("SharkHelper", "[detect_conn]needWifiApprove(), from: " + str);
        Object obj = null;
        try {
            obj = u.a(new a() {
                public void d(boolean z, boolean z2) {
                    mb.n("SharkHelper", "[detect_conn]needWifiApprove() callback,  need: " + z + " receivedError: " + z2);
                }
            });
        } catch (WifiApproveException e) {
            mb.o("SharkHelper", "[detect_conn]needWifiApprove(), exception: " + e.toString());
        }
        if (!TextUtils.isEmpty(obj)) {
            z = true;
        }
        mb.n("SharkHelper", "[detect_conn]needWifiApprove(),  need approve: " + z + " approve url: " + obj);
        return z;
    }

    /* JADX WARNING: Removed duplicated region for block: B:21:0x00a7 A:{SYNTHETIC, Splitter: B:21:0x00a7} */
    /* JADX WARNING: Removed duplicated region for block: B:31:0x00d7 A:{SYNTHETIC, Splitter: B:31:0x00d7} */
    /* JADX WARNING: Removed duplicated region for block: B:40:0x00ec A:{SYNTHETIC, Splitter: B:40:0x00ec} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static boolean ci(String str) {
        Throwable th;
        Throwable th2;
        boolean z = false;
        String str2 = "www.qq.com";
        mb.n("SharkHelper", "[detect_conn]detectConnection, host: " + str2 + " from: " + str);
        long currentTimeMillis = System.currentTimeMillis();
        Socket socket = null;
        try {
            SocketAddress inetSocketAddress = new InetSocketAddress(InetAddress.getByName(str2), 80);
            Socket socket2 = new Socket();
            try {
                socket2.setSoLinger(false, 0);
                socket2.connect(inetSocketAddress, 5000);
                z = true;
                if (socket2 != null) {
                    try {
                        if (socket2.isConnected()) {
                            socket2.close();
                        }
                    } catch (Throwable th3) {
                        th3.printStackTrace();
                    }
                }
                socket = socket2;
            } catch (IOException e) {
                th3 = e;
                socket = socket2;
                try {
                    mb.c("SharkHelper", "[detect_conn]detectConnection, exception: " + th3.getMessage(), th3);
                    if (socket != null) {
                    }
                    mb.n("SharkHelper", "[detect_conn]detectConnection end, isConnect: " + z + " time cost: " + (System.currentTimeMillis() - currentTimeMillis));
                    return z;
                } catch (Throwable th4) {
                    th2 = th4;
                    if (socket != null) {
                        try {
                            if (socket.isConnected()) {
                                socket.close();
                            }
                        } catch (Throwable th5) {
                            th5.printStackTrace();
                        }
                    }
                    throw th2;
                }
            } catch (Throwable th6) {
                th2 = th6;
                socket = socket2;
                if (socket != null) {
                }
                throw th2;
            }
        } catch (IOException e2) {
            th3 = e2;
            mb.c("SharkHelper", "[detect_conn]detectConnection, exception: " + th3.getMessage(), th3);
            if (socket != null) {
                try {
                    if (socket.isConnected()) {
                        socket.close();
                    }
                } catch (Throwable th32) {
                    th32.printStackTrace();
                }
            }
            mb.n("SharkHelper", "[detect_conn]detectConnection end, isConnect: " + z + " time cost: " + (System.currentTimeMillis() - currentTimeMillis));
            return z;
        } catch (Throwable th7) {
            th32 = th7;
            mb.c("SharkHelper", "[detect_conn]detectConnection, Throwable: " + th32.getMessage(), th32);
            if (socket != null) {
                try {
                    if (socket.isConnected()) {
                        socket.close();
                    }
                } catch (Throwable th322) {
                    th322.printStackTrace();
                }
            }
            mb.n("SharkHelper", "[detect_conn]detectConnection end, isConnect: " + z + " time cost: " + (System.currentTimeMillis() - currentTimeMillis));
            return z;
        }
        mb.n("SharkHelper", "[detect_conn]detectConnection end, isConnect: " + z + " time cost: " + (System.currentTimeMillis() - currentTimeMillis));
        return z;
    }

    public static void cj(String str) {
        Ey = str;
    }

    public static boolean gc() {
        return Ex;
    }

    public static String gd() {
        return Ey;
    }

    public static boolean ge() {
        return EC;
    }

    public static Looper getLooper() {
        if (sLooper == null) {
            Class cls = nu.class;
            synchronized (nu.class) {
                if (sLooper == null) {
                    HandlerThread newFreeHandlerThread = ((ki) fj.D(4)).newFreeHandlerThread("Shark-Looper");
                    newFreeHandlerThread.start();
                    sLooper = newFreeHandlerThread.getLooper();
                }
            }
        }
        return sLooper;
    }

    public static oa gf() {
        if (ED == null) {
            Class cls = ob.class;
            synchronized (ob.class) {
                if (ED == null) {
                    ED = new ob(bL());
                }
            }
        }
        return ED;
    }

    public static boolean t(byte[] bArr) {
        return bArr != null && bArr.length == 1;
    }
}
