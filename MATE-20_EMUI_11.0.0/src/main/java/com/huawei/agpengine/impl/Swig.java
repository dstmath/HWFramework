package com.huawei.agpengine.impl;

import com.huawei.agpengine.components.CameraComponent;
import com.huawei.agpengine.components.LightComponent;
import com.huawei.agpengine.components.SceneComponent;
import com.huawei.agpengine.math.Quaternion;
import com.huawei.agpengine.math.Vector2;
import com.huawei.agpengine.math.Vector3;
import com.huawei.agpengine.math.Vector4;

/* access modifiers changed from: package-private */
public class Swig {
    private Swig() {
    }

    static Vector2 get(CoreVec2 vec) {
        if (vec != null) {
            return new Vector2(vec.getX(), vec.getY());
        }
        return Vector2.ZERO;
    }

    static Vector3 get(CoreVec3 vec) {
        if (vec != null) {
            return new Vector3(vec.getX(), vec.getY(), vec.getZ());
        }
        return Vector3.ZERO;
    }

    static Vector4 get(CoreVec4 vec) {
        if (vec != null) {
            return new Vector4(vec.getX(), vec.getY(), vec.getZ(), vec.getW());
        }
        return Vector4.ZERO;
    }

    static Quaternion get(CoreQuat quat) {
        if (quat != null) {
            return new Quaternion(quat.getX(), quat.getY(), quat.getZ(), quat.getW());
        }
        return Quaternion.ZERO;
    }

    static CoreVec2 set(Vector2 vec) {
        return new CoreVec2(vec.getX(), vec.getY());
    }

    static CoreVec3 set(Vector3 vec) {
        return new CoreVec3(vec.getX(), vec.getY(), vec.getZ());
    }

    static CoreVec4 set(Vector4 vec) {
        return new CoreVec4(vec.getX(), vec.getY(), vec.getZ(), vec.getW());
    }

    static CoreQuat set(Quaternion vec) {
        return new CoreQuat(vec.getX(), vec.getY(), vec.getZ(), vec.getW());
    }

    static short getNativeCameraType(CameraComponent.CameraType type) {
        int i = AnonymousClass1.$SwitchMap$com$huawei$agpengine$components$CameraComponent$CameraType[type.ordinal()];
        if (i == 1) {
            return (short) CoreCameraType.CORE_CAMERA_TYPE_ORTHOGRAPHIC.swigValue();
        }
        if (i == 2) {
            return (short) CoreCameraType.CORE_CAMERA_TYPE_PERSPECTIVE.swigValue();
        }
        if (i == 3) {
            return (short) CoreCameraType.CORE_CAMERA_TYPE_CUSTOM.swigValue();
        }
        throw new IllegalArgumentException("Internal graphics engine error");
    }

    static CameraComponent.CameraType getCameraType(short swigValue) {
        int i = AnonymousClass1.$SwitchMap$com$huawei$agpengine$impl$CoreCameraType[CoreCameraType.swigToEnum(swigValue).ordinal()];
        if (i == 1) {
            return CameraComponent.CameraType.ORTHOGRAPHIC;
        }
        if (i == 2) {
            return CameraComponent.CameraType.PERSPECTIVE;
        }
        if (i == 3) {
            return CameraComponent.CameraType.CUSTOM;
        }
        throw new IllegalArgumentException("Internal graphics engine error");
    }

    static short getNativeCameraTargetType(CameraComponent.CameraTargetType type) {
        int i = AnonymousClass1.$SwitchMap$com$huawei$agpengine$components$CameraComponent$CameraTargetType[type.ordinal()];
        if (i == 1) {
            return (short) CoreCameraTargetType.CORE_CAMERA_TARGET_TYPE_DEFAULT.swigValue();
        }
        if (i == 2) {
            return (short) CoreCameraTargetType.CORE_CAMERA_TARGET_TYPE_CUSTOM.swigValue();
        }
        throw new IllegalArgumentException("Internal graphics engine error");
    }

    static CameraComponent.CameraTargetType getCameraTargetType(short swigValue) {
        int i = AnonymousClass1.$SwitchMap$com$huawei$agpengine$impl$CoreCameraTargetType[CoreCameraTargetType.swigToEnum(swigValue).ordinal()];
        if (i == 1) {
            return CameraComponent.CameraTargetType.DEFAULT;
        }
        if (i == 2) {
            return CameraComponent.CameraTargetType.CUSTOM;
        }
        throw new IllegalArgumentException("Internal graphics engine error");
    }

    static short getNativeLightType(LightComponent.LightType type) {
        int i = AnonymousClass1.$SwitchMap$com$huawei$agpengine$components$LightComponent$LightType[type.ordinal()];
        if (i == 1) {
            return (short) CoreLightType.CORE_LIGHT_TYPE_INVALID.swigValue();
        }
        if (i == 2) {
            return (short) CoreLightType.CORE_LIGHT_TYPE_DIRECTIONAL.swigValue();
        }
        if (i == 3) {
            return (short) CoreLightType.CORE_LIGHT_TYPE_POINT.swigValue();
        }
        if (i == 4) {
            return (short) CoreLightType.CORE_LIGHT_TYPE_SPOT.swigValue();
        }
        throw new IllegalArgumentException("Internal graphics engine error");
    }

