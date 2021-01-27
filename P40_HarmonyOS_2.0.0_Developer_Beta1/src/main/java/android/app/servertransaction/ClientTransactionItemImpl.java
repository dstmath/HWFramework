package android.app.servertransaction;

import android.app.ClientTransactionHandler;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;

public class ClientTransactionItemImpl extends ClientTransactionItem {
    public static final Parcelable.Creator<ClientTransactionItemImpl> CREATOR = new Parcelable.Creator<ClientTransactionItemImpl>() {
        /* class android.app.servertransaction.ClientTransactionItemImpl.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public ClientTransactionItemImpl createFromParcel(Parcel in) {
            return new ClientTransactionItemImpl(in);
        }

        @Override // android.os.Parcelable.Creator
        public ClientTransactionItemImpl[] newArray(int size) {
            return new ClientTransactionItemImpl[size];
        }
    };
    private IClientTransactionItem mFlConfigurationChangeItem;

    public ClientTransactionItemImpl(IClientTransactionItem flConfigurationChangeItem) {
        this.mFlConfigurationChangeItem = flConfigurationChangeItem;
    }

    private ClientTransactionItemImpl(Parcel in) {
        this.mFlConfigurationChangeItem = null;
    }

    public void execute(ClientTransactionHandler client, IBinder token, PendingTransactionActions pendingActions) {
        IClientTransactionItem iClientTransactionItem = this.mFlConfigurationChangeItem;
        if (iClientTransactionItem != null) {
            iClientTransactionItem.execute(client, token, pendingActions);
        }
    }

    public void recycle() {
    }

    public void writeToParcel(Parcel dest, int flags) {
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof ClientTransactionItemImpl) || this.mFlConfigurationChangeItem != ((ClientTransactionItemImpl) obj).mFlConfigurationChangeItem) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        IClientTransactionItem iClientTransactionItem = this.mFlConfigurationChangeItem;
        if (iClientTransactionItem != null) {
            return iClientTransactionItem.hashCode();
        }
        return 0;
    }

    public String toString() {
        return "ClientTransactionItemImpl{}";
    }
}
