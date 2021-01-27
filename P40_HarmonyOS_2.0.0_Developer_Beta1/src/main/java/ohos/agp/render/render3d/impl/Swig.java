package ohos.agp.render.render3d.impl;

import ohos.agp.render.render3d.components.CameraComponent;
import ohos.agp.render.render3d.components.LightComponent;
import ohos.agp.render.render3d.components.SceneComponent;
import ohos.agp.render.render3d.math.Quaternion;
import ohos.agp.render.render3d.math.Vector2;
import ohos.agp.render.render3d.math.Vector3;
import ohos.agp.render.render3d.math.Vector4;

/* access modifiers changed from: package-private */
public class Swig {
    private Swig() {
    }

    static Vector2 get(CoreVec2 coreVec2) {
        if (coreVec2 != null) {
            return new Vector2(coreVec2.getX(), coreVec2.getY());
        }
        return Vector2.ZERO;
    }

    static Vector3 get(CoreVec3 coreVec3) {
        if (coreVec3 != null) {
            return new Vector3(coreVec3.getX(), coreVec3.getY(), coreVec3.getZ());
        }
        return Vector3.ZERO;
    }

    static Vector4 get(CoreVec4 coreVec4) {
        if (coreVec4 != null) {
            return new Vector4(coreVec4.getX(), coreVec4.getY(), coreVec4.getZ(), coreVec4.getW());
        }
        return Vector4.ZERO;
    }

    static Quaternion get(CoreQuat coreQuat) {
        if (coreQuat != null) {
            return new Quaternion(coreQuat.getX(), coreQuat.getY(), coreQuat.getZ(), coreQuat.getW());
        }
        return Quaternion.ZERO;
    }

    static CoreVec2 set(Vector2 vector2) {
        return new CoreVec2(vector2.getX(), vector2.getY());
    }

    static CoreVec3 set(Vector3 vector3) {
        return new CoreVec3(vector3.getX(), vector3.getY(), vector3.getZ());
    }

    static CoreVec4 set(Vector4 vector4) {
        return new CoreVec4(vector4.getX(), vector4.getY(), vector4.getZ(), vector4.getW());
    }

    static CoreQuat set(Quaternion quaternion) {
        return new CoreQuat(quaternion.getX(), quaternion.getY(), quaternion.getZ(), quaternion.getW());
    }

    static short getNativeCameraType(CameraComponent.CameraType cameraType) {
        int swigValue;
        int i = AnonymousClass1.$SwitchMap$ohos$agp$render$render3d$components$CameraComponent$CameraType[cameraType.ordinal()];
        if (i == 1) {
            swigValue = CoreCameraType.CORE_CAMERA_TYPE_ORTHOGRAPHIC.swigValue();
        } else if (i == 2) {
            swigValue = CoreCameraType.CORE_CAMERA_TYPE_PERSPECTIVE.swigValue();
        } else if (i == 3) {
            swigValue = CoreCameraType.CORE_CAMERA_TYPE_CUSTOM.swigValue();
        } else {
            throw new IllegalArgumentException();
        }
        return (short) swigValue;
    }

    static CameraComponent.CameraType getCameraType(short s) {
        int i = AnonymousClass1.$SwitchMap$ohos$agp$render$render3d$impl$CoreCameraType[CoreCameraType.swigToEnum(s).ordinal()];
        if (i == 1) {
            return CameraComponent.CameraType.ORTHOGRAPHIC;
        }
        if (i == 2) {
            return CameraComponent.CameraType.PERSPECTIVE;
        }
        if (i == 3) {
            return CameraComponent.CameraType.CUSTOM;
        }
        throw new IllegalArgumentException();
    }

    static short getNativeLightType(LightComponent.LightType lightType) {
        int swigValue;
        int i = AnonymousClass1.$SwitchMap$ohos$agp$render$render3d$components$LightComponent$LightType[lightType.ordinal()];
        if (i == 1) {
            swigValue = CoreLightType.CORE_LIGHT_TYPE_INVALID.swigValue();
        } else if (i == 2) {
            swigValue = CoreLightType.CORE_LIGHT_TYPE_DIRECTIONAL.swigValue();
        } else if (i == 3) {
            swigValue = CoreLightType.CORE_LIGHT_TYPE_POINT.swigValue();
        } else if (i == 4) {
            swigValue = CoreLightType.CORE_LIGHT_TYPE_SPOT.swigValue();
        } else {
            throw new IllegalArgumentException();
        }
        return (short) swigValue;
    }

