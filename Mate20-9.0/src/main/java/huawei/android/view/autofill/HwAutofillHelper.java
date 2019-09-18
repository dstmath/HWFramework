package huawei.android.view.autofill;

import android.content.ComponentName;
import android.content.Context;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.provider.Settings;
import android.util.ArrayMap;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.autofill.AutofillId;
import android.view.autofill.AutofillValue;
import android.view.autofill.IAutoFillManagerClient;
import android.view.autofill.IHwAutofillHelper;
import android.widget.TextView;
import huawei.android.security.IHwKeychainManager;
import huawei.android.security.IHwSecurityService;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class HwAutofillHelper implements IHwAutofillHelper {
    private static final String AUTOFILL_PACKAGE_VALUE_HW = "com.huawei.securitymgr";
    private static final String AUTOFILL_SERVICE_KEY = "autofill_service";
    private static final String AUTOFILL_SERVICE_VALUE_HW = "com.huawei.securitymgr/com.huawei.keychain.service.HwAutofillService";
    private static final int FILL_TYPE_DEFAULT = 0;
    private static final String FILL_TYPE_KEY = "fillType";
    private static final int FILL_TYPE_PASSWORD_TWICE = 2;
    private static final int FILL_TYPE_TWICE = 1;
    private static final int HW_KEYCHAIN_PLUGIN_ID = 20;
    private static final String INITIAL_VALUE_UNCHANGED_KEY = "initialValueUnchanged";
    private static final String IS_SAVING_KEY = "isSaving";
    private static final String SECURITY_SERVICE = "securityserver";
    private static final String SHOULD_HANDLE_INITIAL_VALUE_UNCHANGED_KEY = "shouldHandleInitialValueUnchanged";
    private static final String TAG = "HwAutofillHelper";
    private static final String VALUE_ARRAY_KEY = "values";
    private static final Object mInstanceSync = new Object();
    private static IHwKeychainManager sHwKeychainManager;

    private boolean isHwAutofillService(String packageName) {
        return AUTOFILL_PACKAGE_VALUE_HW.equals(packageName);
    }

    public boolean isHwAutofillService(Context context) {
        if (context == null) {
            Log.e(TAG, "context is null in isHwAutofillService");
            return false;
        }
        boolean resultValue = false;
        String setting = Settings.Secure.getString(context.getContentResolver(), AUTOFILL_SERVICE_KEY);
        if (setting != null) {
            ComponentName componentName = ComponentName.unflattenFromString(setting);
            if (componentName != null) {
                resultValue = AUTOFILL_SERVICE_VALUE_HW.equals(componentName.flattenToString());
            } else {
                resultValue = false;
            }
        }
        return resultValue;
    }

    public void cacheCurrentData(Bundle clientState, String serviceName, AutofillId[] requiredIds, ArrayMap<AutofillId, AutofillValue> currentValues) {
        if (clientState != null && requiredIds != null && requiredIds.length > 0 && currentValues != null && isHwAutofillService(serviceName)) {
            ArrayList<String> arrayList = new ArrayList<>();
            for (AutofillId autofillId : requiredIds) {
                AutofillValue value = currentValues.get(autofillId);
                if (value == null || value.getTextValue() == null) {
                    arrayList.add("");
                } else {
                    arrayList.add(value.getTextValue().toString());
                }
            }
            clientState.putStringArrayList(VALUE_ARRAY_KEY, arrayList);
        }
    }

    private static boolean isPasswordInputType(int inputType) {
        return (inputType & 4080) == 128;
    }

    public void recordCurrentInfo(Context context, View view) {
        if (context == null || view == null) {
            Log.e(TAG, "context or view is null in remindAtFirstTime");
            return;
        }
        Log.d(TAG, "recordCurrentInfo:" + context.getUserId());
        boolean isPasswordInputType = false;
        if (view instanceof TextView) {
            int inputType = ((TextView) view).getInputType();
            Log.d(TAG, "inputType = " + inputType);
            isPasswordInputType = isPasswordInputType(inputType);
        }
        if (isPasswordInputType) {
            Log.d(TAG, "inputType is password, now record");
            IHwKeychainManager manager = getHwKeychainManagerService();
            if (manager != null) {
                try {
                    manager.recordCurrentInfo(context.getUserId());
                } catch (RemoteException e) {
                    Log.e(TAG, "recordCurrentInfo failed!");
                }
            } else {
                Log.e(TAG, "getHwKeychainManagerService = null");
            }
        }
    }

    private static IHwKeychainManager getHwKeychainManagerService() {
        synchronized (mInstanceSync) {
            if (sHwKeychainManager != null) {
                IHwKeychainManager iHwKeychainManager = sHwKeychainManager;
                return iHwKeychainManager;
            }
            IHwSecurityService secService = IHwSecurityService.Stub.asInterface(ServiceManager.getService(SECURITY_SERVICE));
            if (secService != null) {
                try {
                    sHwKeychainManager = IHwKeychainManager.Stub.asInterface(secService.querySecurityInterface(20));
                } catch (RemoteException e) {
                    Log.e(TAG, "getHwKeychainManagerService failed!");
                }
            } else {
                Log.e(TAG, "secService is null!");
            }
            IHwKeychainManager iHwKeychainManager2 = sHwKeychainManager;
            return iHwKeychainManager2;
        }
    }

    public void updateAutoFillManagerClient(Bundle clientState, String serviceName, IAutoFillManagerClient client, int sessionId, List<AutofillId> ids, List<AutofillValue> values) {
        if (client != null && ids != null && values != null && isHwAutofillService(serviceName)) {
            int fillType = getFillType(clientState);
            if (fillType != 0) {
                if (fillType == 1) {
                    try {
                        client.autofill(sessionId, ids, values);
                    } catch (RemoteException e) {
                        Log.e(TAG, "exception in updateAutoFillManagerClient:" + e.getMessage());
                    }
                } else if (fillType == 2 && ids.size() == 2 && values.size() == 2) {
                    List<AutofillId> newIds = new LinkedList<>();
                    newIds.add(ids.get(1));
                    List<AutofillValue> newValues = new LinkedList<>();
                    newValues.add(values.get(1));
                    client.autofill(sessionId, newIds, newValues);
                }
            }
        }
    }

    private int getFillType(Bundle clientState) {
        if (clientState != null) {
            return clientState.getInt(FILL_TYPE_KEY, 0);
        }
        return 0;
    }

    public boolean updateInitialFlag(Bundle clientState, String serviceName) {
        if (clientState == null || !isHwAutofillService(serviceName)) {
            return false;
        }
        boolean result = clientState.getBoolean(SHOULD_HANDLE_INITIAL_VALUE_UNCHANGED_KEY, false);
        if (result) {
            Log.d(TAG, "updateChangedFlagForInitialValueUnchanged");
            clientState.putBoolean(INITIAL_VALUE_UNCHANGED_KEY, true);
        }
        return result;
    }

    public boolean shouldForbidFillRequest(Bundle clientState, String serviceName) {
        if (clientState == null || !isHwAutofillService(serviceName)) {
            return false;
        }
        boolean result = clientState.getBoolean(IS_SAVING_KEY, false);
        if (result) {
            Log.d(TAG, "shouldForbiddenFillRequest");
        }
        return result;
    }

    public void recordSavedState(Bundle clientState, String serviceName) {
        if (clientState != null && isHwAutofillService(serviceName)) {
            clientState.putBoolean(IS_SAVING_KEY, true);
        }
    }

    public void resizeLayoutForLowResolution(View anchor, WindowManager.LayoutParams params) {
        if (anchor != null) {
            DisplayMetrics dm = anchor.getContext().getResources().getDisplayMetrics();
            int widthPixels = dm.widthPixels;
            if (widthPixels != 0) {
                float ratio = (((float) dm.noncompatWidthPixels) / ((float) widthPixels)) * 1.0f;
                params.x = (int) (((float) params.x) * ratio);
                params.y = (int) (((float) params.y) * ratio);
            }
        }
    }
}
