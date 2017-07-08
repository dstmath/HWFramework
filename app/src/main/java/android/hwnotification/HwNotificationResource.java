package android.hwnotification;

import android.common.HwFrameworkFactory;
import android.os.Bundle;

public class HwNotificationResource {
    public static final int BACKGROUND_INDEX_0 = 0;
    public static final int BACKGROUND_INDEX_1 = 1;
    public static final int BACKGROUND_INDEX_2 = 2;
    public static final int BACKGROUND_INDEX_3 = 3;
    public static final int BACKGROUND_INDEX_4 = 4;
    public static final int BACKGROUND_INDEX_5 = 5;
    public static final int BACKGROUND_INDEX_6 = 6;
    public static final int BACKGROUND_INDEX_7 = 7;
    public static final String HW_NOTIFICATION_BACKGROUND_INDEX = "huawei.notification.backgroundIndex";
    public static final String HW_NOTIFICATION_CONTENT_ICON = "huawei.notification.contentIcon";
    public static final String HW_NOTIFICATION_REPLACE_ICONID = "huawei.notification.replace.iconId";
    public static final String HW_NOTIFICATION_REPLACE_LOCATION = "huawei.notification.replace.location";
    public static final int REPLACE_LOCATION_BIGCONTENT = 4;
    public static final int REPLACE_LOCATION_CONTENT = 2;
    public static final int REPLACE_LOCATION_HEADSUP = 8;
    public static final int REPLACE_LOCATION_LARGEICON = 1;
    private static IHwNotificationResource sInstance;

    public interface IHwNotificationResource {
        Bundle getNotificationThemeData(Bundle bundle, int i, int i2, int i3, int i4);
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.hwnotification.HwNotificationResource.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.hwnotification.HwNotificationResource.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.hwnotification.HwNotificationResource.<clinit>():void");
    }

    private static synchronized IHwNotificationResource getImplObject() {
        synchronized (HwNotificationResource.class) {
            if (sInstance != null) {
                IHwNotificationResource iHwNotificationResource = sInstance;
                return iHwNotificationResource;
            }
            IHwNotificationResource instance = HwFrameworkFactory.getHwNotificationResource();
            if (instance == null) {
                instance = new HwNotificationResourceDummy();
            }
            sInstance = instance;
            iHwNotificationResource = sInstance;
            return iHwNotificationResource;
        }
    }

    public static Bundle getNotificationThemeData(int contIconId, int repIconId, int bgIndex, int repLocation) {
        return getImplObject().getNotificationThemeData(null, contIconId, repIconId, bgIndex, repLocation);
    }

    public static Bundle getNotificationThemeData(Bundle bundle, int contIconId, int repIconId, int bgIndex, int repLocation) {
        return getImplObject().getNotificationThemeData(bundle, contIconId, repIconId, bgIndex, repLocation);
    }
}
