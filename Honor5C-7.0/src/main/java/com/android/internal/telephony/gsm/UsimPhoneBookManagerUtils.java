package com.android.internal.telephony.gsm;

import com.android.internal.telephony.gsm.UsimPhoneBookManager.PbrRecord;
import com.android.internal.telephony.uicc.AdnRecord;
import com.android.internal.telephony.uicc.IccFileHandler;
import com.huawei.utils.reflect.EasyInvokeUtils;
import com.huawei.utils.reflect.FieldObject;
import com.huawei.utils.reflect.MethodObject;
import com.huawei.utils.reflect.annotation.GetField;
import com.huawei.utils.reflect.annotation.InvokeMethod;
import com.huawei.utils.reflect.annotation.SetField;
import java.util.ArrayList;

public class UsimPhoneBookManagerUtils extends EasyInvokeUtils {
    FieldObject<IccFileHandler> mFh;
    FieldObject<Boolean> mIsPbrPresent;
    FieldObject<Object> mLock;
    FieldObject<ArrayList<PbrRecord>> mPbrRecords;
    FieldObject<ArrayList<AdnRecord>> mPhoneBookRecords;
    MethodObject<Void> readPbrFileAndWait;

    @GetField(fieldObject = "mPbrRecords")
    public ArrayList<PbrRecord> getPbrRecords(UsimPhoneBookManager usimPhoneBookManager) {
        return (ArrayList) getField(this.mPbrRecords, usimPhoneBookManager);
    }

    @GetField(fieldObject = "mIsPbrPresent")
    public boolean getIsPbrPresent(UsimPhoneBookManager usimPhoneBookManager) {
        return ((Boolean) getField(this.mIsPbrPresent, usimPhoneBookManager)).booleanValue();
    }

    @GetField(fieldObject = "mFh")
    public IccFileHandler getFh(UsimPhoneBookManager usimPhoneBookManager) {
        return (IccFileHandler) getField(this.mFh, usimPhoneBookManager);
    }

    @GetField(fieldObject = "mLock")
    public Object getLockObject(UsimPhoneBookManager usimPhoneBookManager) {
        return getField(this.mLock, usimPhoneBookManager);
    }

    @GetField(fieldObject = "mPhoneBookRecords")
    public ArrayList<AdnRecord> getPhoneBookRecords(UsimPhoneBookManager usimPhoneBookManager) {
        return (ArrayList) getField(this.mPhoneBookRecords, usimPhoneBookManager);
    }

    @SetField(fieldObject = "PbrRecords")
    public void setPbrRecords(UsimPhoneBookManager usimPhoneBookManager, ArrayList<PbrRecord> value) {
        setField(this.mPbrRecords, usimPhoneBookManager, value);
    }

    @SetField(fieldObject = "mIsPbrPresent")
    public void setIsPbrPresent(UsimPhoneBookManager usimPhoneBookManager, boolean value) {
        setField(this.mIsPbrPresent, usimPhoneBookManager, Boolean.valueOf(value));
    }

    @SetField(fieldObject = "mFh")
    public void setFh(UsimPhoneBookManager usimPhoneBookManager, IccFileHandler value) {
        setField(this.mFh, usimPhoneBookManager, value);
    }

    @SetField(fieldObject = "mPhoneBookRecords")
    public void setPhoneBookRecords(UsimPhoneBookManager usimPhoneBookManager, ArrayList<AdnRecord> value) {
        setField(this.mPhoneBookRecords, usimPhoneBookManager, value);
    }

    @InvokeMethod(methodObject = "readPbrFileAndWait")
    public void readPbrFileAndWait(UsimPhoneBookManager usimPhoneBookManager) {
        invokeMethod(this.readPbrFileAndWait, usimPhoneBookManager, new Object[0]);
    }
}
