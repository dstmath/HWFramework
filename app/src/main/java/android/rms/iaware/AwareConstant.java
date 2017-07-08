package android.rms.iaware;

import android.net.Uri;
import android.net.wifi.WifiEnterpriseConfig;
import android.os.SystemProperties;

public class AwareConstant {
    public static final int BETA_UESR_TYPE = 3;
    public static final String CONFIG_FILES_PATH = "/data/system/iaware";
    public static final int CURRENT_USER_TYPE = 0;
    public static final int INVALID_TYPE_ID = -1;
    public static final int STATISTICS_TYPE_APPLICATION_STARTUP_TIME = 3;
    public static final int STATISTICS_TYPE_COLLECT_DATA = 1;
    public static final int STATISTICS_TYPE_EXECUTIVE_STRATEGY = 2;
    private static final String TAG = "AwareConstant";
    public static final int VER_MAJOR = 1;
    public static final int VER_MINOR = 0;

    public static class Database {
        public static final Uri ASSOCIATE_URI = null;
        public static final String AUTHORITY = "iaware.huawei.com";
        public static final Uri HABITPROTECTLIST_URI = null;
        public static final Uri PKGNAME_URI = null;
        public static final Uri PKGRECORD_URI = null;
        private static final String[] TABLES = null;
        public static final String UNKNOWN_MIME_TYPE = "application/octet-stream";
        public static final Uri USERDATA_URI = null;

        public interface AwareTables {
            public static final String ASSOCIATE = "Associate";
            public static final String HABITPROTECTLIST = "HabitProtectList";
            public static final String PKGNAME = "PkgName";
            public static final String PKGRECORD = "PkgRecord";
            public static final String USERDATA = "UserData";
        }

        public interface HwAssociate {
            public static final String DST_PKGNAME = "dstPkgName";
            public static final String SRC_PKGNAME = "srcPkgName";
            public static final String TRANSITION_TIMES = "transitionTimes";
            public static final String USER_ID = "userID";
        }

        public interface HwHabitProtectList {
            public static final String APPTYPE = "appType";
            public static final String AVGUSEDFREQUENCY = "avgUsedFrequency";
            public static final String DELETED = "deleted";
            public static final String DELETED_TIME = "deletedTime";
            public static final String PKGNAME = "appPkgName";
            public static final String RECENTUSED = "recentUsed";
            public static final String USER_ID = "userID";
            public static final String _ID = "_id";
        }

        public interface HwPkgName {
            public static final String DELETED = "deleted";
            public static final String DELETED_TIME = "deletedTime";
            public static final String PKGNAME = "appPkgName";
            public static final String TOTAL_USE_TIMES = "totalUseTimes";
            public static final String USER_ID = "userID";
        }

        public interface HwPkgRecord {
            public static final String FLAG = "flag";
            public static final String PKGNAME = "appPkgName";
            public static final String USER_ID = "userID";
        }

        public interface HwUserData {
            public static final String PKG_NAME = "appPkgName";
            public static final String TIME = "time";
            public static final String USER_ID = "userID";
            public static final String _ID = "_id";
        }

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.rms.iaware.AwareConstant.Database.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.rms.iaware.AwareConstant.Database.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.rms.iaware.AwareConstant.Database.<clinit>():void");
        }

        public static int getTableCount() {
            return TABLES.length;
        }

        public static String getTableNameByIdx(int idx) {
            if (idx < 0 || idx >= TABLES.length) {
                return "Unknown";
            }
            return TABLES[idx];
        }

        public static String makeUri(String table) {
            return "content://iaware.huawei.com/" + table;
        }

        public static String makeMimeType(String table, boolean haveRowId) {
            if (haveRowId) {
                return "vnd.android.cursor.item/" + table;
            }
            return "vnd.android.cursor.dir/" + table;
        }
    }

    public enum FeatureType {
        ;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.rms.iaware.AwareConstant.FeatureType.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.rms.iaware.AwareConstant.FeatureType.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.rms.iaware.AwareConstant.FeatureType.<clinit>():void");
        }

        public static FeatureType getFeatureType(int featureId) {
            FeatureType[] features = values();
            if (featureId < 0 || featureId >= features.length) {
                return FEATURE_INVALIDE_TYPE;
            }
            return features[featureId];
        }

        public static int getFeatureId(FeatureType type) {
            if (type == null) {
                return AwareConstant.INVALID_TYPE_ID;
            }
            return type.ordinal();
        }
    }

    public enum ResourceType {
        ;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.rms.iaware.AwareConstant.ResourceType.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.rms.iaware.AwareConstant.ResourceType.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.rms.iaware.AwareConstant.ResourceType.<clinit>():void");
        }

        public static ResourceType getResourceType(int resourceId) {
            ResourceType[] resources = values();
            if (resourceId < 0 || resourceId >= resources.length) {
                return RESOURCE_INVALIDE_TYPE;
            }
            return resources[resourceId];
        }

        public static int getReousrceId(ResourceType type) {
            if (type == null) {
                return AwareConstant.INVALID_TYPE_ID;
            }
            return type.ordinal();
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.rms.iaware.AwareConstant.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.rms.iaware.AwareConstant.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.rms.iaware.AwareConstant.<clinit>():void");
    }

    public AwareConstant() {
    }

    private static final String getExternalInfo() {
        return "rev";
    }

    private static final boolean checkConfigFileId(String fileId) {
        if (fileId.length() > 91) {
            return false;
        }
        return fileId.matches("[^\\s\\\\/:\\*\\?\\\"<>\\|]*[^\\s\\\\/:\\*\\?\\\"<>\\|\\.]$");
    }

    private static final String getDefaultConfigFileId() {
        return "iaware_config_1.0.xml";
    }

    public static final String generateConfigFileId() {
        StringBuilder builder = new StringBuilder();
        builder.append(SystemProperties.get("ro.product.model", "NXT-AL10"));
        builder.append("_");
        builder.append(SystemProperties.get("ro.product.locale.region", "CN"));
        builder.append("_iaware_config_");
        builder.append(VER_MAJOR).append(".").append(CURRENT_USER_TYPE);
        builder.append("_");
        builder.append(getExternalInfo());
        builder.append(".xml");
        AwareLog.d(TAG, "Original string: '" + builder.toString() + "'");
        String fileId = builder.toString().replace(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER, "-");
        if (!checkConfigFileId(fileId)) {
            AwareLog.w(TAG, "Illegal file id, use default");
            fileId = getDefaultConfigFileId();
        }
        AwareLog.d(TAG, "Final file id string: '" + fileId + "'");
        return fileId;
    }

    public static final String getCloudDownloadedFilePath() {
        return "/data/system/iaware/" + generateConfigFileId();
    }

    public static final String getConfigFilesPath() {
        return CONFIG_FILES_PATH;
    }
}
