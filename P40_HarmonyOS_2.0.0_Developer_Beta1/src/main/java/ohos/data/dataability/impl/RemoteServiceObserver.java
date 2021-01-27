package ohos.data.dataability.impl;

import android.database.ContentObserver;
import android.os.Handler;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public class RemoteServiceObserver extends ContentObserver {
    private static final HiLogLabel LABEL = new HiLogLabel(3, 218109536, "RemoteServiceObserver");
    private IRemoteDataAbilityObserver remoteDataAbilityObserver;

    public RemoteServiceObserver(Handler handler, IRemoteDataAbilityObserver iRemoteDataAbilityObserver) {
        super(handler);
        this.remoteDataAbilityObserver = iRemoteDataAbilityObserver;
    }

    @Override // android.database.ContentObserver
    public void onChange(boolean z) {
        IRemoteDataAbilityObserver iRemoteDataAbilityObserver = this.remoteDataAbilityObserver;
        if (iRemoteDataAbilityObserver != null) {
            iRemoteDataAbilityObserver.onChange();
        } else {
            HiLog.error(LABEL, "remoteDataAbilityObserver is null", new Object[0]);
        }
    }
}
