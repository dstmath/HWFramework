package android.widget;

import android.appwidget.AppWidgetHostView;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.Intent.FilterComparison;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.util.Slog;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.util.SparseIntArray;
import android.util.TimedRemoteCaller;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.widget.RemoteViews.OnClickHandler;
import com.android.internal.R;
import com.android.internal.widget.IRemoteViewsAdapterConnection.Stub;
import com.android.internal.widget.IRemoteViewsFactory;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;

public class RemoteViewsAdapter extends BaseAdapter implements Callback {
    private static final String MULTI_USER_PERM = "android.permission.INTERACT_ACROSS_USERS_FULL";
    private static final int REMOTE_VIEWS_CACHE_DURATION = 5000;
    private static final String TAG = "RemoteViewsAdapter";
    private static Handler sCacheRemovalQueue = null;
    private static HandlerThread sCacheRemovalThread = null;
    private static final HashMap<RemoteViewsCacheKey, FixedSizeRemoteViewsCache> sCachedRemoteViewsCaches = null;
    private static final int sDefaultCacheSize = 40;
    private static final int sDefaultLoadingViewHeight = 50;
    private static final int sDefaultMessageType = 0;
    private static final HashMap<RemoteViewsCacheKey, Runnable> sRemoteViewsCacheRemoveRunnables = null;
    private static final int sUnbindServiceDelay = 5000;
    private static final int sUnbindServiceMessageType = 1;
    private final int mAppWidgetId;
    private final FixedSizeRemoteViewsCache mCache;
    private WeakReference<RemoteAdapterConnectionCallback> mCallback;
    private final Context mContext;
    private boolean mDataReady;
    private final Intent mIntent;
    private LayoutInflater mLayoutInflater;
    private Handler mMainQueue;
    private boolean mNotifyDataSetChangedAfterOnServiceConnected;
    private OnClickHandler mRemoteViewsOnClickHandler;
    private RemoteViewsFrameLayoutRefSet mRequestedViews;
    private RemoteViewsAdapterServiceConnection mServiceConnection;
    private int mVisibleWindowLowerBound;
    private int mVisibleWindowUpperBound;
    private Handler mWorkerQueue;
    private HandlerThread mWorkerThread;

    public interface RemoteAdapterConnectionCallback {
        void deferNotifyDataSetChanged();

        boolean onRemoteAdapterConnected();

        void onRemoteAdapterDisconnected();
    }

    /* renamed from: android.widget.RemoteViewsAdapter.1 */
    class AnonymousClass1 implements Runnable {
        final /* synthetic */ RemoteViewsCacheKey val$key;

        AnonymousClass1(RemoteViewsCacheKey val$key) {
            this.val$key = val$key;
        }

        public void run() {
            synchronized (RemoteViewsAdapter.sCachedRemoteViewsCaches) {
                if (RemoteViewsAdapter.sCachedRemoteViewsCaches.containsKey(this.val$key)) {
                    RemoteViewsAdapter.sCachedRemoteViewsCaches.remove(this.val$key);
                }
                if (RemoteViewsAdapter.sRemoteViewsCacheRemoveRunnables.containsKey(this.val$key)) {
                    RemoteViewsAdapter.sRemoteViewsCacheRemoveRunnables.remove(this.val$key);
                }
            }
        }
    }

    /* renamed from: android.widget.RemoteViewsAdapter.4 */
    class AnonymousClass4 implements Runnable {
        final /* synthetic */ int val$position;
        final /* synthetic */ RemoteViews val$rv;

        AnonymousClass4(int val$position, RemoteViews val$rv) {
            this.val$position = val$position;
            this.val$rv = val$rv;
        }

        public void run() {
            RemoteViewsAdapter.this.mRequestedViews.notifyOnRemoteViewsLoaded(this.val$position, this.val$rv);
        }
    }

    private static class FixedSizeRemoteViewsCache {
        private static final String TAG = "FixedSizeRemoteViewsCache";
        private static final float sMaxCountSlackPercent = 0.75f;
        private static final int sMaxMemoryLimitInBytes = 2097152;
        private final SparseArray<RemoteViewsIndexMetaData> mIndexMetaData;
        private final SparseArray<RemoteViews> mIndexRemoteViews;
        private final SparseBooleanArray mIndicesToLoad;
        private int mLastRequestedIndex;
        private final int mMaxCount;
        private final int mMaxCountSlack;
        private final RemoteViewsMetaData mMetaData;
        private int mPreloadLowerBound;
        private int mPreloadUpperBound;
        private final RemoteViewsMetaData mTemporaryMetaData;

