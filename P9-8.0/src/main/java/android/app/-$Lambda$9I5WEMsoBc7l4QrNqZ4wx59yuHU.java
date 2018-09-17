package android.app;

import android.content.res.Configuration;
import android.view.ViewRootImpl.ActivityConfigCallback;
import android.view.ViewRootImpl.ConfigChangedCallback;

final /* synthetic */ class -$Lambda$9I5WEMsoBc7l4QrNqZ4wx59yuHU implements ActivityConfigCallback {
    private final /* synthetic */ Object -$f0;

    /* renamed from: android.app.-$Lambda$9I5WEMsoBc7l4QrNqZ4wx59yuHU$1 */
    final /* synthetic */ class AnonymousClass1 implements ConfigChangedCallback {
        private final /* synthetic */ Object -$f0;

        private final /* synthetic */ void $m$0(Configuration arg0) {
            ((ActivityThread) this.-$f0).lambda$-android_app_ActivityThread_325384(arg0);
        }

        public /* synthetic */ AnonymousClass1(Object obj) {
            this.-$f0 = obj;
        }

        public final void onConfigurationChanged(Configuration configuration) {
            $m$0(configuration);
        }
    }

    private final /* synthetic */ void $m$0(Configuration arg0, int arg1) {
        ((ActivityClientRecord) this.-$f0).lambda$-android_app_ActivityThread$ActivityClientRecord_19613(arg0, arg1);
    }

    public /* synthetic */ -$Lambda$9I5WEMsoBc7l4QrNqZ4wx59yuHU(Object obj) {
        this.-$f0 = obj;
    }

    public final void onConfigurationChanged(Configuration configuration, int i) {
        $m$0(configuration, i);
    }
}
