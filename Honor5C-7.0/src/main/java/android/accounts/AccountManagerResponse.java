package android.accounts;

import android.accounts.IAccountManagerResponse.Stub;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.os.RemoteException;

public class AccountManagerResponse implements Parcelable {
    public static final Creator<AccountManagerResponse> CREATOR = null;
    private IAccountManagerResponse mResponse;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.accounts.AccountManagerResponse.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.accounts.AccountManagerResponse.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.accounts.AccountManagerResponse.<clinit>():void");
    }

    public AccountManagerResponse(IAccountManagerResponse response) {
        this.mResponse = response;
    }

    public AccountManagerResponse(Parcel parcel) {
        this.mResponse = Stub.asInterface(parcel.readStrongBinder());
    }

    public void onResult(Bundle result) {
        try {
            this.mResponse.onResult(result);
        } catch (RemoteException e) {
        }
    }

    public void onError(int errorCode, String errorMessage) {
        try {
            this.mResponse.onError(errorCode, errorMessage);
        } catch (RemoteException e) {
        }
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStrongBinder(this.mResponse.asBinder());
    }
}
