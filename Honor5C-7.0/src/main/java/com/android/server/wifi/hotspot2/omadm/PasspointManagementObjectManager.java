package com.android.server.wifi.hotspot2.omadm;

import android.net.wifi.PasspointManagementObjectDefinition;
import android.util.Base64;
import android.util.Log;
import com.android.server.wifi.IMSIParameter;
import com.android.server.wifi.anqp.eap.EAP;
import com.android.server.wifi.anqp.eap.EAP.AuthInfoID;
import com.android.server.wifi.anqp.eap.EAP.EAPMethodID;
import com.android.server.wifi.anqp.eap.EAPMethod;
import com.android.server.wifi.anqp.eap.ExpandedEAPMethod;
import com.android.server.wifi.anqp.eap.InnerAuthEAP;
import com.android.server.wifi.anqp.eap.NonEAPInnerAuth;
import com.android.server.wifi.hotspot2.Utils;
import com.android.server.wifi.hotspot2.pps.Credential;
import com.android.server.wifi.hotspot2.pps.HomeSP;
import com.android.server.wifi.hotspot2.pps.Policy;
import com.android.server.wifi.hotspot2.pps.SubscriptionParameters;
import com.android.server.wifi.hotspot2.pps.UpdateInfo;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.xml.sax.SAXException;

