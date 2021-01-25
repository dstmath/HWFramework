package ohos.com.sun.org.apache.xerces.internal.impl.xs;

import ohos.com.sun.org.apache.xerces.internal.impl.xs.util.XSObjectListImpl;
import ohos.com.sun.org.apache.xerces.internal.xs.XSAnnotation;
import ohos.com.sun.org.apache.xerces.internal.xs.XSModelGroup;
import ohos.com.sun.org.apache.xerces.internal.xs.XSNamespaceItem;
import ohos.com.sun.org.apache.xerces.internal.xs.XSObjectList;

public class XSModelGroupImpl implements XSModelGroup {
    public static final short MODELGROUP_ALL = 103;
    public static final short MODELGROUP_CHOICE = 101;
    public static final short MODELGROUP_SEQUENCE = 102;
    public XSObjectList fAnnotations = null;
    public short fCompositor;
    private String fDescription = null;
    public int fParticleCount = 0;
    public XSParticleDecl[] fParticles = null;

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
        return 7;
    }

    public boolean isEmpty() {
        for (int i = 0; i < this.fParticleCount; i++) {
            if (!this.fParticles[i].isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public int minEffectiveTotalRange() {
        if (this.fCompositor == 101) {
            return minEffectiveTotalRangeChoice();
        }
        return minEffectiveTotalRangeAllSeq();
    }

    private int minEffectiveTotalRangeAllSeq() {
        int i = 0;
        for (int i2 = 0; i2 < this.fParticleCount; i2++) {
            i += this.fParticles[i2].minEffectiveTotalRange();
        }
        return i;
    }

    private int minEffectiveTotalRangeChoice() {
        int i = 0;
        if (this.fParticleCount > 0) {
            i = this.fParticles[0].minEffectiveTotalRange();
        }
        for (int i2 = 1; i2 < this.fParticleCount; i2++) {
            int minEffectiveTotalRange = this.fParticles[i2].minEffectiveTotalRange();
            if (minEffectiveTotalRange < i) {
                i = minEffectiveTotalRange;
            }
        }
        return i;
    }

    public int maxEffectiveTotalRange() {
        if (this.fCompositor == 101) {
            return maxEffectiveTotalRangeChoice();
        }
        return maxEffectiveTotalRangeAllSeq();
    }

    private int maxEffectiveTotalRangeAllSeq() {
        int i = 0;
        for (int i2 = 0; i2 < this.fParticleCount; i2++) {
            int maxEffectiveTotalRange = this.fParticles[i2].maxEffectiveTotalRange();
            if (maxEffectiveTotalRange == -1) {
                return -1;
            }
            i += maxEffectiveTotalRange;
        }
        return i;
    }

    private int maxEffectiveTotalRangeChoice() {
        int i = 0;
        if (this.fParticleCount > 0 && (i = this.fParticles[0].maxEffectiveTotalRange()) == -1) {
            return -1;
        }
        for (int i2 = 1; i2 < this.fParticleCount; i2++) {
            int maxEffectiveTotalRange = this.fParticles[i2].maxEffectiveTotalRange();
            if (maxEffectiveTotalRange == -1) {
                return -1;
            }
            if (maxEffectiveTotalRange > i) {
                i = maxEffectiveTotalRange;
            }
        }
        return i;
    }

    public String toString() {
        if (this.fDescription == null) {
            StringBuffer stringBuffer = new StringBuffer();
            if (this.fCompositor == 103) {
                stringBuffer.append("all(");
            } else {
                stringBuffer.append('(');
            }
            if (this.fParticleCount > 0) {
                stringBuffer.append(this.fParticles[0].toString());
            }
            for (int i = 1; i < this.fParticleCount; i++) {
                if (this.fCompositor == 101) {
                    stringBuffer.append('|');
                } else {
                    stringBuffer.append(',');
                }
                stringBuffer.append(this.fParticles[i].toString());
            }
            stringBuffer.append(')');
            this.fDescription = stringBuffer.toString();
        }
        return this.fDescription;
    }

    public void reset() {
        this.fCompositor = 102;
        this.fParticles = null;
        this.fParticleCount = 0;
        this.fDescription = null;
        this.fAnnotations = null;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSModelGroup
    public short getCompositor() {
        short s = this.fCompositor;
        if (s == 101) {
            return 2;
        }
        return s == 102 ? (short) 1 : 3;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSModelGroup
    public XSObjectList getParticles() {
        return new XSObjectListImpl(this.fParticles, this.fParticleCount);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSModelGroup
    public XSAnnotation getAnnotation() {
        XSObjectList xSObjectList = this.fAnnotations;
        if (xSObjectList != null) {
            return (XSAnnotation) xSObjectList.item(0);
        }
        return null;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSModelGroup
    public XSObjectList getAnnotations() {
        XSObjectList xSObjectList = this.fAnnotations;
        return xSObjectList != null ? xSObjectList : XSObjectListImpl.EMPTY_LIST;
    }
}