    static LightComponent.LightType getLightType(short s) {
        int i = AnonymousClass1.$SwitchMap$ohos$agp$render$render3d$impl$CoreLightType[CoreLightType.swigToEnum(s).ordinal()];
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
        throw new IllegalArgumentException();
    }

    static short getNativeEnvBgType(SceneComponent.BackgroundType backgroundType) {
        int swigValue;
        int i = AnonymousClass1.$SwitchMap$ohos$agp$render$render3d$components$SceneComponent$BackgroundType[backgroundType.ordinal()];
        if (i == 1) {
            swigValue = CoreEnvironmentBackgroundType.CORE_ENV_BG_NONE.swigValue();
        } else if (i == 2) {
            swigValue = CoreEnvironmentBackgroundType.CORE_ENV_BG_IMAGE.swigValue();
        } else if (i == 3) {
            swigValue = CoreEnvironmentBackgroundType.CORE_ENV_BG_CUBEMAP.swigValue();
        } else if (i == 4) {
            swigValue = CoreEnvironmentBackgroundType.CORE_ENV_BG_EQUIRECTANGULAR.swigValue();
        } else {
            throw new IllegalArgumentException();
        }
        return (short) swigValue;
    }

    /* access modifiers changed from: package-private */
    /* renamed from: ohos.agp.render.render3d.impl.Swig$1  reason: invalid class name */
    public static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$ohos$agp$render$render3d$components$CameraComponent$CameraType = new int[CameraComponent.CameraType.values().length];
        static final /* synthetic */ int[] $SwitchMap$ohos$agp$render$render3d$components$LightComponent$LightType = new int[LightComponent.LightType.values().length];
        static final /* synthetic */ int[] $SwitchMap$ohos$agp$render$render3d$components$SceneComponent$BackgroundType = new int[SceneComponent.BackgroundType.values().length];
        static final /* synthetic */ int[] $SwitchMap$ohos$agp$render$render3d$impl$CoreCameraType = new int[CoreCameraType.values().length];
        static final /* synthetic */ int[] $SwitchMap$ohos$agp$render$render3d$impl$CoreEnvironmentBackgroundType = new int[CoreEnvironmentBackgroundType.values().length];
        static final /* synthetic */ int[] $SwitchMap$ohos$agp$render$render3d$impl$CoreLightType = new int[CoreLightType.values().length];

