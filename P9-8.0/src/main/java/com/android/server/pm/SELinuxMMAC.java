package com.android.server.pm;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageParser.Package;
import android.os.Environment;
import android.util.Slog;
import android.util.Xml;
import com.android.server.am.HwBroadcastRadarUtil;
import com.android.server.pm.Policy.PolicyBuilder;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import libcore.io.IoUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public final class SELinuxMMAC {
    private static final boolean DEBUG_POLICY = false;
    private static final boolean DEBUG_POLICY_INSTALL = false;
    private static final boolean DEBUG_POLICY_ORDER = false;
    private static final File[] MAC_PERMISSIONS = new File[]{new File(Environment.getRootDirectory(), "/etc/selinux/plat_mac_permissions.xml"), new File(Environment.getVendorDirectory(), "/etc/selinux/nonplat_mac_permissions.xml")};
    private static final String PRIVILEGED_APP_STR = ":privapp";
    private static final String SANDBOX_V2_STR = ":v2";
    static final String TAG = "SELinuxMMAC";
    private static final String TARGETSDKVERSION_STR = ":targetSdkVersion=";
    private static List<Policy> sPolicies = new ArrayList();

    /* JADX WARNING: Removed duplicated region for block: B:50:0x00f9 A:{Splitter: B:3:0x0012, ExcHandler: java.lang.IllegalStateException (e java.lang.IllegalStateException)} */
    /* JADX WARNING: Removed duplicated region for block: B:50:0x00f9 A:{Splitter: B:3:0x0012, ExcHandler: java.lang.IllegalStateException (e java.lang.IllegalStateException)} */
    /* JADX WARNING: Removed duplicated region for block: B:15:0x0069 A:{Splitter: B:5:0x001b, ExcHandler: java.lang.IllegalStateException (e java.lang.IllegalStateException)} */
    /* JADX WARNING: Removed duplicated region for block: B:15:0x0069 A:{Splitter: B:5:0x001b, ExcHandler: java.lang.IllegalStateException (e java.lang.IllegalStateException)} */
    /* JADX WARNING: Missing block: B:15:0x0069, code:
            r0 = e;
     */
    /* JADX WARNING: Missing block: B:16:0x006a, code:
            r5 = r6;
     */
    /* JADX WARNING: Missing block: B:18:?, code:
            r8 = new java.lang.StringBuilder("Exception @");
            r8.append(r3.getPositionDescription());
            r8.append(" while parsing ");
            r8.append(MAC_PERMISSIONS[r1]);
            r8.append(":");
            r8.append(r0);
            android.util.Slog.w(TAG, r8.toString());
     */
    /* JADX WARNING: Missing block: B:19:0x009a, code:
            libcore.io.IoUtils.closeQuietly(r5);
     */
    /* JADX WARNING: Missing block: B:20:0x009d, code:
            return false;
     */
    /* JADX WARNING: Missing block: B:31:0x00ce, code:
            r9 = th;
     */
    /* JADX WARNING: Missing block: B:32:0x00cf, code:
            libcore.io.IoUtils.closeQuietly(r5);
     */
    /* JADX WARNING: Missing block: B:33:0x00d2, code:
            throw r9;
     */
    /* JADX WARNING: Missing block: B:50:0x00f9, code:
            r0 = e;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static boolean readInstallPolicy() {
        IOException ioe;
        List<Policy> policies = new ArrayList();
        AutoCloseable policyFile = null;
        XmlPullParser parser = Xml.newPullParser();
        int i = 0;
        while (i < MAC_PERMISSIONS.length) {
            try {
                FileReader policyFile2 = new FileReader(MAC_PERMISSIONS[i]);
                Object policyFile3;
                try {
                    Slog.d(TAG, "Using policy file " + MAC_PERMISSIONS[i]);
                    parser.setInput(policyFile2);
                    parser.nextTag();
                    parser.require(2, null, "policy");
                    while (parser.next() != 3) {
                        if (parser.getEventType() == 2) {
                            if (parser.getName().equals("signer")) {
                                policies.add(readSignerOrThrow(parser));
                            } else {
                                skip(parser);
                            }
                        }
                    }
                    IoUtils.closeQuietly(policyFile2);
                    i++;
                    policyFile3 = policyFile2;
                } catch (IllegalStateException e) {
                } catch (IOException e2) {
                    ioe = e2;
                    policyFile3 = policyFile2;
                } catch (Throwable th) {
                    Throwable th2 = th;
                    policyFile3 = policyFile2;
                }
            } catch (IllegalStateException e3) {
            } catch (IOException e4) {
                ioe = e4;
            }
        }
        PolicyComparator policySort = new PolicyComparator();
        Collections.sort(policies, policySort);
        if (policySort.foundDuplicate()) {
            Slog.w(TAG, "ERROR! Duplicate entries found parsing mac_permissions.xml files");
            return false;
        }
        synchronized (sPolicies) {
            sPolicies = policies;
        }
        return true;
        Slog.w(TAG, "Exception parsing " + MAC_PERMISSIONS[i], ioe);
        IoUtils.closeQuietly(policyFile3);
        return false;
    }

    private static Policy readSignerOrThrow(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(2, null, "signer");
        PolicyBuilder pb = new PolicyBuilder();
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
                } else if (HwBroadcastRadarUtil.KEY_PACKAGE.equals(tagName)) {
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

    private static void readPackageOrThrow(XmlPullParser parser, PolicyBuilder pb) throws IOException, XmlPullParserException {
        parser.require(2, null, HwBroadcastRadarUtil.KEY_PACKAGE);
        String pkgName = parser.getAttributeValue(null, "name");
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
        if (p.getEventType() != 2) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (p.next()) {
                case 2:
                    depth++;
                    break;
                case 3:
                    depth--;
                    break;
                default:
                    break;
            }
        }
    }

    public static void assignSeInfoValue(Package pkg) {
        ApplicationInfo applicationInfo;
        synchronized (sPolicies) {
            for (Policy policy : sPolicies) {
                String seInfo = policy.getMatchedSeInfo(pkg);
                if (seInfo != null) {
                    pkg.applicationInfo.seInfo = seInfo;
                    break;
                }
            }
        }
        if (pkg.applicationInfo.targetSandboxVersion == 2) {
            applicationInfo = pkg.applicationInfo;
            applicationInfo.seInfo += SANDBOX_V2_STR;
        }
        if (pkg.applicationInfo.isPrivilegedApp()) {
            applicationInfo = pkg.applicationInfo;
            applicationInfo.seInfo += PRIVILEGED_APP_STR;
        }
        applicationInfo = pkg.applicationInfo;
        applicationInfo.seInfo += TARGETSDKVERSION_STR + pkg.applicationInfo.targetSdkVersion;
    }
}
