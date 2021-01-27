package ohos.com.sun.org.apache.xerces.internal.impl.xs.models;

import ohos.com.sun.org.apache.xerces.internal.impl.dtd.models.CMNode;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.XSComplexTypeDecl;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.XSDeclarationPool;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.XSElementDecl;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.XSModelGroupImpl;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.XSParticleDecl;
import ohos.com.sun.org.apache.xerces.internal.xs.XSTerm;

public class CMBuilder {
    private static XSEmptyCM fEmptyCM = new XSEmptyCM();
    private XSDeclarationPool fDeclPool;
    private int fLeafCount;
    private CMNodeFactory fNodeFactory;
    private int fParticleCount;

    public CMBuilder(CMNodeFactory cMNodeFactory) {
        this.fDeclPool = null;
        this.fDeclPool = null;
        this.fNodeFactory = cMNodeFactory;
    }

    public void setDeclPool(XSDeclarationPool xSDeclarationPool) {
        this.fDeclPool = xSDeclarationPool;
    }

    public XSCMValidator getContentModel(XSComplexTypeDecl xSComplexTypeDecl) {
        XSCMValidator xSCMValidator;
        short contentType = xSComplexTypeDecl.getContentType();
        if (contentType == 1 || contentType == 0) {
            return null;
        }
        XSParticleDecl xSParticleDecl = (XSParticleDecl) xSComplexTypeDecl.getParticle();
        if (xSParticleDecl == null) {
            return fEmptyCM;
        }
        if (xSParticleDecl.fType == 3 && ((XSModelGroupImpl) xSParticleDecl.fValue).fCompositor == 103) {
            xSCMValidator = createAllCM(xSParticleDecl);
        } else {
            xSCMValidator = createDFACM(xSParticleDecl);
        }
        this.fNodeFactory.resetNodeCount();
        return xSCMValidator == null ? fEmptyCM : xSCMValidator;
    }

    /* access modifiers changed from: package-private */
    public XSCMValidator createAllCM(XSParticleDecl xSParticleDecl) {
        if (xSParticleDecl.fMaxOccurs == 0) {
            return null;
        }
        XSModelGroupImpl xSModelGroupImpl = (XSModelGroupImpl) xSParticleDecl.fValue;
        XSAllCM xSAllCM = new XSAllCM(xSParticleDecl.fMinOccurs == 0, xSModelGroupImpl.fParticleCount);
        for (int i = 0; i < xSModelGroupImpl.fParticleCount; i++) {
            xSAllCM.addElement((XSElementDecl) xSModelGroupImpl.fParticles[i].fValue, xSModelGroupImpl.fParticles[i].fMinOccurs == 0);
        }
        return xSAllCM;
    }

    /* access modifiers changed from: package-private */
    public XSCMValidator createDFACM(XSParticleDecl xSParticleDecl) {
        this.fLeafCount = 0;
        this.fParticleCount = 0;
        CMNode buildCompactSyntaxTree = useRepeatingLeafNodes(xSParticleDecl) ? buildCompactSyntaxTree(xSParticleDecl) : buildSyntaxTree(xSParticleDecl, true);
        if (buildCompactSyntaxTree == null) {
            return null;
        }
        return new XSDFACM(buildCompactSyntaxTree, this.fLeafCount);
    }

    private CMNode buildSyntaxTree(XSParticleDecl xSParticleDecl, boolean z) {
        int i = xSParticleDecl.fMaxOccurs;
        int i2 = xSParticleDecl.fMinOccurs;
        short s = xSParticleDecl.fType;
        if (s == 2 || s == 1) {
            CMNodeFactory cMNodeFactory = this.fNodeFactory;
            short s2 = xSParticleDecl.fType;
            XSTerm xSTerm = xSParticleDecl.fValue;
            int i3 = this.fParticleCount;
            this.fParticleCount = i3 + 1;
            int i4 = this.fLeafCount;
            this.fLeafCount = i4 + 1;
            return expandContentModel(cMNodeFactory.getCMLeafNode(s2, xSTerm, i3, i4), i2, i, z);
        } else if (s != 3) {
            return null;
        } else {
            XSModelGroupImpl xSModelGroupImpl = (XSModelGroupImpl) xSParticleDecl.fValue;
            boolean z2 = false;
            CMNode cMNode = null;
            for (int i5 = 0; i5 < xSModelGroupImpl.fParticleCount; i5++) {
                CMNode buildSyntaxTree = buildSyntaxTree(xSModelGroupImpl.fParticles[i5], z && i2 == 1 && i == 1 && (xSModelGroupImpl.fCompositor == 102 || xSModelGroupImpl.fParticleCount == 1));
                if (buildSyntaxTree != null) {
                    if (cMNode == null) {
                        cMNode = buildSyntaxTree;
                    } else {
                        cMNode = this.fNodeFactory.getCMBinOpNode(xSModelGroupImpl.fCompositor, cMNode, buildSyntaxTree);
                        z2 = true;
                    }
                }
            }
            if (cMNode == null) {
                return cMNode;
            }
            if (xSModelGroupImpl.fCompositor == 101 && !z2 && xSModelGroupImpl.fParticleCount > 1) {
                cMNode = this.fNodeFactory.getCMUniOpNode(5, cMNode);
            }
            return expandContentModel(cMNode, i2, i, false);
        }
    }

