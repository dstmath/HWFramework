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
    private IClientTransactionItem mFLConfigurationChangeItem;

    public ClientTransactionItemImpl(IClientTransactionItem flConfigurationChangeItem) {
        this.mFLConfigurationChangeItem = flConfigurationChangeItem;
    }

    public void execute(ClientTransactionHandler client, IBinder token, PendingTransactionActions pendingActions) {
        IClientTransactionItem iClientTransactionItem = this.mFLConfigurationChangeItem;
        if (iClientTransactionItem != null) {
            iClientTransactionItem.execute(client, token, pendingActions);
        }
    }

    public void recycle() {
    }

    public void writeToParcel(Parcel dest, int flags) {
    }

    private ClientTransactionItemImpl(Parcel in) {
        this.mFLConfigurationChangeItem = null;
    }

    public boolean equals(Object o) {
        if (!(o instanceof ClientTransactionItemImpl) || this.mFLConfigurationChangeItem != ((ClientTransactionItemImpl) o).mFLConfigurationChangeItem) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        IClientTransactionItem iClientTransactionItem = this.mFLConfigurationChangeItem;
        if (iClientTransactionItem != null) {
            return iClientTransactionItem.hashCode();
        }
        return 0;
    }

    public String toString() {
        return "ClientTransactionItemImpl{}";
    }
}
