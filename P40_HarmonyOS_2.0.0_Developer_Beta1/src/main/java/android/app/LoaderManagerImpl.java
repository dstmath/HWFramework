package android.app;

import android.app.LoaderManager;
import android.content.Loader;
import android.os.Bundle;
import android.provider.SettingsStringUtil;
import android.util.DebugUtils;
import android.util.Log;
import android.util.SparseArray;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.lang.reflect.Modifier;

/* access modifiers changed from: package-private */
/* compiled from: LoaderManager */
public class LoaderManagerImpl extends LoaderManager {
    static boolean DEBUG = false;
    static final String TAG = "LoaderManager";
    boolean mCreatingLoader;
    private FragmentHostCallback mHost;
    final SparseArray<LoaderInfo> mInactiveLoaders = new SparseArray<>(0);
    final SparseArray<LoaderInfo> mLoaders = new SparseArray<>(0);
    boolean mRetaining;
    boolean mRetainingStarted;
    boolean mStarted;
    final String mWho;

    /* access modifiers changed from: package-private */
    /* compiled from: LoaderManager */
    public final class LoaderInfo implements Loader.OnLoadCompleteListener<Object>, Loader.OnLoadCanceledListener<Object> {
        final Bundle mArgs;
        LoaderManager.LoaderCallbacks<Object> mCallbacks;
        Object mData;
        boolean mDeliveredData;
        boolean mDestroyed;
        boolean mHaveData;
        final int mId;
        boolean mListenerRegistered;
        Loader<Object> mLoader;
        LoaderInfo mPendingLoader;
        boolean mReportNextStart;
        boolean mRetaining;
        boolean mRetainingStarted;
        boolean mStarted;

        public LoaderInfo(int id, Bundle args, LoaderManager.LoaderCallbacks<Object> callbacks) {
            this.mId = id;
            this.mArgs = args;
            this.mCallbacks = callbacks;
        }

        /* access modifiers changed from: package-private */
        public void start() {
            LoaderManager.LoaderCallbacks<Object> loaderCallbacks;
            if (this.mRetaining && this.mRetainingStarted) {
                this.mStarted = true;
            } else if (!this.mStarted) {
                this.mStarted = true;
                if (LoaderManagerImpl.DEBUG) {
                    Log.v(LoaderManagerImpl.TAG, "  Starting: " + this);
                }
                if (this.mLoader == null && (loaderCallbacks = this.mCallbacks) != null) {
                    this.mLoader = loaderCallbacks.onCreateLoader(this.mId, this.mArgs);
                }
                Loader<Object> loader = this.mLoader;
                if (loader == null) {
                    return;
                }
                if (!loader.getClass().isMemberClass() || Modifier.isStatic(this.mLoader.getClass().getModifiers())) {
                    if (!this.mListenerRegistered) {
                        this.mLoader.registerListener(this.mId, this);
                        this.mLoader.registerOnLoadCanceledListener(this);
                        this.mListenerRegistered = true;
                    }
                    this.mLoader.startLoading();
                    return;
                }
                throw new IllegalArgumentException("Object returned from onCreateLoader must not be a non-static inner member class: " + this.mLoader);
            }
        }

        /* access modifiers changed from: package-private */
        public void retain() {
            if (LoaderManagerImpl.DEBUG) {
                Log.v(LoaderManagerImpl.TAG, "  Retaining: " + this);
            }
            this.mRetaining = true;
            this.mRetainingStarted = this.mStarted;
            this.mStarted = false;
            this.mCallbacks = null;
        }

        /* access modifiers changed from: package-private */
        public void finishRetain() {
            if (this.mRetaining) {
                if (LoaderManagerImpl.DEBUG) {
                    Log.v(LoaderManagerImpl.TAG, "  Finished Retaining: " + this);
                }
                this.mRetaining = false;
                boolean z = this.mStarted;
                if (z != this.mRetainingStarted && !z) {
                    stop();
                }
            }
            if (this.mStarted && this.mHaveData && !this.mReportNextStart) {
                callOnLoadFinished(this.mLoader, this.mData);
            }
        }

