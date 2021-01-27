package ohos.com.sun.org.apache.xerces.internal.impl.xs;

import ohos.com.sun.org.apache.xerces.internal.impl.xs.util.XSObjectListImpl;
import ohos.com.sun.org.apache.xerces.internal.xs.XSNamespaceItem;
import ohos.com.sun.org.apache.xerces.internal.xs.XSObjectList;
import ohos.com.sun.org.apache.xerces.internal.xs.XSParticle;
import ohos.com.sun.org.apache.xerces.internal.xs.XSTerm;
import ohos.com.sun.org.apache.xml.internal.utils.LocaleUtility;

public class XSParticleDecl implements XSParticle {
    public static final short PARTICLE_ELEMENT = 1;
    public static final short PARTICLE_EMPTY = 0;
    public static final short PARTICLE_MODELGROUP = 3;
    public static final short PARTICLE_ONE_OR_MORE = 6;
    public static final short PARTICLE_WILDCARD = 2;
    public static final short PARTICLE_ZERO_OR_MORE = 4;
    public static final short PARTICLE_ZERO_OR_ONE = 5;
    public XSObjectList fAnnotations = null;
    private String fDescription = null;
    public int fMaxOccurs = 1;
    public int fMinOccurs = 1;
    public short fType = 0;
    public XSTerm fValue = null;

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSObject
    public String getName() {
        return null;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSObject
    public String getNamespace() {
        return null;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSObject
    public XSNamespaceItem getNamespaceItem() {
        return null;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSObject
    public short getType() {
        return 8;
    }

    public XSParticleDecl makeClone() {
        XSParticleDecl xSParticleDecl = new XSParticleDecl();
        xSParticleDecl.fType = this.fType;
        xSParticleDecl.fMinOccurs = this.fMinOccurs;
        xSParticleDecl.fMaxOccurs = this.fMaxOccurs;
        xSParticleDecl.fDescription = this.fDescription;
        xSParticleDecl.fValue = this.fValue;
        xSParticleDecl.fAnnotations = this.fAnnotations;
        return xSParticleDecl;
    }

    public boolean emptiable() {
        return minEffectiveTotalRange() == 0;
    }

    public boolean isEmpty() {
        short s = this.fType;
        if (s == 0) {
            return true;
        }
        if (s == 1 || s == 2) {
            return false;
        }
        return ((XSModelGroupImpl) this.fValue).isEmpty();
    }

    public int minEffectiveTotalRange() {
        short s = this.fType;
        if (s == 0) {
            return 0;
        }
        if (s == 3) {
            return ((XSModelGroupImpl) this.fValue).minEffectiveTotalRange() * this.fMinOccurs;
        }
        return this.fMinOccurs;
    }

    public int maxEffectiveTotalRange() {
        short s = this.fType;
        if (s == 0) {
            return 0;
        }
        if (s != 3) {
            return this.fMaxOccurs;
        }
        int maxEffectiveTotalRange = ((XSModelGroupImpl) this.fValue).maxEffectiveTotalRange();
        if (maxEffectiveTotalRange == -1) {
            return -1;
        }
        if (maxEffectiveTotalRange == 0 || this.fMaxOccurs != -1) {
            return maxEffectiveTotalRange * this.fMaxOccurs;
        }
        return -1;
    }

    public String toString() {
        if (this.fDescription == null) {
            StringBuffer stringBuffer = new StringBuffer();
            appendParticle(stringBuffer);
            if (!((this.fMinOccurs == 0 && this.fMaxOccurs == 0) || (this.fMinOccurs == 1 && this.fMaxOccurs == 1))) {
                stringBuffer.append('{');
                stringBuffer.append(this.fMinOccurs);
                int i = this.fMaxOccurs;
                if (i == -1) {
                    stringBuffer.append("-UNBOUNDED");
                } else if (this.fMinOccurs != i) {
                    stringBuffer.append(LocaleUtility.IETF_SEPARATOR);
                    stringBuffer.append(this.fMaxOccurs);
                }
                stringBuffer.append('}');
            }
            this.fDescription = stringBuffer.toString();
        }
        return this.fDescription;
    }

    /* access modifiers changed from: package-private */
    public void appendParticle(StringBuffer stringBuffer) {
        short s = this.fType;
        if (s == 0) {
            stringBuffer.append("EMPTY");
        } else if (s == 1) {
            stringBuffer.append(this.fValue.toString());
        } else if (s == 2) {
            stringBuffer.append('(');
            stringBuffer.append(this.fValue.toString());
            stringBuffer.append(')');
        } else if (s == 3) {
            stringBuffer.append(this.fValue.toString());
        }
    }

    public void reset() {
        this.fType = 0;
        this.fValue = null;
        this.fMinOccurs = 1;
        this.fMaxOccurs = 1;
        this.fDescription = null;
        this.fAnnotations = null;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSParticle
    public int getMinOccurs() {
        return this.fMinOccurs;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSParticle
    public boolean getMaxOccursUnbounded() {
        return this.fMaxOccurs == -1;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSParticle
    public int getMaxOccurs() {
        return this.fMaxOccurs;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSParticle
    public XSTerm getTerm() {
        return this.fValue;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSParticle
    public XSObjectList getAnnotations() {
        XSObjectList xSObjectList = this.fAnnotations;
        return xSObjectList != null ? xSObjectList : XSObjectListImpl.EMPTY_LIST;
    }
}