        public FixedSizeRemoteViewsCache(int maxCacheSize) {
            this.mMetaData = new RemoteViewsMetaData();
            this.mTemporaryMetaData = new RemoteViewsMetaData();
            this.mIndexMetaData = new SparseArray();
            this.mIndexRemoteViews = new SparseArray();
            this.mIndicesToLoad = new SparseBooleanArray();
            this.mMaxCount = maxCacheSize;
            this.mMaxCountSlack = Math.round(((float) (this.mMaxCount / 2)) * sMaxCountSlackPercent);
            this.mPreloadLowerBound = RemoteViewsAdapter.sDefaultMessageType;
            this.mPreloadUpperBound = -1;
            this.mLastRequestedIndex = -1;
        }

        public void insert(int position, RemoteViews v, long itemId, int[] visibleWindow) {
            if (this.mIndexRemoteViews.size() >= this.mMaxCount) {
                this.mIndexRemoteViews.remove(getFarthestPositionFrom(position, visibleWindow));
            }
            int pruneFromPosition = this.mLastRequestedIndex > -1 ? this.mLastRequestedIndex : position;
            while (getRemoteViewsBitmapMemoryUsage() >= sMaxMemoryLimitInBytes) {
                int trimIndex = getFarthestPositionFrom(pruneFromPosition, visibleWindow);
                if (trimIndex < 0) {
                    break;
                }
                this.mIndexRemoteViews.remove(trimIndex);
            }
            RemoteViewsIndexMetaData metaData = (RemoteViewsIndexMetaData) this.mIndexMetaData.get(position);
            if (metaData != null) {
                metaData.set(v, itemId);
            } else {
                this.mIndexMetaData.put(position, new RemoteViewsIndexMetaData(v, itemId));
            }
            this.mIndexRemoteViews.put(position, v);
        }

        public RemoteViewsMetaData getMetaData() {
            return this.mMetaData;
        }

        public RemoteViewsMetaData getTemporaryMetaData() {
            return this.mTemporaryMetaData;
        }

        public RemoteViews getRemoteViewsAt(int position) {
            return (RemoteViews) this.mIndexRemoteViews.get(position);
        }

        public RemoteViewsIndexMetaData getMetaDataAt(int position) {
            return (RemoteViewsIndexMetaData) this.mIndexMetaData.get(position);
        }

        public void commitTemporaryMetaData() {
            synchronized (this.mTemporaryMetaData) {
                synchronized (this.mMetaData) {
                    this.mMetaData.set(this.mTemporaryMetaData);
                }
            }
        }

        private int getRemoteViewsBitmapMemoryUsage() {
            int mem = RemoteViewsAdapter.sDefaultMessageType;
            for (int i = this.mIndexRemoteViews.size() - 1; i >= 0; i--) {
                RemoteViews v = (RemoteViews) this.mIndexRemoteViews.valueAt(i);
                if (v != null) {
                    mem += v.estimateMemoryUsage();
                }
            }
            return mem;
        }

        private int getFarthestPositionFrom(int pos, int[] visibleWindow) {
            int maxDist = RemoteViewsAdapter.sDefaultMessageType;
            int maxDistIndex = -1;
            int maxDistNotVisible = RemoteViewsAdapter.sDefaultMessageType;
            int maxDistIndexNotVisible = -1;
            for (int i = this.mIndexRemoteViews.size() - 1; i >= 0; i--) {
                int index = this.mIndexRemoteViews.keyAt(i);
                int dist = Math.abs(index - pos);
                if (dist > maxDistNotVisible && Arrays.binarySearch(visibleWindow, index) < 0) {
                    maxDistIndexNotVisible = index;
                    maxDistNotVisible = dist;
                }
                if (dist >= maxDist) {
                    maxDistIndex = index;
                    maxDist = dist;
                }
            }
            if (maxDistIndexNotVisible > -1) {
                return maxDistIndexNotVisible;
            }
            return maxDistIndex;
        }

        public void queueRequestedPositionToLoad(int position) {
            this.mLastRequestedIndex = position;
            synchronized (this.mIndicesToLoad) {
                this.mIndicesToLoad.put(position, true);
            }
        }

        public boolean queuePositionsToBePreloadedFromRequestedPosition(int position) {
            if (this.mPreloadLowerBound <= position && position <= this.mPreloadUpperBound && Math.abs(position - ((this.mPreloadUpperBound + this.mPreloadLowerBound) / 2)) < this.mMaxCountSlack) {
                return false;
            }
            synchronized (this.mMetaData) {
                int count = this.mMetaData.count;
            }
            synchronized (this.mIndicesToLoad) {
                int i;
                for (i = this.mIndicesToLoad.size() - 1; i >= 0; i--) {
                    if (!this.mIndicesToLoad.valueAt(i)) {
                        this.mIndicesToLoad.removeAt(i);
                    }
                }
                int halfMaxCount = this.mMaxCount / 2;
                this.mPreloadLowerBound = position - halfMaxCount;
                this.mPreloadUpperBound = position + halfMaxCount;
                int effectiveLowerBound = Math.max(RemoteViewsAdapter.sDefaultMessageType, this.mPreloadLowerBound);
                int effectiveUpperBound = Math.min(this.mPreloadUpperBound, count - 1);
                i = effectiveLowerBound;
                while (i <= effectiveUpperBound) {
                    if (this.mIndexRemoteViews.indexOfKey(i) < 0 && !this.mIndicesToLoad.get(i)) {
                        this.mIndicesToLoad.put(i, false);
                    }
                    i += RemoteViewsAdapter.sUnbindServiceMessageType;
                }
            }
            return true;
        }

