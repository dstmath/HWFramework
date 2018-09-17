package com.android.server.pm.auth.deviceid;

import android.annotation.SuppressLint;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class DeviceIdSection implements DeviceId {
    private List<Section> mSections = new ArrayList();

    private static class Section {
        private String mEndImei;
        private String mStartImei;

        /* synthetic */ Section(Section -this0) {
            this();
        }

        private Section() {
        }

        private String pieceString() {
            return this.mStartImei + "-" + this.mEndImei;
        }

        private void setStartImei(String mStartImei) {
            this.mStartImei = mStartImei;
        }

        private void setLastImei(String mLastImei) {
            this.mEndImei = mLastImei;
        }

        public boolean isBetweenSection(String devId) {
            BigDecimal dev = new BigDecimal(devId);
            BigDecimal start = new BigDecimal(this.mStartImei);
            BigDecimal end = new BigDecimal(this.mEndImei);
            if (dev.compareTo(start) < 0 || dev.compareTo(end) > 0) {
                return false;
            }
            return true;
        }
    }

    public void addDeviceId(String id) {
        if (id.indexOf("-") < 0) {
            throw new IllegalArgumentException("can not find - in section deviceids");
        }
        String[] times = id.split("-");
        Section sp = new Section();
        sp.setStartImei(times[0]);
        sp.setLastImei(times[1]);
        this.mSections.add(sp);
    }

    public static boolean isType(String ids) {
        if (!ids.startsWith("IMEI/") || ids.indexOf("-") <= 0) {
            return false;
        }
        return true;
    }

    @SuppressLint({"AvoidMethodInForLoop"})
    public void append(StringBuffer sb) {
        sb.append("IMEI/");
        for (int i = 0; i < this.mSections.size(); i++) {
            if (i != 0) {
                sb.append(",");
            }
            sb.append(((Section) this.mSections.get(i)).pieceString());
        }
    }

    public boolean contain(String devId) {
        for (Section section : this.mSections) {
            if (section.isBetweenSection(devId)) {
                return true;
            }
        }
        return false;
    }

    public boolean isEmpty() {
        return this.mSections.isEmpty();
    }
}
