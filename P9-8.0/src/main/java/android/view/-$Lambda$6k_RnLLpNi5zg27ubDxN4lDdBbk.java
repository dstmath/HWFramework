package android.view;

import android.view.FocusFinder.UserSpecifiedFocusComparator.NextFocusGetter;
import java.util.Comparator;

final /* synthetic */ class -$Lambda$6k_RnLLpNi5zg27ubDxN4lDdBbk implements NextFocusGetter {

    /* renamed from: android.view.-$Lambda$6k_RnLLpNi5zg27ubDxN4lDdBbk$2 */
    final /* synthetic */ class AnonymousClass2 implements Comparator {
        private final /* synthetic */ Object -$f0;

        private final /* synthetic */ int $m$0(Object arg0, Object arg1) {
            return ((FocusSorter) this.-$f0).lambda$-android_view_FocusFinder$FocusSorter_31467((View) arg0, (View) arg1);
        }

        public /* synthetic */ AnonymousClass2(Object obj) {
            this.-$f0 = obj;
        }

        public final int compare(Object obj, Object obj2) {
            return $m$0(obj, obj2);
        }
    }

    /* renamed from: android.view.-$Lambda$6k_RnLLpNi5zg27ubDxN4lDdBbk$3 */
    final /* synthetic */ class AnonymousClass3 implements Comparator {
        private final /* synthetic */ Object -$f0;

        private final /* synthetic */ int $m$0(Object arg0, Object arg1) {
            return ((FocusSorter) this.-$f0).lambda$-android_view_FocusFinder$FocusSorter_31927((View) arg0, (View) arg1);
        }

        public /* synthetic */ AnonymousClass3(Object obj) {
            this.-$f0 = obj;
        }

        public final int compare(Object obj, Object obj2) {
            return $m$0(obj, obj2);
        }
    }

    public final View get(View view, View view2) {
        return $m$0(view, view2);
    }
}
