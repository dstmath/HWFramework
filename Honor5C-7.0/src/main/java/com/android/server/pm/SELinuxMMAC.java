package com.android.server.pm;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageParser.Package;
import android.system.ErrnoException;
import android.system.Os;
import android.system.OsConstants;
import android.util.Slog;
import android.util.Xml;
import com.android.server.am.HwBroadcastRadarUtil;
import com.android.server.pm.Policy.PolicyBuilder;
import com.android.server.wm.WindowManagerService.H;
import com.android.server.wm.WindowState;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import libcore.io.IoUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public final class SELinuxMMAC {
    private static final String AUTOPLAY_APP_STR = ":autoplayapp";
    private static final boolean DEBUG_POLICY = false;
    private static final boolean DEBUG_POLICY_INSTALL = false;
    private static final boolean DEBUG_POLICY_ORDER = false;
    private static final File MAC_PERMISSIONS = null;
    private static final String PRIVILEGED_APP_STR = ":privapp";
    private static final File SEAPP_CONTEXTS = null;
    private static final byte[] SEAPP_CONTEXTS_HASH = null;
    static final String TAG = "SELinuxMMAC";
    private static final File VERSION_FILE = null;
    private static final String XATTR_SEAPP_HASH = "user.seapp_hash";
    private static List<Policy> sPolicies;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.pm.SELinuxMMAC.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.pm.SELinuxMMAC.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.pm.SELinuxMMAC.<clinit>():void");
    }

    public static boolean readInstallPolicy() {
        Exception ex;
        IOException ioe;
        Throwable th;
        List<Policy> policies = new ArrayList();
        AutoCloseable autoCloseable = null;
        XmlPullParser parser = Xml.newPullParser();
        try {
            FileReader policyFile = new FileReader(MAC_PERMISSIONS);
            try {
                Slog.d(TAG, "Using policy file " + MAC_PERMISSIONS);
                parser.setInput(policyFile);
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
                IoUtils.closeQuietly(policyFile);
                PolicyComparator policySort = new PolicyComparator();
                Collections.sort(policies, policySort);
                if (policySort.foundDuplicate()) {
                    Slog.w(TAG, "ERROR! Duplicate entries found parsing " + MAC_PERMISSIONS);
                    return DEBUG_POLICY_ORDER;
                }
                synchronized (sPolicies) {
                    sPolicies = policies;
                }
                return true;
            } catch (IllegalStateException e) {
                ex = e;
                autoCloseable = policyFile;
            } catch (IOException e2) {
                ioe = e2;
                autoCloseable = policyFile;
            } catch (Throwable th2) {
                th = th2;
                Object policyFile2 = policyFile;
            }
        } catch (IllegalStateException e3) {
            ex = e3;
            try {
                StringBuilder sb = new StringBuilder("Exception @");
                sb.append(parser.getPositionDescription());
                sb.append(" while parsing ");
                sb.append(MAC_PERMISSIONS);
                sb.append(":");
                sb.append(ex);
                Slog.w(TAG, sb.toString());
                IoUtils.closeQuietly(autoCloseable);
                return DEBUG_POLICY_ORDER;
            } catch (Throwable th3) {
                th = th3;
                IoUtils.closeQuietly(autoCloseable);
                throw th;
            }
        } catch (IOException e4) {
            ioe = e4;
            Slog.w(TAG, "Exception parsing " + MAC_PERMISSIONS, ioe);
            IoUtils.closeQuietly(autoCloseable);
            return DEBUG_POLICY_ORDER;
        }
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
                case WindowState.LOW_RESOLUTION_COMPOSITION_ON /*2*/:
                    depth++;
                    break;
                case H.REPORT_LOSING_FOCUS /*3*/:
                    depth--;
                    break;
                default:
                    break;
            }
        }
    }

    public static void assignSeinfoValue(Package pkg) {
        synchronized (sPolicies) {
            for (Policy policy : sPolicies) {
                String seinfo = policy.getMatchedSeinfo(pkg);
                if (seinfo != null) {
                    pkg.applicationInfo.seinfo = seinfo;
                    break;
                }
            }
        }
        if (pkg.applicationInfo.isAutoPlayApp()) {
            ApplicationInfo applicationInfo = pkg.applicationInfo;
            applicationInfo.seinfo += AUTOPLAY_APP_STR;
        }
        if (pkg.applicationInfo.isPrivilegedApp()) {
            applicationInfo = pkg.applicationInfo;
            applicationInfo.seinfo += PRIVILEGED_APP_STR;
        }
    }

    public static boolean isRestoreconNeeded(File file) {
        try {
            byte[] buf = new byte[20];
            if (Os.getxattr(file.getAbsolutePath(), XATTR_SEAPP_HASH, buf) == 20 && Arrays.equals(SEAPP_CONTEXTS_HASH, buf)) {
                return DEBUG_POLICY_ORDER;
            }
        } catch (ErrnoException e) {
            if (e.errno != OsConstants.ENODATA) {
                Slog.e(TAG, "Failed to read seapp hash for " + file, e);
            }
        }
        return true;
    }

    public static void setRestoreconDone(File file) {
        try {
            Os.setxattr(file.getAbsolutePath(), XATTR_SEAPP_HASH, SEAPP_CONTEXTS_HASH, 0);
        } catch (ErrnoException e) {
            Slog.e(TAG, "Failed to persist seapp hash in " + file, e);
        }
    }

    private static byte[] returnHash(File file) {
        try {
            return MessageDigest.getInstance("SHA-1").digest(IoUtils.readFileAsByteArray(file.getAbsolutePath()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
