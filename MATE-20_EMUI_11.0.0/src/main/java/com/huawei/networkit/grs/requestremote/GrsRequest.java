package com.huawei.networkit.grs.requestremote;

import android.os.SystemClock;
import android.text.TextUtils;
import com.huawei.networkit.grs.GrsBaseInfo;
import com.huawei.networkit.grs.cache.CacheManager;
import com.huawei.networkit.grs.common.Logger;
import com.huawei.networkit.grs.requestremote.base.GrsServerConfigMgr;
import com.huawei.networkit.grs.requestremote.model.GrsServerBean;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.json.JSONArray;

public class GrsRequest implements CallBack {
    private static final int DEFAULT_TIME_OUT = 10;
    private static final String EVENT_ID = "networkkit_grs";
    private static final String TAG = GrsRequest.class.getSimpleName();
    private long blockTime = 1;
    private ArrayList<Future<GrsResponse>> futures = new ArrayList<>();
    private GrsBaseInfo grsBaseInfo;
    private GrsResponse grsResponseResult;
    private GrsServerBean grsServerBean;
    private JSONArray jsonArray = new JSONArray();
    private ArrayList<GrsResponse> reportData = new ArrayList<>();
    private ArrayList<String> urls = new ArrayList<>();

    public GrsRequest(GrsBaseInfo grsBaseInfo2) {
        this.grsBaseInfo = grsBaseInfo2;
        buildRequestUrl();
    }

    public GrsServerBean getGrsServerBean() {
        return this.grsServerBean;
    }

    public void setGrsServerBean(GrsServerBean grsServerBean2) {
        this.grsServerBean = grsServerBean2;
    }

