package com.android.server.wifi.hotspot2.pps;

import com.android.server.wifi.hotspot2.Utils;
import com.android.server.wifi.hotspot2.omadm.OMAException;
import com.android.server.wifi.hotspot2.omadm.OMANode;
import com.android.server.wifi.hotspot2.omadm.PasspointManagementObjectManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Policy {
    private final int mMaxBSSLoad;
    private final List<MinBackhaul> mMinBackhaulThresholds;
    private final UpdateInfo mPolicyUpdate;
    private final List<PreferredRoamingPartner> mPreferredRoamingPartners;
    private final Map<Integer, List<Integer>> mRequiredProtos;
    private final List<String> mSPExclusionList;

    private static class MinBackhaul {
        private final long mDL;
        private final Boolean mHome;
        private final long mUL;

        private MinBackhaul(OMANode node) throws OMAException {
            this.mHome = (Boolean) PasspointManagementObjectManager.getSelection(node, PasspointManagementObjectManager.TAG_NetworkType);
            this.mDL = PasspointManagementObjectManager.getLong(node, PasspointManagementObjectManager.TAG_DLBandwidth, Long.valueOf(Long.MAX_VALUE));
            this.mUL = PasspointManagementObjectManager.getLong(node, PasspointManagementObjectManager.TAG_ULBandwidth, Long.valueOf(Long.MAX_VALUE));
        }

        public String toString() {
            return "MinBackhaul{home=" + this.mHome + ", DL=" + this.mDL + ", UL=" + this.mUL + '}';
        }
    }

    private static class PreferredRoamingPartner {
        private final String mCountry;
        private final List<String> mDomain;
        private final Boolean mIncludeSubDomains;
        private final int mPriority;

        private PreferredRoamingPartner(OMANode node) throws OMAException {
            String[] segments = PasspointManagementObjectManager.getString(node, PasspointManagementObjectManager.TAG_FQDN_Match).split(",");
            if (segments.length != 2) {
                throw new OMAException("Bad FQDN match string: FQDN_Match");
            }
            this.mDomain = Utils.splitDomain(segments[0]);
            this.mIncludeSubDomains = (Boolean) PasspointManagementObjectManager.getSelection(PasspointManagementObjectManager.TAG_FQDN_Match, segments[1]);
            this.mPriority = (int) PasspointManagementObjectManager.getLong(node, PasspointManagementObjectManager.TAG_Priority, null);
            this.mCountry = PasspointManagementObjectManager.getString(node, PasspointManagementObjectManager.TAG_Country);
        }

        public String toString() {
            return "PreferredRoamingPartner{domain=" + this.mDomain + ", includeSubDomains=" + this.mIncludeSubDomains + ", priority=" + this.mPriority + ", country='" + this.mCountry + '\'' + '}';
        }
    }

    public Policy(OMANode node) throws OMAException {
        OMANode rpNode = node.getChild(PasspointManagementObjectManager.TAG_PreferredRoamingPartnerList);
        if (rpNode == null) {
            this.mPreferredRoamingPartners = null;
        } else {
            this.mPreferredRoamingPartners = new ArrayList(rpNode.getChildren().size());
            for (OMANode instance : rpNode.getChildren()) {
                if (instance.isLeaf()) {
                    throw new OMAException("Not expecting leaf node in PreferredRoamingPartnerList");
                }
                this.mPreferredRoamingPartners.add(new PreferredRoamingPartner(null));
            }
        }
        OMANode bhtNode = node.getChild(PasspointManagementObjectManager.TAG_MinBackhaulThreshold);
        if (bhtNode == null) {
            this.mMinBackhaulThresholds = null;
        } else {
            this.mMinBackhaulThresholds = new ArrayList(bhtNode.getChildren().size());
            for (OMANode instance2 : bhtNode.getChildren()) {
                if (instance2.isLeaf()) {
                    throw new OMAException("Not expecting leaf node in MinBackhaulThreshold");
                }
                this.mMinBackhaulThresholds.add(new MinBackhaul(null));
            }
        }
        this.mPolicyUpdate = new UpdateInfo(node.getChild(PasspointManagementObjectManager.TAG_PolicyUpdate));
        OMANode sxNode = node.getChild(PasspointManagementObjectManager.TAG_SPExclusionList);
        if (sxNode == null) {
            this.mSPExclusionList = null;
        } else {
            this.mSPExclusionList = new ArrayList(sxNode.getChildren().size());
            for (OMANode instance22 : sxNode.getChildren()) {
                if (instance22.isLeaf()) {
                    throw new OMAException("Not expecting leaf node in SPExclusionList");
                }
                this.mSPExclusionList.add(PasspointManagementObjectManager.getString(instance22, PasspointManagementObjectManager.TAG_SSID));
            }
        }
        OMANode rptNode = node.getChild(PasspointManagementObjectManager.TAG_RequiredProtoPortTuple);
        if (rptNode == null) {
            this.mRequiredProtos = null;
        } else {
            this.mRequiredProtos = new HashMap(rptNode.getChildren().size());
            for (OMANode instance222 : rptNode.getChildren()) {
                if (instance222.isLeaf()) {
                    throw new OMAException("Not expecting leaf node in RequiredProtoPortTuple");
                }
                int protocol = (int) PasspointManagementObjectManager.getLong(instance222, PasspointManagementObjectManager.TAG_IPProtocol, null);
                String[] portSegments = PasspointManagementObjectManager.getString(instance222, PasspointManagementObjectManager.TAG_PortNumber).split(",");
                List<Integer> ports = new ArrayList(portSegments.length);
                int i = 0;
                int length = portSegments.length;
                while (i < length) {
                    String portSegment = portSegments[i];
                    try {
                        ports.add(Integer.valueOf(Integer.parseInt(portSegment)));
                        i++;
                    } catch (NumberFormatException e) {
                        throw new OMAException("Port is not a number: " + portSegment);
                    }
                }
                this.mRequiredProtos.put(Integer.valueOf(protocol), ports);
            }
        }
        this.mMaxBSSLoad = (int) PasspointManagementObjectManager.getLong(node, PasspointManagementObjectManager.TAG_MaximumBSSLoadValue, Long.valueOf(Long.MAX_VALUE));
    }

    public List<PreferredRoamingPartner> getPreferredRoamingPartners() {
        return this.mPreferredRoamingPartners;
    }

    public List<MinBackhaul> getMinBackhaulThresholds() {
        return this.mMinBackhaulThresholds;
    }

    public UpdateInfo getPolicyUpdate() {
        return this.mPolicyUpdate;
    }

    public List<String> getSPExclusionList() {
        return this.mSPExclusionList;
    }

    public Map<Integer, List<Integer>> getRequiredProtos() {
        return this.mRequiredProtos;
    }

    public int getMaxBSSLoad() {
        return this.mMaxBSSLoad;
    }

    public String toString() {
        return "Policy{preferredRoamingPartners=" + this.mPreferredRoamingPartners + ", minBackhaulThresholds=" + this.mMinBackhaulThresholds + ", policyUpdate=" + this.mPolicyUpdate + ", SPExclusionList=" + this.mSPExclusionList + ", requiredProtos=" + this.mRequiredProtos + ", maxBSSLoad=" + this.mMaxBSSLoad + '}';
    }
}
