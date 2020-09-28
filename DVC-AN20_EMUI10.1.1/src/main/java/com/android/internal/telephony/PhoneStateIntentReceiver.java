package com.android.internal.telephony;

import android.annotation.UnsupportedAppUsage;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.telephony.Rlog;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import com.android.internal.telephony.PhoneConstants;

@Deprecated
public final class PhoneStateIntentReceiver extends BroadcastReceiver {
    private static final boolean DBG = false;
    private static final String LOG_TAG = "PhoneStatIntentReceiver";
    private static final int NOTIF_PHONE = 1;
    private static final int NOTIF_SERVICE = 2;
    private static final int NOTIF_SIGNAL = 4;
    private int mAsuEventWhat;
    private Context mContext;
    private IntentFilter mFilter;
    PhoneConstants.State mPhoneState;
    private int mPhoneStateEventWhat;
    ServiceState mServiceState;
    private int mServiceStateEventWhat;
    @UnsupportedAppUsage
    SignalStrength mSignalStrength;
    private Handler mTarget;
    @UnsupportedAppUsage
    private int mWants;

    public PhoneStateIntentReceiver() {
        this.mPhoneState = PhoneConstants.State.IDLE;
        this.mServiceState = new ServiceState();
        this.mSignalStrength = new SignalStrength();
        this.mFilter = new IntentFilter();
    }

    @UnsupportedAppUsage
    public PhoneStateIntentReceiver(Context context, Handler target) {
        this();
        setContext(context);
        setTarget(target);
    }

    public void setContext(Context c) {
        this.mContext = c;
    }

    public void setTarget(Handler h) {
        this.mTarget = h;
    }

    public PhoneConstants.State getPhoneState() {
        if ((this.mWants & 1) != 0) {
            return this.mPhoneState;
        }
        throw new RuntimeException("client must call notifyPhoneCallState(int)");
    }

    public ServiceState getServiceState() {
        if ((this.mWants & 2) != 0) {
            return this.mServiceState;
        }
        throw new RuntimeException("client must call notifyServiceState(int)");
    }

    public int getSignalStrengthLevelAsu() {
        if ((this.mWants & 4) != 0) {
            return this.mSignalStrength.getAsuLevel();
        }
        throw new RuntimeException("client must call notifySignalStrength(int)");
    }

    @UnsupportedAppUsage
    public int getSignalStrengthDbm() {
        if ((this.mWants & 4) != 0) {
            return this.mSignalStrength.getDbm();
        }
        throw new RuntimeException("client must call notifySignalStrength(int)");
    }

    public void notifyPhoneCallState(int eventWhat) {
        this.mWants |= 1;
        this.mPhoneStateEventWhat = eventWhat;
        this.mFilter.addAction("android.intent.action.PHONE_STATE");
    }

    public boolean getNotifyPhoneCallState() {
        return (this.mWants & 1) != 0;
    }

    @UnsupportedAppUsage
    public void notifyServiceState(int eventWhat) {
        this.mWants |= 2;
        this.mServiceStateEventWhat = eventWhat;
        this.mFilter.addAction("android.intent.action.SERVICE_STATE");
    }

    public boolean getNotifyServiceState() {
        return (this.mWants & 2) != 0;
    }

    @UnsupportedAppUsage
    public void notifySignalStrength(int eventWhat) {
        this.mWants |= 4;
        this.mAsuEventWhat = eventWhat;
        this.mFilter.addAction("android.intent.action.SIG_STR");
    }

    public boolean getNotifySignalStrength() {
        return (this.mWants & 4) != 0;
    }

    @UnsupportedAppUsage
    public void registerIntent() {
        this.mContext.registerReceiver(this, this.mFilter);
    }

    @UnsupportedAppUsage
    public void unregisterIntent() {
        this.mContext.unregisterReceiver(this);
    }

    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        try {
            if ("android.intent.action.SIG_STR".equals(action)) {
                this.mSignalStrength = SignalStrength.newFromBundle(intent.getExtras());
                if (this.mTarget != null && getNotifySignalStrength()) {
                    this.mTarget.sendMessage(Message.obtain(this.mTarget, this.mAsuEventWhat));
                }
            } else if ("android.intent.action.PHONE_STATE".equals(action)) {
                this.mPhoneState = Enum.valueOf(PhoneConstants.State.class, intent.getStringExtra("state"));
                if (this.mTarget != null && getNotifyPhoneCallState()) {
                    this.mTarget.sendMessage(Message.obtain(this.mTarget, this.mPhoneStateEventWhat));
                }
            } else if ("android.intent.action.SERVICE_STATE".equals(action)) {
                this.mServiceState = ServiceState.newFromBundle(intent.getExtras());
                if (this.mTarget != null && getNotifyServiceState()) {
                    this.mTarget.sendMessage(Message.obtain(this.mTarget, this.mServiceStateEventWhat));
                }
            }
        } catch (Exception ex) {
            Rlog.e(LOG_TAG, "[PhoneStateIntentRecv] caught " + ex);
            ex.printStackTrace();
        }
    }

    public int getSignalStrengthNrAsu() {
        if ((this.mWants & 4) != 0) {
            return this.mSignalStrength.getNrAsuLevel();
        }
        throw new RuntimeException("client must call notifySignalStrength(int)");
    }

    public int getSignalStrengthNrDbm() {
        if ((this.mWants & 4) != 0) {
            return this.mSignalStrength.getNrDbm();
        }
        throw new RuntimeException("client must call notifySignalStrength(int)");
    }
}
