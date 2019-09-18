package android.app.servertransaction;

import android.app.ClientTransactionHandler;
import android.app.IApplicationThread;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.RemoteException;
import com.android.internal.annotations.VisibleForTesting;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ClientTransaction implements Parcelable, ObjectPoolItem {
    public static final Parcelable.Creator<ClientTransaction> CREATOR = new Parcelable.Creator<ClientTransaction>() {
        public ClientTransaction createFromParcel(Parcel in) {
            return new ClientTransaction(in);
        }

        public ClientTransaction[] newArray(int size) {
            return new ClientTransaction[size];
        }
    };
    private List<ClientTransactionItem> mActivityCallbacks;
    private IBinder mActivityToken;
    private IApplicationThread mClient;
    private ActivityLifecycleItem mLifecycleStateRequest;

    public IApplicationThread getClient() {
        return this.mClient;
    }

    public void addCallback(ClientTransactionItem activityCallback) {
        if (this.mActivityCallbacks == null) {
            this.mActivityCallbacks = new ArrayList();
        }
        this.mActivityCallbacks.add(activityCallback);
    }

    /* access modifiers changed from: package-private */
    public List<ClientTransactionItem> getCallbacks() {
        return this.mActivityCallbacks;
    }

    public IBinder getActivityToken() {
        return this.mActivityToken;
    }

    @VisibleForTesting
    public ActivityLifecycleItem getLifecycleStateRequest() {
        return this.mLifecycleStateRequest;
    }

    public void setLifecycleStateRequest(ActivityLifecycleItem stateRequest) {
        this.mLifecycleStateRequest = stateRequest;
    }

    public void preExecute(ClientTransactionHandler clientTransactionHandler) {
        if (this.mActivityCallbacks != null) {
            int size = this.mActivityCallbacks.size();
            for (int i = 0; i < size; i++) {
                this.mActivityCallbacks.get(i).preExecute(clientTransactionHandler, this.mActivityToken);
            }
        }
        if (this.mLifecycleStateRequest != null) {
            this.mLifecycleStateRequest.preExecute(clientTransactionHandler, this.mActivityToken);
        }
    }

    public void schedule() throws RemoteException {
        this.mClient.scheduleTransaction(this);
    }

    private ClientTransaction() {
    }

    public static ClientTransaction obtain(IApplicationThread client, IBinder activityToken) {
        ClientTransaction instance = (ClientTransaction) ObjectPool.obtain(ClientTransaction.class);
        if (instance == null) {
            instance = new ClientTransaction();
        }
        instance.mClient = client;
        instance.mActivityToken = activityToken;
        return instance;
    }

    public void recycle() {
        if (this.mActivityCallbacks != null) {
            int size = this.mActivityCallbacks.size();
            for (int i = 0; i < size; i++) {
                this.mActivityCallbacks.get(i).recycle();
            }
            this.mActivityCallbacks.clear();
        }
        if (this.mLifecycleStateRequest != null) {
            this.mLifecycleStateRequest.recycle();
            this.mLifecycleStateRequest = null;
        }
        this.mClient = null;
        this.mActivityToken = null;
        ObjectPool.recycle(this);
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStrongBinder(this.mClient.asBinder());
        boolean writeActivityCallbacks = false;
        boolean writeActivityToken = this.mActivityToken != null;
        dest.writeBoolean(writeActivityToken);
        if (writeActivityToken) {
            dest.writeStrongBinder(this.mActivityToken);
        }
        dest.writeParcelable(this.mLifecycleStateRequest, flags);
        if (this.mActivityCallbacks != null) {
            writeActivityCallbacks = true;
        }
        dest.writeBoolean(writeActivityCallbacks);
        if (writeActivityCallbacks) {
            dest.writeParcelableList(this.mActivityCallbacks, flags);
        }
    }

    private ClientTransaction(Parcel in) {
        this.mClient = (IApplicationThread) in.readStrongBinder();
        if (in.readBoolean()) {
            this.mActivityToken = in.readStrongBinder();
        }
        this.mLifecycleStateRequest = (ActivityLifecycleItem) in.readParcelable(getClass().getClassLoader());
        if (in.readBoolean()) {
            this.mActivityCallbacks = new ArrayList();
            in.readParcelableList(this.mActivityCallbacks, getClass().getClassLoader());
        }
    }

    public int describeContents() {
        return 0;
    }

    public boolean equals(Object o) {
        boolean z = true;
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ClientTransaction other = (ClientTransaction) o;
        if (!Objects.equals(this.mActivityCallbacks, other.mActivityCallbacks) || !Objects.equals(this.mLifecycleStateRequest, other.mLifecycleStateRequest) || this.mClient != other.mClient || this.mActivityToken != other.mActivityToken) {
            z = false;
        }
        return z;
    }

    public int hashCode() {
        return (31 * ((31 * 17) + Objects.hashCode(this.mActivityCallbacks))) + Objects.hashCode(this.mLifecycleStateRequest);
    }
}
