package com.huawei.hwtransition.interfaces;

import android.animation.TimeInterpolator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.Log;
import android.view.View;
import dalvik.system.PathClassLoader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class HwTransitionReflection {
    private static final String CLASS_NAME = "com.huawei.hwtransition.HwTransition";
    private static final String JAR_PAHT = "/system/framework/hwtransition.jar";
    private static final String TAG = "HwTransitionReflection";
    public static final String TRANS_TYPE_BOX = "Box";
    public static final String TRANS_TYPE_CYLINDER = "Cylinder";
    public static final String TRANS_TYPE_DEPTH = "Depth";
    public static final String TRANS_TYPE_FLIPOVER = "Flipover";
    public static final String TRANS_TYPE_NORMAL = "Normal";
    public static final String TRANS_TYPE_PAGE = "Page";
    public static final String TRANS_TYPE_PENDULUM = "Pendulum";
    public static final String TRANS_TYPE_PUSH = "Push";
    public static final String TRANS_TYPE_ROTATION = "Rotation";
    public static final String TRANS_TYPE_WINDMILL = "Windmill";
    private Method mAnimateDispatchDrawMethod;
    private Method mAnimateDrawMethod;
    private Class<?> mClazz;
    private Object mHwTransitionInstance;
    private Method mIs3DAnimationMethod;
    private boolean mIsBerr;
    private Method mSetAnimationFPSMethod;
    private Method mSetBackgroundMethod;
    private Method mSetCameraDistanceMethod;
    private Method mSetInterpolatorMethod;
    private Method mSetLayerTransparentMethod;
    private Method mSetTransitionTypeMethod;
    private Method mStartAnimationMethod;
    private Method mStartViewAnimationMethod;
    private Method msetAlphaModeMethod;

    public HwTransitionReflection(View targetView, String type) {
        this(targetView, type, 0);
    }

    HwTransitionReflection(View targetView, String type, int pageSpacing) {
        this.mClazz = null;
        this.mHwTransitionInstance = null;
        this.mSetTransitionTypeMethod = null;
        this.mAnimateDrawMethod = null;
        this.mStartAnimationMethod = null;
        this.mSetCameraDistanceMethod = null;
        this.mIs3DAnimationMethod = null;
        this.mAnimateDispatchDrawMethod = null;
        this.mSetLayerTransparentMethod = null;
        this.mSetBackgroundMethod = null;
        this.msetAlphaModeMethod = null;
        this.mSetAnimationFPSMethod = null;
        this.mStartViewAnimationMethod = null;
        this.mSetInterpolatorMethod = null;
        this.mIsBerr = false;
        getHwTransitionClass(targetView.getContext());
        getHwTransitionInstance(targetView, type, pageSpacing);
        initDeclaredMethods();
    }

    public void setTransitionType(String type) {
        if (this.mSetTransitionTypeMethod != null) {
            try {
                this.mSetTransitionTypeMethod.invoke(this.mHwTransitionInstance, type);
            } catch (IllegalAccessException | InvocationTargetException e) {
                if (this.mIsBerr) {
                    this.mIsBerr = false;
                    Log.e(TAG, "Catch exception when call setTransitionType()");
                }
            }
        }
    }

    public void setCameraDistance(float distanceCamera) {
        if (this.mSetCameraDistanceMethod != null) {
            try {
                this.mSetCameraDistanceMethod.invoke(this.mHwTransitionInstance, Float.valueOf(distanceCamera));
            } catch (IllegalAccessException | InvocationTargetException e) {
                if (this.mIsBerr) {
                    this.mIsBerr = false;
                    Log.e(TAG, "Catch exception when call setCameraDistance(), error = ");
                }
            }
        }
    }

    public boolean startAnimation(View view) {
        if (this.mStartAnimationMethod == null) {
            return false;
        }
        try {
            if (this.mStartAnimationMethod.invoke(this.mHwTransitionInstance, view) instanceof Boolean) {
                return ((Boolean) this.mStartAnimationMethod.invoke(this.mHwTransitionInstance, view)).booleanValue();
            }
            return false;
        } catch (IllegalAccessException | InvocationTargetException e) {
            if (!this.mIsBerr) {
                return false;
            }
            this.mIsBerr = false;
            Log.e(TAG, "Catch exception when call startAnimation()");
            return false;
        }
    }

    public boolean startViewAnimation(View view) {
        if (this.mStartViewAnimationMethod == null) {
            return false;
        }
        try {
            if (this.mStartViewAnimationMethod.invoke(this.mHwTransitionInstance, view) instanceof Boolean) {
                return ((Boolean) this.mStartViewAnimationMethod.invoke(this.mHwTransitionInstance, view)).booleanValue();
            }
            return false;
        } catch (IllegalAccessException | InvocationTargetException e) {
            if (!this.mIsBerr) {
                return false;
            }
            this.mIsBerr = false;
            Log.e(TAG, "Catch exception when call startViewAnimation()");
            return false;
        }
    }

    public boolean animateDraw(Canvas canvas) {
        if (this.mAnimateDrawMethod == null) {
            return false;
        }
        try {
            if (this.mAnimateDrawMethod.invoke(this.mHwTransitionInstance, canvas) instanceof Boolean) {
                return ((Boolean) this.mAnimateDrawMethod.invoke(this.mHwTransitionInstance, canvas)).booleanValue();
            }
            return false;
        } catch (IllegalAccessException | InvocationTargetException e) {
            if (!this.mIsBerr) {
                return false;
            }
            this.mIsBerr = false;
            Log.e(TAG, "Catch exception when call animateDraw()");
            return false;
        }
    }

    public boolean is3DAnimation() {
        if (this.mIs3DAnimationMethod == null) {
            return false;
        }
        try {
            if (this.mIs3DAnimationMethod.invoke(this.mHwTransitionInstance, new Object[0]) instanceof Boolean) {
                return ((Boolean) this.mIs3DAnimationMethod.invoke(this.mHwTransitionInstance, new Object[0])).booleanValue();
            }
            return false;
        } catch (IllegalAccessException | InvocationTargetException e) {
            if (!this.mIsBerr) {
                return false;
            }
            this.mIsBerr = false;
            Log.e(TAG, "Exception happens when call is3DAnimation()");
            return false;
        }
    }

    public boolean animateDispatchDraw(Canvas canvas, int translationX, boolean isScrolling) {
        if (this.mAnimateDispatchDrawMethod == null) {
            return false;
        }
        try {
            if (this.mAnimateDispatchDrawMethod.invoke(this.mHwTransitionInstance, canvas, Integer.valueOf(translationX), Boolean.valueOf(isScrolling)) instanceof Boolean) {
                return ((Boolean) this.mAnimateDispatchDrawMethod.invoke(this.mHwTransitionInstance, canvas, Integer.valueOf(translationX), Boolean.valueOf(isScrolling))).booleanValue();
            }
            return false;
        } catch (IllegalAccessException | InvocationTargetException e) {
            if (!this.mIsBerr) {
                return false;
            }
            this.mIsBerr = false;
            Log.e(TAG, "Exception happens when call animateDispatchDraw()");
            return false;
        }
    }

    public void setAnimationFPS(int minfps) {
        if (minfps > 0 && this.mSetAnimationFPSMethod != null) {
            try {
                this.mSetAnimationFPSMethod.invoke(this.mHwTransitionInstance, Integer.valueOf(minfps));
            } catch (IllegalAccessException | InvocationTargetException e) {
                if (this.mIsBerr) {
                    this.mIsBerr = false;
                    Log.e(TAG, "Error happens when call setAnimationFPS()");
                }
            }
        }
    }

    public void setInterpolator(TimeInterpolator interpolator) {
        if (this.mSetInterpolatorMethod != null) {
            try {
                this.mSetInterpolatorMethod.invoke(this.mHwTransitionInstance, interpolator);
            } catch (IllegalAccessException | InvocationTargetException e) {
                if (this.mIsBerr) {
                    this.mIsBerr = false;
                    Log.e(TAG, "Catch exception when call is3DAnimation()");
                }
            }
        }
    }

    public void setLayerTransparent(boolean isTransparent) {
        if (this.mSetLayerTransparentMethod != null) {
            try {
                this.mSetLayerTransparentMethod.invoke(this.mHwTransitionInstance, Boolean.valueOf(isTransparent));
            } catch (IllegalAccessException | InvocationTargetException e) {
                if (this.mIsBerr) {
                    this.mIsBerr = false;
                    Log.e(TAG, "Exception happens when call setAnimationFPS()");
                }
            }
        }
    }

    public void setBackground(Bitmap background) {
        if (this.mSetBackgroundMethod != null) {
            try {
                this.mSetBackgroundMethod.invoke(this.mHwTransitionInstance, background);
            } catch (IllegalAccessException | InvocationTargetException e) {
                if (this.mIsBerr) {
                    this.mIsBerr = false;
                    Log.e(TAG, "Exception happens when call setAnimationFPS()");
                }
            }
        }
    }

    public void setAlphaMode(boolean isUseAlpha) {
        if (this.msetAlphaModeMethod != null) {
            try {
                this.msetAlphaModeMethod.invoke(this.mHwTransitionInstance, Boolean.valueOf(isUseAlpha));
            } catch (IllegalAccessException | InvocationTargetException e) {
                if (this.mIsBerr) {
                    this.mIsBerr = false;
                    Log.e(TAG, "Catch exception when call setAlphaMode()");
                }
            }
        }
    }

    private void getHwTransitionClass(Context context) {
        try {
            this.mClazz = new PathClassLoader(JAR_PAHT, context.getClassLoader()).loadClass(CLASS_NAME);
        } catch (ClassNotFoundException e) {
            if (this.mIsBerr) {
                this.mIsBerr = false;
                Log.e(TAG, "Catch exception when call getHwTransitionClass()");
            }
        }
    }

    private void getHwTransitionInstance(View targetView, String type, int pageSpacing) {
        Class<?>[] mConstructorSignatures = {View.class, String.class, Integer.TYPE};
        if (this.mClazz != null) {
            try {
                this.mHwTransitionInstance = this.mClazz.getConstructor(mConstructorSignatures).newInstance(targetView, type, Integer.valueOf(pageSpacing));
                Log.d("wangshx", "mHwTransitionInstance = " + this.mHwTransitionInstance);
            } catch (IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException e) {
                Log.e(TAG, "Exception happens when call getHwTransitionInstance()");
            }
        }
    }

    private void initDeclaredMethods() {
        if (this.mClazz != null) {
            try {
                this.mSetTransitionTypeMethod = this.mClazz.getDeclaredMethod("setTransitionType", String.class);
                this.mAnimateDrawMethod = this.mClazz.getDeclaredMethod("animateDraw", Canvas.class);
                Class<?>[] startAnimationArgs = {View.class};
                this.mStartAnimationMethod = this.mClazz.getDeclaredMethod("startAnimation", startAnimationArgs);
                this.mSetCameraDistanceMethod = this.mClazz.getDeclaredMethod("setCameraDistance", Float.TYPE);
                this.mIs3DAnimationMethod = this.mClazz.getDeclaredMethod("is3DAnimation", new Class[0]);
                this.mAnimateDispatchDrawMethod = this.mClazz.getDeclaredMethod("animateDispatchDraw", Canvas.class, Integer.TYPE, Boolean.TYPE);
                this.mSetLayerTransparentMethod = this.mClazz.getDeclaredMethod("setLayerTransparent", Boolean.TYPE);
                this.mSetBackgroundMethod = this.mClazz.getDeclaredMethod("setBackground", Bitmap.class);
                this.msetAlphaModeMethod = this.mClazz.getDeclaredMethod("setAlphaMode", Boolean.TYPE);
                this.mSetAnimationFPSMethod = this.mClazz.getDeclaredMethod("setAnimationFPS", Integer.TYPE);
                this.mStartViewAnimationMethod = this.mClazz.getDeclaredMethod("startViewAnimation", startAnimationArgs);
                this.mSetInterpolatorMethod = this.mClazz.getDeclaredMethod("setInterpolator", TimeInterpolator.class);
            } catch (NoSuchMethodException e) {
                Log.e(TAG, "Exception happens when call initDeclaredMethods()");
            }
        }
    }
}
