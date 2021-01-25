package com.huawei.displayengine;

import com.huawei.android.os.storage.StorageManagerExt;

/* compiled from: BrigntnessTrainingAlgoImpl */
class AlgoParam {
    public String mBLCurveTypeDefaultLuma;
    public String mBLCurveTypeHighLuma = StorageManagerExt.INVALID_KEY_DESC;
    public String mBLCurveTypeLowLuma = StorageManagerExt.INVALID_KEY_DESC;
    public String mBLCurveTypeMedialLuma = StorageManagerExt.INVALID_KEY_DESC;
    public int mDefaultLumaCount = 0;
    public int mDefaultLumaSize;
    public int mDragCount = 0;
    public String mDragInfo = StorageManagerExt.INVALID_KEY_DESC;
    public int mDragSize = 0;
    public String mESCW = StorageManagerExt.INVALID_KEY_DESC;
    public int mESCWCount = 0;
    public int mESCWSize;
    public int mFirstInital = 0;
    public int mHighLumaCount = 0;
    public int mHighLumaSize;
    public int mLowLumaCount = 0;
    public int mLowLumaSize;
    public int mMedialLumaCount = 0;
    public int mMedialLumaSize;
}
