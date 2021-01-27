package com.android.server.wm;

import java.util.HashSet;
import java.util.Set;

public class HwSnsVideoManager {
    private static Set<String> sDeferLaunchingActivities = new HashSet();
    private static Set<String> sInterceptActivities = new HashSet();

    static {
        sDeferLaunchingActivities.add("com.tencent.mm/.plugin.voip.ui.VideoActivity");
        sDeferLaunchingActivities.add("com.tencent.mobileqq/com.tencent.av.ui.VideoInviteFull");
        sDeferLaunchingActivities.add("com.tencent.tim/com.tencent.av.ui.VideoInviteFull");
        sDeferLaunchingActivities.add("com.tencent.mobileqq/com.tencent.av.ui.VChatActivity");
        sDeferLaunchingActivities.add("com.tencent.tim/com.tencent.av.ui.VChatActivity");
        sDeferLaunchingActivities.add("com.tencent.mobileqq/com.tencent.av.ui.VideoInviteActivity");
        sDeferLaunchingActivities.add("com.tencent.tim/com.tencent.av.ui.VideoInviteActivity");
        sDeferLaunchingActivities.add("com.tencent.mm/.plugin.multitalk.ui.MultiTalkMainUI");
        sInterceptActivities.add("com.tencent.mobileqq/com.tencent.av.ui.VideoInviteFull");
        sInterceptActivities.add("com.tencent.tim/com.tencent.av.ui.VideoInviteFull");
        sInterceptActivities.add("com.tencent.mobileqq/com.tencent.av.ui.VChatActivity");
        sInterceptActivities.add("com.tencent.tim/com.tencent.av.ui.VChatActivity");
        sInterceptActivities.add("com.tencent.mobileqq/com.tencent.av.ui.VideoInviteActivity");
        sInterceptActivities.add("com.tencent.tim/com.tencent.av.ui.VideoInviteActivity");
        sInterceptActivities.add("com.tencent.mm/.plugin.multitalk.ui.MultiTalkMainUI");
    }

    public static Set<String> getDeferLaunchingActivitys() {
        return sDeferLaunchingActivities;
    }

    public static boolean isInterceptActivity(String componentName) {
        return sInterceptActivities.contains(componentName);
    }
}
