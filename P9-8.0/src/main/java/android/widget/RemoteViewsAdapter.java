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
import android.widget.RemoteViews.OnViewAppliedListener;
import com.android.internal.R;
import com.android.internal.widget.IRemoteViewsAdapterConnection.Stub;
import com.android.internal.widget.IRemoteViewsFactory;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.Executor;

public class RemoteViewsAdapter extends BaseAdapter implements Callback {
    private static final String MULTI_USER_PERM = "android.permission.INTERACT_ACROSS_USERS_FULL";
    private static final int REMOTE_VIEWS_CACHE_DURATION = 5000;
    private static final String TAG = "RemoteViewsAdapter";
    private static Handler sCacheRemovalQueue = null;
    private static HandlerThread sCacheRemovalThread = null;
    private static final HashMap<RemoteViewsCacheKey, FixedSizeRemoteViewsCache> sCachedRemoteViewsCaches = new HashMap();
    private static final int sDefaultCacheSize = 40;
    private static final int sDefaultLoadingViewHeight = 50;
    private static final int sDefaultMessageType = 0;
    private static final HashMap<RemoteViewsCacheKey, Runnable> sRemoteViewsCacheRemoveRunnables = new HashMap();
    private static final int sUnbindServiceDelay = 5000;
    private static final int sUnbindServiceMessageType = 1;
    private final int mAppWidgetId;
    private final Executor mAsyncViewLoadExecutor;
    private final FixedSizeRemoteViewsCache mCache;
    private WeakReference<RemoteAdapterConnectionCallback> mCallback;
    private final Context mContext;
    private boolean mDataReady = false;
    private final Intent mIntent;
    private Handler mMainQueue;
    private boolean mNotifyDataSetChangedAfterOnServiceConnected = false;
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

