package android.rms.resource;

import android.app.mtm.IMultiTaskProcessObserver;
import android.app.mtm.IMultiTaskProcessObserver.Stub;
import android.app.mtm.MultiTaskManager;
import android.app.mtm.MultiTaskPolicy;
import android.database.IContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.FreezeScreenScene;
import android.os.IBinder;
import android.os.IBinder.DeathRecipient;
import android.os.RemoteException;
import android.rms.HwSysResImpl;
import android.util.Log;
import com.huawei.hsm.permission.StubController;
import huawei.android.pfw.HwPFWStartupPackageList;
import huawei.com.android.internal.widget.HwFragmentContainer;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;

public class ProviderResource extends HwSysResImpl {
    private static final boolean DEBUG = false;
    private static final String TAG = "ProviderResourceManager";
    private static ProviderResource mInstance;
    private int forgroundUid;
    private final ObserverCacheNode mDelayedNode;
    private MultiTaskManager mMultiTaskManager;
    private IMultiTaskProcessObserver mMultiTaskProcessObserver;

    public static final class ObserverCacheNode {
        private String mName;
        private ArrayList<ObserverCacheEntry> mObservers;

        private class ObserverCacheEntry implements DeathRecipient {
            public final boolean mSelfChange;
            public final IContentObserver observer;
            private final Object observersLock;
            public final int pid;
            public final int uid;
            public final Uri uri;
            private final int userHandle;

            public ObserverCacheEntry(IContentObserver o, boolean SelfChange, Object observersLock, int _uid, int _pid, int _userHandle, Uri _uri) {
                this.observersLock = observersLock;
                this.observer = o;
                this.uid = _uid;
                this.pid = _pid;
                this.userHandle = _userHandle;
                this.mSelfChange = SelfChange;
                this.uri = _uri;
                try {
                    this.observer.asBinder().linkToDeath(this, 0);
                } catch (RemoteException e) {
                    binderDied();
                }
            }

            public void binderDied() {
                Log.w(ProviderResource.TAG, "Found dead observer in Caching entry pid is " + this.pid + ", remove it");
                synchronized (this.observersLock) {
                    ObserverCacheNode.this.removeObserverLocked(this.uid, this.pid, this.observer, this.uri);
                }
            }
        }

        private void addObserverLocked(Uri uri, int index, IContentObserver observer, boolean SelfChange, Object observersLock, int uid, int pid, int userHandle) {
            int N = this.mObservers.size();
            IBinder observerBinder = observer.asBinder();
            for (int i = 0; i < N; i++) {
                ObserverCacheEntry entry = (ObserverCacheEntry) this.mObservers.get(i);
                if (entry != null && entry.uid == uid && entry.pid == pid) {
                    if (uri.equals(entry.uri) && entry.observer.asBinder() == observerBinder) {
                        if (Log.HWINFO) {
                            Log.d(ProviderResource.TAG, "Delayed entry is exist:uid=" + entry.uid + "pid=" + entry.pid + "update at " + entry.uri);
                        }
                        return;
                    }
                }
            }
            if (Log.HWINFO) {
                Log.i(ProviderResource.TAG, "add to the cache uid= " + uid + "pid=" + pid + "uri=" + uri);
            }
            this.mObservers.add(new ObserverCacheEntry(observer, SelfChange, observersLock, uid, pid, userHandle, uri));
        }

        public ObserverCacheNode(String name) {
            this.mObservers = new ArrayList();
            this.mName = name;
        }

        protected void dumpCache(FileDescriptor fd, PrintWriter pw) {
            int N = this.mObservers.size();
            pw.println("Provider Cached  observer is : ");
            pw.println();
            for (int i = 0; i < N; i++) {
                ObserverCacheEntry entry = (ObserverCacheEntry) this.mObservers.get(i);
                if (entry != null) {
                    pw.println("Cached notify observer:" + entry.observer + " of " + "update at " + entry.uri + " entry.pid=" + entry.pid + " entry.uid=" + entry.uid);
                }
            }
            pw.println();
        }

        public void addObserverLocked(Uri uri, IContentObserver observer, boolean SelfChange, Object observersLock, int uid, int pid, int userHandle) {
            addObserverLocked(uri, 0, observer, SelfChange, observersLock, uid, pid, userHandle);
        }