        public int getNextIndexToLoad() {
            synchronized (this.mIndicesToLoad) {
                int index = this.mIndicesToLoad.indexOfValue(true);
                if (index < 0) {
                    index = this.mIndicesToLoad.indexOfValue(false);
                }
                if (index < 0) {
                    return -1;
                }
                int key = this.mIndicesToLoad.keyAt(index);
                this.mIndicesToLoad.removeAt(index);
                return key;
            }
        }

        public boolean containsRemoteViewAt(int position) {
            return this.mIndexRemoteViews.indexOfKey(position) >= 0;
        }

        public boolean containsMetaDataAt(int position) {
            return this.mIndexMetaData.indexOfKey(position) >= 0;
        }

        public void reset() {
            this.mPreloadLowerBound = RemoteViewsAdapter.sDefaultMessageType;
            this.mPreloadUpperBound = -1;
            this.mLastRequestedIndex = -1;
            this.mIndexRemoteViews.clear();
            this.mIndexMetaData.clear();
            synchronized (this.mIndicesToLoad) {
                this.mIndicesToLoad.clear();
            }
        }
    }

    private static class RemoteViewsAdapterServiceConnection extends Stub {
        private WeakReference<RemoteViewsAdapter> mAdapter;
        private boolean mIsConnected;
        private boolean mIsConnecting;
        private IRemoteViewsFactory mRemoteViewsFactory;

        /* renamed from: android.widget.RemoteViewsAdapter.RemoteViewsAdapterServiceConnection.1 */
        class AnonymousClass1 implements Runnable {
            final /* synthetic */ RemoteViewsAdapter val$adapter;

            /* renamed from: android.widget.RemoteViewsAdapter.RemoteViewsAdapterServiceConnection.1.1 */
            class AnonymousClass1 implements Runnable {
                final /* synthetic */ RemoteViewsAdapter val$adapter;

                AnonymousClass1(RemoteViewsAdapter val$adapter) {
                    this.val$adapter = val$adapter;
                }

                public void run() {
                    synchronized (this.val$adapter.mCache) {
                        this.val$adapter.mCache.commitTemporaryMetaData();
                    }
                    RemoteAdapterConnectionCallback callback = (RemoteAdapterConnectionCallback) this.val$adapter.mCallback.get();
                    if (callback != null) {
                        callback.onRemoteAdapterConnected();
                    }
                }
            }

            AnonymousClass1(RemoteViewsAdapter val$adapter) {
                this.val$adapter = val$adapter;
            }

            public void run() {
                if (this.val$adapter.mNotifyDataSetChangedAfterOnServiceConnected) {
                    this.val$adapter.onNotifyDataSetChanged();
                } else {
                    IRemoteViewsFactory factory = this.val$adapter.mServiceConnection.getRemoteViewsFactory();
                    try {
                        if (!factory.isCreated()) {
                            factory.onDataSetChanged();
                        }
                    } catch (RemoteException e) {
                        Log.e(RemoteViewsAdapter.TAG, "Error notifying factory of data set changed in onServiceConnected(): " + e.getMessage());
                        return;
                    } catch (RuntimeException e2) {
                        Log.e(RemoteViewsAdapter.TAG, "Error notifying factory of data set changed in onServiceConnected(): " + e2.getMessage());
                    }
                    this.val$adapter.updateTemporaryMetaData();
                    this.val$adapter.mMainQueue.post(new AnonymousClass1(this.val$adapter));
                }
                this.val$adapter.enqueueDeferredUnbindServiceMessage();
                RemoteViewsAdapterServiceConnection.this.mIsConnected = true;
                RemoteViewsAdapterServiceConnection.this.mIsConnecting = false;
            }
        }

        /* renamed from: android.widget.RemoteViewsAdapter.RemoteViewsAdapterServiceConnection.2 */
        class AnonymousClass2 implements Runnable {
            final /* synthetic */ RemoteViewsAdapter val$adapter;

            AnonymousClass2(RemoteViewsAdapter val$adapter) {
                this.val$adapter = val$adapter;
            }

            public void run() {
                this.val$adapter.mMainQueue.removeMessages(RemoteViewsAdapter.sUnbindServiceMessageType);
                RemoteAdapterConnectionCallback callback = (RemoteAdapterConnectionCallback) this.val$adapter.mCallback.get();
                if (callback != null) {
                    callback.onRemoteAdapterDisconnected();
                }
            }
        }

        public RemoteViewsAdapterServiceConnection(RemoteViewsAdapter adapter) {
            this.mAdapter = new WeakReference(adapter);
        }

