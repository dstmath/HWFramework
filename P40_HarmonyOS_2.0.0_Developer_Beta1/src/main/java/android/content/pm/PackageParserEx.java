package android.content.pm;

import android.content.pm.PackageParser;
import android.os.Bundle;
import android.util.ArraySet;
import java.io.File;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Iterator;

public class PackageParserEx {
    public static final int PARSE_IS_SYSTEM_DIR = 16;
    private PackageParser packageParser = new PackageParser();

    public static class ActivityEx {
        private PackageParser.Activity mActivity;

        public ActivityEx(Object object) {
            this.mActivity = (PackageParser.Activity) object;
        }

        public void setNavigationHide(boolean isNavigationHide) {
            this.mActivity.info.navigationHide = isNavigationHide;
        }

        public Bundle getMetaData() {
            return this.mActivity.metaData;
        }

        public ActivityInfo getActivityInfo() {
            return this.mActivity.info;
        }

        public boolean isResizeable() {
            return this.mActivity.isResizeable();
        }
    }

    public static class PackageEx {
        private PackageParser.Package mPkg;
        public final ArrayList<ProviderEx> providers;
        public final String[] splitNames;

        public PackageEx(Object object) {
            if (object instanceof PackageParser.Package) {
                this.mPkg = (PackageParser.Package) object;
                PackageParser.Package r0 = this.mPkg;
                if (r0 != null) {
                    this.splitNames = r0.splitNames;
                    if (this.mPkg.providers != null) {
                        this.providers = new ArrayList<>();
                        Iterator<PackageParser.Provider> it = this.mPkg.providers.iterator();
                        while (it.hasNext()) {
                            this.providers.add(new ProviderEx(it.next()));
                        }
                        return;
                    }
                    this.providers = null;
                    return;
                }
            }
            this.splitNames = null;
            this.providers = null;
        }

        public Object getPackage() {
            return this.mPkg;
        }

        public ApplicationInfo getApplicationInfo() {
            PackageParser.Package r0 = this.mPkg;
            if (r0 == null) {
                return null;
            }
            return r0.applicationInfo;
        }

        public String getPackageName() {
            PackageParser.Package r0 = this.mPkg;
            if (r0 == null) {
                return null;
            }
            return r0.packageName;
        }

        public SigningDetailsEx getSigningDetailsEx() {
            if (this.mPkg == null) {
                return null;
            }
            SigningDetailsEx signingDetails = new SigningDetailsEx();
            signingDetails.setSigningDetails(this.mPkg.mSigningDetails);
            return signingDetails;
        }

        public void setSigningDetailsEx(SigningDetailsEx detailsEx) {
            this.mPkg.mSigningDetails = detailsEx.getSigningDetails();
        }

        public int getVersionCode() {
            PackageParser.Package r0 = this.mPkg;
            if (r0 == null) {
                return -1;
            }
            return r0.mVersionCode;
        }

        public boolean hasComponentClassName(String name) {
            PackageParser.Package r0 = this.mPkg;
            if (r0 == null) {
                return false;
            }
            return r0.hasComponentClassName(name);
        }

        public String getCodePath() {
            PackageParser.Package r0 = this.mPkg;
            if (r0 == null) {
                return null;
            }
            return r0.codePath;
        }

        public String getBaseCodePath() {
            PackageParser.Package r0 = this.mPkg;
            if (r0 == null) {
                return null;
            }
            return r0.baseCodePath;
        }

        public int[] getSplitPrivateFlags() {
            PackageParser.Package r0 = this.mPkg;
            if (r0 == null) {
                return null;
            }
            return r0.splitPrivateFlags;
        }

        public String[] getSplitCodePaths() {
            PackageParser.Package r0 = this.mPkg;
            if (r0 == null) {
                return null;
            }
            return r0.splitCodePaths;
        }

        public int[] getSplitVersionCodes() {
            PackageParser.Package r0 = this.mPkg;
            if (r0 == null) {
                return null;
            }
            return r0.splitVersionCodes;
        }

        public long getLongVersionCode() {
            PackageParser.Package r0 = this.mPkg;
            if (r0 == null) {
                return -1;
            }
            return r0.getLongVersionCode();
        }

