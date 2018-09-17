package tmsdk.common.utils;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
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
import tmsdkobf.jq;
import tmsdkobf.mo;
import tmsdkobf.ms;
import tmsdkobf.ra;

/* compiled from: Unknown */
public final class ScriptHelper {
    private static final String Lg = null;
    private static final String Lh = null;
    private static final boolean Li = false;
    private static int Lj = 0;
    private static boolean Lk = false;
    private static Object Ll = null;
    private static a Lm = null;
    private static int Ln = 0;
    private static BroadcastReceiver Lo = null;
    private static ra Lp = null;
    public static final int ROOT_GOT = 0;
    public static final int ROOT_NOT_GOT = 2;
    public static final int ROOT_NOT_SUPPORT = 1;
    public static final int ROOT_NO_RESPOND = -1;
    public static final String ROOT_STATE_KEY = "rtstky";
    public static boolean isSuExist;

    /* compiled from: Unknown */
    public interface a {
        int A(long j);

        boolean di(String str);

        int iV();

        void iW();
    }

    /* compiled from: Unknown */
    static final class b {
        byte[] data;
        int size;
        int time;
        int type;

        b() {
        }

        void writeToStream(OutputStream outputStream) throws IOException {
            this.size = this.data == null ? ScriptHelper.ROOT_GOT : this.data.length;
            Object obj = new byte[12];
            System.arraycopy(mo.bE(this.type), ScriptHelper.ROOT_GOT, obj, ScriptHelper.ROOT_GOT, 4);
            System.arraycopy(mo.bE(this.time), ScriptHelper.ROOT_GOT, obj, 4, 4);
            System.arraycopy(mo.bE(this.size), ScriptHelper.ROOT_GOT, obj, 8, 4);
            outputStream.write(obj);
            if (this.data != null && this.data.length > 0) {
                outputStream.write(this.data);
            }
            outputStream.flush();
        }
    }

    /* compiled from: Unknown */
    static final class c {
        byte[] data;
        int size;

        c() {
        }

        void e(InputStream inputStream) throws IOException {
            int i = ScriptHelper.ROOT_GOT;
            byte[] bArr = new byte[4];
            if (inputStream.read(bArr) == 4) {
                this.size = mo.k(bArr);
                if (this.size <= 0) {
                    this.data = new byte[ScriptHelper.ROOT_GOT];
                    return;
                }
                bArr = new byte[this.size];
                while (true) {
                    int read = inputStream.read(bArr, i, this.size - i);
                    if (read <= 0) {
                        break;
                    }
                    i += read;
                }
                if (i == this.size) {
                    this.data = bArr;
                    return;
                }
                throw new IOException("respond data is invalid");
            }
            throw new IOException("respond data is invalid");
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdk.common.utils.ScriptHelper.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdk.common.utils.ScriptHelper.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: tmsdk.common.utils.ScriptHelper.<clinit>():void");
    }

