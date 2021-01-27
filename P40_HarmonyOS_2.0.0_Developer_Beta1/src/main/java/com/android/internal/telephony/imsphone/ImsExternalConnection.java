package com.android.internal.telephony.imsphone;

import android.annotation.UnsupportedAppUsage;
import android.content.Context;
import android.net.Uri;
import android.telephony.PhoneNumberUtils;
import com.android.internal.telephony.Call;
import com.android.internal.telephony.CallStateException;
import com.android.internal.telephony.Connection;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneConfigurationManager;
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

    @Override // com.android.internal.telephony.Connection
    public Call getCall() {
        return this.mCall;
    }

    @Override // com.android.internal.telephony.Connection
    public long getDisconnectTime() {
        return 0;
    }

    @Override // com.android.internal.telephony.Connection
    public long getHoldDurationMillis() {
        return 0;
    }

    @Override // com.android.internal.telephony.Connection
    public String getVendorDisconnectCause() {
        return null;
    }

    @Override // com.android.internal.telephony.Connection
    public void hangup() throws CallStateException {
    }

    @Override // com.android.internal.telephony.Connection
    public void deflect(String number) throws CallStateException {
        throw new CallStateException("Deflect is not supported for external calls");
    }

    @Override // com.android.internal.telephony.Connection
    public void separate() throws CallStateException {
    }

    @Override // com.android.internal.telephony.Connection
    public void proceedAfterWaitChar() {
    }

    @Override // com.android.internal.telephony.Connection
    public void proceedAfterWildChar(String str) {
    }

    @Override // com.android.internal.telephony.Connection
    public void cancelPostDial() {
    }

    @Override // com.android.internal.telephony.Connection
    public int getNumberPresentation() {
        return this.mNumberPresentation;
    }

    @Override // com.android.internal.telephony.Connection
    public UUSInfo getUUSInfo() {
        return null;
    }

    @Override // com.android.internal.telephony.Connection
    public int getPreciseDisconnectCause() {
        return 0;
    }

    @Override // com.android.internal.telephony.Connection
    public boolean isMultiparty() {
        return false;
    }

    @Override // com.android.internal.telephony.Connection
    public void pullExternalCall() {
        for (Listener listener : this.mListeners) {
            listener.onPullExternalCall(this);
        }
    }

    @UnsupportedAppUsage
    public void setActive() {
        ImsExternalCall imsExternalCall = this.mCall;
        if (imsExternalCall != null) {
            imsExternalCall.setActive();
        }
    }

    public void setTerminated() {
        ImsExternalCall imsExternalCall = this.mCall;
        if (imsExternalCall != null) {
            imsExternalCall.setTerminated();
        }
    }

    public void setIsPullable(boolean isPullable) {
        this.mIsPullable = isPullable;
        rebuildCapabilities();
    }

    public void setExternalConnectionAddress(Uri address) {
        this.mOriginalAddress = address;
        if (!"sip".equals(address.getScheme()) || !address.getSchemeSpecificPart().startsWith(CONFERENCE_PREFIX)) {
            this.mAddress = PhoneNumberUtils.convertSipUriToTelUri(address).getSchemeSpecificPart();
            return;
        }
        this.mCnapName = this.mContext.getString(17039779);
        this.mCnapNamePresentation = 1;
        this.mAddress = PhoneConfigurationManager.SSSS;
        this.mNumberPresentation = 2;
    }

    public void addListener(Listener listener) {
        this.mListeners.add(listener);
    }

    public void removeListener(Listener listener) {
        this.mListeners.remove(listener);
    }

    @Override // com.android.internal.telephony.Connection
    public String toString() {
        StringBuilder str = new StringBuilder(128);
        str.append("[ImsExternalConnection dialogCallId:");
        str.append(this.mCallId);
        str.append(" state:");
        if (this.mCall.getState() == Call.State.ACTIVE) {
            str.append("Active");
        } else if (this.mCall.getState() == Call.State.DISCONNECTED) {
            str.append("Disconnected");
        }
        str.append("]");
        return str.toString();
    }

    @UnsupportedAppUsage
    private void rebuildCapabilities() {
        int capabilities = 16;
        if (this.mIsPullable) {
            capabilities = 16 | 32;
        }
        setConnectionCapabilities(capabilities);
    }
}
