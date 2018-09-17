package com.android.server.companion;

import android.content.pm.PackageInfo;
import com.android.internal.util.CollectionUtils;
import com.android.internal.util.FunctionalUtils.ThrowingRunnable;
import com.android.internal.util.FunctionalUtils.ThrowingSupplier;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

final /* synthetic */ class -$Lambda$AGIrYO-M2umJsGqqjbn8lgb57iM implements Function {

    /* renamed from: com.android.server.companion.-$Lambda$AGIrYO-M2umJsGqqjbn8lgb57iM$2 */
    final /* synthetic */ class AnonymousClass2 implements Runnable {
        private final /* synthetic */ Object -$f0;

        private final /* synthetic */ void $m$0() {
            ((CompanionDeviceManagerService) this.-$f0).-com_android_server_companion_CompanionDeviceManagerService-mthref-0();
        }

        public /* synthetic */ AnonymousClass2(Object obj) {
            this.-$f0 = obj;
        }

        public final void run() {
            $m$0();
        }
    }

    /* renamed from: com.android.server.companion.-$Lambda$AGIrYO-M2umJsGqqjbn8lgb57iM$3 */
    final /* synthetic */ class AnonymousClass3 implements Consumer {
        private final /* synthetic */ Object -$f0;

        private final /* synthetic */ void $m$0(Object arg0) {
            CompanionDeviceManagerService.lambda$-com_android_server_companion_CompanionDeviceManagerService_20901((List) this.-$f0, (FileOutputStream) arg0);
        }

        public /* synthetic */ AnonymousClass3(Object obj) {
            this.-$f0 = obj;
        }

        public final void accept(Object obj) {
            $m$0(obj);
        }
    }

    /* renamed from: com.android.server.companion.-$Lambda$AGIrYO-M2umJsGqqjbn8lgb57iM$4 */
    final /* synthetic */ class AnonymousClass4 implements Function {
        private final /* synthetic */ Object -$f0;

        private final /* synthetic */ Object $m$0(Object arg0) {
            return CollectionUtils.filter((List) arg0, new AnonymousClass5((String) this.-$f0));
        }

        public /* synthetic */ AnonymousClass4(Object obj) {
            this.-$f0 = obj;
        }

        public final Object apply(Object obj) {
            return $m$0(obj);
        }
    }

    /* renamed from: com.android.server.companion.-$Lambda$AGIrYO-M2umJsGqqjbn8lgb57iM$5 */
    final /* synthetic */ class AnonymousClass5 implements Predicate {
        private final /* synthetic */ Object -$f0;

        private final /* synthetic */ boolean $m$0(Object arg0) {
            return (Objects.equals(((Association) arg0).companionAppPackage, (String) this.-$f0) ^ 1);
        }

        public /* synthetic */ AnonymousClass5(Object obj) {
            this.-$f0 = obj;
        }

        public final boolean test(Object obj) {
            return $m$0(obj);
        }
    }

    /* renamed from: com.android.server.companion.-$Lambda$AGIrYO-M2umJsGqqjbn8lgb57iM$6 */
    final /* synthetic */ class AnonymousClass6 implements ThrowingRunnable {
        private final /* synthetic */ Object -$f0;
        private final /* synthetic */ Object -$f1;

        private final /* synthetic */ void $m$0() {
            ((CompanionDeviceManagerService) this.-$f0).lambda$-com_android_server_companion_CompanionDeviceManagerService_17629((PackageInfo) this.-$f1);
        }

        public /* synthetic */ AnonymousClass6(Object obj, Object obj2) {
            this.-$f0 = obj;
            this.-$f1 = obj2;
        }

        public final void run() {
            $m$0();
        }
    }

    /* renamed from: com.android.server.companion.-$Lambda$AGIrYO-M2umJsGqqjbn8lgb57iM$7 */
    final /* synthetic */ class AnonymousClass7 implements ThrowingSupplier {
        private final /* synthetic */ int -$f0;
        private final /* synthetic */ Object -$f1;
        private final /* synthetic */ Object -$f2;

        private final /* synthetic */ Object $m$0() {
            return ((CompanionDeviceManagerService) this.-$f1).lambda$-com_android_server_companion_CompanionDeviceManagerService_19221((String) this.-$f2, this.-$f0);
        }

        public /* synthetic */ AnonymousClass7(int i, Object obj, Object obj2) {
            this.-$f0 = i;
            this.-$f1 = obj;
            this.-$f2 = obj2;
        }

        public final Object get() {
            return $m$0();
        }
    }

    /* renamed from: com.android.server.companion.-$Lambda$AGIrYO-M2umJsGqqjbn8lgb57iM$8 */
    final /* synthetic */ class AnonymousClass8 implements Function {
        private final /* synthetic */ int -$f0;
        private final /* synthetic */ Object -$f1;
        private final /* synthetic */ Object -$f2;
        private final /* synthetic */ Object -$f3;

        private final /* synthetic */ Object $m$0(Object arg0) {
            return ((CompanionDeviceManagerService) this.-$f1).lambda$-com_android_server_companion_CompanionDeviceManagerService_20046(this.-$f0, (String) this.-$f2, (String) this.-$f3, (List) arg0);
        }

        public /* synthetic */ AnonymousClass8(int i, Object obj, Object obj2, Object obj3) {
            this.-$f0 = i;
            this.-$f1 = obj;
            this.-$f2 = obj2;
            this.-$f3 = obj3;
        }

        public final Object apply(Object obj) {
            return $m$0(obj);
        }
    }

    /* renamed from: com.android.server.companion.-$Lambda$AGIrYO-M2umJsGqqjbn8lgb57iM$9 */
    final /* synthetic */ class AnonymousClass9 implements Function {
        private final /* synthetic */ int -$f0;
        private final /* synthetic */ Object -$f1;
        private final /* synthetic */ Object -$f2;
        private final /* synthetic */ Object -$f3;

        private final /* synthetic */ Object $m$0(Object arg0) {
            return ((CompanionDeviceManagerService) this.-$f1).lambda$-com_android_server_companion_CompanionDeviceManagerService_17225(this.-$f0, (String) this.-$f2, (String) this.-$f3, (List) arg0);
        }

        public /* synthetic */ AnonymousClass9(int i, Object obj, Object obj2, Object obj3) {
            this.-$f0 = i;
            this.-$f1 = obj;
            this.-$f2 = obj2;
            this.-$f3 = obj3;
        }

        public final Object apply(Object obj) {
            return $m$0(obj);
        }
    }

    public final Object apply(Object obj) {
        return $m$0(obj);
    }
}
