package com.huawei.wallet.sdk.business.bankcard.manager;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import com.huawei.wallet.sdk.business.bankcard.api.BankCardServerApi;
import com.huawei.wallet.sdk.business.bankcard.api.CUPCardOperatorApi;
import com.huawei.wallet.sdk.business.bankcard.api.CUPOperationListener;
import com.huawei.wallet.sdk.business.bankcard.api.CUPService;
import com.huawei.wallet.sdk.business.bankcard.api.CardOperateListener;
import com.huawei.wallet.sdk.business.bankcard.constant.BankcardConstant;
import com.huawei.wallet.sdk.business.bankcard.server.BankCardServer;
import com.huawei.wallet.sdk.business.bankcard.server.CUPServiceImp;
import com.huawei.wallet.sdk.business.bankcard.task.HandleCardOperateBaseTask;
import com.huawei.wallet.sdk.business.bankcard.task.HandleCardSwipeTask;
import com.huawei.wallet.sdk.business.bankcard.task.HandleOperationResultTask;
import com.huawei.wallet.sdk.business.bankcard.util.BankCardStatusUtil;
import com.huawei.wallet.sdk.common.apdu.whitecard.WalletProcessTrace;
import com.huawei.wallet.sdk.common.log.LogC;
import com.huawei.wallet.sdk.common.ta.TACardInfo;
import com.huawei.wallet.sdk.common.ta.WalletTaManager;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BankSPIOperatorManager {
    private CUPCardOperator cupCardOperator;
    private final Object lock = new Object();
    private final Context mContext;
    private final Handler operateHandler;

    public static class CUPCardOperator extends WalletProcessTrace implements CardOperateListener, CUPCardOperatorApi {
        private static final int CHECK_INTERRUPTED_DELAY_TIME = 60000;
        private static final Object LISTENERS_LOCK = new Object();
        private static final String TAG = "CUPCardOperator|";
        private Handler cardModifyHandler;
        private final BankCardServerApi cardServerApi = new BankCardServer(this.mContext);
        /* access modifiers changed from: private */
        public final CUPService cupServiceApi = new CUPServiceImp(this.mContext);
        private Map<String, CUPOperationListener> deleteListeners;
        private Map<String, CUPOperationListener> downloadListeners;
        private final Context mContext;
        private final Handler operateHandler;
        private Map<String, CUPOperationListener> personalizedListeners;
        private List<CUPOperationListener> wipeoutListeners;

        public CUPCardOperator(Context context, Handler prepareHandler) {
            this.mContext = context.getApplicationContext();
            this.operateHandler = prepareHandler;
        }

        public void notifyCUPCardOperation(String event, String ssid, String sign, List<String> refIds, HandleOperationResultTask resulthandleTask, String source) {
            synchronized (LISTENERS_LOCK) {
                LogC.i(getSubProcessPrefix() + "notifyCUPCardOperation, post " + event + " task to operateHandler.", false);
                if (this.cardModifyHandler == null) {
                    HandlerThread prepareThread = new HandlerThread("cup_card_modify_thread");
                    prepareThread.start();
                    this.cardModifyHandler = new Handler(prepareThread.getLooper());
                }
                HandleCardOperateBaseTask operateTask = null;
                if (BankcardConstant.OPERATE_EVENT_WIPEOUT.equals(event)) {
                    operateTask = new HandleCardSwipeTask(this.mContext, this.cupServiceApi, resulthandleTask, this);
                } else {
                    LogC.e(getSubProcessPrefix() + "notifyCardOperation, illegal event.", false);
                }
                if (operateTask != null) {
                    operateTask.setProcessPrefix(getProcessPrefix(), null);
                    operateTask.doTask(ssid, sign, refIds, this.cardModifyHandler);
                }
            }
        }

        public void registerOperationListener(String event, String refId, CUPOperationListener listener) {
            synchronized (LISTENERS_LOCK) {
                if (BankcardConstant.OPERATE_EVENT_DOWNLOAD.equals(event)) {
                    if (this.downloadListeners == null) {
                        this.downloadListeners = new HashMap();
                    }
                    this.downloadListeners.put(refId, listener);
                } else if (BankcardConstant.OPERATE_EVENT_DELETE.equals(event)) {
                    if (this.deleteListeners == null) {
                        this.deleteListeners = new HashMap();
                    }
                    this.deleteListeners.put(refId, listener);
                } else if (BankcardConstant.OPERATE_EVENT_WIPEOUT.equals(event)) {
                    if (this.wipeoutListeners == null) {
                        this.wipeoutListeners = new ArrayList();
                    }
                    this.wipeoutListeners.add(listener);
                } else if (BankcardConstant.OPERATE_EVENT_PERSONALIZED.equals(event)) {
                    if (this.personalizedListeners == null) {
                        this.personalizedListeners = new HashMap();
                    }
                    this.personalizedListeners.put(refId, listener);
                } else {
                    LogC.e("registerOperationListener, illegal event.", false);
                }
            }
        }

        public void unregisterOperationListener(String event, String refId, CUPOperationListener listener) {
            synchronized (LISTENERS_LOCK) {
                if (BankcardConstant.OPERATE_EVENT_DOWNLOAD.equals(event) && this.downloadListeners != null) {
                    this.downloadListeners.remove(refId);
                } else if (BankcardConstant.OPERATE_EVENT_DELETE.equals(event) && this.deleteListeners != null) {
                    this.deleteListeners.remove(refId);
                } else if (!BankcardConstant.OPERATE_EVENT_WIPEOUT.equals(event) || this.wipeoutListeners == null) {
                    if (!BankcardConstant.OPERATE_EVENT_PERSONALIZED.equals(event) || this.personalizedListeners == null) {
                        LogC.e("unregisterOperationListener, illegal event.", false);
                    } else if (listener != null) {
                        this.personalizedListeners.remove(refId);
                    }
                } else if (listener != null) {
                    this.wipeoutListeners.remove(listener);
                }
            }
        }

        public void operateStart(String event, String refId) {
            synchronized (LISTENERS_LOCK) {
                checkOperateEvent(event, true);
                Collection<CUPOperationListener> tempCupOperationListeners = null;
                if (BankcardConstant.OPERATE_EVENT_DOWNLOAD.equals(event) && this.downloadListeners != null) {
                    CUPOperationListener listener = this.downloadListeners.get(refId);
                    if (listener != null) {
                        listener.operateStart(BankcardConstant.OPERATE_EVENT_DOWNLOAD);
                    }
                } else if (BankcardConstant.OPERATE_EVENT_DELETE.equals(event) && this.deleteListeners != null) {
                    CUPOperationListener listener2 = this.deleteListeners.get(refId);
                    if (listener2 != null) {
                        listener2.operateStart(BankcardConstant.OPERATE_EVENT_DELETE);
                    }
                } else if (BankcardConstant.OPERATE_EVENT_PERSONALIZED.equals(event) && this.personalizedListeners != null) {
                    CUPOperationListener listener3 = this.personalizedListeners.get(refId);
                    if (listener3 != null) {
                        listener3.operateStart(BankcardConstant.OPERATE_EVENT_PERSONALIZED);
                    }
                } else if (!BankcardConstant.OPERATE_EVENT_WIPEOUT.equals(event) || this.wipeoutListeners == null) {
                    LogC.e("operateStart, illegal event or empty listeners.", false);
                } else {
                    tempCupOperationListeners = this.wipeoutListeners;
                }
                Collection<CUPOperationListener> cupOperationListers = getCupOperationListeners(tempCupOperationListeners);
                if (cupOperationListers != null) {
                    notifyWipeoutStart(cupOperationListers);
                }
            }
        }

        private void checkOperateEvent(String event, boolean excuteStatus) {
            if (BankcardConstant.OPERATE_EVENT_DOWNLOAD.equals(event) || BankcardConstant.OPERATE_EVENT_DELETE.equals(event) || BankcardConstant.OPERATE_EVENT_WIPEOUT.equals(event)) {
                BankCardStatusUtil.getInstance().setExecutingCMD(excuteStatus);
            }
        }

        private Collection<CUPOperationListener> getCupOperationListeners(Collection<CUPOperationListener> tempCupOperationListeners) {
            if (tempCupOperationListeners == null) {
                LogC.d("getCupOperationListeners, but no listeners.", false);
                return null;
            }
            Collection<CUPOperationListener> cupOperationListers = new ArrayList<>();
            for (CUPOperationListener cupOperationListener : tempCupOperationListeners) {
                if (cupOperationListener != null) {
                    cupOperationListers.add(cupOperationListener);
                }
            }
            return cupOperationListers;
        }

        private void notifyWipeoutStart(Collection<CUPOperationListener> cupOperationListers) {
            for (CUPOperationListener listener : cupOperationListers) {
                if (listener != null) {
                    listener.operateStart(BankcardConstant.OPERATE_EVENT_WIPEOUT);
                }
            }
        }

        public void operateFinished(String event, String refId, int resultCode) {
            synchronized (LISTENERS_LOCK) {
                checkOperateEvent(event, false);
                Collection<CUPOperationListener> tempCupOperationListners = null;
                if (BankcardConstant.OPERATE_EVENT_DOWNLOAD.equals(event) && this.downloadListeners != null) {
                    CUPOperationListener listener = this.downloadListeners.get(refId);
                    if (listener != null) {
                        listener.operateFinished(resultCode, BankcardConstant.OPERATE_EVENT_DOWNLOAD);
                    }
                } else if (BankcardConstant.OPERATE_EVENT_DELETE.equals(event) && this.deleteListeners != null) {
                    CUPOperationListener listener2 = this.deleteListeners.get(refId);
                    if (listener2 != null) {
                        listener2.operateFinished(resultCode, BankcardConstant.OPERATE_EVENT_DELETE);
                    }
                } else if (BankcardConstant.OPERATE_EVENT_PERSONALIZED.equals(event) && this.personalizedListeners != null) {
                    CUPOperationListener listener3 = this.personalizedListeners.get(refId);
                    if (listener3 != null) {
                        listener3.operateFinished(resultCode, BankcardConstant.OPERATE_EVENT_PERSONALIZED);
                    }
                } else if (!BankcardConstant.OPERATE_EVENT_WIPEOUT.equals(event) || this.wipeoutListeners == null) {
                    LogC.e("operateFinished, illegal event or listener is null.", false);
                } else {
                    tempCupOperationListners = this.wipeoutListeners;
                }
                Collection<CUPOperationListener> cupOperationListers = getCupOperationListeners(tempCupOperationListners);
                if (cupOperationListers != null) {
                    notifyWipeoutResult(resultCode, cupOperationListers);
                }
            }
        }

        private void notifyWipeoutResult(int resultCode, Collection<CUPOperationListener> cupOperationListers) {
            for (CUPOperationListener listener : cupOperationListers) {
                if (listener != null) {
                    listener.operateFinished(resultCode, BankcardConstant.OPERATE_EVENT_WIPEOUT);
                }
            }
        }

        public void notifyCardState() {
            synchronized (LISTENERS_LOCK) {
                LogC.i(getSubProcessPrefix() + "Post notifyCardState task to cardModifyHandler.", false);
                if (this.cardModifyHandler == null) {
                    HandlerThread prepareThread = new HandlerThread("cup_card_modify_thread");
                    prepareThread.start();
                    this.cardModifyHandler = new Handler(prepareThread.getLooper());
                }
                this.cardModifyHandler.post(new Runnable() {
                    public void run() {
                        CUPCardOperator.this.cupServiceApi.setProcessPrefix(CUPCardOperator.this.getProcessPrefix(), null);
                        CUPCardOperator.this.cupServiceApi.notifyCardState();
                        CUPCardOperator.this.cupServiceApi.resetProcessPrefix();
                    }
                });
            }
        }

        public void setProcessPrefix(String processPrefix, String tag) {
            super.setProcessPrefix(processPrefix, TAG);
        }
    }

    public BankSPIOperatorManager(Context context, Handler handler) {
        this.mContext = context;
        this.operateHandler = handler;
    }

    public CUPCardOperator getCUPOperator() {
        CUPCardOperator cUPCardOperator;
        synchronized (this.lock) {
            if (this.cupCardOperator == null) {
                this.cupCardOperator = new CUPCardOperator(this.mContext, this.operateHandler);
            }
            cUPCardOperator = this.cupCardOperator;
        }
        return cUPCardOperator;
    }

    public boolean isCiticMode(int mode, String refid) {
        boolean z = false;
        if (mode != 11) {
            return false;
        }
        TACardInfo taCardInfo = WalletTaManager.getInstance(this.mContext).getCard(refid);
        if (taCardInfo != null && "A0000003330101020063020000000301".equals(taCardInfo.getAid())) {
            z = true;
        }
        return z;
    }
}