        public synchronized void bind(Context context, int appWidgetId, Intent intent) {
            if (!this.mIsConnecting) {
                try {
                    AppWidgetManager mgr = AppWidgetManager.getInstance(context);
                    if (((RemoteViewsAdapter) this.mAdapter.get()) != null) {
                        mgr.bindRemoteViewsService(context.getOpPackageName(), appWidgetId, intent, asBinder());
                    } else {
                        Slog.w(RemoteViewsAdapter.TAG, "bind: adapter was null");
                    }
                    this.mIsConnecting = true;
                } catch (Exception e) {
                    Log.e("RemoteViewsAdapterServiceConnection", "bind(): " + e.getMessage());
                    this.mIsConnecting = false;
                    this.mIsConnected = false;
                }
            }
        }

        public synchronized void unbind(Context context, int appWidgetId, Intent intent) {
            try {
                AppWidgetManager mgr = AppWidgetManager.getInstance(context);
                if (((RemoteViewsAdapter) this.mAdapter.get()) != null) {
                    mgr.unbindRemoteViewsService(context.getOpPackageName(), appWidgetId, intent);
                } else {
                    Slog.w(RemoteViewsAdapter.TAG, "unbind: adapter was null");
                }
                this.mIsConnecting = false;
            } catch (Exception e) {
                Log.e("RemoteViewsAdapterServiceConnection", "unbind(): " + e.getMessage());
                this.mIsConnecting = false;
                this.mIsConnected = false;
            }
        }

        public synchronized void onServiceConnected(IBinder service) {
            this.mRemoteViewsFactory = IRemoteViewsFactory.Stub.asInterface(service);
            RemoteViewsAdapter adapter = (RemoteViewsAdapter) this.mAdapter.get();
            if (adapter != null) {
                adapter.mWorkerQueue.post(new AnonymousClass1(adapter));
            }
        }

        public synchronized void onServiceDisconnected() {
            this.mIsConnected = false;
            this.mIsConnecting = false;
            this.mRemoteViewsFactory = null;
            RemoteViewsAdapter adapter = (RemoteViewsAdapter) this.mAdapter.get();
            if (adapter != null) {
                adapter.mMainQueue.post(new AnonymousClass2(adapter));
            }
        }

        public synchronized IRemoteViewsFactory getRemoteViewsFactory() {
            return this.mRemoteViewsFactory;
        }

        public synchronized boolean isConnected() {
            return this.mIsConnected;
        }
    }

    static class RemoteViewsCacheKey {
        final FilterComparison filter;
        final int widgetId;

        RemoteViewsCacheKey(FilterComparison filter, int widgetId) {
            this.filter = filter;
            this.widgetId = widgetId;
        }

        public boolean equals(Object o) {
            boolean z = false;
            if (!(o instanceof RemoteViewsCacheKey)) {
                return false;
            }
            RemoteViewsCacheKey other = (RemoteViewsCacheKey) o;
            if (other.filter.equals(this.filter) && other.widgetId == this.widgetId) {
                z = true;
            }
            return z;
        }

        public int hashCode() {
            return (this.filter == null ? RemoteViewsAdapter.sDefaultMessageType : this.filter.hashCode()) ^ (this.widgetId << 2);
        }
    }

    static class RemoteViewsFrameLayout extends AppWidgetHostView {
        private final FixedSizeRemoteViewsCache mCache;

        public RemoteViewsFrameLayout(Context context, FixedSizeRemoteViewsCache cache) {
            super(context);
            this.mCache = cache;
        }

        public void onRemoteViewsLoaded(RemoteViews view, OnClickHandler handler) {
            setOnClickHandler(handler);
            applyRemoteViews(view);
        }

        protected View getDefaultView() {
            return this.mCache.getMetaData().createDefaultLoadingView(this);
        }

        protected Context getRemoteContext() {
            return null;
        }

        protected View getErrorView() {
            return getDefaultView();
        }
    }

    private class RemoteViewsFrameLayoutRefSet {
        private final SparseArray<LinkedList<RemoteViewsFrameLayout>> mReferences;
        private final HashMap<RemoteViewsFrameLayout, LinkedList<RemoteViewsFrameLayout>> mViewToLinkedList;

        private RemoteViewsFrameLayoutRefSet() {
            this.mReferences = new SparseArray();
            this.mViewToLinkedList = new HashMap();
        }

        public void add(int position, RemoteViewsFrameLayout layout) {
            LinkedList<RemoteViewsFrameLayout> refs = (LinkedList) this.mReferences.get(position);
            if (refs == null) {
                refs = new LinkedList();
                this.mReferences.put(position, refs);
            }
            this.mViewToLinkedList.put(layout, refs);
            refs.add(layout);
        }

