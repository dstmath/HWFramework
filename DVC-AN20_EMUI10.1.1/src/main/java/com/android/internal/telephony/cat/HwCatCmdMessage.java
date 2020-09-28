package com.android.internal.telephony.cat;

import android.os.Parcel;
import android.util.Log;
import com.android.internal.telephony.cat.AppInterface;

public class HwCatCmdMessage extends CatCmdMessage {
    public String mLanguageNotification;

    public HwCatCmdMessage(Parcel in) {
        super(in);
        Log.d("HwCatCmdMessage", "construct HwCatCmdMessage for Parcel");
        AppInterface.CommandType cmdType = getCmdType();
        if (cmdType != null && AnonymousClass1.$SwitchMap$com$android$internal$telephony$cat$AppInterface$CommandType[cmdType.ordinal()] == 1) {
            this.mLanguageNotification = in.readString();
        }
    }

    /* renamed from: com.android.internal.telephony.cat.HwCatCmdMessage$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$android$internal$telephony$cat$AppInterface$CommandType = new int[AppInterface.CommandType.values().length];

        static {
            try {
                $SwitchMap$com$android$internal$telephony$cat$AppInterface$CommandType[AppInterface.CommandType.LANGUAGE_NOTIFICATION.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
        }
    }

    public String getLanguageNotification() {
        return this.mLanguageNotification;
    }

    public void writeToParcel(Parcel dest, int flags) {
        HwCatCmdMessage.super.writeToParcel(dest, flags);
        AppInterface.CommandType cmdType = getCmdType();
        if (cmdType != null && AnonymousClass1.$SwitchMap$com$android$internal$telephony$cat$AppInterface$CommandType[cmdType.ordinal()] == 1) {
            dest.writeString(this.mLanguageNotification);
            dest.setDataPosition(0);
        }
    }
}
