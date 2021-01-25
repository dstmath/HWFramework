package com.android.internal.telephony.gsm;

import android.content.Context;
import android.os.AsyncResult;
import android.os.Message;
import android.telephony.CellLocation;
import android.telephony.SmsCbLocation;
import android.telephony.SmsCbMessage;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import com.android.internal.telephony.CellBroadcastHandler;
import com.android.internal.telephony.Phone;
import com.huawei.internal.telephony.gsm.HwCustGsmCellBroadcastHandler;
import huawei.cust.HwCustUtils;
import java.util.HashMap;
import java.util.Iterator;

public class GsmCellBroadcastHandler extends CellBroadcastHandler {
    private static final boolean VDBG = false;
    private HwCustGsmCellBroadcastHandler mHwCustGsmCellBroadcastHandler;
    private final HashMap<SmsCbConcatInfo, byte[][]> mSmsCbPageMap = new HashMap<>(4);

    protected GsmCellBroadcastHandler(Context context, Phone phone) {
        super("GsmCellBroadcastHandler", context, phone);
        phone.mCi.setOnNewGsmBroadcastSms(getHandler(), 1, null);
        this.mHwCustGsmCellBroadcastHandler = (HwCustGsmCellBroadcastHandler) HwCustUtils.createObj(HwCustGsmCellBroadcastHandler.class, new Object[]{phone});
    }

    /* access modifiers changed from: protected */
    @Override // com.android.internal.telephony.WakeLockStateMachine
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
    @Override // com.android.internal.telephony.CellBroadcastHandler, com.android.internal.telephony.WakeLockStateMachine
    public boolean handleSmsMessage(Message message) {
        SmsCbMessage cbMessage;
        if (!(message.obj instanceof AsyncResult) || (cbMessage = handleGsmBroadcastSms((AsyncResult) message.obj)) == null) {
            return super.handleSmsMessage(message);
        }
        handleBroadcastSms(cbMessage);
        return true;
    }

    /* JADX WARNING: Removed duplicated region for block: B:19:0x0068 A[Catch:{ RuntimeException -> 0x0101 }] */
    /* JADX WARNING: Removed duplicated region for block: B:30:0x009e A[Catch:{ RuntimeException -> 0x0101 }] */
    /* JADX WARNING: Removed duplicated region for block: B:34:0x00b4 A[Catch:{ RuntimeException -> 0x0101 }] */
    /* JADX WARNING: Removed duplicated region for block: B:40:0x00c8 A[Catch:{ RuntimeException -> 0x0101 }] */
    private SmsCbMessage handleGsmBroadcastSms(AsyncResult ar) {
        SmsCbLocation location;
        int pageCount;
        byte[][] pdus;
        Iterator<SmsCbConcatInfo> iter;
        try {
            byte[] receivedPdu = (byte[]) ar.result;
            if (this.mHwCustGsmCellBroadcastHandler != null) {
                receivedPdu = this.mHwCustGsmCellBroadcastHandler.cbsPduAfterDiscardNullBlock(receivedPdu);
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
                if (geographicalScope == 2) {
                    location = new SmsCbLocation(plmn, lac, -1);
                } else if (geographicalScope != 3) {
                    location = new SmsCbLocation(plmn);
                }
                pageCount = header.getNumberOfPages();
                if (pageCount <= 1) {
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
                } else {
                    pdus = new byte[][]{receivedPdu};
                }
                iter = this.mSmsCbPageMap.keySet().iterator();
                while (iter.hasNext()) {
                    if (!iter.next().matchesLocation(plmn, lac, cid)) {
                        iter.remove();
                    }
                }
                if (this.mHwCustGsmCellBroadcastHandler != null) {
                    if (!this.mHwCustGsmCellBroadcastHandler.checkETWSBeLatePN(header.isEmergencyMessage(), (header.getServiceCategory() & 65528) == 4352, header.isEtwsPrimaryNotification(), header.getSerialNumber(), header.getServiceCategory())) {
                        return GsmSmsCbMessage.createSmsCbMessage(this.mContext, header, location, pdus);
                    }
                }
                log("PN is later than SN for etws");
                return null;
            }
            location = new SmsCbLocation(plmn, lac, cid);
            pageCount = header.getNumberOfPages();
            if (pageCount <= 1) {
            }
            iter = this.mSmsCbPageMap.keySet().iterator();
            while (iter.hasNext()) {
            }
            if (this.mHwCustGsmCellBroadcastHandler != null) {
            }
            log("PN is later than SN for etws");
            return null;
        } catch (RuntimeException e) {
            loge("Error in decoding SMS CB pdu", e);
            return null;
        }
    }

    /* access modifiers changed from: private */
    public static final class SmsCbConcatInfo {
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
            if (!(obj instanceof SmsCbConcatInfo)) {
                return false;
            }
            SmsCbConcatInfo other = (SmsCbConcatInfo) obj;
            if (this.mHeader.getSerialNumber() != other.mHeader.getSerialNumber() || !this.mLocation.equals(other.mLocation)) {
                return false;
            }
            return true;
        }

        public boolean matchesLocation(String plmn, int lac, int cid) {
            return this.mLocation.isInLocationArea(plmn, lac, cid);
        }
    }
}
