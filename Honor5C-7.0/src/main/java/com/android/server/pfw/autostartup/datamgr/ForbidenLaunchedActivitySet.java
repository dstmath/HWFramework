package com.android.server.pfw.autostartup.datamgr;

import java.util.HashSet;
import java.util.Set;

public class ForbidenLaunchedActivitySet {
    private static final String TAG = "ForbidenLaunchedActivitySet";
    private Set<String> mForbidenActivitySet;

    public ForbidenLaunchedActivitySet() {
        this.mForbidenActivitySet = new HashSet();
    }

    public void loadForbidenActivitySet() {
        this.mForbidenActivitySet.add("com.igexin.sdk.GActivity");
        this.mForbidenActivitySet.add("com.igexin.sdk.PushActivity");
    }

    public boolean checkActivityExist(String clsName) {
        return this.mForbidenActivitySet.contains(clsName);
    }
}
