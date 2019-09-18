package android.widget;

import android.appwidget.AppWidgetHostView;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.util.SparseIntArray;
import android.util.TimedRemoteCaller;
import android.view.InputDevice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RemoteViews;
import android.widget.RemoteViewsAdapter;
import com.android.internal.widget.IRemoteViewsFactory;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.Executor;

public class RemoteViewsAdapter extends BaseAdapter implements Handler.Callback {
    private static final int DEFAULT_CACHE_SIZE = 40;
    private static final int DEFAULT_LOADING_VIEW_HEIGHT = 50;
    static final int MSG_LOAD_NEXT_ITEM = 3;
    private static final int MSG_MAIN_HANDLER_COMMIT_METADATA = 1;
    private static final int MSG_MAIN_HANDLER_REMOTE_ADAPTER_CONNECTED = 3;
    private static final int MSG_MAIN_HANDLER_REMOTE_ADAPTER_DISCONNECTED = 4;
    private static final int MSG_MAIN_HANDLER_REMOTE_VIEWS_LOADED = 5;
    private static final int MSG_MAIN_HANDLER_SUPER_NOTIFY_DATA_SET_CHANGED = 2;
    static final int MSG_NOTIFY_DATA_SET_CHANGED = 2;
    static final int MSG_REQUEST_BIND = 1;
    static final int MSG_UNBIND_SERVICE = 4;
    private static final int REMOTE_VIEWS_CACHE_DURATION = 5000;
    private static final String TAG = "RemoteViewsAdapter";
    private static final int UNBIND_SERVICE_DELAY = 5000;
    private static Handler sCacheRemovalQueue;
    private static HandlerThread sCacheRemovalThread;
    private static final HashMap<RemoteViewsCacheKey, FixedSizeRemoteViewsCache> sCachedRemoteViewsCaches = new HashMap<>();
    private static final HashMap<RemoteViewsCacheKey, Runnable> sRemoteViewsCacheRemoveRunnables = new HashMap<>();
    private final int mAppWidgetId;
    private final Executor mAsyncViewLoadExecutor;
    /* access modifiers changed from: private */
    public final FixedSizeRemoteViewsCache mCache;
    private final RemoteAdapterConnectionCallback mCallback;
    private final Context mContext;
    private boolean mDataReady = false;
    private final Intent mIntent;
    private ApplicationInfo mLastRemoteViewAppInfo;
    /* access modifiers changed from: private */
    public final Handler mMainHandler;
    /* access modifiers changed from: private */
    public RemoteViews.OnClickHandler mRemoteViewsOnClickHandler;
    private RemoteViewsFrameLayoutRefSet mRequestedViews;
    private final RemoteServiceHandler mServiceHandler;
    private int mVisibleWindowLowerBound;
    private int mVisibleWindowUpperBound;
    private final HandlerThread mWorkerThread;

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
        private static final float sMaxCountSlackPercent = 0.75f;
        private static final int sMaxMemoryLimitInBytes = 2097152;
        private final SparseArray<RemoteViewsIndexMetaData> mIndexMetaData = new SparseArray<>();
        /* access modifiers changed from: private */
        public final SparseArray<RemoteViews> mIndexRemoteViews = new SparseArray<>();
        private final SparseBooleanArray mIndicesToLoad = new SparseBooleanArray();
        private int mLastRequestedIndex;
        private final int mMaxCount;
        private final int mMaxCountSlack;
        /* access modifiers changed from: private */
        public final RemoteViewsMetaData mMetaData = new RemoteViewsMetaData();
        private int mPreloadLowerBound;
        private int mPreloadUpperBound;
        private final RemoteViewsMetaData mTemporaryMetaData = new RemoteViewsMetaData();

        public FixedSizeRemoteViewsCache(int maxCacheSize) {
            this.mMaxCount = maxCacheSize;
            this.mMaxCountSlack = Math.round(sMaxCountSlackPercent * ((float) (this.mMaxCount / 2)));
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
            RemoteViewsIndexMetaData metaData = this.mIndexMetaData.get(position);
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
            return this.mIndexRemoteViews.get(position);
        }

