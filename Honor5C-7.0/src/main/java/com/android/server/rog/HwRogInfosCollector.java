package com.android.server.rog;

import android.content.Context;
import android.rog.AppRogInfo;
import android.text.TextUtils;
import android.util.Slog;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public class HwRogInfosCollector {
    private static final String TAG = "HwRogInfosCollector";
    private HwRogConfigParser mConfigParser;
    private IParseCallback mParseCallback;
    private boolean mParseFinished;
    private HashMap<String, AppRogInfo> mRogInfoList;

    /* renamed from: com.android.server.rog.HwRogInfosCollector.1 */
    class AnonymousClass1 implements Runnable {
        final /* synthetic */ Context val$context;
        final /* synthetic */ float val$rogScale;

        AnonymousClass1(float val$rogScale, Context val$context) {
            this.val$rogScale = val$rogScale;
            this.val$context = val$context;
        }

        public void run() {
            Slog.i(HwRogInfosCollector.TAG, "init->parse started,rogScale:" + this.val$rogScale);
            HwRogInfosCollector.this.mConfigParser.parseConfig(this.val$context, HwRogInfosCollector.this.mRogInfoList, this.val$rogScale);
            Slog.i(HwRogInfosCollector.TAG, "init->parse finished, total size:" + HwRogInfosCollector.this.mRogInfoList.size());
            synchronized (HwRogInfosCollector.this) {
                HwRogInfosCollector.this.mParseFinished = true;
            }
            if (HwRogInfosCollector.this.mParseCallback != null) {
                HwRogInfosCollector.this.mParseCallback.onParseFinished();
            }
        }
    }

    interface IParseCallback {
        void onParseFinished();
    }

    public HwRogInfosCollector(Context context, IParseCallback callback) {
        this.mRogInfoList = new HashMap();
        this.mParseFinished = false;
        this.mConfigParser = new HwRogConfigParser(context);
        this.mParseCallback = callback;
    }

    public void systemReady(Context context, float rogScale) {
        new Thread(new AnonymousClass1(rogScale, context)).start();
    }

    public AppRogInfo getAppRogInfo(String packageName) {
        if (!TextUtils.isEmpty(packageName)) {
            return (AppRogInfo) this.mRogInfoList.get(packageName);
        }
        Slog.w(TAG, "getAppRogInfo->package name is empty");
        return null;
    }

    public ArrayList<AppRogInfo> getAllAppRogInfos() {
        Collection<AppRogInfo> infos = this.mRogInfoList.values();
        ArrayList<AppRogInfo> result = new ArrayList();
        for (AppRogInfo rogInfo : infos) {
            result.add(rogInfo);
        }
        return result;
    }

    public boolean isInList(String packageName) {
        return this.mRogInfoList.containsKey(packageName);
    }

    public AppRogInfo updateAppRogInfo(Context context, AppRogInfo newInfo) {
        if (updateInfoInMemory(newInfo)) {
            saveToFile(context);
        }
        return newInfo;
    }

    private boolean updateInfoInMemory(AppRogInfo newInfo) {
        AppRogInfo rogInfo = (AppRogInfo) this.mRogInfoList.get(newInfo.mPackageName);
        if (rogInfo == null) {
            return false;
        }
        rogInfo.mRogMode = newInfo.mRogMode;
        return true;
    }

    public ArrayList<AppRogInfo> updateBatchAppRogInfos(Context context, ArrayList<AppRogInfo> newInfoList) {
        boolean changed = false;
        for (AppRogInfo newInfo : newInfoList) {
            if (updateInfoInMemory(newInfo)) {
                changed = true;
            }
        }
        if (changed) {
            saveToFile(context);
        }
        return newInfoList;
    }

    private void saveToFile(Context context) {
        this.mConfigParser.updateConfig(context, this.mRogInfoList.values());
    }

    public void setRogSwitchState(Context context, boolean open) {
        this.mConfigParser.setRogSwitchState(context, open);
    }

    public boolean getRogSwitchState(Context context) {
        return this.mConfigParser.getRogSwitchState(context);
    }

    public boolean isParseFinished() {
        boolean z;
        synchronized (this) {
            z = this.mParseFinished;
        }
        return z;
    }
}