    public GrsResponse submitExcutorTaskWithTimeout(final ExecutorService taskExecutor) {
        if (this.urls == null) {
            return null;
        }
        try {
            GrsServerBean localGrsServerBean = getGrsServerBean();
            int grsQueryTimeout = localGrsServerBean != null ? localGrsServerBean.getGrsQueryTimeout() : 10;
            Logger.v(TAG, "getSyncServicesUrls grsQueryTimeout{%d}s", Integer.valueOf(grsQueryTimeout));
            return (GrsResponse) taskExecutor.submit(new Callable<GrsResponse>() {
                /* class com.huawei.networkit.grs.requestremote.GrsRequest.AnonymousClass1 */

                @Override // java.util.concurrent.Callable
                public GrsResponse call() {
                    return GrsRequest.this.submitExcutorTask(taskExecutor);
                }
            }).get((long) grsQueryTimeout, TimeUnit.SECONDS);
        } catch (CancellationException e) {
            Logger.w(TAG, "getSyncServicesUrls the computation was cancelled", e);
            return null;
        } catch (ExecutionException e2) {
            Logger.w(TAG, "getSyncServicesUrls the computation threw an ExecutionException", e2);
            return null;
        } catch (InterruptedException e3) {
            Logger.w(TAG, "getSyncServicesUrls the current thread was interrupted while waiting", e3);
            return null;
        } catch (TimeoutException e4) {
            Logger.w(TAG, "getSyncServicesUrls the wait timed out");
            return null;
        } catch (Exception e5) {
            Logger.w(TAG, "getSyncServicesUrls catch Exception", e5);
            return null;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private GrsResponse submitExcutorTask(ExecutorService taskExecutor) {
        long startTime = SystemClock.elapsedRealtime();
        int size = this.urls.size();
        GrsResponse grsResponse = null;
        int i = 0;
        while (true) {
            if (i >= size) {
                break;
            }
            boolean needBreak = false;
            String url = this.urls.get(i);
            if (!TextUtils.isEmpty(url)) {
                Future<GrsResponse> future = taskExecutor.submit(new RequestCallable(url, i, this));
                this.futures.add(future);
                try {
                    grsResponse = future.get(this.blockTime, TimeUnit.SECONDS);
                    if (grsResponse != null && grsResponse.isOK()) {
                        needBreak = true;
                    }
                } catch (CancellationException e) {
                    Logger.w(TAG, "the computation was cancelled", e);
                    needBreak = true;
                } catch (ExecutionException e2) {
                    Logger.w(TAG, "the computation threw an ExecutionException", e2);
                } catch (InterruptedException e3) {
                    Logger.w(TAG, "the current thread was interrupted while waiting", e3);
                    needBreak = true;
                } catch (TimeoutException e4) {
                    Logger.w(TAG, "the wait timed out");
                }
            }
            if (needBreak) {
                Logger.v(TAG, "needBreak is true so need break current circulation");
                break;
            }
            i++;
        }
        GrsResponse grsResponse2 = checkResponse(grsResponse);
        HaReportHelper.report(this.reportData, SystemClock.elapsedRealtime() - startTime, this.jsonArray);
        return grsResponse2;
    }

    private GrsResponse checkResponse(GrsResponse grsResponse) {
        int futureSize = this.futures.size();
        for (int i = 0; i < futureSize && (grsResponse == null || !grsResponse.isOK()); i++) {
            try {
                grsResponse = this.futures.get(i).get(40000, TimeUnit.MILLISECONDS);
            } catch (CancellationException e) {
                Logger.w(TAG, "when check result, find CancellationException, check others", e);
            } catch (ExecutionException e2) {
                Logger.w(TAG, "when check result, find ExecutionException, check others", e2);
            } catch (InterruptedException e3) {
                Logger.w(TAG, "when check result, find InterruptedException, check others", e3);
            } catch (TimeoutException e4) {
                Logger.w(TAG, "when check result, find TimeoutException, cancel current request task");
                if (!this.futures.get(i).isCancelled()) {
                    this.futures.get(i).cancel(true);
                }
            }
        }
        return grsResponse;
    }

    private void buildRequestUrl() {
        GrsServerBean localGrsServerBean = GrsServerConfigMgr.getGrsServerBean();
        if (localGrsServerBean == null) {
            Logger.w(TAG, "g*s***_se****er_conf*** maybe has a big error");
            return;
        }
        setGrsServerBean(localGrsServerBean);
        List<String> grsServerBaseUrls = localGrsServerBean.getGrsBaseUrl();
        if (grsServerBaseUrls == null || grsServerBaseUrls.size() <= 0) {
            Logger.v(TAG, "maybe grs_base_url config with [],please check.");
        } else if (grsServerBaseUrls.size() <= 10) {
            String grsQueryEndpoint = localGrsServerBean.getGrsQueryEndpoint();
            if (grsServerBaseUrls.size() > 0) {
                for (String baseUrl : grsServerBaseUrls) {
                    if (!baseUrl.startsWith("https://")) {
                        Logger.w(TAG, "grs server just support https scheme url,please check.");
                    } else {
                        StringBuilder sb = new StringBuilder();
                        sb.append(baseUrl);
                        sb.append(String.format(Locale.ROOT, grsQueryEndpoint, this.grsBaseInfo.getAppName()));
                        String reqParam = this.grsBaseInfo.getGrsReqParamJoint(false, false);
                        if (!TextUtils.isEmpty(reqParam)) {
                            sb.append("?");
                            sb.append(reqParam);
                        }
                        this.urls.add(sb.toString());
                    }
                }
            }
        } else {
            throw new IllegalArgumentException("grs_base_url's count is larger than MAX value 10");
        }
    }

    @Override // com.huawei.networkit.grs.requestremote.CallBack
    public synchronized void onResponse(GrsResponse response) {
        this.reportData.add(response);
        if (this.grsResponseResult != null && this.grsResponseResult.isOK()) {
            Logger.v(TAG, "grsResponseResult is ok");
        } else if (!response.isOK()) {
            Logger.v(TAG, "grsResponseResult has exception so need return");
        } else {
            this.grsResponseResult = response;
            CacheManager.updateCacheFromServer(this.grsBaseInfo, this.grsResponseResult);
            for (int i = 0; i < this.futures.size(); i++) {
                if (!this.urls.get(i).equals(response.getUrl()) && !this.futures.get(i).isCancelled()) {
                    Logger.v(TAG, "future cancel");
                    this.futures.get(i).cancel(true);
                }
            }
        }
    }
}
