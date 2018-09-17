package tmsdk.bg.module.wifidetect;

import android.net.LocalServerSocket;
import android.net.LocalSocket;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import tmsdk.common.module.aresengine.IncomingSmsFilterConsts;
import tmsdk.common.utils.ScriptHelper;
import tmsdk.common.utils.f;

class a {
    static final a wP = new a();
    LocalServerSocket wN;
    boolean wO = false;

    private a() {
    }

    public static a dw() {
        return wP;
    }

    public void bp(String str) {
        f.h("WifiDetectManager", "[Beg]checkArp-binaryPath:[" + str + "]");
        if (new File(str).exists()) {
            List arrayList = new ArrayList();
            arrayList.add("chmod 0755 " + str + "\n");
            arrayList.add(str + " " + 10);
            f.h("WifiDetectManager", "ScriptHelper.runScript-cmds:[" + arrayList + "]");
            String str2 = "WifiDetectManager";
            f.h(str2, "[End]checkArp-runScript-ret:[" + ScriptHelper.runScript(-1, arrayList) + "]");
            return;
        }
        f.h("WifiDetectManager", "binaryFile not exist");
    }

    public int dx() {
        f.h("WifiDetectManager", "startServerAutoStop");
        int i = 261;
        try {
            this.wN = new LocalServerSocket("tms_socket_server_path");
            this.wO = false;
            while (!this.wO) {
                f.h("WifiDetectManager", "[Beg]Server.accept");
                LocalSocket accept = this.wN.accept();
                f.h("WifiDetectManager", "[End]Server.accept:[" + accept + "]");
                if (accept != null) {
                    if (!this.wO) {
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
                        f.h("WifiDetectManager", "received from binary:[" + stringBuilder2 + "]");
                        if ("found danger".equals(stringBuilder2)) {
                            i = 262;
                        }
                        this.wO = true;
                    }
                }
                if (accept != null) {
                    try {
                        accept.close();
                    } catch (Exception e) {
                        f.h("WifiDetectManager", "close local socket exception: " + e.getMessage());
                    }
                }
            }
            f.h("WifiDetectManager", "server has been stop, close the server");
            this.wN.close();
            this.wN = null;
            return i;
        } catch (IOException e2) {
            f.e("WifiDetectManager", "startServer:[" + e2 + "]");
            return 263;
        }
    }
}