        static {
            try {
                $SwitchMap$ohos$agp$render$render3d$impl$CoreEnvironmentBackgroundType[CoreEnvironmentBackgroundType.CORE_ENV_BG_NONE.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$ohos$agp$render$render3d$impl$CoreEnvironmentBackgroundType[CoreEnvironmentBackgroundType.CORE_ENV_BG_IMAGE.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                $SwitchMap$ohos$agp$render$render3d$impl$CoreEnvironmentBackgroundType[CoreEnvironmentBackgroundType.CORE_ENV_BG_CUBEMAP.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
            try {
                $SwitchMap$ohos$agp$render$render3d$impl$CoreEnvironmentBackgroundType[CoreEnvironmentBackgroundType.CORE_ENV_BG_EQUIRECTANGULAR.ordinal()] = 4;
            } catch (NoSuchFieldError unused4) {
            }
            try {
                $SwitchMap$ohos$agp$render$render3d$components$SceneComponent$BackgroundType[SceneComponent.BackgroundType.NONE.ordinal()] = 1;
            } catch (NoSuchFieldError unused5) {
            }
            try {
                $SwitchMap$ohos$agp$render$render3d$components$SceneComponent$BackgroundType[SceneComponent.BackgroundType.IMAGE.ordinal()] = 2;
            } catch (NoSuchFieldError unused6) {
            }
            try {
                $SwitchMap$ohos$agp$render$render3d$components$SceneComponent$BackgroundType[SceneComponent.BackgroundType.CUBEMAP.ordinal()] = 3;
            } catch (NoSuchFieldError unused7) {
            }
            try {
                $SwitchMap$ohos$agp$render$render3d$components$SceneComponent$BackgroundType[SceneComponent.BackgroundType.EQUIRECTANGULAR.ordinal()] = 4;
            } catch (NoSuchFieldError unused8) {
            }
            try {
                $SwitchMap$ohos$agp$render$render3d$impl$CoreLightType[CoreLightType.CORE_LIGHT_TYPE_INVALID.ordinal()] = 1;
            } catch (NoSuchFieldError unused9) {
            }
            try {
                $SwitchMap$ohos$agp$render$render3d$impl$CoreLightType[CoreLightType.CORE_LIGHT_TYPE_DIRECTIONAL.ordinal()] = 2;
            } catch (NoSuchFieldError unused10) {
            }
            try {
                $SwitchMap$ohos$agp$render$render3d$impl$CoreLightType[CoreLightType.CORE_LIGHT_TYPE_POINT.ordinal()] = 3;
            } catch (NoSuchFieldError unused11) {
            }
            try {
                $SwitchMap$ohos$agp$render$render3d$impl$CoreLightType[CoreLightType.CORE_LIGHT_TYPE_SPOT.ordinal()] = 4;
            } catch (NoSuchFieldError unused12) {
            }
            try {
                $SwitchMap$ohos$agp$render$render3d$components$LightComponent$LightType[LightComponent.LightType.INVALID.ordinal()] = 1;
            } catch (NoSuchFieldError unused13) {
            }
            try {
                $SwitchMap$ohos$agp$render$render3d$components$LightComponent$LightType[LightComponent.LightType.DIRECTIONAL.ordinal()] = 2;
            } catch (NoSuchFieldError unused14) {
            }
            try {
                $SwitchMap$ohos$agp$render$render3d$components$LightComponent$LightType[LightComponent.LightType.POINT.ordinal()] = 3;
            } catch (NoSuchFieldError unused15) {
            }
            try {
                $SwitchMap$ohos$agp$render$render3d$components$LightComponent$LightType[LightComponent.LightType.SPOT.ordinal()] = 4;
            } catch (NoSuchFieldError unused16) {
            }
            try {
                $SwitchMap$ohos$agp$render$render3d$impl$CoreCameraType[CoreCameraType.CORE_CAMERA_TYPE_ORTHOGRAPHIC.ordinal()] = 1;
            } catch (NoSuchFieldError unused17) {
            }
            try {
                $SwitchMap$ohos$agp$render$render3d$impl$CoreCameraType[CoreCameraType.CORE_CAMERA_TYPE_PERSPECTIVE.ordinal()] = 2;
            } catch (NoSuchFieldError unused18) {
            }
            try {
                $SwitchMap$ohos$agp$render$render3d$impl$CoreCameraType[CoreCameraType.CORE_CAMERA_TYPE_CUSTOM.ordinal()] = 3;
            } catch (NoSuchFieldError unused19) {
            }
            try {
                $SwitchMap$ohos$agp$render$render3d$components$CameraComponent$CameraType[CameraComponent.CameraType.ORTHOGRAPHIC.ordinal()] = 1;
            } catch (NoSuchFieldError unused20) {
            }
            try {
                $SwitchMap$ohos$agp$render$render3d$components$CameraComponent$CameraType[CameraComponent.CameraType.PERSPECTIVE.ordinal()] = 2;
            } catch (NoSuchFieldError unused21) {
            }
            try {
                $SwitchMap$ohos$agp$render$render3d$components$CameraComponent$CameraType[CameraComponent.CameraType.CUSTOM.ordinal()] = 3;
            } catch (NoSuchFieldError unused22) {
            }
        }
    }

    static SceneComponent.BackgroundType getEnvBgType(short s) {
        int i = AnonymousClass1.$SwitchMap$ohos$agp$render$render3d$impl$CoreEnvironmentBackgroundType[CoreEnvironmentBackgroundType.swigToEnum(s).ordinal()];
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
        throw new IllegalArgumentException();
    }
}