        public int getBaseRevisionCode() {
            PackageParser.Package r0 = this.mPkg;
            if (r0 == null) {
                return -1;
            }
            return r0.baseRevisionCode;
        }

        public int[] getSplitRevisionCodes() {
            PackageParser.Package r0 = this.mPkg;
            if (r0 == null) {
                return null;
            }
            return r0.splitRevisionCodes;
        }

        public Bundle getAppMetaData() {
            PackageParser.Package r0 = this.mPkg;
            if (r0 == null) {
                return null;
            }
            return r0.mAppMetaData;
        }

        public boolean getPersistentApp() {
            PackageParser.Package r0 = this.mPkg;
            if (r0 == null) {
                return false;
            }
            return r0.mPersistentApp;
        }

        public final ArrayList<String> requestedPermissions() {
            PackageParser.Package r0 = this.mPkg;
            if (r0 == null) {
                return null;
            }
            return r0.requestedPermissions;
        }

        public ArrayList<ActivityEx> getActivities() {
            PackageParser.Package r0 = this.mPkg;
            if (r0 == null) {
                return null;
            }
            ArrayList<PackageParser.Activity> activityList = r0.activities;
            ArrayList<ActivityEx> activityExList = new ArrayList<>();
            Iterator<PackageParser.Activity> it = activityList.iterator();
            while (it.hasNext()) {
                activityExList.add(new ActivityEx(it.next()));
            }
            return activityExList;
        }

        public String getStaticSharedLibName() {
            PackageParser.Package r0 = this.mPkg;
            if (r0 == null) {
                return null;
            }
            return r0.staticSharedLibName;
        }

        public String getManifestPackageName() {
            PackageParser.Package r0 = this.mPkg;
            if (r0 == null) {
                return null;
            }
            return r0.manifestPackageName;
        }

        public ArrayList<String> getUsesLibraries() {
            PackageParser.Package r0 = this.mPkg;
            if (r0 == null) {
                return null;
            }
            return r0.usesLibraries;
        }

        public ArrayList<String> getUsesOptionalLibraries() {
            PackageParser.Package r0 = this.mPkg;
            if (r0 == null) {
                return null;
            }
            return r0.usesOptionalLibraries;
        }

        public String getVolumeUuid() {
            PackageParser.Package r0 = this.mPkg;
            if (r0 == null) {
                return null;
            }
            return r0.volumeUuid;
        }
    }

    public static class ProviderEx {
        private PackageParser.Provider mProvider;

        private ProviderEx(Object provider) {
            this.mProvider = (PackageParser.Provider) provider;
        }

        public ProviderInfo getProviderInfo() {
            return this.mProvider.info;
        }
    }

    public static class SigningDetailsEx {
        private PackageParser.SigningDetails mSigningDetails;

        public PackageParser.SigningDetails getSigningDetails() {
            return this.mSigningDetails;
        }

        public void setSigningDetails(PackageParser.SigningDetails signingDetails) {
            this.mSigningDetails = signingDetails;
        }

        public static SigningDetailsEx unknown() {
            SigningDetailsEx signingDetails = new SigningDetailsEx();
            signingDetails.setSigningDetails(PackageParser.SigningDetails.UNKNOWN);
            return signingDetails;
        }

        public boolean hasSignatures() {
            return this.mSigningDetails.hasSignatures();
        }

        public int getSignatureSchemeVersion() {
            return this.mSigningDetails.signatureSchemeVersion;
        }

        public ArraySet<PublicKey> getPublicKeys() {
            return this.mSigningDetails.publicKeys;
        }

        public static int getSignatureSchemeVersionJAR() {
            return 1;
        }

        public boolean signaturesMatchExactly(SigningDetailsEx other) {
            if (other == null) {
                return false;
            }
            return this.mSigningDetails.signaturesMatchExactly(other.getSigningDetails());
        }
    }

    public static class ApkLiteEx {
        private PackageParser.ApkLite mApkLite;

        public PackageParser.ApkLite getApkLite() {
            return this.mApkLite;
        }

        public void setApkLite(PackageParser.ApkLite apkLite) {
            this.mApkLite = apkLite;
        }

        public long getLongVersionCode() {
            return this.mApkLite.getLongVersionCode();
        }

        public String getPackageName() {
            return this.mApkLite.packageName;
        }