    static LightComponent.LightType getLightType(short swigValue) {
        int i = AnonymousClass1.$SwitchMap$com$huawei$agpengine$impl$CoreLightType[CoreLightType.swigToEnum(swigValue).ordinal()];
        if (i == 1) {
            return LightComponent.LightType.INVALID;
        }
        if (i == 2) {
            return LightComponent.LightType.DIRECTIONAL;
        }
        if (i == 3) {
            return LightComponent.LightType.POINT;
        }
        if (i == 4) {
            return LightComponent.LightType.SPOT;
        }
        throw new IllegalArgumentException("Internal graphics engine error");
    }

    static short getNativeEnvBgType(SceneComponent.BackgroundType type) {
        int i = AnonymousClass1.$SwitchMap$com$huawei$agpengine$components$SceneComponent$BackgroundType[type.ordinal()];
        if (i == 1) {
            return (short) CoreEnvironmentBackgroundType.CORE_ENV_BG_NONE.swigValue();
        }
        if (i == 2) {
            return (short) CoreEnvironmentBackgroundType.CORE_ENV_BG_IMAGE.swigValue();
        }
        if (i == 3) {
            return (short) CoreEnvironmentBackgroundType.CORE_ENV_BG_CUBEMAP.swigValue();
        }
        if (i == 4) {
            return (short) CoreEnvironmentBackgroundType.CORE_ENV_BG_EQUIRECTANGULAR.swigValue();
        }
        throw new IllegalArgumentException("Internal graphics engine error");
    }

    /* access modifiers changed from: package-private */
    /* renamed from: com.huawei.agpengine.impl.Swig$1  reason: invalid class name */
    public static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$huawei$agpengine$components$CameraComponent$CameraTargetType = new int[CameraComponent.CameraTargetType.values().length];
        static final /* synthetic */ int[] $SwitchMap$com$huawei$agpengine$components$CameraComponent$CameraType = new int[CameraComponent.CameraType.values().length];
        static final /* synthetic */ int[] $SwitchMap$com$huawei$agpengine$components$LightComponent$LightType = new int[LightComponent.LightType.values().length];
        static final /* synthetic */ int[] $SwitchMap$com$huawei$agpengine$components$SceneComponent$BackgroundType = new int[SceneComponent.BackgroundType.values().length];
        static final /* synthetic */ int[] $SwitchMap$com$huawei$agpengine$impl$CoreCameraTargetType = new int[CoreCameraTargetType.values().length];
        static final /* synthetic */ int[] $SwitchMap$com$huawei$agpengine$impl$CoreCameraType = new int[CoreCameraType.values().length];
        static final /* synthetic */ int[] $SwitchMap$com$huawei$agpengine$impl$CoreEnvironmentBackgroundType = new int[CoreEnvironmentBackgroundType.values().length];
        static final /* synthetic */ int[] $SwitchMap$com$huawei$agpengine$impl$CoreLightType = new int[CoreLightType.values().length];

