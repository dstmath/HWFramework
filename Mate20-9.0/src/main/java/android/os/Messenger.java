package android.os;

import android.os.IMessenger;
import android.os.Parcelable;

public final class Messenger implements Parcelable {
    public static final Parcelable.Creator<Messenger> CREATOR = new Parcelable.Creator<Messenger>() {
        public Messenger createFromParcel(Parcel in) {
            IBinder target = in.readStrongBinder();
            if (target != null) {
                return new Messenger(target);
            }
            return null;
        }

        public Messenger[] newArray(int size) {
            return new Messenger[size];
        }
    };
    private final IMessenger mTarget;

    public Messenger(Handler target) {
        this.mTarget = target.getIMessenger();
    }

    public void send(Message message) throws RemoteException {
        this.mTarget.send(message);
    }

    public IBinder getBinder() {
        return this.mTarget.asBinder();
    }

    public boolean equals(Object otherObj) {
        if (otherObj == null) {
            return false;
        }
        try {
            return this.mTarget.asBinder().equals(((Messenger) otherObj).mTarget.asBinder());
        } catch (ClassCastException e) {
            return false;
        }
    }

    public int hashCode() {
        return this.mTarget.asBinder().hashCode();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeStrongBinder(this.mTarget.asBinder());
    }

    public static void writeMessengerOrNullToParcel(Messenger messenger, Parcel out) {
        IBinder iBinder;
        if (messenger != null) {
            iBinder = messenger.mTarget.asBinder();
        } else {
            iBinder = null;
        }
        out.writeStrongBinder(iBinder);
    }

    public static Messenger readMessengerOrNullFromParcel(Parcel in) {
        IBinder b = in.readStrongBinder();
        if (b != null) {
            return new Messenger(b);
        }
        return null;
    }

    public Messenger(IBinder target) {
        this.mTarget = IMessenger.Stub.asInterface(target);
    }
}