        public boolean isPlugin() {
            return this.mApkLite.isPlugin;
        }
    }

    public static class PackageLiteEx {
        private PackageParser.PackageLite mPackageLite;

        public PackageParser.PackageLite getPackageLite() {
            return this.mPackageLite;
        }

        public void setPackageLite(PackageParser.PackageLite packageLite) {
            this.mPackageLite = packageLite;
        }

        public String getBaseCodePath() {
            return this.mPackageLite.baseCodePath;
        }

        public String getCodePath() {
            return this.mPackageLite.codePath;
        }

        public String getPackageName() {
            return this.mPackageLite.packageName;
        }

        public String[] getSplitNames() {
            return this.mPackageLite.splitNames;
        }

        public int[] getSplitVersionCodes() {
            return this.mPackageLite.splitVersionCodes;
        }

        public int[] getSplitPrivateFlags() {
            return this.mPackageLite.splitPrivateFlags;
        }

        public String[] getSplitCodePaths() {
            return this.mPackageLite.splitCodePaths;
        }

        public int getVersionCode() {
            return this.mPackageLite.versionCode;
        }

        public final int getVersionCodeMajor() {
            return this.mPackageLite.versionCodeMajor;
        }

        public final int getBaseRevisionCode() {
            return this.mPackageLite.baseRevisionCode;
        }

        public final int[] getSplitRevisionCodes() {
            return this.mPackageLite.splitRevisionCodes;
        }
    }

    public static class PackageParserExceptionEx extends Exception {
        private PackageParser.PackageParserException mParserException;

        public PackageParserExceptionEx(int error, String detailMessage) {
            this.mParserException = new PackageParser.PackageParserException(error, detailMessage);
        }

        public PackageParserExceptionEx(int error, String detailMessage, Throwable throwable) {
            this.mParserException = new PackageParser.PackageParserException(error, detailMessage, throwable);
        }

        public PackageParserExceptionEx(PackageParser.PackageParserException parserException) {
            this.mParserException = parserException;
        }

        public boolean isMissBase() {
            return this.mParserException.isMissBase;
        }
    }

    public static void setCurrentEmuiSysImgVersion(int version) {
        PackageParser.setCurrentEmuiSysImgVersion(version);
    }

    public static ApkLiteEx parseApkLite(File apkFile, int flags) throws PackageParserExceptionEx {
        try {
            PackageParser.ApkLite apkLite = PackageParser.parseApkLite(apkFile, flags);
            ApkLiteEx liteEx = new ApkLiteEx();
            liteEx.setApkLite(apkLite);
            return liteEx;
        } catch (PackageParser.PackageParserException e) {
            throw new PackageParserExceptionEx(e);
        }
    }

    public static PackageLiteEx parsePackageLite(File packageFile, int flags) throws PackageParserExceptionEx {
        try {
            PackageParser.PackageLite packageLite = PackageParser.parsePackageLite(packageFile, flags);
            PackageLiteEx packageLiteEx = new PackageLiteEx();
            packageLiteEx.setPackageLite(packageLite);
            return packageLiteEx;
        } catch (PackageParser.PackageParserException e) {
            throw new PackageParserExceptionEx(e);
        }
    }

    public PackageEx parsePackage(File packageFile, int flags) throws PackageParserExceptionEx {
        try {
            return new PackageEx(this.packageParser.parsePackage(packageFile, flags));
        } catch (PackageParser.PackageParserException e) {
            throw new PackageParserExceptionEx(e);
        }
    }

    public PackageEx parsePackage(File packageFile, int flags, boolean useCaches, int hwFlags) throws PackageParserExceptionEx {
        try {
            return new PackageEx(this.packageParser.parsePackage(packageFile, flags, useCaches, hwFlags));
        } catch (PackageParser.PackageParserException e) {
            throw new PackageParserExceptionEx(e);
        }
    }

    public static void collectCertificates(PackageEx pkg, boolean skipVerify) throws PackageParserExceptionEx {
        try {
            Object obj = pkg.getPackage();
            if (obj instanceof PackageParser.Package) {
                PackageParser.collectCertificates((PackageParser.Package) obj, skipVerify);
            }
        } catch (PackageParser.PackageParserException e) {
            throw new PackageParserExceptionEx(e);
        }
    }
}
