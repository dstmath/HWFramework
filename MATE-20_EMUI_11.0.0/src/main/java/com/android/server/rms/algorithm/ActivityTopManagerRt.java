package com.android.server.rms.algorithm;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import java.util.ArrayList;

public class ActivityTopManagerRt {
    private static final Object LOCK = new Object();
    private static final String TAG = "ActivityTopManagerRT";
    private static final String TOP_ACTIVITY_SORTED = "topActivitySorted";
    private static ActivityTopManagerRt sActivityManager = null;
    private Context mContext = null;
    private final ArrayList<String> mSortedTopA = new ArrayList<>();

    private ActivityTopManagerRt(Context context) {
        if (context != null) {
            this.mContext = context;
        }
    }

    public static ActivityTopManagerRt getInstance(Context context) {
        ActivityTopManagerRt activityTopManagerRt;
        synchronized (LOCK) {
            if (sActivityManager == null) {
                sActivityManager = new ActivityTopManagerRt(context);
            }
            activityTopManagerRt = sActivityManager;
        }
        return activityTopManagerRt;
    }

    public static ActivityTopManagerRt obtainExistInstance() {
        ActivityTopManagerRt activityTopManagerRt;
        synchronized (LOCK) {
            activityTopManagerRt = sActivityManager;
        }
        return activityTopManagerRt;
    }

    public boolean isTopActivity(String activityName, int topLevel) {
        int index;
        synchronized (this.mSortedTopA) {
            if (this.mSortedTopA.isEmpty()) {
                return false;
            }
            index = this.mSortedTopA.indexOf(activityName);
        }
        if (index == -1 || index >= topLevel) {
            return false;
        }
        return true;
    }

    public ArrayList<String> getTopActivityDumpInfo() {
        ArrayList<String> topList = new ArrayList<>();
        synchronized (this.mSortedTopA) {
            topList.addAll(this.mSortedTopA);
        }
        return topList;
    }

    public void reportTopActData(Bundle bdl) {
        if (bdl != null) {
            synchronized (this.mSortedTopA) {
                this.mSortedTopA.clear();
                try {
                    this.mSortedTopA.addAll(bdl.getStringArrayList(TOP_ACTIVITY_SORTED));
                } catch (ArrayIndexOutOfBoundsException e) {
                    Log.e(TAG, "bundle stringArraylist index out of bounds!");
                }
            }
        }
    }
}
