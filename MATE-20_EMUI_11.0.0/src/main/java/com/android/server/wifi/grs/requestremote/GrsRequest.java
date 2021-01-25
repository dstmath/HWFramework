package com.android.server.wifi.grs.requestremote;

import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import com.android.server.wifi.grs.requestremote.base.GrsServerConfigMgr;
import com.android.server.wifi.grs.requestremote.model.GrsServerBean;
import com.android.server.wifipro.WifiProCommonUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class GrsRequest implements CallBack {
    private static final long BLOCK_TIME = 1;
    private static final String COUNTRY_CODE_CN = "460";
    private static final int DEFAULT_TIME_OUT = 10;
    private static final int EXPECTED_BUFFER_LEN = 70;
    private static final String KEYWORD_CN = "CN";
    private static final String KEYWORD_DE = "DE";
    private static final int MAX_GRSURL_COUNT = 10;
    private static final int RESPONSE_TIMEOUT = 40000;
    private static final String TAG = GrsRequest.class.getSimpleName();
    private ArrayList<Future<GrsResponse>> mFutures = new ArrayList<>();
    private GrsResponse mGrsResponseResult;
    private GrsServerBean mGrsServerBean;
    private ArrayList<String> mUrls = new ArrayList<>();

    public GrsRequest() {
        buildRequestUrl();
    }

    public GrsServerBean getGrsServerBean() {
        return this.mGrsServerBean;
    }

    public void setGrsServerBean(GrsServerBean grsServerBean) {
        this.mGrsServerBean = grsServerBean;
    }

    public GrsResponse submitExcutorTaskWithTimeout(final ExecutorService taskExecutor) {
        if (this.mUrls == null) {
            return null;
        }
        try {
            GrsServerBean grsServerBean = getGrsServerBean();
            int grsQueryTimeout = grsServerBean != null ? grsServerBean.getGrsQueryTimeout() : 10;
            String str = TAG;
            Log.d(str, "getSyncServicesUrls grsQueryTimeout{" + grsQueryTimeout + "}s");
            return (GrsResponse) taskExecutor.submit(new Callable<GrsResponse>() {
                /* class com.android.server.wifi.grs.requestremote.GrsRequest.AnonymousClass1 */

                @Override // java.util.concurrent.Callable
                public GrsResponse call() {
                    return GrsRequest.this.submitExcutorTask(taskExecutor);
                }
            }).get((long) grsQueryTimeout, TimeUnit.SECONDS);
        } catch (CancellationException e) {
            Log.d(TAG, "getSyncServicesUrls the computation was cancelled");
            return null;
        } catch (ExecutionException e2) {
            Log.d(TAG, "getSyncServicesUrls the computation threw an ExecutionException");
            return null;
        } catch (InterruptedException e3) {
            Log.d(TAG, "getSyncServicesUrls the current thread was interrupted while waiting");
            return null;
        } catch (TimeoutException e4) {
            Log.d(TAG, "getSyncServicesUrls the wait timed out");
            return null;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private GrsResponse submitExcutorTask(ExecutorService taskExecutor) {
        int size = this.mUrls.size();
        GrsResponse grsResponse = null;
        int i = 0;
        while (true) {
            if (i >= size) {
                break;
            }
            boolean isNeedBreak = false;
            String url = this.mUrls.get(i);
            if (!TextUtils.isEmpty(url)) {
                String str = TAG;
                Log.d(str, "request url = " + url);
                Future<GrsResponse> future = taskExecutor.submit(new RequestCallable(url, i, this));
                this.mFutures.add(future);
                try {
                    grsResponse = future.get(BLOCK_TIME, TimeUnit.SECONDS);
                    if (grsResponse != null && grsResponse.isOk()) {
                        isNeedBreak = true;
                    }
                } catch (CancellationException e) {
                    Log.d(TAG, "the computation was cancelled");
                    isNeedBreak = true;
                } catch (ExecutionException e2) {
                    Log.d(TAG, "the computation threw an ExecutionException");
                } catch (InterruptedException e3) {
                    Log.d(TAG, "the current thread was interrupted while waiting");
                    isNeedBreak = true;
                } catch (TimeoutException e4) {
                    Log.d(TAG, "the wait timed out");
                }
            }
            if (isNeedBreak) {
                Log.d(TAG, "isNeedBreak is true so need break and continue next task");
                break;
            }
            i++;
        }
        return checkResponse(grsResponse);
    }

    private GrsResponse checkResponse(GrsResponse grsResponse) {
        GrsResponse resp = grsResponse;
        int futureSize = this.mFutures.size();
        for (int i = 0; i < futureSize && (resp == null || !resp.isOk()); i++) {
            try {
                resp = this.mFutures.get(i).get(40000, TimeUnit.MILLISECONDS);
            } catch (CancellationException e) {
                Log.d(TAG, "when check result, find CancellationException, check others");
            } catch (ExecutionException e2) {
                Log.d(TAG, "when check result, find ExecutionException, check others");
            } catch (InterruptedException e3) {
                Log.d(TAG, "when check result, find InterruptedException, check others");
            } catch (TimeoutException e4) {
                Log.d(TAG, "when check result, find TimeoutException, cancel current request task");
                if (!this.mFutures.get(i).isCancelled()) {
                    this.mFutures.get(i).cancel(true);
                }
            }
        }
        return resp;
    }

    private void buildRequestUrl() {
        String serCountry;
        GrsServerBean grsServerBean = GrsServerConfigMgr.getGrsServerBean();
        if (grsServerBean == null) {
            Log.e(TAG, "a big error may happen in g*s***_se****er_conf***");
            return;
        }
        setGrsServerBean(grsServerBean);
        List<String> grsServerBaseUrls = grsServerBean.getGrsBaseUrl();
        if (grsServerBaseUrls.size() <= 10) {
            String grsQueryEndpoint = grsServerBean.getGrsQueryEndpoint();
            if (grsServerBaseUrls.size() > 0) {
                for (String baseUrl : grsServerBaseUrls) {
                    StringBuilder sb = new StringBuilder((int) EXPECTED_BUFFER_LEN);
                    sb.append(baseUrl);
                    sb.append(grsQueryEndpoint);
                    if (isOverSea()) {
                        serCountry = KEYWORD_DE;
                    } else {
                        serCountry = KEYWORD_CN;
                    }
                    if (!TextUtils.isEmpty(serCountry)) {
                        sb.append(serCountry);
                    }
                    this.mUrls.add(sb.toString());
                }
                return;
            }
            return;
        }
        throw new IllegalArgumentException("grs_base_url's count is larger than MAX value 10");
    }

    private boolean isOverSea() {
        String operator = TelephonyManager.getDefault().getNetworkOperator();
        if (operator == null || operator.length() <= 0) {
            if (KEYWORD_CN.equalsIgnoreCase(WifiProCommonUtils.getProductLocale())) {
                return false;
            }
            return true;
        } else if (operator.startsWith(COUNTRY_CODE_CN)) {
            return false;
        } else {
            return true;
        }
    }

    @Override // com.android.server.wifi.grs.requestremote.CallBack
    public synchronized void onResponse(GrsResponse response) {
        if (this.mGrsResponseResult != null && this.mGrsResponseResult.isOk()) {
            Log.d(TAG, "grsResponseResult is ok");
        } else if (!response.isOk()) {
            Log.d(TAG, "grsResponseResult has exception so need return");
        } else {
            this.mGrsResponseResult = response;
            for (int i = 0; i < this.mFutures.size(); i++) {
                if (!this.mUrls.get(i).equals(response.getUrl()) && !this.mFutures.get(i).isCancelled()) {
                    Log.d(TAG, "future cancel");
                    this.mFutures.get(i).cancel(true);
                }
            }
        }
    }
}
