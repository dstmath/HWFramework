package com.huawei.wallet.sdk.common.apdu.oma;

import android.se.omapi.Channel;
import android.se.omapi.Session;
import android.text.TextUtils;
import com.huawei.wallet.sdk.common.apdu.IAPDUService;
import com.huawei.wallet.sdk.common.apdu.OmaException;
import com.huawei.wallet.sdk.common.apdu.util.HexByteHelper;
import com.huawei.wallet.sdk.common.apdu.util.OmaUtil;
import com.huawei.wallet.sdk.common.apdu.whitecard.WalletProcessTrace;
import com.huawei.wallet.sdk.common.log.LogC;
import java.io.IOException;
import java.util.MissingResourceException;
import java.util.NoSuchElementException;

public class NfcChannel extends WalletProcessTrace {
    private static final String ADPU = "6a82";
    private static final String ADPU_CAPITAL = "6A82";
    private static final String TAG = "NfcChannel|";
    private String aid;
    private Channel channel;
    private int channelType;
    private String selectResp;
    private Session session;

    public NfcChannel(Session session2) {
        this.session = session2;
    }

    public String openChannel() throws OmaException {
        NFCOpeningStatusHandler.getInstance().checkNFCOpening();
        LogC.d(getSubProcessPrefix() + IAPDUService.TAG + " NfcChannel open channel for " + this.aid + ", channelType " + this.channelType, false);
        byte[] instanceId = HexByteHelper.hexStringToByteArray(this.aid);
        try {
            if (this.channelType == 0) {
                this.channel = this.session.openLogicalChannel(instanceId);
            } else if (this.channelType == 1) {
                this.channel = this.session.openBasicChannel(instanceId);
            }
            byte[] respByte = null;
            if (this.channel != null) {
                respByte = this.channel.getSelectResponse();
            }
            if (respByte != null) {
                this.selectResp = HexByteHelper.byteArrayToHexString(respByte);
            }
            String msg = "OmaService NfcChannel open channel success for " + this.aid + " ,channelType " + this.channelType;
            LogC.i(getSubProcessPrefix() + msg, false);
            return this.selectResp;
        } catch (IOException e) {
            throw new OmaException(2002, "NfcChannel openChannel[ " + this.aid + "] failed IOException. msg : " + e.getMessage());
        } catch (SecurityException e2) {
            throw new OmaException(2003, "NfcChannel openChannel[" + this.aid + "] failed SecurityException. msg : " + e2.getMessage());
        } catch (NoSuchElementException e3) {
            int result = 2004;
            String errMsg = e3.getLocalizedMessage();
            String radpu = null;
            if (errMsg != null && (errMsg.contains(ADPU) || errMsg.contains(ADPU_CAPITAL))) {
                LogC.d(getSubProcessPrefix() + "the applet is not installed.", false);
                result = 2005;
                radpu = ADPU_CAPITAL;
            }
            OmaException omaException = new OmaException(result, "NfcChannel openChannel[" + this.aid + "] failed NoSuchElementException. msg : " + e3.getMessage());
            if (!TextUtils.isEmpty(radpu)) {
                omaException.setRapdu(radpu);
            }
            throw omaException;
        } catch (NullPointerException e4) {
            throw new OmaException(IAPDUService.RETURN_APDU_EXCUTE_OPENCHANNEL_NULLPOINTEREXCEPTION, "NfcChannel openChannel[" + this.aid + "] failed NullPointerException. msg : " + e4.getMessage());
        } catch (MissingResourceException e5) {
            throw new OmaException(IAPDUService.RETURN_APDU_EXCUTE_OPENCHANNEL_MISSRESOURCEEXCEPTION, "NfcChannel openChannel[" + this.aid + "] failed MissingResourceException. msg : " + e5.getMessage());
        } catch (Exception e6) {
            throw new OmaException(IAPDUService.RETURN_APDU_EXCUTE_OPENCHANNEL_EXCEPTION, "NfcChannel openChannel[" + this.aid + "] failed " + e6.getClass().getName() + ". msg : " + e6.getMessage());
        }
    }

    public String excuteApdu(String apdu) throws OmaException {
        if (this.channel != null) {
            String logApdu = OmaUtil.getLogApdu(apdu);
            try {
                String resp = HexByteHelper.byteArrayToHexString(this.channel.transmit(HexByteHelper.hexStringToByteArray(apdu)));
                LogC.d(getSubProcessPrefix() + IAPDUService.TAG + " NfcChannel transmit apdu : " + logApdu + " resp : " + resp, false);
                return resp;
            } catch (IOException e) {
                throw new OmaException(3002, "NfcChannel excuteApdu transmit apdu[" + logApdu + "] failed. IOException msg : " + e.getMessage());
            } catch (SecurityException e2) {
                throw new OmaException(3003, "NfcChannel excuteApdu transmit apdu[" + logApdu + "] failed. SecurityException. msg : " + e2.getMessage());
            } catch (NoSuchElementException e3) {
                throw new OmaException(3004, "NfcChannel excuteApdu transmit apdu[" + logApdu + "] failed. NoSuchElementException. msg : " + e3.getMessage());
            } catch (NullPointerException e4) {
                throw new OmaException(3005, "NfcChannel excuteApdu transmit apdu[" + logApdu + "] failed. NullPointerException. msg : " + e4.getMessage());
            } catch (MissingResourceException e5) {
                throw new OmaException(3006, "NfcChannel excuteApdu transmit apdu[" + logApdu + "] failed. MissingResourceException. msg : " + e5.getMessage());
            } catch (Exception e6) {
                throw new OmaException(IAPDUService.RETURN_APDU_EXCUTE_TRANSMIT_EXCEPTION, "NfcChannel excuteApdu transmit apdu[" + logApdu + "] failed. " + e6.getClass().getName() + ". msg : " + e6.getMessage());
            }
        } else {
            throw new OmaException(3001, "NfcChannel excuteApdu failed. channel is null");
        }
    }

    public void closeChannel() throws OmaException {
        try {
            if (this.channel != null && this.channel.isOpen()) {
                this.channel.close();
                LogC.i(getSubProcessPrefix() + IAPDUService.TAG + " NfcChannel closed, aid " + this.aid + " channelType " + this.channelType, false);
            }
        } catch (IllegalStateException e) {
            LogC.e(getSubProcessPrefix() + "NfcChannel close failed, aid " + this.aid + " channelType " + this.channelType + " " + e.getMessage(), false);
            this.session = null;
            throw new OmaException(5001, "closeChannel failed IllegalStateException. aid : " + this.aid + " channelType : " + this.channelType);
        } catch (Exception e2) {
            LogC.e(getSubProcessPrefix() + "NfcChannel close failed, aid " + this.aid + " channelType " + this.channelType + " " + e2.getMessage(), false);
            this.session = null;
            throw new OmaException(5001, "closeChannel failed Exception . aid : " + this.aid + " channelType : " + this.channelType);
        }
    }

    public String getAid() {
        return this.aid;
    }

    public void setAid(String aid2) {
        this.aid = aid2;
    }

    public void setChannelType(int channelType2) {
        this.channelType = channelType2;
    }

    public void setSession(Session session2) {
        this.session = session2;
    }

    public String getSelectResp() {
        return this.selectResp;
    }

    public void setProcessPrefix(String processPrefix, String tag) {
        super.setProcessPrefix(processPrefix, TAG);
    }
}
