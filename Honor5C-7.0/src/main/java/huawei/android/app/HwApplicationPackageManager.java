package huawei.android.app;

import android.app.ApplicationPackageManager;
import android.app.ApplicationPackageManager.ResourceName;
import android.common.HwPackageManager;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.os.SystemProperties;
import android.util.Log;

public class HwApplicationPackageManager implements HwPackageManager {
    private static HwPackageManager sInstance;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: huawei.android.app.HwApplicationPackageManager.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: huawei.android.app.HwApplicationPackageManager.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: huawei.android.app.HwApplicationPackageManager.<clinit>():void");
    }

    public static HwPackageManager getDefault() {
        if (sInstance == null) {
            sInstance = new HwApplicationPackageManager();
        }
        return sInstance;
    }

    public CharSequence getAppLabelText(PackageManager pm, String packageName, int resid, ApplicationInfo appInfo) {
        if (pm instanceof ApplicationPackageManager) {
            ApplicationPackageManager apm = (ApplicationPackageManager) pm;
            ResourceName name = new ResourceName("label_" + packageName, resid);
            CharSequence text = apm.getCachedString(name);
            if (text != null) {
                return text;
            }
            if (appInfo == null) {
                try {
                    appInfo = apm.getApplicationInfo(packageName, 0);
                } catch (NameNotFoundException e) {
                    return null;
                }
            }
            try {
                text = apm.getResourcesForApplication(appInfo).getText(resid);
                if (text != null) {
                    String[] labels = text.toString().split(",");
                    int brandId = getBrand();
                    if (labels != null && labels.length > 0) {
                        String label = labels[0];
                        if (labels.length > 1 && brandId < labels.length) {
                            label = labels[brandId];
                        }
                        apm.putCachedString(name, labels[brandId]);
                        return labels[brandId];
                    }
                }
            } catch (NameNotFoundException e2) {
                Log.w("PackageManager", "Failure retrieving resources for" + appInfo.packageName);
            } catch (RuntimeException e3) {
                Log.w("PackageManager", "Failure retrieving text 0x" + Integer.toHexString(resid) + " in package " + packageName, e3);
            }
        }
        return null;
    }

    public static final int getBrand() {
        return getDeliverInfo(0);
    }

    public static final boolean isIOTVersion() {
        return 1 == getDeliverInfo(4);
    }

    private static final int getDeliverInfo(int index) {
        String[] infos = SystemProperties.get("ro.config.hw_channel_info", "0,0,460,1,0").split(",");
        if (infos.length >= index + 1 && infos[index] != null) {
            try {
                return Integer.parseInt(infos[index]);
            } catch (NumberFormatException e) {
            }
        }
        return 0;
    }

    public Drawable getBadgedIconForTrustSpace(PackageManager pm) {
        if (pm == null) {
            return null;
        }
        return pm.getDrawable("androidhwext", 33751347, null);
    }
}
