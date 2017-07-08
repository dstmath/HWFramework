package com.android.server.wifi.hotspot2.pps;

import com.android.server.wifi.hotspot2.Utils;
import com.android.server.wifi.hotspot2.omadm.OMAException;
import com.android.server.wifi.hotspot2.omadm.OMANode;
import com.android.server.wifi.hotspot2.omadm.PasspointManagementObjectManager;
import java.util.ArrayList;
import java.util.List;

public class SubscriptionParameters {
    private final long mCDate;
    private final List<Limit> mLimits;
    private final String mType;
    private final long mXDate;

    private static class Limit {
        private final long mDataLimit;
        private final long mStartDate;
        private final long mTimeLimit;
        private final long mUsageTimePeriod;

        private Limit(OMANode node) throws OMAException {
            this.mDataLimit = PasspointManagementObjectManager.getLong(node, PasspointManagementObjectManager.TAG_DataLimit, Long.valueOf(Long.MAX_VALUE));
            this.mStartDate = PasspointManagementObjectManager.getTime(node.getChild(PasspointManagementObjectManager.TAG_StartDate));
            this.mTimeLimit = PasspointManagementObjectManager.getLong(node, PasspointManagementObjectManager.TAG_TimeLimit, Long.valueOf(Long.MAX_VALUE)) * PasspointManagementObjectManager.IntervalFactor;
            this.mUsageTimePeriod = PasspointManagementObjectManager.getLong(node, PasspointManagementObjectManager.TAG_UsageTimePeriod, null);
        }

        public String toString() {
            return "Limit{dataLimit=" + this.mDataLimit + ", startDate=" + Utils.toUTCString(this.mStartDate) + ", timeLimit=" + this.mTimeLimit + ", usageTimePeriod=" + this.mUsageTimePeriod + '}';
        }
    }

    public SubscriptionParameters(OMANode node) throws OMAException {
        this.mCDate = PasspointManagementObjectManager.getTime(node.getChild(PasspointManagementObjectManager.TAG_CreationDate));
        this.mXDate = PasspointManagementObjectManager.getTime(node.getChild(PasspointManagementObjectManager.TAG_ExpirationDate));
        this.mType = PasspointManagementObjectManager.getString(node.getChild(PasspointManagementObjectManager.TAG_TypeOfSubscription));
        OMANode ulNode = node.getChild(PasspointManagementObjectManager.TAG_UsageLimits);
        if (ulNode == null) {
            this.mLimits = null;
            return;
        }
        this.mLimits = new ArrayList(ulNode.getChildren().size());
        for (OMANode instance : ulNode.getChildren()) {
            if (instance.isLeaf()) {
                throw new OMAException("Not expecting leaf node in UsageLimits");
            }
            this.mLimits.add(new Limit(null));
        }
    }

    public String toString() {
        return "SubscriptionParameters{cDate=" + Utils.toUTCString(this.mCDate) + ", xDate=" + Utils.toUTCString(this.mXDate) + ", type='" + this.mType + '\'' + ", limits=" + this.mLimits + '}';
    }
}
