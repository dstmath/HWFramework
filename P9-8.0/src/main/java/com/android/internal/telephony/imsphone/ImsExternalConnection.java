package com.android.internal.telephony.imsphone;

import android.content.Context;
import android.net.Uri;
import android.telephony.PhoneNumberUtils;
import com.android.internal.telephony.Call;
import com.android.internal.telephony.Call.State;
import com.android.internal.telephony.CallStateException;
import com.android.internal.telephony.Connection;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.UUSInfo;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ImsExternalConnection extends Connection {
    private static final String CONFERENCE_PREFIX = "conf";
    private ImsExternalCall mCall;
    private int mCallId;
    private final Context mContext;
    private boolean mIsPullable;
    private final Set<Listener> mListeners = Collections.newSetFromMap(new ConcurrentHashMap(8, 0.9f, 1));
    private Uri mOriginalAddress;

    public interface Listener {
        void onPullExternalCall(ImsExternalConnection imsExternalConnection);
    }

    protected ImsExternalConnection(Phone phone, int callId, Uri address, boolean isPullable) {
        super(phone.getPhoneType());
        this.mContext = phone.getContext();
        this.mCall = new ImsExternalCall(phone, this);
        this.mCallId = callId;
        setExternalConnectionAddress(address);
        this.mNumberPresentation = 1;
        this.mIsPullable = isPullable;
        rebuildCapabilities();
        setActive();
    }

    public int getCallId() {
        return this.mCallId;
    }

    public Call getCall() {
        return this.mCall;
    }

    public long getDisconnectTime() {
        return 0;
    }

    public long getHoldDurationMillis() {
        return 0;
    }

    public String getVendorDisconnectCause() {
        return null;
    }

    public void hangup() throws CallStateException {
    }

    public void separate() throws CallStateException {
    }

    public void proceedAfterWaitChar() {
    }

    public void proceedAfterWildChar(String str) {
    }

    public void cancelPostDial() {
    }

    public int getNumberPresentation() {
        return this.mNumberPresentation;
    }

    public UUSInfo getUUSInfo() {
        return null;
    }

    public int getPreciseDisconnectCause() {
        return 0;
    }

    public boolean isMultiparty() {
        return false;
    }

    public void pullExternalCall() {
        for (Listener listener : this.mListeners) {
            listener.onPullExternalCall(this);
        }
    }

    public void setActive() {
        if (this.mCall != null) {
            this.mCall.setActive();
        }
    }

    public void setTerminated() {
        if (this.mCall != null) {
            this.mCall.setTerminated();
        }
    }

    public void setIsPullable(boolean isPullable) {
        this.mIsPullable = isPullable;
        rebuildCapabilities();
    }

    public void setExternalConnectionAddress(Uri address) {
        this.mOriginalAddress = address;
        if ("sip".equals(address.getScheme()) && address.getSchemeSpecificPart().startsWith(CONFERENCE_PREFIX)) {
            this.mCnapName = this.mContext.getString(17039748);
            this.mCnapNamePresentation = 1;
            this.mAddress = "";
            this.mNumberPresentation = 2;
            return;
        }
        this.mAddress = PhoneNumberUtils.convertSipUriToTelUri(address).getSchemeSpecificPart();
    }

    public void addListener(Listener listener) {
        this.mListeners.add(listener);
    }

    public void removeListener(Listener listener) {
        this.mListeners.remove(listener);
    }

    public String toString() {
        StringBuilder str = new StringBuilder(128);
        str.append("[ImsExternalConnection dialogCallId:");
        str.append(this.mCallId);
        str.append(" state:");
        if (this.mCall.getState() == State.ACTIVE) {
            str.append("Active");
        } else if (this.mCall.getState() == State.DISCONNECTED) {
            str.append("Disconnected");
        }
        str.append("]");
        return str.toString();
    }

    private void rebuildCapabilities() {
        int capabilities = 16;
        if (this.mIsPullable) {
            capabilities = 48;
        }
        setConnectionCapabilities(capabilities);
    }
}
