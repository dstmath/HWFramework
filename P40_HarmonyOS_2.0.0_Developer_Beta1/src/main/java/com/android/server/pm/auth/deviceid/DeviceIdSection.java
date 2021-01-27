package com.android.server.pm.auth.deviceid;

import android.text.TextUtils;
import com.huawei.hiai.awareness.AwarenessInnerConstants;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class DeviceIdSection implements DeviceId {
    private List<Section> mSections = new ArrayList();

    public static boolean isType(String deviceId) {
        return !TextUtils.isEmpty(deviceId) && deviceId.startsWith("IMEI/") && deviceId.indexOf(AwarenessInnerConstants.DASH_KEY) > 0;
    }

    @Override // com.android.server.pm.auth.deviceid.DeviceId
    public void addDeviceId(String deviceId) {
        if (!TextUtils.isEmpty(deviceId) && deviceId.indexOf(AwarenessInnerConstants.DASH_KEY) >= 0) {
            String[] times = deviceId.split(AwarenessInnerConstants.DASH_KEY);
            Section section = new Section();
            section.setStartImei(times[0]);
            section.setLastImei(times[1]);
            this.mSections.add(section);
        }
    }

    @Override // com.android.server.pm.auth.deviceid.DeviceId
    public void append(StringBuffer strBuf) {
        if (strBuf != null) {
            strBuf.append("IMEI/");
            int size = this.mSections.size();
            for (int i = 0; i < size; i++) {
                if (i != 0) {
                    strBuf.append(",");
                }
                strBuf.append(this.mSections.get(i).pieceString());
            }
        }
    }

    @Override // com.android.server.pm.auth.deviceid.DeviceId
    public boolean contain(String deviceId) {
        if (TextUtils.isEmpty(deviceId)) {
            return false;
        }
        int size = this.mSections.size();
        for (int i = 0; i < size; i++) {
            if (this.mSections.get(i).isBetweenSection(deviceId)) {
                return true;
            }
        }
        return false;
    }

    @Override // com.android.server.pm.auth.deviceid.DeviceId
    public boolean isEmpty() {
        return this.mSections.isEmpty();
    }

    private static class Section {
        private String mEndImei;
        private String mStartImei;

        private Section() {
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private String pieceString() {
            return this.mStartImei + AwarenessInnerConstants.DASH_KEY + this.mEndImei;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void setStartImei(String startImei) {
            this.mStartImei = startImei;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void setLastImei(String lastImei) {
            this.mEndImei = lastImei;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private boolean isBetweenSection(String deviceId) {
            BigDecimal id = new BigDecimal(deviceId);
            return id.compareTo(new BigDecimal(this.mStartImei)) >= 0 && id.compareTo(new BigDecimal(this.mEndImei)) <= 0;
        }
    }
}
