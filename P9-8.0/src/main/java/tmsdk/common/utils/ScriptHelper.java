package tmsdk.common.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.net.LocalSocketAddress.Namespace;
import android.os.Build.VERSION;
import android.os.Process;
import com.huawei.systemmanager.rainbow.comm.request.util.RainbowRequestBasic.CheckVersionField;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import tmsdk.common.TMSDKContext;
import tmsdk.common.module.aresengine.IncomingSmsFilterConsts;
import tmsdkobf.if;
import tmsdkobf.lq;
import tmsdkobf.lu;
import tmsdkobf.qd;

public final class ScriptHelper {
    private static final String LW = (TMSDKContext.getApplicaionContext().getPackageName() + "_" + "athena_v4_2-mfr.dat" + "_" + Process.myUid());
    private static final boolean LX = new File("/dev/socket/script_socket").exists();
    private static int LY = 2;
    private static boolean LZ = LX;
    private static Object Ma = new Object();
    private static a Mb = null;
    private static int Mc = Process.myPid();
    private static BroadcastReceiver Md = new if() {
        public void doOnRecv(Context context, Intent intent) {
        }
    };
    private static qd Me = null;
    public static final int ROOT_GOT = 0;
    public static final int ROOT_NOT_GOT = 2;
    public static final int ROOT_NOT_SUPPORT = 1;
    public static final int ROOT_NO_RESPOND = -1;
    public static final String ROOT_STATE_KEY = "rtstky";
    public static boolean isSuExist;

    public interface a {
        int H(long j);

        boolean cG(String str);

        int jf();

        void jg();
    }

    static final class b {
        byte[] data;
        int size;
        int time;
        int type;

        b() {
        }

        void writeToStream(OutputStream outputStream) throws IOException {
            this.size = this.data == null ? 0 : this.data.length;
            Object obj = new byte[12];
            System.arraycopy(lq.aO(this.type), 0, obj, 0, 4);
            System.arraycopy(lq.aO(this.time), 0, obj, 4, 4);
            System.arraycopy(lq.aO(this.size), 0, obj, 8, 4);
            outputStream.write(obj);
            if (this.data != null && this.data.length > 0) {
                outputStream.write(this.data);
            }
            outputStream.flush();
        }
    }

    static final class c {
        byte[] data;
        int size;

        c() {
        }

        void e(InputStream inputStream) throws IOException {
            byte[] bArr = new byte[4];
            if (inputStream.read(bArr) == 4) {
                this.size = lq.k(bArr);
                if (this.size <= 0) {
                    this.data = new byte[0];
                    return;
                }
                byte[] bArr2 = new byte[this.size];
                int i = 0;
                while (true) {
                    int read = inputStream.read(bArr2, i, this.size - i);
                    if (read <= 0) {
                        break;
                    }
                    i += read;
                }
                if (i == this.size) {
                    this.data = bArr2;
                    return;
                }
                throw new IOException("respond data is invalid");
            }
            throw new IOException("respond data is invalid");
        }
    }

    static {
        boolean z = (lu.bM("/system/bin/su") || lu.bM("/system/xbin/su") || lu.bM("/sbin/su")) ? true : LX;
        isSuExist = z;
    }

