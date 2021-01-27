package com.huawei.android.hardware.qcomfmradio;

import android.util.Log;
import com.huawei.android.hardware.fmradio.common.BaseFmConfig;
import com.huawei.android.os.SystemPropertiesEx;
import java.util.List;

public class QcomFmConfig implements BaseFmConfig {
    private static final String TAG = "FmConfig";
    private static final int V4L2_CID_PRIVATE_BASE = 134217728;
    private static final int V4L2_CID_PRIVATE_TAVARUA_EMPHASIS = 134217740;
    private static final int V4L2_CID_PRIVATE_TAVARUA_RDS_STD = 134217741;
    private static final int V4L2_CID_PRIVATE_TAVARUA_REGION = 134217735;
    private static final int V4L2_CID_PRIVATE_TAVARUA_SPACING = 134217742;
    private static final int V4L2_CID_PRIVATE_TAVARUA_SRCH_ALGORITHM = 134217771;
    private static final int each_Spur_entry_size = 16;
    public static final int no_Of_Spurs_For_Entry = 3;
    private int mBandLowerLimit;
    private int mBandUpperLimit;
    private int mChSpacing;
    private int mEmphasis;
    private int mRadioBand;
    private int mRdsStd;

    public static boolean fmSpurConfig(int fd) {
        SpurTable t = new SpurFileParser().GetSpurTable("/etc/fm/SpurTableFile.txt");
        t.GetSpurList();
        byte no_of_spur_freq = t.GetspurNoOfFreq();
        short[] buff = new short[((no_of_spur_freq * 16) + 2)];
        buff[0] = (short) t.GetMode();
        buff[1] = (short) no_of_spur_freq;
        for (int i = 0; i < no_of_spur_freq; i++) {
            List<Spur> spur = t.GetSpurList();
            int freq = spur.get(i).getSpurFreq();
            buff[(i * 16) + 2] = (short) (freq & 255);
            buff[(i * 16) + 3] = (short) ((freq >> 8) & 255);
            buff[(i * 16) + 4] = (short) ((freq >> 16) & 255);
            buff[(i * 16) + 5] = (short) spur.get(i).getNoOfSpursToTrack();
            List<SpurDetails> spurDetails = spur.get(i).getSpurDetailsList();
            int j = 0;
            for (int i2 = 3; j < i2; i2 = 3) {
                int rotation_value = spurDetails.get(j).getRotationValue();
                buff[(j * 4) + 6 + (i * 16)] = (short) (rotation_value & 255);
                buff[(j * 4) + 7 + (i * 16)] = (short) ((rotation_value >> 8) & 255);
                buff[(j * 4) + 8 + (i * 16)] = (short) ((rotation_value >> 16) & 15);
                int i3 = (j * 4) + 8 + (i * 16);
                buff[i3] = (short) (((short) (spurDetails.get(j).getLsbOfIntegrationLength() << 4)) | buff[i3]);
                int i4 = (j * 4) + 8 + (i * 16);
                buff[i4] = (short) (buff[i4] | ((short) (spurDetails.get(j).getFilterCoefficeint() << 5)));
                int i5 = (j * 4) + 8 + (i * 16);
                buff[i5] = (short) (buff[i5] | ((short) (spurDetails.get(j).getIsEnableSpur() << 7)));
                buff[(j * 4) + 9 + (i * 16)] = (short) spurDetails.get(j).getSpurLevel();
                j++;
            }
        }
        if (FmReceiverJNI.setSpurDataNative(fd, buff, (no_of_spur_freq * 16) + 2) < 0) {
            return false;
        }
        return true;
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseFmConfig
    public int getRadioBand() {
        return this.mRadioBand;
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseFmConfig
    public void setRadioBand(int band) {
        this.mRadioBand = band;
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseFmConfig
    public int getEmphasis() {
        return this.mEmphasis;
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseFmConfig
    public void setEmphasis(int emp) {
        this.mEmphasis = emp;
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseFmConfig
    public int getChSpacing() {
        return this.mChSpacing;
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseFmConfig
    public void setChSpacing(int spacing) {
        this.mChSpacing = spacing;
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseFmConfig
    public int getRdsStd() {
        return this.mRdsStd;
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseFmConfig
    public void setRdsStd(int rdsStandard) {
        this.mRdsStd = rdsStandard;
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseFmConfig
    public int getLowerLimit() {
        return this.mBandLowerLimit;
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseFmConfig
    public void setLowerLimit(int lowLimit) {
        this.mBandLowerLimit = lowLimit;
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseFmConfig
    public int getUpperLimit() {
        return this.mBandUpperLimit;
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseFmConfig
    public void setUpperLimit(int upLimit) {
        this.mBandUpperLimit = upLimit;
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseFmConfig
    public boolean fmConfigure(int fd) {
        int re;
        Log.v(TAG, "In fmConfigure");
        FmReceiverJNI.setControlNative(fd, V4L2_CID_PRIVATE_TAVARUA_EMPHASIS, getEmphasis());
        FmReceiverJNI.setControlNative(fd, V4L2_CID_PRIVATE_TAVARUA_RDS_STD, getRdsStd());
        FmReceiverJNI.setControlNative(fd, V4L2_CID_PRIVATE_TAVARUA_SPACING, getChSpacing());
        if (SystemPropertiesEx.getBoolean("persist.fm.new.srch.algorithm", false)) {
            Log.v(TAG, "fmConfigure() : FM Srch Alg : NEW ");
            re = FmReceiverJNI.setControlNative(fd, V4L2_CID_PRIVATE_TAVARUA_SRCH_ALGORITHM, 1);
        } else {
            Log.v(TAG, "fmConfigure() : FM Srch Alg : OLD ");
            re = FmReceiverJNI.setControlNative(fd, V4L2_CID_PRIVATE_TAVARUA_SRCH_ALGORITHM, 0);
        }
        if (re >= 0 && FmReceiverJNI.setBandNative(fd, getLowerLimit(), getUpperLimit()) >= 0 && FmReceiverJNI.setControlNative(fd, V4L2_CID_PRIVATE_TAVARUA_REGION, this.mRadioBand) >= 0) {
            return true;
        }
        return false;
    }
}
