package com.android.server.rms.statistic;

import android.util.Pair;
import com.android.server.security.securitydiagnose.HwSecDiagnoseConstant;
import java.util.ArrayList;

public final class HwResRecord {
    private static final String TAG = "HwResRecord";
    private final ArrayList<Aspect> mAspectData;
    private final String mGroupName;
    private final int mLevel;
    private final String mSubTypeName;

    public static class Aspect {
        public String name;
        public int value;

        public Aspect(String name, int value) {
            this.name = name;
            this.value = value;
        }
    }

    public HwResRecord(String groupName, String subTypeName, int level) {
        this.mAspectData = new ArrayList(0);
        this.mGroupName = groupName;
        this.mSubTypeName = subTypeName;
        this.mLevel = level;
    }

    public void updateAspectData(ArrayList<Pair<String, Integer>> data) {
        if (data != null && data.size() > 0) {
            for (int i = 0; i < data.size(); i++) {
                Pair item = (Pair) data.get(i);
                if (!(item == null || item.first == null || item.second == null || ((Integer) item.second).intValue() < 0)) {
                    Aspect aspect = null;
                    if (i < this.mAspectData.size()) {
                        aspect = (Aspect) this.mAspectData.get(i);
                    }
                    if (aspect == null) {
                        this.mAspectData.add(i, new Aspect((String) item.first, ((Integer) item.second).intValue()));
                    } else if (item.first.equals(aspect.name)) {
                        int intValue;
                        if (aspect.value > 0) {
                            intValue = ((Integer) item.second).intValue() + aspect.value;
                        } else {
                            intValue = ((Integer) item.second).intValue();
                        }
                        aspect.value = intValue;
                    }
                }
            }
        }
    }

    public String getGroupName() {
        return this.mGroupName;
    }

    public String getSubTypeName() {
        return this.mSubTypeName;
    }

    public int getLevel() {
        return this.mLevel;
    }

    public ArrayList<Aspect> getAspectData() {
        return this.mAspectData;
    }

    public void resetAspectData() {
        for (Aspect item : this.mAspectData) {
            if (item != null) {
                item.value = -1;
            }
        }
    }

    public String toString() {
        StringBuilder buffer = new StringBuilder(HwSecDiagnoseConstant.BIT_VERIFYBOOT);
        buffer.append(" [ ");
        for (Aspect data : this.mAspectData) {
            buffer.append(data.name).append(":").append(data.value).append(" ");
        }
        buffer.append("]");
        return buffer.toString();
    }
}
