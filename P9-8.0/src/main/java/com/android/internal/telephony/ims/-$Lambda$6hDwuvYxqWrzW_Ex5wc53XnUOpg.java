package com.android.internal.telephony.ims;

import android.content.ComponentName;
import android.content.Context;
import android.os.Handler.Callback;
import android.os.Message;
import android.util.Pair;
import com.android.internal.telephony.ims.ImsResolver.ImsServiceControllerFactory;
import com.android.internal.telephony.ims.ImsResolver.ImsServiceInfo;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

final /* synthetic */ class -$Lambda$6hDwuvYxqWrzW_Ex5wc53XnUOpg implements Supplier {

    /* renamed from: com.android.internal.telephony.ims.-$Lambda$6hDwuvYxqWrzW_Ex5wc53XnUOpg$1 */
    final /* synthetic */ class AnonymousClass1 implements Callback {
        private final /* synthetic */ Object -$f0;

        private final /* synthetic */ boolean $m$0(Message arg0) {
            return ((ImsResolver) this.-$f0).lambda$-com_android_internal_telephony_ims_ImsResolver_8747(arg0);
        }

        public /* synthetic */ AnonymousClass1(Object obj) {
            this.-$f0 = obj;
        }

        public final boolean handleMessage(Message message) {
            return $m$0(message);
        }
    }

    /* renamed from: com.android.internal.telephony.ims.-$Lambda$6hDwuvYxqWrzW_Ex5wc53XnUOpg$2 */
    final /* synthetic */ class AnonymousClass2 implements ImsServiceControllerFactory {
        private final /* synthetic */ Object -$f0;

        private final /* synthetic */ ImsServiceController $m$0(Context arg0, ComponentName arg1) {
            return ((ImsResolver) this.-$f0).lambda$-com_android_internal_telephony_ims_ImsResolver_8131(arg0, arg1);
        }

        public /* synthetic */ AnonymousClass2(Object obj) {
            this.-$f0 = obj;
        }

        public final ImsServiceController get(Context context, ComponentName componentName) {
            return $m$0(context, componentName);
        }
    }

    /* renamed from: com.android.internal.telephony.ims.-$Lambda$6hDwuvYxqWrzW_Ex5wc53XnUOpg$3 */
    final /* synthetic */ class AnonymousClass3 implements Predicate {
        private final /* synthetic */ Object -$f0;

        private final /* synthetic */ boolean $m$0(Object arg0) {
            return Objects.equals(((ImsServiceController) arg0).getComponentName(), ((ImsServiceInfo) this.-$f0).name);
        }

        public /* synthetic */ AnonymousClass3(Object obj) {
            this.-$f0 = obj;
        }

        public final boolean test(Object obj) {
            return $m$0(obj);
        }
    }

    /* renamed from: com.android.internal.telephony.ims.-$Lambda$6hDwuvYxqWrzW_Ex5wc53XnUOpg$4 */
    final /* synthetic */ class AnonymousClass4 implements Predicate {
        private final /* synthetic */ Object -$f0;

        private final /* synthetic */ boolean $m$0(Object arg0) {
            return Objects.equals(((ImsServiceInfo) arg0).name, (ComponentName) this.-$f0);
        }

        public /* synthetic */ AnonymousClass4(Object obj) {
            this.-$f0 = obj;
        }

        public final boolean test(Object obj) {
            return $m$0(obj);
        }
    }

    /* renamed from: com.android.internal.telephony.ims.-$Lambda$6hDwuvYxqWrzW_Ex5wc53XnUOpg$5 */
    final /* synthetic */ class AnonymousClass5 implements Predicate {
        private final /* synthetic */ Object -$f0;

        private final /* synthetic */ boolean $m$0(Object arg0) {
            return Objects.equals(((ImsServiceInfo) arg0).name.getPackageName(), (String) this.-$f0);
        }

        public /* synthetic */ AnonymousClass5(Object obj) {
            this.-$f0 = obj;
        }

        public final boolean test(Object obj) {
            return $m$0(obj);
        }
    }

    /* renamed from: com.android.internal.telephony.ims.-$Lambda$6hDwuvYxqWrzW_Ex5wc53XnUOpg$6 */
    final /* synthetic */ class AnonymousClass6 implements Function {
        private final /* synthetic */ int -$f0;

        private final /* synthetic */ Object $m$0(Object arg0) {
            return new Pair(Integer.valueOf(this.-$f0), (Integer) arg0);
        }

        public /* synthetic */ AnonymousClass6(int i) {
            this.-$f0 = i;
        }

        public final Object apply(Object obj) {
            return $m$0(obj);
        }
    }

    /* renamed from: com.android.internal.telephony.ims.-$Lambda$6hDwuvYxqWrzW_Ex5wc53XnUOpg$7 */
    final /* synthetic */ class AnonymousClass7 implements Function {
        private final /* synthetic */ int -$f0;

        private final /* synthetic */ Object $m$0(Object arg0) {
            return new Pair(Integer.valueOf(this.-$f0), (Integer) arg0);
        }

        public /* synthetic */ AnonymousClass7(int i) {
            this.-$f0 = i;
        }

        public final Object apply(Object obj) {
            return $m$0(obj);
        }
    }

    /* renamed from: com.android.internal.telephony.ims.-$Lambda$6hDwuvYxqWrzW_Ex5wc53XnUOpg$8 */
    final /* synthetic */ class AnonymousClass8 implements Function {
        private final /* synthetic */ int -$f0;

        private final /* synthetic */ Object $m$0(Object arg0) {
            return new Pair(Integer.valueOf(this.-$f0), (Integer) arg0);
        }

        public /* synthetic */ AnonymousClass8(int i) {
            this.-$f0 = i;
        }

        public final Object apply(Object obj) {
            return $m$0(obj);
        }
    }

    public final Object get() {
        return $m$0();
    }
}
