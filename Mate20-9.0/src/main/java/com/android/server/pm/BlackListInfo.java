package com.android.server.pm;

import android.content.pm.Signature;
import java.util.ArrayList;

public class BlackListInfo {
    public ArrayList<BlackListApp> mBlackList = new ArrayList<>();
    public int mVersionCode = -1;

    static class BlackListApp {
        public String mHashValue;
        public int mMaxVersionId;
        public int mMinVersionId;
        public String mPackageName;
        public Signature[] mSignature;

        BlackListApp() {
        }

        public void setSignature(String[] signature) {
            int length = signature != null ? signature.length : 0;
            if (length > 0) {
                Signature[] mSig = new Signature[length];
                for (int i = 0; i < length; i++) {
                    mSig[i] = new Signature(signature[i]);
                }
                this.mSignature = mSig;
            }
        }
    }
}
