package com.android.internal.telephony.gsm;

import android.telephony.SmsCbCmasInfo;
import android.telephony.SmsCbEtwsInfo;
import com.android.internal.telephony.imsphone.CallFailCause;
import com.google.android.mms.pdu.PduHeaders;
import com.google.android.mms.pdu.PduPart;
import java.util.Arrays;

public class SmsCbHeader {
    static final int FORMAT_ETWS_PRIMARY = 3;
    static final int FORMAT_GSM = 1;
    static final int FORMAT_UMTS = 2;
    private static final int MESSAGE_TYPE_CBS_MESSAGE = 1;
    static final int PDU_HEADER_LENGTH = 6;
    private static final int PDU_LENGTH_ETWS = 56;
    private static final int PDU_LENGTH_GSM = 88;
    private final SmsCbCmasInfo mCmasInfo;
    private final int mDataCodingScheme;
    private final SmsCbEtwsInfo mEtwsInfo;
    private final int mFormat;
    private final int mGeographicalScope;
    private final int mMessageIdentifier;
    private final int mNrOfPages;
    private final int mPageIndex;
    private final int mSerialNumber;

    public SmsCbHeader(byte[] pdu) throws IllegalArgumentException {
        if (pdu == null || pdu.length < PDU_HEADER_LENGTH) {
            throw new IllegalArgumentException("Illegal PDU");
        }
        if (pdu.length <= PDU_LENGTH_GSM) {
            this.mGeographicalScope = (pdu[0] & PduPart.P_CONTENT_ID) >>> PDU_HEADER_LENGTH;
            this.mSerialNumber = ((pdu[0] & PduHeaders.STORE_STATUS_ERROR_END) << 8) | (pdu[MESSAGE_TYPE_CBS_MESSAGE] & PduHeaders.STORE_STATUS_ERROR_END);
            this.mMessageIdentifier = ((pdu[FORMAT_UMTS] & PduHeaders.STORE_STATUS_ERROR_END) << 8) | (pdu[FORMAT_ETWS_PRIMARY] & PduHeaders.STORE_STATUS_ERROR_END);
            if (!isEtwsMessage() || pdu.length > PDU_LENGTH_ETWS) {
                this.mFormat = MESSAGE_TYPE_CBS_MESSAGE;
                this.mDataCodingScheme = pdu[4] & PduHeaders.STORE_STATUS_ERROR_END;
                int pageIndex = (pdu[5] & CallFailCause.CALL_BARRED) >>> 4;
                int nrOfPages = pdu[5] & 15;
                if (!(pageIndex == 0 || nrOfPages == 0)) {
                    if (pageIndex > nrOfPages) {
                    }
                    this.mPageIndex = pageIndex;
                    this.mNrOfPages = nrOfPages;
                }
                pageIndex = MESSAGE_TYPE_CBS_MESSAGE;
                nrOfPages = MESSAGE_TYPE_CBS_MESSAGE;
                this.mPageIndex = pageIndex;
                this.mNrOfPages = nrOfPages;
            } else {
                byte[] copyOfRange;
                this.mFormat = FORMAT_ETWS_PRIMARY;
                this.mDataCodingScheme = -1;
                this.mPageIndex = -1;
                this.mNrOfPages = -1;
                boolean emergencyUserAlert = (pdu[4] & MESSAGE_TYPE_CBS_MESSAGE) != 0;
                boolean activatePopup = (pdu[5] & PduPart.P_Q) != 0;
                int warningType = (pdu[4] & 254) >>> MESSAGE_TYPE_CBS_MESSAGE;
                if (pdu.length > PDU_HEADER_LENGTH) {
                    copyOfRange = Arrays.copyOfRange(pdu, PDU_HEADER_LENGTH, pdu.length);
                } else {
                    copyOfRange = null;
                }
                this.mEtwsInfo = new SmsCbEtwsInfo(warningType, emergencyUserAlert, activatePopup, true, copyOfRange);
                this.mCmasInfo = null;
                return;
            }
        }
        this.mFormat = FORMAT_UMTS;
        int messageType = pdu[0];
        if (messageType != MESSAGE_TYPE_CBS_MESSAGE) {
            throw new IllegalArgumentException("Unsupported message type " + messageType);
        }
        this.mMessageIdentifier = ((pdu[MESSAGE_TYPE_CBS_MESSAGE] & PduHeaders.STORE_STATUS_ERROR_END) << 8) | (pdu[FORMAT_UMTS] & PduHeaders.STORE_STATUS_ERROR_END);
        this.mGeographicalScope = (pdu[FORMAT_ETWS_PRIMARY] & PduPart.P_CONTENT_ID) >>> PDU_HEADER_LENGTH;
        this.mSerialNumber = ((pdu[FORMAT_ETWS_PRIMARY] & PduHeaders.STORE_STATUS_ERROR_END) << 8) | (pdu[4] & PduHeaders.STORE_STATUS_ERROR_END);
        this.mDataCodingScheme = pdu[5] & PduHeaders.STORE_STATUS_ERROR_END;
        this.mPageIndex = MESSAGE_TYPE_CBS_MESSAGE;
        this.mNrOfPages = MESSAGE_TYPE_CBS_MESSAGE;
        if (isEtwsMessage()) {
            this.mEtwsInfo = new SmsCbEtwsInfo(getEtwsWarningType(), isEtwsEmergencyUserAlert(), isEtwsPopupAlert(), false, null);
            this.mCmasInfo = null;
        } else if (isCmasMessage()) {
            int messageClass = getCmasMessageClass();
            int severity = getCmasSeverity();
            int urgency = getCmasUrgency();
            int certainty = getCmasCertainty();
            this.mEtwsInfo = null;
            this.mCmasInfo = new SmsCbCmasInfo(messageClass, -1, -1, severity, urgency, certainty);
        } else {
            this.mEtwsInfo = null;
            this.mCmasInfo = null;
        }
    }

