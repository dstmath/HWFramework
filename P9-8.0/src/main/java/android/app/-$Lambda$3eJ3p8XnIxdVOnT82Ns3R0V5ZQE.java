package android.app;

import android.app.FragmentTransition.FragmentContainerTransition;
import android.graphics.Rect;
import android.transition.Transition;
import android.transition.TransitionSet;
import android.util.ArrayMap;
import android.view.View;
import java.util.ArrayList;

final /* synthetic */ class -$Lambda$3eJ3p8XnIxdVOnT82Ns3R0V5ZQE implements Runnable {
    private final /* synthetic */ Object -$f0;

    /* renamed from: android.app.-$Lambda$3eJ3p8XnIxdVOnT82Ns3R0V5ZQE$1 */
    final /* synthetic */ class AnonymousClass1 implements Runnable {
        private final /* synthetic */ Object -$f0;
        private final /* synthetic */ Object -$f1;
        private final /* synthetic */ Object -$f2;
        private final /* synthetic */ Object -$f3;
        private final /* synthetic */ Object -$f4;
        private final /* synthetic */ Object -$f5;
        private final /* synthetic */ Object -$f6;

        private final /* synthetic */ void $m$0() {
            FragmentTransition.lambda$-android_app_FragmentTransition_18686((Transition) this.-$f0, (View) this.-$f1, (Fragment) this.-$f2, (ArrayList) this.-$f3, (ArrayList) this.-$f4, (ArrayList) this.-$f5, (Transition) this.-$f6);
        }

        public /* synthetic */ AnonymousClass1(Object obj, Object obj2, Object obj3, Object obj4, Object obj5, Object obj6, Object obj7) {
            this.-$f0 = obj;
            this.-$f1 = obj2;
            this.-$f2 = obj3;
            this.-$f3 = obj4;
            this.-$f4 = obj5;
            this.-$f5 = obj6;
            this.-$f6 = obj7;
        }

        public final void run() {
            $m$0();
        }
    }

    /* renamed from: android.app.-$Lambda$3eJ3p8XnIxdVOnT82Ns3R0V5ZQE$2 */
    final /* synthetic */ class AnonymousClass2 implements Runnable {
        private final /* synthetic */ boolean -$f0;
        private final /* synthetic */ Object -$f1;
        private final /* synthetic */ Object -$f2;
        private final /* synthetic */ Object -$f3;
        private final /* synthetic */ Object -$f4;
        private final /* synthetic */ Object -$f5;

        private final /* synthetic */ void $m$0() {
            FragmentTransition.lambda$-android_app_FragmentTransition_26843((Fragment) this.-$f1, (Fragment) this.-$f2, this.-$f0, (ArrayMap) this.-$f3, (View) this.-$f4, (Rect) this.-$f5);
        }

        public /* synthetic */ AnonymousClass2(boolean z, Object obj, Object obj2, Object obj3, Object obj4, Object obj5) {
            this.-$f0 = z;
            this.-$f1 = obj;
            this.-$f2 = obj2;
            this.-$f3 = obj3;
            this.-$f4 = obj4;
            this.-$f5 = obj5;
        }

        public final void run() {
            $m$0();
        }
    }

    /* renamed from: android.app.-$Lambda$3eJ3p8XnIxdVOnT82Ns3R0V5ZQE$3 */
    final /* synthetic */ class AnonymousClass3 implements Runnable {
        private final /* synthetic */ boolean -$f0;
        private final /* synthetic */ Object -$f1;
        private final /* synthetic */ Object -$f10;
        private final /* synthetic */ Object -$f2;
        private final /* synthetic */ Object -$f3;
        private final /* synthetic */ Object -$f4;
        private final /* synthetic */ Object -$f5;
        private final /* synthetic */ Object -$f6;
        private final /* synthetic */ Object -$f7;
        private final /* synthetic */ Object -$f8;
        private final /* synthetic */ Object -$f9;

        private final /* synthetic */ void $m$0() {
            FragmentTransition.lambda$-android_app_FragmentTransition_32404((ArrayMap) this.-$f1, (TransitionSet) this.-$f2, (FragmentContainerTransition) this.-$f3, (ArrayList) this.-$f4, (View) this.-$f5, (Fragment) this.-$f6, (Fragment) this.-$f7, this.-$f0, (ArrayList) this.-$f8, (Transition) this.-$f9, (Rect) this.-$f10);
        }

        public /* synthetic */ AnonymousClass3(boolean z, Object obj, Object obj2, Object obj3, Object obj4, Object obj5, Object obj6, Object obj7, Object obj8, Object obj9, Object obj10) {
            this.-$f0 = z;
            this.-$f1 = obj;
            this.-$f2 = obj2;
            this.-$f3 = obj3;
            this.-$f4 = obj4;
            this.-$f5 = obj5;
            this.-$f6 = obj6;
            this.-$f7 = obj7;
            this.-$f8 = obj8;
            this.-$f9 = obj9;
            this.-$f10 = obj10;
        }

        public final void run() {
            $m$0();
        }
    }

    private final /* synthetic */ void $m$0() {
        FragmentTransition.setViewVisibility((ArrayList) this.-$f0, 4);
    }

    public /* synthetic */ -$Lambda$3eJ3p8XnIxdVOnT82Ns3R0V5ZQE(Object obj) {
        this.-$f0 = obj;
    }

    public final void run() {
        $m$0();
    }
}
