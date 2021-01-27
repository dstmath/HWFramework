package com.android.server.wifi.hotspot2.soap;

import android.text.TextUtils;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.ksoap2.serialization.AttributeInfo;
import org.ksoap2.serialization.SoapObject;

public class SppResponseMessage {
    static final String SPPErrorCodeAttribute = "errorCode";
    static final String SPPErrorProperty = "sppError";
    static final String SPPSessionIDAttribute = "sessionID";
    static final String SPPStatusAttribute = "sppStatus";
    static final String SPPVersionAttribute = "sppVersion";
    private Map<String, String> mAttributes;
    private int mError = -1;
    private final int mMessageType;
    private final String mSessionID;
    private int mStatus;
    private final String mVersion;

    public static class MessageType {
        public static final int EXCHANGE_COMPLETE = 1;
        public static final int POST_DEV_DATA_RESPONSE = 0;
    }

    protected SppResponseMessage(SoapObject response, int messageType) throws IllegalArgumentException {
        int i;
        if (response.hasAttribute("sppStatus")) {
            this.mMessageType = messageType;
            this.mStatus = SppConstants.mapStatusStringToInt(response.getAttributeAsString("sppStatus"));
            if (!response.hasAttribute("sppVersion") || !response.hasAttribute("sessionID") || (i = this.mStatus) == -1) {
                throw new IllegalArgumentException("Incomplete request: " + messageType);
            } else if (i != 6 || response.hasProperty("sppError")) {
                if (response.hasProperty("sppError")) {
                    SoapObject errorInfo = (SoapObject) response.getProperty("sppError");
                    if (errorInfo.hasAttribute("errorCode")) {
                        this.mError = SppConstants.mapErrorStringToInt(errorInfo.getAttributeAsString("errorCode"));
                    } else {
                        throw new IllegalArgumentException("Missing errorCode");
                    }
                }
                this.mSessionID = response.getAttributeAsString("sessionID");
                this.mVersion = response.getAttributeAsString("sppVersion");
                if (response.getAttributeCount() > 0) {
                    this.mAttributes = new HashMap();
                    for (int i2 = 0; i2 < response.getAttributeCount(); i2++) {
                        AttributeInfo attributeInfo = new AttributeInfo();
                        response.getAttributeInfo(i2, attributeInfo);
                        this.mAttributes.put(attributeInfo.getName(), response.getAttributeAsString(attributeInfo.getName()));
                    }
                }
            } else {
                throw new IllegalArgumentException("Missing sppError");
            }
        } else {
            throw new IllegalArgumentException("Missing status");
        }
    }

    public int getMessageType() {
        return this.mMessageType;
    }

    public String getVersion() {
        return this.mVersion;
    }

    public String getSessionID() {
        return this.mSessionID;
    }

    public int getStatus() {
        return this.mStatus;
    }

    public int getError() {
        return this.mError;
    }

    /* access modifiers changed from: protected */
    public final Map<String, String> getAttributes() {
        return Collections.unmodifiableMap(this.mAttributes);
    }

    public int hashCode() {
        return Objects.hash(Integer.valueOf(this.mMessageType), this.mVersion, this.mSessionID, Integer.valueOf(this.mStatus), Integer.valueOf(this.mError), this.mAttributes);
    }

    public boolean equals(Object thatObject) {
        if (this == thatObject) {
            return true;
        }
        if (!(thatObject instanceof SppResponseMessage)) {
            return false;
        }
        SppResponseMessage that = (SppResponseMessage) thatObject;
        if (this.mMessageType == that.mMessageType && this.mStatus == that.mStatus && this.mError == that.mError && TextUtils.equals(this.mVersion, that.mVersion) && TextUtils.equals(this.mSessionID, that.mSessionID)) {
            Map<String, String> map = this.mAttributes;
            if (map == null) {
                if (that.mAttributes == null) {
                    return true;
                }
            } else if (map.equals(that.mAttributes)) {
                return true;
            }
        }
        return false;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(this.mMessageType);
        sb.append(", version ");
        sb.append(this.mVersion);
        sb.append(", status ");
        sb.append(this.mStatus);
        sb.append(", session-id ");
        sb.append(this.mSessionID);
        if (this.mError != -1) {
            sb.append(", error ");
            sb.append(this.mError);
        }
        return sb.toString();
    }
}