    int getGeographicalScope() {
        return this.mGeographicalScope;
    }

    int getSerialNumber() {
        return this.mSerialNumber;
    }

    int getServiceCategory() {
        return this.mMessageIdentifier;
    }

    int getDataCodingScheme() {
        return this.mDataCodingScheme;
    }

    int getPageIndex() {
        return this.mPageIndex;
    }

    int getNumberOfPages() {
        return this.mNrOfPages;
    }

    SmsCbEtwsInfo getEtwsInfo() {
        return this.mEtwsInfo;
    }

    SmsCbCmasInfo getCmasInfo() {
        return this.mCmasInfo;
    }

    boolean isEmergencyMessage() {
        if (this.mMessageIdentifier < SmsCbConstants.MESSAGE_ID_PWS_FIRST_IDENTIFIER || this.mMessageIdentifier > SmsCbConstants.MESSAGE_ID_PWS_LAST_IDENTIFIER) {
            return false;
        }
        return true;
    }

    private boolean isEtwsMessage() {
        return (this.mMessageIdentifier & SmsCbConstants.MESSAGE_ID_ETWS_TYPE_MASK) == SmsCbConstants.MESSAGE_ID_PWS_FIRST_IDENTIFIER;
    }

    boolean isEtwsPrimaryNotification() {
        return this.mFormat == FORMAT_ETWS_PRIMARY;
    }

    boolean isUmtsFormat() {
        return this.mFormat == FORMAT_UMTS;
    }

    private boolean isCmasMessage() {
        if (this.mMessageIdentifier < SmsCbConstants.MESSAGE_ID_CMAS_FIRST_IDENTIFIER || this.mMessageIdentifier > SmsCbConstants.MESSAGE_ID_CMAS_LAST_IDENTIFIER) {
            return false;
        }
        return true;
    }

    private boolean isEtwsPopupAlert() {
        return (this.mSerialNumber & SmsCbConstants.SERIAL_NUMBER_ETWS_ACTIVATE_POPUP) != 0;
    }

    private boolean isEtwsEmergencyUserAlert() {
        return (this.mSerialNumber & SmsCbConstants.SERIAL_NUMBER_ETWS_EMERGENCY_USER_ALERT) != 0;
    }

    private int getEtwsWarningType() {
        return this.mMessageIdentifier - 4352;
    }