    private static synchronized c a(b bVar, boolean z) {
        IOException e;
        InputStream inputStream;
        OutputStream outputStream;
        Exception e2;
        Throwable th;
        Error e3;
        InputStream inputStream2 = null;
        synchronized (ScriptHelper.class) {
            LocalSocket localSocket = new LocalSocket();
            c cVar = new c();
            try {
                LocalSocketAddress localSocketAddress = !Li ? new LocalSocketAddress(Lh, Namespace.ABSTRACT) : new LocalSocketAddress("/dev/socket/script_socket", Namespace.FILESYSTEM);
                try {
                    d.e("Root-ScriptHelper", "connect:[" + localSocketAddress + "]");
                    localSocket.connect(localSocketAddress);
                } catch (IOException e4) {
                    d.e("Root-ScriptHelper", "connect IOException:[" + e4 + "]");
                    e4.printStackTrace();
                    if (!Li && z) {
                        iR();
                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException e5) {
                            e5.printStackTrace();
                        }
                        c a = a(bVar, Li);
                        try {
                            localSocket.close();
                        } catch (IOException e6) {
                            e6.printStackTrace();
                        }
                        return a;
                    }
                }
                try {
                    inputStream = localSocket.getInputStream();
                } catch (IOException e7) {
                    e4 = e7;
                    inputStream = null;
                    outputStream = null;
                    try {
                        e4.printStackTrace();
                        if (outputStream != null) {
                            try {
                                outputStream.close();
                            } catch (IOException e42) {
                                e42.printStackTrace();
                            }
                        }
                        if (inputStream != null) {
                            try {
                                inputStream.close();
                            } catch (IOException e422) {
                                e422.printStackTrace();
                            }
                        }
                        try {
                            localSocket.close();
                        } catch (IOException e4222) {
                            e4222.printStackTrace();
                        }
                    } catch (Exception e8) {
                        e2 = e8;
                        try {
                            e2.printStackTrace();
                            if (outputStream != null) {
                                try {
                                    outputStream.close();
                                } catch (IOException e42222) {
                                    e42222.printStackTrace();
                                }
                            }
                            if (inputStream != null) {
                                try {
                                    inputStream.close();
                                } catch (IOException e422222) {
                                    e422222.printStackTrace();
                                }
                            }
                            try {
                                localSocket.close();
                            } catch (IOException e4222222) {
                                e4222222.printStackTrace();
                            }
                            return null;
                        } catch (Throwable th2) {
                            th = th2;
                            inputStream2 = inputStream;
                            if (outputStream != null) {
                                try {
                                    outputStream.close();
                                } catch (IOException e62) {
                                    e62.printStackTrace();
                                }
                            }
                            if (inputStream2 != null) {
                                try {
                                    inputStream2.close();
                                } catch (IOException e622) {
                                    e622.printStackTrace();
                                }
                            }
                            try {
                                localSocket.close();
                            } catch (IOException e6222) {
                                e6222.printStackTrace();
                            }
                            throw th;
                        }
                    } catch (Error e9) {
                        e3 = e9;
                        e3.printStackTrace();
                        if (outputStream != null) {
                            try {
                                outputStream.close();
                            } catch (IOException e42222222) {
                                e42222222.printStackTrace();
                            }
                        }
                        if (inputStream != null) {
                            try {
                                inputStream.close();
                            } catch (IOException e422222222) {
                                e422222222.printStackTrace();
                            }
                        }
                        try {
                            localSocket.close();
                        } catch (IOException e4222222222) {
                            e4222222222.printStackTrace();
                        }
                        return null;
                    }
                    return null;
                }
                try {
                    outputStream = localSocket.getOutputStream();
                    try {
                        bVar.writeToStream(outputStream);
                        cVar.e(inputStream);
                        if (outputStream != null) {
                            try {
                                outputStream.close();
                            } catch (IOException e42222222222) {
                                e42222222222.printStackTrace();
                            }
                        }
                        if (inputStream != null) {
                            try {
                                inputStream.close();
                            } catch (IOException e422222222222) {
                                e422222222222.printStackTrace();
                            }
                        }
                        try {
                            localSocket.close();
                        } catch (IOException e4222222222222) {
                            e4222222222222.printStackTrace();
                        }
                        return cVar;
                    } catch (IOException e10) {
                        e4222222222222 = e10;
                        e4222222222222.printStackTrace();
                        if (outputStream != null) {
                            outputStream.close();
                        }
                        if (inputStream != null) {
                            inputStream.close();
                        }
                        localSocket.close();
                        return null;
                    }
                } catch (IOException e11) {
                    e4222222222222 = e11;
                    outputStream = null;
                    e4222222222222.printStackTrace();
                    if (outputStream != null) {
                        outputStream.close();
                    }
                    if (inputStream != null) {
                        inputStream.close();
                    }
                    localSocket.close();
                    return null;
                } catch (Exception e12) {
                    e2 = e12;
                    outputStream = null;
                    e2.printStackTrace();
                    if (outputStream != null) {
                        outputStream.close();
                    }
                    if (inputStream != null) {
                        inputStream.close();
                    }
                    localSocket.close();
                    return null;
                } catch (Error e13) {
                    e3 = e13;
                    outputStream = null;
                    e3.printStackTrace();
                    if (outputStream != null) {
                        outputStream.close();
                    }
                    if (inputStream != null) {
                        inputStream.close();
                    }
                    localSocket.close();
                    return null;
                } catch (Throwable th3) {
                    th = th3;
                    outputStream = null;
                    inputStream2 = inputStream;
                    if (outputStream != null) {
                        outputStream.close();
                    }
                    if (inputStream2 != null) {
                        inputStream2.close();
                    }
                    localSocket.close();
                    throw th;
                }
            } catch (Exception e14) {
                e2 = e14;
                inputStream = null;
                outputStream = null;
                e2.printStackTrace();
                if (outputStream != null) {
                    outputStream.close();
                }
                if (inputStream != null) {
                    inputStream.close();
                }
                localSocket.close();
                return null;
            } catch (Error e15) {
                e3 = e15;
                inputStream = null;
                outputStream = null;
                e3.printStackTrace();
                if (outputStream != null) {
                    outputStream.close();
                }
                if (inputStream != null) {
                    inputStream.close();
                }
                localSocket.close();
                return null;
            } catch (Throwable th4) {
                th = th4;
                outputStream = null;
                if (outputStream != null) {
                    outputStream.close();
                }
                if (inputStream2 != null) {
                    inputStream2.close();
                }
                localSocket.close();
                throw th;
            }
        }
    }

