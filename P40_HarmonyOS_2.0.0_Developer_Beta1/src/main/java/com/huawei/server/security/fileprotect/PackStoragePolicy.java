package com.huawei.server.security.fileprotect;

import android.content.Context;
import android.util.Log;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/* access modifiers changed from: package-private */
public class PackStoragePolicy {
    private static final int DEFAULT_LIST_SIZE = 4;
    private static final int EXPECTED_BUFFER_SIZE = 5120;
    private static final String KEY_DIR = "dir";
    private static final String KEY_DIR_NAME = "name";
    private static final String KEY_FILE = "file";
    private static final String KEY_FILE_NAME = "name";
    private static final String KEY_PACKAGE = "Package";
    private static final String KEY_PACKAGE_NAME = "name";
    private static final String KEY_STORAGE_TYPE = "StorageType";
    private static final String KEY_TRAVERSAL = "traversal";
    private static final String TAG = "PackStoragePolicy";
    private String mPackageName;
    private List<PathPolicy> mPolicies = new ArrayList(4);

    PackStoragePolicy() {
    }

    public void setPackageName(String packageName) {
        this.mPackageName = packageName;
    }

    public String getPackageName() {
        return this.mPackageName;
    }

    public List<PathPolicy> getPolicies() {
        return this.mPolicies;
    }

    public static List<PackStoragePolicy> parse(Context context, String fileName) {
        int pkgListLen;
        JSONArray packageList;
        JSONObject json;
        String str;
        int pkgListLen2;
        JSONArray packageList2;
        JSONObject json2;
        String path;
        String str2 = KEY_DIR;
        List<PackStoragePolicy> packPolicyList = new ArrayList<>(4);
        if (context == null || fileName == null) {
            Log.w(TAG, "parse: the input value is null!");
            return packPolicyList;
        }
        try {
            JSONObject json3 = new JSONObject(readFileString(context, fileName));
            JSONArray packageList3 = json3.getJSONArray(KEY_PACKAGE);
            int pkgListLen3 = packageList3.length();
            int i = 0;
            while (i < pkgListLen3) {
                JSONObject packagePolicy = packageList3.getJSONObject(i);
                String pkgName = packagePolicy.getString("name");
                PackStoragePolicy oneApp = new PackStoragePolicy();
                oneApp.setPackageName(pkgName);
                if (packagePolicy.has(str2)) {
                    JSONArray dirList = packagePolicy.getJSONArray(str2);
                    int dirListLen = dirList.length();
                    int j = 0;
                    while (j < dirListLen) {
                        JSONObject dirObj = (JSONObject) dirList.get(j);
                        String path2 = dirObj.getString("name");
                        String storageType = dirObj.getString(KEY_STORAGE_TYPE);
                        if (dirObj.getBoolean(KEY_TRAVERSAL)) {
                            json2 = json3;
                            packageList2 = packageList3;
                            pkgListLen2 = pkgListLen3;
                            path = pkgName;
                            oneApp.getPolicies().add(new PathPolicy(path2, storageType, 17));
                        } else {
                            json2 = json3;
                            packageList2 = packageList3;
                            pkgListLen2 = pkgListLen3;
                            path = pkgName;
                            oneApp.getPolicies().add(new PathPolicy(path2, storageType, 16));
                        }
                        j++;
                        str2 = str2;
                        pkgName = path;
                        json3 = json2;
                        packageList3 = packageList2;
                        pkgListLen3 = pkgListLen2;
                    }
                    str = str2;
                    json = json3;
                    packageList = packageList3;
                    pkgListLen = pkgListLen3;
                } else {
                    str = str2;
                    json = json3;
                    packageList = packageList3;
                    pkgListLen = pkgListLen3;
                }
                if (packagePolicy.has(KEY_FILE)) {
                    JSONArray fileList = packagePolicy.getJSONArray(KEY_FILE);
                    int fileListLen = fileList.length();
                    for (int j2 = 0; j2 < fileListLen; j2++) {
                        JSONObject fileObj = (JSONObject) fileList.get(j2);
                        oneApp.getPolicies().add(new PathPolicy(fileObj.getString("name"), fileObj.getString(KEY_STORAGE_TYPE), 0));
                    }
                }
                packPolicyList.add(oneApp);
                i++;
                str2 = str;
                json3 = json;
                packageList3 = packageList;
                pkgListLen3 = pkgListLen;
            }
        } catch (JSONException e) {
            packPolicyList.clear();
        }
        return packPolicyList;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0042, code lost:
        r6 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:?, code lost:
        r5.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0047, code lost:
        r7 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0048, code lost:
        r2.addSuppressed(r7);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x004b, code lost:
        throw r6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x004e, code lost:
        r5 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x004f, code lost:
        if (r4 != null) goto L_0x0051;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:?, code lost:
        r4.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x0055, code lost:
        r6 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x0056, code lost:
        r2.addSuppressed(r6);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x0059, code lost:
        throw r5;
     */
    private static String readFileString(Context context, String fileName) {
        StringBuilder builder = new StringBuilder((int) EXPECTED_BUFFER_SIZE);
        int totalSize = 0;
        try {
            InputStream inputStream = context.getAssets().open(fileName);
            BufferedReader bufferReader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            while (true) {
                String line = bufferReader.readLine();
                if (line != null) {
                    totalSize += line.length();
                    if (totalSize >= EXPECTED_BUFFER_SIZE) {
                        Log.e(TAG, "line size is larger than EXPECTED_BUFFER_SIZE!");
                        break;
                    }
                    builder.append(line);
                } else {
                    break;
                }
            }
            bufferReader.close();
            if (inputStream != null) {
                inputStream.close();
            }
        } catch (IOException e) {
            Log.e(TAG, "read file error!");
        }
        return builder.toString();
    }
}
