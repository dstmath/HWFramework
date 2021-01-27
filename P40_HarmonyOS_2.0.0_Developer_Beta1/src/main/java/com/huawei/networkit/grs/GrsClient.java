package com.huawei.networkit.grs;

import android.content.Context;
import android.text.TextUtils;
import com.huawei.networkit.grs.cache.CacheManager;
import com.huawei.networkit.grs.cache.GrsPreferences;
import com.huawei.networkit.grs.common.Logger;
import com.huawei.networkit.grs.local.LocalManagerV1;
import com.huawei.networkit.grs.local.LocalManagerV2;
import com.huawei.networkit.grs.utils.ContextUtil;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class GrsClient {
    private static final int DEFAULT_TIME_OUT = 10;
    private static final long REQUEST_BLOCK_TIME = 604800000;
    private static final String SPKEY_UNION_SUFFIX = "time";
    private static final String TAG = GrsClient.class.getSimpleName();
    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private Future<Boolean> future;
    private GrsBaseInfo grsBaseInfo;
    private boolean isInit = false;
    private final Object lock = new Object();

    public GrsClient(Context context, GrsBaseInfo grsBaseInfoParam) {
        if (context == null || grsBaseInfoParam == null || TextUtils.isEmpty(grsBaseInfoParam.getAppName())) {
            throw new NullPointerException("invalid init params for context is null or {appname} set null or empty string.");
        }
        ContextUtil.setContext(context.getApplicationContext());
        try {
            this.grsBaseInfo = grsBaseInfoParam.clone();
        } catch (CloneNotSupportedException e) {
            Logger.w(TAG, "GrsClient catch CloneNotSupportedException", e);
            this.grsBaseInfo = grsBaseInfoParam.copy();
        }
        if (!this.isInit) {
            synchronized (this.lock) {
                if (!this.isInit) {
                    final GrsBaseInfo grsBaseInfoLocal = this.grsBaseInfo;
                    this.future = this.executorService.submit(new Callable<Boolean>() {
                        /* class com.huawei.networkit.grs.GrsClient.AnonymousClass1 */

                        @Override // java.util.concurrent.Callable
                        public Boolean call() throws Exception {
                            if (!LocalManagerV2.getLocalManagerV2().updateCountryGroupMap(grsBaseInfoLocal)) {
                                LocalManagerV1.getLocalManager().updateCountryGroupMap(grsBaseInfoLocal);
                            }
                            GrsClient.this.autoClear(GrsPreferences.getInstance().getAll());
                            CacheManager.initCache(grsBaseInfoLocal);
                            return Boolean.valueOf(GrsClient.this.isInit = true);
                        }
                    });
                }
            }
        }
    }

    private boolean isValid(long timeStamp) {
        return System.currentTimeMillis() - timeStamp <= REQUEST_BLOCK_TIME;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void autoClear(Map<String, ?> spMap) {
        if (spMap == null || spMap.isEmpty()) {
            Logger.v(TAG, "sp's content is empty.");
            return;
        }
        for (String timeKey : spMap.keySet()) {
            if (timeKey.endsWith(SPKEY_UNION_SUFFIX)) {
                String time = GrsPreferences.getInstance().getString(timeKey, "");
                long last = 0;
                if (!TextUtils.isEmpty(time) && time.matches("\\d+")) {
                    try {
                        last = Long.parseLong(time);
                    } catch (NumberFormatException e) {
                        Logger.w(TAG, "convert expire time from String to Long catch NumberFormatException.", e);
                        last = 0;
                    }
                }
                if (!isValid(last)) {
                    Logger.i(TAG, "init interface auto clear some invalid sp's data.");
                    GrsPreferences.getInstance().removeKeyValue(timeKey.substring(0, timeKey.length() - SPKEY_UNION_SUFFIX.length()));
                    GrsPreferences.getInstance().removeKeyValue(timeKey);
                }
            }
        }
    }

    private boolean isInitialized() {
        try {
            if (this.future != null) {
                return this.future.get(10, TimeUnit.SECONDS).booleanValue();
            }
            return false;
        } catch (CancellationException e) {
            Logger.w(TAG, "init compute task canceled.", e);
            return false;
        } catch (ExecutionException e2) {
            Logger.w(TAG, "init compute task failed.", e2);
            return false;
        } catch (InterruptedException e3) {
            Logger.w(TAG, "init compute task interrupted.", e3);
            return false;
        } catch (TimeoutException e4) {
            Logger.w(TAG, "init compute task timed out");
            return false;
        } catch (Exception e5) {
            Logger.w(TAG, "init compute task occur unknown Exception", e5);
            return false;
        }
    }

    public String synGetGrsUrl(String serviceName, String key) {
        if (this.grsBaseInfo == null || serviceName == null || key == null) {
            Logger.w(TAG, "invalid para!");
            return null;
        } else if (isInitialized()) {
            return new GrsApiManager(this.grsBaseInfo).synGetGrsUrl(serviceName, key);
        } else {
            return null;
        }
    }

    public Map<String, String> synGetGrsUrls(String serviceName) {
        if (this.grsBaseInfo == null || serviceName == null) {
            Logger.w(TAG, "invalid para!");
            return new HashMap();
        } else if (isInitialized()) {
            return new GrsApiManager(this.grsBaseInfo).synGetGrsUrls(serviceName);
        } else {
            return new HashMap();
        }
    }

    public void ayncGetGrsUrl(String serviceName, String key, IQueryUrlCallBack callBack) {
        if (callBack == null) {
            Logger.w(TAG, "IQueryUrlCallBack is must not null for process continue.");
        } else if (this.grsBaseInfo == null || serviceName == null || key == null) {
            callBack.onCallBackFail(-6);
        } else if (isInitialized()) {
            new GrsApiManager(this.grsBaseInfo).ayncGetGrsUrl(serviceName, key, callBack);
        }
    }

    public void ayncGetGrsUrls(String serviceName, IQueryUrlsCallBack callBack) {
        if (callBack == null) {
            Logger.w(TAG, "IQueryUrlsCallBack is must not null for process continue.");
        } else if (this.grsBaseInfo == null || serviceName == null) {
            callBack.onCallBackFail(-6);
        } else if (isInitialized()) {
            new GrsApiManager(this.grsBaseInfo).ayncGetGrsUrls(serviceName, callBack);
        }
    }

    public boolean forceExpire() {
        if (!isInitialized() || this.grsBaseInfo == null || ContextUtil.getContext() == null) {
            return false;
        }
        CacheManager.forceExpire(this.grsBaseInfo);
        return true;
    }
}
