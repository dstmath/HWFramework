package ohos.abilityshell.utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import ohos.aafwk.content.Intent;
import ohos.aafwk.content.IntentParams;
import ohos.abilityshell.AbilityShellData;
import ohos.appexecfwk.utils.AppLog;
import ohos.bundle.AbilityInfo;
import ohos.bundle.BundleInfo;
import ohos.bundle.ShellInfo;
import ohos.hiviewdfx.HiLogLabel;

public class AbilityShellConverterUtils {
    private static final String FORM_SERVICE_SHELL_SUFFIX = "ShellServiceForm";
    private static final String PAGE_SHELL_SUFFIX = "ShellActivity";
    private static final String PROVIDER_SHELL_SUFFIX = "ShellProvider";
    private static final String SERVICE_SHELL_SUFFIX = "ShellService";
    private static final int SHELL_ACTIVITY = 1;
    private static final HiLogLabel SHELL_LABEL = new HiLogLabel(3, 218108160, "AbilityShell");
    private static final int SHELL_SERVICE = 2;
    private static final String SHELL_TYPE = "shellType";

    private AbilityShellConverterUtils() {
    }

    public static ShellInfo convertToShellInfo(AbilityInfo abilityInfo) {
        String str;
        if (abilityInfo == null) {
            AppLog.e(SHELL_LABEL, "AbilityShellConverterUtils::convertToShellInfo param invalid", new Object[0]);
            return null;
        }
        AppLog.d(SHELL_LABEL, "AbilityShellConverterUtils::convertToShellInfo Ability package: %{private}s, class: %{private}s", abilityInfo.getBundleName(), abilityInfo.getClassName());
        ShellInfo shellInfo = new ShellInfo();
        int i = AnonymousClass1.$SwitchMap$ohos$bundle$AbilityInfo$AbilityType[abilityInfo.getType().ordinal()];
        if (i == 1) {
            str = abilityInfo.getClassName().concat("ShellActivity");
            shellInfo.setType(ShellInfo.ShellType.ACTIVITY);
        } else if (i == 2) {
            str = abilityInfo.getClassName().concat("ShellService");
            shellInfo.setType(ShellInfo.ShellType.SERVICE);
        } else if (i != 3) {
            AppLog.w(SHELL_LABEL, "AbilityShellConverterUtils::convertToShellInfo unknown type", new Object[0]);
            str = null;
        } else {
            str = abilityInfo.getClassName().concat("ShellProvider");
            shellInfo.setType(ShellInfo.ShellType.PROVIDER);
        }
        if (str == null) {
            AppLog.e(SHELL_LABEL, "AbilityShellConverterUtils::convertToShellInfo failed", new Object[0]);
            return null;
        }
        shellInfo.setPackageName(abilityInfo.getBundleName());
        shellInfo.setName(str);
        AppLog.d(SHELL_LABEL, "AbilityShellConverterUtils::convertToShellInfo Shell package: %{private}s, class: %{private}s", shellInfo.getPackageName(), shellInfo.getName());
        return shellInfo;
    }

    public static ShellInfo convertToShellInfoSupportDiffPkg(AbilityInfo abilityInfo, BundleInfo bundleInfo) {
        String str = null;
        if (bundleInfo == null || abilityInfo == null) {
            AppLog.e(SHELL_LABEL, "AbilityShellConverterUtils::convertToShellInfoDiffPkg param invalid", new Object[0]);
            return null;
        } else if (!bundleInfo.isDifferentName()) {
            return convertToShellInfo(abilityInfo);
        } else {
            ShellInfo shellInfo = new ShellInfo();
            int i = AnonymousClass1.$SwitchMap$ohos$bundle$AbilityInfo$AbilityType[abilityInfo.getType().ordinal()];
            if (i == 1) {
                str = abilityInfo.getOriginalClassName();
                shellInfo.setType(ShellInfo.ShellType.ACTIVITY);
            } else if (i == 2) {
                str = abilityInfo.getOriginalClassName();
                shellInfo.setType(ShellInfo.ShellType.SERVICE);
            } else if (i != 3) {
                AppLog.w(SHELL_LABEL, "AbilityShellConverterUtils::convertToShellInfo unknown type", new Object[0]);
            } else {
                str = abilityInfo.getOriginalClassName().concat("ShellProvider");
                shellInfo.setType(ShellInfo.ShellType.PROVIDER);
            }
            shellInfo.setPackageName(bundleInfo.getOriginalName());
            shellInfo.setName(str);
            return shellInfo;
        }
    }