public class PasspointManagementObjectManager {
    private static final DateFormat DTFormat = null;
    private static final List<String> FQDNPath = null;
    public static final long IntervalFactor = 60000;
    public static final String TAG_AAAServerTrustRoot = "AAAServerTrustRoot";
    public static final String TAG_AbleToShare = "AbleToShare";
    public static final String TAG_CertSHA256Fingerprint = "CertSHA256Fingerprint";
    public static final String TAG_CertURL = "CertURL";
    public static final String TAG_CertificateType = "CertificateType";
    public static final String TAG_CheckAAAServerCertStatus = "CheckAAAServerCertStatus";
    public static final String TAG_Country = "Country";
    public static final String TAG_CreationDate = "CreationDate";
    public static final String TAG_Credential = "Credential";
    public static final String TAG_CredentialPriority = "CredentialPriority";
    public static final String TAG_DLBandwidth = "DLBandwidth";
    public static final String TAG_DataLimit = "DataLimit";
    public static final String TAG_DigitalCertificate = "DigitalCertificate";
    public static final String TAG_EAPMethod = "EAPMethod";
    public static final String TAG_EAPType = "EAPType";
    public static final String TAG_ExpirationDate = "ExpirationDate";
    public static final String TAG_Extension = "Extension";
    public static final String TAG_FQDN = "FQDN";
    public static final String TAG_FQDN_Match = "FQDN_Match";
    public static final String TAG_FriendlyName = "FriendlyName";
    public static final String TAG_HESSID = "HESSID";
    public static final String TAG_HomeOI = "HomeOI";
    public static final String TAG_HomeOIList = "HomeOIList";
    public static final String TAG_HomeOIRequired = "HomeOIRequired";
    public static final String TAG_HomeSP = "HomeSP";
    public static final String TAG_IMSI = "IMSI";
    public static final String TAG_IPProtocol = "IPProtocol";
    public static final String TAG_IconURL = "IconURL";
    public static final String TAG_InnerEAPType = "InnerEAPType";
    public static final String TAG_InnerMethod = "InnerMethod";
    public static final String TAG_InnerVendorID = "InnerVendorID";
    public static final String TAG_InnerVendorType = "InnerVendorType";
    public static final String TAG_MachineManaged = "MachineManaged";
    public static final String TAG_MaximumBSSLoadValue = "MaximumBSSLoadValue";
    public static final String TAG_MinBackhaulThreshold = "MinBackhaulThreshold";
    public static final String TAG_NetworkID = "NetworkID";
    public static final String TAG_NetworkType = "NetworkType";
    public static final String TAG_Other = "Other";
    public static final String TAG_OtherHomePartners = "OtherHomePartners";
    public static final String TAG_Password = "Password";
    public static final String TAG_PerProviderSubscription = "PerProviderSubscription";
    public static final String TAG_Policy = "Policy";
    public static final String TAG_PolicyUpdate = "PolicyUpdate";
    public static final String TAG_PortNumber = "PortNumber";
    public static final String TAG_PreferredRoamingPartnerList = "PreferredRoamingPartnerList";
    public static final String TAG_Priority = "Priority";
    public static final String TAG_Realm = "Realm";
    public static final String TAG_RequiredProtoPortTuple = "RequiredProtoPortTuple";
    public static final String TAG_Restriction = "Restriction";
    public static final String TAG_RoamingConsortiumOI = "RoamingConsortiumOI";
    public static final String TAG_SIM = "SIM";
    public static final String TAG_SPExclusionList = "SPExclusionList";
    public static final String TAG_SSID = "SSID";
    public static final String TAG_SoftTokenApp = "SoftTokenApp";
    public static final String TAG_StartDate = "StartDate";
    public static final String TAG_SubscriptionParameters = "SubscriptionParameters";
    public static final String TAG_SubscriptionUpdate = "SubscriptionUpdate";
    public static final String TAG_TimeLimit = "TimeLimit";
    public static final String TAG_TrustRoot = "TrustRoot";
    public static final String TAG_TypeOfSubscription = "TypeOfSubscription";
    public static final String TAG_ULBandwidth = "ULBandwidth";
    public static final String TAG_URI = "URI";
    public static final String TAG_UpdateIdentifier = "UpdateIdentifier";
    public static final String TAG_UpdateInterval = "UpdateInterval";
    public static final String TAG_UpdateMethod = "UpdateMethod";
    public static final String TAG_UsageLimits = "UsageLimits";
    public static final String TAG_UsageTimePeriod = "UsageTimePeriod";
    public static final String TAG_Username = "Username";
    public static final String TAG_UsernamePassword = "UsernamePassword";
    public static final String TAG_VendorId = "VendorId";
    public static final String TAG_VendorType = "VendorType";
    private static final Map<String, Map<String, Object>> sSelectionMap = null;
    private final boolean mEnabled;
    private final File mPpsFile;
    private final Map<String, HomeSP> mSPs;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.wifi.hotspot2.omadm.PasspointManagementObjectManager.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.wifi.hotspot2.omadm.PasspointManagementObjectManager.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.wifi.hotspot2.omadm.PasspointManagementObjectManager.<clinit>():void");
    }

    private static void setSelections(String key, Object... pairs) {
        Map<String, Object> kvp = new HashMap();
        sSelectionMap.put(key, kvp);
        for (int n = 0; n < pairs.length; n += 2) {
            kvp.put(pairs[n].toString(), pairs[n + 1]);
        }
    }

    public PasspointManagementObjectManager(File ppsFile, boolean hs2enabled) {
        this.mPpsFile = ppsFile;
        this.mEnabled = hs2enabled;
        this.mSPs = new HashMap();
    }

    public boolean isEnabled() {
        return this.mEnabled;
    }

    public boolean isConfigured() {
        return this.mEnabled && !this.mSPs.isEmpty();
    }

    public Map<String, HomeSP> getLoadedSPs() {
        return Collections.unmodifiableMap(this.mSPs);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public List<HomeSP> loadAllSPs() throws IOException {
        Throwable th;
        Throwable th2 = null;
        if (!this.mEnabled || !this.mPpsFile.exists()) {
            return Collections.emptyList();
        }
        BufferedInputStream bufferedInputStream = null;
        try {
            BufferedInputStream in = new BufferedInputStream(new FileInputStream(this.mPpsFile));
            try {
                this.mSPs.clear();
                try {
                    List<HomeSP> sps = buildSPs(MOTree.unmarshal(in));
                    if (sps != null) {
                        for (HomeSP sp : sps) {
                            if (this.mSPs.put(sp.getFQDN(), sp) != null) {
                                throw new OMAException("Multiple SPs for FQDN '" + sp.getFQDN() + "'");
                            }
                            Log.d(Utils.hs2LogTag(getClass()), "retrieved " + sp.getFQDN() + " from PPS");
                        }
                        if (in != null) {
                            try {
                                in.close();
                            } catch (Throwable th3) {
                                th2 = th3;
                            }
                        }
                        if (th2 == null) {
                            return sps;
                        }
                        throw th2;
                    }
                    throw new OMAException("Failed to build HomeSP");
                } catch (FileNotFoundException e) {
                    List<HomeSP> emptyList = Collections.emptyList();
                    if (in != null) {
                        in.close();
                    }
                } catch (Throwable th4) {
                    th2 = th4;
                }
            } catch (Throwable th5) {
                th = th5;
                bufferedInputStream = in;
            }
        } catch (Throwable th6) {
            th = th6;
            if (bufferedInputStream != null) {
                try {
                    bufferedInputStream.close();
                } catch (Throwable th7) {
                    if (th2 == null) {
                        th2 = th7;
                    } else if (th2 != th7) {
                        th2.addSuppressed(th7);
                    }
                }
            }
            if (th2 != null) {
                throw th2;
            }
            throw th;
        }
    }

    public static HomeSP buildSP(String xml) throws IOException, SAXException {
        List<HomeSP> spList = buildSPs(new OMAParser().parse(xml, OMAConstants.PPS_URN));
        if (spList.size() == 1) {
            return (HomeSP) spList.iterator().next();
        }
        throw new OMAException("Expected exactly one HomeSP, got " + spList.size());
    }

    public HomeSP addSP(String xml) throws IOException, SAXException {
        return addSP(new OMAParser().parse(xml, OMAConstants.PPS_URN));
    }

    public void addSP(HomeSP homeSP) throws IOException {
        if (!this.mEnabled) {
            throw new IOException("HS2.0 not enabled on this device");
        } else if (this.mSPs.containsKey(homeSP.getFQDN())) {
            Log.d(Utils.hs2LogTag(getClass()), "HS20 profile for " + homeSP.getFQDN() + " already exists");
        } else {
            Log.d(Utils.hs2LogTag(getClass()), "Adding new HS20 profile for " + homeSP.getFQDN());
            OMANode dummyRoot = new OMAConstructed(null, TAG_PerProviderSubscription, null, new String[0]);
            buildHomeSPTree(homeSP, dummyRoot, this.mSPs.size() + 1);
            try {
                addSP(dummyRoot);
            } catch (FileNotFoundException e) {
                writeMO(MOTree.buildMgmtTree(OMAConstants.PPS_URN, OMAConstants.OMAVersion, dummyRoot), this.mPpsFile);
            }
            this.mSPs.put(homeSP.getFQDN(), homeSP);
        }
    }

    public HomeSP addSP(MOTree instanceTree) throws IOException {
        List<HomeSP> spList = buildSPs(instanceTree);
        if (spList.size() != 1) {
            throw new OMAException("Expected exactly one HomeSP, got " + spList.size());
        }
        HomeSP sp = (HomeSP) spList.iterator().next();
        String fqdn = sp.getFQDN();
        if (this.mSPs.put(fqdn, sp) != null) {
            throw new OMAException("SP " + fqdn + " already exists");
        }
        try {
            addSP((OMAConstructed) instanceTree.getRoot().getChild(TAG_PerProviderSubscription));
        } catch (FileNotFoundException e) {
            writeMO(new MOTree(instanceTree.getUrn(), instanceTree.getDtdRev(), instanceTree.getRoot()), this.mPpsFile);
        }
        return sp;
    }

    private void addSP(OMANode mo) throws IOException {
        Throwable th;
        Throwable th2 = null;
        BufferedInputStream bufferedInputStream = null;
        try {
            BufferedInputStream in = new BufferedInputStream(new FileInputStream(this.mPpsFile));
            try {
                MOTree moTree = MOTree.unmarshal(in);
                moTree.getRoot().addChild(mo);
                if (in != null) {
                    try {
                        in.close();
                    } catch (Throwable th3) {
                        th2 = th3;
                    }
                }
                if (th2 != null) {
                    throw th2;
                }
                writeMO(moTree, this.mPpsFile);
            } catch (Throwable th4) {
                th = th4;
                bufferedInputStream = in;
                if (bufferedInputStream != null) {
                    try {
                        bufferedInputStream.close();
                    } catch (Throwable th5) {
                        if (th2 == null) {
                            th2 = th5;
                        } else if (th2 != th5) {
                            th2.addSuppressed(th5);
                        }
                    }
                }
                if (th2 == null) {
                    throw th2;
                }
                throw th;
            }
        } catch (Throwable th6) {
            th = th6;
            if (bufferedInputStream != null) {
                bufferedInputStream.close();
            }
            if (th2 == null) {
                throw th;
            }
            throw th2;
        }
    }

    private static OMAConstructed findTargetTree(MOTree moTree, String fqdn) throws OMAException {
        for (OMANode node : moTree.getRoot().getChildren()) {
            OMANode instance = null;
            if (node.getName().equals(TAG_PerProviderSubscription)) {
                instance = getInstanceNode((OMAConstructed) node);
            } else if (!node.isLeaf()) {
                instance = node;
            }
            if (instance != null && fqdn.equalsIgnoreCase(getString(instance.getListValue(FQDNPath.iterator())))) {
                return (OMAConstructed) node;
            }
        }
        return null;
    }

    private static OMAConstructed getInstanceNode(OMAConstructed root) throws OMAException {
        for (OMANode child : root.getChildren()) {
            if (!child.isLeaf()) {
                return (OMAConstructed) child;
            }
        }
        throw new OMAException("Cannot find instance node");
    }

    public int modifySP(String fqdn, Collection<PasspointManagementObjectDefinition> mods) throws IOException, SAXException {
        Throwable th;
        Log.d(Utils.hs2LogTag(getClass()), "modifying SP: " + mods);
        int ppsMods = 0;
        Throwable th2 = null;
        BufferedInputStream bufferedInputStream = null;
        try {
            BufferedInputStream in = new BufferedInputStream(new FileInputStream(this.mPpsFile));
            try {
                MOTree moTree = MOTree.unmarshal(in);
                OMAConstructed targetTree = findTargetTree(moTree, fqdn);
                if (targetTree == null) {
                    throw new IOException("Failed to find PPS tree for " + fqdn);
                }
                OMAConstructed instance = getInstanceNode(targetTree);
                for (PasspointManagementObjectDefinition mod : mods) {
                    LinkedList<String> tailPath = getTailPath(mod.getBaseUri(), TAG_PerProviderSubscription);
                    OMAConstructed modRoot = buildMoTree(mod).getRoot();
                    if (((String) tailPath.getFirst()).equals(TAG_UpdateIdentifier)) {
                        int updateIdentifier = getInteger((OMANode) modRoot.getChildren().iterator().next());
                        OMANode oldUdi = targetTree.getChild(TAG_UpdateIdentifier);
                        if (getInteger(oldUdi) != updateIdentifier) {
                            ppsMods++;
                        }
                        if (oldUdi != null) {
                            targetTree.replaceNode(oldUdi, modRoot.getChild(TAG_UpdateIdentifier));
                        } else {
                            targetTree.addChild(modRoot.getChild(TAG_UpdateIdentifier));
                        }
                    } else {
                        tailPath.removeFirst();
                        OMANode current = instance.getListValue(tailPath.iterator());
                        if (current == null) {
                            throw new IOException("No previous node for " + tailPath + " in " + fqdn);
                        }
                        for (OMANode newNode : modRoot.getChildren()) {
                            current.getParent().replaceNode(current, newNode);
                            ppsMods++;
                        }
                    }
                }
                if (in != null) {
                    try {
                        in.close();
                    } catch (Throwable th3) {
                        th2 = th3;
                    }
                }
                if (th2 != null) {
                    throw th2;
                }
                writeMO(moTree, this.mPpsFile);
                return ppsMods;
            } catch (Throwable th4) {
                th = th4;
                bufferedInputStream = in;
                if (bufferedInputStream != null) {
                    try {
                        bufferedInputStream.close();
                    } catch (Throwable th5) {
                        if (th2 == null) {
                            th2 = th5;
                        } else if (th2 != th5) {
                            th2.addSuppressed(th5);
                        }
                    }
                }
                if (th2 == null) {
                    throw th;
                }
                throw th2;
            }
        } catch (Throwable th6) {
            th = th6;
            if (bufferedInputStream != null) {
                bufferedInputStream.close();
            }
            if (th2 == null) {
                throw th2;
            }
            throw th;
        }
    }

    private static MOTree buildMoTree(PasspointManagementObjectDefinition managementObjectDefinition) throws IOException, SAXException {
        return new OMAParser().parse(managementObjectDefinition.getMoTree(), OMAConstants.PPS_URN);
    }

    private static LinkedList<String> getTailPath(String pathString, String rootName) throws IOException {
        String[] path = pathString.split("/");
        int pathIndex = 0;
        while (pathIndex < path.length) {
            if (path[pathIndex].equalsIgnoreCase(rootName)) {
                pathIndex++;
                break;
            }
            pathIndex++;
        }
        if (pathIndex >= path.length) {
            throw new IOException("Bad node-path: " + pathString);
        }
        LinkedList<String> tailPath = new LinkedList();
        while (pathIndex < path.length) {
            tailPath.add(path[pathIndex]);
            pathIndex++;
        }
        return tailPath;
    }

    public HomeSP getHomeSP(String fqdn) {
        return (HomeSP) this.mSPs.get(fqdn);
    }

    public void removeSP(String fqdn) throws IOException {
        Throwable th;
        Throwable th2 = null;
        if (this.mSPs.remove(fqdn) == null) {
            Log.d(Utils.hs2LogTag(getClass()), "No HS20 profile to delete for " + fqdn);
            return;
        }
        Log.d(Utils.hs2LogTag(getClass()), "Deleting HS20 profile for " + fqdn);
        BufferedInputStream bufferedInputStream = null;
        try {
            BufferedInputStream in = new BufferedInputStream(new FileInputStream(this.mPpsFile));
            try {
                MOTree moTree = MOTree.unmarshal(in);
                OMAConstructed tbd = findTargetTree(moTree, fqdn);
                if (tbd == null) {
                    throw new IOException("Node " + fqdn + " doesn't exist in MO tree");
                } else if (moTree.getRoot().removeNode("?", tbd) == null) {
                    throw new IOException("Failed to remove " + fqdn + " out of MO tree");
                } else {
                    if (in != null) {
                        try {
                            in.close();
                        } catch (Throwable th3) {
                            th2 = th3;
                        }
                    }
                    if (th2 != null) {
                        throw th2;
                    }
                    writeMO(moTree, this.mPpsFile);
                }
            } catch (Throwable th4) {
                th = th4;
                bufferedInputStream = in;
                if (bufferedInputStream != null) {
                    try {
                        bufferedInputStream.close();
                    } catch (Throwable th5) {
                        if (th2 == null) {
                            th2 = th5;
                        } else if (th2 != th5) {
                            th2.addSuppressed(th5);
                        }
                    }
                }
                if (th2 == null) {
                    throw th2;
                }
                throw th;
            }
        } catch (Throwable th6) {
            th = th6;
            if (bufferedInputStream != null) {
                bufferedInputStream.close();
            }
            if (th2 == null) {
                throw th;
            }
            throw th2;
        }
    }

    public String getMOTree(String fqdn) throws IOException {
        Throwable th;
        if (fqdn == null) {
            return null;
        }
        BufferedInputStream bufferedInputStream = null;
        Throwable th2;
        try {
            BufferedInputStream in = new BufferedInputStream(new FileInputStream(this.mPpsFile));
            try {
                OMAConstructed target = findTargetTree(MOTree.unmarshal(in), fqdn);
                if (target == null) {
                    if (in != null) {
                        try {
                            in.close();
                        } catch (Throwable th3) {
                            th2 = th3;
                        }
                    }
                    th2 = null;
                    if (th2 == null) {
                        return null;
                    }
                    try {
                        throw th2;
                    } catch (FileNotFoundException e) {
                        bufferedInputStream = in;
                    }
                } else {
                    String toXml = MOTree.buildMgmtTree(OMAConstants.PPS_URN, OMAConstants.OMAVersion, target).toXml();
                    if (in != null) {
                        try {
                            in.close();
                        } catch (Throwable th4) {
                            th2 = th4;
                        }
                    }
                    th2 = null;
                    if (th2 == null) {
                        return toXml;
                    }
                    throw th2;
                }
            } catch (Throwable th5) {
                th2 = th5;
                th = null;
                bufferedInputStream = in;
                if (bufferedInputStream != null) {
                    try {
                        bufferedInputStream.close();
                    } catch (Throwable th6) {
                        if (th == null) {
                            th = th6;
                        } else if (th != th6) {
                            th.addSuppressed(th6);
                        }
                    }
                }
                if (th == null) {
                    throw th2;
                }
                try {
                    throw th;
                } catch (FileNotFoundException e2) {
                    return null;
                }
            }
        } catch (Throwable th7) {
            th2 = th7;
            th = null;
            if (bufferedInputStream != null) {
                bufferedInputStream.close();
            }
            if (th == null) {
                throw th;
            }
            throw th2;
        }
    }

    private static void writeMO(MOTree moTree, File f) throws IOException {
        Throwable th;
        Throwable th2 = null;
        BufferedOutputStream bufferedOutputStream = null;
        try {
            BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(f, false));
            try {
                moTree.marshal(out);
                out.flush();
                if (out != null) {
                    try {
                        out.close();
                    } catch (Throwable th3) {
                        th2 = th3;
                    }
                }
                if (th2 != null) {
                    throw th2;
                }
            } catch (Throwable th4) {
                th = th4;
                bufferedOutputStream = out;
                if (bufferedOutputStream != null) {
                    try {
                        bufferedOutputStream.close();
                    } catch (Throwable th5) {
                        if (th2 == null) {
                            th2 = th5;
                        } else if (th2 != th5) {
                            th2.addSuppressed(th5);
                        }
                    }
                }
                if (th2 == null) {
                    throw th2;
                }
                throw th;
            }
        } catch (Throwable th6) {
            th = th6;
            if (bufferedOutputStream != null) {
                bufferedOutputStream.close();
            }
            if (th2 == null) {
                throw th;
            }
            throw th2;
        }
    }

    private static OMANode buildHomeSPTree(HomeSP homeSP, OMAConstructed root, int instanceID) throws IOException {
        int instance;
        OMAConstructed inode;
        OMANode providerSubNode = root.addChild(getInstanceString(instanceID), null, null, null);
        OMANode homeSpNode = providerSubNode.addChild(TAG_HomeSP, null, null, null);
        if (!homeSP.getSSIDs().isEmpty()) {
            OMAConstructed nwkIDNode = (OMAConstructed) homeSpNode.addChild(TAG_NetworkID, null, null, null);
            instance = 0;
            for (Entry<String, Long> entry : homeSP.getSSIDs().entrySet()) {
                int instance2 = instance + 1;
                inode = (OMAConstructed) nwkIDNode.addChild(getInstanceString(instance), null, null, null);
                inode.addChild(TAG_SSID, null, (String) entry.getKey(), null);
                if (entry.getValue() != null) {
                    inode.addChild(TAG_HESSID, null, String.format("%012x", new Object[]{entry.getValue()}), null);
                }
                instance = instance2;
            }
        }
        homeSpNode.addChild(TAG_FriendlyName, null, homeSP.getFriendlyName(), null);
        if (homeSP.getIconURL() != null) {
            homeSpNode.addChild(TAG_IconURL, null, homeSP.getIconURL(), null);
        }
        homeSpNode.addChild(TAG_FQDN, null, homeSP.getFQDN(), null);
        if (!(homeSP.getMatchAllOIs().isEmpty() && homeSP.getMatchAnyOIs().isEmpty())) {
            OMAConstructed homeOIList = (OMAConstructed) homeSpNode.addChild(TAG_HomeOIList, null, null, null);
            instance = 0;
            for (Long oi : homeSP.getMatchAllOIs()) {
                instance2 = instance + 1;
                inode = (OMAConstructed) homeOIList.addChild(getInstanceString(instance), null, null, null);
                inode.addChild(TAG_HomeOI, null, String.format("%x", new Object[]{oi}), null);
                inode.addChild(TAG_HomeOIRequired, null, "TRUE", null);
                instance = instance2;
            }
            for (Long oi2 : homeSP.getMatchAnyOIs()) {
                instance2 = instance + 1;
                inode = (OMAConstructed) homeOIList.addChild(getInstanceString(instance), null, null, null);
                inode.addChild(TAG_HomeOI, null, String.format("%x", new Object[]{oi2}), null);
                inode.addChild(TAG_HomeOIRequired, null, "FALSE", null);
                instance = instance2;
            }
        }
        if (!homeSP.getOtherHomePartners().isEmpty()) {
            OMAConstructed otherPartners = (OMAConstructed) homeSpNode.addChild(TAG_OtherHomePartners, null, null, null);
            instance = 0;
            for (String fqdn : homeSP.getOtherHomePartners()) {
                instance2 = instance + 1;
                ((OMAConstructed) otherPartners.addChild(getInstanceString(instance), null, null, null)).addChild(TAG_FQDN, null, fqdn, null);
                instance = instance2;
            }
        }
        if (!homeSP.getRoamingConsortiums().isEmpty()) {
            homeSpNode.addChild(TAG_RoamingConsortiumOI, null, getRCList(homeSP.getRoamingConsortiums()), null);
        }
        OMANode credentialNode = providerSubNode.addChild(TAG_Credential, null, null, null);
        Credential cred = homeSP.getCredential();
        EAPMethod method = cred.getEAPMethod();
        if (cred.getCtime() > 0) {
            credentialNode.addChild(TAG_CreationDate, null, DTFormat.format(new Date(cred.getCtime())), null);
        }
        if (cred.getExpTime() > 0) {
            credentialNode.addChild(TAG_ExpirationDate, null, DTFormat.format(new Date(cred.getExpTime())), null);
        }
        if (method.getEAPMethodID() == EAPMethodID.EAP_SIM || method.getEAPMethodID() == EAPMethodID.EAP_AKA || method.getEAPMethodID() == EAPMethodID.EAP_AKAPrim) {
            OMANode simNode = credentialNode.addChild(TAG_SIM, null, null, null);
            simNode.addChild(TAG_IMSI, null, cred.getImsi().toString(), null);
            simNode.addChild(TAG_EAPType, null, Integer.toString(EAP.mapEAPMethod(method.getEAPMethodID()).intValue()), null);
        } else {
            if (method.getEAPMethodID() == EAPMethodID.EAP_TTLS) {
                OMANode unpNode = credentialNode.addChild(TAG_UsernamePassword, null, null, null);
                unpNode.addChild(TAG_Username, null, cred.getUserName(), null);
                unpNode.addChild(TAG_Password, null, Base64.encodeToString(cred.getPassword().getBytes(StandardCharsets.UTF_8), 0), null);
                OMANode eapNode = unpNode.addChild(TAG_EAPMethod, null, null, null);
                eapNode.addChild(TAG_EAPType, null, Integer.toString(EAP.mapEAPMethod(method.getEAPMethodID()).intValue()), null);
                eapNode.addChild(TAG_InnerMethod, null, ((NonEAPInnerAuth) method.getAuthParam()).getOMAtype(), null);
            } else {
                if (method.getEAPMethodID() == EAPMethodID.EAP_TLS) {
                    OMANode certNode = credentialNode.addChild(TAG_DigitalCertificate, null, null, null);
                    certNode.addChild(TAG_CertificateType, null, Credential.CertTypeX509, null);
                    certNode.addChild(TAG_CertSHA256Fingerprint, null, Utils.toHex(cred.getFingerPrint()), null);
                } else {
                    throw new OMAException("Invalid credential on " + homeSP.getFQDN());
                }
            }
        }
        credentialNode.addChild(TAG_Realm, null, cred.getRealm(), null);
        return providerSubNode;
    }

    private static String getInstanceString(int instance) {
        return "r1i" + instance;
    }

    private static String getRCList(Collection<Long> rcs) {
        StringBuilder builder = new StringBuilder();
        boolean first = true;
        for (Long roamingConsortium : rcs) {
            if (first) {
                first = false;
            } else {
                builder.append(',');
            }
            builder.append(String.format("%x", new Object[]{roamingConsortium}));
        }
        return builder.toString();
    }

    private static List<HomeSP> buildSPs(MOTree moTree) throws OMAException {
        List<HomeSP> homeSPs = new ArrayList();
        if (moTree.getRoot().getName().equals(TAG_PerProviderSubscription)) {
            OMAConstructed spList = moTree.getRoot();
            if (spList == null) {
                return homeSPs;
            }
            for (OMANode node : spList.getChildren()) {
                if (!node.isLeaf()) {
                    homeSPs.add(buildHomeSP(node, 0));
                }
            }
        } else {
            for (OMANode ppsRoot : moTree.getRoot().getChildren()) {
                if (ppsRoot.getName().equals(TAG_PerProviderSubscription)) {
                    Integer updateIdentifier = null;
                    OMANode instance = null;
                    for (OMANode child : ppsRoot.getChildren()) {
                        if (child.getName().equals(TAG_UpdateIdentifier)) {
                            updateIdentifier = Integer.valueOf(getInteger(child));
                        } else if (!child.isLeaf()) {
                            instance = child;
                        }
                    }
                    if (instance == null) {
                        throw new OMAException("PPS node missing instance node");
                    }
                    int intValue;
                    if (updateIdentifier != null) {
                        intValue = updateIdentifier.intValue();
                    } else {
                        intValue = 0;
                    }
                    homeSPs.add(buildHomeSP(instance, intValue));
                }
            }
        }
        return homeSPs;
    }

    private static HomeSP buildHomeSP(OMANode ppsRoot, int updateIdentifier) throws OMAException {
        Map map;
        SubscriptionParameters subscriptionParameters;
        OMANode spRoot = ppsRoot.getChild(TAG_HomeSP);
        String fqdn = spRoot.getScalarValue(Arrays.asList(new String[]{TAG_FQDN}).iterator());
        String friendlyName = spRoot.getScalarValue(Arrays.asList(new String[]{TAG_FriendlyName}).iterator());
        String iconURL = spRoot.getScalarValue(Arrays.asList(new String[]{TAG_IconURL}).iterator());
        HashSet<Long> roamingConsortiums = new HashSet();
        String oiString = spRoot.getScalarValue(Arrays.asList(new String[]{TAG_RoamingConsortiumOI}).iterator());
        if (oiString != null) {
            for (String oi : oiString.split(",")) {
                roamingConsortiums.add(Long.valueOf(Long.parseLong(oi.trim(), 16)));
            }
        }
        Map<String, Long> ssids = new HashMap();
        OMANode ssidListNode = spRoot.getListValue(Arrays.asList(new String[]{TAG_NetworkID}).iterator());
        if (ssidListNode != null) {
            for (OMANode ssidRoot : ssidListNode.getChildren()) {
                ssids.put(ssidRoot.getChild(TAG_SSID).getValue(), getMac(ssidRoot.getChild(TAG_HESSID)));
            }
        }
        Set<Long> matchAnyOIs = new HashSet();
        List<Long> matchAllOIs = new ArrayList();
        OMANode homeOIListNode = spRoot.getListValue(Arrays.asList(new String[]{TAG_HomeOIList}).iterator());
        if (homeOIListNode != null) {
            for (OMANode homeOIRoot : homeOIListNode.getChildren()) {
                String homeOI = homeOIRoot.getChild(TAG_HomeOI).getValue();
                if (Boolean.parseBoolean(homeOIRoot.getChild(TAG_HomeOIRequired).getValue())) {
                    matchAllOIs.add(Long.valueOf(Long.parseLong(homeOI, 16)));
                } else {
                    matchAnyOIs.add(Long.valueOf(Long.parseLong(homeOI, 16)));
                }
            }
        }
        Set<String> otherHomePartners = new HashSet();
        OMANode otherListNode = spRoot.getListValue(Arrays.asList(new String[]{TAG_OtherHomePartners}).iterator());
        if (otherListNode != null) {
            for (OMANode child : otherListNode.getChildren()) {
                otherHomePartners.add(child.getChild(TAG_FQDN).getValue());
            }
        }
        Credential credential = buildCredential(ppsRoot.getChild(TAG_Credential));
        OMANode policyNode = ppsRoot.getChild(TAG_Policy);
        Policy policy = policyNode != null ? new Policy(policyNode) : null;
        OMANode aaaRootNode = ppsRoot.getChild(TAG_AAAServerTrustRoot);
        if (aaaRootNode == null) {
            map = null;
        } else {
            map = new HashMap(aaaRootNode.getChildren().size());
            for (OMANode child2 : aaaRootNode.getChildren()) {
                map.put(getString(child2, TAG_CertURL), getString(child2, TAG_CertSHA256Fingerprint));
            }
        }
        OMANode updateNode = ppsRoot.getChild(TAG_SubscriptionUpdate);
        UpdateInfo updateInfo = updateNode != null ? new UpdateInfo(updateNode) : null;
        OMANode subNode = ppsRoot.getChild(TAG_SubscriptionParameters);
        if (subNode != null) {
            SubscriptionParameters subscriptionParameters2 = new SubscriptionParameters(subNode);
        } else {
            subscriptionParameters = null;
        }
        return new HomeSP(ssids, fqdn, roamingConsortiums, otherHomePartners, matchAnyOIs, matchAllOIs, friendlyName, iconURL, credential, policy, getInteger(ppsRoot.getChild(TAG_CredentialPriority), 0), map, updateInfo, subscriptionParameters, updateIdentifier);
    }

    private static Credential buildCredential(OMANode credNode) throws OMAException {
        long ctime = getTime(credNode.getChild(TAG_CreationDate));
        long expTime = getTime(credNode.getChild(TAG_ExpirationDate));
        String realm = getString(credNode.getChild(TAG_Realm));
        boolean checkAAACert = getBoolean(credNode.getChild(TAG_CheckAAAServerCertStatus));
        OMANode unNode = credNode.getChild(TAG_UsernamePassword);
        OMANode certNode = credNode.getChild(TAG_DigitalCertificate);
        OMANode simNode = credNode.getChild(TAG_SIM);
        int alternatives = (((unNode != null ? 1 : 0) + 0) + (certNode != null ? 1 : 0)) + (simNode != null ? 1 : 0);
        if (alternatives != 1) {
            throw new OMAException("Expected exactly one credential type, got " + alternatives);
        } else if (unNode != null) {
            String userName = getString(unNode.getChild(TAG_Username));
            String password = getString(unNode.getChild(TAG_Password));
            boolean machineManaged = getBoolean(unNode.getChild(TAG_MachineManaged));
            String softTokenApp = getString(unNode.getChild(TAG_SoftTokenApp));
            boolean ableToShare = getBoolean(unNode.getChild(TAG_AbleToShare));
            OMANode eapMethodNode = unNode.getChild(TAG_EAPMethod);
            int eapID = getInteger(eapMethodNode.getChild(TAG_EAPType));
            EAPMethodID eapMethodID = EAP.mapEAPMethod(eapID);
            if (eapMethodID == null) {
                throw new OMAException("Unknown EAP method: " + eapID);
            }
            EAPMethod eapMethod;
            Long vid = getOptionalInteger(eapMethodNode.getChild(TAG_VendorId));
            Long vtype = getOptionalInteger(eapMethodNode.getChild(TAG_VendorType));
            Long innerEAPType = getOptionalInteger(eapMethodNode.getChild(TAG_InnerEAPType));
            EAPMethodID eAPMethodID = null;
            if (innerEAPType != null) {
                eAPMethodID = EAP.mapEAPMethod(innerEAPType.intValue());
                if (eAPMethodID == null) {
                    throw new OMAException("Bad inner EAP method: " + innerEAPType);
                }
            }
            Long innerVid = getOptionalInteger(eapMethodNode.getChild(TAG_InnerVendorID));
            Long innerVtype = getOptionalInteger(eapMethodNode.getChild(TAG_InnerVendorType));
            String innerNonEAPMethod = getString(eapMethodNode.getChild(TAG_InnerMethod));
            if (eAPMethodID != null) {
                eapMethod = new EAPMethod(eapMethodID, new InnerAuthEAP(eAPMethodID));
            } else if (vid != null) {
                eapMethod = new EAPMethod(eapMethodID, new ExpandedEAPMethod(AuthInfoID.ExpandedEAPMethod, vid.intValue(), vtype.longValue()));
            } else if (innerVid != null) {
                eapMethod = new EAPMethod(eapMethodID, new ExpandedEAPMethod(AuthInfoID.ExpandedInnerEAPMethod, innerVid.intValue(), innerVtype.longValue()));
            } else if (innerNonEAPMethod != null) {
                eapMethod = new EAPMethod(eapMethodID, new NonEAPInnerAuth(innerNonEAPMethod));
            } else {
                throw new OMAException("Incomplete set of EAP parameters");
            }
            return new Credential(ctime, expTime, realm, checkAAACert, eapMethod, userName, password, machineManaged, softTokenApp, ableToShare);
        } else if (certNode != null) {
            try {
                return new Credential(ctime, expTime, realm, checkAAACert, new EAPMethod(EAPMethodID.EAP_TLS, null), Credential.mapCertType(getString(certNode.getChild(TAG_CertificateType))), getOctets(certNode.getChild(TAG_CertSHA256Fingerprint)));
            } catch (NumberFormatException nfe) {
                throw new OMAException("Bad hex string: " + nfe.toString());
            }
        } else if (simNode != null) {
            try {
                return new Credential(ctime, expTime, realm, checkAAACert, new EAPMethod(EAP.mapEAPMethod(getInteger(simNode.getChild(TAG_EAPType))), null), new IMSIParameter(getString(simNode.getChild(TAG_IMSI))));
            } catch (IOException ioe) {
                throw new OMAException("Failed to parse IMSI: " + ioe);
            }
        } else {
            throw new OMAException("Missing credential parameters");
        }
    }

    public static OMANode getChild(OMANode node, String key) throws OMAException {
        OMANode child = node.getChild(key);
        if (child != null) {
            return child;
        }
        throw new OMAException("No such node: " + key);
    }

    public static String getString(OMANode node, String key) throws OMAException {
        OMANode child = node.getChild(key);
        if (child == null) {
            throw new OMAException("Missing value for " + key);
        } else if (child.isLeaf()) {
            return child.getValue();
        } else {
            throw new OMAException(key + " is not a leaf node");
        }
    }

    public static long getLong(OMANode node, String key, Long dflt) throws OMAException {
        OMANode child = node.getChild(key);
        if (child == null) {
            if (dflt != null) {
                return dflt.longValue();
            }
            throw new OMAException("Missing value for " + key);
        } else if (child.isLeaf()) {
            String value = child.getValue();
            try {
                long result = Long.parseLong(value);
                if (result >= 0) {
                    return result;
                }
                throw new OMAException("Negative value for " + key);
            } catch (NumberFormatException e) {
                throw new OMAException("Value for " + key + " is non-numeric: " + value);
            }
        } else {
            throw new OMAException(key + " is not a leaf node");
        }
    }

    public static <T> T getSelection(OMANode node, String key) throws OMAException {
        OMANode child = node.getChild(key);
        if (child == null) {
            throw new OMAException("Missing value for " + key);
        } else if (child.isLeaf()) {
            return getSelection(key, child.getValue());
        } else {
            throw new OMAException(key + " is not a leaf node");
        }
    }

    public static <T> T getSelection(String key, String value) throws OMAException {
        if (value == null) {
            throw new OMAException("No value for " + key);
        }
        T result = ((Map) sSelectionMap.get(key)).get(value.toLowerCase());
        if (result != null) {
            return result;
        }
        throw new OMAException("Invalid value '" + value + "' for " + key);
    }

    private static boolean getBoolean(OMANode boolNode) {
        return boolNode != null ? Boolean.parseBoolean(boolNode.getValue()) : false;
    }

    public static String getString(OMANode stringNode) {
        return stringNode != null ? stringNode.getValue() : null;
    }

    private static int getInteger(OMANode intNode, int dflt) throws OMAException {
        if (intNode == null) {
            return dflt;
        }
        return getInteger(intNode);
    }

    private static int getInteger(OMANode intNode) throws OMAException {
        if (intNode == null) {
            throw new OMAException("Missing integer value");
        }
        try {
            return Integer.parseInt(intNode.getValue());
        } catch (NumberFormatException e) {
            throw new OMAException("Invalid integer: " + intNode.getValue());
        }
    }

    private static Long getMac(OMANode macNode) throws OMAException {
        if (macNode == null) {
            return null;
        }
        try {
            return Long.valueOf(Long.parseLong(macNode.getValue(), 16));
        } catch (NumberFormatException e) {
            throw new OMAException("Invalid MAC: " + macNode.getValue());
        }
    }

    private static Long getOptionalInteger(OMANode intNode) throws OMAException {
        if (intNode == null) {
            return null;
        }
        try {
            return Long.valueOf(Long.parseLong(intNode.getValue()));
        } catch (NumberFormatException e) {
            throw new OMAException("Invalid integer: " + intNode.getValue());
        }
    }

    public static long getTime(OMANode timeNode) throws OMAException {
        if (timeNode == null) {
            return -1;
        }
        String timeText = timeNode.getValue();
        try {
            return DTFormat.parse(timeText).getTime();
        } catch (ParseException e) {
            throw new OMAException("Badly formatted time: " + timeText);
        }
    }

    private static byte[] getOctets(OMANode octetNode) throws OMAException {
        if (octetNode != null) {
            return Utils.hexToBytes(octetNode.getValue());
        }
        throw new OMAException("Missing byte value");
    }
}
