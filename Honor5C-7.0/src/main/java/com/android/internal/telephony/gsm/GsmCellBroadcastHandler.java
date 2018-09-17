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
import com.google.android.mms.pdu.PduPersister;
import com.huawei.internal.telephony.gsm.HwCustGsmCellBroadcastHandler;
import java.util.HashMap;
import java.util.Iterator;

public class GsmCellBroadcastHandler extends CellBroadcastHandler {
    private static final boolean VDBG = false;
    private HwCustGsmCellBroadcastHandler mHwCustGsmCellBroadcastHandler;
    private final HashMap<SmsCbConcatInfo, byte[][]> mSmsCbPageMap;

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
            if (this.mHeader.getSerialNumber() == other.mHeader.getSerialNumber()) {
                z = this.mLocation.equals(other.mLocation);
            }
            return z;
        }

        public boolean matchesLocation(String plmn, int lac, int cid) {
            return this.mLocation.isInLocationArea(plmn, lac, cid);
        }
    }

    protected GsmCellBroadcastHandler(Context context, Phone phone) {
        super("GsmCellBroadcastHandler", context, phone);
        this.mSmsCbPageMap = new HashMap(4);
        this.mHwCustGsmCellBroadcastHandler = new HwCustGsmCellBroadcastHandler();
        phone.mCi.setOnNewGsmBroadcastSms(getHandler(), 1, null);
    }

    protected void onQuitting() {
        this.mPhone.mCi.unSetOnNewGsmBroadcastSms(getHandler());
        super.onQuitting();
    }

    public static GsmCellBroadcastHandler makeGsmCellBroadcastHandler(Context context, Phone phone) {
        GsmCellBroadcastHandler handler = new GsmCellBroadcastHandler(context, phone);
        handler.start();
        return handler;
    }

    protected boolean handleSmsMessage(Message message) {
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
        try {
            SmsCbLocation location;
            byte[][] pdus;
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
            switch (header.getGeographicalScope()) {
                case PduPersister.LOAD_MODE_MMS_COMMON /*0*/:
                case PduPersister.PROC_STATUS_COMPLETED /*3*/:
                    location = new SmsCbLocation(plmn, lac, cid);
                    break;
                case PduPersister.PROC_STATUS_PERMANENTLY_FAILURE /*2*/:
                    location = new SmsCbLocation(plmn, lac, -1);
                    break;
                default:
                    location = new SmsCbLocation(plmn);
                    break;
            }
            int pageCount = header.getNumberOfPages();
            if (pageCount > 1) {
                SmsCbConcatInfo concatInfo = new SmsCbConcatInfo(header, location);
                pdus = (byte[][]) this.mSmsCbPageMap.get(concatInfo);
                if (pdus == null) {
                    pdus = new byte[pageCount][];
                    this.mSmsCbPageMap.put(concatInfo, pdus);
                }
                pdus[header.getPageIndex() - 1] = receivedPdu;
                for (byte[] pdu : pdus) {
                    if (pdu == null) {
                        return null;
                    }
                }
                this.mSmsCbPageMap.remove(concatInfo);
            } else {
                pdus = new byte[][]{receivedPdu};
            }
            Iterator<SmsCbConcatInfo> iter = this.mSmsCbPageMap.keySet().iterator();
            while (iter.hasNext()) {
                if (!((SmsCbConcatInfo) iter.next()).matchesLocation(plmn, lac, cid)) {
                    iter.remove();
                }
            }
            return GsmSmsCbMessage.createSmsCbMessage(header, location, pdus);
        } catch (RuntimeException e) {
            loge("Error in decoding SMS CB pdu", e);
            return null;
        }
    }
}
