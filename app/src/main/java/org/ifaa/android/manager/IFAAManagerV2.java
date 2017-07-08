package org.ifaa.android.manager;

import android.content.Context;

public abstract class IFAAManagerV2 extends IFAAManager {
    public abstract byte[] processCmdV2(Context context, byte[] bArr);
}