        public void notifyOnRemoteViewsLoaded(int position, RemoteViews view) {
            if (view != null) {
                LinkedList<RemoteViewsFrameLayout> refs = (LinkedList) this.mReferences.get(position);
                if (refs != null) {
                    for (RemoteViewsFrameLayout ref : refs) {
                        ref.onRemoteViewsLoaded(view, RemoteViewsAdapter.this.mRemoteViewsOnClickHandler);
                        if (this.mViewToLinkedList.containsKey(ref)) {
                            this.mViewToLinkedList.remove(ref);
                        }
                    }
                    refs.clear();
                    this.mReferences.remove(position);
                }
            }
        }

        public void removeView(RemoteViewsFrameLayout rvfl) {
            if (this.mViewToLinkedList.containsKey(rvfl)) {
                ((LinkedList) this.mViewToLinkedList.get(rvfl)).remove(rvfl);
                this.mViewToLinkedList.remove(rvfl);
            }
        }

        public void clear() {
            this.mReferences.clear();
            this.mViewToLinkedList.clear();
        }
    }

    private static class RemoteViewsIndexMetaData {
        long itemId;
        int typeId;

        public RemoteViewsIndexMetaData(RemoteViews v, long itemId) {
            set(v, itemId);
        }

        public void set(RemoteViews v, long id) {
            this.itemId = id;
            if (v != null) {
                this.typeId = v.getLayoutId();
            } else {
                this.typeId = RemoteViewsAdapter.sDefaultMessageType;
            }
        }
    }

    private static class RemoteViewsMetaData {
        int count;
        boolean hasStableIds;
        RemoteViews mFirstView;
        int mFirstViewHeight;
        private final SparseIntArray mTypeIdIndexMap;
        RemoteViews mUserLoadingView;
        int viewTypeCount;

        public RemoteViewsMetaData() {
            this.mTypeIdIndexMap = new SparseIntArray();
            reset();
        }

        public void set(RemoteViewsMetaData d) {
            synchronized (d) {
                this.count = d.count;
                this.viewTypeCount = d.viewTypeCount;
                this.hasStableIds = d.hasStableIds;
                setLoadingViewTemplates(d.mUserLoadingView, d.mFirstView);
            }
        }

        public void reset() {
            this.count = RemoteViewsAdapter.sDefaultMessageType;
            this.viewTypeCount = RemoteViewsAdapter.sUnbindServiceMessageType;
            this.hasStableIds = true;
            this.mUserLoadingView = null;
            this.mFirstView = null;
            this.mFirstViewHeight = RemoteViewsAdapter.sDefaultMessageType;
            this.mTypeIdIndexMap.clear();
        }

        public void setLoadingViewTemplates(RemoteViews loadingView, RemoteViews firstView) {
            this.mUserLoadingView = loadingView;
            if (firstView != null) {
                this.mFirstView = firstView;
                this.mFirstViewHeight = -1;
            }
        }

        public int getMappedViewType(int typeId) {
            int mappedTypeId = this.mTypeIdIndexMap.get(typeId, -1);
            if (mappedTypeId != -1) {
                return mappedTypeId;
            }
            mappedTypeId = this.mTypeIdIndexMap.size() + RemoteViewsAdapter.sUnbindServiceMessageType;
            this.mTypeIdIndexMap.put(typeId, mappedTypeId);
            return mappedTypeId;
        }

        public boolean isViewTypeInRange(int typeId) {
            return getMappedViewType(typeId) < this.viewTypeCount;
        }

        private synchronized View createDefaultLoadingView(ViewGroup parent) {
            TextView loadingTextView;
            Context context = parent.getContext();
            if (this.mFirstViewHeight < 0) {
                try {
                    View firstView = this.mFirstView.apply(parent.getContext(), parent);
                    firstView.measure(MeasureSpec.makeMeasureSpec(RemoteViewsAdapter.sDefaultMessageType, RemoteViewsAdapter.sDefaultMessageType), MeasureSpec.makeMeasureSpec(RemoteViewsAdapter.sDefaultMessageType, RemoteViewsAdapter.sDefaultMessageType));
                    this.mFirstViewHeight = firstView.getMeasuredHeight();
                } catch (Exception e) {
                    this.mFirstViewHeight = Math.round(50.0f * context.getResources().getDisplayMetrics().density);
                    Log.w(RemoteViewsAdapter.TAG, "Error inflating first RemoteViews" + e);
                }
                this.mFirstView = null;
            }
            loadingTextView = (TextView) LayoutInflater.from(context).inflate((int) R.layout.remote_views_adapter_default_loading_view, parent, false);
            loadingTextView.setHeight(this.mFirstViewHeight);
            return loadingTextView;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.widget.RemoteViewsAdapter.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.widget.RemoteViewsAdapter.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.widget.RemoteViewsAdapter.<clinit>():void");
    }

