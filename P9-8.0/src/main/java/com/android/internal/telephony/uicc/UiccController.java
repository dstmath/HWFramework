package com.android.internal.telephony.uicc;

import android.content.Context;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.os.Registrant;
import android.os.RegistrantList;
import android.os.SystemProperties;
import android.telephony.Rlog;
import android.telephony.TelephonyManager;
import android.text.format.Time;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.CommandsInterface.RadioState;
import com.android.internal.telephony.HwModemCapability;
import com.android.internal.telephony.HwTelephonyFactory;
import com.android.internal.telephony.uicc.IccCardStatus.CardState;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.LinkedList;

public class UiccController extends AbstractUiccController {
    public static final int APP_FAM_3GPP = 1;
    public static final int APP_FAM_3GPP2 = 2;
    public static final int APP_FAM_IMS = 3;
    private static final boolean DBG = true;
    private static final int EVENT_BALONG_MODEM_RESET = 5;
    private static final int EVENT_GET_ICC_STATUS_DONE = 2;
    private static final int EVENT_ICC_STATUS_CHANGED = 1;
    private static final int EVENT_RADIO_UNAVAILABLE = 3;
    private static final int EVENT_SIM_REFRESH = 4;
    public static final boolean IS_QUICK_BROADCAST_STATUS = SystemProperties.getBoolean("ro.quick_broadcast_cardstatus", false);
    private static final String LOG_TAG = "UiccController";
    private static final int MAX_PROACTIVE_COMMANDS_TO_LOG = 20;
    private static UiccController mInstance;
    private static UiccStateChangedLauncher mLauncher;
    private static final Object mLock = new Object();
    private LinkedList<String> mCardLogs = new LinkedList();
    private CommandsInterface[] mCis;
    private Context mContext;
    protected RegistrantList mIccChangedRegistrants = new RegistrantList();
    private UiccCard[] mUiccCards = new UiccCard[TelephonyManager.getDefault().getPhoneCount()];

    public static UiccController make(Context c, CommandsInterface[] ci) {
        UiccController uiccController;
        synchronized (mLock) {
            if (mInstance != null) {
                throw new RuntimeException("MSimUiccController.make() should only be called once");
            }
            mInstance = new UiccController(c, ci);
            HwTelephonyFactory.getHwUiccManager().initHwDsdsController(c, ci);
            HwTelephonyFactory.getHwUiccManager().initHwAllInOneController(c, ci);
            mLauncher = new UiccStateChangedLauncher(c, mInstance);
            uiccController = mInstance;
        }
        return uiccController;
    }

    private UiccController(Context c, CommandsInterface[] ci) {
        log("Creating UiccController");
        this.mContext = c;
        this.mCis = ci;
        for (int i = 0; i < this.mCis.length; i++) {
            Integer index = new Integer(i);
            this.mCis[i].registerForIccStatusChanged(this, 1, index);
            if (HwModemCapability.isCapabilitySupport(9)) {
                this.mCis[i].registerForOn(this, 1, index);
            }
            this.mCis[i].registerForAvailable(this, 1, index);
            this.mCis[i].registerForNotAvailable(this, 3, index);
            this.mCis[i].registerForIccRefresh(this, 4, index);
            if (IS_QUICK_BROADCAST_STATUS) {
                this.mCis[i].registerForIccidChanged(this, 1, index);
            }
            this.mCis[i].registerForUnsolBalongModemReset(this, 5, index);
        }
    }

    public static UiccController getInstance() {
        UiccController uiccController;
        synchronized (mLock) {
            if (mInstance == null) {
                throw new RuntimeException("UiccController.getInstance can't be called before make()");
            }
            uiccController = mInstance;
        }
        return uiccController;
    }

    public UiccCard getUiccCard(int phoneId) {
        synchronized (mLock) {
            if (isValidCardIndex(phoneId)) {
                UiccCard uiccCard = this.mUiccCards[phoneId];
                return uiccCard;
            }
            return null;
        }
    }

    public UiccCard[] getUiccCards() {
        UiccCard[] uiccCardArr;
        synchronized (mLock) {
            uiccCardArr = (UiccCard[]) this.mUiccCards.clone();
        }
        return uiccCardArr;
    }

    public IccRecords getIccRecords(int phoneId, int family) {
        synchronized (mLock) {
            UiccCardApplication app = getUiccCardApplication(phoneId, family);
            if (app != null) {
                IccRecords iccRecords = app.getIccRecords();
                return iccRecords;
            }
            return null;
        }
    }

    public CommandsInterface[] getmCis() {
        return (CommandsInterface[]) this.mCis.clone();
    }

    public IccFileHandler getIccFileHandler(int phoneId, int family) {
        synchronized (mLock) {
            UiccCardApplication app = getUiccCardApplication(phoneId, family);
            if (app != null) {
                IccFileHandler iccFileHandler = app.getIccFileHandler();
                return iccFileHandler;
            }
            return null;
        }
    }

