package com.android.server.om;

import java.util.function.Function;
import java.util.function.Predicate;

final /* synthetic */ class -$Lambda$VuwDBWerAG9B6xB4Rr4-FeDL3jk implements Function {

    /* renamed from: com.android.server.om.-$Lambda$VuwDBWerAG9B6xB4Rr4-FeDL3jk$5 */
    final /* synthetic */ class AnonymousClass5 implements Predicate {
        private final /* synthetic */ Object -$f0;

        private final /* synthetic */ boolean $m$0(Object arg0) {
            return ((SettingsItem) arg0).getTargetPackageName().equals((String) this.-$f0);
        }

        public /* synthetic */ AnonymousClass5(Object obj) {
            this.-$f0 = obj;
        }

        public final boolean test(Object obj) {
            return $m$0(obj);
        }
    }

    /* renamed from: com.android.server.om.-$Lambda$VuwDBWerAG9B6xB4Rr4-FeDL3jk$6 */
    final /* synthetic */ class AnonymousClass6 implements Predicate {
        private final /* synthetic */ int -$f0;

        private final /* synthetic */ boolean $m$0(Object arg0) {
            return OverlayManagerSettings.lambda$-com_android_server_om_OverlayManagerSettings_20778(this.-$f0, (SettingsItem) arg0);
        }

        public /* synthetic */ AnonymousClass6(int i) {
            this.-$f0 = i;
        }

        public final boolean test(Object obj) {
            return $m$0(obj);
        }
    }

    public final Object apply(Object obj) {
        return $m$0(obj);
    }
}
