package com.android.internal.telephony.uicc;

import android.os.Message;
import java.util.ArrayList;

public interface IHwIccFileHandlerEx {
    default void getSmscAddress(Message result) {
    }

    default void setSmscAddress(String address, Message result) {
    }

    default void loadEFTransparent(String filePath, int fileid, Message onLoaded) {
    }

    default void loadEFTransparent(String filePath, int fileid, Message onLoaded, boolean isForApp) {
    }

    default void loadEFLinearFixedPartHW(int fileid, ArrayList<Integer> arrayList, Message onLoaded) {
    }

    default void loadEFLinearFixedAllExcludeEmpty(int fileId, Message onLoaded) {
    }
}