    private int getCmasMessageClass() {
        switch (this.mMessageIdentifier) {
            case SmsCbConstants.MESSAGE_ID_CMAS_FIRST_IDENTIFIER /*4370*/:
            case SmsCbConstants.MESSAGE_ID_CMAS_ALERT_PRESIDENTIAL_LEVEL_LANGUAGE /*4383*/:
                return 0;
            case SmsCbConstants.MESSAGE_ID_CMAS_ALERT_EXTREME_IMMEDIATE_OBSERVED /*4371*/:
            case SmsCbConstants.MESSAGE_ID_CMAS_ALERT_EXTREME_IMMEDIATE_LIKELY /*4372*/:
            case SmsCbConstants.MESSAGE_ID_CMAS_ALERT_EXTREME_IMMEDIATE_OBSERVED_LANGUAGE /*4384*/:
            case SmsCbConstants.MESSAGE_ID_CMAS_ALERT_EXTREME_IMMEDIATE_LIKELY_LANGUAGE /*4385*/:
                return MESSAGE_TYPE_CBS_MESSAGE;
            case SmsCbConstants.MESSAGE_ID_CMAS_ALERT_EXTREME_EXPECTED_OBSERVED /*4373*/:
            case SmsCbConstants.MESSAGE_ID_CMAS_ALERT_EXTREME_EXPECTED_LIKELY /*4374*/:
            case SmsCbConstants.MESSAGE_ID_CMAS_ALERT_SEVERE_IMMEDIATE_OBSERVED /*4375*/:
            case SmsCbConstants.MESSAGE_ID_CMAS_ALERT_SEVERE_IMMEDIATE_LIKELY /*4376*/:
            case SmsCbConstants.MESSAGE_ID_CMAS_ALERT_SEVERE_EXPECTED_OBSERVED /*4377*/:
            case SmsCbConstants.MESSAGE_ID_CMAS_ALERT_SEVERE_EXPECTED_LIKELY /*4378*/:
            case SmsCbConstants.MESSAGE_ID_CMAS_ALERT_EXTREME_EXPECTED_OBSERVED_LANGUAGE /*4386*/:
            case SmsCbConstants.MESSAGE_ID_CMAS_ALERT_EXTREME_EXPECTED_LIKELY_LANGUAGE /*4387*/:
            case SmsCbConstants.MESSAGE_ID_CMAS_ALERT_SEVERE_IMMEDIATE_OBSERVED_LANGUAGE /*4388*/:
            case SmsCbConstants.MESSAGE_ID_CMAS_ALERT_SEVERE_IMMEDIATE_LIKELY_LANGUAGE /*4389*/:
            case SmsCbConstants.MESSAGE_ID_CMAS_ALERT_SEVERE_EXPECTED_OBSERVED_LANGUAGE /*4390*/:
            case SmsCbConstants.MESSAGE_ID_CMAS_ALERT_SEVERE_EXPECTED_LIKELY_LANGUAGE /*4391*/:
                return FORMAT_UMTS;
            case SmsCbConstants.MESSAGE_ID_CMAS_ALERT_CHILD_ABDUCTION_EMERGENCY /*4379*/:
            case SmsCbConstants.MESSAGE_ID_CMAS_ALERT_CHILD_ABDUCTION_EMERGENCY_LANGUAGE /*4392*/:
                return FORMAT_ETWS_PRIMARY;
            case SmsCbConstants.MESSAGE_ID_CMAS_ALERT_REQUIRED_MONTHLY_TEST /*4380*/:
            case SmsCbConstants.MESSAGE_ID_CMAS_ALERT_REQUIRED_MONTHLY_TEST_LANGUAGE /*4393*/:
                return 4;
            case SmsCbConstants.MESSAGE_ID_CMAS_ALERT_EXERCISE /*4381*/:
            case SmsCbConstants.MESSAGE_ID_CMAS_ALERT_EXERCISE_LANGUAGE /*4394*/:
                return 5;
            case SmsCbConstants.MESSAGE_ID_CMAS_ALERT_OPERATOR_DEFINED_USE /*4382*/:
            case SmsCbConstants.MESSAGE_ID_CMAS_ALERT_OPERATOR_DEFINED_USE_LANGUAGE /*4395*/:
                return PDU_HEADER_LENGTH;
            default:
                return -1;
        }
    }

