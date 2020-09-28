package android.os;

import android.media.AudioAttributes;
import android.os.IBinder;
import android.os.IExternalVibrationController;
import android.os.Parcelable;
import android.util.Slog;
import com.android.internal.util.Preconditions;

public class ExternalVibration implements Parcelable {
    public static final Parcelable.Creator<ExternalVibration> CREATOR = new Parcelable.Creator<ExternalVibration>() {
        /* class android.os.ExternalVibration.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public ExternalVibration createFromParcel(Parcel in) {
            return new ExternalVibration(in);
        }

        @Override // android.os.Parcelable.Creator
        public ExternalVibration[] newArray(int size) {
            return new ExternalVibration[size];
        }
    };
    private static final String TAG = "ExternalVibration";
    private AudioAttributes mAttrs;
    private IExternalVibrationController mController;
    private String mPkg;
    private IBinder mToken;
    private int mUid;

    public ExternalVibration(int uid, String pkg, AudioAttributes attrs, IExternalVibrationController controller) {
        this.mUid = uid;
        this.mPkg = (String) Preconditions.checkNotNull(pkg);
        this.mAttrs = (AudioAttributes) Preconditions.checkNotNull(attrs);
        this.mController = (IExternalVibrationController) Preconditions.checkNotNull(controller);
        this.mToken = new Binder();
    }

    private ExternalVibration(Parcel in) {
        this.mUid = in.readInt();
        this.mPkg = in.readString();
        this.mAttrs = readAudioAttributes(in);
        this.mController = IExternalVibrationController.Stub.asInterface(in.readStrongBinder());
        this.mToken = in.readStrongBinder();
    }

    private AudioAttributes readAudioAttributes(Parcel in) {
        int usage = in.readInt();
        int contentType = in.readInt();
        int capturePreset = in.readInt();
        return new AudioAttributes.Builder().setUsage(usage).setContentType(contentType).setCapturePreset(capturePreset).setFlags(in.readInt()).build();
    }

    public int getUid() {
        return this.mUid;
    }

    public String getPackage() {
        return this.mPkg;
    }

    public AudioAttributes getAudioAttributes() {
        return this.mAttrs;
    }

    public boolean mute() {
        try {
            this.mController.mute();
            return true;
        } catch (RemoteException e) {
            Slog.wtf(TAG, "Failed to mute vibration stream: " + this, e);
            return false;
        }
    }

    public boolean unmute() {
        try {
            this.mController.unmute();
            return true;
        } catch (RemoteException e) {
            Slog.wtf(TAG, "Failed to unmute vibration stream: " + this, e);
            return false;
        }
    }

    public void linkToDeath(IBinder.DeathRecipient recipient) {
        try {
            this.mToken.linkToDeath(recipient, 0);
        } catch (RemoteException e) {
        }
    }

    public void unlinkToDeath(IBinder.DeathRecipient recipient) {
        this.mToken.unlinkToDeath(recipient, 0);
    }

    public boolean equals(Object o) {
        if (o == null || !(o instanceof ExternalVibration)) {
            return false;
        }
        return this.mToken.equals(((ExternalVibration) o).mToken);
    }

    public String toString() {
        return "ExternalVibration{uid=" + this.mUid + ", pkg=" + this.mPkg + ", attrs=" + this.mAttrs + ", controller=" + this.mController + "token=" + this.mController + "}";
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(this.mUid);
        out.writeString(this.mPkg);
        writeAudioAttributes(this.mAttrs, out, flags);
        out.writeStrongBinder(this.mController.asBinder());
        out.writeStrongBinder(this.mToken);
    }

    private static void writeAudioAttributes(AudioAttributes attrs, Parcel out, int flags) {
        out.writeInt(attrs.getUsage());
        out.writeInt(attrs.getContentType());
        out.writeInt(attrs.getCapturePreset());
        out.writeInt(attrs.getAllFlags());
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }
}
