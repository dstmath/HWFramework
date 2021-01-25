package com.huawei.server.security.hwkeychain;

import android.content.ComponentName;
import android.content.Context;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.provider.Settings;
import android.securitydiagnose.HwSecurityDiagnoseManager;
import android.text.TextUtils;
import android.util.Log;
import com.huawei.android.app.HiViewEx;
import com.huawei.server.security.core.IHwSecurityPlugin;
import huawei.android.security.IHwKeychainManager;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;

public class HwKeychainService extends IHwKeychainManager.Stub implements IHwSecurityPlugin {
    private static final String AUTOFILL_FLAG_FILE_NAME = "autofill_flag";
    private static final String AUTOFILL_SERVICE_KEY = "autofill_service";
    private static final String AUTOFILL_SERVICE_VALUE_HW = "com.huawei.securitymgr/com.huawei.keychain.service.HwAutofillService";
    public static final IHwSecurityPlugin.Creator CREATOR = new IHwSecurityPlugin.Creator() {
        /* class com.huawei.server.security.hwkeychain.HwKeychainService.AnonymousClass1 */

        @Override // com.huawei.server.security.core.IHwSecurityPlugin.Creator
        public IHwSecurityPlugin createPlugin(Context context) {
            Log.d(HwKeychainService.TAG, "create HwKeychainService");
            return new HwKeychainService(context);
        }

        @Override // com.huawei.server.security.core.IHwSecurityPlugin.Creator
        public String getPluginPermission() {
            return null;
        }
    };
    private static final String IS_DECISION_REPORT_KEY = "isDecisionReport";
    private static final String IS_FILLING_USED_KEY = "isFillingUsed";
    private static final String IS_FIRST_REMIND_KEY = "isFirstRemind";
    private static final String IS_KEYCHAIN_ENABLE_KEY = "isKeychainEnable";
    private static final String IS_PRIMARY_USER_KEY = "isPrimaryUser";
    private static final String IS_ROOT_KEY = "isroot";
    private static final String IS_USED_KEY = "isUsed";
    private static final String OP_KEY = "OP";
    private static final String SYSTEM_DATA_PATH = "system";
    private static final String TAG = "HwKeychainService";
    private static final int TYPE_HWKEYCHAIN_FIRST_REMIND_EVENT = 991310700;
    private static final int TYPE_HWKEYCHAIN_GLOABLE_SWITCH_STATE_EVENT = 991310701;
    private static final String WEEK_REMIND_KEY = "weekRemind";
    private Context mContext;

    public HwKeychainService(Context context) {
        this.mContext = context;
    }

    /* JADX DEBUG: Multi-variable search result rejected for r0v0, resolved type: com.huawei.server.security.hwkeychain.HwKeychainService */
    /* JADX WARN: Multi-variable type inference failed */
    @Override // com.huawei.server.security.core.IHwSecurityPlugin
    public IBinder asBinder() {
        return this;
    }

    @Override // com.huawei.server.security.core.IHwSecurityPlugin
    public void onStart() {
    }

    @Override // com.huawei.server.security.core.IHwSecurityPlugin
    public void onStop() {
    }

    private boolean isHwAutofillService(Context context) {
        ComponentName componentName;
        if (context == null) {
            Log.e(TAG, "context is null in isHwAutofillService");
            return false;
        }
        String setting = Settings.Secure.getString(context.getContentResolver(), AUTOFILL_SERVICE_KEY);
        if (setting == null || (componentName = ComponentName.unflattenFromString(setting)) == null) {
            return false;
        }
        return AUTOFILL_SERVICE_VALUE_HW.equals(componentName.flattenToString());
    }

    public void recordCurrentInfo(int userId) {
        Log.d(TAG, "start recordCurrentInfo");
        JSONObject dict = getConfigureDictionary();
        boolean isHwAutofillService = isHwAutofillService(this.mContext);
        boolean isChanged = false;
        if (checkFirstRemind(this.mContext, dict, isHwAutofillService)) {
            isChanged = true;
        }
        if (checkWeekRemind(this.mContext, dict, isHwAutofillService)) {
            isChanged = true;
        }
        if (isDecisionReport(dict)) {
            Log.d(TAG, "DecisionUtil: bindservice");
            Map<String, Object> extras = new HashMap<>();
            extras.put(IS_PRIMARY_USER_KEY, Boolean.valueOf(userId == 0));
            extras.put(IS_KEYCHAIN_ENABLE_KEY, Boolean.valueOf(isHwAutofillService));
            extras.put(IS_FILLING_USED_KEY, Boolean.valueOf(isUsedFlag(dict)));
            DecisionUtil.autoExecuteEvent(extras);
            setDecisionReport(dict);
            isChanged = true;
        }
        if (isChanged) {
            saveConfigureDictionary(dict);
        }
    }

