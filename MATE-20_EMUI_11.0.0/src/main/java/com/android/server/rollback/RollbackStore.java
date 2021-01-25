package com.android.server.rollback;

import android.content.pm.VersionedPackage;
import android.content.rollback.PackageRollbackInfo;
import android.content.rollback.RollbackInfo;
import android.util.IntArray;
import android.util.Log;
import android.util.SparseLongArray;
import com.android.server.net.watchlist.WatchlistLoggingHandler;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.text.ParseException;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import libcore.io.IoUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/* access modifiers changed from: package-private */
public class RollbackStore {
    private static final String TAG = "RollbackManager";
    private final File mRollbackDataDir;

    RollbackStore(File rollbackDataDir) {
        this.mRollbackDataDir = rollbackDataDir;
    }

    /* access modifiers changed from: package-private */
    public List<RollbackData> loadAllRollbackData() {
        List<RollbackData> rollbacks = new ArrayList<>();
        this.mRollbackDataDir.mkdirs();
        File[] listFiles = this.mRollbackDataDir.listFiles();
        for (File rollbackDir : listFiles) {
            if (rollbackDir.isDirectory()) {
                try {
                    rollbacks.add(loadRollbackData(rollbackDir));
                } catch (IOException e) {
                    Log.e(TAG, "Unable to read rollback data at " + rollbackDir, e);
                    removeFile(rollbackDir);
                }
            }
        }
        return rollbacks;
    }

