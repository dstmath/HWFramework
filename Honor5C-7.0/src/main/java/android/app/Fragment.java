package android.app;

import android.animation.Animator;
import android.content.ComponentCallbacks2;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.IntentSender.SendIntentException;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.net.wifi.WifiEnterpriseConfig;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.ClassLoaderCreator;
import android.os.Trace;
import android.security.keymaster.KeymasterDefs;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.transition.TransitionSet;
import android.util.AndroidRuntimeException;
import android.util.ArrayMap;
import android.util.AttributeSet;
import android.util.DebugUtils;
import android.util.SparseArray;
import android.util.SuperNotCalledException;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.view.ViewGroup;
import com.android.internal.R;
import java.io.FileDescriptor;
import java.io.PrintWriter;

public class Fragment implements ComponentCallbacks2, OnCreateContextMenuListener {
    static final int ACTIVITY_CREATED = 2;
    static final int CREATED = 1;
    static final int INITIALIZING = 0;
    static final int INVALID_STATE = -1;
    static final int RESUMED = 5;
    static final int STARTED = 4;
    static final int STOPPED = 3;
    private static final Transition USE_DEFAULT_TRANSITION = null;
    private static final ArrayMap<String, Class<?>> sClassMap = null;
    boolean mAdded;
    private Boolean mAllowEnterTransitionOverlap;
    private Boolean mAllowReturnTransitionOverlap;
    Animator mAnimatingAway;
    Bundle mArguments;
    int mBackStackNesting;
    boolean mCalled;
    boolean mCheckedForLoaderManager;
    FragmentManagerImpl mChildFragmentManager;
    FragmentManagerNonConfig mChildNonConfig;
    ViewGroup mContainer;
    int mContainerId;
    boolean mDeferStart;
    boolean mDetached;
    private Transition mEnterTransition;
    SharedElementCallback mEnterTransitionCallback;
    private Transition mExitTransition;
    SharedElementCallback mExitTransitionCallback;
    int mFragmentId;
    FragmentManagerImpl mFragmentManager;
    boolean mFromLayout;
    boolean mHasMenu;
    boolean mHidden;
    FragmentHostCallback mHost;
    boolean mInLayout;
    int mIndex;
    LoaderManagerImpl mLoaderManager;
    boolean mLoadersStarted;
    boolean mMenuVisible;
    int mNextAnim;
    Fragment mParentFragment;
    private Transition mReenterTransition;
    boolean mRemoving;
    boolean mRestored;
    boolean mRetainInstance;
    boolean mRetaining;
    private Transition mReturnTransition;
    Bundle mSavedFragmentState;
    SparseArray<Parcelable> mSavedViewState;
    private Transition mSharedElementEnterTransition;
    private Transition mSharedElementReturnTransition;
    int mState;
    int mStateAfterAnimating;
    String mTag;
    Fragment mTarget;
    int mTargetIndex;
    int mTargetRequestCode;
    boolean mUserVisibleHint;
    View mView;
    String mWho;

    public static class InstantiationException extends AndroidRuntimeException {
        public InstantiationException(String msg, Exception cause) {
            super(msg, cause);
        }
    }

    public static class SavedState implements Parcelable {
        public static final ClassLoaderCreator<SavedState> CREATOR = null;
        final Bundle mState;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.app.Fragment.SavedState.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.app.Fragment.SavedState.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.app.Fragment.SavedState.<clinit>():void");
        }

        SavedState(Bundle state) {
            this.mState = state;
        }

        SavedState(Parcel in, ClassLoader loader) {
            this.mState = in.readBundle();
            if (loader != null && this.mState != null) {
                this.mState.setClassLoader(loader);
            }
        }

        public int describeContents() {
            return Fragment.INITIALIZING;
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeBundle(this.mState);
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.app.Fragment.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.app.Fragment.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.app.Fragment.<clinit>():void");
    }

    public Fragment() {
        this.mState = INITIALIZING;
        this.mIndex = INVALID_STATE;
        this.mTargetIndex = INVALID_STATE;
        this.mMenuVisible = true;
        this.mUserVisibleHint = true;
        this.mEnterTransition = null;
        this.mReturnTransition = USE_DEFAULT_TRANSITION;
        this.mExitTransition = null;
        this.mReenterTransition = USE_DEFAULT_TRANSITION;
        this.mSharedElementEnterTransition = null;
        this.mSharedElementReturnTransition = USE_DEFAULT_TRANSITION;
        this.mEnterTransitionCallback = SharedElementCallback.NULL_CALLBACK;
        this.mExitTransitionCallback = SharedElementCallback.NULL_CALLBACK;
    }

    public static Fragment instantiate(Context context, String fname) {
        return instantiate(context, fname, null);
    }

