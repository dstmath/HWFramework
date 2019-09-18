package com.android.internal.telephony.gsm;

import android.content.Context;
import android.os.AsyncResult;
import android.os.Message;
import android.os.SystemProperties;
import android.telephony.CellLocation;
import android.telephony.SmsCbLocation;
import android.telephony.SmsCbMessage;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import com.android.internal.telephony.CellBroadcastHandler;
import com.android.internal.telephony.Phone;
import com.huawei.internal.telephony.gsm.HwCustGsmCellBroadcastHandler;
import huawei.cust.HwCfgFilePolicy;
import huawei.cust.HwCustUtils;
import java.util.HashMap;
import java.util.Iterator;

public class GsmCellBroadcastHandler extends CellBroadcastHandler {
    private static final boolean IS_CBSPDU_HANDLER_NULL_MSG = SystemProperties.getBoolean("ro.config.cbs_del_2B", false);
    private static final boolean VDBG = false;
    private HwCustGsmCellBroadcastHandler mHwCustGsmCellBroadcastHandler = ((HwCustGsmCellBroadcastHandler) HwCustUtils.createObj(HwCustGsmCellBroadcastHandler.class, new Object[0]));
    private final HashMap<SmsCbConcatInfo, byte[][]> mSmsCbPageMap = new HashMap<>(4);

    private static final class SmsCbConcatInfo {
        private final SmsCbHeader mHeader;
        private final SmsCbLocation mLocation;

        SmsCbConcatInfo(SmsCbHeader header, SmsCbLocation location) {
            this.mHeader = header;
            this.mLocation = location;
        }

        public int hashCode() {
            return (this.mHeader.getSerialNumber() * 31) + this.mLocation.hashCode();
        }

        public boolean equals(Object obj) {
            boolean z = false;
            if (!(obj instanceof SmsCbConcatInfo)) {
                return false;
            }
            SmsCbConcatInfo other = (SmsCbConcatInfo) obj;
            if (this.mHeader.getSerialNumber() == other.mHeader.getSerialNumber() && this.mLocation.equals(other.mLocation)) {
                z = true;
            }
            return z;
        }

        public boolean matchesLocation(String plmn, int lac, int cid) {
            return this.mLocation.isInLocationArea(plmn, lac, cid);
        }
    }

    protected GsmCellBroadcastHandler(Context context, Phone phone) {
        super("GsmCellBroadcastHandler", context, phone);
        phone.mCi.setOnNewGsmBroadcastSms(getHandler(), 1, null);
    }

    /* access modifiers changed from: protected */
    public void onQuitting() {
        this.mPhone.mCi.unSetOnNewGsmBroadcastSms(getHandler());
        super.onQuitting();
    }

    public static GsmCellBroadcastHandler makeGsmCellBroadcastHandler(Context context, Phone phone) {
        GsmCellBroadcastHandler handler = new GsmCellBroadcastHandler(context, phone);
        handler.start();
        return handler;
    }

    /* access modifiers changed from: protected */
    public boolean handleSmsMessage(Message message) {
        if (message.obj instanceof AsyncResult) {
            SmsCbMessage cbMessage = handleGsmBroadcastSms((AsyncResult) message.obj);
            if (cbMessage != null) {
                handleBroadcastSms(cbMessage);
                return true;
            }
        }
        return super.handleSmsMessage(message);
    }

    private SmsCbMessage handleGsmBroadcastSms(AsyncResult ar) {
        SmsCbLocation location;
        boolean z;
        byte[][] pdus;
        try {
            byte[] receivedPdu = (byte[]) ar.result;
            boolean hasHwCfgConfig = false;
            boolean delCbsState = false;
            if (this.mPhone != null) {
                Boolean deleteCbs = (Boolean) HwCfgFilePolicy.getValue("cbs_del_2b_switch", SubscriptionManager.getSlotIndex(this.mPhone.getPhoneId()), Boolean.class);
                if (deleteCbs != null) {
                    delCbsState = deleteCbs.booleanValue();
                    hasHwCfgConfig = true;
                }
            }
            if (!hasHwCfgConfig || this.mHwCustGsmCellBroadcastHandler == null) {
                if (this.mHwCustGsmCellBroadcastHandler != null && IS_CBSPDU_HANDLER_NULL_MSG) {
                    receivedPdu = this.mHwCustGsmCellBroadcastHandler.cbsPduAfterDiscardNullBlock(receivedPdu);
                }
            } else if (delCbsState) {
                receivedPdu = this.mHwCustGsmCellBroadcastHandler.cbsPduAfterDiscardNullBlock(receivedPdu);
                log("The switch is true,del the CBS");
            } else {
                log("The switch is false, not del the CBS");
            }
            SmsCbHeader header = new SmsCbHeader(receivedPdu);
            String plmn = TelephonyManager.from(this.mContext).getNetworkOperatorForPhone(this.mPhone.getPhoneId());
            int lac = -1;
            int cid = -1;
            CellLocation cl = this.mPhone.getCellLocation();
            if (cl instanceof GsmCellLocation) {
                GsmCellLocation cellLocation = (GsmCellLocation) cl;
                lac = cellLocation.getLac();
                cid = cellLocation.getCid();
            }
            int geographicalScope = header.getGeographicalScope();
            if (geographicalScope != 0) {
                switch (geographicalScope) {
                    case 2:
                        location = new SmsCbLocation(plmn, lac, -1);
                        break;
                    case 3:
                        break;
                    default:
                        location = new SmsCbLocation(plmn);
                        break;
                }
            }
            location = new SmsCbLocation(plmn, lac, cid);
            int pageCount = header.getNumberOfPages();
            if (pageCount > 1) {
                SmsCbConcatInfo concatInfo = new SmsCbConcatInfo(header, location);
                pdus = this.mSmsCbPageMap.get(concatInfo);
                if (pdus == null) {
                    pdus = new byte[pageCount][];
                    this.mSmsCbPageMap.put(concatInfo, pdus);
                }
                pdus[header.getPageIndex() - 1] = receivedPdu;
                for (byte[] pdu : pdus) {
                    if (pdu == null) {
                        log("still missing pdu");
                        return null;
                    }
                }
                this.mSmsCbPageMap.remove(concatInfo);
                z = false;
            } else {
                z = false;
                pdus = new byte[][]{receivedPdu};
            }
            Iterator<SmsCbConcatInfo> iter = this.mSmsCbPageMap.keySet().iterator();
            while (iter.hasNext()) {
                if (!iter.next().matchesLocation(plmn, lac, cid)) {
                    iter.remove();
                }
            }
            byte[] bArr = receivedPdu;
            if (!this.mHwCustGsmCellBroadcastHandler.checkETWSBeLatePN(header.isEmergencyMessage(), (header.getServiceCategory() & 65528) == 4352 ? true : z, header.isEtwsPrimaryNotification(), header.getSerialNumber(), header.getServiceCategory())) {
                return GsmSmsCbMessage.createSmsCbMessage(this.mContext, header, location, pdus);
            }
            log("PN is later than SN for etws");
            return null;
        } catch (RuntimeException e) {
            loge("Error in decoding SMS CB pdu", e);
            return null;
        }
    }
}
