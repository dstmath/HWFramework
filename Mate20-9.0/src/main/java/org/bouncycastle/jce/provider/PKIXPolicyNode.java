package org.bouncycastle.jce.provider;

import java.security.cert.PolicyNode;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class PKIXPolicyNode implements PolicyNode {
    protected List children;
    protected boolean critical;
    protected int depth;
    protected Set expectedPolicies;
    protected PolicyNode parent;
    protected Set policyQualifiers;
    protected String validPolicy;

    public PKIXPolicyNode(List list, int i, Set set, PolicyNode policyNode, Set set2, String str, boolean z) {
        this.children = list;
        this.depth = i;
        this.expectedPolicies = set;
        this.parent = policyNode;
        this.policyQualifiers = set2;
        this.validPolicy = str;
        this.critical = z;
    }

    public void addChild(PKIXPolicyNode pKIXPolicyNode) {
        this.children.add(pKIXPolicyNode);
        pKIXPolicyNode.setParent(this);
    }

    public Object clone() {
        return copy();
    }

    public PKIXPolicyNode copy() {
        HashSet hashSet = new HashSet();
        for (String str : this.expectedPolicies) {
            hashSet.add(new String(str));
        }
        HashSet hashSet2 = new HashSet();
        for (String str2 : this.policyQualifiers) {
            hashSet2.add(new String(str2));
        }
        PKIXPolicyNode pKIXPolicyNode = new PKIXPolicyNode(new ArrayList(), this.depth, hashSet, null, hashSet2, new String(this.validPolicy), this.critical);
        for (PKIXPolicyNode copy : this.children) {
            PKIXPolicyNode copy2 = copy.copy();
            copy2.setParent(pKIXPolicyNode);
            pKIXPolicyNode.addChild(copy2);
        }
        return pKIXPolicyNode;
    }

    public Iterator getChildren() {
        return this.children.iterator();
    }

    public int getDepth() {
        return this.depth;
    }

    public Set getExpectedPolicies() {
        return this.expectedPolicies;
    }

    public PolicyNode getParent() {
        return this.parent;
    }

    public Set getPolicyQualifiers() {
        return this.policyQualifiers;
    }

    public String getValidPolicy() {
        return this.validPolicy;
    }

    public boolean hasChildren() {
        return !this.children.isEmpty();
    }

    public boolean isCritical() {
        return this.critical;
    }

    public void removeChild(PKIXPolicyNode pKIXPolicyNode) {
        this.children.remove(pKIXPolicyNode);
    }

    public void setCritical(boolean z) {
        this.critical = z;
    }

    public void setExpectedPolicies(Set set) {
        this.expectedPolicies = set;
    }

    public void setParent(PKIXPolicyNode pKIXPolicyNode) {
        this.parent = pKIXPolicyNode;
    }

    public String toString() {
        return toString("");
    }

    public String toString(String str) {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(str);
        stringBuffer.append(this.validPolicy);
        stringBuffer.append(" {\n");
        for (int i = 0; i < this.children.size(); i++) {
            stringBuffer.append(((PKIXPolicyNode) this.children.get(i)).toString(str + "    "));
        }
        stringBuffer.append(str);
        stringBuffer.append("}\n");
        return stringBuffer.toString();
    }
}