    public RemoteViewsAdapter(Context context, Intent intent, RemoteAdapterConnectionCallback callback) {
        this.mNotifyDataSetChangedAfterOnServiceConnected = false;
        this.mDataReady = false;
        this.mContext = context;
        this.mIntent = intent;
        if (this.mIntent == null) {
            throw new IllegalArgumentException("Non-null Intent must be specified.");
        }
        this.mAppWidgetId = intent.getIntExtra("remoteAdapterAppWidgetId", -1);
        this.mLayoutInflater = LayoutInflater.from(context);
        this.mRequestedViews = new RemoteViewsFrameLayoutRefSet();
        if (intent.hasExtra("remoteAdapterAppWidgetId")) {
            intent.removeExtra("remoteAdapterAppWidgetId");
        }
        this.mWorkerThread = new HandlerThread("RemoteViewsCache-loader");
        this.mWorkerThread.start();
        this.mWorkerQueue = new Handler(this.mWorkerThread.getLooper());
        this.mMainQueue = new Handler(Looper.myLooper(), this);
        if (sCacheRemovalThread == null) {
            sCacheRemovalThread = new HandlerThread("RemoteViewsAdapter-cachePruner");
            sCacheRemovalThread.start();
            sCacheRemovalQueue = new Handler(sCacheRemovalThread.getLooper());
        }
        this.mCallback = new WeakReference(callback);
        this.mServiceConnection = new RemoteViewsAdapterServiceConnection(this);
        RemoteViewsCacheKey key = new RemoteViewsCacheKey(new FilterComparison(this.mIntent), this.mAppWidgetId);
        synchronized (sCachedRemoteViewsCaches) {
            if (sCachedRemoteViewsCaches.containsKey(key)) {
                this.mCache = (FixedSizeRemoteViewsCache) sCachedRemoteViewsCaches.get(key);
                synchronized (this.mCache.mMetaData) {
                    if (this.mCache.mMetaData.count > 0) {
                        this.mDataReady = true;
                    }
                }
            } else {
                this.mCache = new FixedSizeRemoteViewsCache(sDefaultCacheSize);
            }
            if (!this.mDataReady) {
                requestBindService();
            }
        }
    }

    protected void finalize() throws Throwable {
        try {
            if (this.mWorkerThread != null) {
                this.mWorkerThread.quit();
            }
            super.finalize();
        } catch (Throwable th) {
            super.finalize();
        }
    }

    public boolean isDataReady() {
        return this.mDataReady;
    }

    public void setRemoteViewsOnClickHandler(OnClickHandler handler) {
        this.mRemoteViewsOnClickHandler = handler;
    }

    public void saveRemoteViewsCache() {
        RemoteViewsCacheKey key = new RemoteViewsCacheKey(new FilterComparison(this.mIntent), this.mAppWidgetId);
        synchronized (sCachedRemoteViewsCaches) {
            if (sRemoteViewsCacheRemoveRunnables.containsKey(key)) {
                sCacheRemovalQueue.removeCallbacks((Runnable) sRemoteViewsCacheRemoveRunnables.get(key));
                sRemoteViewsCacheRemoveRunnables.remove(key);
            }
            synchronized (this.mCache.mMetaData) {
                int metaDataCount = this.mCache.mMetaData.count;
            }
            synchronized (this.mCache) {
                int numRemoteViewsCached = this.mCache.mIndexRemoteViews.size();
            }
            if (metaDataCount > 0 && numRemoteViewsCached > 0) {
                sCachedRemoteViewsCaches.put(key, this.mCache);
            }
            Runnable r = new AnonymousClass1(key);
            sRemoteViewsCacheRemoveRunnables.put(key, r);
            sCacheRemovalQueue.postDelayed(r, TimedRemoteCaller.DEFAULT_CALL_TIMEOUT_MILLIS);
        }
    }

    private void loadNextIndexInBackground() {
        this.mWorkerQueue.post(new Runnable() {
            public void run() {
                if (RemoteViewsAdapter.this.mServiceConnection.isConnected()) {
                    int position;
                    synchronized (RemoteViewsAdapter.this.mCache) {
                        position = RemoteViewsAdapter.this.mCache.getNextIndexToLoad();
                    }
                    if (position > -1) {
                        RemoteViewsAdapter.this.updateRemoteViews(position, true);
                        RemoteViewsAdapter.this.loadNextIndexInBackground();
                        return;
                    }
                    RemoteViewsAdapter.this.enqueueDeferredUnbindServiceMessage();
                }
            }
        });
    }

    private void processException(String method, Exception e) {
        Log.e(TAG, "Error in " + method + ": " + e.getMessage());
        RemoteViewsMetaData metaData = this.mCache.getMetaData();
        synchronized (metaData) {
            metaData.reset();
        }
        synchronized (this.mCache) {
            this.mCache.reset();
        }
        this.mMainQueue.post(new Runnable() {
            public void run() {
                RemoteViewsAdapter.this.superNotifyDataSetChanged();
            }
        });
    }

