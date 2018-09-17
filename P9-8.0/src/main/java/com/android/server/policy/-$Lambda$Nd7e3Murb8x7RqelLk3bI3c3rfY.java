package com.android.server.policy;

import android.os.Looper;
import android.view.InputChannel;
import android.view.InputEventReceiver;
import android.view.InputEventReceiver.Factory;

final /* synthetic */ class -$Lambda$Nd7e3Murb8x7RqelLk3bI3c3rfY implements Factory {
    private final /* synthetic */ Object -$f0;

    private final /* synthetic */ InputEventReceiver $m$0(InputChannel arg0, Looper arg1) {
        return ((PhoneWindowManager) this.-$f0).lambda$-com_android_server_policy_PhoneWindowManager_235201(arg0, arg1);
    }

    public /* synthetic */ -$Lambda$Nd7e3Murb8x7RqelLk3bI3c3rfY(Object obj) {
        this.-$f0 = obj;
    }

    public final InputEventReceiver createInputEventReceiver(InputChannel inputChannel, Looper looper) {
        return $m$0(inputChannel, looper);
    }
}
