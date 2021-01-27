package android.view;

import android.os.IBinder;
import com.huawei.annotation.HwSystemApi;

@HwSystemApi
public class IApplicationTokenEx {
    private IApplicationToken mApplicationToken;

    public void setApplicationToken(IApplicationToken applicationToken) {
        this.mApplicationToken = applicationToken;
    }

    public IApplicationToken getApplicationToken() {
        return this.mApplicationToken;
    }

    public IBinder asBinder() {
        IApplicationToken iApplicationToken = this.mApplicationToken;
        if (iApplicationToken == null) {
            return null;
        }
        return iApplicationToken.asBinder();
    }

    public boolean isEmpty() {
        return this.mApplicationToken == null;
    }
}
