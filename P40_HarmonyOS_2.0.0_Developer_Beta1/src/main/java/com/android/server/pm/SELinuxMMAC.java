package com.android.server.pm;

import android.content.pm.PackageParser;
import android.os.Environment;
import android.util.Slog;
import android.util.Xml;
import com.android.server.pm.Policy;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import libcore.io.IoUtils;
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
        File productMacPermission = new File(Environment.getProductDirectory(), "/etc/selinux/product_mac_permissions.xml");
        if (productMacPermission.exists()) {
            sMacPermissions.add(productMacPermission);
        }
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

    public static boolean readInstallPolicy() {
        synchronized (sPolicies) {
            if (sPolicyRead) {
                return true;
            }
        }
        List<Policy> policies = new ArrayList<>();
        XmlPullParser parser = Xml.newPullParser();
        int count = sMacPermissions.size();
        FileReader policyFile = null;
        for (int i = 0; i < count; i++) {
            File macPermission = sMacPermissions.get(i);
            try {
                policyFile = new FileReader(macPermission);
                Slog.d(TAG, "Using policy file " + macPermission);
                parser.setInput(policyFile);
                parser.nextTag();
                parser.require(2, null, "policy");
                while (parser.next() != 3) {
                    if (parser.getEventType() == 2) {
                        String name = parser.getName();
                        char c = 65535;
                        if (name.hashCode() == -902467798 && name.equals("signer")) {
                            c = 0;
                        }
                        if (c != 0) {
                            skip(parser);
                        } else {
                            policies.add(readSignerOrThrow(parser));
                        }
                    }
                }
                IoUtils.closeQuietly(policyFile);
            } catch (IllegalArgumentException | IllegalStateException | XmlPullParserException ex) {
                Slog.w(TAG, "Exception @" + parser.getPositionDescription() + " while parsing " + macPermission + ":" + ex);
                IoUtils.closeQuietly(policyFile);
                return false;
            } catch (IOException ioe) {
                Slog.w(TAG, "Exception parsing " + macPermission, ioe);
                IoUtils.closeQuietly(policyFile);
                return false;
            } catch (Throwable th) {
                IoUtils.closeQuietly(policyFile);
                throw th;
            }
        }
        PolicyComparator policySort = new PolicyComparator();
        Collections.sort(policies, policySort);
        if (policySort.foundDuplicate()) {
            Slog.w(TAG, "ERROR! Duplicate entries found parsing mac_permissions.xml files");
            return false;
        }
        synchronized (sPolicies) {
            sPolicies.clear();
            sPolicies.addAll(policies);
            sPolicyRead = true;
        }
        return true;
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
                int next = p.next();
                if (next == 2) {
                    depth++;
                } else if (next == 3) {
                    depth--;
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
