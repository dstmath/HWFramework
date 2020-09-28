package android.rms.iaware.scenerecog;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.rms.iaware.IAwareSdk;
import android.text.TextUtils;
import android.util.Log;
import java.util.ArrayList;

public class AwareAppDataService extends Binder implements IInterface {
    private static final int BINDER_TRANSACTION_END = 1;
    private static final String SDK_CALLBACK_DESCRIPTOR = "android.rms.iaware.AppSceneRecog.AwareAppSceneService";
    private static final String TAG = "AwareAppDataService";
    private static final int TRANSACTION_APPSCENEDATA = 1;
    private IAwareDataCallBack mDataCallBack;

    public interface IAwareDataCallBack {
        void onInitDataFailure();

        void onInitDataSuccess(ArrayList<String> arrayList);
    }

    public AwareAppDataService(IAwareDataCallBack callBack) {
        attachInterface(this, SDK_CALLBACK_DESCRIPTOR);
        this.mDataCallBack = callBack;
    }

    public void initAppSceneData(String message) {
        if (!TextUtils.isEmpty(message)) {
            IAwareSdk.asyncReportDataWithCallback(3035, message, this, 0);
        }
    }

    public IBinder asBinder() {
        return this;
    }

    /* access modifiers changed from: protected */
    @Override // android.os.Binder
    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        if (code != 1) {
            return super.onTransact(code, data, reply, flags);
        }
        handleSceneData(data);
        return true;
    }

    private void handleSceneData(Parcel data) {
        if (this.mDataCallBack != null && data != null) {
            data.enforceInterface(SDK_CALLBACK_DESCRIPTOR);
            ArrayList<String> list = new ArrayList<>();
            data.readStringList(list);
            if (data.readInt() != 1 || list.isEmpty()) {
                Log.i(TAG, "transact scene data failure");
                this.mDataCallBack.onInitDataFailure();
                return;
            }
            this.mDataCallBack.onInitDataSuccess(list);
        }
    }
}
