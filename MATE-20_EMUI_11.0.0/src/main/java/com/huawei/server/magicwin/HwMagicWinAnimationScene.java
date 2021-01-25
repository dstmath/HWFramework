package com.huawei.server.magicwin;

import com.huawei.android.util.SlogEx;

public class HwMagicWinAnimationScene {
    public static final int SCENE_ANAN_MASTER_TO_SLAVE = 104;
    public static final int SCENE_DEFAULT = -1;
    public static final int SCENE_EXIT = 100;
    public static final int SCENE_EXIT_BY_MAGIC_WINDOW = 106;
    public static final int SCENE_EXIT_MASTER_TO_SLAVE = 105;
    public static final int SCENE_EXIT_SLAVE_TO_SLAVE = 103;
    public static final int SCENE_MASTER_TO_SLAVE = 0;
    public static final int SCENE_MIDDLE = 101;
    public static final int SCENE_MIDDLE_TO_SLAVE = 2;
    public static final int SCENE_SLAVE_TO_MASTER = 200;
    public static final int SCENE_SLAVE_TO_SLAVE = 1;
    public static final int SCENE_START_APP = 102;
    private static final String TAG = "HWMW_HwMagicWinAnimationScene";
    private static int sLastStartScene = -1;

    public interface AnimationScene {
        int getAnimationScene();
    }

    HwMagicWinAnimationScene() {
    }

    public static class BaseAnimationScene implements AnimationScene {
        protected boolean mIsTransition;
        protected int mScene;
        protected int mTargetPosition;

        public void setTargetPosition(int targetPosition) {
            this.mTargetPosition = targetPosition;
        }

        public void setTransition(boolean isTransition) {
            this.mIsTransition = isTransition;
        }

        public AnimationScene calculatedAnimationScene() {
            return this;
        }

        @Override // com.huawei.server.magicwin.HwMagicWinAnimationScene.AnimationScene
        public int getAnimationScene() {
            return this.mScene;
        }
    }

    public static class StartAnimationScene extends BaseAnimationScene {
        private int mFocusSourcePosition;
        private int mFocusTargetPosition;
        private boolean mIsAllDrawn;
        private int mMagicMode;

        public void setFocusSourcePosition(int focusSourcePosition) {
            this.mFocusSourcePosition = focusSourcePosition;
        }

        public void setFocusTargetPosition(int focusTargetPosition) {
            this.mFocusTargetPosition = focusTargetPosition;
        }

        public void setMagicMode(int magicMode) {
            this.mMagicMode = magicMode;
        }

        public void setAllDrawn(boolean isAllDrawn) {
            this.mIsAllDrawn = isAllDrawn;
        }

        @Override // com.huawei.server.magicwin.HwMagicWinAnimationScene.BaseAnimationScene
        public AnimationScene calculatedAnimationScene() {
            if (this.mIsTransition) {
                this.mScene = getTransitionScne();
            } else {
                this.mScene = getStartAnimationScene();
            }
            int unused = HwMagicWinAnimationScene.sLastStartScene = this.mScene;
            return this;
        }

        private int getStartAnimationScene() {
            int i = this.mTargetPosition;
            if (i == 2) {
                int i2 = this.mFocusTargetPosition;
                if (i2 == 1) {
                    int i3 = this.mFocusSourcePosition;
                    if (i3 == 2) {
                        return 200;
                    }
                    if (i3 == 3) {
                        return 2;
                    }
                    if (this.mMagicMode == 2) {
                        return HwMagicWinAnimationScene.SCENE_ANAN_MASTER_TO_SLAVE;
                    }
                    return 0;
                } else if (i2 != 2) {
                    return -1;
                } else {
                    return 1;
                }
            } else if (i != 3) {
                return -1;
            } else {
                int i4 = this.mFocusTargetPosition;
                if (i4 == 5 || i4 == 0) {
                    return HwMagicWinAnimationScene.SCENE_START_APP;
                }
                if (i4 == 3) {
                    return HwMagicWinAnimationScene.SCENE_MIDDLE;
                }
                return -1;
            }
        }

        private int getTransitionScne() {
            SlogEx.d(HwMagicWinAnimationScene.TAG, "focus activity is transition activity");
            if (this.mIsAllDrawn) {
                return -1;
            }
            if (HwMagicWinAnimationScene.sLastStartScene == 0 || HwMagicWinAnimationScene.sLastStartScene == 104 || HwMagicWinAnimationScene.sLastStartScene == 200) {
                return HwMagicWinAnimationScene.sLastStartScene;
            }
            return -1;
        }

        public String toString() {
            return "StartAnimationScene{ mFocusSourcePosition = " + this.mFocusSourcePosition + ", mFocusTargetPosition = " + this.mFocusTargetPosition + ", mTargetPosition = " + this.mTargetPosition + ", isTransition = " + this.mIsTransition + ", mMagicMode = " + this.mMagicMode + ", isAllDrawn = " + this.mIsAllDrawn + "}";
        }
    }

    public static class FinishAnimationScene extends BaseAnimationScene {
        private boolean mIsExitSliding;
        private boolean mIsMastersFinish;

        public void setExitSliding(boolean isExitSliding) {
            this.mIsExitSliding = isExitSliding;
        }

        public void setMastersFinish(boolean isMastersFinish) {
            this.mIsMastersFinish = isMastersFinish;
        }

        @Override // com.huawei.server.magicwin.HwMagicWinAnimationScene.BaseAnimationScene
        public AnimationScene calculatedAnimationScene() {
            if (!this.mIsTransition || this.mIsMastersFinish) {
                if (this.mTargetPosition == 2 && this.mIsExitSliding) {
                    this.mScene = HwMagicWinAnimationScene.SCENE_EXIT_SLAVE_TO_SLAVE;
                } else if (this.mTargetPosition == 1 && this.mIsMastersFinish) {
                    this.mScene = HwMagicWinAnimationScene.SCENE_EXIT_MASTER_TO_SLAVE;
                } else if (this.mTargetPosition != 2 || !this.mIsMastersFinish) {
                    this.mScene = 100;
                } else {
                    this.mScene = HwMagicWinAnimationScene.SCENE_EXIT_BY_MAGIC_WINDOW;
                }
                return this;
            }
            this.mScene = -1;
            return this;
        }

        public String toString() {
            return "FinishAnimationScene{ mTargetPosition = " + this.mTargetPosition + ", isTransition = " + this.mIsTransition + ", isExitSliding = " + this.mIsExitSliding + ", isMastersFinish = " + this.mIsMastersFinish + "}";
        }
    }
}