    public static AbilityInfo convertToAbilityInfo(ShellInfo shellInfo) {
        String str;
        String str2;
        if (shellInfo == null) {
            AppLog.e(SHELL_LABEL, "AbilityShellConverterUtils::convertToAbilityInfo param invalid", new Object[0]);
            return null;
        }
        AbilityInfo abilityInfo = new AbilityInfo();
        int i = AnonymousClass1.$SwitchMap$ohos$bundle$ShellInfo$ShellType[shellInfo.getType().ordinal()];
        if (i == 1) {
            str = removeShellSuffix(shellInfo.getName(), "ShellActivity");
            abilityInfo.setType(AbilityInfo.AbilityType.PAGE);
        } else if (i == 2) {
            abilityInfo.setType(AbilityInfo.AbilityType.SERVICE);
            if (isFormShell(shellInfo)) {
                abilityInfo.setType(AbilityInfo.AbilityType.PAGE);
                str2 = FORM_SERVICE_SHELL_SUFFIX;
            } else {
                str2 = "ShellService";
            }
            str = removeShellSuffix(shellInfo.getName(), str2);
        } else if (i != 3) {
            AppLog.w(SHELL_LABEL, "AbilityShellConverterUtils::convertToAbilityInfo unknown type", new Object[0]);
            str = null;
        } else {
            str = removeShellSuffix(shellInfo.getName(), "ShellProvider");
            abilityInfo.setType(AbilityInfo.AbilityType.DATA);
        }
        if (str == null) {
            AppLog.e(SHELL_LABEL, "AbilityShellConverterUtils::convertToAbilityInfo failed", new Object[0]);
            return null;
        }
        abilityInfo.setBundleName(shellInfo.getPackageName());
        abilityInfo.setClassName(str);
        return abilityInfo;
    }

    /* access modifiers changed from: package-private */
    /* renamed from: ohos.abilityshell.utils.AbilityShellConverterUtils$1  reason: invalid class name */
    public static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$ohos$bundle$AbilityInfo$AbilityType = new int[AbilityInfo.AbilityType.values().length];
        static final /* synthetic */ int[] $SwitchMap$ohos$bundle$ShellInfo$ShellType = new int[ShellInfo.ShellType.values().length];

