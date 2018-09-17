package android.rms.iaware;

import android.os.Binder;
import android.os.IBinder;

public class AwareAppStartBinder extends Binder {
    public int callingPid;
    public int callingUid;
    public IBinder tokenRaw;

    public AwareAppStartBinder(int callingPid, int callingUid, IBinder tokenRaw) {
        this.callingPid = callingPid;
        this.callingUid = callingUid;
        this.tokenRaw = tokenRaw;
    }
}
