package android.app;

import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.os.Handler;
import android.os.UserHandle;
import android.util.ArrayMap;
import android.view.LayoutInflater;
import android.view.View;
import java.io.FileDescriptor;
import java.io.PrintWriter;

@Deprecated
public abstract class FragmentHostCallback<E> extends FragmentContainer {
    private final Activity mActivity;
    private ArrayMap<String, LoaderManager> mAllLoaderManagers;
    private boolean mCheckedForLoaderManager;
    final Context mContext;
    final FragmentManagerImpl mFragmentManager;
    private final Handler mHandler;
    private LoaderManagerImpl mLoaderManager;
    private boolean mLoadersStarted;
    private boolean mRetainLoaders;
    final int mWindowAnimations;

    public abstract E onGetHost();

    /* JADX INFO: this call moved to the top of the method (can break code semantics) */
    public FragmentHostCallback(Context context, Handler handler, int windowAnimations) {
        this(context instanceof Activity ? (Activity) context : null, context, chooseHandler(context, handler), windowAnimations);
    }

    FragmentHostCallback(Activity activity) {
        this(activity, activity, activity.mHandler, 0);
    }

    FragmentHostCallback(Activity activity, Context context, Handler handler, int windowAnimations) {
        this.mFragmentManager = new FragmentManagerImpl();
        this.mActivity = activity;
        this.mContext = context;
        this.mHandler = handler;
        this.mWindowAnimations = windowAnimations;
    }

    private static Handler chooseHandler(Context context, Handler handler) {
        if (handler != null || !(context instanceof Activity)) {
            return handler;
        }
        return ((Activity) context).mHandler;
    }

    public void onDump(String prefix, FileDescriptor fd, PrintWriter writer, String[] args) {
    }

    public boolean onShouldSaveFragmentState(Fragment fragment) {
        return true;
    }

