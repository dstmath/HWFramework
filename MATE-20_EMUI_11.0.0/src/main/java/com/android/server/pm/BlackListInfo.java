package com.android.server.pm;

import android.content.pm.Signature;
import android.text.TextUtils;
import java.util.ArrayList;
import java.util.Arrays;

public class BlackListInfo {
    private static final int BASE_HASHCODE = 17;
    private static final int HASHCODE_FACTOR = 31;
    private static final int INVALID_VERSION = -1;
    private ArrayList<BlackListApp> mBlackList = new ArrayList<>();
    private int mVersionCode = -1;

    /* access modifiers changed from: package-private */
    public ArrayList<BlackListApp> getBlacklistApps() {
        return this.mBlackList;
    }

    /* access modifiers changed from: package-private */
    public void setVersionCode(int versionCode) {
        this.mVersionCode = versionCode;
    }

    /* access modifiers changed from: package-private */
    public int getVersionCode() {
        return this.mVersionCode;
    }

    /* access modifiers changed from: package-private */
    public static class BlackListApp {
        private String mHashValue;
        private int mMaxVersionId;
        private int mMinVersionId;
        private String mPackageName;
        private Signature[] mSignatures;

        BlackListApp() {
        }

        /* access modifiers changed from: package-private */
        public void setPackageName(String pkgName) {
            this.mPackageName = pkgName;
        }

        /* access modifiers changed from: package-private */
        public String getPackageName() {
            return this.mPackageName;
        }

        /* access modifiers changed from: package-private */
        public void setHashValue(String hashValue) {
            this.mHashValue = hashValue;
        }

        /* access modifiers changed from: package-private */
        public String getHashValue() {
            return this.mHashValue;
        }

        /* access modifiers changed from: package-private */
        public void setSignatures(String[] strings) {
            int size = strings != null ? strings.length : 0;
            if (size > 0) {
                Signature[] signatures = new Signature[size];
                for (int i = 0; i < size; i++) {
                    signatures[i] = new Signature(strings[i]);
                }
                this.mSignatures = signatures;
            }
        }

        /* access modifiers changed from: package-private */
        public Signature[] getSignatures() {
            return this.mSignatures;
        }

        /* access modifiers changed from: package-private */
        public void setMinVersionId(int minVersionId) {
            this.mMinVersionId = minVersionId;
        }

        /* access modifiers changed from: package-private */
        public int getMinVersionId() {
            return this.mMinVersionId;
        }

        /* access modifiers changed from: package-private */
        public void setMaxVersionId(int maxVersionId) {
            this.mMaxVersionId = maxVersionId;
        }

        /* access modifiers changed from: package-private */
        public int getMaxVersionId() {
            return this.mMaxVersionId;
        }

        public boolean equals(Object object) {
            if (!(object instanceof BlackListApp)) {
                return false;
            }
            BlackListApp app = (BlackListApp) object;
            if (!TextUtils.equals(this.mPackageName, app.getPackageName()) || !TextUtils.equals(this.mHashValue, app.getHashValue()) || !Arrays.equals(this.mSignatures, app.getSignatures()) || this.mMinVersionId != app.getMinVersionId() || this.mMaxVersionId != app.getMaxVersionId()) {
                return false;
            }
            return true;
        }

        public int hashCode() {
            int i = 17 * 31;
            String str = this.mPackageName;
            int i2 = 0;
            int result = (i + (str == null ? 0 : str.hashCode())) * 31;
            String str2 = this.mHashValue;
            int result2 = (result + (str2 == null ? 0 : str2.hashCode())) * 31;
            Signature[] signatureArr = this.mSignatures;
            if (signatureArr != null) {
                i2 = Arrays.hashCode(signatureArr);
            }
            return ((((result2 + i2) * 31) + this.mMinVersionId) * 31) + this.mMaxVersionId;
        }
    }
}
