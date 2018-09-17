package android.widget;

import android.app.Service;
import android.content.Intent;
import android.content.Intent.FilterComparison;
import android.os.IBinder;
import com.android.internal.widget.IRemoteViewsFactory.Stub;
import java.util.HashMap;

public abstract class RemoteViewsService extends Service {
    private static final String LOG_TAG = "RemoteViewsService";
    private static final Object sLock = new Object();
    private static final HashMap<FilterComparison, RemoteViewsFactory> sRemoteViewFactories = new HashMap();

    public interface RemoteViewsFactory {
        int getCount();

        long getItemId(int i);

        RemoteViews getLoadingView();

        RemoteViews getViewAt(int i);

        int getViewTypeCount();

        boolean hasStableIds();

        void onCreate();

        void onDataSetChanged();

        void onDestroy();
    }

    private static class RemoteViewsFactoryAdapter extends Stub {
        private RemoteViewsFactory mFactory;
        private boolean mIsCreated;

        public RemoteViewsFactoryAdapter(RemoteViewsFactory factory, boolean isCreated) {
            this.mFactory = factory;
            this.mIsCreated = isCreated;
        }

        public synchronized boolean isCreated() {
            return this.mIsCreated;
        }

        public synchronized void onDataSetChanged() {
            try {
                this.mFactory.onDataSetChanged();
            } catch (Exception ex) {
                Thread.getDefaultUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), ex);
            }
            return;
        }

        public synchronized void onDataSetChangedAsync() {
            onDataSetChanged();
        }

        public synchronized int getCount() {
            int count;
            count = 0;
            try {
                count = this.mFactory.getCount();
            } catch (Exception ex) {
                Thread.getDefaultUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), ex);
            }
            return count;
        }

        public synchronized RemoteViews getViewAt(int position) {
            RemoteViews rv;
            rv = null;
            try {
                rv = this.mFactory.getViewAt(position);
                if (rv != null) {
                    rv.setIsWidgetCollectionChild(true);
                }
            } catch (Exception ex) {
                Thread.getDefaultUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), ex);
            }
            return rv;
        }

        public synchronized RemoteViews getLoadingView() {
            RemoteViews rv;
            rv = null;
            try {
                rv = this.mFactory.getLoadingView();
            } catch (Exception ex) {
                Thread.getDefaultUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), ex);
            }
            return rv;
        }

        public synchronized int getViewTypeCount() {
            int count;
            count = 0;
            try {
                count = this.mFactory.getViewTypeCount();
            } catch (Exception ex) {
                Thread.getDefaultUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), ex);
            }
            return count;
        }

        public synchronized long getItemId(int position) {
            long id;
            id = 0;
            try {
                id = this.mFactory.getItemId(position);
            } catch (Exception ex) {
                Thread.getDefaultUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), ex);
            }
            return id;
        }

        public synchronized boolean hasStableIds() {
            boolean hasStableIds;
            hasStableIds = false;
            try {
                hasStableIds = this.mFactory.hasStableIds();
            } catch (Exception ex) {
                Thread.getDefaultUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), ex);
            }
            return hasStableIds;
        }

        public void onDestroy(Intent intent) {
            synchronized (RemoteViewsService.sLock) {
                FilterComparison fc = new FilterComparison(intent);
                if (RemoteViewsService.sRemoteViewFactories.containsKey(fc)) {
                    try {
                        ((RemoteViewsFactory) RemoteViewsService.sRemoteViewFactories.get(fc)).onDestroy();
                    } catch (Exception ex) {
                        Thread.getDefaultUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), ex);
                    }
                    RemoteViewsService.sRemoteViewFactories.remove(fc);
                }
            }
            return;
        }
    }

    public abstract RemoteViewsFactory onGetViewFactory(Intent intent);

    public IBinder onBind(Intent intent) {
        IBinder remoteViewsFactoryAdapter;
        synchronized (sLock) {
            RemoteViewsFactory factory;
            boolean isCreated;
            FilterComparison fc = new FilterComparison(intent);
            if (sRemoteViewFactories.containsKey(fc)) {
                factory = (RemoteViewsFactory) sRemoteViewFactories.get(fc);
                isCreated = true;
            } else {
                factory = onGetViewFactory(intent);
                sRemoteViewFactories.put(fc, factory);
                factory.onCreate();
                isCreated = false;
            }
            remoteViewsFactoryAdapter = new RemoteViewsFactoryAdapter(factory, isCreated);
        }
        return remoteViewsFactoryAdapter;
    }
}