    private File getAutofillFlagFile() {
        return new File(new File(Environment.getDataDirectory(), SYSTEM_DATA_PATH), AUTOFILL_FLAG_FILE_NAME);
    }

    private JSONObject getConfigureDictionary() {
        File file = getAutofillFlagFile();
        if (!file.exists()) {
            return new JSONObject();
        }
        InputStreamReader inputStreamReader = null;
        BufferedReader bufferedReader = null;
        FileInputStream fileInputStream = null;
        try {
            FileInputStream fileInputStream2 = new FileInputStream(file);
            InputStreamReader inputStreamReader2 = new InputStreamReader(fileInputStream2, "utf-8");
            BufferedReader bufferedReader2 = new BufferedReader(inputStreamReader2);
            StringBuilder stringBuilder = new StringBuilder();
            while (true) {
                String line = bufferedReader2.readLine();
                if (line == null) {
                    break;
                }
                if (TextUtils.isEmpty(line)) {
                    Log.e(TAG, "line is empty");
                }
                stringBuilder.append(line);
                stringBuilder.append("\n");
            }
            JSONObject jSONObject = new JSONObject(stringBuilder.toString());
            try {
                bufferedReader2.close();
            } catch (IOException e) {
                Log.e(TAG, "close bufferedReader IOException in getConfigureDictionary");
            }
            try {
                inputStreamReader2.close();
            } catch (IOException e2) {
                Log.e(TAG, "close fileReader IOException in getConfigureDictionary");
            }
            try {
                fileInputStream2.close();
            } catch (IOException e3) {
                Log.e(TAG, "close fileInputStream IOException in getConfigureDictionary");
            }
            return jSONObject;
        } catch (FileNotFoundException e4) {
            Log.e(TAG, "FileNotFoundException in getConfigureDictionary");
            if (0 != 0) {
                try {
                    bufferedReader.close();
                } catch (IOException e5) {
                    Log.e(TAG, "close bufferedReader IOException in getConfigureDictionary");
                }
            }
            if (0 != 0) {
                try {
                    inputStreamReader.close();
                } catch (IOException e6) {
                    Log.e(TAG, "close fileReader IOException in getConfigureDictionary");
                }
            }
            if (0 != 0) {
                fileInputStream.close();
            }
        } catch (IOException e7) {
            Log.e(TAG, "IOException in getConfigureDictionary");
            if (0 != 0) {
                try {
                    bufferedReader.close();
                } catch (IOException e8) {
                    Log.e(TAG, "close bufferedReader IOException in getConfigureDictionary");
                }
            }
            if (0 != 0) {
                try {
                    inputStreamReader.close();
                } catch (IOException e9) {
                    Log.e(TAG, "close fileReader IOException in getConfigureDictionary");
                }
            }
            if (0 != 0) {
                fileInputStream.close();
            }
        } catch (JSONException e10) {
            Log.e(TAG, "JSONException in getConfigureDictionary");
            if (0 != 0) {
                try {
                    bufferedReader.close();
                } catch (IOException e11) {
                    Log.e(TAG, "close bufferedReader IOException in getConfigureDictionary");
                }
            }
            if (0 != 0) {
                try {
                    inputStreamReader.close();
                } catch (IOException e12) {
                    Log.e(TAG, "close fileReader IOException in getConfigureDictionary");
                }
            }
            if (0 != 0) {
                try {
                    fileInputStream.close();
                } catch (IOException e13) {
                    Log.e(TAG, "close fileInputStream IOException in getConfigureDictionary");
                }
            }
        } catch (Throwable th) {
            if (0 != 0) {
                try {
                    bufferedReader.close();
                } catch (IOException e14) {
                    Log.e(TAG, "close bufferedReader IOException in getConfigureDictionary");
                }
            }
            if (0 != 0) {
                try {
                    inputStreamReader.close();
                } catch (IOException e15) {
                    Log.e(TAG, "close fileReader IOException in getConfigureDictionary");
                }
            }
            if (0 != 0) {
                try {
                    fileInputStream.close();
                } catch (IOException e16) {
                    Log.e(TAG, "close fileInputStream IOException in getConfigureDictionary");
                }
            }
            throw th;
        }
        return new JSONObject();
    }