    public static Fragment instantiate(Context context, String fname, Bundle args) {
        try {
            Class<?> clazz = (Class) sClassMap.get(fname);
            if (clazz == null) {
                clazz = context.getClassLoader().loadClass(fname);
                if (Fragment.class.isAssignableFrom(clazz)) {
                    sClassMap.put(fname, clazz);
                } else {
                    throw new InstantiationException("Trying to instantiate a class " + fname + " that is not a Fragment", new ClassCastException());
                }
            }
            Fragment f = (Fragment) clazz.newInstance();
            if (args != null) {
                args.setClassLoader(f.getClass().getClassLoader());
                f.mArguments = args;
            }
            return f;
        } catch (ClassNotFoundException e) {
            throw new InstantiationException("Unable to instantiate fragment " + fname + ": make sure class name exists, is public, and has an" + " empty constructor that is public", e);
        } catch (InstantiationException e2) {
            throw new InstantiationException("Unable to instantiate fragment " + fname + ": make sure class name exists, is public, and has an" + " empty constructor that is public", e2);
        } catch (IllegalAccessException e3) {
            throw new InstantiationException("Unable to instantiate fragment " + fname + ": make sure class name exists, is public, and has an" + " empty constructor that is public", e3);
        }
    }

    final void restoreViewState(Bundle savedInstanceState) {
        if (this.mSavedViewState != null) {
            this.mView.restoreHierarchyState(this.mSavedViewState);
            this.mSavedViewState = null;
        }
        this.mCalled = false;
        Trace.traceBegin(64, "Fragment.onViewStateRestored");
        onViewStateRestored(savedInstanceState);
        Trace.traceEnd(64);
        if (!this.mCalled) {
            throw new SuperNotCalledException("Fragment " + this + " did not call through to super.onViewStateRestored()");
        }
    }

    final void setIndex(int index, Fragment parent) {
        this.mIndex = index;
        if (parent != null) {
            this.mWho = parent.mWho + ":" + this.mIndex;
        } else {
            this.mWho = "android:fragment:" + this.mIndex;
        }
    }

    final boolean isInBackStack() {
        return this.mBackStackNesting > 0;
    }

    public final boolean equals(Object o) {
        return super.equals(o);
    }

    public final int hashCode() {
        return super.hashCode();
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(KeymasterDefs.KM_ALGORITHM_HMAC);
        DebugUtils.buildShortClassTag(this, sb);
        if (this.mIndex >= 0) {
            sb.append(" #");
            sb.append(this.mIndex);
        }
        if (this.mFragmentId != 0) {
            sb.append(" id=0x");
            sb.append(Integer.toHexString(this.mFragmentId));
        }
        if (this.mTag != null) {
            sb.append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
            sb.append(this.mTag);
        }
        sb.append('}');
        return sb.toString();
    }

    public final int getId() {
        return this.mFragmentId;
    }

    public final String getTag() {
        return this.mTag;
    }

    public void setArguments(Bundle args) {
        if (this.mIndex >= 0) {
            throw new IllegalStateException("Fragment already active");
        }
        this.mArguments = args;
    }

    public final Bundle getArguments() {
        return this.mArguments;
    }

    public void setInitialSavedState(SavedState state) {
        Bundle bundle = null;
        if (this.mIndex >= 0) {
            throw new IllegalStateException("Fragment already active");
        }
        if (!(state == null || state.mState == null)) {
            bundle = state.mState;
        }
        this.mSavedFragmentState = bundle;
    }

    public void setTargetFragment(Fragment fragment, int requestCode) {
        this.mTarget = fragment;
        this.mTargetRequestCode = requestCode;
    }

    public final Fragment getTargetFragment() {
        return this.mTarget;
    }

    public final int getTargetRequestCode() {
        return this.mTargetRequestCode;
    }

    public Context getContext() {
        return this.mHost == null ? null : this.mHost.getContext();
    }

    public final Activity getActivity() {
        return this.mHost == null ? null : this.mHost.getActivity();
    }

    public final Object getHost() {
        return this.mHost == null ? null : this.mHost.onGetHost();
    }

    public final Resources getResources() {
        if (this.mHost != null) {
            return this.mHost.getContext().getResources();
        }
        throw new IllegalStateException("Fragment " + this + " not attached to Activity");
    }

    public final CharSequence getText(int resId) {
        return getResources().getText(resId);
    }

    public final String getString(int resId) {
        return getResources().getString(resId);
    }

    public final String getString(int resId, Object... formatArgs) {
        return getResources().getString(resId, formatArgs);
    }

    public final FragmentManager getFragmentManager() {
        return this.mFragmentManager;
    }

    public final FragmentManager getChildFragmentManager() {
        if (this.mChildFragmentManager == null) {
            instantiateChildFragmentManager();
            if (this.mState >= RESUMED) {
                this.mChildFragmentManager.dispatchResume();
            } else if (this.mState >= STARTED) {
                this.mChildFragmentManager.dispatchStart();
            } else if (this.mState >= ACTIVITY_CREATED) {
                this.mChildFragmentManager.dispatchActivityCreated();
            } else if (this.mState >= CREATED) {
                this.mChildFragmentManager.dispatchCreate();
            }
        }
        return this.mChildFragmentManager;
    }

