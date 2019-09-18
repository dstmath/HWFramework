package com.huawei.wallet.sdk.common.apdu.model;

import com.huawei.wallet.sdk.common.utils.StringUtil;
import java.util.Locale;

public class ApduCommand {
    private static final int SW_LENGTH = 4;
    private String apdu;
    private String checker;
    private int index;
    private String rapdu;
    private String sw;

    public ApduCommand() {
    }

    public ApduCommand(int index2, String apdu2, String checker2) {
        this.index = index2;
        this.apdu = apdu2;
        this.checker = checker2;
    }

    public int getIndex() {
        return this.index;
    }

    public void setIndex(int index2) {
        this.index = index2;
    }

    public String getApdu() {
        return this.apdu;
    }

    public void setApdu(String apdu2) {
        this.apdu = apdu2;
    }

    public String getRapdu() {
        return this.rapdu;
    }

    public void setRapdu(String rapdu2) {
        this.rapdu = rapdu2;
    }

    public String getChecker() {
        return this.checker;
    }

    public void setChecker(String checker2) {
        this.checker = checker2;
    }

    public void setChecker(String[] checkers) {
        if (checkers == null || checkers.length <= 0) {
            this.checker = null;
            return;
        }
        StringBuilder sBuilder = new StringBuilder();
        for (int i = 0; i < checkers.length; i++) {
            sBuilder.append(checkers[i].toUpperCase(Locale.getDefault()));
            if (i != checkers.length - 1) {
                sBuilder.append("|");
            }
        }
        this.checker = sBuilder.toString();
    }

    public String getSw() {
        return this.sw;
    }

    public void setSw(String sw2) {
        this.sw = sw2;
    }

    public void parseRapduAndSw(String resp) {
        if (StringUtil.isEmpty(resp, true) || resp.length() < 4) {
            this.rapdu = resp;
            return;
        }
        this.rapdu = resp.substring(0, resp.length() - 4);
        this.sw = resp.substring(resp.length() - 4, resp.length());
    }

    public String toString() {
        return "ApduCommand{index=" + this.index + ", apdu='" + this.apdu + '\'' + ", rapdu='" + this.rapdu + '\'' + ", checker='" + this.checker + '\'' + ", sw='" + this.sw + '\'' + '}';
    }
}
