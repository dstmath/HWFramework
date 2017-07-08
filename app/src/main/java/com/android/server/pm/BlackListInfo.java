package com.android.server.pm;

import android.content.pm.Signature;
import java.util.ArrayList;

public class BlackListInfo {
    public ArrayList<BlackListApp> mBlackList;
    public int mVersionCode;

    static class BlackListApp {
        public String mHashValue;
        public int mMaxVersionId;
        public int mMinVersionId;
        public String mPackageName;
        public Signature[] mSignature;

        BlackListApp() {
        }

        public void setSignature(String[] signature) {
            int N = 0;
            if (signature != null) {
                N = signature.length;
            }
            if (N > 0) {
                Signature[] mSig = new Signature[N];
                for (int i = 0; i < N; i++) {
                    mSig[i] = new Signature(signature[i]);
                }
                this.mSignature = mSig;
            }
        }
    }

    public BlackListInfo() {
        this.mBlackList = new ArrayList();
        this.mVersionCode = -1;
    }
}
