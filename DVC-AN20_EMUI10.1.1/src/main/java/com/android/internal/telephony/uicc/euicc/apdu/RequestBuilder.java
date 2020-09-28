package com.android.internal.telephony.uicc.euicc.apdu;

import com.android.internal.telephony.PhoneConfigurationManager;
import java.util.ArrayList;
import java.util.List;

public class RequestBuilder {
    private static final int CLA_STORE_DATA = 128;
    private static final int INS_STORE_DATA = 226;
    private static final int MAX_APDU_DATA_LEN = 255;
    private static final int MAX_EXT_APDU_DATA_LEN = 65535;
    private static final int P1_STORE_DATA_END = 145;
    private static final int P1_STORE_DATA_INTERM = 17;
    private final int mChannel;
    private final List<ApduCommand> mCommands = new ArrayList();
    private final int mMaxApduDataLen;

    public void addApdu(int cla, int ins, int p1, int p2, int p3, String cmdHex) {
        this.mCommands.add(new ApduCommand(this.mChannel, cla, ins, p1, p2, p3, cmdHex));
    }

    public void addApdu(int cla, int ins, int p1, int p2, String cmdHex) {
        this.mCommands.add(new ApduCommand(this.mChannel, cla, ins, p1, p2, cmdHex.length() / 2, cmdHex));
    }

    public void addApdu(int cla, int ins, int p1, int p2) {
        this.mCommands.add(new ApduCommand(this.mChannel, cla, ins, p1, p2, 0, PhoneConfigurationManager.SSSS));
    }

    public void addStoreData(String cmdHex) {
        int cmdLen = this.mMaxApduDataLen * 2;
        int startPos = 0;
        int totalLen = cmdHex.length() / 2;
        int totalSubCmds = 1;
        if (totalLen != 0) {
            int i = this.mMaxApduDataLen;
            totalSubCmds = ((totalLen + i) - 1) / i;
        }
        for (int i2 = 1; i2 < totalSubCmds; i2++) {
            addApdu(128, 226, 17, i2 - 1, cmdHex.substring(startPos, startPos + cmdLen));
            startPos += cmdLen;
        }
        addApdu(128, 226, 145, totalSubCmds - 1, cmdHex.substring(startPos));
    }

    /* access modifiers changed from: package-private */
    public List<ApduCommand> getCommands() {
        return this.mCommands;
    }

    RequestBuilder(int channel, boolean supportExtendedApdu) {
        this.mChannel = channel;
        this.mMaxApduDataLen = supportExtendedApdu ? 65535 : 255;
    }
}