        /* access modifiers changed from: package-private */
        public void reportStart() {
            if (this.mStarted && this.mReportNextStart) {
                this.mReportNextStart = false;
                if (this.mHaveData && !this.mRetaining) {
                    callOnLoadFinished(this.mLoader, this.mData);
                }
            }
        }

        /* access modifiers changed from: package-private */
        public void stop() {
            Loader<Object> loader;
            if (LoaderManagerImpl.DEBUG) {
                Log.v(LoaderManagerImpl.TAG, "  Stopping: " + this);
            }
            this.mStarted = false;
            if (!this.mRetaining && (loader = this.mLoader) != null && this.mListenerRegistered) {
                this.mListenerRegistered = false;
                loader.unregisterListener(this);
                this.mLoader.unregisterOnLoadCanceledListener(this);
                this.mLoader.stopLoading();
            }
        }

        /* access modifiers changed from: package-private */
        public boolean cancel() {
            Loader<Object> loader;
            if (LoaderManagerImpl.DEBUG) {
                Log.v(LoaderManagerImpl.TAG, "  Canceling: " + this);
            }
            if (!this.mStarted || (loader = this.mLoader) == null || !this.mListenerRegistered) {
                return false;
            }
            boolean cancelLoadResult = loader.cancelLoad();
            if (!cancelLoadResult) {
                onLoadCanceled(this.mLoader);
            }
            return cancelLoadResult;
        }

        /* access modifiers changed from: package-private */
        public void destroy() {
            if (LoaderManagerImpl.DEBUG) {
                Log.v(LoaderManagerImpl.TAG, "  Destroying: " + this);
            }
            this.mDestroyed = true;
            boolean needReset = this.mDeliveredData;
            this.mDeliveredData = false;
            if (this.mCallbacks != null && this.mLoader != null && this.mHaveData && needReset) {
                if (LoaderManagerImpl.DEBUG) {
                    Log.v(LoaderManagerImpl.TAG, "  Reseting: " + this);
                }
                String lastBecause = null;
                if (LoaderManagerImpl.this.mHost != null) {
                    lastBecause = LoaderManagerImpl.this.mHost.mFragmentManager.mNoTransactionsBecause;
                    LoaderManagerImpl.this.mHost.mFragmentManager.mNoTransactionsBecause = "onLoaderReset";
                }
                try {
                    this.mCallbacks.onLoaderReset(this.mLoader);
                } finally {
                    if (LoaderManagerImpl.this.mHost != null) {
                        LoaderManagerImpl.this.mHost.mFragmentManager.mNoTransactionsBecause = lastBecause;
                    }
                }
            }
            this.mCallbacks = null;
            this.mData = null;
            this.mHaveData = false;
            Loader<Object> loader = this.mLoader;
            if (loader != null) {
                if (this.mListenerRegistered) {
                    this.mListenerRegistered = false;
                    loader.unregisterListener(this);
                    this.mLoader.unregisterOnLoadCanceledListener(this);
                }
                this.mLoader.reset();
            }
            LoaderInfo loaderInfo = this.mPendingLoader;
            if (loaderInfo != null) {
                loaderInfo.destroy();
            }
        }

        @Override // android.content.Loader.OnLoadCanceledListener
        public void onLoadCanceled(Loader<Object> loader) {
            if (LoaderManagerImpl.DEBUG) {
                Log.v(LoaderManagerImpl.TAG, "onLoadCanceled: " + this);
            }
            if (this.mDestroyed) {
                if (LoaderManagerImpl.DEBUG) {
                    Log.v(LoaderManagerImpl.TAG, "  Ignoring load canceled -- destroyed");
                }
            } else if (LoaderManagerImpl.this.mLoaders.get(this.mId) == this) {
                LoaderInfo pending = this.mPendingLoader;
                if (pending != null) {
                    if (LoaderManagerImpl.DEBUG) {
                        Log.v(LoaderManagerImpl.TAG, "  Switching to pending loader: " + pending);
                    }
                    this.mPendingLoader = null;
                    LoaderManagerImpl.this.mLoaders.put(this.mId, null);
                    destroy();
                    LoaderManagerImpl.this.installLoader(pending);
                }
            } else if (LoaderManagerImpl.DEBUG) {
                Log.v(LoaderManagerImpl.TAG, "  Ignoring load canceled -- not active");
            }
        }

