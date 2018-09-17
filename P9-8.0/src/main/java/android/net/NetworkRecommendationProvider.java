package android.net;

import android.Manifest.permission;
import android.content.Context;
import android.net.INetworkRecommendationProvider.Stub;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import com.android.internal.util.Preconditions;
import java.util.concurrent.Executor;

public abstract class NetworkRecommendationProvider {
    private static final String TAG = "NetworkRecProvider";
    private static final boolean VERBOSE = (Build.IS_DEBUGGABLE ? Log.isLoggable(TAG, 2) : false);
    private final IBinder mService;

    private final class ServiceWrapper extends Stub {
        private final Context mContext;
        private final Executor mExecutor;
        private final Handler mHandler = null;

        ServiceWrapper(Context context, Executor executor) {
            this.mContext = context;
            this.mExecutor = executor;
        }

        public void requestScores(final NetworkKey[] networks) throws RemoteException {
            enforceCallingPermission();
            if (networks != null && networks.length > 0) {
                execute(new Runnable() {
                    public void run() {
                        NetworkRecommendationProvider.this.onRequestScores(networks);
                    }
                });
            }
        }

        private void execute(Runnable command) {
            if (this.mExecutor != null) {
                this.mExecutor.execute(command);
            } else {
                this.mHandler.post(command);
            }
        }

        private void enforceCallingPermission() {
            if (this.mContext != null) {
                this.mContext.enforceCallingOrSelfPermission(permission.REQUEST_NETWORK_SCORES, "Permission denied.");
            }
        }
    }

    public abstract void onRequestScores(NetworkKey[] networkKeyArr);

    public NetworkRecommendationProvider(Context context, Executor executor) {
        Preconditions.checkNotNull(context);
        Preconditions.checkNotNull(executor);
        this.mService = new ServiceWrapper(context, executor);
    }

    public final IBinder getBinder() {
        return this.mService;
    }
}