    private static synchronized c a(b bVar, boolean z) {
        synchronized (ScriptHelper.class) {
            LocalSocket localSocket = new LocalSocket();
            OutputStream outputStream = null;
            InputStream inputStream = null;
            c cVar = new c();
            try {
                LocalSocketAddress localSocketAddress;
                if (LX) {
                    localSocketAddress = new LocalSocketAddress("/dev/socket/script_socket", Namespace.FILESYSTEM);
                } else {
                    localSocketAddress = new LocalSocketAddress(LW, Namespace.ABSTRACT);
                }
                try {
                    f.d("Root-ScriptHelper", "connect:[" + localSocketAddress + "]");
                    localSocket.connect(localSocketAddress);
                } catch (IOException e) {
                    f.d("Root-ScriptHelper", "connect IOException:[" + e + "]");
                    if (!LX && z) {
                        jd();
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e2) {
                            e2.printStackTrace();
                        }
                        c a = a(bVar, LX);
                        if (null != null) {
                            try {
                                outputStream.close();
                            } catch (IOException e3) {
                                e3.printStackTrace();
                            }
                        }
                        if (null != null) {
                            try {
                                inputStream.close();
                            } catch (IOException e32) {
                                e32.printStackTrace();
                            }
                        }
                        try {
                            localSocket.close();
                        } catch (IOException e322) {
                            e322.printStackTrace();
                        }
                        return a;
                    }
                }
                try {
                    inputStream = localSocket.getInputStream();
                    outputStream = localSocket.getOutputStream();
                    bVar.writeToStream(outputStream);
                    cVar.e(inputStream);
                    c cVar2 = cVar;
                    if (outputStream != null) {
                        try {
                            outputStream.close();
                        } catch (IOException e4) {
                            e4.printStackTrace();
                        }
                    }
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException e42) {
                            e42.printStackTrace();
                        }
                    }
                    try {
                        localSocket.close();
                    } catch (IOException e422) {
                        e422.printStackTrace();
                    }
                    return cVar;
                } catch (IOException e5) {
                    e5.printStackTrace();
                    if (outputStream != null) {
                        try {
                            outputStream.close();
                        } catch (IOException e52) {
                            e52.printStackTrace();
                        }
                    }
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException e522) {
                            e522.printStackTrace();
                        }
                    }
                    try {
                        localSocket.close();
                    } catch (IOException e5222) {
                        e5222.printStackTrace();
                    }
                    return null;
                }
            } catch (Exception e6) {
                e6.printStackTrace();
                if (outputStream != null) {
                    try {
                        outputStream.close();
                    } catch (IOException e52222) {
                        e52222.printStackTrace();
                    }
                }
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e522222) {
                        e522222.printStackTrace();
                    }
                }
                try {
                    localSocket.close();
                } catch (IOException e5222222) {
                    e5222222.printStackTrace();
                }
                return null;
            } catch (Error e7) {
                e7.printStackTrace();
                if (outputStream != null) {
                    try {
                        outputStream.close();
                    } catch (IOException e52222222) {
                        e52222222.printStackTrace();
                    }
                }
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e522222222) {
                        e522222222.printStackTrace();
                    }
                }
                try {
                    localSocket.close();
                } catch (IOException e5222222222) {
                    e5222222222.printStackTrace();
                }
                return null;
            } catch (Throwable th) {
                if (outputStream != null) {
                    try {
                        outputStream.close();
                    } catch (IOException e8) {
                        e8.printStackTrace();
                    }
                }
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e82) {
                        e82.printStackTrace();
                    }
                }
                try {
                    localSocket.close();
                } catch (IOException e822) {
                    e822.printStackTrace();
                }
            }
        }
    }

    public static int acquireRoot() {
        int doAcquireRoot;
        if (LY == 0) {
            boolean je = je();
            f.f("Root-ScriptHelper", "acquireRoot(), sCurrRootState = ROOT_GOT; isReallyGot ? " + je);
            if (je) {
                return LY;
            }
        }
        if (Mb == null) {
            doAcquireRoot = doAcquireRoot();
            f.f("Root-ScriptHelper", "do acquire root locally, root state=" + doAcquireRoot);
        } else {
            doAcquireRoot = Mb.H(4294967299L);
            f.f("Root-ScriptHelper", "do acquire root by proxy-RootService, root state=" + doAcquireRoot);
        }
        return doAcquireRoot;
    }

    public static String acquireRootAndRunScript(int i, List<String> list) {
        return acquireRoot() == 0 ? runScript(i, (List) list) : null;
    }

    public static String acquireRootAndRunScript(int i, String... strArr) {
        return acquireRootAndRunScript(i, new ArrayList(Arrays.asList(strArr)));
    }

    public static void actualStartDaemon() {
        int i = LY;
        LY = 2;
        if (i != LY) {
            jb();
        }
        f.f("Root-ScriptHelper", "[beg]startDaemon @ " + Process.myPid());
        String b = lu.b(TMSDKContext.getApplicaionContext(), "athena_v4_2-mfr.dat", null);
        String str = "chmod 755 " + b + "\n" + String.format(Locale.US, "%s %s %d", new Object[]{b, LW, Integer.valueOf(Process.myUid())}) + "\n";
        if (Mb == null || !Mb.cG(str)) {
            ProcessBuilder processBuilder = new ProcessBuilder(new String[0]);
            processBuilder.command(new String[]{"sh"});
            OutputStream outputStream = null;
            try {
                processBuilder.redirectErrorStream(true);
                outputStream = processBuilder.start().getOutputStream();
                outputStream.write(str.getBytes());
                outputStream.flush();
                if (outputStream != null) {
                    try {
                        outputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } catch (IOException e2) {
                e2.printStackTrace();
                if (outputStream != null) {
                    try {
                        outputStream.close();
                    } catch (IOException e22) {
                        e22.printStackTrace();
                    }
                }
            } catch (Error e3) {
                if (outputStream != null) {
                    try {
                        outputStream.close();
                    } catch (IOException e222) {
                        e222.printStackTrace();
                    }
                }
            } catch (Throwable th) {
                if (outputStream != null) {
                    try {
                        outputStream.close();
                    } catch (IOException e4) {
                        e4.printStackTrace();
                    }
                }
            }
            f.f("Root-ScriptHelper", "[end]startDaemon @ " + Process.myPid());
        }
    }

    public static boolean checkIfSuExist() {
        boolean z = LX;
        if (lu.bM("/system/bin/su") || lu.bM("/system/xbin/su") || lu.bM("/sbin/su")) {
            z = true;
        }
        isSuExist = z;
        f.d("Root-ScriptHelper", "checkIfSuExist:[" + isSuExist + "]");
        return isSuExist;
    }

    public static int doAcquireRoot() {
        int i = LY;
        checkIfSuExist();
        if (LX) {
            LY = 0;
        } else if (isSuExist) {
            synchronized (Ma) {
                int i2 = 2;
                int i3 = 0;
                while (i3 < 1) {
                    i2 = jc();
                    if (i2 == -1) {
                        i3++;
                    }
                }
                LY = i2;
            }
        } else {
            LY = 1;
        }
        if (!LZ) {
            int i4 = LY;
        }
        if (i != LY) {
            jb();
        }
        return LY;
    }

    public static String[] exec(File file, String... strArr) {
        try {
            StringBuffer stringBuffer = new StringBuffer();
            ProcessBuilder processBuilder = new ProcessBuilder(strArr);
            if (file != null) {
                processBuilder.directory(file);
            }
            processBuilder.redirectErrorStream(LX);
            Process start = processBuilder.start();
            InputStream inputStream = start.getInputStream();
            byte[] bArr = new byte[IncomingSmsFilterConsts.PAY_SMS];
            while (true) {
                int read = inputStream.read(bArr);
                if (read <= 0) {
                    inputStream.close();
                    start.destroy();
                    return stringBuffer.toString().split("\n");
                }
                stringBuffer.append(new String(bArr, 0, read));
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } catch (Error e2) {
            return null;
        }
    }

    public static String[] exec(String... strArr) {
        return exec(null, strArr);
    }

    public static int getRootState() {
        return Mb == null ? LY : Mb.jf();
    }

    public static int getRootStateActual() {
        return LY;
    }

    public static void initForeMultiProcessUse() {
        ja();
    }

    public static boolean isRootGot() {
        return getRootState() != 0 ? LX : true;
    }

    /* JADX WARNING: Missing block: B:9:0x003d, code:
            return LX;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static boolean isRootUid() {
        f.d("Root-ScriptHelper", "isRootUid");
        synchronized (Ma) {
            String runScript = runScript(-1, "id");
            f.d("Root-ScriptHelper", "isRootUid res=" + runScript);
            if (runScript != null && runScript.contains("uid=0")) {
                return true;
            } else if (LY == 0) {
                LY = 2;
                jb();
            }
        }
    }

    public static boolean isSystemUid() {
        return Process.myUid() != CheckVersionField.CHECK_VERSION_MAX_UPDATE_DAY ? LX : true;
    }

    private static void ja() {
    }

    private static void jb() {
    }

    private static int jc() {
        String runScript = runScript(-1, "id");
        f.d("Root-ScriptHelper", "run (id):[" + runScript + "]");
        if (runScript != null) {
            if (runScript.contains("uid=0")) {
                return 0;
            }
            runScript = runScript(-1, "su");
            f.d("Root-ScriptHelper", "run (su):[" + runScript + "]");
            if (runScript != null) {
                if (runScript.contains("Kill") || runScript.contains("kill")) {
                    return -1;
                }
                runScript = runScript(-1, "id");
                f.d("Root-ScriptHelper", "run (su--id):[" + runScript + "]");
                if (runScript != null) {
                    if (!runScript.contains("uid=0")) {
                        return 2;
                    }
                    List arrayList = new ArrayList();
                    v(arrayList);
                    runScript(-1, arrayList);
                    return 0;
                }
            }
        }
        return 2;
    }

    private static void jd() {
        if (Mb != null) {
            Mb.jg();
        } else {
            actualStartDaemon();
        }
    }

    private static boolean je() {
        b bVar = new b();
        bVar.time = CheckVersionField.CHECK_VERSION_MAX_UPDATE_DAY;
        bVar.data = "id\n".getBytes();
        c a = a(bVar, LX);
        return (a != null && new String(a.data).contains("uid=0")) ? true : LX;
    }

    public static qd provider() {
        return Me;
    }

    public static boolean providerSupportCancelMissCall() {
        return (Me != null && Me.bU(2)) ? true : LX;
    }

    public static boolean providerSupportCpuRelative() {
        return (Me != null && Me.bU(3)) ? true : LX;
    }

    public static boolean providerSupportGetAllApkFiles() {
        return (Me != null && Me.bU(1)) ? true : LX;
    }

    public static boolean providerSupportPmRelative() {
        return (Me != null && Me.bU(4)) ? true : LX;
    }

    public static String runScript(int i, List<String> list) {
        if (i < 0) {
            i = 30000;
        }
        v(list);
        StringBuilder stringBuilder = new StringBuilder();
        for (String str : list) {
            String str2;
            if (VERSION.SDK_INT >= 21 && str2 != null) {
                if (str2.indexOf("pm ") == 0 || str2.indexOf("am ") == 0 || str2.indexOf("service ") == 0) {
                    str2 = "su -cn u:r:shell:s0 -c " + str2 + " < /dev/null";
                } else if (str2.indexOf("dumpsys ") == 0) {
                    str2 = "su -cn u:r:system:s0 -c " + str2 + " < /dev/null";
                }
            }
            stringBuilder.append(str2).append("\n");
        }
        b bVar = new b();
        bVar.type = 0;
        bVar.time = i;
        bVar.data = stringBuilder.toString().getBytes();
        c a = a(bVar, true);
        if (a != null) {
            try {
                if (a.data != null) {
                    return new String(a.data).trim();
                }
            } catch (Exception e) {
                e.printStackTrace();
            } catch (Error e2) {
            }
        }
        return null;
    }

    public static String runScript(int i, String... strArr) {
        return runScript(i, new ArrayList(Arrays.asList(strArr)));
    }

    public static void setProvider(qd qdVar) {
        Object obj = null;
        for (StackTraceElement stackTraceElement : Thread.currentThread().getStackTrace()) {
            if (stackTraceElement.getClass().equals(TMSDKContext.class) && stackTraceElement.getMethodName().indexOf("init") >= 0) {
                obj = 1;
                break;
            }
        }
        if (obj != null) {
            Me = qdVar;
        } else {
            f.e("ScriptHelper", "Unauthorized caller");
        }
    }

    public static void setRootService(a aVar) {
        Mb = aVar;
    }

    public static boolean stopDaemon() {
        b bVar = new b();
        bVar.type = 1;
        bVar.data = "echo old".getBytes();
        c a = a(bVar, LX);
        return (a == null || new String(a.data).trim().contains("old")) ? LX : true;
    }

    private static void v(List<String> list) {
        for (Entry entry : new ProcessBuilder(new String[0]).environment().entrySet()) {
            list.add("export " + ((String) entry.getKey()) + "=" + ((String) entry.getValue()));
        }
    }
}
