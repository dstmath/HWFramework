package com.android.server.pm;

import android.content.pm.PackageParser.Package;
import java.util.Comparator;
import java.util.function.Predicate;

final /* synthetic */ class -$Lambda$JzP9CRiQ8kxViovHG-q6Wako1Xw implements Comparator {
    private final /* synthetic */ Object -$f0;

    /* renamed from: com.android.server.pm.-$Lambda$JzP9CRiQ8kxViovHG-q6Wako1Xw$1 */
    final /* synthetic */ class AnonymousClass1 implements Predicate {
        private final /* synthetic */ Object -$f0;

        private final /* synthetic */ boolean $m$0(Object arg0) {
            return ((UninstalledInstantAppState) arg0).mInstantAppInfo.getPackageName().equals((String) this.-$f0);
        }

        public /* synthetic */ AnonymousClass1(Object obj) {
            this.-$f0 = obj;
        }

        public final boolean test(Object obj) {
            return $m$0(obj);
        }
    }

    /* renamed from: com.android.server.pm.-$Lambda$JzP9CRiQ8kxViovHG-q6Wako1Xw$2 */
    final /* synthetic */ class AnonymousClass2 implements Predicate {
        private final /* synthetic */ Object -$f0;

        private final /* synthetic */ boolean $m$0(Object arg0) {
            return ((UninstalledInstantAppState) arg0).mInstantAppInfo.getPackageName().equals(((Package) this.-$f0).packageName);
        }

        public /* synthetic */ AnonymousClass2(Object obj) {
            this.-$f0 = obj;
        }

        public final boolean test(Object obj) {
            return $m$0(obj);
        }
    }

    /* renamed from: com.android.server.pm.-$Lambda$JzP9CRiQ8kxViovHG-q6Wako1Xw$3 */
    final /* synthetic */ class AnonymousClass3 implements Predicate {
        private final /* synthetic */ long -$f0;

        private final /* synthetic */ boolean $m$0(Object arg0) {
            return InstantAppRegistry.lambda$-com_android_server_pm_InstantAppRegistry_29374(this.-$f0, (UninstalledInstantAppState) arg0);
        }

        public /* synthetic */ AnonymousClass3(long j) {
            this.-$f0 = j;
        }

        public final boolean test(Object obj) {
            return $m$0(obj);
        }
    }

    private final /* synthetic */ int $m$0(Object arg0, Object arg1) {
        return ((InstantAppRegistry) this.-$f0).lambda$-com_android_server_pm_InstantAppRegistry_26684((String) arg0, (String) arg1);
    }

    public /* synthetic */ -$Lambda$JzP9CRiQ8kxViovHG-q6Wako1Xw(Object obj) {
        this.-$f0 = obj;
    }

    public final int compare(Object obj, Object obj2) {
        return $m$0(obj, obj2);
    }
}
