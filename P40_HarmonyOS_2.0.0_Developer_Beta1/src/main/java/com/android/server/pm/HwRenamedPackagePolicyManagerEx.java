package com.android.server.pm;

import android.content.pm.PackageParser;
import android.content.pm.PackageParserEx;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class HwRenamedPackagePolicyManagerEx {
    public static Optional<HwRenamedPackagePolicyEx> generateRenamedPackagePolicyLocked(PackageParserEx.PackageEx pkg, PackageManagerServiceEx pms) {
        Optional<HwRenamedPackagePolicy> policy = HwRenamedPackagePolicyManager.generateRenamedPackagePolicyLocked((PackageParser.Package) pkg.getPackage(), pms.getPackageManagerSerivce());
        if (!policy.isPresent()) {
            return Optional.empty();
        }
        HwRenamedPackagePolicyEx policyEx = new HwRenamedPackagePolicyEx();
        policyEx.setmPackagePolicy(policy.get());
        return Optional.of(policyEx);
    }

    public static boolean addRenamedPackagePolicy(HwRenamedPackagePolicyEx renamedPackagePolicy) {
        return HwRenamedPackagePolicyManager.getInstance().addRenamedPackagePolicy(renamedPackagePolicy.getmPackagePolicy());
    }

    public static List<HwRenamedPackagePolicyEx> getRenamedPackagePolicy(int flags) {
        List<HwRenamedPackagePolicy> policyList = HwRenamedPackagePolicyManager.getInstance().getRenamedPackagePolicy(flags);
        List<HwRenamedPackagePolicyEx> policyExList = new ArrayList<>();
        for (HwRenamedPackagePolicy policy : policyList) {
            HwRenamedPackagePolicyEx policyEx = new HwRenamedPackagePolicyEx();
            policyEx.setmPackagePolicy(policy);
            policyExList.add(policyEx);
        }
        return policyExList;
    }

    public static boolean migrateDataForRenamedPackageLocked(PackageParserEx.PackageEx pkg, int userId, int flags, PackageManagerServiceEx pms) {
        return HwRenamedPackagePolicyManager.getInstance().migrateDataForRenamedPackageLocked((PackageParser.Package) pkg.getPackage(), userId, flags, pms.getPackageManagerSerivce());
    }

    public static Optional<HwRenamedPackagePolicyEx> getRenamedPackagePolicyByOriginalName(String originalPackageName) {
        Optional<HwRenamedPackagePolicy> policy = HwRenamedPackagePolicyManager.getInstance().getRenamedPackagePolicyByOriginalName(originalPackageName);
        if (!policy.isPresent()) {
            return Optional.empty();
        }
        HwRenamedPackagePolicyEx policyEx = new HwRenamedPackagePolicyEx();
        policyEx.setmPackagePolicy(policy.get());
        return Optional.of(policyEx);
    }

    public static Optional<HwRenamedPackagePolicyEx> getRenamedPackagePolicyByNewPackageName(String newPackageName) {
        Optional<HwRenamedPackagePolicy> policy = HwRenamedPackagePolicyManager.getInstance().getRenamedPackagePolicyByNewPackageName(newPackageName);
        if (!policy.isPresent()) {
            return Optional.empty();
        }
        HwRenamedPackagePolicyEx policyEx = new HwRenamedPackagePolicyEx();
        policyEx.setmPackagePolicy(policy.get());
        return Optional.of(policyEx);
    }
}
