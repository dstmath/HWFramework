package com.android.server.pm;

import android.content.Context;
import android.content.pm.PackageParser;
import android.content.pm.PackageParserEx;

public class GunstallUtilEx {
    public static void initPackageManagerInner(PackageManagerServiceEx pms, Context context) {
        GunstallUtil.getInstance().initPackageManagerInner(pms.getPackageManagerSerivce(), context);
    }

    public static HwGunstallSwitchState updateGunstallState() {
        return GunstallUtil.getInstance().updateGunstallState();
    }

    public static boolean forbidGMSUpgrade(PackageParserEx.PackageEx pkg, PackageParserEx.PackageEx oldPackage, int callingSessionUid, HwGunstallSwitchState hwGSwitchState) {
        return GunstallUtil.getInstance().forbidGMSUpgrade((PackageParser.Package) pkg.getPackage(), (PackageParser.Package) oldPackage.getPackage(), callingSessionUid, hwGSwitchState);
    }
}