        public RemoteViewsIndexMetaData getMetaDataAt(int position) {
            return this.mIndexMetaData.get(position);
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
                RemoteViews v = this.mIndexRemoteViews.valueAt(i);
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
            int count;
            if (this.mPreloadLowerBound <= position && position <= this.mPreloadUpperBound && Math.abs(position - ((this.mPreloadUpperBound + this.mPreloadLowerBound) / 2)) < this.mMaxCountSlack) {
                return false;
            }
            synchronized (this.mMetaData) {
                count = this.mMetaData.count;
            }
            synchronized (this.mIndicesToLoad) {
                for (int i = this.mIndicesToLoad.size() - 1; i >= 0; i--) {
                    if (!this.mIndicesToLoad.valueAt(i)) {
                        this.mIndicesToLoad.removeAt(i);
                    }
                }
                int halfMaxCount = this.mMaxCount / 2;
                this.mPreloadLowerBound = position - halfMaxCount;
                this.mPreloadUpperBound = position + halfMaxCount;
                int effectiveLowerBound = Math.max(0, this.mPreloadLowerBound);
                int effectiveUpperBound = Math.min(this.mPreloadUpperBound, count - 1);
                for (int i2 = effectiveLowerBound; i2 <= effectiveUpperBound; i2++) {
                    if (this.mIndexRemoteViews.indexOfKey(i2) < 0 && !this.mIndicesToLoad.get(i2)) {
                        this.mIndicesToLoad.put(i2, false);
                    }
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
            firstView.applyAsync(context, new RemoteViewsFrameLayout(context, null), executor, new RemoteViews.OnViewAppliedListener() {
                public void onViewApplied(View v) {
                    try {
                        v.measure(View.MeasureSpec.makeMeasureSpec(0, 0), View.MeasureSpec.makeMeasureSpec(0, 0));
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

    public interface RemoteAdapterConnectionCallback {
        void deferNotifyDataSetChanged();

        boolean onRemoteAdapterConnected();

        void onRemoteAdapterDisconnected();

        void setRemoteViewsAdapter(Intent intent, boolean z);
    }

    private static class RemoteServiceHandler extends Handler implements ServiceConnection {
        private final WeakReference<RemoteViewsAdapter> mAdapter;
        private boolean mBindRequested = false;
        private final Context mContext;
        private boolean mNotifyDataSetChangedPending = false;
        private IRemoteViewsFactory mRemoteViewsFactory;

        RemoteServiceHandler(Looper workerLooper, RemoteViewsAdapter adapter, Context context) {
            super(workerLooper);
            this.mAdapter = new WeakReference<>(adapter);
            this.mContext = context;
        }

        public void onServiceConnected(ComponentName name, IBinder service) {
            this.mRemoteViewsFactory = IRemoteViewsFactory.Stub.asInterface(service);
            enqueueDeferredUnbindServiceMessage();
            RemoteViewsAdapter adapter = (RemoteViewsAdapter) this.mAdapter.get();
            if (adapter != null) {
                if (this.mNotifyDataSetChangedPending) {
                    this.mNotifyDataSetChangedPending = false;
                    Message msg = Message.obtain((Handler) this, 2);
                    handleMessage(msg);
                    msg.recycle();
                } else if (sendNotifyDataSetChange(false)) {
                    adapter.updateTemporaryMetaData(this.mRemoteViewsFactory);
                    adapter.mMainHandler.sendEmptyMessage(1);
                    adapter.mMainHandler.sendEmptyMessage(3);
                }
            }
        }

        public void onServiceDisconnected(ComponentName name) {
            this.mRemoteViewsFactory = null;
            RemoteViewsAdapter adapter = (RemoteViewsAdapter) this.mAdapter.get();
            if (adapter != null) {
                adapter.mMainHandler.sendEmptyMessage(4);
            }
        }

        public void handleMessage(Message msg) {
            int newCount;
            int[] visibleWindow;
            RemoteViewsAdapter adapter = (RemoteViewsAdapter) this.mAdapter.get();
            switch (msg.what) {
                case 1:
                    if (adapter == null || this.mRemoteViewsFactory != null) {
                        enqueueDeferredUnbindServiceMessage();
                    }
                    if (!this.mBindRequested) {
                        this.mBindRequested = AppWidgetManager.getInstance(this.mContext).bindRemoteViewsService(this.mContext, msg.arg1, (Intent) msg.obj, this.mContext.getServiceDispatcher(this, this, InputDevice.SOURCE_HDMI), InputDevice.SOURCE_HDMI);
                        return;
                    }
                    return;
                case 2:
                    enqueueDeferredUnbindServiceMessage();
                    if (adapter != null) {
                        if (this.mRemoteViewsFactory == null) {
                            this.mNotifyDataSetChangedPending = true;
                            adapter.requestBindService();
                            return;
                        } else if (sendNotifyDataSetChange(true)) {
                            synchronized (adapter.mCache) {
                                adapter.mCache.reset();
                            }
                            adapter.updateTemporaryMetaData(this.mRemoteViewsFactory);
                            synchronized (adapter.mCache.getTemporaryMetaData()) {
                                newCount = adapter.mCache.getTemporaryMetaData().count;
                                visibleWindow = adapter.getVisibleWindow(newCount);
                            }
                            for (int position : visibleWindow) {
                                if (position < newCount) {
                                    adapter.updateRemoteViews(this.mRemoteViewsFactory, position, false);
                                }
                            }
                            adapter.mMainHandler.sendEmptyMessage(1);
                            adapter.mMainHandler.sendEmptyMessage(2);
                            return;
                        } else {
                            return;
                        }
                    } else {
                        return;
                    }
                case 3:
                    if (adapter != null && this.mRemoteViewsFactory != null) {
                        removeMessages(4);
                        int position2 = adapter.mCache.getNextIndexToLoad();
                        if (position2 > -1) {
                            adapter.updateRemoteViews(this.mRemoteViewsFactory, position2, true);
                            sendEmptyMessage(3);
                        } else {
                            enqueueDeferredUnbindServiceMessage();
                        }
                        return;
                    }
                    return;
                case 4:
                    unbindNow();
                    return;
                default:
                    return;
            }
        }

        /* access modifiers changed from: protected */
        public void unbindNow() {
            if (this.mBindRequested) {
                this.mBindRequested = false;
                this.mContext.unbindService(this);
            }
            this.mRemoteViewsFactory = null;
        }

        private boolean sendNotifyDataSetChange(boolean always) {
            if (!always) {
                try {
                    if (!this.mRemoteViewsFactory.isCreated()) {
                    }
                    return true;
                } catch (RemoteException | RuntimeException e) {
                    Log.e(RemoteViewsAdapter.TAG, "Error in updateNotifyDataSetChanged(): " + e.getMessage());
                    return false;
                }
            }
            this.mRemoteViewsFactory.onDataSetChanged();
            return true;
        }

        private void enqueueDeferredUnbindServiceMessage() {
            removeMessages(4);
            sendEmptyMessageDelayed(4, TimedRemoteCaller.DEFAULT_CALL_TIMEOUT_MILLIS);
        }
    }

    static class RemoteViewsCacheKey {
        final Intent.FilterComparison filter;
        final int widgetId;

        RemoteViewsCacheKey(Intent.FilterComparison filter2, int widgetId2) {
            this.filter = filter2;
            this.widgetId = widgetId2;
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
        public int cacheIndex = -1;
        private final FixedSizeRemoteViewsCache mCache;

        public RemoteViewsFrameLayout(Context context, FixedSizeRemoteViewsCache cache) {
            super(context);
            this.mCache = cache;
        }

        public void onRemoteViewsLoaded(RemoteViews view, RemoteViews.OnClickHandler handler, boolean forceApplyAsync) {
            setOnClickHandler(handler);
            applyRemoteViews(view, forceApplyAsync || (view != null && view.prefersAsyncApply()));
        }

        /* access modifiers changed from: protected */
        public View getDefaultView() {
            int viewHeight = this.mCache.getMetaData().getLoadingTemplate(getContext()).defaultHeight;
            TextView loadingTextView = (TextView) LayoutInflater.from(getContext()).inflate(17367249, (ViewGroup) this, false);
            loadingTextView.setHeight(viewHeight);
            return loadingTextView;
        }

        /* access modifiers changed from: protected */
        public Context getRemoteContext() {
            return null;
        }

        /* access modifiers changed from: protected */
        public View getErrorView() {
            return getDefaultView();
        }
    }

    private class RemoteViewsFrameLayoutRefSet extends SparseArray<LinkedList<RemoteViewsFrameLayout>> {
        private RemoteViewsFrameLayoutRefSet() {
        }

        public void add(int position, RemoteViewsFrameLayout layout) {
            LinkedList<RemoteViewsFrameLayout> refs = (LinkedList) get(position);
            if (refs == null) {
                refs = new LinkedList<>();
                put(position, refs);
            }
            layout.cacheIndex = position;
            refs.add(layout);
        }

        public void notifyOnRemoteViewsLoaded(int position, RemoteViews view) {
            if (view != null) {
                LinkedList<RemoteViewsFrameLayout> refs = (LinkedList) removeReturnOld(position);
                if (refs != null) {
                    Iterator it = refs.iterator();
                    while (it.hasNext()) {
                        ((RemoteViewsFrameLayout) it.next()).onRemoteViewsLoaded(view, RemoteViewsAdapter.this.mRemoteViewsOnClickHandler, true);
                    }
                }
            }
        }

        public void removeView(RemoteViewsFrameLayout rvfl) {
            if (rvfl.cacheIndex >= 0) {
                LinkedList<RemoteViewsFrameLayout> refs = (LinkedList) get(rvfl.cacheIndex);
                if (refs != null) {
                    refs.remove(rvfl);
                }
                rvfl.cacheIndex = -1;
            }
        }
    }

    private static class RemoteViewsIndexMetaData {
        long itemId;
        int typeId;

        public RemoteViewsIndexMetaData(RemoteViews v, long itemId2) {
            set(v, itemId2);
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
            int mappedTypeId2 = this.mTypeIdIndexMap.size() + 1;
            this.mTypeIdIndexMap.put(typeId, mappedTypeId2);
            return mappedTypeId2;
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
        this.mContext = context;
        this.mIntent = intent;
        if (this.mIntent != null) {
            this.mAppWidgetId = intent.getIntExtra("remoteAdapterAppWidgetId", -1);
            HandlerThreadExecutor handlerThreadExecutor = null;
            this.mRequestedViews = new RemoteViewsFrameLayoutRefSet();
            if (intent.hasExtra("remoteAdapterAppWidgetId")) {
                intent.removeExtra("remoteAdapterAppWidgetId");
            }
            this.mWorkerThread = new HandlerThread("RemoteViewsCache-loader");
            this.mWorkerThread.start();
            this.mMainHandler = new Handler(Looper.myLooper(), (Handler.Callback) this);
            this.mServiceHandler = new RemoteServiceHandler(this.mWorkerThread.getLooper(), this, context.getApplicationContext() == null ? context : context.getApplicationContext());
            this.mAsyncViewLoadExecutor = useAsyncLoader ? new HandlerThreadExecutor(this.mWorkerThread) : handlerThreadExecutor;
            this.mCallback = callback;
            if (sCacheRemovalThread == null) {
                sCacheRemovalThread = new HandlerThread("RemoteViewsAdapter-cachePruner");
                sCacheRemovalThread.start();
                sCacheRemovalQueue = new Handler(sCacheRemovalThread.getLooper());
            }
            RemoteViewsCacheKey key = new RemoteViewsCacheKey(new Intent.FilterComparison(this.mIntent), this.mAppWidgetId);
            synchronized (sCachedRemoteViewsCaches) {
                if (sCachedRemoteViewsCaches.containsKey(key)) {
                    this.mCache = sCachedRemoteViewsCaches.get(key);
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
            return;
        }
        throw new IllegalArgumentException("Non-null Intent must be specified.");
    }

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        try {
            this.mServiceHandler.unbindNow();
            this.mWorkerThread.quit();
        } finally {
            super.finalize();
        }
    }

    public boolean isDataReady() {
        return this.mDataReady;
    }

    public void setRemoteViewsOnClickHandler(RemoteViews.OnClickHandler handler) {
        this.mRemoteViewsOnClickHandler = handler;
    }

    public void saveRemoteViewsCache() {
        int metaDataCount;
        int numRemoteViewsCached;
        RemoteViewsCacheKey key = new RemoteViewsCacheKey(new Intent.FilterComparison(this.mIntent), this.mAppWidgetId);
        synchronized (sCachedRemoteViewsCaches) {
            if (sRemoteViewsCacheRemoveRunnables.containsKey(key)) {
                sCacheRemovalQueue.removeCallbacks(sRemoteViewsCacheRemoveRunnables.get(key));
                sRemoteViewsCacheRemoveRunnables.remove(key);
            }
            synchronized (this.mCache.mMetaData) {
                metaDataCount = this.mCache.mMetaData.count;
            }
            synchronized (this.mCache) {
                numRemoteViewsCached = this.mCache.mIndexRemoteViews.size();
            }
            if (metaDataCount > 0 && numRemoteViewsCached > 0) {
                sCachedRemoteViewsCaches.put(key, this.mCache);
            }
            Runnable r = new Runnable() {
                public final void run() {
                    RemoteViewsAdapter.lambda$saveRemoteViewsCache$0(RemoteViewsAdapter.RemoteViewsCacheKey.this);
                }
            };
            sRemoteViewsCacheRemoveRunnables.put(key, r);
            sCacheRemovalQueue.postDelayed(r, TimedRemoteCaller.DEFAULT_CALL_TIMEOUT_MILLIS);
        }
    }

    static /* synthetic */ void lambda$saveRemoteViewsCache$0(RemoteViewsCacheKey key) {
        synchronized (sCachedRemoteViewsCaches) {
            if (sCachedRemoteViewsCaches.containsKey(key)) {
                sCachedRemoteViewsCaches.remove(key);
            }
            if (sRemoteViewsCacheRemoveRunnables.containsKey(key)) {
                sRemoteViewsCacheRemoveRunnables.remove(key);
            }
        }
    }

    /* access modifiers changed from: private */
    public void updateTemporaryMetaData(IRemoteViewsFactory factory) {
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
        } catch (RemoteException | RuntimeException e) {
            Log.e(TAG, "Error in updateMetaData: " + e.getMessage());
            synchronized (this.mCache.getMetaData()) {
                this.mCache.getMetaData().reset();
                synchronized (this.mCache) {
                    this.mCache.reset();
                    this.mMainHandler.sendEmptyMessage(2);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public void updateRemoteViews(IRemoteViewsFactory factory, int position, boolean notifyWhenLoaded) {
        boolean viewTypeInRange;
        int cacheCount;
        try {
            RemoteViews remoteViews = factory.getViewAt(position);
            long itemId = factory.getItemId(position);
            if (remoteViews != null) {
                if (remoteViews.mApplication != null) {
                    if (this.mLastRemoteViewAppInfo == null || !remoteViews.hasSameAppInfo(this.mLastRemoteViewAppInfo)) {
                        this.mLastRemoteViewAppInfo = remoteViews.mApplication;
                    } else {
                        remoteViews.mApplication = this.mLastRemoteViewAppInfo;
                    }
                }
                int layoutId = remoteViews.getLayoutId();
                RemoteViewsMetaData metaData = this.mCache.getMetaData();
                synchronized (metaData) {
                    viewTypeInRange = metaData.isViewTypeInRange(layoutId);
                    cacheCount = this.mCache.mMetaData.count;
                }
                synchronized (this.mCache) {
                    if (viewTypeInRange) {
                        try {
                            this.mCache.insert(position, remoteViews, itemId, getVisibleWindow(cacheCount));
                            if (notifyWhenLoaded) {
                                Message.obtain(this.mMainHandler, 5, position, 0, remoteViews).sendToTarget();
                            }
                        } catch (Throwable th) {
                            throw th;
                        }
                    } else {
                        Log.e(TAG, "Error: widget's RemoteViewsFactory returns more view types than  indicated by getViewTypeCount() ");
                    }
                }
                return;
            }
            throw new RuntimeException("Null remoteViews");
        } catch (RemoteException | RuntimeException e) {
            Log.e(TAG, "Error in updateRemoteViews(" + position + "): " + e.getMessage());
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
            if (!this.mCache.containsMetaDataAt(position)) {
                return 0;
            }
            long j = this.mCache.getMetaDataAt(position).itemId;
            return j;
        }
    }

    public int getItemViewType(int position) {
        int mappedViewType;
        synchronized (this.mCache) {
            if (!this.mCache.containsMetaDataAt(position)) {
                return 0;
            }
            int typeId = this.mCache.getMetaDataAt(position).typeId;
            RemoteViewsMetaData metaData = this.mCache.getMetaData();
            synchronized (metaData) {
                mappedViewType = metaData.getMappedViewType(typeId);
            }
            return mappedViewType;
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
            boolean hasNewItems = false;
            if (convertView != null && (convertView instanceof RemoteViewsFrameLayout)) {
                this.mRequestedViews.removeView((RemoteViewsFrameLayout) convertView);
            }
            if (!isInCache) {
                requestBindService();
            } else {
                hasNewItems = this.mCache.queuePositionsToBePreloadedFromRequestedPosition(position);
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
                    this.mServiceHandler.sendEmptyMessage(3);
                }
            } else {
                layout.onRemoteViewsLoaded(this.mCache.getMetaData().getLoadingTemplate(this.mContext).remoteViews, this.mRemoteViewsOnClickHandler, false);
                this.mRequestedViews.add(position, layout);
                this.mCache.queueRequestedPositionToLoad(position);
                this.mServiceHandler.sendEmptyMessage(3);
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

    /* access modifiers changed from: private */
    public int[] getVisibleWindow(int count) {
        int[] window;
        int lower = this.mVisibleWindowLowerBound;
        int upper = this.mVisibleWindowUpperBound;
        int j = 0;
        if ((lower == 0 && upper == 0) || lower < 0 || upper < 0) {
            return new int[0];
        }
        if (lower <= upper) {
            window = new int[((upper + 1) - lower)];
            int i = lower;
            while (i <= upper) {
                window[j] = i;
                i++;
                j++;
            }
        } else {
            int count2 = Math.max(count, lower);
            window = new int[((count2 - lower) + upper + 1)];
            int j2 = 0;
            while (j <= upper) {
                window[j2] = j;
                j++;
                j2++;
            }
            int i2 = lower;
            while (i2 < count2) {
                window[j2] = i2;
                i2++;
                j2++;
            }
        }
        return window;
    }

    public void notifyDataSetChanged() {
        this.mServiceHandler.removeMessages(4);
        this.mServiceHandler.sendEmptyMessage(2);
    }

    /* access modifiers changed from: package-private */
    public void superNotifyDataSetChanged() {
        super.notifyDataSetChanged();
    }

    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case 1:
                this.mCache.commitTemporaryMetaData();
                return true;
            case 2:
                superNotifyDataSetChanged();
                return true;
            case 3:
                if (this.mCallback != null) {
                    this.mCallback.onRemoteAdapterConnected();
                }
                return true;
            case 4:
                if (this.mCallback != null) {
                    this.mCallback.onRemoteAdapterDisconnected();
                }
                return true;
            case 5:
                this.mRequestedViews.notifyOnRemoteViewsLoaded(msg.arg1, (RemoteViews) msg.obj);
                return true;
            default:
                return false;
        }
    }

    /* access modifiers changed from: private */
    public void requestBindService() {
        this.mServiceHandler.removeMessages(4);
        Message.obtain(this.mServiceHandler, 1, this.mAppWidgetId, 0, this.mIntent).sendToTarget();
    }
}