        static {
            try {
                $SwitchMap$ohos$bundle$ShellInfo$ShellType[ShellInfo.ShellType.ACTIVITY.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$ohos$bundle$ShellInfo$ShellType[ShellInfo.ShellType.SERVICE.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                $SwitchMap$ohos$bundle$ShellInfo$ShellType[ShellInfo.ShellType.PROVIDER.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
            try {
                $SwitchMap$ohos$bundle$AbilityInfo$AbilityType[AbilityInfo.AbilityType.PAGE.ordinal()] = 1;
            } catch (NoSuchFieldError unused4) {
            }
            try {
                $SwitchMap$ohos$bundle$AbilityInfo$AbilityType[AbilityInfo.AbilityType.SERVICE.ordinal()] = 2;
            } catch (NoSuchFieldError unused5) {
            }
            try {
                $SwitchMap$ohos$bundle$AbilityInfo$AbilityType[AbilityInfo.AbilityType.DATA.ordinal()] = 3;
            } catch (NoSuchFieldError unused6) {
            }
        }
    }

    public static ShellInfo convertToFormShellInfo(AbilityInfo abilityInfo, ShellInfo.ShellType shellType) {
        if (abilityInfo == null || abilityInfo.getType() != AbilityInfo.AbilityType.PAGE) {
            AppLog.e(SHELL_LABEL, "AbilityShellConverterUtils::convertToFormShellInfo info invalid", new Object[0]);
            return null;
        } else if (shellType != ShellInfo.ShellType.SERVICE) {
            AppLog.e(SHELL_LABEL, "AbilityShellConverterUtils::convertToFormShellInfo type invalid", new Object[0]);
            return null;
        } else {
            AppLog.d(SHELL_LABEL, "AbilityShellConverterUtils::convertToFormShellInfo Ability package: %{private}s, class: %{private}s", abilityInfo.getBundleName(), abilityInfo.getClassName());
            ShellInfo shellInfo = new ShellInfo();
            if (abilityInfo.getClassName() != null) {
                shellInfo.setName(abilityInfo.getClassName().concat(FORM_SERVICE_SHELL_SUFFIX));
            }
            shellInfo.setPackageName(abilityInfo.getBundleName());
            shellInfo.setType(shellType);
            AppLog.d(SHELL_LABEL, "AbilityShellConverterUtils::convertToFormShellInfo Shell package: %{private}s, class: %{private}s", shellInfo.getPackageName(), shellInfo.getName());
            return shellInfo;
        }
    }

    public static boolean isFormShell(ShellInfo shellInfo) {
        if (shellInfo == null || shellInfo.getType() != ShellInfo.ShellType.SERVICE) {
            AppLog.e(SHELL_LABEL, "AbilityShellConverterUtils::isFormShell param invalid", new Object[0]);
            return false;
        } else if (shellInfo.getName() == null || shellInfo.getName().lastIndexOf(FORM_SERVICE_SHELL_SUFFIX) == -1) {
            return false;
        } else {
            return true;
        }
    }

    public static AbilityShellData createAbilityShellData(Intent intent, boolean z) {
        if (intent == null || intent.getElement() == null) {
            AppLog.e("AndroidUtils::createAbilityShellData intent or element is null!", new Object[0]);
            return null;
        }
        String bundleName = intent.getElement().getBundleName();
        String abilityName = intent.getElement().getAbilityName();
        AbilityInfo abilityInfo = new AbilityInfo();
        abilityInfo.setBundleName(bundleName);
        abilityInfo.setClassName(abilityName);
        ShellInfo shellInfo = new ShellInfo();
        shellInfo.setPackageName(bundleName);
        shellInfo.setName(abilityName);
        IntentParams params = intent.getParams();
        if (params != null && params.hasParam(SHELL_TYPE)) {
            Object param = params.getParam(SHELL_TYPE);
            if (param instanceof Integer) {
                int intValue = ((Integer) param).intValue();
                if (intValue == 1) {
                    shellInfo.setType(ShellInfo.ShellType.ACTIVITY);
                } else if (intValue != 2) {
                    AppLog.w("AndroidUtils::createAbilityShellData::onTransact unknown code", new Object[0]);
                } else {
                    shellInfo.setType(ShellInfo.ShellType.SERVICE);
                }
            }
        }
        return new AbilityShellData(z, abilityInfo, shellInfo);
    }

    public static boolean isAndroidComponent(Context context, Intent intent) {
        if (!(context == null || intent == null || (intent.getFlags() & 16) == 0)) {
            Optional<android.content.Intent> createAndroidIntent = IntentConverter.createAndroidIntent(intent, null);
            if (!createAndroidIntent.isPresent()) {
                AppLog.e("AbilityShellConverterUtils::isAndroidComponent createAndroidIntent failed", new Object[0]);
                return false;
            }
            PackageManager packageManager = context.getPackageManager();
            if (packageManager == null) {
                AppLog.e("AbilityShellConverterUtils::isAndroidComponent packageManager is null", new Object[0]);
                return false;
            } else if (!packageManager.queryIntentActivities(createAndroidIntent.get(), 0).isEmpty()) {
                addIntentType(intent, 1);
                return true;
            } else if (!packageManager.queryIntentServices(createAndroidIntent.get(), 0).isEmpty()) {
                addIntentType(intent, 2);
                return true;
            }
        }
        return false;
    }

    public static ResolveInfo getAndroidComponent(Context context, Intent intent) {
        if (!(context == null || intent == null || (intent.getFlags() & 16) == 0)) {
            Optional<android.content.Intent> createAndroidIntent = IntentConverter.createAndroidIntent(intent, null);
            if (!createAndroidIntent.isPresent()) {
                AppLog.e("AbilityShellConverterUtils::getAndroidComponent createAndroidIntent failed", new Object[0]);
                return null;
            }
            PackageManager packageManager = context.getPackageManager();
            if (packageManager == null) {
                AppLog.e("AbilityShellConverterUtils::getAndroidComponent packageManager is null", new Object[0]);
                return null;
            }
            List<ResolveInfo> queryIntentActivities = packageManager.queryIntentActivities(createAndroidIntent.get(), 0);
            if (!queryIntentActivities.isEmpty()) {
                addIntentType(intent, 1);
                return queryIntentActivities.get(0);
            }
            List<ResolveInfo> queryIntentServices = packageManager.queryIntentServices(createAndroidIntent.get(), 0);
            if (!queryIntentServices.isEmpty()) {
                addIntentType(intent, 2);
                return queryIntentServices.get(0);
            }
        }
        return null;
    }

    public static AbilityShellData createAbilityShellDataByResolveInfo(ResolveInfo resolveInfo, boolean z) {
        String str;
        if (resolveInfo == null) {
            AppLog.e("AbilityShellConverterUtils::createAbilityShellDataByResolveInfo info is null!", new Object[0]);
            return null;
        }
        ShellInfo.ShellType shellType = ShellInfo.ShellType.UNKNOWN;
        String str2 = "";
        if (resolveInfo.activityInfo != null) {
            str2 = resolveInfo.activityInfo.packageName;
            str = resolveInfo.activityInfo.name;
            shellType = ShellInfo.ShellType.ACTIVITY;
        } else {
            str = str2;
        }
        if (resolveInfo.serviceInfo != null) {
            str2 = resolveInfo.serviceInfo.packageName;
            str = resolveInfo.serviceInfo.name;
            shellType = ShellInfo.ShellType.SERVICE;
        }
        if (shellType == ShellInfo.ShellType.UNKNOWN) {
            AppLog.w("AbilityShellConverterUtils::createAbilityShellDataByResolveInfo unknown type", new Object[0]);
        }
        AbilityInfo abilityInfo = new AbilityInfo();
        abilityInfo.setBundleName(str2);
        abilityInfo.setClassName(str);
        ShellInfo shellInfo = new ShellInfo();
        shellInfo.setPackageName(str2);
        shellInfo.setName(str);
        shellInfo.setType(shellType);
        return new AbilityShellData(z, abilityInfo, shellInfo);
    }

    public static List<AbilityShellData> getAndroidShellDatas(Context context, Intent intent) {
        PackageManager packageManager = context.getPackageManager();
        if (packageManager == null) {
            return Collections.emptyList();
        }
        Optional<android.content.Intent> createAndroidIntent = IntentConverter.createAndroidIntent(intent, null);
        if (!createAndroidIntent.isPresent()) {
            return Collections.emptyList();
        }
        List<ResolveInfo> queryIntentActivities = packageManager.queryIntentActivities(createAndroidIntent.get(), 65536);
        if (queryIntentActivities.isEmpty()) {
            return Collections.emptyList();
        }
        addIntentType(intent, 1);
        ArrayList arrayList = new ArrayList();
        for (ResolveInfo resolveInfo : queryIntentActivities) {
            arrayList.add(convertToAbilityShellData(packageManager, resolveInfo, 1));
        }
        return arrayList;
    }

    public static String convertToHarmonyClassName(String str) {
        return removeShellSuffix(str, "ShellActivity");
    }

    private static String removeShellSuffix(String str, String str2) {
        if (str == null) {
            AppLog.e(SHELL_LABEL, "AbilityShellConverterUtils::removeShellSuffix parameter name is null", new Object[0]);
            return null;
        }
        int lastIndexOf = str.lastIndexOf(str2);
        if (lastIndexOf != -1) {
            return str.substring(0, lastIndexOf);
        }
        AppLog.e(SHELL_LABEL, "AbilityShellConverterUtils::removeShellSuffix %{private}s not contain %{private}s", str, str2);
        return null;
    }

    private static AbilityShellData convertToAbilityShellData(PackageManager packageManager, ResolveInfo resolveInfo, int i) {
        if (resolveInfo == null) {
            return new AbilityShellData(true, new AbilityInfo(), new ShellInfo());
        }
        String str = resolveInfo.activityInfo.packageName;
        String str2 = resolveInfo.activityInfo.name;
        CharSequence loadLabel = resolveInfo.loadLabel(packageManager);
        String charSequence = loadLabel != null ? loadLabel.toString() : "";
        AbilityInfo abilityInfo = new AbilityInfo();
        abilityInfo.setBundleName(str);
        abilityInfo.setClassName(str2);
        abilityInfo.label = charSequence;
        ShellInfo shellInfo = new ShellInfo();
        shellInfo.setPackageName(str);
        shellInfo.setName(str2);
        if (i != 1) {
            AppLog.w("AndroidUtils::convertToAbilityShellData::type not supported", new Object[0]);
        } else {
            shellInfo.setType(ShellInfo.ShellType.ACTIVITY);
        }
        return new AbilityShellData(true, abilityInfo, shellInfo);
    }

    private static void addIntentType(Intent intent, int i) {
        IntentParams params = intent.getParams();
        if (params == null) {
            params = new IntentParams();
        }
        params.setParam(SHELL_TYPE, Integer.valueOf(i));
        intent.setParams(params);
    }
}
