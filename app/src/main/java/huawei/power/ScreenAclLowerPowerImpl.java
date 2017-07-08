package huawei.power;

import android.content.Context;
import android.util.Log;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;

public class ScreenAclLowerPowerImpl {
    private static final String ENABLE_VALUE = null;
    private static String TAG;
    private boolean DEBUG;
    private boolean mAclActive;
    private ArrayList<String> mAclApps;
    private String mCurrentApk;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: huawei.power.ScreenAclLowerPowerImpl.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: huawei.power.ScreenAclLowerPowerImpl.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: huawei.power.ScreenAclLowerPowerImpl.<clinit>():void");
    }

    public ScreenAclLowerPowerImpl(Context context) {
        this.mCurrentApk = null;
        this.mAclActive = false;
        this.mAclApps = new ArrayList();
        this.DEBUG = false;
        this.mCurrentApk = "com.huawei.android.launcher";
        this.mAclApps.add("com.android.contacts");
        this.mAclApps.add("com.android.browser");
        this.mAclApps.add("com.huawei.camera");
        this.mAclApps.add("com.android.email");
        this.mAclApps.add("com.android.mediacenter");
        this.mAclApps.add("com.mybook66");
        this.mAclApps.add("com.google.android.gm");
        this.mAclApps.add("com.qzone");
        this.mAclApps.add("com.tencent.mm");
        this.mAclApps.add("com.android.contacts");
        this.mAclApps.add("com.taobao.taobao");
        this.mAclApps.add("com.jingdong.app.mall");
        this.mAclApps.add("com.huawei.android.webcustomize");
        this.mAclApps.add("10001");
        this.mAclApps.add("10008");
        this.mAclApps.add("10013");
        this.mAclApps.add("10003");
        unsetACL();
    }

    public void handlePGScene(int stateType, int eventType, int pid, String pkgName, int uid) {
        if (this.DEBUG) {
            Log.d(TAG, "handle stateType = " + stateType + ", eventType = " + eventType + ", pid = " + pid + ", pkgName = " + pkgName + ", uid = " + uid);
        }
        if (pkgName != null && eventType == 1) {
            if (this.mCurrentApk.equals(pkgName)) {
                this.mAclActive = false;
                if (this.DEBUG) {
                    Log.d(TAG, "screen state changed, reset mAclActive state to false");
                }
            } else {
                this.mCurrentApk = pkgName;
            }
            if (this.mAclApps.contains(pkgName) || this.mAclApps.contains("" + stateType)) {
                if (!this.mAclActive) {
                    if (this.DEBUG) {
                        Log.d(TAG, "will set ACL for eventType = " + eventType + ", apk = " + pkgName);
                    }
                    setACL();
                    this.mAclActive = true;
                } else if (this.DEBUG) {
                    Log.d(TAG, "no need to setACL for already set");
                }
            } else if (this.mAclActive) {
                if (this.DEBUG) {
                    Log.d(TAG, "will unset ACL for eventType = " + eventType + ", pkgName = " + pkgName);
                }
                unsetACL();
                this.mAclActive = false;
            } else if (this.DEBUG) {
                Log.d(TAG, "no need to unset ACL for unset already");
            }
        }
    }

    private boolean setACL() {
        return setACLPowerSavingEnableState(true);
    }

    private boolean unsetACL() {
        return setACLPowerSavingEnableState(false);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean writeDeviceNode(String path, String node, String writeValue) {
        boolean retVal = true;
        try {
            FileOutputStream fos = new FileOutputStream(path + node);
            if (fos == null) {
                Log.w(TAG, "failed to writeDeviceNode for fos is null");
                return false;
            }
            try {
                byte[] byteValue = writeValue.getBytes(Charset.defaultCharset());
                fos.write(byteValue, 0, byteValue.length);
                try {
                    fos.close();
                } catch (IOException e) {
                    Log.w(TAG, "failed to close fos");
                }
            } catch (IOException e2) {
                Log.w(TAG, "failed to writeDeviceNode for IOException");
                retVal = false;
            } catch (Throwable th) {
                try {
                    fos.close();
                } catch (IOException e3) {
                    Log.w(TAG, "failed to close fos");
                }
            }
            return retVal;
        } catch (FileNotFoundException e4) {
            Log.w(TAG, "failed to construct FileOutputStream for ACL node");
            Log.w(TAG, "failed to writeDeviceNode for fos is null");
            return false;
        } catch (Throwable th2) {
            Log.w(TAG, "failed to writeDeviceNode for fos is null");
            return false;
        }
    }

    private boolean setACLPowerSavingEnableState(boolean enable) {
        String str;
        String enableValue = ENABLE_VALUE;
        String disableValue = "1,0";
        String path = "/sys/class/graphics/fb0/";
        String node = "amoled_acl";
        if (enable) {
            str = enableValue;
        } else {
            str = disableValue;
        }
        boolean retValue = writeDeviceNode(path, node, str);
        if (retValue) {
            str = TAG;
            StringBuilder append = new StringBuilder().append("setACL with: ");
            if (!enable) {
                enableValue = disableValue;
            }
            Log.w(str, append.append(enableValue).append(", 1,0 means disable, 1,3 means 30% for: ").append(this.mCurrentApk).toString());
        }
        return retValue;
    }
}
