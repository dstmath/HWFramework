package com.huawei.wallet.sdk.common.utils;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import com.huawei.wallet.sdk.business.bankcard.modle.IssuerInfoItem;
import com.huawei.wallet.sdk.business.bankcard.util.Router;
import com.huawei.wallet.sdk.business.idcard.walletbase.util.LogX;
import com.huawei.wallet.sdk.common.apdu.model.ServerAccessAPDU;
import com.huawei.wallet.sdk.common.apdu.response.ServerAccessBaseResponse;
import com.huawei.wallet.sdk.common.buscard.PollTimeOutException;
import com.huawei.wallet.sdk.common.buscard.response.ServerAccessQueryOrderResultResponse;
import com.huawei.wallet.sdk.common.http.response.BaseResponse;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class PollingOperate {
    private static final int MAX_POLLING_TIME = 90;
    private static final int MSG_MSG_KEY = 0;
    private static final String TRANSFER_OUT_METHOD = "cloudTransferOut";
    private static volatile PollingOperate instance;
    private static final Object sLock = new Object();
    private String clsName = getClass().getSimpleName();
    private Handler handler;
    private HandlerThread handlerThread = new HandlerThread("polling_operate");
    private int intervalTime = 0;
    private volatile boolean isGetApdus = false;
    private boolean isPolling = false;
    private volatile boolean isRunning = false;
    private boolean isSupportPolling = false;
    private IssuerInfoItem issueInfo;
    private AtomicInteger requestDuration = new AtomicInteger(0);
    private AtomicInteger requestTimes = new AtomicInteger(0);
    private volatile boolean runfindBugs = true;

    private PollingOperate() {
        this.handlerThread.start();
        this.handler = new Handler(this.handlerThread.getLooper()) {
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what == 0) {
                    PollingOperate.this.wakeUp();
                }
            }
        };
    }

    public static PollingOperate getInstance() {
        if (instance == null) {
            synchronized (sLock) {
                if (instance == null) {
                    instance = new PollingOperate();
                }
            }
        }
        return instance;
    }

    public Object excute(Context context, Object targetObj, Method method, Object[] args) throws PollTimeOutException {
        this.isRunning = true;
        setRequestParams(args);
        Object object = null;
        try {
            object = method.invoke(targetObj, args);
            this.isSupportPolling = supportPolling(context, args, method);
            while (true) {
                if (this.requestDuration.get() >= MAX_POLLING_TIME || !needPolling(object) || !this.isSupportPolling) {
                    break;
                }
                this.requestTimes.incrementAndGet();
                this.requestDuration.addAndGet(this.intervalTime);
                if (this.intervalTime <= 0) {
                    LogX.i(this.clsName + " not receive times break", false);
                    break;
                }
                waitForWhile(this.intervalTime);
                setRequestParams(args);
                object = method.invoke(targetObj, args);
            }
            if (this.isPolling && this.isSupportPolling) {
                if (this.requestDuration.get() >= MAX_POLLING_TIME) {
                    LogX.i(this.clsName + " polling time out intervalTime=" + this.intervalTime, false);
                    throw new PollTimeOutException(BaseResponse.RESPONSE_CODE_RESULT_TIME_OUT);
                }
            }
        } catch (IllegalAccessException e) {
            LogX.e(this.clsName + " proxy " + method.getName() + " illeagel", false);
        } catch (InvocationTargetException e2) {
            LogX.e(this.clsName + "proxy " + method.getName() + " invocation", false);
        } catch (Throwable th) {
            this.requestTimes.set(0);
            this.requestDuration.set(0);
            this.intervalTime = 0;
            throw th;
        }
        this.requestTimes.set(0);
        this.requestDuration.set(0);
        this.intervalTime = 0;
        this.isRunning = false;
        return object;
    }

    public boolean isRunning() {
        return this.isRunning;
    }

    public void setRequestParams(Object[] args) {
        args[0].setRequestTimes(this.requestTimes.intValue());
    }

    public boolean supportPolling(Context context, Object[] args, Method method) {
        this.issueInfo = Router.getCardAndIssuerInfoCacheApi(context).cacheIssuerInfoItem(args[0].getIssuerId());
        if (!this.issueInfo.isSupportSyncPush(context) && !this.issueInfo.getIssuerId().equals("t_sh_01")) {
            return false;
        }
        return true;
    }

    public boolean needPolling(Object object) {
        this.isPolling = false;
        if (object == null) {
            return true;
        }
        if (object instanceof ServerAccessQueryOrderResultResponse) {
            ServerAccessQueryOrderResultResponse res = (ServerAccessQueryOrderResultResponse) object;
            if (res.returnCode != 0) {
                this.isPolling = false;
            } else if (res.getResult() == 2) {
                this.isPolling = true;
            } else {
                this.isPolling = false;
            }
            this.intervalTime = res.getInvokeIntervalTime();
        } else if (object instanceof ServerAccessBaseResponse) {
            ServerAccessBaseResponse res2 = (ServerAccessBaseResponse) object;
            if (res2.returnCode == 0) {
                List<ServerAccessAPDU> apduList = res2.getApduList();
                if (apduList == null || apduList.isEmpty()) {
                    this.isPolling = true;
                    this.isGetApdus = false;
                } else {
                    this.isGetApdus = true;
                    this.isPolling = false;
                }
            } else {
                this.isGetApdus = false;
                this.isPolling = false;
            }
            this.intervalTime = res2.getInvokeIntervalTime();
        }
        return this.isPolling;
    }

    public boolean isGetApdus() {
        return this.isGetApdus;
    }

    private void waitForWhile(int time) {
        try {
            LogX.i(this.clsName + " wait for + " + time + " seconds", false);
            synchronized (sLock) {
                this.runfindBugs = true;
                this.handler.sendEmptyMessageDelayed(0, 1000 * ((long) time));
                while (this.runfindBugs) {
                    sLock.wait();
                }
            }
            LogX.i(this.clsName + " auto awake", false);
        } catch (InterruptedException e) {
            LogX.e(this.clsName + " interupted", false);
        }
    }

    public synchronized void wakeUp() {
        synchronized (sLock) {
            LogX.i(this.clsName + " notify", false);
            this.runfindBugs = false;
            sLock.notifyAll();
        }
    }
}
