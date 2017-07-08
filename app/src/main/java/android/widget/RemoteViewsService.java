package android.widget;

import android.app.Service;
import android.content.Intent;
import android.content.Intent.FilterComparison;
import android.os.IBinder;
import com.android.internal.widget.IRemoteViewsFactory.Stub;
import java.util.HashMap;

public abstract class RemoteViewsService extends Service {
    private static final String LOG_TAG = "RemoteViewsService";
    private static final Object sLock = null;
    private static final HashMap<FilterComparison, RemoteViewsFactory> sRemoteViewFactories = null;

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
            RemoteViews remoteViews;
            remoteViews = null;
            try {
                remoteViews = this.mFactory.getViewAt(position);
                if (remoteViews != null) {
                    remoteViews.setIsWidgetCollectionChild(true);
                }
            } catch (Exception ex) {
                Thread.getDefaultUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), ex);
            }
            return remoteViews;
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
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.widget.RemoteViewsService.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.widget.RemoteViewsService.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.widget.RemoteViewsService.<clinit>():void");
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
