package com.android.server.pm;

import android.content.pm.PackageParser.Package;
import android.util.ArraySet;
import java.util.Comparator;
import java.util.function.Predicate;

final /* synthetic */ class -$Lambda$LlDgbnHlShdoOCTPTWIe496B9MM implements Comparator {

    /* renamed from: com.android.server.pm.-$Lambda$LlDgbnHlShdoOCTPTWIe496B9MM$5 */
    final /* synthetic */ class AnonymousClass5 implements Predicate {
        private final /* synthetic */ Object -$f0;

        private final /* synthetic */ boolean $m$0(Object arg0) {
            return ((ArraySet) this.-$f0).contains(((Package) arg0).packageName);
        }

        public /* synthetic */ AnonymousClass5(Object obj) {
            this.-$f0 = obj;
        }

        public final boolean test(Object obj) {
            return $m$0(obj);
        }
    }

    /* renamed from: com.android.server.pm.-$Lambda$LlDgbnHlShdoOCTPTWIe496B9MM$6 */
    final /* synthetic */ class AnonymousClass6 implements Predicate {
        private final /* synthetic */ Object -$f0;

        private final /* synthetic */ boolean $m$0(Object arg0) {
            return ((PackageManagerService) this.-$f0).getDexManager().isUsedByOtherApps(((Package) arg0).packageName);
        }

        public /* synthetic */ AnonymousClass6(Object obj) {
            this.-$f0 = obj;
        }

        public final boolean test(Object obj) {
            return $m$0(obj);
        }
    }

    /* renamed from: com.android.server.pm.-$Lambda$LlDgbnHlShdoOCTPTWIe496B9MM$7 */
    final /* synthetic */ class AnonymousClass7 implements Predicate {
        private final /* synthetic */ long -$f0;

        private final /* synthetic */ boolean $m$0(Object arg0) {
            return PackageManagerServiceUtils.lambda$-com_android_server_pm_PackageManagerServiceUtils_7053(this.-$f0, (Package) arg0);
        }

        public /* synthetic */ AnonymousClass7(long j) {
            this.-$f0 = j;
        }

        public final boolean test(Object obj) {
            return $m$0(obj);
        }
    }

    public final int compare(Object obj, Object obj2) {
        return $m$0(obj, obj2);
    }
}
