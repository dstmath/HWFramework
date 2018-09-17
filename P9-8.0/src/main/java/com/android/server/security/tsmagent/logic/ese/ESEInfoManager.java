package com.android.server.security.tsmagent.logic.ese;

import android.content.Context;
import com.android.server.security.tsmagent.utils.HexByteHelper;
import com.android.server.security.tsmagent.utils.HwLog;
import java.io.IOException;
import org.simalliance.openmobileapi.Channel;
import org.simalliance.openmobileapi.Reader;
import org.simalliance.openmobileapi.SEService;
import org.simalliance.openmobileapi.SEService.CallBack;
import org.simalliance.openmobileapi.Session;

public class ESEInfoManager {
    private static final String AMSD_AID = "A000000151000000";
    private static final String APDU_QUERY_CPLC = "80CA9F7F00";
    private static final String CPLC_STORAGE_TAG = "cplc_storage_tag";
    private static ESEInfoManager instance;
    private static final Object serviceLock = new Object();
    private Context mContext;
    private SEService mService;
    private Session mSession;

    public static ESEInfoManager getInstance(Context context) {
        synchronized (serviceLock) {
            if (instance == null) {
                instance = new ESEInfoManager(context);
            }
        }
        return instance;
    }

    private ESEInfoManager(Context context) {
        this.mContext = context.getApplicationContext();
    }

    private boolean initSession() {
        boolean z = false;
        SEService sEService = new SEService(this.mContext, new CallBack() {
            public void serviceConnected(SEService arg0) {
                synchronized (ESEInfoManager.serviceLock) {
                    ESEInfoManager.this.mService = arg0;
                    ESEInfoManager.serviceLock.notifyAll();
                }
            }
        });
        synchronized (serviceLock) {
            while (this.mService == null) {
                try {
                    serviceLock.wait();
                } catch (InterruptedException e) {
                    HwLog.d("wait interrupted.");
                }
            }
        }
        Reader[] readers = this.mService.getReaders();
        if (readers.length < 1) {
            HwLog.d("available readers not exist.");
            return false;
        }
        HwLog.i("The reader nums : " + readers.length);
        Reader eSeReader = null;
        for (int i = 0; i < readers.length; i++) {
            HwLog.i("The " + i + " reader name is : " + readers[i].getName());
            if (readers[i].getName().contains("eSE")) {
                eSeReader = readers[i];
            }
        }
        if (eSeReader == null) {
            HwLog.e("can not access ESEReader");
            return false;
        }
        try {
            this.mSession = eSeReader.openSession();
            StringBuilder append = new StringBuilder().append("get session succesfully ? ");
            if (this.mSession != null) {
                z = true;
            }
            HwLog.i(append.append(z).toString());
            return true;
        } catch (IOException ex) {
            HwLog.e("get session exception : " + ex);
            return false;
        }
    }

    public void releaseSession() {
        if (!(this.mSession == null || (this.mSession.isClosed() ^ 1) == 0)) {
            HwLog.d("close channels in the session begin.");
            this.mSession.closeChannels();
            HwLog.d("close channels in the session end.");
            HwLog.d("close the session begin.");
            this.mSession.close();
            HwLog.d("close the session end.");
        }
        if (this.mService != null && this.mService.isConnected()) {
            HwLog.d("shutdown the connection to SE begin.");
            this.mService.shutdown();
            this.mService = null;
            HwLog.d("shutdown the connection to SE end.");
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:22:0x00b9 A:{Splitter: B:1:0x0008, PHI: r0 , ExcHandler: java.io.IOException (e java.io.IOException)} */
    /* JADX WARNING: Removed duplicated region for block: B:22:0x00b9 A:{Splitter: B:1:0x0008, PHI: r0 , ExcHandler: java.io.IOException (e java.io.IOException)} */
    /* JADX WARNING: Removed duplicated region for block: B:22:0x00b9 A:{Splitter: B:1:0x0008, PHI: r0 , ExcHandler: java.io.IOException (e java.io.IOException)} */
    /* JADX WARNING: Removed duplicated region for block: B:22:0x00b9 A:{Splitter: B:1:0x0008, PHI: r0 , ExcHandler: java.io.IOException (e java.io.IOException)} */
    /* JADX WARNING: Removed duplicated region for block: B:22:0x00b9 A:{Splitter: B:1:0x0008, PHI: r0 , ExcHandler: java.io.IOException (e java.io.IOException)} */
    /* JADX WARNING: Missing block: B:24:?, code:
            com.android.server.security.tsmagent.utils.HwLog.e("getCplc Exception : Session or Channel operation failed");
     */
    /* JADX WARNING: Missing block: B:28:0x00ca, code:
            com.android.server.security.tsmagent.utils.HwLog.d("getCplc, channel close begin.");
            r0.close();
            com.android.server.security.tsmagent.utils.HwLog.d("getCplc, channel close end.");
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public String getCplc() {
        HwLog.d("getCplc begin.");
        String cplcInfo = null;
        Channel channel = null;
        try {
            HwLog.d("getCplc, openLogicalChannel begin.");
            channel = this.mSession.openLogicalChannel(HexByteHelper.hexStringToByteArray(AMSD_AID));
            HwLog.d("getCplc, openLogicalChannel end.");
            if (channel != null) {
                HwLog.d("getCplc, get the channel to transmit now.");
                String reqStr = APDU_QUERY_CPLC;
                HwLog.d("getCplc request apdu : " + reqStr);
                String resultStr = HexByteHelper.byteArrayToHexString(channel.transmit(HexByteHelper.hexStringToByteArray(reqStr)));
                HwLog.d("getCplc response end");
                if (resultStr != null && resultStr.startsWith("9F7F") && resultStr.endsWith("9000")) {
                    cplcInfo = resultStr.substring(6, (HexByteHelper.hexStringToDecimalInteger(resultStr.substring(4, 6)) * 2) + 6);
                } else {
                    HwLog.e("Bad apdu response");
                }
            }
            if (!(channel == null || (channel.isClosed() ^ 1) == 0)) {
                HwLog.d("getCplc, channel close begin.");
                channel.close();
                HwLog.d("getCplc, channel close end.");
            }
        } catch (IOException e) {
        } catch (IndexOutOfBoundsException e2) {
            HwLog.e("getCplc Exception : bad returned substring");
            if (!(channel == null || (channel.isClosed() ^ 1) == 0)) {
                HwLog.d("getCplc, channel close begin.");
                channel.close();
                HwLog.d("getCplc, channel close end.");
            }
        } catch (Throwable th) {
            if (!(channel == null || (channel.isClosed() ^ 1) == 0)) {
                HwLog.d("getCplc, channel close begin.");
                channel.close();
                HwLog.d("getCplc, channel close end.");
            }
        }
        HwLog.d("getCplc : " + (cplcInfo == null ? "failed" : "success"));
        return cplcInfo;
    }

    public String queryCplc() {
        if (!initSession()) {
            return null;
        }
        String cplc = getCplc();
        releaseSession();
        return cplc;
    }
}
