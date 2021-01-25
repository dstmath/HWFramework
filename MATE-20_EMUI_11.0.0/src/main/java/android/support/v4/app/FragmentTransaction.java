package android.support.v4.app;

import android.support.annotation.AnimRes;
import android.support.annotation.AnimatorRes;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.annotation.StyleRes;
import android.view.View;

public abstract class FragmentTransaction {
    public static final int TRANSIT_ENTER_MASK = 4096;
    public static final int TRANSIT_EXIT_MASK = 8192;
    public static final int TRANSIT_FRAGMENT_CLOSE = 8194;
    public static final int TRANSIT_FRAGMENT_FADE = 4099;
    public static final int TRANSIT_FRAGMENT_OPEN = 4097;
    public static final int TRANSIT_NONE = 0;
    public static final int TRANSIT_UNSET = -1;

    @NonNull
    public abstract FragmentTransaction add(@IdRes int i, @NonNull Fragment fragment);

    @NonNull
    public abstract FragmentTransaction add(@IdRes int i, @NonNull Fragment fragment, @Nullable String str);

    @NonNull
    public abstract FragmentTransaction add(@NonNull Fragment fragment, @Nullable String str);

    @NonNull
    public abstract FragmentTransaction addSharedElement(@NonNull View view, @NonNull String str);

    @NonNull
    public abstract FragmentTransaction addToBackStack(@Nullable String str);

    @NonNull
    public abstract FragmentTransaction attach(@NonNull Fragment fragment);

    public abstract int commit();

    public abstract int commitAllowingStateLoss();

    public abstract void commitNow();

    public abstract void commitNowAllowingStateLoss();

    @NonNull
    public abstract FragmentTransaction detach(@NonNull Fragment fragment);

    @NonNull
    public abstract FragmentTransaction disallowAddToBackStack();

    @NonNull
    public abstract FragmentTransaction hide(@NonNull Fragment fragment);

    public abstract boolean isAddToBackStackAllowed();

    public abstract boolean isEmpty();

    @NonNull
    public abstract FragmentTransaction remove(@NonNull Fragment fragment);

    @NonNull
    public abstract FragmentTransaction replace(@IdRes int i, @NonNull Fragment fragment);

    @NonNull
    public abstract FragmentTransaction replace(@IdRes int i, @NonNull Fragment fragment, @Nullable String str);

    @NonNull
    public abstract FragmentTransaction runOnCommit(@NonNull Runnable runnable);

    @Deprecated
    public abstract FragmentTransaction setAllowOptimization(boolean z);

    @NonNull
    public abstract FragmentTransaction setBreadCrumbShortTitle(@StringRes int i);

    @NonNull
    public abstract FragmentTransaction setBreadCrumbShortTitle(@Nullable CharSequence charSequence);

    @NonNull
    public abstract FragmentTransaction setBreadCrumbTitle(@StringRes int i);

    @NonNull
    public abstract FragmentTransaction setBreadCrumbTitle(@Nullable CharSequence charSequence);

    @NonNull
    public abstract FragmentTransaction setCustomAnimations(@AnimRes @AnimatorRes int i, @AnimRes @AnimatorRes int i2);

    @NonNull
    public abstract FragmentTransaction setCustomAnimations(@AnimRes @AnimatorRes int i, @AnimRes @AnimatorRes int i2, @AnimRes @AnimatorRes int i3, @AnimRes @AnimatorRes int i4);

    @NonNull
    public abstract FragmentTransaction setPrimaryNavigationFragment(@Nullable Fragment fragment);

    @NonNull
    public abstract FragmentTransaction setReorderingAllowed(boolean z);

    @NonNull
    public abstract FragmentTransaction setTransition(int i);

    @NonNull
    public abstract FragmentTransaction setTransitionStyle(@StyleRes int i);

    @NonNull
    public abstract FragmentTransaction show(@NonNull Fragment fragment);
}
