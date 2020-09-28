package android.mtp;

import com.huawei.annotation.HwSystemApi;

@HwSystemApi
public class MtpPropertyListEx extends MtpPropertyList {
    @Override // android.mtp.MtpPropertyList
    public /* bridge */ /* synthetic */ int getCode() {
        return super.getCode();
    }

    @Override // android.mtp.MtpPropertyList
    public /* bridge */ /* synthetic */ int getCount() {
        return super.getCount();
    }

    @Override // android.mtp.MtpPropertyList
    public /* bridge */ /* synthetic */ int[] getDataTypes() {
        return super.getDataTypes();
    }

    @Override // android.mtp.MtpPropertyList
    public /* bridge */ /* synthetic */ long[] getLongValues() {
        return super.getLongValues();
    }

    @Override // android.mtp.MtpPropertyList
    public /* bridge */ /* synthetic */ int[] getObjectHandles() {
        return super.getObjectHandles();
    }

    @Override // android.mtp.MtpPropertyList
    public /* bridge */ /* synthetic */ int[] getPropertyCodes() {
        return super.getPropertyCodes();
    }

    @Override // android.mtp.MtpPropertyList
    public /* bridge */ /* synthetic */ String[] getStringValues() {
        return super.getStringValues();
    }

    public MtpPropertyListEx(int code) {
        super(code);
    }

    @Override // android.mtp.MtpPropertyList
    public void setResult(int result) {
        super.setResult(result);
    }

    @Override // android.mtp.MtpPropertyList
    public void append(int handle, int property, String value) {
        super.append(handle, property, value);
    }

    @Override // android.mtp.MtpPropertyList
    public void append(int handle, int property, int type, long value) {
        super.append(handle, property, type, value);
    }
}
