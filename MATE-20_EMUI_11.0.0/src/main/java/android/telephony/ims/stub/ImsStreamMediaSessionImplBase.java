package android.telephony.ims.stub;

import android.os.RemoteException;
import com.android.ims.internal.IImsStreamMediaSession;

public class ImsStreamMediaSessionImplBase extends IImsStreamMediaSession.Stub {
    @Override // com.android.ims.internal.IImsStreamMediaSession
    public void close() throws RemoteException {
    }
}
