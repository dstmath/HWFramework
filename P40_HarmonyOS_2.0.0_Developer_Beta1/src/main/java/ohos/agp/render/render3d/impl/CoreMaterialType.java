package ohos.agp.render.render3d.impl;

/* access modifiers changed from: package-private */
public enum CoreMaterialType {
    CORE_MATERIAL_METALLIC_ROUGHNESS(0),
    CORE_MATERIAL_SPECULAR_GLOSSINESS(1),
    CORE_MATERIAL_UNLIT(2),
    CORE_MATERIAL_UNLIT_SHADOW_ALPHA(3);
    
    private final int swigValue;

    /* access modifiers changed from: package-private */
    public final int swigValue() {
        return this.swigValue;
    }

    static CoreMaterialType swigToEnum(int i) {
        CoreMaterialType[] coreMaterialTypeArr = (CoreMaterialType[]) CoreMaterialType.class.getEnumConstants();
        if (i < coreMaterialTypeArr.length && i >= 0 && coreMaterialTypeArr[i].swigValue == i) {
            return coreMaterialTypeArr[i];
        }
        for (CoreMaterialType coreMaterialType : coreMaterialTypeArr) {
            if (coreMaterialType.swigValue == i) {
                return coreMaterialType;
            }
        }
        throw new IllegalArgumentException("No enum " + CoreMaterialType.class + " with value " + i);
    }

    private CoreMaterialType() {
        this(SwigNext.next);
    }

    private CoreMaterialType(int i) {
        this.swigValue = i;
        int unused = SwigNext.next = i + 1;
    }

    private CoreMaterialType(CoreMaterialType coreMaterialType) {
        this(coreMaterialType.swigValue);
    }

    private static class SwigNext {
        private static int next;

        private SwigNext() {
        }
    }
}
