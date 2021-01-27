package com.huawei.server.security.hwkeychain;

import android.content.ComponentName;
import android.content.Context;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.provider.Settings;
import android.securitydiagnose.HwSecurityDiagnoseManager;
import android.support.annotation.NonNull;
import android.text.TextUtils;
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
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
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
            LogUtils.debug(HwKeychainService.TAG, "Create HwKeychainService.");
            return new HwKeychainService(context);
        }

        @Override // com.huawei.server.security.core.IHwSecurityPlugin.Creator
        public String getPluginPermission() {
            return null;
        }
    };
    private static final String DIVIDER_CHARACTER = "-";
    private static final int HWAUTOFILL_SERVICE_AVAILABLE = 1;
    private static final int HWAUTOFILL_SERVICE_UNAVAILABLE = 0;
    private static final String IS_DECISION_REPORT_KEY = "isDecisionReport";
    private static final String IS_FILLING_USED_KEY = "isFillingUsed";
    private static final String IS_FIRST_REMIND_KEY = "isFirstRemind";
    private static final String IS_KEYCHAIN_ENABLE_KEY = "isKeychainEnable";
    private static final String IS_PRIMARY_USER_KEY = "isPrimaryUser";
    private static final String IS_ROOT_KEY = "isroot";
    private static final String IS_USED_KEY = "isUsed";
    private static final String JSON_EXCEPTION_LOG_STRING = "Unsupported data parse event occurred";
    private static final String OP_KEY = "OP";
    private static final String SYSTEM_DATA_PATH = "system";
    private static final String TAG = "HwKeychainService";
    private static final int TYPE_HWKEYCHAIN_FIRST_REMIND_EVENT = 991310700;
    private static final int TYPE_HWKEYCHAIN_GLOABLE_SWITCH_STATE_EVENT = 991310701;
    private static final String WEEK_REMIND_KEY = "weekRemind";
    private Context mContext;

    public HwKeychainService(@NonNull Context context) {
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
            LogUtils.error(TAG, "The context is null in isHwAutofillService.");
            return false;
        }
        String setting = Settings.Secure.getString(context.getContentResolver(), AUTOFILL_SERVICE_KEY);
        if (setting == null || (componentName = ComponentName.unflattenFromString(setting)) == null) {
            return false;
        }
        return AUTOFILL_SERVICE_VALUE_HW.equals(componentName.flattenToString());
    }

    public void recordCurrentInfo(int userId) {
        LogUtils.debug(TAG, "Start recordCurrentInfo.");
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
            LogUtils.info(TAG, "Auto execute event in recordCurrentInfo.");
            Map<String, Object> extras = new HashMap<>();
            extras.put(IS_PRIMARY_USER_KEY, Boolean.valueOf(userId == 0));
            extras.put(IS_KEYCHAIN_ENABLE_KEY, Boolean.valueOf(isHwAutofillService));
            extras.put(IS_FILLING_USED_KEY, Boolean.valueOf(isUsedFlag(dict)));
            DecisionUtil.getInstance().autoExecuteEvent(extras);
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

    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0062, code lost:
        r6 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:?, code lost:
        r4.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x0067, code lost:
        r7 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x0068, code lost:
        r5.addSuppressed(r7);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x006b, code lost:
        throw r6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x006e, code lost:
        r5 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:?, code lost:
        r3.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x0073, code lost:
        r6 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x0074, code lost:
        r4.addSuppressed(r6);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:0x0077, code lost:
        throw r5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:0x007a, code lost:
        r4 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:45:?, code lost:
        r1.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:46:0x007f, code lost:
        r5 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:47:0x0080, code lost:
        r3.addSuppressed(r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:48:0x0083, code lost:
        throw r4;
     */
    private JSONObject getConfigureDictionary() {
        File file = getAutofillFlagFile();
        if (!file.exists()) {
            LogUtils.error(TAG, "The file is not exist in getConfigureDictionary.");
            return new JSONObject();
        }
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, StandardCharsets.UTF_8);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            StringBuilder stringBuilder = new StringBuilder();
            while (true) {
                String line = bufferedReader.readLine();
                if (line != null) {
                    if (TextUtils.isEmpty(line)) {
                        LogUtils.error(TAG, "The read buffer is empty in getConfigureDictionary.");
                    }
                    stringBuilder.append(line);
                    stringBuilder.append(System.lineSeparator());
                } else {
                    JSONObject jSONObject = new JSONObject(stringBuilder.toString());
                    bufferedReader.close();
                    inputStreamReader.close();
                    fileInputStream.close();
                    return jSONObject;
                }
            }
        } catch (FileNotFoundException e) {
            LogUtils.error(TAG, "File not found in getConfigureDictionary.");
            return new JSONObject();
        } catch (UnsupportedEncodingException e2) {
            LogUtils.error(TAG, "Unsupported encode in getConfigureDictionary.");
            return new JSONObject();
        } catch (IOException e3) {
            LogUtils.error(TAG, "Unsupported input event occurred in getConfigureDictionary.");
            return new JSONObject();
        } catch (JSONException e4) {
            LogUtils.error(TAG, "Unsupported data parse event occurred in getConfigureDictionary.");
            return new JSONObject();
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0029, code lost:
        r4 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:?, code lost:
        r2.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x002e, code lost:
        r5 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x002f, code lost:
        r3.addSuppressed(r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0032, code lost:
        throw r4;
     */
    private void saveConfigureDictionary(JSONObject dict) {
        if (dict == null) {
            LogUtils.error(TAG, "The context is null in saveConfigureDictionary.");
            return;
        }
        try {
            FileOutputStream outputStream = new FileOutputStream(getAutofillFlagFile());
            outputStream.write(dict.toString().getBytes(StandardCharsets.UTF_8));
            outputStream.flush();
            outputStream.close();
        } catch (FileNotFoundException e) {
            LogUtils.error(TAG, "File not found in saveConfigureDictionary.");
        } catch (UnsupportedEncodingException e2) {
            LogUtils.error(TAG, "Unsupported encode in saveConfigureDictionary.");
        } catch (IOException e3) {
            LogUtils.error(TAG, "Unsupported data parse event occurred in saveConfigureDictionary.");
        }
    }

    private boolean checkFirstRemind(Context context, JSONObject dict, boolean isHwAutofillService) {
        boolean isFirstRemind = true;
        if (dict.has(IS_FIRST_REMIND_KEY)) {
            try {
                isFirstRemind = dict.getBoolean(IS_FIRST_REMIND_KEY);
            } catch (JSONException e) {
                LogUtils.error(TAG, "Unsupported data parse event occurred in checkFirstRemind.");
            }
        }
        if (isFirstRemind) {
            JSONObject eventDict = new JSONObject();
            try {
                eventDict.put(OP_KEY, isHwAutofillService ? 1 : 0);
                eventDict.put(IS_ROOT_KEY, isRoot());
            } catch (JSONException e2) {
                LogUtils.error(TAG, "Unsupported data parse event occurred in checkFirstRemind.");
            }
            HiViewEx.report(HiViewEx.byContent((int) TYPE_HWKEYCHAIN_FIRST_REMIND_EVENT, context, eventDict.toString()));
            try {
                dict.put(IS_FIRST_REMIND_KEY, false);
            } catch (JSONException e3) {
                LogUtils.error(TAG, "Unsupported data parse event occurred in checkFirstRemind.");
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
                LogUtils.error(TAG, "Unsupported data parse event occurred in checkWeekRemind.");
            }
        }
        Calendar calendar = Calendar.getInstance();
        int week = calendar.get(3);
        String currentWeekRemind = calendar.get(1) + DIVIDER_CHARACTER + week;
        int i = 0;
        if (currentWeekRemind.equals(weekRemind)) {
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
            LogUtils.error(TAG, "Unsupported data parse event occurred in checkWeekRemind.");
        }
        HiViewEx.report(HiViewEx.byContent((int) TYPE_HWKEYCHAIN_GLOABLE_SWITCH_STATE_EVENT, context, eventDict.toString()));
        try {
            dict.put(WEEK_REMIND_KEY, currentWeekRemind);
        } catch (JSONException e3) {
            LogUtils.error(TAG, "Unsupported data parse event occurred in checkWeekRemind.");
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
            LogUtils.error(TAG, "Unsupported data parse event occurred in isUsedFlag.");
            return false;
        }
    }

    private boolean isDecisionReport(JSONObject dict) {
        String decisionReport = null;
        if (dict.has(IS_DECISION_REPORT_KEY)) {
            try {
                decisionReport = dict.getString(IS_DECISION_REPORT_KEY);
            } catch (JSONException e) {
                LogUtils.error(TAG, "Unsupported data parse event occurred in isDecisionReport.");
            }
        }
        if (decisionReport != null) {
            return true ^ getCurrentDecisionReport().equals(decisionReport);
        }
        LogUtils.warn(TAG, "DecisionReport is null in isDecisionReport.");
        return true;
    }

    private String getCurrentDecisionReport() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(1);
        int month = calendar.get(2);
        int day = calendar.get(5);
        return year + DIVIDER_CHARACTER + month + DIVIDER_CHARACTER + day;
    }

    private void setDecisionReport(JSONObject dict) {
        try {
            dict.put(IS_DECISION_REPORT_KEY, getCurrentDecisionReport());
        } catch (JSONException e) {
            LogUtils.error(TAG, "Unsupported data parse event occurred in setDecisionReport.");
        }
    }

    private boolean isRoot() {
        long identity = Binder.clearCallingIdentity();
        HwSecurityDiagnoseManager mSdm = HwSecurityDiagnoseManager.getInstance();
        if (mSdm == null) {
            Binder.restoreCallingIdentity(identity);
            return true;
        }
        int rootStatus = mSdm.getRootStatusSync();
        Binder.restoreCallingIdentity(identity);
        if (rootStatus != 0) {
            return true;
        }
        return false;
    }
}
