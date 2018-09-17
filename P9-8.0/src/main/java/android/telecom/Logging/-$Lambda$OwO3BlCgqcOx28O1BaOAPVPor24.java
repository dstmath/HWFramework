package android.telecom.Logging;

import android.telecom.Logging.SessionManager.ICurrentThreadId;

final /* synthetic */ class -$Lambda$OwO3BlCgqcOx28O1BaOAPVPor24 implements ICurrentThreadId {

    /* renamed from: android.telecom.Logging.-$Lambda$OwO3BlCgqcOx28O1BaOAPVPor24$1 */
    final /* synthetic */ class AnonymousClass1 implements ISessionCleanupTimeoutMs {
        private final /* synthetic */ Object -$f0;

        private final /* synthetic */ long $m$0() {
            return ((SessionManager) this.-$f0).lambda$-android_telecom_Logging_SessionManager_2450();
        }

        public /* synthetic */ AnonymousClass1(Object obj) {
            this.-$f0 = obj;
        }

        public final long get() {
            return $m$0();
        }
    }

    /* renamed from: android.telecom.Logging.-$Lambda$OwO3BlCgqcOx28O1BaOAPVPor24$2 */
    final /* synthetic */ class AnonymousClass2 implements Runnable {
        private final /* synthetic */ Object -$f0;

        private final /* synthetic */ void $m$0() {
            ((SessionManager) this.-$f0).lambda$-android_telecom_Logging_SessionManager_1888();
        }

        public /* synthetic */ AnonymousClass2(Object obj) {
            this.-$f0 = obj;
        }

        public final void run() {
            $m$0();
        }
    }

    public final int get() {
        return $m$0();
    }
}
