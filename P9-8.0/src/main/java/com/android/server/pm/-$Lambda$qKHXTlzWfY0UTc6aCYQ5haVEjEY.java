package com.android.server.pm;

import android.content.ComponentName;
import android.content.pm.ApplicationInfo;
import android.content.pm.ShortcutInfo;
import android.util.ArraySet;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

final /* synthetic */ class -$Lambda$qKHXTlzWfY0UTc6aCYQ5haVEjEY implements Consumer {

    /* renamed from: com.android.server.pm.-$Lambda$qKHXTlzWfY0UTc6aCYQ5haVEjEY$10 */
    final /* synthetic */ class AnonymousClass10 implements Predicate {
        private final /* synthetic */ Object -$f0;

        private final /* synthetic */ boolean $m$0(Object arg0) {
            return ((String) this.-$f0).equals(((ShortcutInfo) arg0).getId());
        }

        public /* synthetic */ AnonymousClass10(Object obj) {
            this.-$f0 = obj;
        }

        public final boolean test(Object obj) {
            return $m$0(obj);
        }
    }

    /* renamed from: com.android.server.pm.-$Lambda$qKHXTlzWfY0UTc6aCYQ5haVEjEY$11 */
    final /* synthetic */ class AnonymousClass11 implements Consumer {
        private final /* synthetic */ Object -$f0;
        private final /* synthetic */ Object -$f1;

        private final /* synthetic */ void $m$0(Object arg0) {
            ((ShortcutService) this.-$f0).lambda$-com_android_server_pm_ShortcutService_102576((ArrayList) this.-$f1, (ShortcutPackageItem) arg0);
        }

        public /* synthetic */ AnonymousClass11(Object obj, Object obj2) {
            this.-$f0 = obj;
            this.-$f1 = obj2;
        }

        public final void accept(Object obj) {
            $m$0(obj);
        }
    }

    /* renamed from: com.android.server.pm.-$Lambda$qKHXTlzWfY0UTc6aCYQ5haVEjEY$12 */
    final /* synthetic */ class AnonymousClass12 implements Consumer {
        private final /* synthetic */ int -$f0;
        private final /* synthetic */ Object -$f1;

        private final /* synthetic */ void $m$0(Object arg0) {
            ((ShortcutLauncher) arg0).lambda$-com_android_server_pm_ShortcutService_84229((String) this.-$f1, this.-$f0);
        }

        public /* synthetic */ AnonymousClass12(int i, Object obj) {
            this.-$f0 = i;
            this.-$f1 = obj;
        }

        public final void accept(Object obj) {
            $m$0(obj);
        }
    }

    /* renamed from: com.android.server.pm.-$Lambda$qKHXTlzWfY0UTc6aCYQ5haVEjEY$13 */
    final /* synthetic */ class AnonymousClass13 implements Runnable {
        private final /* synthetic */ int -$f0;
        private final /* synthetic */ Object -$f1;
        private final /* synthetic */ Object -$f2;

        private final /* synthetic */ void $m$0() {
            ((ShortcutService) this.-$f1).lambda$-com_android_server_pm_ShortcutService_54750(this.-$f0, (String) this.-$f2);
        }

        public /* synthetic */ AnonymousClass13(int i, Object obj, Object obj2) {
            this.-$f0 = i;
            this.-$f1 = obj;
            this.-$f2 = obj2;
        }

        public final void run() {
            $m$0();
        }
    }

    /* renamed from: com.android.server.pm.-$Lambda$qKHXTlzWfY0UTc6aCYQ5haVEjEY$14 */
    final /* synthetic */ class AnonymousClass14 implements Consumer {
        private final /* synthetic */ int -$f0;
        private final /* synthetic */ Object -$f1;
        private final /* synthetic */ Object -$f2;

        private final /* synthetic */ void $m$0(Object arg0) {
            ((ShortcutService) this.-$f1).lambda$-com_android_server_pm_ShortcutService_104347((ShortcutUser) this.-$f2, this.-$f0, (ApplicationInfo) arg0);
        }

        public /* synthetic */ AnonymousClass14(int i, Object obj, Object obj2) {
            this.-$f0 = i;
            this.-$f1 = obj;
            this.-$f2 = obj2;
        }

        public final void accept(Object obj) {
            $m$0(obj);
        }
    }

    /* renamed from: com.android.server.pm.-$Lambda$qKHXTlzWfY0UTc6aCYQ5haVEjEY$15 */
    final /* synthetic */ class AnonymousClass15 implements Runnable {
        private final /* synthetic */ int -$f0;
        private final /* synthetic */ long -$f1;
        private final /* synthetic */ Object -$f2;

        private final /* synthetic */ void $m$0() {
            ((ShortcutService) this.-$f2).lambda$-com_android_server_pm_ShortcutService_21393(this.-$f1, this.-$f0);
        }

        public /* synthetic */ AnonymousClass15(int i, long j, Object obj) {
            this.-$f0 = i;
            this.-$f1 = j;
            this.-$f2 = obj;
        }

        public final void run() {
            $m$0();
        }
    }

    /* renamed from: com.android.server.pm.-$Lambda$qKHXTlzWfY0UTc6aCYQ5haVEjEY$16 */
    final /* synthetic */ class AnonymousClass16 implements Predicate {
        private final /* synthetic */ int -$f0;
        private final /* synthetic */ long -$f1;
        private final /* synthetic */ Object -$f2;
        private final /* synthetic */ Object -$f3;

        private final /* synthetic */ boolean $m$0(Object arg0) {
            return LocalService.lambda$-com_android_server_pm_ShortcutService$LocalService_87902(this.-$f1, (ArraySet) this.-$f2, (ComponentName) this.-$f3, this.-$f0, (ShortcutInfo) arg0);
        }

        public /* synthetic */ AnonymousClass16(int i, long j, Object obj, Object obj2) {
            this.-$f0 = i;
            this.-$f1 = j;
            this.-$f2 = obj;
            this.-$f3 = obj2;
        }

        public final boolean test(Object obj) {
            return $m$0(obj);
        }
    }

    /* renamed from: com.android.server.pm.-$Lambda$qKHXTlzWfY0UTc6aCYQ5haVEjEY$17 */
    final /* synthetic */ class AnonymousClass17 implements Consumer {
        private final /* synthetic */ int -$f0;
        private final /* synthetic */ int -$f1;
        private final /* synthetic */ int -$f2;
        private final /* synthetic */ int -$f3;
        private final /* synthetic */ long -$f4;
        private final /* synthetic */ Object -$f5;
        private final /* synthetic */ Object -$f6;
        private final /* synthetic */ Object -$f7;
        private final /* synthetic */ Object -$f8;
        private final /* synthetic */ Object -$f9;

        private final /* synthetic */ void $m$0(Object arg0) {
            ((LocalService) this.-$f5).lambda$-com_android_server_pm_ShortcutService$LocalService_86786(this.-$f0, (String) this.-$f6, (List) this.-$f7, this.-$f4, (ComponentName) this.-$f8, this.-$f1, this.-$f2, (ArrayList) this.-$f9, this.-$f3, (ShortcutPackage) arg0);
        }

        public /* synthetic */ AnonymousClass17(int i, int i2, int i3, int i4, long j, Object obj, Object obj2, Object obj3, Object obj4, Object obj5) {
            this.-$f0 = i;
            this.-$f1 = i2;
            this.-$f2 = i3;
            this.-$f3 = i4;
            this.-$f4 = j;
            this.-$f5 = obj;
            this.-$f6 = obj2;
            this.-$f7 = obj3;
            this.-$f8 = obj4;
            this.-$f9 = obj5;
        }

        public final void accept(Object obj) {
            $m$0(obj);
        }
    }

    /* renamed from: com.android.server.pm.-$Lambda$qKHXTlzWfY0UTc6aCYQ5haVEjEY$18 */
    final /* synthetic */ class AnonymousClass18 implements Consumer {
        private final /* synthetic */ boolean -$f0;
        private final /* synthetic */ int -$f1;
        private final /* synthetic */ Object -$f2;
        private final /* synthetic */ Object -$f3;

        private final /* synthetic */ void $m$0(Object arg0) {
            ((ShortcutService) this.-$f2).lambda$-com_android_server_pm_ShortcutService_82952((String) this.-$f3, this.-$f1, this.-$f0, (ShortcutUser) arg0);
        }

        public /* synthetic */ AnonymousClass18(boolean z, int i, Object obj, Object obj2) {
            this.-$f0 = z;
            this.-$f1 = i;
            this.-$f2 = obj;
            this.-$f3 = obj2;
        }

        public final void accept(Object obj) {
            $m$0(obj);
        }
    }

    /* renamed from: com.android.server.pm.-$Lambda$qKHXTlzWfY0UTc6aCYQ5haVEjEY$9 */
    final /* synthetic */ class AnonymousClass9 implements Runnable {
        private final /* synthetic */ Object -$f0;

        private final /* synthetic */ void $m$0() {
            ((ShortcutService) this.-$f0).-com_android_server_pm_ShortcutService-mthref-0();
        }

        public /* synthetic */ AnonymousClass9(Object obj) {
            this.-$f0 = obj;
        }

        public final void run() {
            $m$0();
        }
    }

    public final void accept(Object obj) {
        $m$0(obj);
    }
}
