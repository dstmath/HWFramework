package com.android.server.pm;

import android.content.IntentFilter;
import java.util.ArrayList;
import java.util.Iterator;

public class PreferredIntentResolverEx {
    private PreferredIntentResolver preferredIntentResolver;

    public PreferredIntentResolver getPreferredIntentResolver() {
        return this.preferredIntentResolver;
    }

    public void setPreferredIntentResolver(PreferredIntentResolver preferredIntentResolver2) {
        this.preferredIntentResolver = preferredIntentResolver2;
    }

    public ArrayList<PreferredActivityEx> findFilters(IntentFilter matching) {
        ArrayList<PreferredActivity> activityList;
        ArrayList<PreferredActivityEx> activityExList = new ArrayList<>();
        PreferredIntentResolver preferredIntentResolver2 = this.preferredIntentResolver;
        if (preferredIntentResolver2 == null || (activityList = preferredIntentResolver2.findFilters(matching)) == null) {
            return activityExList;
        }
        Iterator<PreferredActivity> it = activityList.iterator();
        while (it.hasNext()) {
            PreferredActivityEx paEx = new PreferredActivityEx();
            paEx.setPreferredActivity(it.next());
            activityExList.add(paEx);
        }
        return activityExList;
    }

    public void removeFilter(PreferredActivityEx f) {
        PreferredIntentResolver preferredIntentResolver2 = this.preferredIntentResolver;
        if (preferredIntentResolver2 != null && f != null) {
            preferredIntentResolver2.removeFilter(f.getPreferredActivity());
        }
    }
}
