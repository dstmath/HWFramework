package com.android.server.pm;

import android.content.pm.Signature;
import java.util.ArrayList;

public class BlackListInfo {
    public ArrayList<BlackListApp> mBlackList = new ArrayList();
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
            int N = signature != null ? signature.length : 0;
            if (N > 0) {
                Signature[] mSig = new Signature[N];
                for (int i = 0; i < N; i++) {
                    mSig[i] = new Signature(signature[i]);
                }
                this.mSignature = mSig;
            }
        }
    }
}