    public final Fragment getParentFragment() {
        return this.mParentFragment;
    }

    public final boolean isAdded() {
        return this.mHost != null ? this.mAdded : false;
    }

    public final boolean isDetached() {
        return this.mDetached;
    }

    public final boolean isRemoving() {
        return this.mRemoving;
    }

    public final boolean isInLayout() {
        return this.mInLayout;
    }

    public final boolean isResumed() {
        return this.mState >= RESUMED;
    }

    public final boolean isVisible() {
        if (!isAdded() || isHidden() || this.mView == null || this.mView.getWindowToken() == null || this.mView.getVisibility() != 0) {
            return false;
        }
        return true;
    }

    public final boolean isHidden() {
        return this.mHidden;
    }

    public void onHiddenChanged(boolean hidden) {
    }

    public void setRetainInstance(boolean retain) {
        this.mRetainInstance = retain;
    }

    public final boolean getRetainInstance() {
        return this.mRetainInstance;
    }

    public void setHasOptionsMenu(boolean hasMenu) {
        if (this.mHasMenu != hasMenu) {
            this.mHasMenu = hasMenu;
            if (isAdded() && !isHidden()) {
                this.mFragmentManager.invalidateOptionsMenu();
            }
        }
    }

    public void setMenuVisibility(boolean menuVisible) {
        if (this.mMenuVisible != menuVisible) {
            this.mMenuVisible = menuVisible;
            if (this.mHasMenu && isAdded() && !isHidden()) {
                this.mFragmentManager.invalidateOptionsMenu();
            }
        }
    }

    public void setUserVisibleHint(boolean isVisibleToUser) {
        boolean z = false;
        boolean useBrokenAddedCheck = false;
        Context context = getContext();
        if (!(this.mFragmentManager == null || this.mFragmentManager.mHost == null)) {
            context = this.mFragmentManager.mHost.getContext();
        }
        if (context != null) {
            useBrokenAddedCheck = context.getApplicationInfo().targetSdkVersion <= 23;
        }
        boolean performDeferredStart = useBrokenAddedCheck ? (this.mUserVisibleHint || !isVisibleToUser || this.mState >= STARTED) ? false : this.mFragmentManager != null : (this.mUserVisibleHint || !isVisibleToUser || this.mState >= STARTED || this.mFragmentManager == null) ? false : isAdded();
        if (performDeferredStart) {
            this.mFragmentManager.performPendingDeferredStart(this);
        }
        this.mUserVisibleHint = isVisibleToUser;
        if (this.mState < STARTED && !isVisibleToUser) {
            z = true;
        }
        this.mDeferStart = z;
    }

    public boolean getUserVisibleHint() {
        return this.mUserVisibleHint;
    }

    public LoaderManager getLoaderManager() {
        if (this.mLoaderManager != null) {
            return this.mLoaderManager;
        }
        if (this.mHost == null) {
            throw new IllegalStateException("Fragment " + this + " not attached to Activity");
        }
        this.mCheckedForLoaderManager = true;
        this.mLoaderManager = this.mHost.getLoaderManager(this.mWho, this.mLoadersStarted, true);
        return this.mLoaderManager;
    }

    public void startActivity(Intent intent) {
        startActivity(intent, null);
    }

    public void startActivity(Intent intent, Bundle options) {
        if (this.mHost == null) {
            throw new IllegalStateException("Fragment " + this + " not attached to Activity");
        } else if (options != null) {
            this.mHost.onStartActivityFromFragment(this, intent, INVALID_STATE, options);
        } else {
            this.mHost.onStartActivityFromFragment(this, intent, INVALID_STATE, null);
        }
    }

    public void startActivityForResult(Intent intent, int requestCode) {
        startActivityForResult(intent, requestCode, null);
    }

    public void startActivityForResult(Intent intent, int requestCode, Bundle options) {
        if (this.mHost == null) {
            throw new IllegalStateException("Fragment " + this + " not attached to Activity");
        }
        this.mHost.onStartActivityFromFragment(this, intent, requestCode, options);
    }

    public void startIntentSenderForResult(IntentSender intent, int requestCode, Intent fillInIntent, int flagsMask, int flagsValues, int extraFlags, Bundle options) throws SendIntentException {
        if (this.mHost == null) {
            throw new IllegalStateException("Fragment " + this + " not attached to Activity");
        }
        this.mHost.onStartIntentSenderFromFragment(this, intent, requestCode, fillInIntent, flagsMask, flagsValues, extraFlags, options);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
    }

    public final void requestPermissions(String[] permissions, int requestCode) {
        if (this.mHost == null) {
            throw new IllegalStateException("Fragment " + this + " not attached to Activity");
        }
        this.mHost.onRequestPermissionsFromFragment(this, permissions, requestCode);
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
    }

    public boolean shouldShowRequestPermissionRationale(String permission) {
        if (this.mHost != null) {
            return this.mHost.getContext().getPackageManager().shouldShowRequestPermissionRationale(permission);
        }
        return false;
    }

