package com.android.server.wifi.hotspot2;

import android.text.TextUtils;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class DomainMatcher {
    public static final int MATCH_NONE = 0;
    public static final int MATCH_PRIMARY = 1;
    public static final int MATCH_SECONDARY = 2;
    private final Label mRoot = new Label(0);

    private static class Label {
        private int mMatch;
        private final Map<String, Label> mSubDomains = new HashMap();

        Label(int match) {
            this.mMatch = match;
        }

        public void addDomain(Iterator<String> labels, int match) {
            String labelName = (String) labels.next();
            Label subLabel = (Label) this.mSubDomains.get(labelName);
            if (subLabel == null) {
                subLabel = new Label(0);
                this.mSubDomains.put(labelName, subLabel);
            }
            if (labels.hasNext()) {
                subLabel.addDomain(labels, match);
            } else {
                subLabel.mMatch = match;
            }
        }

        public Label getSubLabel(String labelString) {
            return (Label) this.mSubDomains.get(labelString);
        }

        public int getMatch() {
            return this.mMatch;
        }

        private void toString(StringBuilder sb) {
            if (this.mSubDomains != null) {
                sb.append(".{");
                for (Entry<String, Label> entry : this.mSubDomains.entrySet()) {
                    sb.append((String) entry.getKey());
                    ((Label) entry.getValue()).toString(sb);
                }
                sb.append('}');
                return;
            }
            sb.append('=').append(this.mMatch);
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            toString(sb);
            return sb.toString();
        }
    }

    public DomainMatcher(String primaryDomain, List<String> secondaryDomains) {
        if (secondaryDomains != null) {
            for (String domain : secondaryDomains) {
                if (!TextUtils.isEmpty(domain)) {
                    this.mRoot.addDomain(Utils.splitDomain(domain).iterator(), 2);
                }
            }
        }
        if (!TextUtils.isEmpty(primaryDomain)) {
            this.mRoot.addDomain(Utils.splitDomain(primaryDomain).iterator(), 1);
        }
    }

    public int isSubDomain(String domainName) {
        if (TextUtils.isEmpty(domainName)) {
            return 0;
        }
        List<String> domainLabels = Utils.splitDomain(domainName);
        Label label = this.mRoot;
        int match = 0;
        for (String labelString : domainLabels) {
            label = label.getSubLabel(labelString);
            if (label == null) {
                break;
            } else if (label.getMatch() != 0) {
                match = label.getMatch();
                if (match == 1) {
                    break;
                }
            }
        }
        return match;
    }

    public static boolean arg2SubdomainOfArg1(String domain1, String domain2) {
        if (TextUtils.isEmpty(domain1) || TextUtils.isEmpty(domain2)) {
            return false;
        }
        List<String> labels1 = Utils.splitDomain(domain1);
        List<String> labels2 = Utils.splitDomain(domain2);
        if (labels2.size() < labels1.size()) {
            return false;
        }
        Iterator<String> l2 = labels2.iterator();
        for (CharSequence equals : labels1) {
            if (!TextUtils.equals(equals, (CharSequence) l2.next())) {
                return false;
            }
        }
        return true;
    }

    public String toString() {
        return "Domain matcher " + this.mRoot;
    }
}