    private int getCmasSeverity() {
        switch (this.mMessageIdentifier) {
            case SmsCbConstants.MESSAGE_ID_CMAS_ALERT_EXTREME_IMMEDIATE_OBSERVED /*4371*/:
            case SmsCbConstants.MESSAGE_ID_CMAS_ALERT_EXTREME_IMMEDIATE_LIKELY /*4372*/:
            case SmsCbConstants.MESSAGE_ID_CMAS_ALERT_EXTREME_EXPECTED_OBSERVED /*4373*/:
            case SmsCbConstants.MESSAGE_ID_CMAS_ALERT_EXTREME_EXPECTED_LIKELY /*4374*/:
            case SmsCbConstants.MESSAGE_ID_CMAS_ALERT_EXTREME_IMMEDIATE_OBSERVED_LANGUAGE /*4384*/:
            case SmsCbConstants.MESSAGE_ID_CMAS_ALERT_EXTREME_IMMEDIATE_LIKELY_LANGUAGE /*4385*/:
            case SmsCbConstants.MESSAGE_ID_CMAS_ALERT_EXTREME_EXPECTED_OBSERVED_LANGUAGE /*4386*/:
            case SmsCbConstants.MESSAGE_ID_CMAS_ALERT_EXTREME_EXPECTED_LIKELY_LANGUAGE /*4387*/:
                return 0;
            case SmsCbConstants.MESSAGE_ID_CMAS_ALERT_SEVERE_IMMEDIATE_OBSERVED /*4375*/:
            case SmsCbConstants.MESSAGE_ID_CMAS_ALERT_SEVERE_IMMEDIATE_LIKELY /*4376*/:
            case SmsCbConstants.MESSAGE_ID_CMAS_ALERT_SEVERE_EXPECTED_OBSERVED /*4377*/:
            case SmsCbConstants.MESSAGE_ID_CMAS_ALERT_SEVERE_EXPECTED_LIKELY /*4378*/:
            case SmsCbConstants.MESSAGE_ID_CMAS_ALERT_SEVERE_IMMEDIATE_OBSERVED_LANGUAGE /*4388*/:
            case SmsCbConstants.MESSAGE_ID_CMAS_ALERT_SEVERE_IMMEDIATE_LIKELY_LANGUAGE /*4389*/:
            case SmsCbConstants.MESSAGE_ID_CMAS_ALERT_SEVERE_EXPECTED_OBSERVED_LANGUAGE /*4390*/:
            case SmsCbConstants.MESSAGE_ID_CMAS_ALERT_SEVERE_EXPECTED_LIKELY_LANGUAGE /*4391*/:
                return MESSAGE_TYPE_CBS_MESSAGE;
            default:
                return -1;
        }
    }

    private int getCmasUrgency() {
        switch (this.mMessageIdentifier) {
            case SmsCbConstants.MESSAGE_ID_CMAS_ALERT_EXTREME_IMMEDIATE_OBSERVED /*4371*/:
            case SmsCbConstants.MESSAGE_ID_CMAS_ALERT_EXTREME_IMMEDIATE_LIKELY /*4372*/:
            case SmsCbConstants.MESSAGE_ID_CMAS_ALERT_SEVERE_IMMEDIATE_OBSERVED /*4375*/:
            case SmsCbConstants.MESSAGE_ID_CMAS_ALERT_SEVERE_IMMEDIATE_LIKELY /*4376*/:
            case SmsCbConstants.MESSAGE_ID_CMAS_ALERT_EXTREME_IMMEDIATE_OBSERVED_LANGUAGE /*4384*/:
            case SmsCbConstants.MESSAGE_ID_CMAS_ALERT_EXTREME_IMMEDIATE_LIKELY_LANGUAGE /*4385*/:
            case SmsCbConstants.MESSAGE_ID_CMAS_ALERT_SEVERE_IMMEDIATE_OBSERVED_LANGUAGE /*4388*/:
            case SmsCbConstants.MESSAGE_ID_CMAS_ALERT_SEVERE_IMMEDIATE_LIKELY_LANGUAGE /*4389*/:
                return 0;
            case SmsCbConstants.MESSAGE_ID_CMAS_ALERT_EXTREME_EXPECTED_OBSERVED /*4373*/:
            case SmsCbConstants.MESSAGE_ID_CMAS_ALERT_EXTREME_EXPECTED_LIKELY /*4374*/:
            case SmsCbConstants.MESSAGE_ID_CMAS_ALERT_SEVERE_EXPECTED_OBSERVED /*4377*/:
            case SmsCbConstants.MESSAGE_ID_CMAS_ALERT_SEVERE_EXPECTED_LIKELY /*4378*/:
            case SmsCbConstants.MESSAGE_ID_CMAS_ALERT_EXTREME_EXPECTED_OBSERVED_LANGUAGE /*4386*/:
            case SmsCbConstants.MESSAGE_ID_CMAS_ALERT_EXTREME_EXPECTED_LIKELY_LANGUAGE /*4387*/:
            case SmsCbConstants.MESSAGE_ID_CMAS_ALERT_SEVERE_EXPECTED_OBSERVED_LANGUAGE /*4390*/:
            case SmsCbConstants.MESSAGE_ID_CMAS_ALERT_SEVERE_EXPECTED_LIKELY_LANGUAGE /*4391*/:
                return MESSAGE_TYPE_CBS_MESSAGE;
            default:
                return -1;
        }
    }

