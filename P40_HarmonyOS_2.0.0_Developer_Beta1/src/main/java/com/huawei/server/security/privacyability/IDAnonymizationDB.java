package com.huawei.server.security.privacyability;

import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import com.huawei.util.LogEx;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import org.json.JSONException;
import org.json.JSONObject;

public class IDAnonymizationDB {
    private static final boolean DEBUG = LogEx.getLogHWInfo();
    private static final String DEFAULT_JSON_CONTENT = "{}";
    private static final String KEY_CUID = "CUID";
    static final int RESULT_FAIL = -1;
    static final int RESULT_SUCCESS = 0;
    private static final String TAG = "IDAnonymizationDB";
    private static volatile IDAnonymizationDB sInstance = null;
    private JSONObject mActiveDB = readDatabase(this.mFile);
    private final Path mFile = Paths.get("/data/system/IDAnonymizationDB.json", new String[0]);

    private IDAnonymizationDB() {
    }

    public static IDAnonymizationDB getInstance() {
        if (sInstance == null) {
            synchronized (IDAnonymizationDB.class) {
                if (sInstance == null) {
                    sInstance = new IDAnonymizationDB();
                }
            }
        }
        return sInstance;
    }

    private JSONObject readDatabase(Path file) {
        JSONObject jsonDataBase;
        if (DEBUG) {
            Log.d(TAG, "readDatabase file begin.");
        }
        if (!Files.exists(file, new LinkOption[0])) {
            return parseJson(DEFAULT_JSON_CONTENT);
        }
        try {
            jsonDataBase = parseJson(new String(Files.readAllBytes(file), StandardCharsets.UTF_8));
        } catch (IOException e) {
            Log.e(TAG, "readDatabase IOException: " + e.getMessage());
            jsonDataBase = parseJson(DEFAULT_JSON_CONTENT);
        }
        if (DEBUG) {
            Log.d(TAG, "readDatabase file done.");
        }
        return jsonDataBase;
    }

    private void writeDataBase(Path file, JSONObject jsonDataBase) {
        try {
            Files.write(file, jsonDataBase.toString().getBytes(StandardCharsets.UTF_8), new OpenOption[0]);
        } catch (IOException e) {
            Log.e(TAG, "writeDataBase IOException: " + e.getMessage());
        }
    }

    private void writeActiveDataBaseToNative() {
        writeDataBase(this.mFile, this.mActiveDB);
    }

    @Nullable
    private static JSONObject parseJson(String json) {
        if (json == null) {
            return null;
        }
        try {
            return new JSONObject(json);
        } catch (JSONException e) {
            Log.e(TAG, "parseJson err: " + e.getMessage());
            return null;
        }
    }

    private String parseUserKey(int userId) {
        return "user_" + userId;
    }

    private JSONObject getUserJsonObject(int userId) throws JSONException {
        String userKey = parseUserKey(userId);
        JSONObject result = this.mActiveDB;
        if (result != null) {
            return result.getJSONObject(userKey);
        }
        Log.d(TAG, "the database is null");
        return new JSONObject();
    }

    private JSONObject getPackageJsonObject(int userId, String packageName) throws JSONException {
        return getUserJsonObject(userId).getJSONObject(packageName);
    }

    @Nullable
    private String getActiveValue(int userId, String packageName, String key) {
        try {
            return getPackageJsonObject(userId, packageName).getString(key);
        } catch (JSONException e) {
            Log.w(TAG, packageName + " get no active value from latest DB: " + e.getMessage());
            return null;
        }
    }

    private void removeActiveValue(int userId, String packageName, String key) {
        try {
            JSONObject packageObj = getPackageJsonObject(userId, packageName);
            packageObj.remove(key);
            if (packageObj.length() == 0) {
                getUserJsonObject(userId).remove(packageName);
            }
            writeActiveDataBaseToNative();
        } catch (JSONException e) {
            if (DEBUG) {
                Log.w(TAG, "removeActiveValueByKey json exception: " + e.getMessage());
            }
        }
    }

    private void removeUserDatabase(int userId) {
        this.mActiveDB.remove(parseUserKey(userId));
        writeActiveDataBaseToNative();
    }

    private void setActiveValue(int userId, String packageName, String key, String value) {
        try {
            String userKey = parseUserKey(userId);
            JSONObject userObj = this.mActiveDB.optJSONObject(userKey);
            if (userObj == null) {
                userObj = new JSONObject();
                this.mActiveDB.put(userKey, userObj);
            }
            JSONObject packageObj = userObj.optJSONObject(packageName);
            if (packageObj == null) {
                packageObj = new JSONObject();
                userObj.put(packageName, packageObj);
            }
            packageObj.put(key, value);
            writeActiveDataBaseToNative();
        } catch (JSONException e) {
            Log.w(TAG, "setActiveValue json exception: " + e.getMessage());
        }
    }

    private String newCUID() {
        return UUID.randomUUID().toString();
    }

    /* access modifiers changed from: package-private */
    @Nullable
    public synchronized String getCUID(int userId, String packageName) {
        if (TextUtils.isEmpty(packageName)) {
            Log.e(TAG, "get cuid err for packageName is null");
            return null;
        }
        String activeCuid = getActiveValue(userId, packageName, KEY_CUID);
        if (!TextUtils.isEmpty(activeCuid)) {
            return activeCuid;
        }
        String cuid = newCUID();
        setActiveValue(userId, packageName, KEY_CUID, cuid);
        if (DEBUG) {
            Log.i(TAG, "create new cuid for userId:124 " + userId + ", package: " + packageName);
        }
        return cuid;
    }

    /* access modifiers changed from: package-private */
    public synchronized int removeCUID(int userId, String packageName) {
        if (TextUtils.isEmpty(packageName)) {
            Log.e(TAG, "remove cuid err for packageName is null");
            return -1;
        }
        if (DEBUG) {
            Log.d(TAG, "remove cuid,userId: 123" + userId + ", package: " + packageName);
        }
        removeActiveValue(userId, packageName, KEY_CUID);
        return 0;
    }

    /* access modifiers changed from: package-private */
    public synchronized void removeUserAllData(int userId) {
        if (DEBUG) {
            Log.i(TAG, "removeUserAllData, userId:123 " + userId);
        }
        removeUserDatabase(userId);
    }
}
