package android.accounts;

import android.accounts.IAccountManagerResponse;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.RemoteException;

public class AccountManagerResponse implements Parcelable {
    public static final Parcelable.Creator<AccountManagerResponse> CREATOR = new Parcelable.Creator<AccountManagerResponse>() {
        /* class android.accounts.AccountManagerResponse.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public AccountManagerResponse createFromParcel(Parcel source) {
            return new AccountManagerResponse(source);
        }

        @Override // android.os.Parcelable.Creator
        public AccountManagerResponse[] newArray(int size) {
            return new AccountManagerResponse[size];
        }
    };
    private IAccountManagerResponse mResponse;

    public AccountManagerResponse(IAccountManagerResponse response) {
        this.mResponse = response;
    }

    public AccountManagerResponse(Parcel parcel) {
        this.mResponse = IAccountManagerResponse.Stub.asInterface(parcel.readStrongBinder());
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

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStrongBinder(this.mResponse.asBinder());
    }
}