        @Override // android.content.Loader.OnLoadCompleteListener
        public void onLoadComplete(Loader<Object> loader, Object data) {
            if (LoaderManagerImpl.DEBUG) {
                Log.v(LoaderManagerImpl.TAG, "onLoadComplete: " + this);
            }
            if (this.mDestroyed) {
                if (LoaderManagerImpl.DEBUG) {
                    Log.v(LoaderManagerImpl.TAG, "  Ignoring load complete -- destroyed");
                }
            } else if (LoaderManagerImpl.this.mLoaders.get(this.mId) == this) {
                LoaderInfo pending = this.mPendingLoader;
                if (pending != null) {
                    if (LoaderManagerImpl.DEBUG) {
                        Log.v(LoaderManagerImpl.TAG, "  Switching to pending loader: " + pending);
                    }
                    this.mPendingLoader = null;
                    LoaderManagerImpl.this.mLoaders.put(this.mId, null);
                    destroy();
                    LoaderManagerImpl.this.installLoader(pending);
                    return;
                }
                if (this.mData != data || !this.mHaveData) {
                    this.mData = data;
                    this.mHaveData = true;
                    if (this.mStarted) {
                        callOnLoadFinished(loader, data);
                    }
                }
                LoaderInfo info = LoaderManagerImpl.this.mInactiveLoaders.get(this.mId);
                if (!(info == null || info == this)) {
                    info.mDeliveredData = false;
                    info.destroy();
                    LoaderManagerImpl.this.mInactiveLoaders.remove(this.mId);
                }
                if (LoaderManagerImpl.this.mHost != null && !LoaderManagerImpl.this.hasRunningLoaders()) {
                    LoaderManagerImpl.this.mHost.mFragmentManager.startPendingDeferredFragments();
                }
            } else if (LoaderManagerImpl.DEBUG) {
                Log.v(LoaderManagerImpl.TAG, "  Ignoring load complete -- not active");
            }
        }

        /* access modifiers changed from: package-private */
        public void callOnLoadFinished(Loader<Object> loader, Object data) {
            if (this.mCallbacks != null) {
                String lastBecause = null;
                if (LoaderManagerImpl.this.mHost != null) {
                    lastBecause = LoaderManagerImpl.this.mHost.mFragmentManager.mNoTransactionsBecause;
                    LoaderManagerImpl.this.mHost.mFragmentManager.mNoTransactionsBecause = "onLoadFinished";
                }
                try {
                    if (LoaderManagerImpl.DEBUG) {
                        Log.v(LoaderManagerImpl.TAG, "  onLoadFinished in " + loader + ": " + loader.dataToString(data));
                    }
                    this.mCallbacks.onLoadFinished(loader, data);
                    this.mDeliveredData = true;
                } finally {
                    if (LoaderManagerImpl.this.mHost != null) {
                        LoaderManagerImpl.this.mHost.mFragmentManager.mNoTransactionsBecause = lastBecause;
                    }
                }
            }
        }

        public String toString() {
            StringBuilder sb = new StringBuilder(64);
            sb.append("LoaderInfo{");
            sb.append(Integer.toHexString(System.identityHashCode(this)));
            sb.append(" #");
            sb.append(this.mId);
            sb.append(" : ");
            DebugUtils.buildShortClassTag(this.mLoader, sb);
            sb.append("}}");
            return sb.toString();
        }