    private void updateTemporaryMetaData() {
        IRemoteViewsFactory factory = this.mServiceConnection.getRemoteViewsFactory();
        try {
            boolean hasStableIds = factory.hasStableIds();
            int viewTypeCount = factory.getViewTypeCount();
            int count = factory.getCount();
            RemoteViews loadingView = factory.getLoadingView();
            RemoteViews firstView = null;
            if (count > 0 && loadingView == null) {
                firstView = factory.getViewAt(sDefaultMessageType);
            }
            RemoteViewsMetaData tmpMetaData = this.mCache.getTemporaryMetaData();
            synchronized (tmpMetaData) {
                tmpMetaData.hasStableIds = hasStableIds;
                tmpMetaData.viewTypeCount = viewTypeCount + sUnbindServiceMessageType;
                tmpMetaData.count = count;
                tmpMetaData.setLoadingViewTemplates(loadingView, firstView);
            }
        } catch (RemoteException e) {
            processException("updateMetaData", e);
        } catch (RuntimeException e2) {
            processException("updateMetaData", e2);
        }
    }

    private void updateRemoteViews(int position, boolean notifyWhenLoaded) {
        String str;
        IRemoteViewsFactory factory = this.mServiceConnection.getRemoteViewsFactory();
        try {
            RemoteViews remoteViews = factory.getViewAt(position);
            long itemId = factory.getItemId(position);
            if (remoteViews == null) {
                str = "): ";
                str = " null RemoteViews ";
                str = "returned from RemoteViewsFactory.";
                Log.e(TAG, "Error in updateRemoteViews(" + position + r16 + r16 + r16);
                return;
            }
            boolean viewTypeInRange;
            int cacheCount;
            int layoutId = remoteViews.getLayoutId();
            RemoteViewsMetaData metaData = this.mCache.getMetaData();
            synchronized (metaData) {
                viewTypeInRange = metaData.isViewTypeInRange(layoutId);
                cacheCount = this.mCache.mMetaData.count;
            }
            synchronized (this.mCache) {
                if (viewTypeInRange) {
                    this.mCache.insert(position, remoteViews, itemId, getVisibleWindow(this.mVisibleWindowLowerBound, this.mVisibleWindowUpperBound, cacheCount));
                    RemoteViews rv = remoteViews;
                    if (notifyWhenLoaded) {
                        this.mMainQueue.post(new AnonymousClass4(position, remoteViews));
                    }
                } else {
                    Log.e(TAG, "Error: widget's RemoteViewsFactory returns more view types than  indicated by getViewTypeCount() ");
                }
            }
        } catch (RemoteException e) {
            str = "): ";
            str = e.getMessage();
            Log.e(TAG, "Error in updateRemoteViews(" + position + r16 + r16);
        } catch (RuntimeException e2) {
            str = "): ";
            str = e2.getMessage();
            Log.e(TAG, "Error in updateRemoteViews(" + position + r16 + r16);
        }
    }

    public Intent getRemoteViewsServiceIntent() {
        return this.mIntent;
    }

    public int getCount() {
        int i;
        RemoteViewsMetaData metaData = this.mCache.getMetaData();
        synchronized (metaData) {
            i = metaData.count;
        }
        return i;
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        synchronized (this.mCache) {
            if (this.mCache.containsMetaDataAt(position)) {
                long j = this.mCache.getMetaDataAt(position).itemId;
                return j;
            }
            return 0;
        }
    }

    public int getItemViewType(int position) {
        synchronized (this.mCache) {
            if (this.mCache.containsMetaDataAt(position)) {
                int mappedViewType;
                int typeId = this.mCache.getMetaDataAt(position).typeId;
                RemoteViewsMetaData metaData = this.mCache.getMetaData();
                synchronized (metaData) {
                    mappedViewType = metaData.getMappedViewType(typeId);
                }
                return mappedViewType;
            }
            return sDefaultMessageType;
        }
    }

    public void setVisibleRangeHint(int lowerBound, int upperBound) {
        this.mVisibleWindowLowerBound = lowerBound;
        this.mVisibleWindowUpperBound = upperBound;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        RemoteViewsFrameLayout layout;
        synchronized (this.mCache) {
            RemoteViews rv = this.mCache.getRemoteViewsAt(position);
            boolean isInCache = rv != null;
            boolean isConnected = this.mServiceConnection.isConnected();
            boolean hasNewItems = false;
            if (convertView != null && (convertView instanceof RemoteViewsFrameLayout)) {
                this.mRequestedViews.removeView((RemoteViewsFrameLayout) convertView);
            }
            if (isInCache || isConnected) {
                hasNewItems = this.mCache.queuePositionsToBePreloadedFromRequestedPosition(position);
            } else {
                requestBindService();
            }
            if (convertView instanceof RemoteViewsFrameLayout) {
                layout = (RemoteViewsFrameLayout) convertView;
            } else {
                layout = new RemoteViewsFrameLayout(parent.getContext(), this.mCache);
            }
            if (isInCache) {
                layout.onRemoteViewsLoaded(rv, this.mRemoteViewsOnClickHandler);
                if (hasNewItems) {
                    loadNextIndexInBackground();
                }
            } else {
                layout.onRemoteViewsLoaded(this.mCache.getMetaData().mUserLoadingView, this.mRemoteViewsOnClickHandler);
                this.mRequestedViews.add(position, layout);
                this.mCache.queueRequestedPositionToLoad(position);
                loadNextIndexInBackground();
            }
        }
        return layout;
    }