        public void removeObserverLocked(int uid, int pid, IContentObserver observer, Uri uri) {
            int size = this.mObservers.size();
            IBinder observerBinder = observer.asBinder();
            for (int i = 0; i < size; i++) {
                ObserverCacheEntry entry = (ObserverCacheEntry) this.mObservers.get(i);
                if (entry != null && entry.uid == uid && entry.pid == pid && entry.uri.equals(uri) && entry.observer.asBinder() == observerBinder) {
                    if (Log.HWINFO) {
                        Log.d(ProviderResource.TAG, "move the delay observer: pid=" + pid + "uid=" + uid + "uri=" + uri);
                    }
                    this.mObservers.remove(i);
                    return;
                }
            }
        }

        public void removeObserverLocked(int uid, int pid) {
            int size = this.mObservers.size();
            int i = 0;
            while (i < size) {
                ObserverCacheEntry entry = (ObserverCacheEntry) this.mObservers.get(i);
                if (entry != null && entry.uid == uid && entry.pid == pid) {
                    if (Log.HWINFO) {
                        Log.d(ProviderResource.TAG, "move the delay observer: pid=" + entry.pid + "uid=" + entry.uid + "uri=" + entry.uri);
                    }
                    this.mObservers.remove(i);
                    i--;
                    size--;
                }
                i++;
            }
        }

        public void collectMyDelayedObserversLocked(int uid, int pid, ArrayList<ObserverDelayCall> calls) {
            int N = this.mObservers.size();
            for (int i = 0; i < N; i++) {
                ObserverCacheEntry entry = (ObserverCacheEntry) this.mObservers.get(i);
                if (entry != null && entry.uid == uid) {
                    if (Log.HWINFO) {
                        Log.d(ProviderResource.TAG, "Find the delayed notify observer:" + entry.observer + " of " + "update at " + entry.uri + " pid=" + pid + " uid=" + uid + " entry.pid=" + entry.pid + " entry.uid=" + entry.uid);
                    }
                    calls.add(new ObserverDelayCall(entry.observer, entry.mSelfChange, entry.pid, entry.uid, entry.uri, entry.userHandle));
                }
            }
        }
    }

    public static final class ObserverDelayCall {
        final IContentObserver mObserver;
        final boolean mSelfChange;
        final int pid;
        final int uid;
        final Uri uri;
        final int userHandle;

        ObserverDelayCall(IContentObserver observer, boolean selfChange, int _pid, int _uid, Uri _uri, int _userHandle) {
            this.mObserver = observer;
            this.mSelfChange = selfChange;
            this.pid = _pid;
            this.uid = _uid;
            this.uri = _uri;
            this.userHandle = _userHandle;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.rms.resource.ProviderResource.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.rms.resource.ProviderResource.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.rms.resource.ProviderResource.<clinit>():void");
    }

    private MultiTaskManager getMultiTaskManager() {
        if (this.mMultiTaskManager == null) {
            this.mMultiTaskManager = MultiTaskManager.getInstance();
        }
        return this.mMultiTaskManager;
    }

    private void notifyForegroundChanged(int pid, int uid) {
        ObserverCacheNode observerCacheNode;
        ArrayList<ObserverDelayCall> calls = new ArrayList();
        synchronized (this.mDelayedNode) {
            this.mDelayedNode.collectMyDelayedObserversLocked(uid, pid, calls);
        }
        int numCalls = calls.size();
        for (int i = 0; i < numCalls; i++) {
            ObserverDelayCall oc = (ObserverDelayCall) calls.get(i);
            try {
                oc.mObserver.onChange(oc.mSelfChange, oc.uri, oc.userHandle);
                if (Log.HWINFO) {
                    Log.d(TAG, "Delayed DataChange Notify is finished pid =" + oc.pid + " observer is " + oc.mObserver + " of " + "update at " + oc.uri);
                }
                observerCacheNode = this.mDelayedNode;
                synchronized (observerCacheNode) {
                }
                this.mDelayedNode.removeObserverLocked(oc.uid, oc.pid, oc.mObserver, oc.uri);
            } catch (RemoteException e) {
                Log.w(TAG, "Found dead observer pid is " + oc.pid + ", remove it");
                observerCacheNode = this.mDelayedNode;
                synchronized (observerCacheNode) {
                }
                this.mDelayedNode.removeObserverLocked(oc.uid, oc.pid, oc.mObserver, oc.uri);
            } catch (Throwable th) {
                synchronized (this.mDelayedNode) {
                }
                this.mDelayedNode.removeObserverLocked(oc.uid, oc.pid, oc.mObserver, oc.uri);
            }
        }
    }