    public static int acquireRoot() {
        int doAcquireRoot;
        if (Lj == 0) {
            boolean iS = iS();
            d.d("Root-ScriptHelper", "acquireRoot(), sCurrRootState = ROOT_GOT; isReallyGot ? " + iS);
            if (iS) {
                return Lj;
            }
        }
        if (Lm == null) {
            doAcquireRoot = doAcquireRoot();
            d.d("Root-ScriptHelper", "do acquire root locally, root state=" + doAcquireRoot);
        } else {
            doAcquireRoot = Lm.A(4294967299L);
            d.d("Root-ScriptHelper", "do acquire root by proxy-RootService, root state=" + doAcquireRoot);
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
        OutputStream outputStream = null;
        int i = Lj;
        Lj = ROOT_NOT_GOT;
        if (i != Lj) {
            iP();
        }
        d.d("RootService", "startDaemon @ " + Process.myPid());
        String str = "chmod 755 " + str + "\n" + String.format(Locale.US, "%s %s %d", new Object[]{ms.a(TMSDKContext.getApplicaionContext(), Lg, null), Lh, Integer.valueOf(Process.myUid())}) + "\n";
        if (Lm == null || !Lm.di(str)) {
            ProcessBuilder processBuilder = new ProcessBuilder(new String[ROOT_GOT]);
            String[] strArr = new String[ROOT_NOT_SUPPORT];
            strArr[ROOT_GOT] = "sh";
            processBuilder.command(strArr);
            try {
                processBuilder.redirectErrorStream(true);
                outputStream = processBuilder.start().getOutputStream();
                outputStream.write(str.getBytes());
                outputStream.flush();
                try {
                    TMSDKContext.getApplicaionContext().sendBroadcast(new Intent(TMSDKContext.getStrFromEnvMap(TMSDKContext.CON_ROOT_DAEMON_START_ACTION)), "com.tencent.qqsecure.INNER_BROCAST");
                } catch (Throwable e) {
                    d.a("Root-ScriptHelper", "broadcast ROOT_DAEMON_START, err: " + e.getMessage(), e);
                }
                if (outputStream != null) {
                    try {
                        outputStream.close();
                    } catch (IOException e2) {
                        e2.printStackTrace();
                    }
                }
            } catch (IOException e22) {
                e22.printStackTrace();
                if (outputStream != null) {
                    try {
                        outputStream.close();
                    } catch (IOException e222) {
                        e222.printStackTrace();
                    }
                }
            } catch (Error e3) {
                if (outputStream != null) {
                    try {
                        outputStream.close();
                    } catch (IOException e2222) {
                        e2222.printStackTrace();
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
        }
    }

    public static boolean checkIfSuExist() {
        boolean z = Li;
        if (ms.cy("/system/bin/su") || ms.cy("/system/xbin/su") || ms.cy("/sbin/su")) {
            z = true;
        }
        isSuExist = z;
        d.e("Root-ScriptHelper", "checkIfSuExist:[" + isSuExist + "]");
        return isSuExist;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static int doAcquireRoot() {
        int i = Lj;
        checkIfSuExist();
        if (Li) {
            Lj = ROOT_GOT;
        } else if (isSuExist) {
            synchronized (Ll) {
                int i2 = ROOT_NOT_GOT;
                int i3 = ROOT_GOT;
                while (i3 < ROOT_NOT_SUPPORT) {
                    i2 = iQ();
                    if (i2 == ROOT_NO_RESPOND) {
                        i3 += ROOT_NOT_SUPPORT;
                    }
                }
                Lj = i2;
            }
        } else {
            Lj = ROOT_NOT_SUPPORT;
        }
        if (!Lk && Lj == 0) {
            try {
                TMSDKContext.getApplicaionContext().sendBroadcast(new Intent(TMSDKContext.getStrFromEnvMap(TMSDKContext.CON_ROOT_GOT_ACTION)), "com.tencent.qqsecure.INNER_BROCAST");
                Lk = true;
                d.d("Root-ScriptHelper", "broadcast ROOT_GOT");
            } catch (Throwable e) {
                d.a("Root-ScriptHelper", "broadcast ROOT_GOT err: " + e.getMessage(), e);
            }
        }
        if (i != Lj) {
            iP();
        }
        return Lj;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static String[] exec(File file, String... strArr) {
        try {
            StringBuffer stringBuffer = new StringBuffer();
            ProcessBuilder processBuilder = new ProcessBuilder(strArr);
            if (file != null) {
                processBuilder.directory(file);
            }
            processBuilder.redirectErrorStream(Li);
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
                stringBuffer.append(new String(bArr, ROOT_GOT, read));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Error e2) {
        }
    }

    public static String[] exec(String... strArr) {
        return exec(null, strArr);
    }

    public static int getRootState() {
        return Lm == null ? Lj : Lm.iV();
    }

    public static int getRootStateActual() {
        return Lj;
    }

    private static void iO() {
        int cw = jq.cw();
        String strFromEnvMap = TMSDKContext.getStrFromEnvMap(TMSDKContext.CON_ROOT_CHANGE_ACTION);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(strFromEnvMap);
        if (ROOT_NOT_GOT == cw) {
            intentFilter.addAction("tms.scripthelper.create");
            d.d("Root-ScriptHelper", "register SCRIPT_HELPER_CREATE receiver");
        }
        TMSDKContext.getApplicaionContext().registerReceiver(Lo, intentFilter);
        if (ROOT_NOT_SUPPORT == cw) {
            try {
                Intent intent = new Intent("tms.scripthelper.create");
                intent.putExtra("pidky", Ln);
                TMSDKContext.getApplicaionContext().sendBroadcast(intent, "com.tencent.qqsecure.INNER_BROCAST");
                d.d("Root-ScriptHelper", "broadcast SCRIPT_HELPER_CREATE");
            } catch (Throwable e) {
                d.a("Root-ScriptHelper", "broadcast SCRIPT_HELPER_CREATE err: " + e.getMessage(), e);
            }
        }
    }

    private static void iP() {
        try {
            Intent intent = new Intent(TMSDKContext.getStrFromEnvMap(TMSDKContext.CON_ROOT_CHANGE_ACTION));
            intent.putExtra(ROOT_STATE_KEY, Lj);
            intent.putExtra("pidky", Ln);
            TMSDKContext.getApplicaionContext().sendBroadcast(intent, "com.tencent.qqsecure.INNER_BROCAST");
            d.d("Root-ScriptHelper", "broadcast root state, state: " + Lj);
        } catch (Throwable e) {
            d.a("Root-ScriptHelper", "broadcast root state, err: " + e.getMessage(), e);
        }
    }

    private static int iQ() {
        String[] strArr = new String[ROOT_NOT_SUPPORT];
        strArr[ROOT_GOT] = "id";
        String runScript = runScript((int) ROOT_NO_RESPOND, strArr);
        d.e("Root-ScriptHelper", "run (id):[" + runScript + "]");
        if (runScript == null) {
            return ROOT_NOT_GOT;
        }
        if (!runScript.contains("uid=0")) {
            strArr = new String[ROOT_NOT_SUPPORT];
            strArr[ROOT_GOT] = "su";
            runScript = runScript((int) ROOT_NO_RESPOND, strArr);
            d.e("Root-ScriptHelper", "run (su):[" + runScript + "]");
            if (runScript == null) {
                return ROOT_NOT_GOT;
            }
            if (runScript.contains("Kill") || runScript.contains("kill")) {
                return ROOT_NO_RESPOND;
            }
            strArr = new String[ROOT_NOT_SUPPORT];
            strArr[ROOT_GOT] = "id";
            runScript = runScript((int) ROOT_NO_RESPOND, strArr);
            d.e("Root-ScriptHelper", "run (su--id):[" + runScript + "]");
            if (runScript == null) {
                return ROOT_NOT_GOT;
            }
            if (!runScript.contains("uid=0")) {
                return ROOT_NOT_GOT;
            }
            List arrayList = new ArrayList();
            z(arrayList);
            runScript((int) ROOT_NO_RESPOND, arrayList);
        }
        return ROOT_GOT;
    }

    private static void iR() {
        if (Lm != null) {
            Lm.iW();
        } else {
            actualStartDaemon();
        }
    }

    private static boolean iS() {
        b bVar = new b();
        bVar.time = CheckVersionField.CHECK_VERSION_MAX_UPDATE_DAY;
        bVar.data = "id\n".getBytes();
        c a = a(bVar, Li);
        return (a != null && new String(a.data).contains("uid=0")) ? true : Li;
    }

    public static void initForeMultiProcessUse() {
        iO();
    }

    public static boolean isRootGot() {
        return getRootState() != 0 ? Li : true;
    }

    public static boolean isRootUid() {
        d.e("Root-ScriptHelper", "isRootUid");
        synchronized (Ll) {
            String[] strArr = new String[ROOT_NOT_SUPPORT];
            strArr[ROOT_GOT] = "id";
            String runScript = runScript((int) ROOT_NO_RESPOND, strArr);
            d.e("Root-ScriptHelper", "isRootUid res=" + runScript);
            if (runScript != null && runScript.contains("uid=0")) {
                return true;
            }
            if (Lj == 0) {
                Lj = ROOT_NOT_GOT;
                iP();
            }
            return Li;
        }
    }

    public static boolean isSystemUid() {
        return Process.myUid() != CheckVersionField.CHECK_VERSION_MAX_UPDATE_DAY ? Li : true;
    }

    public static ra provider() {
        return Lp;
    }

    public static boolean providerSupportCancelMissCall() {
        return (Lp != null && Lp.cB(ROOT_NOT_GOT)) ? true : Li;
    }

    public static boolean providerSupportCpuRelative() {
        return (Lp != null && Lp.cB(3)) ? true : Li;
    }

    public static boolean providerSupportGetAllApkFiles() {
        if (Lp != null) {
            if (Lp.cB(ROOT_NOT_SUPPORT)) {
                return true;
            }
        }
        return Li;
    }

    public static boolean providerSupportPmRelative() {
        return (Lp != null && Lp.cB(4)) ? true : Li;
    }

    public static String runScript(int i, List<String> list) {
        if (i < 0) {
            i = 30000;
        }
        z(list);
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
        bVar.type = ROOT_GOT;
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

    public static void setProvider(ra raVar) {
        Object obj = null;
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        int length = stackTrace.length;
        for (int i = ROOT_GOT; i < length; i += ROOT_NOT_SUPPORT) {
            StackTraceElement stackTraceElement = stackTrace[i];
            if (stackTraceElement.getClass().equals(TMSDKContext.class) && stackTraceElement.getMethodName().indexOf("init") >= 0) {
                obj = ROOT_NOT_SUPPORT;
                break;
            }
        }
        if (obj != null) {
            Lp = raVar;
        } else {
            d.c("ScriptHelper", "Unauthorized caller");
        }
    }

    public static void setRootService(a aVar) {
        Lm = aVar;
    }

    public static boolean stopDaemon() {
        b bVar = new b();
        bVar.type = ROOT_NOT_SUPPORT;
        bVar.data = "echo old".getBytes();
        c a = a(bVar, Li);
        return (a == null || new String(a.data).trim().contains("old")) ? Li : true;
    }

    private static void z(List<String> list) {
        for (Entry entry : new ProcessBuilder(new String[ROOT_GOT]).environment().entrySet()) {
            list.add("export " + ((String) entry.getKey()) + "=" + ((String) entry.getValue()));
        }
    }
}