    private int getCmasCertainty() {
        switch (this.mMessageIdentifier) {
            case SmsCbConstants.MESSAGE_ID_CMAS_ALERT_EXTREME_IMMEDIATE_OBSERVED /*4371*/:
            case SmsCbConstants.MESSAGE_ID_CMAS_ALERT_EXTREME_EXPECTED_OBSERVED /*4373*/:
            case SmsCbConstants.MESSAGE_ID_CMAS_ALERT_SEVERE_IMMEDIATE_OBSERVED /*4375*/:
            case SmsCbConstants.MESSAGE_ID_CMAS_ALERT_SEVERE_EXPECTED_OBSERVED /*4377*/:
            case SmsCbConstants.MESSAGE_ID_CMAS_ALERT_EXTREME_IMMEDIATE_OBSERVED_LANGUAGE /*4384*/:
            case SmsCbConstants.MESSAGE_ID_CMAS_ALERT_EXTREME_EXPECTED_OBSERVED_LANGUAGE /*4386*/:
            case SmsCbConstants.MESSAGE_ID_CMAS_ALERT_SEVERE_IMMEDIATE_OBSERVED_LANGUAGE /*4388*/:
            case SmsCbConstants.MESSAGE_ID_CMAS_ALERT_SEVERE_EXPECTED_OBSERVED_LANGUAGE /*4390*/:
                return 0;
            case SmsCbConstants.MESSAGE_ID_CMAS_ALERT_EXTREME_IMMEDIATE_LIKELY /*4372*/:
            case SmsCbConstants.MESSAGE_ID_CMAS_ALERT_EXTREME_EXPECTED_LIKELY /*4374*/:
            case SmsCbConstants.MESSAGE_ID_CMAS_ALERT_SEVERE_IMMEDIATE_LIKELY /*4376*/:
            case SmsCbConstants.MESSAGE_ID_CMAS_ALERT_SEVERE_EXPECTED_LIKELY /*4378*/:
            case SmsCbConstants.MESSAGE_ID_CMAS_ALERT_EXTREME_IMMEDIATE_LIKELY_LANGUAGE /*4385*/:
            case SmsCbConstants.MESSAGE_ID_CMAS_ALERT_EXTREME_EXPECTED_LIKELY_LANGUAGE /*4387*/:
            case SmsCbConstants.MESSAGE_ID_CMAS_ALERT_SEVERE_IMMEDIATE_LIKELY_LANGUAGE /*4389*/:
            case SmsCbConstants.MESSAGE_ID_CMAS_ALERT_SEVERE_EXPECTED_LIKELY_LANGUAGE /*4391*/:
                return MESSAGE_TYPE_CBS_MESSAGE;
            default:
                return -1;
        }
    }

    public String toString() {
        return "SmsCbHeader{GS=" + this.mGeographicalScope + ", serialNumber=0x" + Integer.toHexString(this.mSerialNumber) + ", messageIdentifier=0x" + Integer.toHexString(this.mMessageIdentifier) + ", DCS=0x" + Integer.toHexString(this.mDataCodingScheme) + ", page " + this.mPageIndex + " of " + this.mNrOfPages + '}';
    }
}