        public void dump(String prefix, FileDescriptor fd, PrintWriter writer, String[] args) {
            writer.print(prefix);
            writer.print("mId=");
            writer.print(this.mId);
            writer.print(" mArgs=");
            writer.println(this.mArgs);
            writer.print(prefix);
            writer.print("mCallbacks=");
            writer.println(this.mCallbacks);
            writer.print(prefix);
            writer.print("mLoader=");
            writer.println(this.mLoader);
            Loader<Object> loader = this.mLoader;
            if (loader != null) {
                loader.dump(prefix + "  ", fd, writer, args);
            }
            if (this.mHaveData || this.mDeliveredData) {
                writer.print(prefix);
                writer.print("mHaveData=");
                writer.print(this.mHaveData);
                writer.print("  mDeliveredData=");
                writer.println(this.mDeliveredData);
                writer.print(prefix);
                writer.print("mData=");
                writer.println(this.mData);
            }
            writer.print(prefix);
            writer.print("mStarted=");
            writer.print(this.mStarted);
            writer.print(" mReportNextStart=");
            writer.print(this.mReportNextStart);
            writer.print(" mDestroyed=");
            writer.println(this.mDestroyed);
            writer.print(prefix);
            writer.print("mRetaining=");
            writer.print(this.mRetaining);
            writer.print(" mRetainingStarted=");
            writer.print(this.mRetainingStarted);
            writer.print(" mListenerRegistered=");
            writer.println(this.mListenerRegistered);
            if (this.mPendingLoader != null) {
                writer.print(prefix);
                writer.println("Pending Loader ");
                writer.print(this.mPendingLoader);
                writer.println(SettingsStringUtil.DELIMITER);
                LoaderInfo loaderInfo = this.mPendingLoader;
                loaderInfo.dump(prefix + "  ", fd, writer, args);
            }
        }
    }

    LoaderManagerImpl(String who, FragmentHostCallback host, boolean started) {
        this.mWho = who;
        this.mHost = host;
        this.mStarted = started;
    }

    /* access modifiers changed from: package-private */
    public void updateHostController(FragmentHostCallback host) {
        this.mHost = host;
    }

    @Override // android.app.LoaderManager
    public FragmentHostCallback getFragmentHostCallback() {
        return this.mHost;
    }

    private LoaderInfo createLoader(int id, Bundle args, LoaderManager.LoaderCallbacks<Object> callback) {
        LoaderInfo info = new LoaderInfo(id, args, callback);
        info.mLoader = callback.onCreateLoader(id, args);
        return info;
    }

    /* JADX INFO: finally extract failed */
    private LoaderInfo createAndInstallLoader(int id, Bundle args, LoaderManager.LoaderCallbacks<Object> callback) {
        try {
            this.mCreatingLoader = true;
            LoaderInfo info = createLoader(id, args, callback);
            installLoader(info);
            this.mCreatingLoader = false;
            return info;
        } catch (Throwable th) {
            this.mCreatingLoader = false;
            throw th;
        }
    }

    /* access modifiers changed from: package-private */
    public void installLoader(LoaderInfo info) {
        this.mLoaders.put(info.mId, info);
        if (this.mStarted) {
            info.start();
        }
    }

    @Override // android.app.LoaderManager
    public <D> Loader<D> initLoader(int id, Bundle args, LoaderManager.LoaderCallbacks<D> callback) {
        if (!this.mCreatingLoader) {
            LoaderInfo info = this.mLoaders.get(id);
            if (DEBUG) {
                Log.v(TAG, "initLoader in " + this + ": args=" + args);
            }
            if (info == null) {
                info = createAndInstallLoader(id, args, callback);
                if (DEBUG) {
                    Log.v(TAG, "  Created new loader " + info);
                }
            } else {
                if (DEBUG) {
                    Log.v(TAG, "  Re-using existing loader " + info);
                }
                info.mCallbacks = callback;
            }
            if (info.mHaveData && this.mStarted) {
                info.callOnLoadFinished(info.mLoader, info.mData);
            }
            return (Loader<D>) info.mLoader;
        }
        throw new IllegalStateException("Called while creating a loader");
    }

