package ohos.media.camera.mode.tags.hisi;

import java.util.ArrayList;
import ohos.media.camera.device.impl.CameraAbilityImpl;
import ohos.media.camera.mode.adapter.utils.constant.ConstantValue;
import ohos.media.camera.mode.tags.BaseModeTags;
import ohos.media.camera.mode.tags.hisi.utils.HisiBeautyCapabilityUtil;

public class HisiPortraitTags extends BaseModeTags {
    public HisiPortraitTags(CameraAbilityImpl cameraAbilityImpl, int i) {
        this(cameraAbilityImpl, i, BaseModeTags.WorkMode.FULL);
    }

    public HisiPortraitTags(CameraAbilityImpl cameraAbilityImpl, int i, BaseModeTags.WorkMode workMode) {
        super(cameraAbilityImpl, i, workMode);
        this.isSupportedForegroundProcess = true;
        if (workMode == BaseModeTags.WorkMode.FULL) {
            this.skinSmoothLevel = HisiBeautyCapabilityUtil.getUnifySkinSmoothDefaultLevel(cameraAbilityImpl, ConstantValue.MODE_NAME_BEAUTY);
            this.faceSlenderLevel = HisiBeautyCapabilityUtil.getUnifyFaceSlenderDefaultLevel(cameraAbilityImpl, ConstantValue.MODE_NAME_BEAUTY);
            this.skinToneLevel = HisiBeautyCapabilityUtil.getSkinToneDefaultLevel(cameraAbilityImpl, ConstantValue.MODE_NAME_BEAUTY);
            this.bodyShapeLevel = HisiBeautyCapabilityUtil.getBodyShapingDefaultLevel(cameraAbilityImpl, ConstantValue.MODE_NAME_BEAUTY);
            return;
        }
        this.skinSmoothLevel = HisiBeautyCapabilityUtil.getUnifySkinSmoothDefaultLevel(cameraAbilityImpl, ConstantValue.MODE_NAME_BEAUTY);
    }

    public HisiPortraitTags(int i) {
        super(null, i);
        this.isSupportedForegroundProcess = true;
    }

    public static boolean isAvailable(CameraAbilityImpl cameraAbilityImpl) {
        return isAvailable(cameraAbilityImpl, BaseModeTags.WorkMode.FULL);
    }

    public static boolean isAvailable(CameraAbilityImpl cameraAbilityImpl, BaseModeTags.WorkMode workMode) {
        if (workMode == BaseModeTags.WorkMode.FULL) {
            return true;
        }
        return HisiBeautyCapabilityUtil.isUnifySkinSmoothAvailable(cameraAbilityImpl);
    }

    public static int[] getSupportBeautyTypes(CameraAbilityImpl cameraAbilityImpl) {
        ArrayList arrayList = new ArrayList();
        if (isUnifySkinSmoothAvailable(cameraAbilityImpl)) {
            arrayList.add(1);
        }
        if (isUnifyFaceSlenderAvailable(cameraAbilityImpl)) {
            arrayList.add(2);
        }
        if (isSkinToneAvailable(cameraAbilityImpl)) {
            arrayList.add(3);
        }
        if (isBodyShapingAvailable(cameraAbilityImpl)) {
            arrayList.add(4);
        }
        return arrayList.stream().mapToInt($$Lambda$HisiPortraitTags$jPGcJTOgc7EgnZgfBw0zgxGoTX4.INSTANCE).toArray();
    }

    public static boolean isUnifySkinSmoothAvailable(CameraAbilityImpl cameraAbilityImpl) {
        return HisiBeautyCapabilityUtil.isUnifySkinSmoothAvailable(cameraAbilityImpl);
    }

    public static int[] getUnifySkinSmoothRange(CameraAbilityImpl cameraAbilityImpl) {
        return HisiBeautyCapabilityUtil.getUnifySkinSmoothRange(cameraAbilityImpl);
    }

    public static boolean isSkinToneAvailable(CameraAbilityImpl cameraAbilityImpl) {
        return HisiBeautyCapabilityUtil.isSkinToneAvailable(cameraAbilityImpl);
    }

    public static int[] getSkinToneRange(CameraAbilityImpl cameraAbilityImpl) {
        return HisiBeautyCapabilityUtil.getSkinToneRange(cameraAbilityImpl);
    }

    public static boolean isUnifyFaceSlenderAvailable(CameraAbilityImpl cameraAbilityImpl) {
        return HisiBeautyCapabilityUtil.isUnifyFaceSlenderAvailable(cameraAbilityImpl);
    }

    public static int[] getUnifyFaceSlenderRange(CameraAbilityImpl cameraAbilityImpl) {
        return HisiBeautyCapabilityUtil.getUnifyFaceSlenderRange(cameraAbilityImpl);
    }

    public static boolean isBodyShapingAvailable(CameraAbilityImpl cameraAbilityImpl) {
        return HisiBeautyCapabilityUtil.isBodyShapingAvailable(cameraAbilityImpl);
    }

    public static int[] getBodyShapingRange(CameraAbilityImpl cameraAbilityImpl) {
        return HisiBeautyCapabilityUtil.getBodyShapingRange(cameraAbilityImpl);
    }

    @Override // ohos.media.camera.mode.tags.BaseModeTags, ohos.media.camera.mode.tags.ModeTags
    public boolean isBeautyEnabled() {
        if (this.skinSmoothLevel != 0 && isUnifySkinSmoothAvailable(this.cameraAbility)) {
            return true;
        }
        if (this.faceSlenderLevel != 0 && isUnifyFaceSlenderAvailable(this.cameraAbility)) {
            return true;
        }
        if (this.skinToneLevel != 0 && isSkinToneAvailable(this.cameraAbility)) {
            return true;
        }
        if (this.bodyShapeLevel == 0 || !isBodyShapingAvailable(this.cameraAbility)) {
            return false;
        }
        return true;
    }
}
