package com.android.internal.telephony;

import com.android.internal.telephony.AbstractRIL.HwRILReference;

public interface HwTelephonyBaseManager {
    HwRILReference createHwRILReference(AbstractRIL abstractRIL);

    CommandException fromRilErrnoEx(int i);

    String gsm8BitUnpackedToString(byte[] bArr, int i, int i2, boolean z);

    String requestToStringEx(int i);

    String responseToStringEx(int i);
}
