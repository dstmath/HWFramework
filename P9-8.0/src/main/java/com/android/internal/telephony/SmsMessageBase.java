package com.android.internal.telephony;

import android.provider.Telephony.Mms;
import android.telephony.SmsMessage;
import android.text.Emoji;
import android.util.LogException;
import com.android.internal.telephony.GsmAlphabet.TextEncodingDetails;
import com.android.internal.telephony.SmsConstants.MessageClass;
import java.text.BreakIterator;
import java.util.Arrays;

public abstract class SmsMessageBase {
    protected boolean blacklistFlag = false;
    protected String mEmailBody;
    protected String mEmailFrom;
    protected int mIndexOnIcc = -1;
    protected boolean mIsEmail;
    protected boolean mIsMwi;
    protected String mMessageBody;
    public int mMessageRef;
    protected boolean mMwiDontStore;
    protected boolean mMwiSense;
    protected SmsAddress mOriginatingAddress;
    protected byte[] mPdu;
    protected String mPseudoSubject;
    protected String mScAddress;
    protected long mScTimeMillis;
    protected int mStatusOnIcc = -1;
    protected byte[] mUserData;
    protected SmsHeader mUserDataHeader;

    public static abstract class SubmitPduBase {
        public byte[] encodedMessage;
        public byte[] encodedScAddress;

        public String toString() {
            return "SubmitPdu: encodedScAddress = " + Arrays.toString(this.encodedScAddress) + ", encodedMessage = " + Arrays.toString(this.encodedMessage);
        }
    }

    public abstract MessageClass getMessageClass();

    public abstract int getProtocolIdentifier();

    public abstract int getStatus();

    public abstract boolean isCphsMwiMessage();

    public abstract boolean isMWIClearMessage();

    public abstract boolean isMWISetMessage();

    public abstract boolean isMwiDontStore();

    public abstract boolean isReplace();

    public abstract boolean isReplyPathPresent();

    public abstract boolean isStatusReportMessage();

    public String getServiceCenterAddress() {
        return this.mScAddress;
    }

    public String getOriginatingAddress() {
        if (this.mOriginatingAddress == null) {
            return null;
        }
        return this.mOriginatingAddress.getAddressString();
    }

    public String getDisplayOriginatingAddress() {
        if (this.mIsEmail) {
            return this.mEmailFrom;
        }
        return getOriginatingAddress();
    }

    public String getMessageBody() {
        return this.mMessageBody;
    }

    public String getDisplayMessageBody() {
        if (this.mIsEmail) {
            return this.mEmailBody;
        }
        return getMessageBody();
    }

    public String getPseudoSubject() {
        return this.mPseudoSubject == null ? LogException.NO_VALUE : this.mPseudoSubject;
    }

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

    public byte[] getUserData() {
        return this.mUserData;
    }

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

    protected void parseMessageBody() {
        if (this.mOriginatingAddress != null && this.mOriginatingAddress.couldBeEmailGateway()) {
            extractEmailAddressFromMessageBody();
        }
    }

    protected void extractEmailAddressFromMessageBody() {
        String[] parts = this.mMessageBody.split("( /)|( )", 2);
        if (parts.length >= 2) {
            this.mEmailFrom = parts[0];
            this.mEmailBody = parts[1];
            this.mIsEmail = Mms.isEmailAddress(this.mEmailFrom);
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

    public static TextEncodingDetails calcUnicodeEncodingDetails(CharSequence msgBody) {
        TextEncodingDetails ted = new TextEncodingDetails();
        int octets = msgBody.length() * 2;
        ted.codeUnitSize = 3;
        ted.codeUnitCount = msgBody.length();
        if (octets > 140) {
            int maxUserDataBytesWithHeader = 134;
            if (!SmsMessage.hasEmsSupport() && octets <= 1188) {
                maxUserDataBytesWithHeader = 132;
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
}
