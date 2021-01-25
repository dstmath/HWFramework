package com.huawei.server.security.hsm;

import android.app.AppOpsManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import com.huawei.android.util.SlogEx;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class HwAddViewManager {
    private static final int ACTIVITY_BG_DENIED = 4;
    private static final int ACTIVITY_LS_DENIED = 8;
    private static final int HANDLER_ADD = 0;
    private static final int HANDLER_DELETE = 1;
    private static final int HANDLER_UNKNOW = -1;
    private static final int OPERATION_ADD = 0;
    private static final int OPERATION_DELETE = 1;
    private static final int OPS_ACTIVITY_BG_DENIED = 5;
    private static final int OPS_ACTIVITY_LS_DENIED = 9;
    private static final int OPS_ALLOW = 0;
    private static final int OPS_DENIED_ORG = 1;
    private static final int OPS_TOAST_DENIED = 3;
    private static final String PACKAGENAME = "packagename";
    private static final int PER_USER_RANGE = 100000;
    private static final int RET_DIRTY_DATA = 2;
    private static final int RET_FAIL = 1;
    private static final int RET_SUCCESS = 0;
    private static final String TAG = "HwAddViewManager";
    private static final int TOAST_DENIED = 2;
    private static final String USERID = "userid";
    private static final String VALUE = "value";
    private static volatile HwAddViewManager sInstance;
    private static final Object serviceLock = new Object();
    private Map<Integer, Map<String, Integer>> addviewMapWithUser = new HashMap();
    private Context mContext;
    private Handler mHandler = null;
    private HandlerThread mHandlerThread = new HandlerThread("HwAddViewManagerHandleThread");

    private HwAddViewManager(Context context) {
        this.mContext = context;
        this.mHandlerThread.start();
        this.mHandler = new InnerHandler(this.mHandlerThread.getLooper());
    }

    public static HwAddViewManager getInstance(Context context) {
        HwAddViewManager hwAddViewManager;
        synchronized (serviceLock) {
            if (sInstance == null) {
                sInstance = new HwAddViewManager(context);
            }
            hwAddViewManager = sInstance;
        }
        return hwAddViewManager;
    }

    public int updateAddViewData(Bundle data, int operation) {
        Message msg = this.mHandler.obtainMessage();
        if (operation == 0) {
            msg.what = 0;
        } else if (operation != 1) {
            return 1;
        } else {
            msg.what = 1;
        }
        msg.setData(data);
        this.mHandler.sendMessage(msg);
        return 0;
    }

    public boolean addViewPermissionCheck(String packageName, int type, int uid) {
        if (packageName == null) {
            SlogEx.e(TAG, "param error");
            return false;
        } else if (uid == 0) {
            SlogEx.i(TAG, "param uid info error, true default");
            return true;
        } else {
            synchronized (serviceLock) {
                int userid = uid / PER_USER_RANGE;
                if (this.addviewMapWithUser.size() != 0) {
                    if (this.addviewMapWithUser.get(Integer.valueOf(userid)) != null) {
                        if (((AppOpsManager) this.mContext.getSystemService("appops")).checkOpNoThrow("android:system_alert_window", uid, packageName) == 0) {
                            SlogEx.d(TAG, "permission allow");
                            return true;
                        }
                        Map<String, Integer> currentUserPermissionMap = this.addviewMapWithUser.get(Integer.valueOf(userid));
                        if (currentUserPermissionMap == null || !currentUserPermissionMap.containsKey(packageName)) {
                            SlogEx.d(TAG, "not in blacklist, return default result-success");
                            return true;
                        }
                        SlogEx.d(TAG, "addViewPermissionCheck: " + packageName + " type: " + type);
                        return getPermissionResult(type, currentUserPermissionMap.get(packageName).intValue());
                    }
                }
                SlogEx.i(TAG, "list not ready, return allow, uid:" + uid);
                return true;
            }
        }
    }

    private boolean getPermissionResult(int compareValue, int sourceValue) {
        if (sourceValue == 0 || sourceValue == 1 || (compareValue & sourceValue) == 0) {
            SlogEx.d(TAG, "permission: " + compareValue + " allow");
            return true;
        }
        SlogEx.d(TAG, "permission: " + compareValue + " denied");
        return false;
    }

    private class InnerHandler extends Handler {
        public InnerHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            SlogEx.d(HwAddViewManager.TAG, "handleMessage msg=" + msg.what);
            super.handleMessage(msg);
            int i = msg.what;
            if (i == 0) {
                HwAddViewManager.this.addListInfo(msg.getData());
            } else if (i == 1) {
                HwAddViewManager.this.deleteListInfo(msg.getData());
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void addListInfo(Bundle data) {
        if (data == null) {
            SlogEx.e(TAG, "error of null bundle");
            return;
        }
        int userid = data.getInt(USERID, -1);
        ArrayList<String> packageNameList = null;
        ArrayList<Integer> valueList = null;
        try {
            packageNameList = data.getStringArrayList(PACKAGENAME);
            valueList = data.getIntegerArrayList(VALUE);
        } catch (ArrayIndexOutOfBoundsException e) {
            SlogEx.e(TAG, "addListInfo get List out of bounds.");
        }
        Map<String, Integer> blackListMap = arrayConvertMap(packageNameList, valueList);
        if (blackListMap != null && userid != -1) {
            synchronized (serviceLock) {
                Map<String, Integer> currentUserPermissionMap = this.addviewMapWithUser.get(Integer.valueOf(userid));
                if (currentUserPermissionMap != null) {
                    currentUserPermissionMap.putAll(blackListMap);
                } else {
                    currentUserPermissionMap = blackListMap;
                }
                this.addviewMapWithUser.put(Integer.valueOf(userid), currentUserPermissionMap);
                SlogEx.i(TAG, "update data ok, user=" + userid);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void deleteListInfo(Bundle data) {
        if (data != null) {
            int userid = data.getInt(USERID, -1);
            ArrayList<String> packageNameList = null;
            try {
                packageNameList = data.getStringArrayList(PACKAGENAME);
            } catch (ArrayIndexOutOfBoundsException e) {
                SlogEx.e(TAG, "deleteListInfo getStringArrayList out of bounds.");
            }
            if (!(packageNameList == null || userid == -1)) {
                int packagenameSize = packageNameList.size();
                synchronized (serviceLock) {
                    Map<String, Integer> currentUserPermissionMap = this.addviewMapWithUser.get(Integer.valueOf(userid));
                    if (currentUserPermissionMap != null) {
                        for (int i = 0; i < packagenameSize; i++) {
                            currentUserPermissionMap.remove(packageNameList.get(i));
                            SlogEx.d(TAG, "index:" + i + " remove package:" + packageNameList.get(i));
                        }
                        this.addviewMapWithUser.put(Integer.valueOf(userid), currentUserPermissionMap);
                    }
                    SlogEx.i(TAG, "remove done, size " + packagenameSize);
                }
            }
        }
    }

    private Map<String, Integer> arrayConvertMap(ArrayList<String> packageNameList, ArrayList<Integer> valueList) {
        if (packageNameList == null || valueList == null) {
            SlogEx.e(TAG, "null list or map");
            return null;
        }
        int packagenameSize = packageNameList.size();
        if (packagenameSize != valueList.size()) {
            SlogEx.e(TAG, "dirty list");
            return null;
        }
        Map<String, Integer> listMap = new HashMap<>();
        for (int i = 0; i < packagenameSize; i++) {
            listMap.put(packageNameList.get(i), valueList.get(i));
        }
        SlogEx.d(TAG, "comvert array over");
        return listMap;
    }
}