    public LayoutInflater onGetLayoutInflater() {
        return (LayoutInflater) this.mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public boolean onUseFragmentManagerInflaterFactory() {
        return false;
    }

    public void onInvalidateOptionsMenu() {
    }

    public void onStartActivityFromFragment(Fragment fragment, Intent intent, int requestCode, Bundle options) {
        if (requestCode == -1) {
            this.mContext.startActivity(intent);
            return;
        }
        throw new IllegalStateException("Starting activity with a requestCode requires a FragmentActivity host");
    }

    public void onStartActivityAsUserFromFragment(Fragment fragment, Intent intent, int requestCode, Bundle options, UserHandle userHandle) {
        if (requestCode == -1) {
            this.mContext.startActivityAsUser(intent, userHandle);
            return;
        }
        throw new IllegalStateException("Starting activity with a requestCode requires a FragmentActivity host");
    }

    public void onStartIntentSenderFromFragment(Fragment fragment, IntentSender intent, int requestCode, Intent fillInIntent, int flagsMask, int flagsValues, int extraFlags, Bundle options) throws IntentSender.SendIntentException {
        if (requestCode == -1) {
            this.mContext.startIntentSender(intent, fillInIntent, flagsMask, flagsValues, extraFlags, options);
        } else {
            throw new IllegalStateException("Starting intent sender with a requestCode requires a FragmentActivity host");
        }
    }

    public void onRequestPermissionsFromFragment(Fragment fragment, String[] permissions, int requestCode) {
    }

    public boolean onHasWindowAnimations() {
        return true;
    }

    public int onGetWindowAnimations() {
        return this.mWindowAnimations;
    }

    public void onAttachFragment(Fragment fragment) {
    }

    public <T extends View> T onFindViewById(int id) {
        return null;
    }

    public boolean onHasView() {
        return true;
    }

    /* access modifiers changed from: package-private */
    public boolean getRetainLoaders() {
        return this.mRetainLoaders;
    }

    /* access modifiers changed from: package-private */
    public Activity getActivity() {
        return this.mActivity;
    }

    /* access modifiers changed from: package-private */
    public Context getContext() {
        return this.mContext;
    }

    /* access modifiers changed from: package-private */
    public Handler getHandler() {
        return this.mHandler;
    }

    /* access modifiers changed from: package-private */
    public FragmentManagerImpl getFragmentManagerImpl() {
        return this.mFragmentManager;
    }

    /* access modifiers changed from: package-private */
    public LoaderManagerImpl getLoaderManagerImpl() {
        if (this.mLoaderManager != null) {
            return this.mLoaderManager;
        }
        this.mCheckedForLoaderManager = true;
        this.mLoaderManager = getLoaderManager("(root)", this.mLoadersStarted, true);
        return this.mLoaderManager;
    }

    /* access modifiers changed from: package-private */
    public void inactivateFragment(String who) {
        if (this.mAllLoaderManagers != null) {
            LoaderManagerImpl lm = (LoaderManagerImpl) this.mAllLoaderManagers.get(who);
            if (lm != null && !lm.mRetaining) {
                lm.doDestroy();
                this.mAllLoaderManagers.remove(who);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void doLoaderStart() {
        if (!this.mLoadersStarted) {
            this.mLoadersStarted = true;
            if (this.mLoaderManager != null) {
                this.mLoaderManager.doStart();
            } else if (!this.mCheckedForLoaderManager) {
                this.mLoaderManager = getLoaderManager("(root)", this.mLoadersStarted, false);
            }
            this.mCheckedForLoaderManager = true;
        }
    }

    /* access modifiers changed from: package-private */
    public void doLoaderStop(boolean retain) {
        this.mRetainLoaders = retain;
        if (this.mLoaderManager != null && this.mLoadersStarted) {
            this.mLoadersStarted = false;
            if (retain) {
                this.mLoaderManager.doRetain();
            } else {
                this.mLoaderManager.doStop();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void doLoaderRetain() {
        if (this.mLoaderManager != null) {
            this.mLoaderManager.doRetain();
        }
    }

    /* access modifiers changed from: package-private */
    public void doLoaderDestroy() {
        if (this.mLoaderManager != null) {
            this.mLoaderManager.doDestroy();
        }
    }

    /* access modifiers changed from: package-private */
    public void reportLoaderStart() {
        if (this.mAllLoaderManagers != null) {
            int N = this.mAllLoaderManagers.size();
            LoaderManagerImpl[] loaders = new LoaderManagerImpl[N];
            for (int i = N - 1; i >= 0; i--) {
                loaders[i] = (LoaderManagerImpl) this.mAllLoaderManagers.valueAt(i);
            }
            for (int i2 = 0; i2 < N; i2++) {
                LoaderManagerImpl lm = loaders[i2];
                lm.finishRetain();
                lm.doReportStart();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public LoaderManagerImpl getLoaderManager(String who, boolean started, boolean create) {
        if (this.mAllLoaderManagers == null) {
            this.mAllLoaderManagers = new ArrayMap<>();
        }
        LoaderManagerImpl lm = (LoaderManagerImpl) this.mAllLoaderManagers.get(who);
        if (lm == null && create) {
            LoaderManagerImpl lm2 = new LoaderManagerImpl(who, this, started);
            this.mAllLoaderManagers.put(who, lm2);
            return lm2;
        } else if (!started || lm == null || lm.mStarted) {
            return lm;
        } else {
            lm.doStart();
            return lm;
        }
    }

    /* access modifiers changed from: package-private */
    public ArrayMap<String, LoaderManager> retainLoaderNonConfig() {
        boolean retainLoaders = false;
        if (this.mAllLoaderManagers != null) {
            int N = this.mAllLoaderManagers.size();
            LoaderManagerImpl[] loaders = new LoaderManagerImpl[N];
            for (int i = N - 1; i >= 0; i--) {
                loaders[i] = (LoaderManagerImpl) this.mAllLoaderManagers.valueAt(i);
            }
            int i2 = getRetainLoaders();
            for (int i3 = 0; i3 < N; i3++) {
                LoaderManagerImpl lm = loaders[i3];
                if (!lm.mRetaining && i2 != 0) {
                    if (!lm.mStarted) {
                        lm.doStart();
                    }
                    lm.doRetain();
                }
                if (lm.mRetaining) {
                    retainLoaders = true;
                } else {
                    lm.doDestroy();
                    this.mAllLoaderManagers.remove(lm.mWho);
                }
            }
        }
        if (retainLoaders) {
            return this.mAllLoaderManagers;
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public void restoreLoaderNonConfig(ArrayMap<String, LoaderManager> loaderManagers) {
        if (loaderManagers != null) {
            int N = loaderManagers.size();
            for (int i = 0; i < N; i++) {
                ((LoaderManagerImpl) loaderManagers.valueAt(i)).updateHostController(this);
            }
        }
        this.mAllLoaderManagers = loaderManagers;
    }

    /* access modifiers changed from: package-private */
    public void dumpLoaders(String prefix, FileDescriptor fd, PrintWriter writer, String[] args) {
        writer.print(prefix);
        writer.print("mLoadersStarted=");
        writer.println(this.mLoadersStarted);
        if (this.mLoaderManager != null) {
            writer.print(prefix);
            writer.print("Loader Manager ");
            writer.print(Integer.toHexString(System.identityHashCode(this.mLoaderManager)));
            writer.println(":");
            LoaderManagerImpl loaderManagerImpl = this.mLoaderManager;
            loaderManagerImpl.dump(prefix + "  ", fd, writer, args);
        }
    }
}
