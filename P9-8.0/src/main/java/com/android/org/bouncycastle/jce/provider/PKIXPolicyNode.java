package com.android.org.bouncycastle.jce.provider;

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

    public PKIXPolicyNode(List _children, int _depth, Set _expectedPolicies, PolicyNode _parent, Set _policyQualifiers, String _validPolicy, boolean _critical) {
        this.children = _children;
        this.depth = _depth;
        this.expectedPolicies = _expectedPolicies;
        this.parent = _parent;
        this.policyQualifiers = _policyQualifiers;
        this.validPolicy = _validPolicy;
        this.critical = _critical;
    }

    public void addChild(PKIXPolicyNode _child) {
        this.children.add(_child);
        _child.setParent(this);
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
        return this.children.isEmpty() ^ 1;
    }

    public boolean isCritical() {
        return this.critical;
    }

    public void removeChild(PKIXPolicyNode _child) {
        this.children.remove(_child);
    }

    public void setCritical(boolean _critical) {
        this.critical = _critical;
    }

    public void setParent(PKIXPolicyNode _parent) {
        this.parent = _parent;
    }

    public String toString() {
        return toString("");
    }

    public String toString(String _indent) {
        StringBuffer _buf = new StringBuffer();
        _buf.append(_indent);
        _buf.append(this.validPolicy);
        _buf.append(" {\n");
        for (int i = 0; i < this.children.size(); i++) {
            _buf.append(((PKIXPolicyNode) this.children.get(i)).toString(_indent + "    "));
        }
        _buf.append(_indent);
        _buf.append("}\n");
        return _buf.toString();
    }

    public Object clone() {
        return copy();
    }

    public PKIXPolicyNode copy() {
        Set _expectedPolicies = new HashSet();
        for (String str : this.expectedPolicies) {
            _expectedPolicies.add(new String(str));
        }
        Set _policyQualifiers = new HashSet();
        for (String str2 : this.policyQualifiers) {
            _policyQualifiers.add(new String(str2));
        }
        PKIXPolicyNode _node = new PKIXPolicyNode(new ArrayList(), this.depth, _expectedPolicies, null, _policyQualifiers, new String(this.validPolicy), this.critical);
        for (PKIXPolicyNode copy : this.children) {
            PKIXPolicyNode _child = copy.copy();
            _child.setParent(_node);
            _node.addChild(_child);
        }
        return _node;
    }

    public void setExpectedPolicies(Set expectedPolicies) {
        this.expectedPolicies = expectedPolicies;
    }
}