    @Override // android.app.LoaderManager
    public <D> Loader<D> restartLoader(int id, Bundle args, LoaderManager.LoaderCallbacks<D> callback) {
        if (!this.mCreatingLoader) {
            LoaderInfo info = this.mLoaders.get(id);
            if (DEBUG) {
                Log.v(TAG, "restartLoader in " + this + ": args=" + args);
            }
            if (info != null) {
                LoaderInfo inactive = this.mInactiveLoaders.get(id);
                if (inactive == null) {
                    if (DEBUG) {
                        Log.v(TAG, "  Making last loader inactive: " + info);
                    }
                    info.mLoader.abandon();
                    this.mInactiveLoaders.put(id, info);
                } else if (info.mHaveData) {
                    if (DEBUG) {
                        Log.v(TAG, "  Removing last inactive loader: " + info);
                    }
                    inactive.mDeliveredData = false;
                    inactive.destroy();
                    info.mLoader.abandon();
                    this.mInactiveLoaders.put(id, info);
                } else if (!info.cancel()) {
                    if (DEBUG) {
                        Log.v(TAG, "  Current loader is stopped; replacing");
                    }
                    this.mLoaders.put(id, null);
                    info.destroy();
                } else {
                    if (DEBUG) {
                        Log.v(TAG, "  Current loader is running; configuring pending loader");
                    }
                    if (info.mPendingLoader != null) {
                        if (DEBUG) {
                            Log.v(TAG, "  Removing pending loader: " + info.mPendingLoader);
                        }
                        info.mPendingLoader.destroy();
                        info.mPendingLoader = null;
                    }
                    if (DEBUG) {
                        Log.v(TAG, "  Enqueuing as new pending loader");
                    }
                    info.mPendingLoader = createLoader(id, args, callback);
                    return (Loader<D>) info.mPendingLoader.mLoader;
                }
            }
            return (Loader<D>) createAndInstallLoader(id, args, callback).mLoader;
        }
        throw new IllegalStateException("Called while creating a loader");
    }

    @Override // android.app.LoaderManager
    public void destroyLoader(int id) {
        if (!this.mCreatingLoader) {
            if (DEBUG) {
                Log.v(TAG, "destroyLoader in " + this + " of " + id);
            }
            int idx = this.mLoaders.indexOfKey(id);
            if (idx >= 0) {
                this.mLoaders.removeAt(idx);
                this.mLoaders.valueAt(idx).destroy();
            }
            int idx2 = this.mInactiveLoaders.indexOfKey(id);
            if (idx2 >= 0) {
                this.mInactiveLoaders.removeAt(idx2);
                this.mInactiveLoaders.valueAt(idx2).destroy();
            }
            if (this.mHost != null && !hasRunningLoaders()) {
                this.mHost.mFragmentManager.startPendingDeferredFragments();
                return;
            }
            return;
        }
        throw new IllegalStateException("Called while creating a loader");
    }

    @Override // android.app.LoaderManager
    public <D> Loader<D> getLoader(int id) {
        if (!this.mCreatingLoader) {
            LoaderInfo loaderInfo = this.mLoaders.get(id);
            if (loaderInfo != null) {
                return loaderInfo.mPendingLoader != null ? (Loader<D>) loaderInfo.mPendingLoader.mLoader : (Loader<D>) loaderInfo.mLoader;
            }
            return null;
        }
        throw new IllegalStateException("Called while creating a loader");
    }

    /* access modifiers changed from: package-private */
    public void doStart() {
        if (DEBUG) {
            Log.v(TAG, "Starting in " + this);
        }
        if (this.mStarted) {
            RuntimeException e = new RuntimeException("here");
            e.fillInStackTrace();
            Log.w(TAG, "Called doStart when already started: " + this, e);
            return;
        }
        this.mStarted = true;
        for (int i = this.mLoaders.size() - 1; i >= 0; i--) {
            this.mLoaders.valueAt(i).start();
        }
    }

    /* access modifiers changed from: package-private */
    public void doStop() {
        if (DEBUG) {
            Log.v(TAG, "Stopping in " + this);
        }
        if (!this.mStarted) {
            RuntimeException e = new RuntimeException("here");
            e.fillInStackTrace();
            Log.w(TAG, "Called doStop when not started: " + this, e);
            return;
        }
        for (int i = this.mLoaders.size() - 1; i >= 0; i--) {
            this.mLoaders.valueAt(i).stop();
        }
        this.mStarted = false;
    }

