package huawei.android.security.fileprotect;

import android.text.TextUtils;
import android.util.Slog;
import com.huawei.uikit.effect.BuildConfig;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;

public class HwaaPackageInfo {
    private static final int INVALID_USERID = -1;
    private static final String POLICY_IS_SUBDIRS_FALSE = "false";
    private static final String POLICY_IS_SUBDIRS_TRUE = "true";
    private static final String POLICY_KEY_EXT = "ext";
    private static final String POLICY_KEY_PATH = "path";
    private static final String POLICY_KEY_SUBDIRS = "subdirs";
    private static final String TAG = "HwaaPackageInfo";
    private int mAppId;
    private String mPackageName;
    private String mProtectPolicys;
    private String mSharedUserId;

    private HwaaPackageInfo(int appId, String packageName, String shareUid, String protectPolicys) {
        this.mAppId = appId;
        this.mPackageName = packageName;
        this.mSharedUserId = shareUid;
        this.mProtectPolicys = protectPolicys;
    }

    public static class Builder {
        private static final String ALGORITHM_SHA256 = "SHA-256";
        private static final String ENCODING_UTF8 = "utf-8";
        private static final String ESCAPE_REGEX = "\\\\";
        private static final int HEX_MASK = 255;
        private static final int HEX_MULTIPLY = 2;
        private int mAppId = -1;
        private String mPackageName;
        private List<HwaaPolicy> mProtectPolicys = new ArrayList();
        private String mSharedUserId;

        public Builder setAppId(int appId) {
            this.mAppId = appId;
            return this;
        }

        public Builder setPackageName(String packageName) {
            if (!TextUtils.isEmpty(packageName)) {
                this.mPackageName = packageName;
                return this;
            }
            throw new IllegalArgumentException("Invalid packageName" + packageName);
        }

        public Builder setSharedUserId(String sharedUserId) {
            this.mSharedUserId = sharedUserId;
            return this;
        }

        public Builder addDefaultPolicy(String dataDir) {
            if (TextUtils.isEmpty(dataDir)) {
                return this;
            }
            this.mProtectPolicys.add(new HwaaPolicy(dataDir));
            return this;
        }

        public HwaaPackageInfo build() {
            return new HwaaPackageInfo(this.mAppId, getSHA256String(this.mPackageName), getSHA256String(this.mSharedUserId), generatePolicys(this.mProtectPolicys));
        }

        private String generatePolicys(List<HwaaPolicy> policys) {
            HwaaPolicy policy;
            if (policys == null || policys.isEmpty() || (policy = policys.get(0)) == null) {
                return null;
            }
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put(HwaaPackageInfo.POLICY_KEY_PATH, policy.mDataDir);
                jsonObject.put(HwaaPackageInfo.POLICY_KEY_EXT, policy.mFileExt);
                jsonObject.put(HwaaPackageInfo.POLICY_KEY_SUBDIRS, policy.mIsSubDirsIncluded ? HwaaPackageInfo.POLICY_IS_SUBDIRS_TRUE : HwaaPackageInfo.POLICY_IS_SUBDIRS_FALSE);
            } catch (JSONException e) {
                Slog.w(HwaaPackageInfo.TAG, "Json constuct falied!");
            }
            return jsonObject.toString().replaceAll(ESCAPE_REGEX, BuildConfig.FLAVOR);
        }

        private String getSHA256String(String input) {
            if (TextUtils.isEmpty(input)) {
                return null;
            }
            try {
                MessageDigest messageDigest = MessageDigest.getInstance(ALGORITHM_SHA256);
                messageDigest.update(input.getBytes(ENCODING_UTF8));
                return byte2Hex(messageDigest.digest());
            } catch (NoSuchAlgorithmException e) {
                Slog.e(HwaaPackageInfo.TAG, "NoSuchAlgorithm for getSHA");
                return null;
            } catch (UnsupportedEncodingException e2) {
                Slog.e(HwaaPackageInfo.TAG, "encoding not supported for utf-8");
                return null;
            }
        }

        private String byte2Hex(byte[] bytes) {
            if (bytes == null || bytes.length < 1) {
                return null;
            }
            StringBuilder result = new StringBuilder(bytes.length * 2);
            int length = bytes.length;
            for (int i = 0; i < length; i++) {
                result.append(String.format("%02x", Integer.valueOf(bytes[i] & 255)));
            }
            return result.toString();
        }
    }

    /* access modifiers changed from: private */
    public static class HwaaPolicy {
        private static final String DEFAULT_FILE_EXTS = ".db|.db-shm|.db-wal|.db-journal";
        private String mDataDir;
        private String mFileExt;
        private boolean mIsSubDirsIncluded;

        private HwaaPolicy(String dataDir) {
            this(dataDir, DEFAULT_FILE_EXTS, true);
        }

        private HwaaPolicy(String dataDir, String fileExt, boolean isSubDirsIncluded) {
            this.mDataDir = dataDir;
            this.mFileExt = fileExt;
            this.mIsSubDirsIncluded = isSubDirsIncluded;
            String str = this.mDataDir;
            if (str != null && !str.endsWith(File.separator)) {
                this.mDataDir += File.separator;
            }
        }
    }
}