    private CMNode expandContentModel(CMNode cMNode, int i, int i2, boolean z) {
        if (i == 1 && i2 == 1) {
            return cMNode;
        }
        if (i == 0 && i2 == 1) {
            return this.fNodeFactory.getCMUniOpNode(5, cMNode);
        }
        int i3 = 4;
        if (i == 0 && i2 == -1) {
            return this.fNodeFactory.getCMUniOpNode(4, cMNode);
        }
        if (i == 1 && i2 == -1) {
            return this.fNodeFactory.getCMUniOpNode(6, cMNode);
        }
        if ((z && cMNode.type() == 1) || cMNode.type() == 2) {
            CMNodeFactory cMNodeFactory = this.fNodeFactory;
            if (i != 0) {
                i3 = 6;
            }
            CMNode cMUniOpNode = cMNodeFactory.getCMUniOpNode(i3, cMNode);
            cMUniOpNode.setUserData(new int[]{i, i2});
            return cMUniOpNode;
        } else if (i2 == -1) {
            return this.fNodeFactory.getCMBinOpNode(102, multiNodes(cMNode, i - 1, true), this.fNodeFactory.getCMUniOpNode(6, cMNode));
        } else {
            CMNode multiNodes = i > 0 ? multiNodes(cMNode, i, false) : null;
            if (i2 <= i) {
                return multiNodes;
            }
            CMNode cMUniOpNode2 = this.fNodeFactory.getCMUniOpNode(5, cMNode);
            if (multiNodes == null) {
                return multiNodes(cMUniOpNode2, i2 - i, false);
            }
            return this.fNodeFactory.getCMBinOpNode(102, multiNodes, multiNodes(cMUniOpNode2, i2 - i, true));
        }
    }

    private CMNode multiNodes(CMNode cMNode, int i, boolean z) {
        if (i == 0) {
            return null;
        }
        if (i == 1) {
            return z ? copyNode(cMNode) : cMNode;
        }
        int i2 = i / 2;
        return this.fNodeFactory.getCMBinOpNode(102, multiNodes(cMNode, i2, z), multiNodes(cMNode, i - i2, true));
    }

    private CMNode copyNode(CMNode cMNode) {
        int type = cMNode.type();
        if (type == 101 || type == 102) {
            XSCMBinOp xSCMBinOp = (XSCMBinOp) cMNode;
            return this.fNodeFactory.getCMBinOpNode(type, copyNode(xSCMBinOp.getLeft()), copyNode(xSCMBinOp.getRight()));
        } else if (type == 4 || type == 6 || type == 5) {
            return this.fNodeFactory.getCMUniOpNode(type, copyNode(((XSCMUniOp) cMNode).getChild()));
        } else {
            if (type != 1 && type != 2) {
                return cMNode;
            }
            XSCMLeaf xSCMLeaf = (XSCMLeaf) cMNode;
            CMNodeFactory cMNodeFactory = this.fNodeFactory;
            int type2 = xSCMLeaf.type();
            Object leaf = xSCMLeaf.getLeaf();
            int particleId = xSCMLeaf.getParticleId();
            int i = this.fLeafCount;
            this.fLeafCount = i + 1;
            return cMNodeFactory.getCMLeafNode(type2, leaf, particleId, i);
        }
    }

    private CMNode buildCompactSyntaxTree(XSParticleDecl xSParticleDecl) {
        int i = xSParticleDecl.fMaxOccurs;
        int i2 = xSParticleDecl.fMinOccurs;
        short s = xSParticleDecl.fType;
        if (s == 2 || s == 1) {
            return buildCompactSyntaxTree2(xSParticleDecl, i2, i);
        }
        CMNode cMNode = null;
        if (s != 3) {
            return null;
        }
        XSModelGroupImpl xSModelGroupImpl = (XSModelGroupImpl) xSParticleDecl.fValue;
        if (xSModelGroupImpl.fParticleCount == 1 && !(i2 == 1 && i == 1)) {
            return buildCompactSyntaxTree2(xSModelGroupImpl.fParticles[0], i2, i);
        }
        int i3 = 0;
        for (int i4 = 0; i4 < xSModelGroupImpl.fParticleCount; i4++) {
            CMNode buildCompactSyntaxTree = buildCompactSyntaxTree(xSModelGroupImpl.fParticles[i4]);
            if (buildCompactSyntaxTree != null) {
                i3++;
                if (cMNode != null) {
                    buildCompactSyntaxTree = this.fNodeFactory.getCMBinOpNode(xSModelGroupImpl.fCompositor, cMNode, buildCompactSyntaxTree);
                }
                cMNode = buildCompactSyntaxTree;
            }
        }
        return (cMNode == null || xSModelGroupImpl.fCompositor != 101 || i3 >= xSModelGroupImpl.fParticleCount) ? cMNode : this.fNodeFactory.getCMUniOpNode(5, cMNode);
    }