    private void removeObserverFromCache(int pid, int uid) {
        synchronized (this.mDelayedNode) {
            this.mDelayedNode.removeObserverLocked(uid, pid);
        }
    }

    private int observerMultiTaskPolicy(Uri uri, int pid, int uid) {
        MultiTaskPolicy policy = null;
        String dataBaseName = uri.getScheme() + "://" + uri.getAuthority();
        if (this.mMultiTaskManager != null) {
            Bundle args = new Bundle();
            args.putInt(FreezeScreenScene.PID_PARAM, pid);
            args.putInt(StubController.TABLE_COLUM_UID, uid);
            policy = this.mMultiTaskManager.getMultiTaskPolicy(15, dataBaseName, 1, args);
        }
        if (policy != null) {
            return policy.getPolicy();
        }
        return 1;
    }

    public ProviderResource() {
        this.forgroundUid = -1;
        this.mDelayedNode = new ObserverCacheNode("MultiTaskProviderManager");
        this.mMultiTaskManager = null;
        this.mMultiTaskProcessObserver = new Stub() {
            public void onForegroundActivitiesChanged(int pid, int uid, boolean foregroundActivities) {
                if (foregroundActivities) {
                    ProviderResource.this.forgroundUid = uid;
                    ProviderResource.this.notifyForegroundChanged(pid, uid);
                }
            }

            public void onProcessStateChanged(int pid, int uid, int procState) {
            }

            public void onProcessDied(int pid, int uid) {
                ProviderResource.this.removeObserverFromCache(pid, uid);
            }
        };
        getMultiTaskManager();
        if (this.mMultiTaskManager != null) {
            this.mMultiTaskManager.registerObserver(this.mMultiTaskProcessObserver);
            if (Log.HWINFO) {
                Log.d(TAG, "registered MultiTaskProcess");
            }
        }
    }

    private boolean isObserverResourceManaged(Uri _uri, IContentObserver _observer, int _pid, int _uid, boolean _mSelfChange, int _userHandle) {
        if (this.mMultiTaskManager == null) {
            getMultiTaskManager();
            if (this.mMultiTaskManager != null) {
                this.mMultiTaskManager.registerObserver(this.mMultiTaskProcessObserver);
                if (Log.HWINFO) {
                    Log.d(TAG, "registered MultiTaskProcess");
                }
            }
            return DEBUG;
        } else if (_pid < 0 || _uid < 0) {
            return DEBUG;
        } else {
            if (_uid < StubController.MIN_APPLICATION_UID || _uid == this.forgroundUid) {
                return DEBUG;
            }
            switch (observerMultiTaskPolicy(_uri, _pid, _uid)) {
                case HwFragmentContainer.TRANSITION_SLIDE_HORIZONTAL /*2*/:
                    if (Log.HWINFO) {
                        Log.d(TAG, "ForBid policy pid is " + _pid + " uid is " + _uid + " database is " + _uri);
                        break;
                    }
                    break;
                case HwPFWStartupPackageList.STARTUP_LIST_TYPE_MUST_CONTROL_APPS /*4*/:
                    if (!_mSelfChange) {
                        synchronized (this.mDelayedNode) {
                            this.mDelayedNode.addObserverLocked(_uri, _observer, _mSelfChange, this.mDelayedNode, _uid, _pid, _userHandle);
                            break;
                        }
                        if (Log.HWINFO) {
                            Log.d(TAG, "Delay policy  pid is " + _pid + " uid is " + _uid + " database is " + _uri);
                        }
                        return true;
                    }
                    break;
            }
            return DEBUG;
        }
    }

    public static synchronized ProviderResource getInstance() {
        ProviderResource providerResource;
        synchronized (ProviderResource.class) {
            if (mInstance == null) {
                mInstance = new ProviderResource();
                if (Log.HWINFO) {
                    Log.d(TAG, "getInstance create new provider resource");
                }
            }
            providerResource = mInstance;
        }
        return providerResource;
    }

    public int acquire(Uri uri, IContentObserver observer, Bundle args) {
        if (!(args == null || observer == null)) {
            int _pid = args.getInt("PID");
            int _uid = args.getInt("UID");
            int _userHandle = args.getInt("USERHANDLE");
            if (isObserverResourceManaged(uri, observer, _pid, _uid, args.getBoolean("SELFCHANGE"), _userHandle)) {
                return 3;
            }
        }
        return 1;
    }

    public void dump(FileDescriptor fd, PrintWriter pw) {
        synchronized (this.mDelayedNode) {
            this.mDelayedNode.dumpCache(fd, pw);
        }
    }
}