        static {
            try {
                $SwitchMap$com$huawei$agpengine$impl$CoreEnvironmentBackgroundType[CoreEnvironmentBackgroundType.CORE_ENV_BG_NONE.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$huawei$agpengine$impl$CoreEnvironmentBackgroundType[CoreEnvironmentBackgroundType.CORE_ENV_BG_IMAGE.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$huawei$agpengine$impl$CoreEnvironmentBackgroundType[CoreEnvironmentBackgroundType.CORE_ENV_BG_CUBEMAP.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$com$huawei$agpengine$impl$CoreEnvironmentBackgroundType[CoreEnvironmentBackgroundType.CORE_ENV_BG_EQUIRECTANGULAR.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$com$huawei$agpengine$components$SceneComponent$BackgroundType[SceneComponent.BackgroundType.NONE.ordinal()] = 1;
            } catch (NoSuchFieldError e5) {
            }
            try {
                $SwitchMap$com$huawei$agpengine$components$SceneComponent$BackgroundType[SceneComponent.BackgroundType.IMAGE.ordinal()] = 2;
            } catch (NoSuchFieldError e6) {
            }
            try {
                $SwitchMap$com$huawei$agpengine$components$SceneComponent$BackgroundType[SceneComponent.BackgroundType.CUBEMAP.ordinal()] = 3;
            } catch (NoSuchFieldError e7) {
            }
            try {
                $SwitchMap$com$huawei$agpengine$components$SceneComponent$BackgroundType[SceneComponent.BackgroundType.EQUIRECTANGULAR.ordinal()] = 4;
            } catch (NoSuchFieldError e8) {
            }
            try {
                $SwitchMap$com$huawei$agpengine$impl$CoreLightType[CoreLightType.CORE_LIGHT_TYPE_INVALID.ordinal()] = 1;
            } catch (NoSuchFieldError e9) {
            }
            try {
                $SwitchMap$com$huawei$agpengine$impl$CoreLightType[CoreLightType.CORE_LIGHT_TYPE_DIRECTIONAL.ordinal()] = 2;
            } catch (NoSuchFieldError e10) {
            }
            try {
                $SwitchMap$com$huawei$agpengine$impl$CoreLightType[CoreLightType.CORE_LIGHT_TYPE_POINT.ordinal()] = 3;
            } catch (NoSuchFieldError e11) {
            }
            try {
                $SwitchMap$com$huawei$agpengine$impl$CoreLightType[CoreLightType.CORE_LIGHT_TYPE_SPOT.ordinal()] = 4;
            } catch (NoSuchFieldError e12) {
            }
            try {
                $SwitchMap$com$huawei$agpengine$components$LightComponent$LightType[LightComponent.LightType.INVALID.ordinal()] = 1;
            } catch (NoSuchFieldError e13) {
            }
            try {
                $SwitchMap$com$huawei$agpengine$components$LightComponent$LightType[LightComponent.LightType.DIRECTIONAL.ordinal()] = 2;
            } catch (NoSuchFieldError e14) {
            }
            try {
                $SwitchMap$com$huawei$agpengine$components$LightComponent$LightType[LightComponent.LightType.POINT.ordinal()] = 3;
            } catch (NoSuchFieldError e15) {
            }
            try {
                $SwitchMap$com$huawei$agpengine$components$LightComponent$LightType[LightComponent.LightType.SPOT.ordinal()] = 4;
            } catch (NoSuchFieldError e16) {
            }
            try {
                $SwitchMap$com$huawei$agpengine$impl$CoreCameraTargetType[CoreCameraTargetType.CORE_CAMERA_TARGET_TYPE_DEFAULT.ordinal()] = 1;
            } catch (NoSuchFieldError e17) {
            }
            try {
                $SwitchMap$com$huawei$agpengine$impl$CoreCameraTargetType[CoreCameraTargetType.CORE_CAMERA_TARGET_TYPE_CUSTOM.ordinal()] = 2;
            } catch (NoSuchFieldError e18) {
            }
            try {
                $SwitchMap$com$huawei$agpengine$components$CameraComponent$CameraTargetType[CameraComponent.CameraTargetType.DEFAULT.ordinal()] = 1;
            } catch (NoSuchFieldError e19) {
            }
            try {
                $SwitchMap$com$huawei$agpengine$components$CameraComponent$CameraTargetType[CameraComponent.CameraTargetType.CUSTOM.ordinal()] = 2;
            } catch (NoSuchFieldError e20) {
            }
            try {
                $SwitchMap$com$huawei$agpengine$impl$CoreCameraType[CoreCameraType.CORE_CAMERA_TYPE_ORTHOGRAPHIC.ordinal()] = 1;
            } catch (NoSuchFieldError e21) {
            }
            try {
                $SwitchMap$com$huawei$agpengine$impl$CoreCameraType[CoreCameraType.CORE_CAMERA_TYPE_PERSPECTIVE.ordinal()] = 2;
            } catch (NoSuchFieldError e22) {
            }
            try {
                $SwitchMap$com$huawei$agpengine$impl$CoreCameraType[CoreCameraType.CORE_CAMERA_TYPE_CUSTOM.ordinal()] = 3;
            } catch (NoSuchFieldError e23) {
            }
            try {
                $SwitchMap$com$huawei$agpengine$components$CameraComponent$CameraType[CameraComponent.CameraType.ORTHOGRAPHIC.ordinal()] = 1;
            } catch (NoSuchFieldError e24) {
            }
            try {
                $SwitchMap$com$huawei$agpengine$components$CameraComponent$CameraType[CameraComponent.CameraType.PERSPECTIVE.ordinal()] = 2;
            } catch (NoSuchFieldError e25) {
            }
            try {
                $SwitchMap$com$huawei$agpengine$components$CameraComponent$CameraType[CameraComponent.CameraType.CUSTOM.ordinal()] = 3;
            } catch (NoSuchFieldError e26) {
            }
        }
    }

    static SceneComponent.BackgroundType getEnvBgType(short swigValue) {
        int i = AnonymousClass1.$SwitchMap$com$huawei$agpengine$impl$CoreEnvironmentBackgroundType[CoreEnvironmentBackgroundType.swigToEnum(swigValue).ordinal()];
        if (i == 1) {
            return SceneComponent.BackgroundType.NONE;
        }
        if (i == 2) {
            return SceneComponent.BackgroundType.IMAGE;
        }
        if (i == 3) {
            return SceneComponent.BackgroundType.CUBEMAP;
        }
        if (i == 4) {
            return SceneComponent.BackgroundType.EQUIRECTANGULAR;
        }
        throw new IllegalArgumentException("Internal graphics engine error");
    }
}