    public LayoutInflater getLayoutInflater(Bundle savedInstanceState) {
        LayoutInflater result = this.mHost.onGetLayoutInflater();
        if (this.mHost.onUseFragmentManagerInflaterFactory()) {
            getChildFragmentManager();
            result.setPrivateFactory(this.mChildFragmentManager.getLayoutInflaterFactory());
        }
        return result;
    }

    @Deprecated
    public void onInflate(AttributeSet attrs, Bundle savedInstanceState) {
        this.mCalled = true;
    }

    public void onInflate(Context context, AttributeSet attrs, Bundle savedInstanceState) {
        Activity hostActivity = null;
        onInflate(attrs, savedInstanceState);
        this.mCalled = true;
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.Fragment);
        this.mEnterTransition = loadTransition(context, a, this.mEnterTransition, null, STARTED);
        this.mReturnTransition = loadTransition(context, a, this.mReturnTransition, USE_DEFAULT_TRANSITION, 6);
        this.mExitTransition = loadTransition(context, a, this.mExitTransition, null, STOPPED);
        this.mReenterTransition = loadTransition(context, a, this.mReenterTransition, USE_DEFAULT_TRANSITION, 8);
        this.mSharedElementEnterTransition = loadTransition(context, a, this.mSharedElementEnterTransition, null, RESUMED);
        this.mSharedElementReturnTransition = loadTransition(context, a, this.mSharedElementReturnTransition, USE_DEFAULT_TRANSITION, 7);
        if (this.mAllowEnterTransitionOverlap == null) {
            this.mAllowEnterTransitionOverlap = Boolean.valueOf(a.getBoolean(9, true));
        }
        if (this.mAllowReturnTransitionOverlap == null) {
            this.mAllowReturnTransitionOverlap = Boolean.valueOf(a.getBoolean(10, true));
        }
        a.recycle();
        if (this.mHost != null) {
            hostActivity = this.mHost.getActivity();
        }
        if (hostActivity != null) {
            this.mCalled = false;
            onInflate(hostActivity, attrs, savedInstanceState);
        }
    }

    @Deprecated
    public void onInflate(Activity activity, AttributeSet attrs, Bundle savedInstanceState) {
        this.mCalled = true;
    }

    public void onAttachFragment(Fragment childFragment) {
    }

    public void onAttach(Context context) {
        Activity hostActivity = null;
        this.mCalled = true;
        if (this.mHost != null) {
            hostActivity = this.mHost.getActivity();
        }
        if (hostActivity != null) {
            this.mCalled = false;
            onAttach(hostActivity);
        }
    }

    @Deprecated
    public void onAttach(Activity activity) {
        this.mCalled = true;
    }

    public Animator onCreateAnimator(int transit, boolean enter, int nextAnim) {
        return null;
    }

    public void onCreate(Bundle savedInstanceState) {
        this.mCalled = true;
        Context context = getContext();
        if ((context != null ? context.getApplicationInfo().targetSdkVersion : INITIALIZING) >= 24) {
            restoreChildFragmentState(savedInstanceState, true);
            if (this.mChildFragmentManager != null && !this.mChildFragmentManager.isStateAtLeast(CREATED)) {
                this.mChildFragmentManager.dispatchCreate();
            }
        }
    }

    void restoreChildFragmentState(Bundle savedInstanceState, boolean provideNonConfig) {
        if (savedInstanceState != null) {
            Parcelable p = savedInstanceState.getParcelable("android:fragments");
            if (p != null) {
                FragmentManagerNonConfig fragmentManagerNonConfig;
                if (this.mChildFragmentManager == null) {
                    instantiateChildFragmentManager();
                }
                FragmentManagerImpl fragmentManagerImpl = this.mChildFragmentManager;
                if (provideNonConfig) {
                    fragmentManagerNonConfig = this.mChildNonConfig;
                } else {
                    fragmentManagerNonConfig = null;
                }
                fragmentManagerImpl.restoreAllState(p, fragmentManagerNonConfig);
                this.mChildNonConfig = null;
                this.mChildFragmentManager.dispatchCreate();
            }
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return null;
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
    }

    public View getView() {
        return this.mView;
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        this.mCalled = true;
    }

    public void onViewStateRestored(Bundle savedInstanceState) {
        this.mCalled = true;
    }

    public void onStart() {
        this.mCalled = true;
        if (!this.mLoadersStarted) {
            this.mLoadersStarted = true;
            if (!this.mCheckedForLoaderManager) {
                this.mCheckedForLoaderManager = true;
                this.mLoaderManager = this.mHost.getLoaderManager(this.mWho, this.mLoadersStarted, false);
            }
            if (this.mLoaderManager != null) {
                this.mLoaderManager.doStart();
            }
        }
    }

    public void onResume() {
        this.mCalled = true;
    }

    public void onSaveInstanceState(Bundle outState) {
    }

    public void onMultiWindowModeChanged(boolean isInMultiWindowMode) {
    }

    public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode) {
    }

    public void onConfigurationChanged(Configuration newConfig) {
        this.mCalled = true;
    }

    public void onPause() {
        this.mCalled = true;
    }

    public void onStop() {
        this.mCalled = true;
    }

    public void onLowMemory() {
        this.mCalled = true;
    }

    public void onTrimMemory(int level) {
        this.mCalled = true;
    }

    public void onDestroyView() {
        this.mCalled = true;
    }

    public void onDestroy() {
        this.mCalled = true;
        if (!this.mCheckedForLoaderManager) {
            this.mCheckedForLoaderManager = true;
            this.mLoaderManager = this.mHost.getLoaderManager(this.mWho, this.mLoadersStarted, false);
        }
        if (this.mLoaderManager != null) {
            this.mLoaderManager.doDestroy();
        }
    }

    void initState() {
        this.mIndex = INVALID_STATE;
        this.mWho = null;
        this.mAdded = false;
        this.mRemoving = false;
        this.mFromLayout = false;
        this.mInLayout = false;
        this.mRestored = false;
        this.mBackStackNesting = INITIALIZING;
        this.mFragmentManager = null;
        this.mChildFragmentManager = null;
        this.mHost = null;
        this.mFragmentId = INITIALIZING;
        this.mContainerId = INITIALIZING;
        this.mTag = null;
        this.mHidden = false;
        this.mDetached = false;
        this.mRetaining = false;
        this.mLoaderManager = null;
        this.mLoadersStarted = false;
        this.mCheckedForLoaderManager = false;
    }

    public void onDetach() {
        this.mCalled = true;
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    }

    public void onPrepareOptionsMenu(Menu menu) {
    }

    public void onDestroyOptionsMenu() {
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        return false;
    }

    public void onOptionsMenuClosed(Menu menu) {
    }

    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        getActivity().onCreateContextMenu(menu, v, menuInfo);
    }

    public void registerForContextMenu(View view) {
        view.setOnCreateContextMenuListener(this);
    }

    public void unregisterForContextMenu(View view) {
        view.setOnCreateContextMenuListener(null);
    }

    public boolean onContextItemSelected(MenuItem item) {
        return false;
    }

    public void setEnterSharedElementCallback(SharedElementCallback callback) {
        if (callback == null) {
            callback = SharedElementCallback.NULL_CALLBACK;
        }
        this.mEnterTransitionCallback = callback;
    }

    public void setEnterSharedElementTransitionCallback(SharedElementCallback callback) {
        setEnterSharedElementCallback(callback);
    }

    public void setExitSharedElementCallback(SharedElementCallback callback) {
        if (callback == null) {
            callback = SharedElementCallback.NULL_CALLBACK;
        }
        this.mExitTransitionCallback = callback;
    }

    public void setExitSharedElementTransitionCallback(SharedElementCallback callback) {
        setExitSharedElementCallback(callback);
    }

    public void setEnterTransition(Transition transition) {
        this.mEnterTransition = transition;
    }

    public Transition getEnterTransition() {
        return this.mEnterTransition;
    }

    public void setReturnTransition(Transition transition) {
        this.mReturnTransition = transition;
    }

    public Transition getReturnTransition() {
        if (this.mReturnTransition == USE_DEFAULT_TRANSITION) {
            return getEnterTransition();
        }
        return this.mReturnTransition;
    }

    public void setExitTransition(Transition transition) {
        this.mExitTransition = transition;
    }

    public Transition getExitTransition() {
        return this.mExitTransition;
    }

    public void setReenterTransition(Transition transition) {
        this.mReenterTransition = transition;
    }

    public Transition getReenterTransition() {
        if (this.mReenterTransition == USE_DEFAULT_TRANSITION) {
            return getExitTransition();
        }
        return this.mReenterTransition;
    }

    public void setSharedElementEnterTransition(Transition transition) {
        this.mSharedElementEnterTransition = transition;
    }

    public Transition getSharedElementEnterTransition() {
        return this.mSharedElementEnterTransition;
    }

    public void setSharedElementReturnTransition(Transition transition) {
        this.mSharedElementReturnTransition = transition;
    }

    public Transition getSharedElementReturnTransition() {
        return this.mSharedElementReturnTransition == USE_DEFAULT_TRANSITION ? getSharedElementEnterTransition() : this.mSharedElementReturnTransition;
    }

    public void setAllowEnterTransitionOverlap(boolean allow) {
        this.mAllowEnterTransitionOverlap = Boolean.valueOf(allow);
    }

    public boolean getAllowEnterTransitionOverlap() {
        return this.mAllowEnterTransitionOverlap == null ? true : this.mAllowEnterTransitionOverlap.booleanValue();
    }

    public void setAllowReturnTransitionOverlap(boolean allow) {
        this.mAllowReturnTransitionOverlap = Boolean.valueOf(allow);
    }

    public boolean getAllowReturnTransitionOverlap() {
        return this.mAllowReturnTransitionOverlap == null ? true : this.mAllowReturnTransitionOverlap.booleanValue();
    }

    public void dump(String prefix, FileDescriptor fd, PrintWriter writer, String[] args) {
        writer.print(prefix);
        writer.print("mFragmentId=#");
        writer.print(Integer.toHexString(this.mFragmentId));
        writer.print(" mContainerId=#");
        writer.print(Integer.toHexString(this.mContainerId));
        writer.print(" mTag=");
        writer.println(this.mTag);
        writer.print(prefix);
        writer.print("mState=");
        writer.print(this.mState);
        writer.print(" mIndex=");
        writer.print(this.mIndex);
        writer.print(" mWho=");
        writer.print(this.mWho);
        writer.print(" mBackStackNesting=");
        writer.println(this.mBackStackNesting);
        writer.print(prefix);
        writer.print("mAdded=");
        writer.print(this.mAdded);
        writer.print(" mRemoving=");
        writer.print(this.mRemoving);
        writer.print(" mFromLayout=");
        writer.print(this.mFromLayout);
        writer.print(" mInLayout=");
        writer.println(this.mInLayout);
        writer.print(prefix);
        writer.print("mHidden=");
        writer.print(this.mHidden);
        writer.print(" mDetached=");
        writer.print(this.mDetached);
        writer.print(" mMenuVisible=");
        writer.print(this.mMenuVisible);
        writer.print(" mHasMenu=");
        writer.println(this.mHasMenu);
        writer.print(prefix);
        writer.print("mRetainInstance=");
        writer.print(this.mRetainInstance);
        writer.print(" mRetaining=");
        writer.print(this.mRetaining);
        writer.print(" mUserVisibleHint=");
        writer.println(this.mUserVisibleHint);
        if (this.mFragmentManager != null) {
            writer.print(prefix);
            writer.print("mFragmentManager=");
            writer.println(this.mFragmentManager);
        }
        if (this.mHost != null) {
            writer.print(prefix);
            writer.print("mHost=");
            writer.println(this.mHost);
        }
        if (this.mParentFragment != null) {
            writer.print(prefix);
            writer.print("mParentFragment=");
            writer.println(this.mParentFragment);
        }
        if (this.mArguments != null) {
            writer.print(prefix);
            writer.print("mArguments=");
            writer.println(this.mArguments);
        }
        if (this.mSavedFragmentState != null) {
            writer.print(prefix);
            writer.print("mSavedFragmentState=");
            writer.println(this.mSavedFragmentState);
        }
        if (this.mSavedViewState != null) {
            writer.print(prefix);
            writer.print("mSavedViewState=");
        }
        if (this.mTarget != null) {
            writer.print(prefix);
            writer.print("mTarget=");
            writer.print(this.mTarget);
            writer.print(" mTargetRequestCode=");
            writer.println(this.mTargetRequestCode);
        }
        if (this.mNextAnim != 0) {
            writer.print(prefix);
            writer.print("mNextAnim=");
            writer.println(this.mNextAnim);
        }
        if (this.mContainer != null) {
            writer.print(prefix);
            writer.print("mContainer=");
            writer.println(this.mContainer);
        }
        if (this.mView != null) {
            writer.print(prefix);
            writer.print("mView=");
            writer.println(this.mView);
        }
        if (this.mAnimatingAway != null) {
            writer.print(prefix);
            writer.print("mAnimatingAway=");
            writer.println(this.mAnimatingAway);
            writer.print(prefix);
            writer.print("mStateAfterAnimating=");
            writer.println(this.mStateAfterAnimating);
        }
        if (this.mLoaderManager != null) {
            writer.print(prefix);
            writer.println("Loader Manager:");
            this.mLoaderManager.dump(prefix + "  ", fd, writer, args);
        }
        if (this.mChildFragmentManager != null) {
            writer.print(prefix);
            writer.println("Child " + this.mChildFragmentManager + ":");
            this.mChildFragmentManager.dump(prefix + "  ", fd, writer, args);
        }
    }

    Fragment findFragmentByWho(String who) {
        if (who.equals(this.mWho)) {
            return this;
        }
        if (this.mChildFragmentManager != null) {
            return this.mChildFragmentManager.findFragmentByWho(who);
        }
        return null;
    }

    void instantiateChildFragmentManager() {
        this.mChildFragmentManager = new FragmentManagerImpl();
        this.mChildFragmentManager.attachController(this.mHost, new FragmentContainer() {
            public View onFindViewById(int id) {
                if (Fragment.this.mView != null) {
                    return Fragment.this.mView.findViewById(id);
                }
                throw new IllegalStateException("Fragment does not have a view");
            }

            public boolean onHasView() {
                return Fragment.this.mView != null;
            }
        }, this);
    }

    void performCreate(Bundle savedInstanceState) {
        if (this.mChildFragmentManager != null) {
            this.mChildFragmentManager.noteStateNotSaved();
        }
        this.mState = CREATED;
        this.mCalled = false;
        Trace.traceBegin(64, "Fragment.onCreate");
        onCreate(savedInstanceState);
        Trace.traceEnd(64);
        if (this.mCalled) {
            int version;
            Context context = getContext();
            if (context != null) {
                version = context.getApplicationInfo().targetSdkVersion;
            } else {
                version = INITIALIZING;
            }
            if (version < 24) {
                restoreChildFragmentState(savedInstanceState, false);
                return;
            }
            return;
        }
        throw new SuperNotCalledException("Fragment " + this + " did not call through to super.onCreate()");
    }

    View performCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (this.mChildFragmentManager != null) {
            this.mChildFragmentManager.noteStateNotSaved();
        }
        Trace.traceBegin(64, "Fragment.onCreateView");
        View view = onCreateView(inflater, container, savedInstanceState);
        Trace.traceEnd(64);
        return view;
    }

    void performActivityCreated(Bundle savedInstanceState) {
        if (this.mChildFragmentManager != null) {
            this.mChildFragmentManager.noteStateNotSaved();
        }
        this.mState = ACTIVITY_CREATED;
        this.mCalled = false;
        Trace.traceBegin(64, "Fragment.onActivityCreated");
        onActivityCreated(savedInstanceState);
        Trace.traceEnd(64);
        if (!this.mCalled) {
            throw new SuperNotCalledException("Fragment " + this + " did not call through to super.onActivityCreated()");
        } else if (this.mChildFragmentManager != null) {
            this.mChildFragmentManager.dispatchActivityCreated();
        }
    }

    void performStart() {
        if (this.mChildFragmentManager != null) {
            this.mChildFragmentManager.noteStateNotSaved();
            this.mChildFragmentManager.execPendingActions();
        }
        this.mState = STARTED;
        this.mCalled = false;
        Trace.traceBegin(64, "Fragment.onStart");
        onStart();
        Trace.traceEnd(64);
        if (this.mCalled) {
            if (this.mChildFragmentManager != null) {
                this.mChildFragmentManager.dispatchStart();
            }
            if (this.mLoaderManager != null) {
                this.mLoaderManager.doReportStart();
                return;
            }
            return;
        }
        throw new SuperNotCalledException("Fragment " + this + " did not call through to super.onStart()");
    }

    void performResume() {
        if (this.mChildFragmentManager != null) {
            this.mChildFragmentManager.noteStateNotSaved();
            this.mChildFragmentManager.execPendingActions();
        }
        this.mState = RESUMED;
        this.mCalled = false;
        Trace.traceBegin(64, "Fragment.onResume");
        onResume();
        Trace.traceEnd(64);
        if (!this.mCalled) {
            throw new SuperNotCalledException("Fragment " + this + " did not call through to super.onResume()");
        } else if (this.mChildFragmentManager != null) {
            this.mChildFragmentManager.dispatchResume();
            this.mChildFragmentManager.execPendingActions();
        }
    }

    void performMultiWindowModeChanged(boolean isInMultiWindowMode) {
        onMultiWindowModeChanged(isInMultiWindowMode);
        if (this.mChildFragmentManager != null) {
            this.mChildFragmentManager.dispatchMultiWindowModeChanged(isInMultiWindowMode);
        }
    }

    void performPictureInPictureModeChanged(boolean isInPictureInPictureMode) {
        onPictureInPictureModeChanged(isInPictureInPictureMode);
        if (this.mChildFragmentManager != null) {
            this.mChildFragmentManager.dispatchPictureInPictureModeChanged(isInPictureInPictureMode);
        }
    }

    void performConfigurationChanged(Configuration newConfig) {
        onConfigurationChanged(newConfig);
        if (this.mChildFragmentManager != null) {
            this.mChildFragmentManager.dispatchConfigurationChanged(newConfig);
        }
    }

    void performLowMemory() {
        onLowMemory();
        if (this.mChildFragmentManager != null) {
            this.mChildFragmentManager.dispatchLowMemory();
        }
    }

    void performTrimMemory(int level) {
        onTrimMemory(level);
        if (this.mChildFragmentManager != null) {
            this.mChildFragmentManager.dispatchTrimMemory(level);
        }
    }

    boolean performCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        boolean show = false;
        if (this.mHidden) {
            return false;
        }
        if (this.mHasMenu && this.mMenuVisible) {
            show = true;
            onCreateOptionsMenu(menu, inflater);
        }
        if (this.mChildFragmentManager != null) {
            return show | this.mChildFragmentManager.dispatchCreateOptionsMenu(menu, inflater);
        }
        return show;
    }

    boolean performPrepareOptionsMenu(Menu menu) {
        boolean show = false;
        if (this.mHidden) {
            return false;
        }
        if (this.mHasMenu && this.mMenuVisible) {
            show = true;
            onPrepareOptionsMenu(menu);
        }
        if (this.mChildFragmentManager != null) {
            return show | this.mChildFragmentManager.dispatchPrepareOptionsMenu(menu);
        }
        return show;
    }

    boolean performOptionsItemSelected(MenuItem item) {
        if (!this.mHidden) {
            if (this.mHasMenu && this.mMenuVisible && onOptionsItemSelected(item)) {
                return true;
            }
            if (this.mChildFragmentManager != null && this.mChildFragmentManager.dispatchOptionsItemSelected(item)) {
                return true;
            }
        }
        return false;
    }

    boolean performContextItemSelected(MenuItem item) {
        if (!this.mHidden) {
            if (onContextItemSelected(item)) {
                return true;
            }
            if (this.mChildFragmentManager != null && this.mChildFragmentManager.dispatchContextItemSelected(item)) {
                return true;
            }
        }
        return false;
    }

    void performOptionsMenuClosed(Menu menu) {
        if (!this.mHidden) {
            if (this.mHasMenu && this.mMenuVisible) {
                onOptionsMenuClosed(menu);
            }
            if (this.mChildFragmentManager != null) {
                this.mChildFragmentManager.dispatchOptionsMenuClosed(menu);
            }
        }
    }

    void performSaveInstanceState(Bundle outState) {
        onSaveInstanceState(outState);
        if (this.mChildFragmentManager != null) {
            Parcelable p = this.mChildFragmentManager.saveAllState();
            if (p != null) {
                outState.putParcelable("android:fragments", p);
            }
        }
    }

    void performPause() {
        if (this.mChildFragmentManager != null) {
            this.mChildFragmentManager.dispatchPause();
        }
        this.mState = STARTED;
        this.mCalled = false;
        Trace.traceBegin(64, "Fragment.onPause");
        onPause();
        Trace.traceEnd(64);
        if (!this.mCalled) {
            throw new SuperNotCalledException("Fragment " + this + " did not call through to super.onPause()");
        }
    }

    void performStop() {
        if (this.mChildFragmentManager != null) {
            this.mChildFragmentManager.dispatchStop();
        }
        this.mState = STOPPED;
        this.mCalled = false;
        Trace.traceBegin(64, "Fragment.onStop");
        onStop();
        Trace.traceEnd(64);
        if (!this.mCalled) {
            throw new SuperNotCalledException("Fragment " + this + " did not call through to super.onStop()");
        } else if (this.mLoadersStarted) {
            this.mLoadersStarted = false;
            if (!this.mCheckedForLoaderManager) {
                this.mCheckedForLoaderManager = true;
                this.mLoaderManager = this.mHost.getLoaderManager(this.mWho, this.mLoadersStarted, false);
            }
            if (this.mLoaderManager == null) {
                return;
            }
            if (this.mHost.getRetainLoaders()) {
                this.mLoaderManager.doRetain();
            } else {
                this.mLoaderManager.doStop();
            }
        }
    }

    void performDestroyView() {
        if (this.mChildFragmentManager != null) {
            this.mChildFragmentManager.dispatchDestroyView();
        }
        this.mState = CREATED;
        this.mCalled = false;
        Trace.traceBegin(64, "Fragment.onDestroyView");
        onDestroyView();
        Trace.traceEnd(64);
        if (!this.mCalled) {
            throw new SuperNotCalledException("Fragment " + this + " did not call through to super.onDestroyView()");
        } else if (this.mLoaderManager != null) {
            this.mLoaderManager.doReportNextStart();
        }
    }

    void performDestroy() {
        if (this.mChildFragmentManager != null) {
            this.mChildFragmentManager.dispatchDestroy();
        }
        this.mState = INITIALIZING;
        this.mCalled = false;
        Trace.traceBegin(64, "Fragment.onDestroy");
        onDestroy();
        Trace.traceEnd(64);
        if (this.mCalled) {
            this.mChildFragmentManager = null;
            return;
        }
        throw new SuperNotCalledException("Fragment " + this + " did not call through to super.onDestroy()");
    }

    void performDetach() {
        this.mCalled = false;
        onDetach();
        if (!this.mCalled) {
            throw new SuperNotCalledException("Fragment " + this + " did not call through to super.onDetach()");
        } else if (this.mChildFragmentManager == null) {
        } else {
            if (this.mRetaining) {
                this.mChildFragmentManager.dispatchDestroy();
                this.mChildFragmentManager = null;
                return;
            }
            throw new IllegalStateException("Child FragmentManager of " + this + " was not " + " destroyed and this fragment is not retaining instance");
        }
    }

    private static Transition loadTransition(Context context, TypedArray typedArray, Transition currentValue, Transition defaultValue, int id) {
        if (currentValue != defaultValue) {
            return currentValue;
        }
        int transitionId = typedArray.getResourceId(id, INITIALIZING);
        Transition transition = defaultValue;
        if (!(transitionId == 0 || transitionId == android.R.transition.no_transition)) {
            transition = TransitionInflater.from(context).inflateTransition(transitionId);
            if ((transition instanceof TransitionSet) && ((TransitionSet) transition).getTransitionCount() == 0) {
                transition = null;
            }
        }
        return transition;
    }
}
