package com.android.server.notification;

import android.app.Notification;
import android.content.Context;
import android.util.Slog;

public class NotificationIntrusivenessExtractor implements NotificationSignalExtractor {
    private static final boolean DBG = false;
    private static final long HANG_TIME_MS = 10000;
    private static final String TAG = "IntrusivenessExtractor";

    /* renamed from: com.android.server.notification.NotificationIntrusivenessExtractor.1 */
    class AnonymousClass1 extends RankingReconsideration {
        AnonymousClass1(String $anonymous0, long $anonymous1) {
            super($anonymous0, $anonymous1);
        }

        public void work() {
        }

        public void applyChangesLocked(NotificationRecord record) {
            record.setRecentlyIntrusive(NotificationIntrusivenessExtractor.DBG);
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.notification.NotificationIntrusivenessExtractor.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.notification.NotificationIntrusivenessExtractor.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.notification.NotificationIntrusivenessExtractor.<clinit>():void");
    }

    public void initialize(Context ctx, NotificationUsageStats usageStats) {
        if (DBG) {
            Slog.d(TAG, "Initializing  " + getClass().getSimpleName() + ".");
        }
    }

    public RankingReconsideration process(NotificationRecord record) {
        if (record == null || record.getNotification() == null) {
            if (DBG) {
                Slog.d(TAG, "skipping empty notification");
            }
            return null;
        }
        if (record.getImportance() >= 3) {
            Notification notification = record.getNotification();
            if ((notification.defaults & 2) == 0 && notification.vibrate == null && (notification.defaults & 1) == 0 && notification.sound == null) {
                if (notification.fullScreenIntent != null) {
                }
            }
            record.setRecentlyIntrusive(true);
        }
        return new AnonymousClass1(record.getKey(), HANG_TIME_MS);
    }

    public void setConfig(RankingConfig config) {
    }
}
