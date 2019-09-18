package com.android.server.hidata.wavemapping.chr.entity;

import com.android.server.hidata.wavemapping.util.LogUtil;

public class ApChrStatInfo {
    private int enterpriseAp = 0;
    private int finalUsed = 0;
    private int mobileApSrc1 = 0;
    private int mobileApSrc2 = 0;
    private int mobileApSrc3 = 0;
    private int result = 0;
    private int totalFound = 0;
    private int update = 0;

    public ApChrStatInfo() {
    }

    public ApChrStatInfo(int result2) {
        this.result = result2;
    }

    public ApChrStatInfo str2ApChrStatInfo(String strInfo) {
        if (strInfo == null || strInfo.equals("")) {
            return null;
        }
        String[] infos = strInfo.split("_");
        if (infos.length < 8) {
            return null;
        }
        ApChrStatInfo apChrStatInfo = new ApChrStatInfo();
        try {
            if (isNumeric(infos[0])) {
                apChrStatInfo.setResult(Integer.parseInt(infos[0]));
            }
            if (isNumeric(infos[1])) {
                apChrStatInfo.setEnterpriseAp(Integer.parseInt(infos[1]));
            }
            if (isNumeric(infos[2])) {
                apChrStatInfo.setMobileApSrc1(Integer.parseInt(infos[2]));
            }
            if (isNumeric(infos[3])) {
                apChrStatInfo.setMobileApSrc2(Integer.parseInt(infos[3]));
            }
            if (isNumeric(infos[4])) {
                apChrStatInfo.setMobileApSrc3(Integer.parseInt(infos[4]));
            }
            if (isNumeric(infos[5])) {
                apChrStatInfo.setUpdate(Integer.parseInt(infos[5]));
            }
            if (isNumeric(infos[6])) {
                apChrStatInfo.setTotalFound(Integer.parseInt(infos[6]));
            }
            if (isNumeric(infos[7])) {
                apChrStatInfo.setFinalUsed(Integer.parseInt(infos[7]));
            }
        } catch (NumberFormatException e) {
            LogUtil.e(e.getMessage());
        }
        return apChrStatInfo;
    }

    public static boolean isNumeric(String str) {
        for (int i = 0; i < str.length(); i++) {
            if (!Character.isDigit(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public int getResult() {
        return this.result;
    }

    public void setResult(int result2) {
        this.result = result2;
    }

    public int getEnterpriseAp() {
        return this.enterpriseAp;
    }

    public void setEnterpriseAp(int enterpriseAp2) {
        this.enterpriseAp = enterpriseAp2;
    }

    public int getMobileApSrc1() {
        return this.mobileApSrc1;
    }

    public void setMobileApSrc1(int mobileApSrc12) {
        this.mobileApSrc1 = mobileApSrc12;
    }

    public int getMobileApSrc2() {
        return this.mobileApSrc2;
    }

    public void setMobileApSrc2(int mobileApSrc22) {
        this.mobileApSrc2 = mobileApSrc22;
    }

    public int getMobileApSrc3() {
        return this.mobileApSrc3;
    }

    public void setMobileApSrc3(int mobileApSrc32) {
        this.mobileApSrc3 = mobileApSrc32;
    }

    public int getUpdate() {
        return this.update;
    }

    public void setUpdate(int update2) {
        this.update = update2;
    }

    public int getTotalFound() {
        return this.totalFound;
    }

    public void setTotalFound(int totalFound2) {
        this.totalFound = totalFound2;
    }

    public int getFinalUsed() {
        return this.finalUsed;
    }

    public void setFinalUsed(int finalUsed2) {
        this.finalUsed = finalUsed2;
    }

    public String toString() {
        return this.result + "_" + this.enterpriseAp + "_" + this.mobileApSrc1 + "_" + this.mobileApSrc2 + "_" + this.mobileApSrc3 + "_" + this.update + "_" + this.totalFound + "_" + this.finalUsed;
    }
}
