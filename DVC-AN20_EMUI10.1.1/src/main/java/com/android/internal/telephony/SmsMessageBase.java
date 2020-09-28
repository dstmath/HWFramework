package com.android.internal.telephony;

import android.annotation.UnsupportedAppUsage;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import android.text.Emoji;
import com.android.internal.telephony.GsmAlphabet;
import com.android.internal.telephony.SmsConstants;
import java.text.BreakIterator;
import java.util.Arrays;

public abstract class SmsMessageBase {
    public static final String CT_SMS_SEND_CENTER = "10659401";
    protected boolean blacklistFlag = false;
    protected String mEmailBody;
    protected String mEmailFrom;
    protected int mIndexOnIcc = -1;
    protected boolean mIsEmail;
    @UnsupportedAppUsage
    protected boolean mIsMwi;
    @UnsupportedAppUsage
    protected String mMessageBody;
    @UnsupportedAppUsage
    public int mMessageRef;
    @UnsupportedAppUsage
    protected boolean mMwiDontStore;
    @UnsupportedAppUsage
    protected boolean mMwiSense;
    @UnsupportedAppUsage
    protected SmsAddress mOriginatingAddress;
    @UnsupportedAppUsage
    protected byte[] mPdu;
    protected String mPseudoSubject;
    protected SmsAddress mRecipientAddress;
    @UnsupportedAppUsage
    protected String mScAddress;
    protected long mScTimeMillis;
    protected int mStatusOnIcc = -1;
    protected byte[] mUserData;
    @UnsupportedAppUsage
    protected SmsHeader mUserDataHeader;

    public abstract SmsConstants.MessageClass getMessageClass();

    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    public abstract int getProtocolIdentifier();

    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    public abstract int getStatus();

    public abstract boolean isCphsMwiMessage();

    public abstract boolean isMWIClearMessage();

    public abstract boolean isMWISetMessage();

    public abstract boolean isMwiDontStore();

    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    public abstract boolean isReplace();

    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    public abstract boolean isReplyPathPresent();

    @UnsupportedAppUsage
    public abstract boolean isStatusReportMessage();

    public static abstract class SubmitPduBase {
        @UnsupportedAppUsage
        public byte[] encodedMessage;
        @UnsupportedAppUsage
        public byte[] encodedScAddress;

        public String toString() {
            return "SubmitPdu: encodedScAddress = " + Arrays.toString(this.encodedScAddress) + ", encodedMessage = " + Arrays.toString(this.encodedMessage);
        }
    }

    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    public String getServiceCenterAddress() {
        return this.mScAddress;
    }

    @UnsupportedAppUsage
    public String getOriginatingAddress() {
        SmsAddress smsAddress = this.mOriginatingAddress;
        if (smsAddress == null) {
            return null;
        }
        return smsAddress.getAddressString();
    }

    @UnsupportedAppUsage
    public String getDisplayOriginatingAddress() {
        if (this.mIsEmail) {
            return this.mEmailFrom;
        }
        return getOriginatingAddress();
    }

    @UnsupportedAppUsage
    public String getMessageBody() {
        return this.mMessageBody;
    }

    @UnsupportedAppUsage
    public String getDisplayMessageBody() {
        if (this.mIsEmail) {
            return this.mEmailBody;
        }
        return getMessageBody();
    }

    @UnsupportedAppUsage
    public String getPseudoSubject() {
        String str = this.mPseudoSubject;
        return str == null ? "" : str;
    }

    @UnsupportedAppUsage
    public long getTimestampMillis() {
        return this.mScTimeMillis;
    }

    public boolean isEmail() {
        return this.mIsEmail;
    }

    public String getEmailBody() {
        return this.mEmailBody;
    }

    public String getEmailFrom() {
        return this.mEmailFrom;
    }

    @UnsupportedAppUsage
    public byte[] getUserData() {
        return this.mUserData;
    }

    @UnsupportedAppUsage
    public SmsHeader getUserDataHeader() {
        return this.mUserDataHeader;
    }

    public byte[] getPdu() {
        return this.mPdu;
    }

    public int getStatusOnIcc() {
        return this.mStatusOnIcc;
    }

    public int getIndexOnIcc() {
        return this.mIndexOnIcc;
    }

    /* access modifiers changed from: protected */
    public void parseMessageBody() {
        SmsAddress smsAddress = this.mOriginatingAddress;
        if (smsAddress != null && smsAddress.couldBeEmailGateway()) {
            extractEmailAddressFromMessageBody();
        }
    }

    /* access modifiers changed from: protected */
    public void extractEmailAddressFromMessageBody() {
        String[] parts = this.mMessageBody.split("( /)|( )", 2);
        if (parts.length >= 2) {
            this.mEmailFrom = parts[0];
            this.mEmailBody = parts[1];
            this.mIsEmail = Telephony.Mms.isEmailAddress(this.mEmailFrom);
        }
    }

    public static int findNextUnicodePosition(int currentPosition, int byteLimit, CharSequence msgBody) {
        int nextPos = Math.min((byteLimit / 2) + currentPosition, msgBody.length());
        if (nextPos >= msgBody.length()) {
            return nextPos;
        }
        BreakIterator breakIterator = BreakIterator.getCharacterInstance();
        breakIterator.setText(msgBody.toString());
        if (breakIterator.isBoundary(nextPos)) {
            return nextPos;
        }
        int breakPos = breakIterator.preceding(nextPos);
        while (breakPos + 4 <= nextPos && Emoji.isRegionalIndicatorSymbol(Character.codePointAt(msgBody, breakPos)) && Emoji.isRegionalIndicatorSymbol(Character.codePointAt(msgBody, breakPos + 2))) {
            breakPos += 4;
        }
        if (breakPos > currentPosition) {
            return breakPos;
        }
        if (Character.isHighSurrogate(msgBody.charAt(nextPos - 1))) {
            return nextPos - 1;
        }
        return nextPos;
    }

    public static GsmAlphabet.TextEncodingDetails calcUnicodeEncodingDetails(CharSequence msgBody) {
        GsmAlphabet.TextEncodingDetails ted = new GsmAlphabet.TextEncodingDetails();
        int octets = msgBody.length() * 2;
        ted.codeUnitSize = 3;
        ted.codeUnitCount = msgBody.length();
        if (octets > 140) {
            int maxUserDataBytesWithHeader = 134;
            if (!SmsMessage.hasEmsSupport() && octets <= (134 - 2) * 9) {
                maxUserDataBytesWithHeader = 134 - 2;
            }
            int pos = 0;
            int msgCount = 0;
            while (pos < msgBody.length()) {
                int nextPos = findNextUnicodePosition(pos, maxUserDataBytesWithHeader, msgBody);
                if (nextPos == msgBody.length()) {
                    ted.codeUnitsRemaining = ((maxUserDataBytesWithHeader / 2) + pos) - msgBody.length();
                }
                pos = nextPos;
                msgCount++;
            }
            ted.msgCount = msgCount;
        } else {
            ted.msgCount = 1;
            ted.codeUnitsRemaining = (140 - octets) / 2;
        }
        return ted;
    }

    public String getRecipientAddress() {
        SmsAddress smsAddress = this.mRecipientAddress;
        if (smsAddress == null) {
            return null;
        }
        return smsAddress.getAddressString();
    }

    public void setOriginatingAddress(SmsAddress smsAddress) {
        this.mOriginatingAddress = smsAddress;
    }

    public void setPdu(byte[] pdu) {
        this.mPdu = pdu;
    }
}