    public int getViewTypeCount() {
        int i;
        RemoteViewsMetaData metaData = this.mCache.getMetaData();
        synchronized (metaData) {
            i = metaData.viewTypeCount;
        }
        return i;
    }

    public boolean hasStableIds() {
        boolean z;
        RemoteViewsMetaData metaData = this.mCache.getMetaData();
        synchronized (metaData) {
            z = metaData.hasStableIds;
        }
        return z;
    }

    public boolean isEmpty() {
        return getCount() <= 0;
    }

    private void onNotifyDataSetChanged() {
        try {
            int newCount;
            int[] visibleWindow;
            this.mServiceConnection.getRemoteViewsFactory().onDataSetChanged();
            synchronized (this.mCache) {
                this.mCache.reset();
            }
            updateTemporaryMetaData();
            synchronized (this.mCache.getTemporaryMetaData()) {
                newCount = this.mCache.getTemporaryMetaData().count;
                visibleWindow = getVisibleWindow(this.mVisibleWindowLowerBound, this.mVisibleWindowUpperBound, newCount);
            }
            int length = visibleWindow.length;
            for (int i = sDefaultMessageType; i < length; i += sUnbindServiceMessageType) {
                int i2 = visibleWindow[i];
                if (i2 < newCount) {
                    updateRemoteViews(i2, false);
                }
            }
            this.mMainQueue.post(new Runnable() {
                public void run() {
                    synchronized (RemoteViewsAdapter.this.mCache) {
                        RemoteViewsAdapter.this.mCache.commitTemporaryMetaData();
                    }
                    RemoteViewsAdapter.this.superNotifyDataSetChanged();
                    RemoteViewsAdapter.this.enqueueDeferredUnbindServiceMessage();
                }
            });
            this.mNotifyDataSetChangedAfterOnServiceConnected = false;
        } catch (RemoteException e) {
            Log.e(TAG, "Error in updateNotifyDataSetChanged(): " + e.getMessage());
        } catch (RuntimeException e2) {
            Log.e(TAG, "Error in updateNotifyDataSetChanged(): " + e2.getMessage());
        }
    }

    private int[] getVisibleWindow(int lower, int upper, int count) {
        if ((lower == 0 && upper == 0) || lower < 0 || upper < 0) {
            return new int[sDefaultMessageType];
        }
        int[] window;
        int i;
        int j;
        if (lower <= upper) {
            window = new int[((upper + sUnbindServiceMessageType) - lower)];
            i = lower;
            j = sDefaultMessageType;
            while (i <= upper) {
                window[j] = i;
                i += sUnbindServiceMessageType;
                j += sUnbindServiceMessageType;
            }
        } else {
            count = Math.max(count, lower);
            window = new int[(((count - lower) + upper) + sUnbindServiceMessageType)];
            j = sDefaultMessageType;
            i = sDefaultMessageType;
            while (i <= upper) {
                window[j] = i;
                i += sUnbindServiceMessageType;
                j += sUnbindServiceMessageType;
            }
            i = lower;
            while (i < count) {
                window[j] = i;
                i += sUnbindServiceMessageType;
                j += sUnbindServiceMessageType;
            }
        }
        return window;
    }

    public void notifyDataSetChanged() {
        this.mMainQueue.removeMessages(sUnbindServiceMessageType);
        if (this.mServiceConnection.isConnected()) {
            this.mWorkerQueue.post(new Runnable() {
                public void run() {
                    RemoteViewsAdapter.this.onNotifyDataSetChanged();
                }
            });
            return;
        }
        this.mNotifyDataSetChangedAfterOnServiceConnected = true;
        requestBindService();
    }

    void superNotifyDataSetChanged() {
        super.notifyDataSetChanged();
    }

    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case sUnbindServiceMessageType /*1*/:
                if (this.mServiceConnection.isConnected()) {
                    this.mServiceConnection.unbind(this.mContext, this.mAppWidgetId, this.mIntent);
                }
                return true;
            default:
                return false;
        }
    }

    private void enqueueDeferredUnbindServiceMessage() {
        this.mMainQueue.removeMessages(sUnbindServiceMessageType);
        this.mMainQueue.sendEmptyMessageDelayed(sUnbindServiceMessageType, TimedRemoteCaller.DEFAULT_CALL_TIMEOUT_MILLIS);
    }

    private boolean requestBindService() {
        if (!this.mServiceConnection.isConnected()) {
            this.mServiceConnection.bind(this.mContext, this.mAppWidgetId, this.mIntent);
        }
        this.mMainQueue.removeMessages(sUnbindServiceMessageType);
        return this.mServiceConnection.isConnected();
    }
}
