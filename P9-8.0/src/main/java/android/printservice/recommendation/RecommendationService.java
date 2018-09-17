package android.printservice.recommendation;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.printservice.recommendation.IRecommendationService.Stub;
import java.util.List;

public abstract class RecommendationService extends Service {
    private static final String LOG_TAG = "PrintServiceRecS";
    public static final String SERVICE_INTERFACE = "android.printservice.recommendation.RecommendationService";
    private IRecommendationServiceCallbacks mCallbacks;
    private Handler mHandler;

    private class MyHandler extends Handler {
        static final int MSG_CONNECT = 1;
        static final int MSG_DISCONNECT = 2;
        static final int MSG_UPDATE = 3;

        MyHandler() {
            super(Looper.getMainLooper());
        }

        /* JADX WARNING: Removed duplicated region for block: B:6:0x002f A:{Splitter: B:4:0x0021, ExcHandler: android.os.RemoteException (r0_0 'e' java.lang.Exception)} */
        /* JADX WARNING: Missing block: B:6:0x002f, code:
            r0 = move-exception;
     */
        /* JADX WARNING: Missing block: B:7:0x0030, code:
            android.util.Log.e(android.printservice.recommendation.RecommendationService.LOG_TAG, "Could not update recommended services", r0);
     */
        /* JADX WARNING: Missing block: B:11:?, code:
            return;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    RecommendationService.this.mCallbacks = (IRecommendationServiceCallbacks) msg.obj;
                    RecommendationService.this.onConnected();
                    return;
                case 2:
                    RecommendationService.this.onDisconnected();
                    RecommendationService.this.mCallbacks = null;
                    return;
                case 3:
                    try {
                        RecommendationService.this.mCallbacks.onRecommendationsUpdated((List) msg.obj);
                        return;
                    } catch (Exception e) {
                    }
                default:
                    return;
            }
        }
    }

    public abstract void onConnected();

    public abstract void onDisconnected();

    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        this.mHandler = new MyHandler();
    }

    public final void updateRecommendations(List<RecommendationInfo> recommendations) {
        this.mHandler.obtainMessage(3, recommendations).sendToTarget();
    }

    public final IBinder onBind(Intent intent) {
        return new Stub() {
            public void registerCallbacks(IRecommendationServiceCallbacks callbacks) {
                if (callbacks != null) {
                    RecommendationService.this.mHandler.obtainMessage(1, callbacks).sendToTarget();
                } else {
                    RecommendationService.this.mHandler.obtainMessage(2).sendToTarget();
                }
            }
        };
    }
}
