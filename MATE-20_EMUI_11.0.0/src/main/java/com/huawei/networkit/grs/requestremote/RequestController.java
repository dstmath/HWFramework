package com.huawei.networkit.grs.requestremote;

import com.huawei.networkit.grs.GrsBaseInfo;
import com.huawei.networkit.grs.GrsCallBack;
import com.huawei.networkit.grs.common.Logger;
import com.huawei.networkit.grs.requestremote.model.GrsRequestBean;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class RequestController {
    private static final String TAG = "RequestController";
    private static volatile RequestController instance;
    private Map<String, GrsRequestBean> grsRequestMap = new ConcurrentHashMap(16);
    private final Object lock = new Object();
    private ExecutorService taskExecutor = Executors.newCachedThreadPool();

    public static RequestController getInstance() {
        if (instance == null) {
            synchronized (RequestController.class) {
                if (instance == null) {
                    instance = new RequestController();
                }
            }
        }
        return instance;
    }

    private RequestController() {
    }

    public GrsResponse getSyncServicesUrls(final GrsBaseInfo grsBaseInfo) {
        Future<GrsResponse> future;
        String spUrlKey = grsBaseInfo.getGrsParasKey(false, true);
        synchronized (this.lock) {
            GrsRequestBean hitGrsRequestBean = this.grsRequestMap.get(spUrlKey);
            if (hitGrsRequestBean != null) {
                if (hitGrsRequestBean.isValid()) {
                    future = hitGrsRequestBean.getFuture();
                }
            }
            Logger.v(TAG, "hitGrsRequestBean == null");
            Future<GrsResponse> future2 = this.taskExecutor.submit(new Callable<GrsResponse>() {
                /* class com.huawei.networkit.grs.requestremote.RequestController.AnonymousClass1 */

                @Override // java.util.concurrent.Callable
                public GrsResponse call() {
                    return new GrsRequest(grsBaseInfo).submitExcutorTaskWithTimeout(RequestController.this.taskExecutor);
                }
            });
            this.grsRequestMap.put(spUrlKey, new GrsRequestBean(future2));
            future = future2;
        }
        try {
            return future.get();
        } catch (CancellationException e) {
            Logger.w(TAG, "when check result, find CancellationException, check others", e);
            return null;
        } catch (ExecutionException e2) {
            Logger.w(TAG, "when check result, find ExecutionException, check others", e2);
            return null;
        } catch (InterruptedException e3) {
            Logger.w(TAG, "when check result, find InterruptedException, check others", e3);
            return null;
        }
    }

    public void getAyncServicesUrls(final GrsBaseInfo grsBaseInfo, final GrsCallBack grsCallBack) {
        this.taskExecutor.submit(new Runnable() {
            /* class com.huawei.networkit.grs.requestremote.RequestController.AnonymousClass2 */

            @Override // java.lang.Runnable
            public void run() {
                RequestController requestController = RequestController.this;
                requestController.doCallBack(requestController.getSyncServicesUrls(grsBaseInfo), grsCallBack);
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
            Logger.v(TAG, "GrsResponse is null");
            grsCallBack.onFailure();
            return;
        }
        Logger.v(TAG, "GrsResponse is not null");
        grsCallBack.onResponse(grsResponse);
    }

    public void removeCurrentRequest(String urlKey) {
        synchronized (this.lock) {
            this.grsRequestMap.remove(urlKey);
        }
    }
}
