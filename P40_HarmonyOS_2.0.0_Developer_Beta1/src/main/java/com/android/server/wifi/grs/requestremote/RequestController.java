package com.android.server.wifi.grs.requestremote;

import android.provider.Settings;
import android.util.Log;
import com.android.server.wifi.grs.GrsCallBack;
import com.android.server.wifi.grs.utils.ContextUtil;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class RequestController {
    private static final String CAPTIVE_PORTAL_REQUEST_TIME = "captive_portal_request_time";
    private static final int DEFAULT_TIME = 0;
    private static final int SILENT_PERIOD = 300000;
    private static final String TAG = "RequestController";
    private static volatile RequestController sInstance;
    private final Object mLock = new Object();
    private ExecutorService taskExecutor = Executors.newCachedThreadPool();

    public static synchronized RequestController getInstance() {
        RequestController requestController;
        synchronized (RequestController.class) {
            if (sInstance == null) {
                sInstance = new RequestController();
            }
            requestController = sInstance;
        }
        return requestController;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private GrsResponse getSyncServicesUrls() {
        synchronized (this.mLock) {
            if (System.currentTimeMillis() - Long.valueOf(Settings.Global.getLong(ContextUtil.getContext().getContentResolver(), CAPTIVE_PORTAL_REQUEST_TIME, 0)).longValue() < 300000) {
                Log.d(TAG, "cancel this request because the last request is initiated less than 5 mins ago");
                return null;
            }
            Settings.Global.putLong(ContextUtil.getContext().getContentResolver(), CAPTIVE_PORTAL_REQUEST_TIME, System.currentTimeMillis());
            Future<GrsResponse> future = this.taskExecutor.submit(new Callable<GrsResponse>() {
                /* class com.android.server.wifi.grs.requestremote.RequestController.AnonymousClass1 */

                @Override // java.util.concurrent.Callable
                public GrsResponse call() {
                    return new GrsRequest().submitExcutorTaskWithTimeout(RequestController.this.taskExecutor);
                }
            });
            try {
                return future.get();
            } catch (CancellationException e) {
                Log.d(TAG, "when check result, find CancellationException, check others");
                return null;
            } catch (ExecutionException e2) {
                Log.d(TAG, "when check result, find ExecutionException, check others");
                return null;
            } catch (InterruptedException e3) {
                Log.d(TAG, "when check result, find InterruptedException, check others");
                return null;
            }
        }
    }

    public void getAsyncServicesUrls(final GrsCallBack grsCallBack) {
        this.taskExecutor.submit(new Runnable() {
            /* class com.android.server.wifi.grs.requestremote.RequestController.AnonymousClass2 */

            @Override // java.lang.Runnable
            public void run() {
                RequestController requestController = RequestController.this;
                requestController.doCallBack(requestController.getSyncServicesUrls(), grsCallBack);
            }
        });
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void doCallBack(GrsResponse grsResponse, GrsCallBack grsCallBack) {
        if (grsCallBack == null) {
            return;
        }
        if (grsResponse == null) {
            Log.d(TAG, "GrsResponse is null");
            grsCallBack.onFailure();
            return;
        }
        Log.d(TAG, "GrsResponse is not null");
        grsCallBack.onResponse(grsResponse);
    }
}
