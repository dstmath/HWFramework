package android.support.v4.app;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.view.View;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.List;

public abstract class FragmentManager {
    public static final int POP_BACK_STACK_INCLUSIVE = 1;

    public interface BackStackEntry {
        @Nullable
        CharSequence getBreadCrumbShortTitle();

        @StringRes
        int getBreadCrumbShortTitleRes();

        @Nullable
        CharSequence getBreadCrumbTitle();

        @StringRes
        int getBreadCrumbTitleRes();

        int getId();

        @Nullable
        String getName();
    }

    public interface OnBackStackChangedListener {
        void onBackStackChanged();
    }

    public abstract void addOnBackStackChangedListener(@NonNull OnBackStackChangedListener onBackStackChangedListener);

    @NonNull
    public abstract FragmentTransaction beginTransaction();

    public abstract void dump(String str, FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr);

    public abstract boolean executePendingTransactions();

    @Nullable
    public abstract Fragment findFragmentById(@IdRes int i);

    @Nullable
    public abstract Fragment findFragmentByTag(@Nullable String str);

    @NonNull
    public abstract BackStackEntry getBackStackEntryAt(int i);

    public abstract int getBackStackEntryCount();

    @Nullable
    public abstract Fragment getFragment(@NonNull Bundle bundle, @NonNull String str);

    @NonNull
    public abstract List<Fragment> getFragments();

    @Nullable
    public abstract Fragment getPrimaryNavigationFragment();

    public abstract boolean isDestroyed();

    public abstract boolean isStateSaved();

    public abstract void popBackStack();

    public abstract void popBackStack(int i, int i2);

    public abstract void popBackStack(@Nullable String str, int i);

    public abstract boolean popBackStackImmediate();

    public abstract boolean popBackStackImmediate(int i, int i2);

    public abstract boolean popBackStackImmediate(@Nullable String str, int i);

    public abstract void putFragment(@NonNull Bundle bundle, @NonNull String str, @NonNull Fragment fragment);

    public abstract void registerFragmentLifecycleCallbacks(@NonNull FragmentLifecycleCallbacks fragmentLifecycleCallbacks, boolean z);

    public abstract void removeOnBackStackChangedListener(@NonNull OnBackStackChangedListener onBackStackChangedListener);

    @Nullable
    public abstract Fragment.SavedState saveFragmentInstanceState(Fragment fragment);

    public abstract void unregisterFragmentLifecycleCallbacks(@NonNull FragmentLifecycleCallbacks fragmentLifecycleCallbacks);

    @RestrictTo({RestrictTo.Scope.LIBRARY_GROUP})
    @Deprecated
    public FragmentTransaction openTransaction() {
        return beginTransaction();
    }

    public static void enableDebugLogging(boolean enabled) {
        FragmentManagerImpl.DEBUG = enabled;
    }

    public static abstract class FragmentLifecycleCallbacks {
        public void onFragmentPreAttached(@NonNull FragmentManager fm, @NonNull Fragment f, @NonNull Context context) {
        }

        public void onFragmentAttached(@NonNull FragmentManager fm, @NonNull Fragment f, @NonNull Context context) {
        }

        public void onFragmentPreCreated(@NonNull FragmentManager fm, @NonNull Fragment f, @Nullable Bundle savedInstanceState) {
        }

        public void onFragmentCreated(@NonNull FragmentManager fm, @NonNull Fragment f, @Nullable Bundle savedInstanceState) {
        }

        public void onFragmentActivityCreated(@NonNull FragmentManager fm, @NonNull Fragment f, @Nullable Bundle savedInstanceState) {
        }

        public void onFragmentViewCreated(@NonNull FragmentManager fm, @NonNull Fragment f, @NonNull View v, @Nullable Bundle savedInstanceState) {
        }

        public void onFragmentStarted(@NonNull FragmentManager fm, @NonNull Fragment f) {
        }

        public void onFragmentResumed(@NonNull FragmentManager fm, @NonNull Fragment f) {
        }

        public void onFragmentPaused(@NonNull FragmentManager fm, @NonNull Fragment f) {
        }

        public void onFragmentStopped(@NonNull FragmentManager fm, @NonNull Fragment f) {
        }

        public void onFragmentSaveInstanceState(@NonNull FragmentManager fm, @NonNull Fragment f, @NonNull Bundle outState) {
        }

        public void onFragmentViewDestroyed(@NonNull FragmentManager fm, @NonNull Fragment f) {
        }

        public void onFragmentDestroyed(@NonNull FragmentManager fm, @NonNull Fragment f) {
        }

        public void onFragmentDetached(@NonNull FragmentManager fm, @NonNull Fragment f) {
        }
    }
}
