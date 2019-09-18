package android.net;

import android.net.ICaptivePortal;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.RemoteException;

public class CaptivePortal implements Parcelable {
    public static final int APP_RETURN_DISMISSED = 0;
    public static final int APP_RETURN_UNWANTED = 1;
    public static final int APP_RETURN_WANTED_AS_IS = 2;
    public static final Parcelable.Creator<CaptivePortal> CREATOR = new Parcelable.Creator<CaptivePortal>() {
        public CaptivePortal createFromParcel(Parcel in) {
            return new CaptivePortal(in.readStrongBinder());
        }

        public CaptivePortal[] newArray(int size) {
            return new CaptivePortal[size];
        }
    };
    private final IBinder mBinder;

    public CaptivePortal(IBinder binder) {
        this.mBinder = binder;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeStrongBinder(this.mBinder);
    }

    public void reportCaptivePortalDismissed() {
        try {
            ICaptivePortal.Stub.asInterface(this.mBinder).appResponse(0);
        } catch (RemoteException e) {
        }
    }

    public void ignoreNetwork() {
        try {
            ICaptivePortal.Stub.asInterface(this.mBinder).appResponse(1);
        } catch (RemoteException e) {
        }
    }

    public void useNetwork() {
        try {
            ICaptivePortal.Stub.asInterface(this.mBinder).appResponse(2);
        } catch (RemoteException e) {
        }
    }
}
