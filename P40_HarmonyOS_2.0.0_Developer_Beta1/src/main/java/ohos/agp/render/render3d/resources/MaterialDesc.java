package ohos.agp.render.render3d.resources;

import ohos.agp.render.render3d.math.Vector3;
import ohos.agp.render.render3d.math.Vector4;

public class MaterialDesc {
    public static final int MATERIAL_CLEAR_COAT_BIT = 2;
    public static final int MATERIAL_DOUBLE_SIDED_BIT = 1;
    public static final int MATERIAL_NORMAL_MAP_BIT = 8;
    public static final int MATERIAL_SHADOW_RECEIVER_BIT = 4;
    private float alphaCutoff;
    private MaterialAlphaMode alphaMode;
    private GpuResourceHandle ambientOcclusion;
    private float ambientOcclusionFactor;
    private GpuResourceHandle baseColor;
    private Vector4 baseColorFactor;
    private float clearCoatFactor;
    private float clearCoatRoughness;
    private GpuResourceHandle emissive;
    private Vector3 emissiveFactor;
    private float glossinessFactor;
    private GpuResourceHandle material;
    private int materialFlags;
    private float metallicFactor;
    private GpuResourceHandle normal;
    private float normalScale;
    private float reflectance;
    private float roughnessFactor;
    private GpuResourceHandle sampler;
    private Vector3 specularFactor;
    private MaterialType type;

    public enum MaterialAlphaMode {
        UNKNOWN,
        OPAQUE,
        MASK,
        BLEND
    }

    public enum MaterialType {
        UNKNOWN,
        METALLIC_ROUGHNESS,
        SPECULAR_GLOSSINESS,
        UNLIT,
        UNLIT_SHADOW_ALPHA
    }

    public MaterialType getType() {
        return this.type;
    }

    public void setType(MaterialType materialType) {
        this.type = materialType;
    }

    public GpuResourceHandle getBaseColor() {
        return this.baseColor;
    }

    public void setBaseColor(GpuResourceHandle gpuResourceHandle) {
        this.baseColor = gpuResourceHandle;
    }

    public GpuResourceHandle getNormal() {
        return this.normal;
    }

    public void setNormal(GpuResourceHandle gpuResourceHandle) {
        this.normal = gpuResourceHandle;
    }

    public GpuResourceHandle getEmissive() {
        return this.emissive;
    }

    public void setEmissive(GpuResourceHandle gpuResourceHandle) {
        this.emissive = gpuResourceHandle;
    }

    public GpuResourceHandle getMaterial() {
        return this.material;
    }

    public void setMaterial(GpuResourceHandle gpuResourceHandle) {
        this.material = gpuResourceHandle;
    }

    public GpuResourceHandle getAmbientOcclusion() {
        return this.ambientOcclusion;
    }

    public void setAmbientOcclusion(GpuResourceHandle gpuResourceHandle) {
        this.ambientOcclusion = gpuResourceHandle;
    }

    public GpuResourceHandle getSampler() {
        return this.sampler;
    }

    public void setSampler(GpuResourceHandle gpuResourceHandle) {
        this.sampler = gpuResourceHandle;
    }

    public Vector4 getBaseColorFactor() {
        return this.baseColorFactor;
    }

    public void setBaseColorFactor(Vector4 vector4) {
        this.baseColorFactor = vector4;
    }

    public Vector3 getEmissiveFactor() {
        return this.emissiveFactor;
    }

    public void setEmissiveFactor(Vector3 vector3) {
        this.emissiveFactor = vector3;
    }

    public float getAmbientOcclusionFactor() {
        return this.ambientOcclusionFactor;
    }

    public void setAmbientOcclusionFactor(float f) {
        this.ambientOcclusionFactor = f;
    }

    public float getRoughnessFactor() {
        return this.roughnessFactor;
    }

    public void setRoughnessFactor(float f) {
        this.roughnessFactor = f;
    }

    public float getMetallicFactor() {
        return this.metallicFactor;
    }

    public void setMetallicFactor(float f) {
        this.metallicFactor = f;
    }

    public float getReflectance() {
        return this.reflectance;
    }

    public void setReflectance(float f) {
        this.reflectance = f;
    }

    public float getNormalScale() {
        return this.normalScale;
    }

    public void setNormalScale(float f) {
        this.normalScale = f;
    }

    public Vector3 getSpecularFactor() {
        return this.specularFactor;
    }

    public void setSpecularFactor(Vector3 vector3) {
        this.specularFactor = vector3;
    }

    public float getGlossinessFactor() {
        return this.glossinessFactor;
    }

    public void setGlossinessFactor(float f) {
        this.glossinessFactor = f;
    }

    public float getClearCoatFactor() {
        return this.clearCoatFactor;
    }

    public void setClearCoatFactor(float f) {
        this.clearCoatFactor = f;
    }

    public float getClearCoatRoughness() {
        return this.clearCoatRoughness;
    }

    public void setClearCoatRoughness(float f) {
        this.clearCoatRoughness = f;
    }

    public MaterialAlphaMode getAlphaMode() {
        return this.alphaMode;
    }

    public void setAlphaMode(MaterialAlphaMode materialAlphaMode) {
        this.alphaMode = materialAlphaMode;
    }

    public float getAlphaCutoff() {
        return this.alphaCutoff;
    }

    public void setAlphaCutoff(float f) {
        this.alphaCutoff = f;
    }

    public int getMaterialFlags() {
        return this.materialFlags;
    }

    public void setMaterialFlags(int i) {
        this.materialFlags = i;
    }
}
