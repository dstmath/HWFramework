package ohos.data.searchimpl.connect;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public class SearchServiceConnection {
    private static final String CLASS_NAME = "com.huawei.searchservice.service.SearchService";
    private static final String COMMAND_TYPE_HOSP = "HOSP";
    private static final HiLogLabel LABEL = new HiLogLabel(3, 218109504, "SearchServiceConnection");
    private static final String PKG_NAME = "com.huawei.searchservice";
    private OnConnectListener connectListener;
    private ServiceConnection connection = new ServiceConnection() {
        /* class ohos.data.searchimpl.connect.SearchServiceConnection.AnonymousClass1 */

        @Override // android.content.ServiceConnection
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            SearchServiceConnection.this.service = iBinder;
            if (SearchServiceConnection.this.connectListener != null) {
                SearchServiceConnection.this.connectListener.onConnect(iBinder);
            }
        }

        @Override // android.content.ServiceConnection
        public void onServiceDisconnected(ComponentName componentName) {
            if (SearchServiceConnection.this.service != null) {
                SearchServiceConnection.this.service = null;
                if (SearchServiceConnection.this.connectListener != null) {
                    SearchServiceConnection.this.connectListener.onDisconnect();
                }
            }
        }
    };
    private final Context context;
    private IBinder service;

    public interface OnConnectListener {
        void onConnect(IBinder iBinder);

        void onDisconnect();
    }

    public SearchServiceConnection(Context context2) {
        this.context = context2;
        this.service = null;
    }

    public boolean open(OnConnectListener onConnectListener) {
        this.connectListener = onConnectListener;
        try {
            if (this.context.bindService(createExplicitIntent(PKG_NAME, CLASS_NAME), this.connection, 1)) {
                return true;
            }
            HiLog.error(LABEL, "failed to connect to search service", new Object[0]);
            return false;
        } catch (SecurityException e) {
            HiLog.error(LABEL, "bind search service exception: %{public}s", new Object[]{e.getMessage()});
            return false;
        }
    }

    public boolean close() {
        this.context.unbindService(this.connection);
        this.service = null;
        this.connectListener = null;
        return true;
    }

    private Intent createExplicitIntent(String str, String str2) {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName(str, str2));
        intent.setType(COMMAND_TYPE_HOSP);
        return intent;
    }
}