    private static IntArray convertToIntArray(JSONArray jsonArray) throws JSONException {
        if (jsonArray.length() == 0) {
            return new IntArray();
        }
        int[] ret = new int[jsonArray.length()];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = jsonArray.getInt(i);
        }
        return IntArray.wrap(ret);
    }

    private static JSONArray convertToJsonArray(IntArray intArray) {
        JSONArray jsonArray = new JSONArray();
        for (int i = 0; i < intArray.size(); i++) {
            jsonArray.put(intArray.get(i));
        }
        return jsonArray;
    }

    private static JSONArray convertToJsonArray(List<PackageRollbackInfo.RestoreInfo> list) throws JSONException {
        JSONArray jsonArray = new JSONArray();
        for (PackageRollbackInfo.RestoreInfo ri : list) {
            JSONObject jo = new JSONObject();
            jo.put("userId", ri.userId);
            jo.put("appId", ri.appId);
            jo.put("seInfo", ri.seInfo);
            jsonArray.put(jo);
        }
        return jsonArray;
    }

    private static ArrayList<PackageRollbackInfo.RestoreInfo> convertToRestoreInfoArray(JSONArray array) throws JSONException {
        ArrayList<PackageRollbackInfo.RestoreInfo> restoreInfos = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            JSONObject jo = array.getJSONObject(i);
            restoreInfos.add(new PackageRollbackInfo.RestoreInfo(jo.getInt("userId"), jo.getInt("appId"), jo.getString("seInfo")));
        }
        return restoreInfos;
    }

    private static JSONArray ceSnapshotInodesToJson(SparseLongArray ceSnapshotInodes) throws JSONException {
        JSONArray array = new JSONArray();
        for (int i = 0; i < ceSnapshotInodes.size(); i++) {
            JSONObject entryJson = new JSONObject();
            entryJson.put("userId", ceSnapshotInodes.keyAt(i));
            entryJson.put("ceSnapshotInode", ceSnapshotInodes.valueAt(i));
            array.put(entryJson);
        }
        return array;
    }

    private static SparseLongArray ceSnapshotInodesFromJson(JSONArray json) throws JSONException {
        SparseLongArray ceSnapshotInodes = new SparseLongArray(json.length());
        for (int i = 0; i < json.length(); i++) {
            JSONObject entry = json.getJSONObject(i);
            ceSnapshotInodes.append(entry.getInt("userId"), entry.getLong("ceSnapshotInode"));
        }
        return ceSnapshotInodes;
    }

    private static JSONObject rollbackInfoToJson(RollbackInfo rollback) throws JSONException {
        JSONObject json = new JSONObject();
        json.put("rollbackId", rollback.getRollbackId());
        json.put("packages", toJson(rollback.getPackages()));
        json.put("isStaged", rollback.isStaged());
        json.put("causePackages", versionedPackagesToJson(rollback.getCausePackages()));
        json.put("committedSessionId", rollback.getCommittedSessionId());
        return json;
    }

    private static RollbackInfo rollbackInfoFromJson(JSONObject json) throws JSONException {
        return new RollbackInfo(json.getInt("rollbackId"), packageRollbackInfosFromJson(json.getJSONArray("packages")), json.getBoolean("isStaged"), versionedPackagesFromJson(json.getJSONArray("causePackages")), json.getInt("committedSessionId"));
    }

    /* access modifiers changed from: package-private */
    public RollbackData createNonStagedRollback(int rollbackId) {
        return new RollbackData(rollbackId, new File(this.mRollbackDataDir, Integer.toString(rollbackId)), -1);
    }

    /* access modifiers changed from: package-private */
    public RollbackData createStagedRollback(int rollbackId, int stagedSessionId) {
        return new RollbackData(rollbackId, new File(this.mRollbackDataDir, Integer.toString(rollbackId)), stagedSessionId);
    }

    static void backupPackageCodePath(RollbackData data, String packageName, String codePath) throws IOException {
        File sourceFile = new File(codePath);
        File targetDir = new File(data.backupDir, packageName);
        targetDir.mkdirs();
        Files.copy(sourceFile.toPath(), new File(targetDir, sourceFile.getName()).toPath(), new CopyOption[0]);
    }

    static File[] getPackageCodePaths(RollbackData data, String packageName) {
        File[] files = new File(data.backupDir, packageName).listFiles();
        if (files == null || files.length == 0) {
            return null;
        }
        return files;
    }

    static void deletePackageCodePaths(RollbackData data) {
        for (PackageRollbackInfo info : data.info.getPackages()) {
            removeFile(new File(data.backupDir, info.getPackageName()));
        }
    }

    /* access modifiers changed from: package-private */
    public void saveRollbackData(RollbackData data) throws IOException {
        try {
            JSONObject dataJson = new JSONObject();
            dataJson.put("info", rollbackInfoToJson(data.info));
            dataJson.put(WatchlistLoggingHandler.WatchlistEventKeys.TIMESTAMP, data.timestamp.toString());
            dataJson.put("stagedSessionId", data.stagedSessionId);
            dataJson.put("state", RollbackData.rollbackStateToString(data.state));
            dataJson.put("apkSessionId", data.apkSessionId);
            dataJson.put("restoreUserDataInProgress", data.restoreUserDataInProgress);
            PrintWriter pw = new PrintWriter(new File(data.backupDir, "rollback.json"));
            pw.println(dataJson.toString());
            pw.close();
        } catch (JSONException e) {
            throw new IOException(e);
        }
    }

    /* access modifiers changed from: package-private */
    public void deleteRollbackData(RollbackData data) {
        removeFile(data.backupDir);
    }

    private static RollbackData loadRollbackData(File backupDir) throws IOException {
        try {
            JSONObject dataJson = new JSONObject(IoUtils.readFileAsString(new File(backupDir, "rollback.json").getAbsolutePath()));
            return new RollbackData(rollbackInfoFromJson(dataJson.getJSONObject("info")), backupDir, Instant.parse(dataJson.getString(WatchlistLoggingHandler.WatchlistEventKeys.TIMESTAMP)), dataJson.getInt("stagedSessionId"), RollbackData.rollbackStateFromString(dataJson.getString("state")), dataJson.getInt("apkSessionId"), dataJson.getBoolean("restoreUserDataInProgress"));
        } catch (ParseException | DateTimeParseException | JSONException e) {
            throw new IOException(e);
        }
    }

    private static JSONObject toJson(VersionedPackage pkg) throws JSONException {
        JSONObject json = new JSONObject();
        json.put("packageName", pkg.getPackageName());
        json.put("longVersionCode", pkg.getLongVersionCode());
        return json;
    }

    private static VersionedPackage versionedPackageFromJson(JSONObject json) throws JSONException {
        return new VersionedPackage(json.getString("packageName"), json.getLong("longVersionCode"));
    }

    private static JSONObject toJson(PackageRollbackInfo info) throws JSONException {
        JSONObject json = new JSONObject();
        json.put("versionRolledBackFrom", toJson(info.getVersionRolledBackFrom()));
        json.put("versionRolledBackTo", toJson(info.getVersionRolledBackTo()));
        IntArray pendingBackups = info.getPendingBackups();
        List<PackageRollbackInfo.RestoreInfo> pendingRestores = info.getPendingRestores();
        IntArray installedUsers = info.getInstalledUsers();
        json.put("pendingBackups", convertToJsonArray(pendingBackups));
        json.put("pendingRestores", convertToJsonArray(pendingRestores));
        json.put("isApex", info.isApex());
        json.put("installedUsers", convertToJsonArray(installedUsers));
        json.put("ceSnapshotInodes", ceSnapshotInodesToJson(info.getCeSnapshotInodes()));
        return json;
    }

    private static PackageRollbackInfo packageRollbackInfoFromJson(JSONObject json) throws JSONException {
        return new PackageRollbackInfo(versionedPackageFromJson(json.getJSONObject("versionRolledBackFrom")), versionedPackageFromJson(json.getJSONObject("versionRolledBackTo")), convertToIntArray(json.getJSONArray("pendingBackups")), convertToRestoreInfoArray(json.getJSONArray("pendingRestores")), json.getBoolean("isApex"), convertToIntArray(json.getJSONArray("installedUsers")), ceSnapshotInodesFromJson(json.getJSONArray("ceSnapshotInodes")));
    }

    private static JSONArray versionedPackagesToJson(List<VersionedPackage> packages) throws JSONException {
        JSONArray json = new JSONArray();
        for (VersionedPackage pkg : packages) {
            json.put(toJson(pkg));
        }
        return json;
    }

    private static List<VersionedPackage> versionedPackagesFromJson(JSONArray json) throws JSONException {
        List<VersionedPackage> packages = new ArrayList<>();
        for (int i = 0; i < json.length(); i++) {
            packages.add(versionedPackageFromJson(json.getJSONObject(i)));
        }
        return packages;
    }

    private static JSONArray toJson(List<PackageRollbackInfo> infos) throws JSONException {
        JSONArray json = new JSONArray();
        for (PackageRollbackInfo info : infos) {
            json.put(toJson(info));
        }
        return json;
    }

    private static List<PackageRollbackInfo> packageRollbackInfosFromJson(JSONArray json) throws JSONException {
        List<PackageRollbackInfo> infos = new ArrayList<>();
        for (int i = 0; i < json.length(); i++) {
            infos.add(packageRollbackInfoFromJson(json.getJSONObject(i)));
        }
        return infos;
    }

    private static void removeFile(File file) {
        if (file.isDirectory()) {
            for (File child : file.listFiles()) {
                removeFile(child);
            }
        }
        if (file.exists()) {
            file.delete();
        }
    }
}
