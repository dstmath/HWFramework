package com.android.server.pm;

import android.content.pm.PackageParser;
import android.os.Environment;
import com.android.server.pm.Policy;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public final class SELinuxMMAC {
    private static final boolean DEBUG_POLICY = false;
    private static final boolean DEBUG_POLICY_INSTALL = false;
    private static final boolean DEBUG_POLICY_ORDER = false;
    private static final String DEFAULT_SEINFO = "default";
    private static final String PRIVILEGED_APP_STR = ":privapp";
    private static final String SANDBOX_V2_STR = ":v2";
    static final String TAG = "SELinuxMMAC";
    private static final String TARGETSDKVERSION_STR = ":targetSdkVersion=";
    private static List<File> sMacPermissions = new ArrayList();
    private static List<Policy> sPolicies = new ArrayList();
    private static boolean sPolicyRead;

    static {
        sMacPermissions.add(new File(Environment.getRootDirectory(), "/etc/selinux/plat_mac_permissions.xml"));
        File vendorMacPermission = new File(Environment.getVendorDirectory(), "/etc/selinux/vendor_mac_permissions.xml");
        if (vendorMacPermission.exists()) {
            sMacPermissions.add(vendorMacPermission);
        } else {
            sMacPermissions.add(new File(Environment.getVendorDirectory(), "/etc/selinux/nonplat_mac_permissions.xml"));
        }
        File odmMacPermission = new File(Environment.getOdmDirectory(), "/etc/selinux/odm_mac_permissions.xml");
        if (odmMacPermission.exists()) {
            sMacPermissions.add(odmMacPermission);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:12:?, code lost:
        r6 = new java.io.FileReader(sMacPermissions.get(r0));
        android.util.Slog.d(TAG, "Using policy file " + r7);
        r3.setInput(r6);
        r3.nextTag();
        r3.require(2, null, "policy");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0058, code lost:
        if (r3.next() == 3) goto L_0x0088;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x005e, code lost:
        if (r3.getEventType() == 2) goto L_0x0061;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0061, code lost:
        r8 = r3.getName();
        r9 = 65535;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x006d, code lost:
        if (r8.hashCode() == -902467798) goto L_0x0070;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0077, code lost:
        if (r8.equals("signer") == false) goto L_0x007a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0079, code lost:
        r9 = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x007a, code lost:
        if (r9 == 0) goto L_0x0080;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x007c, code lost:
        skip(r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x0080, code lost:
        r1.add(readSignerOrThrow(r3));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0088, code lost:
        libcore.io.IoUtils.closeQuietly(r6);
        r0 = r0 + 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x008f, code lost:
        r2 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x0091, code lost:
        r2 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:?, code lost:
        android.util.Slog.w(TAG, "Exception parsing " + r7, r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x00a8, code lost:
        libcore.io.IoUtils.closeQuietly(r6);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x00ac, code lost:
        return false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x00ad, code lost:
        r2 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:?, code lost:
        android.util.Slog.w(TAG, "Exception @" + r3.getPositionDescription() + " while parsing " + r7 + ":" + r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x00d5, code lost:
        libcore.io.IoUtils.closeQuietly(r6);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:0x00d9, code lost:
        return false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:0x00da, code lost:
        libcore.io.IoUtils.closeQuietly(r6);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:41:0x00dd, code lost:
        throw r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:42:0x00de, code lost:
        r7 = new com.android.server.pm.PolicyComparator();
        java.util.Collections.sort(r1, r7);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:0x00eb, code lost:
        if (r7.foundDuplicate() == false) goto L_0x00f5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:44:0x00ed, code lost:
        android.util.Slog.w(TAG, "ERROR! Duplicate entries found parsing mac_permissions.xml files");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:45:0x00f4, code lost:
        return false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:46:0x00f5, code lost:
        r5 = sPolicies;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:47:0x00f7, code lost:
        monitor-enter(r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:49:?, code lost:
        sPolicies.clear();
        sPolicies.addAll(r1);
        sPolicyRead = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:50:0x0104, code lost:
        monitor-exit(r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:51:0x0105, code lost:
        return true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:8:0x000b, code lost:
        r1 = new java.util.ArrayList<>();
        r3 = android.util.Xml.newPullParser();
        r4 = sMacPermissions.size();
        r6 = null;
        r0 = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x001f, code lost:
        if (r0 >= r4) goto L_0x00de;
     */
    public static boolean readInstallPolicy() {
        synchronized (sPolicies) {
            if (sPolicyRead) {
                return true;
            }
        }
    }

    private static Policy readSignerOrThrow(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(2, null, "signer");
        Policy.PolicyBuilder pb = new Policy.PolicyBuilder();
        String cert = parser.getAttributeValue(null, "signature");
        if (cert != null) {
            pb.addSignature(cert);
        }
        while (parser.next() != 3) {
            if (parser.getEventType() == 2) {
                String tagName = parser.getName();
                if ("seinfo".equals(tagName)) {
                    pb.setGlobalSeinfoOrThrow(parser.getAttributeValue(null, "value"));
                    readSeinfo(parser);
                } else if ("package".equals(tagName)) {
                    readPackageOrThrow(parser, pb);
                } else if ("cert".equals(tagName)) {
                    pb.addSignature(parser.getAttributeValue(null, "signature"));
                    readCert(parser);
                } else {
                    skip(parser);
                }
            }
        }
        return pb.build();
    }

    private static void readPackageOrThrow(XmlPullParser parser, Policy.PolicyBuilder pb) throws IOException, XmlPullParserException {
        parser.require(2, null, "package");
        String pkgName = parser.getAttributeValue(null, Settings.ATTR_NAME);
        while (parser.next() != 3) {
            if (parser.getEventType() == 2) {
                if ("seinfo".equals(parser.getName())) {
                    pb.addInnerPackageMapOrThrow(pkgName, parser.getAttributeValue(null, "value"));
                    readSeinfo(parser);
                } else {
                    skip(parser);
                }
            }
        }
    }

    private static void readCert(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(2, null, "cert");
        parser.nextTag();
    }

    private static void readSeinfo(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(2, null, "seinfo");
        parser.nextTag();
    }

    private static void skip(XmlPullParser p) throws IOException, XmlPullParserException {
        if (p.getEventType() == 2) {
            int depth = 1;
            while (depth != 0) {
                switch (p.next()) {
                    case 2:
                        depth++;
                        break;
                    case 3:
                        depth--;
                        break;
                }
            }
            return;
        }
        throw new IllegalStateException();
    }

    public static String getSeInfo(PackageParser.Package pkg, boolean isPrivileged, int targetSandboxVersion, int targetSdkVersion) {
        String seInfo = null;
        synchronized (sPolicies) {
            if (sPolicyRead) {
                Iterator<Policy> it = sPolicies.iterator();
                while (true) {
                    if (!it.hasNext()) {
                        break;
                    }
                    seInfo = it.next().getMatchedSeInfo(pkg);
                    if (seInfo != null) {
                        break;
                    }
                }
            }
        }
        if (seInfo == null) {
            seInfo = "default";
        }
        if (targetSandboxVersion == 2) {
            seInfo = seInfo + SANDBOX_V2_STR;
        }
        if (isPrivileged) {
            seInfo = seInfo + PRIVILEGED_APP_STR;
        }
        return seInfo + TARGETSDKVERSION_STR + targetSdkVersion;
    }
}
