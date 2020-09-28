package android.rms.iaware;

import android.os.Binder;
import android.os.IBinder;

public class AwareAppStartBinder extends Binder {
    public int callingPid;
    public int callingUid;
    public IBinder tokenRaw;

    public AwareAppStartBinder(int callingPid2, int callingUid2, IBinder tokenRaw2) {
        this.callingPid = callingPid2;
        this.callingUid = callingUid2;
        this.tokenRaw = tokenRaw2;
    }
}