    public void registerForIccChanged(Handler h, int what, Object obj) {
        synchronized (mLock) {
            Registrant r = new Registrant(h, what, obj);
            this.mIccChangedRegistrants.add(r);
            r.notifyRegistrant();
        }
        HwTelephonyFactory.getHwUiccManager().registerForIccChanged(h, what, obj);
    }

    public void unregisterForIccChanged(Handler h) {
        synchronized (mLock) {
            this.mIccChangedRegistrants.remove(h);
        }
        HwTelephonyFactory.getHwUiccManager().unregisterForIccChanged(h);
    }

    /* JADX WARNING: Missing block: B:15:0x0065, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void handleMessage(Message msg) {
        synchronized (mLock) {
            Integer index = getCiIndex(msg);
            if (index.intValue() >= 0 && index.intValue() < this.mCis.length) {
                AsyncResult ar = msg.obj;
                switch (msg.what) {
                    case 1:
                        log("Received EVENT_ICC_STATUS_CHANGED, calling getIccCardStatus");
                        this.mCis[index.intValue()].getIccCardStatus(obtainMessage(2, index));
                        break;
                    case 2:
                        log("Received EVENT_GET_ICC_STATUS_DONE");
                        onGetIccCardStatusDone(ar, index);
                        HwTelephonyFactory.getHwPhoneManager().saveUiccCardsToVirtualNet(this.mUiccCards);
                        break;
                    case 3:
                        log("EVENT_RADIO_UNAVAILABLE, dispose card");
                        if (this.mUiccCards[index.intValue()] != null) {
                            this.mUiccCards[index.intValue()].dispose();
                        }
                        this.mUiccCards[index.intValue()] = null;
                        this.mIccChangedRegistrants.notifyRegistrants(new AsyncResult(null, index, null));
                        HwTelephonyFactory.getHwPhoneManager().saveUiccCardsToVirtualNet(this.mUiccCards);
                        break;
                    case 4:
                        log("Received EVENT_SIM_REFRESH");
                        onSimRefresh(ar, index);
                        break;
                    case 5:
                        log("EVENT_BALONG_MODEM_RESET, dispose all cards.");
                        for (int card_index = 0; card_index < this.mCis.length; card_index++) {
                            if (this.mUiccCards[card_index] != null) {
                                this.mUiccCards[card_index].dispose();
                            }
                            this.mUiccCards[card_index] = null;
                            this.mIccChangedRegistrants.notifyRegistrants(new AsyncResult(null, Integer.valueOf(card_index), null));
                        }
                        HwTelephonyFactory.getHwPhoneManager().saveUiccCardsToVirtualNet(this.mUiccCards);
                        break;
                    default:
                        Rlog.e(LOG_TAG, " Unknown Event " + msg.what);
                        break;
                }
            }
            Rlog.e(LOG_TAG, "Invalid index : " + index + " received with event " + msg.what);
        }
    }

    private Integer getCiIndex(Message msg) {
        Integer index = new Integer(0);
        if (msg == null) {
            return index;
        }
        if (msg.obj != null && (msg.obj instanceof Integer)) {
            return msg.obj;
        }
        if (msg.obj == null || !(msg.obj instanceof AsyncResult)) {
            return index;
        }
        AsyncResult ar = msg.obj;
        if (ar.userObj == null || !(ar.userObj instanceof Integer)) {
            return index;
        }
        return ar.userObj;
    }

    /* JADX WARNING: Missing block: B:11:0x001b, code:
            return null;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public UiccCardApplication getUiccCardApplication(int phoneId, int family) {
        synchronized (mLock) {
            if (!isValidCardIndex(phoneId) || this.mUiccCards[phoneId] == null) {
            } else {
                UiccCardApplication application = this.mUiccCards[phoneId].getApplication(family);
                return application;
            }
        }
    }

    private synchronized void onGetIccCardStatusDone(AsyncResult ar, Integer index) {
        if (ar.exception != null) {
            Rlog.e(LOG_TAG, "Error getting ICC status. RIL_REQUEST_GET_ICC_STATUS should never return an error", ar.exception);
        } else if (isValidCardIndex(index.intValue())) {
            IccCardStatus status = ar.result;
            if (status.mCardState == CardState.CARDSTATE_ABSENT) {
                TelephonyManager.getDefault();
                TelephonyManager.setTelephonyProperty(index.intValue(), IccRecords.PROPERTY_MCC_MATCHING_FYROM, "");
            }
            if (this.mUiccCards[index.intValue()] != null) {
                this.mUiccCards[index.intValue()].update(this.mContext, this.mCis[index.intValue()], status);
                HwTelephonyFactory.getHwUiccManager().updateUiccCard(this.mUiccCards[index.intValue()], status, index);
            } else if (RadioState.RADIO_UNAVAILABLE == this.mCis[index.intValue()].getRadioState()) {
                Rlog.e(LOG_TAG, "Current RadioState is RADIO_UNAVAILABLE,return immediatly");
                return;
            } else {
                this.mUiccCards[index.intValue()] = new UiccCard(this.mContext, this.mCis[index.intValue()], status, index.intValue());
                HwTelephonyFactory.getHwUiccManager().initUiccCard(this.mUiccCards[index.intValue()], status, index);
            }
            HwTelephonyFactory.getHwUiccManager().onGetIccStatusDone(ar, index);
            if (HwTelephonyFactory.getHwUiccManager().uiccHwdsdsNeedSetActiveMode()) {
                log("onGetIccCardStatusDone: uiccHwdsdsWatingActiveMode ");
                return;
            }
            log("Notifying IccChangedRegistrants");
            this.mIccChangedRegistrants.notifyRegistrants(new AsyncResult(null, index, null));
            processRadioPowerDownIfNoCard(this.mUiccCards);
        } else {
            Rlog.e(LOG_TAG, "onGetIccCardStatusDone: invalid index : " + index);
        }
    }

    private void onSimRefresh(AsyncResult ar, Integer index) {
        if (ar.exception != null) {
            Rlog.e(LOG_TAG, "Sim REFRESH with exception: " + ar.exception);
        } else if (isValidCardIndex(index.intValue())) {
            IccRefreshResponse resp = ar.result;
            Rlog.d(LOG_TAG, "onSimRefresh: " + resp);
            if (this.mUiccCards[index.intValue()] == null) {
                Rlog.e(LOG_TAG, "onSimRefresh: refresh on null card : " + index);
            } else if (resp.refreshResult != 2) {
                Rlog.d(LOG_TAG, "Ignoring non reset refresh: " + resp);
            } else {
                Rlog.d(LOG_TAG, "Handling refresh reset: " + resp);
                if (this.mUiccCards[index.intValue()].resetAppWithAid(resp.aid)) {
                    if (this.mContext.getResources().getBoolean(17956994)) {
                        this.mCis[index.intValue()].setRadioPower(false, null);
                    } else {
                        this.mCis[index.intValue()].getIccCardStatus(obtainMessage(2));
                    }
                    this.mIccChangedRegistrants.notifyRegistrants(new AsyncResult(null, index, null));
                }
            }
        } else {
            Rlog.e(LOG_TAG, "onSimRefresh: invalid index : " + index);
        }
    }

    private boolean isValidCardIndex(int index) {
        return index >= 0 && index < this.mUiccCards.length;
    }

    public void disposeCard(int index) {
        synchronized (mLock) {
            if (index < this.mUiccCards.length && this.mUiccCards[index] != null) {
                Rlog.d(LOG_TAG, "Disposing card " + index);
                this.mUiccCards[index].dispose();
                this.mUiccCards[index] = null;
                HwTelephonyFactory.getHwPhoneManager().saveUiccCardsToVirtualNet(this.mUiccCards);
            }
        }
    }

    public void onRefresh(int slotId, int[] fileList) {
        boolean fileChanged = fileList != null && fileList.length > 0;
        log("onRefresh: fileChanged = " + fileChanged + "  slotId = " + slotId);
        if (fileChanged && isValidCardIndex(slotId) && this.mUiccCards[slotId] != null && this.mUiccCards[slotId].getApplicationIndex(slotId) != null) {
            IccRecords iccRecords = this.mUiccCards[slotId].getApplicationIndex(slotId).getIccRecords();
            if (iccRecords != null) {
                iccRecords.onRefresh(fileChanged, fileList);
            }
        }
    }

    private void log(String string) {
        Rlog.d(LOG_TAG, string);
    }

    public void addCardLog(String data) {
        Time t = new Time();
        t.setToNow();
        this.mCardLogs.addLast(t.format("%m-%d %H:%M:%S") + " " + data);
        if (this.mCardLogs.size() > 20) {
            this.mCardLogs.removeFirst();
        }
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        int i;
        pw.println("UiccController: " + this);
        pw.println(" mContext=" + this.mContext);
        pw.println(" mInstance=" + mInstance);
        pw.println(" mIccChangedRegistrants: size=" + this.mIccChangedRegistrants.size());
        for (i = 0; i < this.mIccChangedRegistrants.size(); i++) {
            pw.println("  mIccChangedRegistrants[" + i + "]=" + ((Registrant) this.mIccChangedRegistrants.get(i)).getHandler());
        }
        pw.println();
        pw.flush();
        pw.println(" mUiccCards: size=" + this.mUiccCards.length);
        for (i = 0; i < this.mUiccCards.length; i++) {
            if (this.mUiccCards[i] == null) {
                pw.println("  mUiccCards[" + i + "]=null");
            } else {
                pw.println("  mUiccCards[" + i + "]=" + this.mUiccCards[i]);
                this.mUiccCards[i].dump(fd, pw, args);
            }
        }
        pw.println("mCardLogs: ");
        for (i = 0; i < this.mCardLogs.size(); i++) {
            pw.println("  " + ((String) this.mCardLogs.get(i)));
        }
    }
}
