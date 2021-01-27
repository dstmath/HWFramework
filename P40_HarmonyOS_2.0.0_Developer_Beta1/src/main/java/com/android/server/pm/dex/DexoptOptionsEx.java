package com.android.server.pm.dex;

public class DexoptOptionsEx {
    private DexoptOptions mDexoptOptions;

    public DexoptOptions getDexoptOptions() {
        return this.mDexoptOptions;
    }

    public void setDexoptOptions(DexoptOptions dexoptOptions) {
        this.mDexoptOptions = dexoptOptions;
    }
}
