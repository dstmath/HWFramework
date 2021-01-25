package com.huawei.agpengine.resources;

import com.huawei.agpengine.math.Vector3;
import com.huawei.agpengine.math.Vector4;

public class MaterialDesc {
    public static final int EXTRA_MATERIAL_RENDERING_DISCARD_BIT = 1;
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
    private GpuResourceHandle customMaterialShader;
    private GpuResourceHandle emissive;
    private Vector3 emissiveFactor;
    private float glossinessFactor;
    private GpuResourceHandle material;
    private int materialExtraFlags;
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

    public void setType(MaterialType type2) {
        this.type = type2;
    }

    public GpuResourceHandle getBaseColor() {
        return this.baseColor;
    }

    public void setBaseColor(GpuResourceHandle baseColor2) {
        this.baseColor = baseColor2;
    }

    public GpuResourceHandle getNormal() {
        return this.normal;
    }

    public void setNormal(GpuResourceHandle normal2) {
        this.normal = normal2;
    }

    public GpuResourceHandle getEmissive() {
        return this.emissive;
    }

    public void setEmissive(GpuResourceHandle emissive2) {
        this.emissive = emissive2;
    }

    public GpuResourceHandle getMaterial() {
        return this.material;
    }

    public void setMaterial(GpuResourceHandle material2) {
        this.material = material2;
    }

    public GpuResourceHandle getAmbientOcclusion() {
        return this.ambientOcclusion;
    }

    public void setAmbientOcclusion(GpuResourceHandle ambientOcclusion2) {
        this.ambientOcclusion = ambientOcclusion2;
    }

    public GpuResourceHandle getSampler() {
        return this.sampler;
    }

    public void setSampler(GpuResourceHandle sampler2) {
        this.sampler = sampler2;
    }

    public Vector4 getBaseColorFactor() {
        return this.baseColorFactor;
    }

    public void setBaseColorFactor(Vector4 baseColorFactor2) {
        this.baseColorFactor = baseColorFactor2;
    }

    public Vector3 getEmissiveFactor() {
        return this.emissiveFactor;
    }

    public void setEmissiveFactor(Vector3 emissiveFactor2) {
        this.emissiveFactor = emissiveFactor2;
    }

    public float getAmbientOcclusionFactor() {
        return this.ambientOcclusionFactor;
    }

    public void setAmbientOcclusionFactor(float ambientOcclusionFactor2) {
        this.ambientOcclusionFactor = ambientOcclusionFactor2;
    }

    public float getRoughnessFactor() {
        return this.roughnessFactor;
    }

    public void setRoughnessFactor(float roughnessFactor2) {
        this.roughnessFactor = roughnessFactor2;
    }

    public float getMetallicFactor() {
        return this.metallicFactor;
    }

    public void setMetallicFactor(float metallicFactor2) {
        this.metallicFactor = metallicFactor2;
    }

    public float getReflectance() {
        return this.reflectance;
    }

    public void setReflectance(float reflectance2) {
        this.reflectance = reflectance2;
    }

    public float getNormalScale() {
        return this.normalScale;
    }

    public void setNormalScale(float normalScale2) {
        this.normalScale = normalScale2;
    }

    public Vector3 getSpecularFactor() {
        return this.specularFactor;
    }

    public void setSpecularFactor(Vector3 specularFactor2) {
        this.specularFactor = specularFactor2;
    }

    public float getGlossinessFactor() {
        return this.glossinessFactor;
    }

    public void setGlossinessFactor(float glossinessFactor2) {
        this.glossinessFactor = glossinessFactor2;
    }

    public float getClearCoatFactor() {
        return this.clearCoatFactor;
    }

    public void setClearCoatFactor(float clearCoatFactor2) {
        this.clearCoatFactor = clearCoatFactor2;
    }

    public float getClearCoatRoughness() {
        return this.clearCoatRoughness;
    }

    public void setClearCoatRoughness(float clearCoatRoughness2) {
        this.clearCoatRoughness = clearCoatRoughness2;
    }

    public MaterialAlphaMode getAlphaMode() {
        return this.alphaMode;
    }

    public void setAlphaMode(MaterialAlphaMode alphaMode2) {
        this.alphaMode = alphaMode2;
    }

    public float getAlphaCutoff() {
        return this.alphaCutoff;
    }

    public void setAlphaCutoff(float alphaCutoff2) {
        this.alphaCutoff = alphaCutoff2;
    }

    public int getMaterialFlags() {
        return this.materialFlags;
    }

    public void setMaterialFlags(int materialFlags2) {
        this.materialFlags = materialFlags2;
    }

    public int getMaterialExtraFlags() {
        return this.materialExtraFlags;
    }

    public void setMaterialExtraFlags(int materialFlags2) {
        this.materialExtraFlags = materialFlags2;
    }

    public GpuResourceHandle getCustomMaterialShader() {
        return this.customMaterialShader;
    }

    public void setCustomMaterialShader(GpuResourceHandle shader) {
        this.customMaterialShader = shader;
    }
}
