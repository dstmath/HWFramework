package ohos.abilityshell.utils;

import android.app.ActivityManager;
import android.content.ComponentName;
import ohos.aafwk.ability.AbilityMissionInfo;
import ohos.aafwk.ability.AbilityStackInfo;
import ohos.bundle.ElementName;

public class AbilityMissionConvertor {
    private static int ABILITY_INDEX = 1;
    private static String ABILITY_SUFFIX = "ShellActivity";
    private static int BUNDLE_INDEX = 0;
    private static int COMPONENT_NAME_LENGTH = 2;
    private static String SPLIT = "/";

    public static AbilityMissionInfo convertorRuningTaskInfo(ActivityManager.RunningTaskInfo runningTaskInfo) {
        return new AbilityMissionInfo(runningTaskInfo.id, runningTaskInfo.stackId, convertorComponentName(runningTaskInfo.topActivity), convertorComponentName(runningTaskInfo.baseActivity));
    }

    public static AbilityMissionInfo convertorRecentTaskInfo(ActivityManager.RecentTaskInfo recentTaskInfo) {
        return new AbilityMissionInfo(recentTaskInfo.id, recentTaskInfo.stackId, convertorComponentName(recentTaskInfo.topActivity), convertorComponentName(recentTaskInfo.baseActivity));
    }

    public static AbilityStackInfo convertorStackInfo(ActivityManager.StackInfo stackInfo) {
        AbilityStackInfo abilityStackInfo = new AbilityStackInfo();
        abilityStackInfo.setAbilityStackId(stackInfo.stackId);
        if (stackInfo.bounds != null) {
            abilityStackInfo.setAbilityStackBounds(stackInfo.bounds.left, stackInfo.bounds.top, stackInfo.bounds.right, stackInfo.bounds.bottom);
        }
        abilityStackInfo.settopBundleName(stackInfo.topActivity == null ? null : convertorComponentName(stackInfo.topActivity.getPackageName()));
        for (int i = 0; i < stackInfo.taskIds.length; i++) {
            abilityStackInfo.addAbilityMissionInfo(new AbilityMissionInfo(stackInfo.taskIds[i], stackInfo.stackId, convertorComponentName(stackInfo.taskNames[i]), null));
        }
        return abilityStackInfo;
    }

    private static ElementName convertorComponentName(ComponentName componentName) {
        if (componentName == null) {
            return null;
        }
        ElementName elementName = new ElementName();
        elementName.setBundleName(componentName.getPackageName());
        String className = componentName.getClassName();
        if (className.indexOf(ABILITY_SUFFIX) == -1) {
            return null;
        }
        elementName.setAbilityName(className.substring(0, className.length() - ABILITY_SUFFIX.length()));
        return elementName;
    }

    private static ElementName convertorComponentName(String str) {
        if (str == null || str.isEmpty()) {
            return null;
        }
        String[] split = str.split(SPLIT);
        if (split.length != COMPONENT_NAME_LENGTH) {
            return null;
        }
        ElementName elementName = new ElementName();
        elementName.setBundleName(split[BUNDLE_INDEX]);
        String str2 = split[ABILITY_INDEX];
        if (str2.indexOf(ABILITY_SUFFIX) == -1) {
            return null;
        }
        if (str2.startsWith(".")) {
            str2 = split[BUNDLE_INDEX] + str2;
        }
        elementName.setAbilityName(str2.substring(0, str2.length() - ABILITY_SUFFIX.length()));
        return elementName;
    }
}