    /* access modifiers changed from: package-private */
    public void doRetain() {
        if (DEBUG) {
            Log.v(TAG, "Retaining in " + this);
        }
        if (!this.mStarted) {
            RuntimeException e = new RuntimeException("here");
            e.fillInStackTrace();
            Log.w(TAG, "Called doRetain when not started: " + this, e);
            return;
        }
        this.mRetaining = true;
        this.mStarted = false;
        for (int i = this.mLoaders.size() - 1; i >= 0; i--) {
            this.mLoaders.valueAt(i).retain();
        }
    }

    /* access modifiers changed from: package-private */
    public void finishRetain() {
        if (this.mRetaining) {
            if (DEBUG) {
                Log.v(TAG, "Finished Retaining in " + this);
            }
            this.mRetaining = false;
            for (int i = this.mLoaders.size() - 1; i >= 0; i--) {
                this.mLoaders.valueAt(i).finishRetain();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void doReportNextStart() {
        for (int i = this.mLoaders.size() - 1; i >= 0; i--) {
            this.mLoaders.valueAt(i).mReportNextStart = true;
        }
    }

    /* access modifiers changed from: package-private */
    public void doReportStart() {
        for (int i = this.mLoaders.size() - 1; i >= 0; i--) {
            this.mLoaders.valueAt(i).reportStart();
        }
    }

    /* access modifiers changed from: package-private */
    public void doDestroy() {
        if (!this.mRetaining) {
            if (DEBUG) {
                Log.v(TAG, "Destroying Active in " + this);
            }
            for (int i = this.mLoaders.size() - 1; i >= 0; i--) {
                this.mLoaders.valueAt(i).destroy();
            }
            this.mLoaders.clear();
        }
        if (DEBUG) {
            Log.v(TAG, "Destroying Inactive in " + this);
        }
        for (int i2 = this.mInactiveLoaders.size() - 1; i2 >= 0; i2--) {
            this.mInactiveLoaders.valueAt(i2).destroy();
        }
        this.mInactiveLoaders.clear();
        this.mHost = null;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(128);
        sb.append("LoaderManager{");
        sb.append(Integer.toHexString(System.identityHashCode(this)));
        sb.append(" in ");
        DebugUtils.buildShortClassTag(this.mHost, sb);
        sb.append("}}");
        return sb.toString();
    }

    @Override // android.app.LoaderManager
    public void dump(String prefix, FileDescriptor fd, PrintWriter writer, String[] args) {
        if (this.mLoaders.size() > 0) {
            writer.print(prefix);
            writer.println("Active Loaders:");
            String innerPrefix = prefix + "    ";
            for (int i = 0; i < this.mLoaders.size(); i++) {
                LoaderInfo li = this.mLoaders.valueAt(i);
                writer.print(prefix);
                writer.print("  #");
                writer.print(this.mLoaders.keyAt(i));
                writer.print(": ");
                writer.println(li.toString());
                li.dump(innerPrefix, fd, writer, args);
            }
        }
        if (this.mInactiveLoaders.size() > 0) {
            writer.print(prefix);
            writer.println("Inactive Loaders:");
            String innerPrefix2 = prefix + "    ";
            for (int i2 = 0; i2 < this.mInactiveLoaders.size(); i2++) {
                LoaderInfo li2 = this.mInactiveLoaders.valueAt(i2);
                writer.print(prefix);
                writer.print("  #");
                writer.print(this.mInactiveLoaders.keyAt(i2));
                writer.print(": ");
                writer.println(li2.toString());
                li2.dump(innerPrefix2, fd, writer, args);
            }
        }
    }

    public boolean hasRunningLoaders() {
        boolean loadersRunning = false;
        int count = this.mLoaders.size();
        for (int i = 0; i < count; i++) {
            LoaderInfo li = this.mLoaders.valueAt(i);
            loadersRunning |= li.mStarted && !li.mDeliveredData;
        }
        return loadersRunning;
    }
}
