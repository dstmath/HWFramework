package com.android.server.wifi;

import android.app.admin.IDevicePolicyManager.Stub;
import android.content.Context;
import android.os.Environment;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.security.KeyStore;
import android.text.TextUtils;
import android.util.Log;
import com.android.server.net.DelayedDiskWrite;
import com.android.server.net.DelayedDiskWrite.Writer;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class WifiCertManager {
    private static final String CONFIG_FILE = (Environment.getDataDirectory() + "/misc/wifi/affiliatedcerts.txt");
    private static final String SEP = "\n";
    private static final String TAG = "WifiCertManager";
    private final Set<String> mAffiliatedUserOnlyCerts;
    private final String mConfigFile;
    private final Context mContext;
    private final DelayedDiskWrite mWriter;

    WifiCertManager(Context context) {
        this(context, CONFIG_FILE);
    }

    WifiCertManager(Context context, String configFile) {
        this.mAffiliatedUserOnlyCerts = new HashSet();
        this.mWriter = new DelayedDiskWrite();
        this.mContext = context;
        this.mConfigFile = configFile;
        byte[] bytes = readConfigFile();
        if (bytes != null) {
            String[] keys = new String(bytes, StandardCharsets.UTF_8).split(SEP);
            for (String key : keys) {
                this.mAffiliatedUserOnlyCerts.add(key);
            }
            if (this.mAffiliatedUserOnlyCerts.retainAll(Arrays.asList(listClientCertsForAllUsers()))) {
                writeConfig();
            }
        }
    }

    public void hideCertFromUnaffiliatedUsers(String key) {
        if (this.mAffiliatedUserOnlyCerts.add("USRPKEY_" + key)) {
            writeConfig();
        }
    }

    public String[] listClientCertsForCurrentUser() {
        HashSet<String> results = new HashSet();
        String[] keys = listClientCertsForAllUsers();
        if (isAffiliatedUser()) {
            return keys;
        }
        for (String key : keys) {
            if (!this.mAffiliatedUserOnlyCerts.contains(key)) {
                results.add(key);
            }
        }
        return (String[]) results.toArray(new String[results.size()]);
    }

    private void writeConfig() {
        writeConfigFile(TextUtils.join(SEP, (String[]) this.mAffiliatedUserOnlyCerts.toArray(new String[this.mAffiliatedUserOnlyCerts.size()])).getBytes(StandardCharsets.UTF_8));
    }

    protected byte[] readConfigFile() {
        byte[] bytes = null;
        try {
            File file = new File(this.mConfigFile);
            long fileSize = file.exists() ? file.length() : 0;
            if (fileSize == 0 || fileSize >= 2147483647L) {
                return null;
            }
            bytes = new byte[((int) file.length())];
            new DataInputStream(new FileInputStream(file)).readFully(bytes);
            return bytes;
        } catch (IOException e) {
            Log.e(TAG, "readConfigFile: failed to read " + e, e);
        }
    }

    protected void writeConfigFile(final byte[] payload) {
        byte[] data = payload;
        this.mWriter.write(this.mConfigFile, new Writer() {
            public void onWriteCalled(DataOutputStream out) throws IOException {
                out.write(payload, 0, payload.length);
            }
        });
    }

    protected String[] listClientCertsForAllUsers() {
        return KeyStore.getInstance().list("USRPKEY_", UserHandle.myUserId());
    }

    protected boolean isAffiliatedUser() {
        boolean result = false;
        try {
            return Stub.asInterface(ServiceManager.getService("device_policy")).isAffiliatedUser();
        } catch (Exception e) {
            Log.e(TAG, "failed to check user affiliation", e);
            return result;
        }
    }
}
