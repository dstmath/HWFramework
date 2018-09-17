package com.android.ims;

import android.os.Message;

public abstract class AbstractImsUt implements AbstractImsUtInterface {
    protected ImsUtReference mReference = null;

    public interface ImsUtReference {
        boolean isSupportCFT();

        boolean isUtEnable();

        Message popUtMessage(int i);

        void updateCallBarringOption(String str, int i, boolean z, Message message, String[] strArr);

        void updateCallForwardUncondTimer(int i, int i2, int i3, int i4, int i5, int i6, String str, Message message);
    }

    public boolean isSupportCFT() {
        return this.mReference.isSupportCFT();
    }

    public boolean isUtEnable() {
        return this.mReference.isUtEnable();
    }

    public void updateCallForwardUncondTimer(int starthour, int startminute, int endhour, int endminute, int action, int condition, String number, Message result) {
        this.mReference.updateCallForwardUncondTimer(starthour, startminute, endhour, endminute, action, condition, number, result);
    }

    public void updateCallBarringOption(String password, int cbType, boolean enable, Message result, String[] barrList) {
        this.mReference.updateCallBarringOption(password, cbType, enable, result, barrList);
    }

    public Message popUtMessage(int id) {
        return this.mReference.popUtMessage(id);
    }
}
