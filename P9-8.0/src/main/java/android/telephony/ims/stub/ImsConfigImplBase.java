package android.telephony.ims.stub;

import android.os.RemoteException;
import com.android.ims.ImsConfigListener;
import com.android.ims.internal.IImsConfig.Stub;

public class ImsConfigImplBase extends Stub {
    public int getProvisionedValue(int item) throws RemoteException {
        return -1;
    }

    public String getProvisionedStringValue(int item) throws RemoteException {
        return null;
    }

    public int setProvisionedValue(int item, int value) throws RemoteException {
        return 1;
    }

    public int setProvisionedStringValue(int item, String value) throws RemoteException {
        return 1;
    }

    public void getFeatureValue(int feature, int network, ImsConfigListener listener) throws RemoteException {
    }

    public void setFeatureValue(int feature, int network, int value, ImsConfigListener listener) throws RemoteException {
    }

    public boolean getVolteProvisioned() throws RemoteException {
        return false;
    }

    public void getVideoQuality(ImsConfigListener listener) throws RemoteException {
    }

    public void setVideoQuality(int quality, ImsConfigListener listener) throws RemoteException {
    }
}