    private void saveConfigureDictionary(JSONObject dict) {
        if (dict != null) {
            FileOutputStream outputStream = null;
            try {
                outputStream = new FileOutputStream(getAutofillFlagFile());
                outputStream.write(dict.toString().getBytes("utf-8"));
                outputStream.flush();
                try {
                    outputStream.close();
                } catch (IOException e) {
                    Log.e(TAG, "close outputStream IOException in saveConfigureDictionary");
                }
            } catch (FileNotFoundException e2) {
                Log.e(TAG, "FileNotFoundException in saveConfigureDictionary");
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (IOException e3) {
                Log.e(TAG, "IOException in saveConfigureDictionary");
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (Throwable th) {
                if (outputStream != null) {
                    try {
                        outputStream.close();
                    } catch (IOException e4) {
                        Log.e(TAG, "close outputStream IOException in saveConfigureDictionary");
                    }
                }
                throw th;
            }
        }
    }

    private boolean checkFirstRemind(Context context, JSONObject dict, boolean isHwAutofillService) {
        boolean isFirstRemind = true;
        if (dict.has(IS_FIRST_REMIND_KEY)) {
            try {
                isFirstRemind = dict.getBoolean(IS_FIRST_REMIND_KEY);
            } catch (JSONException e) {
                Log.e(TAG, "JSON exception in checkFirstRemind get isFirstRemind: " + e.getMessage());
            }
        }
        if (isFirstRemind) {
            JSONObject eventDict = new JSONObject();
            try {
                eventDict.put(OP_KEY, isHwAutofillService ? 1 : 0);
                eventDict.put(IS_ROOT_KEY, isRoot());
            } catch (JSONException e2) {
                Log.e(TAG, "JSON exception in checkFirstRemind event isFirstRemind: " + e2.getMessage());
            }
            HiViewEx.report(HiViewEx.byContent((int) TYPE_HWKEYCHAIN_FIRST_REMIND_EVENT, context, eventDict.toString()));
            try {
                dict.put(IS_FIRST_REMIND_KEY, false);
            } catch (JSONException e3) {
                Log.e(TAG, "JSON exception in checkFirstRemind update isFirstRemind: " + e3.getMessage());
            }
        }
        return isFirstRemind;
    }

    private boolean checkWeekRemind(Context context, JSONObject dict, boolean isHwAutofillService) {
        String weekRemind = null;
        if (dict.has(WEEK_REMIND_KEY)) {
            try {
                weekRemind = dict.getString(WEEK_REMIND_KEY);
            } catch (JSONException e) {
                Log.e(TAG, "JSON exception in checkWeekRemind get weekRemind: " + e.getMessage());
            }
        }
        Calendar calendar = Calendar.getInstance();
        int week = calendar.get(3);
        String currentWeekRemind = calendar.get(1) + "-" + week;
        int i = 0;
        if (weekRemind != null && currentWeekRemind.equals(weekRemind)) {
            return false;
        }
        JSONObject eventDict = new JSONObject();
        if (isHwAutofillService) {
            i = 1;
        }
        try {
            eventDict.put(OP_KEY, i);
            eventDict.put(IS_ROOT_KEY, isRoot());
        } catch (JSONException e2) {
            Log.e(TAG, "JSON exception in checkWeekRemind event: " + e2.getMessage());
        }
        HiViewEx.report(HiViewEx.byContent((int) TYPE_HWKEYCHAIN_GLOABLE_SWITCH_STATE_EVENT, context, eventDict.toString()));
        try {
            dict.put(WEEK_REMIND_KEY, currentWeekRemind);
        } catch (JSONException e3) {
            Log.e(TAG, "JSON exception in checkWeekRemind update: " + e3.getMessage());
        }
        return true;
    }

    private boolean isUsedFlag(JSONObject dict) {
        try {
            if (!dict.has(IS_USED_KEY) || !dict.getBoolean(IS_USED_KEY)) {
                return false;
            }
            return true;
        } catch (JSONException e) {
            Log.e(TAG, "JSON exception in isUsedFlag: " + e.getMessage());
            return false;
        }
    }

    private boolean isDecisionReport(JSONObject dict) {
        String decisionReport = null;
        if (dict.has(IS_DECISION_REPORT_KEY)) {
            try {
                decisionReport = dict.getString(IS_DECISION_REPORT_KEY);
            } catch (JSONException e) {
                Log.e(TAG, "JSON exception in isDecisionReport get decisionReport: " + e.getMessage());
            }
        }
        if (decisionReport == null) {
            Log.w(TAG, "decisionReport is null.");
            return true;
        }
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(1);
        int month = calendar.get(2);
        int day = calendar.get(5);
        return true ^ (year + "-" + month + "-" + day).equals(decisionReport);
    }

    private void setDecisionReport(JSONObject dict) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(1);
        int month = calendar.get(2);
        int day = calendar.get(5);
        try {
            dict.put(IS_DECISION_REPORT_KEY, year + "-" + month + "-" + day);
        } catch (JSONException e) {
            Log.e(TAG, "JSON exception in setDecisionReport: " + e.getMessage());
        }
    }

    private boolean isRoot() {
        long identity = Binder.clearCallingIdentity();
        HwSecurityDiagnoseManager mSdm = HwSecurityDiagnoseManager.getInstance();
        if (mSdm == null) {
            return true;
        }
        int rootstatus = mSdm.getRootStatusSync();
        Binder.restoreCallingIdentity(identity);
        if (rootstatus != 0) {
            return true;
        }
        return false;
    }
}