    private CMNode buildCompactSyntaxTree2(XSParticleDecl xSParticleDecl, int i, int i2) {
        if (i == 1 && i2 == 1) {
            CMNodeFactory cMNodeFactory = this.fNodeFactory;
            short s = xSParticleDecl.fType;
            XSTerm xSTerm = xSParticleDecl.fValue;
            int i3 = this.fParticleCount;
            this.fParticleCount = i3 + 1;
            int i4 = this.fLeafCount;
            this.fLeafCount = i4 + 1;
            return cMNodeFactory.getCMLeafNode(s, xSTerm, i3, i4);
        } else if (i == 0 && i2 == 1) {
            CMNodeFactory cMNodeFactory2 = this.fNodeFactory;
            short s2 = xSParticleDecl.fType;
            XSTerm xSTerm2 = xSParticleDecl.fValue;
            int i5 = this.fParticleCount;
            this.fParticleCount = i5 + 1;
            int i6 = this.fLeafCount;
            this.fLeafCount = i6 + 1;
            return this.fNodeFactory.getCMUniOpNode(5, cMNodeFactory2.getCMLeafNode(s2, xSTerm2, i5, i6));
        } else if (i == 0 && i2 == -1) {
            CMNodeFactory cMNodeFactory3 = this.fNodeFactory;
            short s3 = xSParticleDecl.fType;
            XSTerm xSTerm3 = xSParticleDecl.fValue;
            int i7 = this.fParticleCount;
            this.fParticleCount = i7 + 1;
            int i8 = this.fLeafCount;
            this.fLeafCount = i8 + 1;
            return this.fNodeFactory.getCMUniOpNode(4, cMNodeFactory3.getCMLeafNode(s3, xSTerm3, i7, i8));
        } else if (i == 1 && i2 == -1) {
            CMNodeFactory cMNodeFactory4 = this.fNodeFactory;
            short s4 = xSParticleDecl.fType;
            XSTerm xSTerm4 = xSParticleDecl.fValue;
            int i9 = this.fParticleCount;
            this.fParticleCount = i9 + 1;
            int i10 = this.fLeafCount;
            this.fLeafCount = i10 + 1;
            return this.fNodeFactory.getCMUniOpNode(6, cMNodeFactory4.getCMLeafNode(s4, xSTerm4, i9, i10));
        } else {
            CMNodeFactory cMNodeFactory5 = this.fNodeFactory;
            short s5 = xSParticleDecl.fType;
            XSTerm xSTerm5 = xSParticleDecl.fValue;
            int i11 = this.fParticleCount;
            this.fParticleCount = i11 + 1;
            int i12 = this.fLeafCount;
            this.fLeafCount = i12 + 1;
            CMNode cMRepeatingLeafNode = cMNodeFactory5.getCMRepeatingLeafNode(s5, xSTerm5, i, i2, i11, i12);
            if (i == 0) {
                return this.fNodeFactory.getCMUniOpNode(4, cMRepeatingLeafNode);
            }
            return this.fNodeFactory.getCMUniOpNode(6, cMRepeatingLeafNode);
        }
    }

    private boolean useRepeatingLeafNodes(XSParticleDecl xSParticleDecl) {
        int i = xSParticleDecl.fMaxOccurs;
        int i2 = xSParticleDecl.fMinOccurs;
        if (xSParticleDecl.fType == 3) {
            XSModelGroupImpl xSModelGroupImpl = (XSModelGroupImpl) xSParticleDecl.fValue;
            if (i2 == 1 && i == 1) {
                for (int i3 = 0; i3 < xSModelGroupImpl.fParticleCount; i3++) {
                    if (!useRepeatingLeafNodes(xSModelGroupImpl.fParticles[i3])) {
                        return false;
                    }
                }
            } else if (xSModelGroupImpl.fParticleCount == 1) {
                XSParticleDecl xSParticleDecl2 = xSModelGroupImpl.fParticles[0];
                short s = xSParticleDecl2.fType;
                if ((s == 1 || s == 2) && xSParticleDecl2.fMinOccurs == 1 && xSParticleDecl2.fMaxOccurs == 1) {
                    return true;
                }
                return false;
            } else if (xSModelGroupImpl.fParticleCount == 0) {
                return true;
            } else {
                return false;
            }
        }
        return true;
    }
}
