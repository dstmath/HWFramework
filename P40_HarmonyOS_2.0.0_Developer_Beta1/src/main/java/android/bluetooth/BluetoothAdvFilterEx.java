package android.bluetooth;

import android.util.Log;
import java.util.UUID;

public class BluetoothAdvFilterEx {
    private static final String TAG = "BluetoothAdvFilterEx";
    private static BluetoothAdvFilterEx instance = new BluetoothAdvFilterEx();

    private BluetoothAdvFilterEx() {
    }

    public static BluetoothAdvFilterEx getInstanse() {
        return instance;
    }

    public static boolean isSupportSensorAdvertisFilter() {
        boolean isSupp = BluetoothDeviceAdapterEx.isSupportSensorAdvertisFilter();
        Log.i(TAG, "isSupportSensorAdvertisFilter support:" + isSupp);
        return isSupp;
    }

    public int getRemainFilterNum() {
        int filterNum = BluetoothDeviceAdapterEx.getAdvertisFilterRemained();
        Log.i(TAG, "getRemainFilterNum remained:" + filterNum);
        return filterNum;
    }

    public boolean setAdvFilterParam(BluetoothAdvFilterParamEx advFilterParam, IBluetoothAdvCallback callback) {
        UUID uuid = advFilterParam.getUuid();
        Log.i(TAG, "setAdvFilterParam: uuid:" + uuid);
        if (uuid.equals(new UUID(0, 0))) {
            Log.e(TAG, "setAdvFilterParam uuid is null!");
            return false;
        }
        Log.i(TAG, "setAdvFilterParam: callback:" + callback);
        boolean retVal = BluetoothAdvFilterCallbackEx.getInstance().registAdvFilterCallback(uuid, callback);
        if (!retVal) {
            Log.e(TAG, "setAdvFilterParam: regist callback fail!");
            return retVal;
        }
        boolean retVal2 = BluetoothDeviceAdapterEx.setAdvertisFilterParamExt(advFilterParam);
        Log.i(TAG, "setAdvFilterParam:" + retVal2);
        return retVal2;
    }

    public void removeAdvFilter(UUID uuid) {
        Log.i(TAG, "removeAdvFilterParams: remove fliter uuid: " + uuid);
        BluetoothDeviceAdapterEx.removeAdvertisFilter(uuid);
    }
}
