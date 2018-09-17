package tmsdk.bg.module.wifidetect;

import android.net.LocalServerSocket;
import android.net.LocalSocket;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import tmsdk.common.module.aresengine.IncomingSmsFilterConsts;
import tmsdk.common.module.intelli_sms.SmsCheckResult;
import tmsdk.common.utils.ScriptHelper;
import tmsdk.common.utils.d;

/* compiled from: Unknown */
class a {
    static final a zE = null;
    LocalServerSocket zC;
    boolean zD;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdk.bg.module.wifidetect.a.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdk.bg.module.wifidetect.a.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: tmsdk.bg.module.wifidetect.a.<clinit>():void");
    }

    private a() {
        this.zD = false;
    }

    public static a el() {
        return zE;
    }

    public void cn(String str) {
        d.g("WifiDetectManager", "[Beg]checkArp-binaryPath:[" + str + "]");
        if (new File(str).exists()) {
            List arrayList = new ArrayList();
            arrayList.add("chmod 0755 " + str + "\n");
            arrayList.add(str + " " + 10);
            d.g("WifiDetectManager", "ScriptHelper.runScript-cmds:[" + arrayList + "]");
            d.g("WifiDetectManager", "[End]checkArp-runScript-ret:[" + ScriptHelper.runScript(-1, arrayList) + "]");
            return;
        }
        d.g("WifiDetectManager", "binaryFile not exist");
    }

    public int em() {
        d.g("WifiDetectManager", "startServerAutoStop");
        int i = SmsCheckResult.ESCT_261;
        this.zC = new LocalServerSocket("tms_socket_server_path");
        this.zD = false;
        while (!this.zD) {
            d.g("WifiDetectManager", "[Beg]Server.accept");
            LocalSocket accept = this.zC.accept();
            d.g("WifiDetectManager", "[End]Server.accept:[" + accept + "]");
            if (accept != null) {
                if (!this.zD) {
                    InputStream inputStream = accept.getInputStream();
                    StringBuilder stringBuilder = new StringBuilder();
                    byte[] bArr = new byte[IncomingSmsFilterConsts.PAY_SMS];
                    while (true) {
                        int read = inputStream.read(bArr);
                        if (read == -1) {
                            break;
                        }
                        stringBuilder.append(new String(bArr, 0, read));
                    }
                    String stringBuilder2 = stringBuilder.toString();
                    d.g("WifiDetectManager", "received from binary:[" + stringBuilder2 + "]");
                    if ("found danger".equals(stringBuilder2)) {
                        i = SmsCheckResult.ESCT_262;
                    }
                    this.zD = true;
                }
            }
            if (accept != null) {
                try {
                    accept.close();
                } catch (Exception e) {
                    try {
                        d.g("WifiDetectManager", "close local socket exception: " + e.getMessage());
                    } catch (IOException e2) {
                        d.c("WifiDetectManager", "startServer:[" + e2 + "]");
                        return SmsCheckResult.ESCT_263;
                    }
                }
            }
        }
        d.g("WifiDetectManager", "server has been stop, close the server");
        this.zC.close();
        this.zC = null;
        return i;
    }
}
