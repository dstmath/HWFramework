package android.app.servertransaction;

import android.os.Parcelable;

public abstract class ClientTransactionItem implements BaseClientRequest, Parcelable {
    public int getPostExecutionState() {
        return -1;
    }

    public int describeContents() {
        return 0;
    }
}
