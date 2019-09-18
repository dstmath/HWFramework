package com.android.server.security.hwkeychain;

import android.content.ComponentName;
import android.content.Context;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.provider.Settings;
import android.securitydiagnose.HwSecurityDiagnoseManager;
import android.util.Flog;
import android.util.Log;
import com.android.server.security.core.IHwSecurityPlugin;
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
        public IHwSecurityPlugin createPlugin(Context context) {
            Log.d(HwKeychainService.TAG, "create HwKeychainService");
            return new HwKeychainService(context);
        }

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
    private static final String WEEK_REMIND_KEY = "weekRemind";
    private Context mContext;

    public HwKeychainService(Context context) {
        this.mContext = context;
    }

    /* JADX WARNING: type inference failed for: r0v0, types: [com.android.server.security.hwkeychain.HwKeychainService, android.os.IBinder] */
    public IBinder asBinder() {
        return this;
    }

    public void onStart() {
    }

    public void onStop() {
    }

    private boolean isHwAutofillService(Context context) {
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
        String str;
        StringBuilder sb;
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
                String readLine = bufferedReader2.readLine();
                String line = readLine;
                if (readLine == null) {
                    break;
                }
                stringBuilder.append(line);
                stringBuilder.append("\n");
            }
            String inputString = stringBuilder.toString();
            bufferedReader2.close();
            BufferedReader bufferedReader3 = null;
            inputStreamReader2.close();
            InputStreamReader inputStreamReader3 = null;
            fileInputStream2.close();
            FileInputStream fileInputStream3 = null;
            JSONObject jSONObject = new JSONObject(inputString);
            if (bufferedReader3 != null) {
                try {
                    bufferedReader3.close();
                } catch (IOException e) {
                    Log.e(TAG, "close bufferedReader IOException in getConfigureDictionary: " + e.getMessage());
                }
            }
            if (inputStreamReader3 != null) {
                try {
                    inputStreamReader3.close();
                } catch (IOException e2) {
                    Log.e(TAG, "close fileReader IOException in getConfigureDictionary: " + e2.getMessage());
                }
            }
            if (fileInputStream3 != null) {
                try {
                    fileInputStream3.close();
                } catch (IOException e3) {
                    Log.e(TAG, "close fileInputStream IOException in getConfigureDictionary: " + e3.getMessage());
                }
            }
            return jSONObject;
        } catch (FileNotFoundException e4) {
            Log.e(TAG, "FileNotFoundException in getConfigureDictionary: " + e4.getMessage());
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e5) {
                    Log.e(TAG, "close bufferedReader IOException in getConfigureDictionary: " + e5.getMessage());
                }
            }
            if (inputStreamReader != null) {
                try {
                    inputStreamReader.close();
                } catch (IOException e6) {
                    Log.e(TAG, "close fileReader IOException in getConfigureDictionary: " + e6.getMessage());
                }
            }
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e7) {
                    e = e7;
                    str = TAG;
                    sb = new StringBuilder();
                }
            }
            return new JSONObject();
        } catch (IOException e8) {
            Log.e(TAG, "IOException in getConfigureDictionary: " + e8.getMessage());
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e9) {
                    Log.e(TAG, "close bufferedReader IOException in getConfigureDictionary: " + e9.getMessage());
                }
            }
            if (inputStreamReader != null) {
                try {
                    inputStreamReader.close();
                } catch (IOException e10) {
                    Log.e(TAG, "close fileReader IOException in getConfigureDictionary: " + e10.getMessage());
                }
            }
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e11) {
                    e = e11;
                    str = TAG;
                    sb = new StringBuilder();
                }
            }
            return new JSONObject();
        } catch (JSONException e12) {
            Log.e(TAG, "JSONException in getConfigureDictionary: " + e12.getMessage());
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e13) {
                    Log.e(TAG, "close bufferedReader IOException in getConfigureDictionary: " + e13.getMessage());
                }
            }
            if (inputStreamReader != null) {
                try {
                    inputStreamReader.close();
                } catch (IOException e14) {
                    Log.e(TAG, "close fileReader IOException in getConfigureDictionary: " + e14.getMessage());
                }
            }
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e15) {
                    e = e15;
                    str = TAG;
                    sb = new StringBuilder();
                }
            }
            return new JSONObject();
        } catch (Throwable th) {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e16) {
                    Log.e(TAG, "close bufferedReader IOException in getConfigureDictionary: " + e16.getMessage());
                }
            }
            if (inputStreamReader != null) {
                try {
                    inputStreamReader.close();
                } catch (IOException e17) {
                    Log.e(TAG, "close fileReader IOException in getConfigureDictionary: " + e17.getMessage());
                }
            }
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e18) {
                    Log.e(TAG, "close fileInputStream IOException in getConfigureDictionary: " + e18.getMessage());
                }
            }
            throw th;
        }
        sb.append("close fileInputStream IOException in getConfigureDictionary: ");
        sb.append(e.getMessage());
        Log.e(str, sb.toString());
        return new JSONObject();
    }

    private void saveConfigureDictionary(JSONObject dict) {
        String str;
        StringBuilder sb;
        if (dict != null) {
            FileOutputStream outputStream = null;
            try {
                outputStream = new FileOutputStream(getAutofillFlagFile());
                outputStream.write(dict.toString().getBytes("utf-8"));
                outputStream.flush();
                outputStream.close();
                FileOutputStream outputStream2 = null;
                if (outputStream2 != null) {
                    try {
                        outputStream2.close();
                    } catch (IOException e) {
                        e = e;
                        str = TAG;
                        sb = new StringBuilder();
                    }
                }
            } catch (FileNotFoundException e2) {
                Log.e(TAG, "FileNotFoundException in saveConfigureDictionary: " + e2.getMessage());
                if (outputStream != null) {
                    try {
                        outputStream.close();
                    } catch (IOException e3) {
                        e = e3;
                        str = TAG;
                        sb = new StringBuilder();
                    }
                }
            } catch (IOException e4) {
                Log.e(TAG, "IOException in saveConfigureDictionary: " + e4.getMessage());
                if (outputStream != null) {
                    try {
                        outputStream.close();
                    } catch (IOException e5) {
                        e = e5;
                        str = TAG;
                        sb = new StringBuilder();
                    }
                }
            } catch (Throwable th) {
                if (outputStream != null) {
                    try {
                        outputStream.close();
                    } catch (IOException e6) {
                        Log.e(TAG, "close outputStream IOException in saveConfigureDictionary: " + e6.getMessage());
                    }
                }
                throw th;
            }
        }
        return;
        sb.append("close outputStream IOException in saveConfigureDictionary: ");
        sb.append(e.getMessage());
        Log.e(str, sb.toString());
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
                eventDict.put(OP_KEY, isHwAutofillService);
                eventDict.put(IS_ROOT_KEY, isRoot());
            } catch (JSONException e2) {
                Log.e(TAG, "JSON exception in checkFirstRemind event isFirstRemind: " + e2.getMessage());
            }
            Flog.bdReport(context, 700, eventDict);
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
        int year = calendar.get(1);
        String currentWeekRemind = year + "-" + week;
        if (currentWeekRemind.equals(weekRemind)) {
            return false;
        }
        JSONObject eventDict = new JSONObject();
        try {
            eventDict.put(OP_KEY, isHwAutofillService);
            eventDict.put(IS_ROOT_KEY, isRoot());
        } catch (JSONException e2) {
            Log.e(TAG, "JSON exception in checkWeekRemind event: " + e2.getMessage());
        }
        Flog.bdReport(context, 701, eventDict);
        try {
            dict.put(WEEK_REMIND_KEY, currentWeekRemind);
        } catch (JSONException e3) {
            Log.e(TAG, "JSON exception in checkWeekRemind update: " + e3.getMessage());
        }
        return true;
    }

    private boolean isUsedFlag(JSONObject dict) {
        try {
            if (dict.has(IS_USED_KEY) && dict.getBoolean(IS_USED_KEY)) {
                return true;
            }
        } catch (JSONException e) {
            Log.e(TAG, "JSON exception in isUsedFlag: " + e.getMessage());
        }
        return false;
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
        boolean z = true;
        int rootstatus = 1;
        try {
            HwSecurityDiagnoseManager mSdm = HwSecurityDiagnoseManager.getInstance();
            if (mSdm == null) {
                Binder.restoreCallingIdentity(identity);
                return true;
            }
            rootstatus = mSdm.getRootStatusSync();
            Binder.restoreCallingIdentity(identity);
            if (rootstatus == 0) {
                z = false;
            }
            return z;
        } catch (Exception e) {
            Log.e(TAG, "isRoot error: " + e.getMessage());
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(identity);
            throw th;
        }
    }
}
