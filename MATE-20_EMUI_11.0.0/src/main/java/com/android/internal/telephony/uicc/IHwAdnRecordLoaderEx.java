package com.android.internal.telephony.uicc;

import android.os.Message;

public interface IHwAdnRecordLoaderEx {
    default void loadAllAdnFromEFHw(int fileId, int extensionEF, Message response) {
    }
}