        void setRemoteViewsAdapter(Intent intent, boolean z);
    }

    public static class AsyncRemoteAdapterAction implements Runnable {
        private final RemoteAdapterConnectionCallback mCallback;
        private final Intent mIntent;

        public AsyncRemoteAdapterAction(RemoteAdapterConnectionCallback callback, Intent intent) {
            this.mCallback = callback;
            this.mIntent = intent;
        }

        public void run() {
            this.mCallback.setRemoteViewsAdapter(this.mIntent, true);
        }
    }

    private static class FixedSizeRemoteViewsCache {
        private static final String TAG = "FixedSizeRemoteViewsCache";
        private static final float sMaxCountSlackPercent = 0.75f;
        private static final int sMaxMemoryLimitInBytes = 2097152;
        private final SparseArray<RemoteViewsIndexMetaData> mIndexMetaData = new SparseArray();
        private final SparseArray<RemoteViews> mIndexRemoteViews = new SparseArray();
        private final SparseBooleanArray mIndicesToLoad = new SparseBooleanArray();
        private int mLastRequestedIndex;
        private final int mMaxCount;
        private final int mMaxCountSlack;
        private final RemoteViewsMetaData mMetaData = new RemoteViewsMetaData();
        private int mPreloadLowerBound;
        private int mPreloadUpperBound;
        private final RemoteViewsMetaData mTemporaryMetaData = new RemoteViewsMetaData();

        public FixedSizeRemoteViewsCache(int maxCacheSize) {
            this.mMaxCount = maxCacheSize;
            this.mMaxCountSlack = Math.round(((float) (this.mMaxCount / 2)) * sMaxCountSlackPercent);
            this.mPreloadLowerBound = 0;
            this.mPreloadUpperBound = -1;
            this.mLastRequestedIndex = -1;
        }

        public void insert(int position, RemoteViews v, long itemId, int[] visibleWindow) {
            if (this.mIndexRemoteViews.size() >= this.mMaxCount) {
                this.mIndexRemoteViews.remove(getFarthestPositionFrom(position, visibleWindow));
            }
            int pruneFromPosition = this.mLastRequestedIndex > -1 ? this.mLastRequestedIndex : position;
            while (getRemoteViewsBitmapMemoryUsage() >= 2097152) {
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
            int mem = 0;
            for (int i = this.mIndexRemoteViews.size() - 1; i >= 0; i--) {
                RemoteViews v = (RemoteViews) this.mIndexRemoteViews.valueAt(i);
                if (v != null) {
                    mem += v.estimateMemoryUsage();
                }
            }
            return mem;
        }

        private int getFarthestPositionFrom(int pos, int[] visibleWindow) {
            int maxDist = 0;
            int maxDistIndex = -1;
            int maxDistNotVisible = 0;
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
            int count;
            synchronized (this.mMetaData) {
                count = this.mMetaData.count;
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
                int effectiveLowerBound = Math.max(0, this.mPreloadLowerBound);
                int effectiveUpperBound = Math.min(this.mPreloadUpperBound, count - 1);
                i = effectiveLowerBound;
                while (i <= effectiveUpperBound) {
                    if (this.mIndexRemoteViews.indexOfKey(i) < 0 && (this.mIndicesToLoad.get(i) ^ 1) != 0) {
                        this.mIndicesToLoad.put(i, false);
                    }
                    i++;
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
            this.mPreloadLowerBound = 0;
            this.mPreloadUpperBound = -1;
            this.mLastRequestedIndex = -1;
            this.mIndexRemoteViews.clear();
            this.mIndexMetaData.clear();
            synchronized (this.mIndicesToLoad) {
                this.mIndicesToLoad.clear();
            }
        }
    }

    private static class HandlerThreadExecutor implements Executor {
        private final HandlerThread mThread;

        HandlerThreadExecutor(HandlerThread thread) {
            this.mThread = thread;
        }

        public void execute(Runnable runnable) {
            if (Thread.currentThread().getId() == this.mThread.getId()) {
                runnable.run();
            } else {
                new Handler(this.mThread.getLooper()).post(runnable);
            }
        }
    }

    private static class LoadingViewTemplate {
        public int defaultHeight;
        public final RemoteViews remoteViews;

        LoadingViewTemplate(RemoteViews views, Context context) {
            this.remoteViews = views;
            this.defaultHeight = Math.round(50.0f * context.getResources().getDisplayMetrics().density);
        }

        public void loadFirstViewHeight(RemoteViews firstView, Context context, Executor executor) {
            firstView.applyAsync(context, new RemoteViewsFrameLayout(context, null), executor, new OnViewAppliedListener() {
                public void onViewApplied(View v) {
                    try {
                        v.measure(MeasureSpec.makeMeasureSpec(0, 0), MeasureSpec.makeMeasureSpec(0, 0));
                        LoadingViewTemplate.this.defaultHeight = v.getMeasuredHeight();
                    } catch (Exception e) {
                        onError(e);
                    }
                }

                public void onError(Exception e) {
                    Log.w(RemoteViewsAdapter.TAG, "Error inflating first RemoteViews", e);
                }
            });
        }
    }

    private static class RemoteViewsAdapterServiceConnection extends Stub {
        private WeakReference<RemoteViewsAdapter> mAdapter;
        private boolean mIsConnected;
        private boolean mIsConnecting;
        private IRemoteViewsFactory mRemoteViewsFactory;

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
                    Log.e("RVAServiceConnection", "bind(): " + e.getMessage());
                    this.mIsConnecting = false;
                    this.mIsConnected = false;
                }
            }
            return;
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
                Log.e("RVAServiceConnection", "unbind(): " + e.getMessage());
                this.mIsConnecting = false;
                this.mIsConnected = false;
            }
            return;
        }

        public synchronized void onServiceConnected(IBinder service) {
            this.mRemoteViewsFactory = IRemoteViewsFactory.Stub.asInterface(service);
            final RemoteViewsAdapter adapter = (RemoteViewsAdapter) this.mAdapter.get();
            if (adapter != null) {
                adapter.mWorkerQueue.post(new Runnable() {
                    public void run() {
                        if (adapter.mNotifyDataSetChangedAfterOnServiceConnected) {
                            adapter.onNotifyDataSetChanged();
                        } else {
                            IRemoteViewsFactory factory = adapter.mServiceConnection.getRemoteViewsFactory();
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
                            adapter.updateTemporaryMetaData();
                            Handler -get2 = adapter.mMainQueue;
                            final RemoteViewsAdapter remoteViewsAdapter = adapter;
                            -get2.post(new Runnable() {
                                public void run() {
                                    synchronized (remoteViewsAdapter.mCache) {
                                        remoteViewsAdapter.mCache.commitTemporaryMetaData();
                                    }
                                    RemoteAdapterConnectionCallback callback = (RemoteAdapterConnectionCallback) remoteViewsAdapter.mCallback.get();
                                    if (callback != null) {
                                        callback.onRemoteAdapterConnected();
                                    }
                                }
                            });
                        }
                        adapter.enqueueDeferredUnbindServiceMessage();
                        RemoteViewsAdapterServiceConnection.this.mIsConnected = true;
                        RemoteViewsAdapterServiceConnection.this.mIsConnecting = false;
                    }
                });
            }
        }

        public synchronized void onServiceDisconnected() {
            this.mIsConnected = false;
            this.mIsConnecting = false;
            this.mRemoteViewsFactory = null;
            final RemoteViewsAdapter adapter = (RemoteViewsAdapter) this.mAdapter.get();
            if (adapter != null) {
                adapter.mMainQueue.post(new Runnable() {
                    public void run() {
                        adapter.mMainQueue.removeMessages(1);
                        RemoteAdapterConnectionCallback callback = (RemoteAdapterConnectionCallback) adapter.mCallback.get();
                        if (callback != null) {
                            callback.onRemoteAdapterDisconnected();
                        }
                    }
                });
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
            return (this.filter == null ? 0 : this.filter.hashCode()) ^ (this.widgetId << 2);
        }
    }

    static class RemoteViewsFrameLayout extends AppWidgetHostView {
        private final FixedSizeRemoteViewsCache mCache;

        public RemoteViewsFrameLayout(Context context, FixedSizeRemoteViewsCache cache) {
            super(context);
            this.mCache = cache;
        }

        public void onRemoteViewsLoaded(RemoteViews view, OnClickHandler handler, boolean forceApplyAsync) {
            setOnClickHandler(handler);
            boolean prefersAsyncApply = !forceApplyAsync ? view != null ? view.prefersAsyncApply() : false : true;
            applyRemoteViews(view, prefersAsyncApply);
        }

        protected View getDefaultView() {
            TextView loadingTextView = (TextView) LayoutInflater.from(getContext()).inflate((int) R.layout.remote_views_adapter_default_loading_view, (ViewGroup) this, false);
            loadingTextView.setHeight(this.mCache.getMetaData().getLoadingTemplate(getContext()).defaultHeight);
            return loadingTextView;
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

        /* synthetic */ RemoteViewsFrameLayoutRefSet(RemoteViewsAdapter this$0, RemoteViewsFrameLayoutRefSet -this1) {
            this();
        }

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
                        ref.onRemoteViewsLoaded(view, RemoteViewsAdapter.this.mRemoteViewsOnClickHandler, true);
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
                this.typeId = 0;
            }
        }
    }

    private static class RemoteViewsMetaData {
        int count;
        boolean hasStableIds;
        LoadingViewTemplate loadingTemplate;
        private final SparseIntArray mTypeIdIndexMap = new SparseIntArray();
        int viewTypeCount;

        public RemoteViewsMetaData() {
            reset();
        }

        public void set(RemoteViewsMetaData d) {
            synchronized (d) {
                this.count = d.count;
                this.viewTypeCount = d.viewTypeCount;
                this.hasStableIds = d.hasStableIds;
                this.loadingTemplate = d.loadingTemplate;
            }
        }

        public void reset() {
            this.count = 0;
            this.viewTypeCount = 1;
            this.hasStableIds = true;
            this.loadingTemplate = null;
            this.mTypeIdIndexMap.clear();
        }

        public int getMappedViewType(int typeId) {
            int mappedTypeId = this.mTypeIdIndexMap.get(typeId, -1);
            if (mappedTypeId != -1) {
                return mappedTypeId;
            }
            mappedTypeId = this.mTypeIdIndexMap.size() + 1;
            this.mTypeIdIndexMap.put(typeId, mappedTypeId);
            return mappedTypeId;
        }

        public boolean isViewTypeInRange(int typeId) {
            return getMappedViewType(typeId) < this.viewTypeCount;
        }

        public synchronized LoadingViewTemplate getLoadingTemplate(Context context) {
            if (this.loadingTemplate == null) {
                this.loadingTemplate = new LoadingViewTemplate(null, context);
            }
            return this.loadingTemplate;
        }
    }

    public RemoteViewsAdapter(Context context, Intent intent, RemoteAdapterConnectionCallback callback, boolean useAsyncLoader) {
        Executor executor = null;
        this.mContext = context;
        this.mIntent = intent;
        if (this.mIntent == null) {
            throw new IllegalArgumentException("Non-null Intent must be specified.");
        }
        this.mAppWidgetId = intent.getIntExtra("remoteAdapterAppWidgetId", -1);
        this.mRequestedViews = new RemoteViewsFrameLayoutRefSet(this, null);
        if (intent.hasExtra("remoteAdapterAppWidgetId")) {
            intent.removeExtra("remoteAdapterAppWidgetId");
        }
        this.mWorkerThread = new HandlerThread("RemoteViewsCache-loader");
        this.mWorkerThread.start();
        this.mWorkerQueue = new Handler(this.mWorkerThread.getLooper());
        this.mMainQueue = new Handler(Looper.myLooper(), this);
        if (useAsyncLoader) {
            executor = new HandlerThreadExecutor(this.mWorkerThread);
        }
        this.mAsyncViewLoadExecutor = executor;
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
                this.mCache = new FixedSizeRemoteViewsCache(40);
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
        final RemoteViewsCacheKey key = new RemoteViewsCacheKey(new FilterComparison(this.mIntent), this.mAppWidgetId);
        synchronized (sCachedRemoteViewsCaches) {
            int metaDataCount;
            if (sRemoteViewsCacheRemoveRunnables.containsKey(key)) {
                sCacheRemovalQueue.removeCallbacks((Runnable) sRemoteViewsCacheRemoveRunnables.get(key));
                sRemoteViewsCacheRemoveRunnables.remove(key);
            }
            synchronized (this.mCache.mMetaData) {
                metaDataCount = this.mCache.mMetaData.count;
            }
            synchronized (this.mCache) {
                int numRemoteViewsCached = this.mCache.mIndexRemoteViews.size();
            }
            if (metaDataCount > 0 && numRemoteViewsCached > 0) {
                sCachedRemoteViewsCaches.put(key, this.mCache);
            }
            Runnable r = new Runnable() {
                public void run() {
                    synchronized (RemoteViewsAdapter.sCachedRemoteViewsCaches) {
                        if (RemoteViewsAdapter.sCachedRemoteViewsCaches.containsKey(key)) {
                            RemoteViewsAdapter.sCachedRemoteViewsCaches.remove(key);
                        }
                        if (RemoteViewsAdapter.sRemoteViewsCacheRemoveRunnables.containsKey(key)) {
                            RemoteViewsAdapter.sRemoteViewsCacheRemoveRunnables.remove(key);
                        }
                    }
                }
            };
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
            LoadingViewTemplate loadingTemplate = new LoadingViewTemplate(factory.getLoadingView(), this.mContext);
            if (count > 0 && loadingTemplate.remoteViews == null) {
                RemoteViews firstView = factory.getViewAt(0);
                if (firstView != null) {
                    loadingTemplate.loadFirstViewHeight(firstView, this.mContext, new HandlerThreadExecutor(this.mWorkerThread));
                }
            }
            RemoteViewsMetaData tmpMetaData = this.mCache.getTemporaryMetaData();
            synchronized (tmpMetaData) {
                tmpMetaData.hasStableIds = hasStableIds;
                tmpMetaData.viewTypeCount = viewTypeCount + 1;
                tmpMetaData.count = count;
                tmpMetaData.loadingTemplate = loadingTemplate;
            }
        } catch (RemoteException e) {
            processException("updateMetaData", e);
        } catch (RuntimeException e2) {
            processException("updateMetaData", e2);
        }
    }

    private void updateRemoteViews(int position, boolean notifyWhenLoaded) {
        IRemoteViewsFactory factory = this.mServiceConnection.getRemoteViewsFactory();
        try {
            final RemoteViews remoteViews = factory.getViewAt(position);
            long itemId = factory.getItemId(position);
            if (remoteViews == null) {
                Log.e(TAG, "Error in updateRemoteViews(" + position + "): " + " null RemoteViews " + "returned from RemoteViewsFactory.");
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
                        final int i = position;
                        this.mMainQueue.post(new Runnable() {
                            public void run() {
                                RemoteViewsAdapter.this.mRequestedViews.notifyOnRemoteViewsLoaded(i, remoteViews);
                            }
                        });
                    }
                } else {
                    Log.e(TAG, "Error: widget's RemoteViewsFactory returns more view types than  indicated by getViewTypeCount() ");
                }
            }
        } catch (RemoteException e) {
            Log.e(TAG, "Error in updateRemoteViews(" + position + "): " + e.getMessage());
        } catch (RuntimeException e2) {
            Log.e(TAG, "Error in updateRemoteViews(" + position + "): " + e2.getMessage());
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
            return 0;
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
            if (isInCache || (isConnected ^ 1) == 0) {
                hasNewItems = this.mCache.queuePositionsToBePreloadedFromRequestedPosition(position);
            } else {
                requestBindService();
            }
            if (convertView instanceof RemoteViewsFrameLayout) {
                layout = (RemoteViewsFrameLayout) convertView;
            } else {
                layout = new RemoteViewsFrameLayout(parent.getContext(), this.mCache);
                layout.setExecutor(this.mAsyncViewLoadExecutor);
            }
            if (isInCache) {
                layout.onRemoteViewsLoaded(rv, this.mRemoteViewsOnClickHandler, false);
                if (hasNewItems) {
                    loadNextIndexInBackground();
                }
            } else {
                layout.onRemoteViewsLoaded(this.mCache.getMetaData().getLoadingTemplate(this.mContext).remoteViews, this.mRemoteViewsOnClickHandler, false);
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
            this.mServiceConnection.getRemoteViewsFactory().onDataSetChanged();
            synchronized (this.mCache) {
                this.mCache.reset();
            }
            updateTemporaryMetaData();
            synchronized (this.mCache.getTemporaryMetaData()) {
                newCount = this.mCache.getTemporaryMetaData().count;
            }
            for (int i : getVisibleWindow(this.mVisibleWindowLowerBound, this.mVisibleWindowUpperBound, newCount)) {
                if (i < newCount) {
                    updateRemoteViews(i, false);
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
            return new int[0];
        }
        int[] window;
        int i;
        int j;
        if (lower <= upper) {
            window = new int[((upper + 1) - lower)];
            i = lower;
            j = 0;
            while (i <= upper) {
                window[j] = i;
                i++;
                j++;
            }
        } else {
            count = Math.max(count, lower);
            window = new int[(((count - lower) + upper) + 1)];
            j = 0;
            i = 0;
            while (i <= upper) {
                window[j] = i;
                i++;
                j++;
            }
            i = lower;
            while (i < count) {
                window[j] = i;
                i++;
                j++;
            }
        }
        return window;
    }

    public void notifyDataSetChanged() {
        this.mMainQueue.removeMessages(1);
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
            case 1:
                if (this.mServiceConnection.isConnected()) {
                    this.mServiceConnection.unbind(this.mContext, this.mAppWidgetId, this.mIntent);
                }
                return true;
            default:
                return false;
        }
    }

    private void enqueueDeferredUnbindServiceMessage() {
        this.mMainQueue.removeMessages(1);
        this.mMainQueue.sendEmptyMessageDelayed(1, TimedRemoteCaller.DEFAULT_CALL_TIMEOUT_MILLIS);
    }

    private boolean requestBindService() {
        if (!this.mServiceConnection.isConnected()) {
            this.mServiceConnection.bind(this.mContext, this.mAppWidgetId, this.mIntent);
        }
        this.mMainQueue.removeMessages(1);
        return this.mServiceConnection.isConnected();
    }
}
