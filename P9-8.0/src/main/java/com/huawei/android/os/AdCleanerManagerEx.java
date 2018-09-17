package com.huawei.android.os;

import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AdCleanerManagerEx {
    private static final int CODE_AD_DEBUG = 1019;
    private static final int CODE_CLEAN_AD_STRATEGY = 1018;
    private static final int CODE_SET_AD_STRATEGY = 1017;
    private static final String DESCRIPTOR_ADCLEANER_MANAGER_Ex = "android.os.AdCleanerManagerEx";
    private static final String TAG = "AdCleanerManagerEx";

    public static int printRuleMaps() {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        IBinder b = ServiceManager.getService("network_management");
        if (b != null) {
            try {
                _data.writeInterfaceToken(DESCRIPTOR_ADCLEANER_MANAGER_Ex);
                _data.writeInt(0);
                b.transact(1019, _data, _reply, 0);
                _reply.readException();
            } catch (RemoteException localRemoteException) {
                localRemoteException.printStackTrace();
                _reply.recycle();
                _data.recycle();
                return 1;
            } catch (Throwable th) {
                _reply.recycle();
                _data.recycle();
                throw th;
            }
        }
        _reply.recycle();
        _data.recycle();
        return 0;
    }

    public static int cleanAdFilterRules(List<String> adAppList, boolean needRest) {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        IBinder b = ServiceManager.getService("network_management");
        if (b != null) {
            try {
                _data.writeInterfaceToken(DESCRIPTOR_ADCLEANER_MANAGER_Ex);
                Log.d(TAG, "CleanAdFilterRules needRest: " + needRest);
                if (needRest) {
                    _data.writeInt(1);
                } else if (adAppList == null) {
                    _data.writeInt(-1);
                } else {
                    _data.writeInt(0);
                    _data.writeStringList(adAppList);
                    Log.d(TAG, "CleanAdFilterRules adAppList: ");
                    for (int i = 0; i < adAppList.size(); i++) {
                        Log.d(TAG, i + " = " + ((String) adAppList.get(i)));
                    }
                }
                b.transact(1018, _data, _reply, 0);
                _reply.readException();
            } catch (RemoteException localRemoteException) {
                Log.d(TAG, "------- err: CleanAdFilterRules() RemoteException ! ");
                localRemoteException.printStackTrace();
                _reply.recycle();
                _data.recycle();
                return 1;
            } catch (Throwable th) {
                _reply.recycle();
                _data.recycle();
                throw th;
            }
        }
        _reply.recycle();
        _data.recycle();
        return 0;
    }

    public static int setAdFilterRules(Map<String, List<String>> adViewMap, Map<String, List<String>> adIdMap, boolean needRest) {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        IBinder b = ServiceManager.getService("network_management");
        if (b != null) {
            try {
                int size;
                Set<String> keysSet;
                List<String> keysList;
                int i;
                String key;
                List<String> value;
                _data.writeInterfaceToken(DESCRIPTOR_ADCLEANER_MANAGER_Ex);
                _data.writeInt(needRest ? 1 : 0);
                Log.d(TAG, "setAdFilterRules needRest: " + needRest);
                if (adViewMap == null) {
                    _data.writeInt(-1);
                } else {
                    size = adViewMap.size();
                    _data.writeInt(size);
                    Log.d(TAG, "adViewMap size = " + size);
                    keysSet = adViewMap.keySet();
                    keysList = new ArrayList();
                    for (String keyString : keysSet) {
                        keysList.add(keyString);
                    }
                    for (i = 0; i < size; i++) {
                        key = (String) keysList.get(i);
                        value = (List) adViewMap.get(key);
                        _data.writeString(key);
                        _data.writeStringList(value);
                        Log.d(TAG, "i=" + i + ", send adViewMap key: " + key);
                    }
                }
                if (adIdMap == null) {
                    _data.writeInt(-1);
                } else {
                    size = adIdMap.size();
                    _data.writeInt(size);
                    Log.d(TAG, " adIdMap size = " + size);
                    keysSet = adIdMap.keySet();
                    keysList = new ArrayList();
                    for (String keyString2 : keysSet) {
                        keysList.add(keyString2);
                    }
                    for (i = 0; i < size; i++) {
                        key = (String) keysList.get(i);
                        value = (List) adIdMap.get(key);
                        _data.writeString(key);
                        _data.writeStringList(value);
                        Log.d(TAG, "i=" + i + ", send adIdMap key: " + key);
                    }
                }
                b.transact(1017, _data, _reply, 0);
                _reply.readException();
            } catch (RemoteException localRemoteException) {
                Log.d(TAG, "------- err: setAdFilterRules() RemoteException ! ");
                localRemoteException.printStackTrace();
                _reply.recycle();
                _data.recycle();
                return 1;
            } catch (Throwable th) {
                _reply.recycle();
                _data.recycle();
                throw th;
            }
        }
        _reply.recycle();
        _data.recycle();
        return 0;
    }
}
