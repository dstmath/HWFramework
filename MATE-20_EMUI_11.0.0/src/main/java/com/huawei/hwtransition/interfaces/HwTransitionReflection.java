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
    private Object HwTransitionInstance;
    private Class<?> clazz;
    private Method mAnimateDispatchDrawMethod;
    private Method mAnimateDrawMethod;
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
        this.clazz = null;
        this.HwTransitionInstance = null;
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
                this.mSetTransitionTypeMethod.invoke(this.HwTransitionInstance, type);
            } catch (IllegalAccessException | InvocationTargetException e) {
                if (this.mIsBerr) {
                    this.mIsBerr = false;
                    Log.e(TAG, "Catch exception when call setTransitionType(), error = " + e.getMessage());
                }
            }
        }
    }

    public void setCameraDistance(float z) {
        if (this.mSetCameraDistanceMethod != null) {
            try {
                this.mSetCameraDistanceMethod.invoke(this.HwTransitionInstance, Float.valueOf(z));
            } catch (IllegalAccessException | InvocationTargetException e) {
                if (this.mIsBerr) {
                    this.mIsBerr = false;
                    Log.e(TAG, "Catch exception when call setCameraDistance(), error = " + e.getMessage());
                }
            }
        }
    }

    public boolean startAnimation(View view) {
        if (this.mStartAnimationMethod == null) {
            return false;
        }
        try {
            return ((Boolean) this.mStartAnimationMethod.invoke(this.HwTransitionInstance, view)).booleanValue();
        } catch (IllegalAccessException | InvocationTargetException e) {
            if (!this.mIsBerr) {
                return false;
            }
            this.mIsBerr = false;
            Log.e(TAG, "Catch exception when call startAnimation(), error = " + e.getMessage());
            return false;
        }
    }

    public boolean startViewAnimation(View view) {
        if (this.mStartViewAnimationMethod == null) {
            return false;
        }
        try {
            return ((Boolean) this.mStartViewAnimationMethod.invoke(this.HwTransitionInstance, view)).booleanValue();
        } catch (IllegalAccessException | InvocationTargetException e) {
            if (!this.mIsBerr) {
                return false;
            }
            this.mIsBerr = false;
            Log.e(TAG, "Catch exception when call startViewAnimation(), error = " + e.getMessage());
            return false;
        }
    }

    public boolean animateDraw(Canvas canvas) {
        if (this.mAnimateDrawMethod == null) {
            return false;
        }
        try {
            return ((Boolean) this.mAnimateDrawMethod.invoke(this.HwTransitionInstance, canvas)).booleanValue();
        } catch (IllegalAccessException | InvocationTargetException e) {
            if (!this.mIsBerr) {
                return false;
            }
            this.mIsBerr = false;
            Log.e(TAG, "Catch exception when call animateDraw(), error = " + e.getMessage());
            return false;
        }
    }

    public boolean is3DAnimation() {
        if (this.mIs3DAnimationMethod == null) {
            return false;
        }
        try {
            return ((Boolean) this.mIs3DAnimationMethod.invoke(this.HwTransitionInstance, new Object[0])).booleanValue();
        } catch (IllegalAccessException | InvocationTargetException e) {
            if (!this.mIsBerr) {
                return false;
            }
            this.mIsBerr = false;
            Log.e(TAG, "Exception happens when call is3DAnimation(), error = " + e.getMessage());
            return false;
        }
    }

    public boolean animateDispatchDraw(Canvas canvas, int translationX, boolean isScrolling) {
        if (this.mAnimateDispatchDrawMethod == null) {
            return false;
        }
        try {
            return ((Boolean) this.mAnimateDispatchDrawMethod.invoke(this.HwTransitionInstance, canvas, Integer.valueOf(translationX), Boolean.valueOf(isScrolling))).booleanValue();
        } catch (IllegalAccessException | InvocationTargetException e) {
            if (!this.mIsBerr) {
                return false;
            }
            this.mIsBerr = false;
            Log.e(TAG, "Exception happens when call animateDispatchDraw(), error = " + e.getMessage());
            return false;
        }
    }

    public void setAnimationFPS(int minfps) {
        if (minfps > 0 && this.mSetAnimationFPSMethod != null) {
            try {
                this.mSetAnimationFPSMethod.invoke(this.HwTransitionInstance, Integer.valueOf(minfps));
            } catch (IllegalAccessException | InvocationTargetException e) {
                if (this.mIsBerr) {
                    this.mIsBerr = false;
                    Log.e(TAG, "Error happens when call setAnimationFPS(), error = " + e.getMessage());
                }
            }
        }
    }

    public void setInterpolator(TimeInterpolator interpolator) {
        if (this.mSetInterpolatorMethod != null) {
            try {
                this.mSetInterpolatorMethod.invoke(this.HwTransitionInstance, interpolator);
            } catch (IllegalAccessException | InvocationTargetException e) {
                if (this.mIsBerr) {
                    this.mIsBerr = false;
                    Log.e(TAG, "Catch exception when call is3DAnimation(), error = " + e.getMessage());
                }
            }
        }
    }

    public void setLayerTransparent(boolean isTransparent) {
        if (this.mSetLayerTransparentMethod != null) {
            try {
                this.mSetLayerTransparentMethod.invoke(this.HwTransitionInstance, Boolean.valueOf(isTransparent));
            } catch (IllegalAccessException | InvocationTargetException e) {
                if (this.mIsBerr) {
                    this.mIsBerr = false;
                    Log.e(TAG, "Exception happens when call setAnimationFPS(), error = " + e.getMessage());
                }
            }
        }
    }

    public void setBackground(Bitmap background) {
        if (this.mSetBackgroundMethod != null) {
            try {
                this.mSetBackgroundMethod.invoke(this.HwTransitionInstance, background);
            } catch (IllegalAccessException | InvocationTargetException e) {
                if (this.mIsBerr) {
                    this.mIsBerr = false;
                    Log.e(TAG, "Exception happens when call setAnimationFPS(), error = " + e.getMessage());
                }
            }
        }
    }

    public void setAlphaMode(boolean isUseAlpha) {
        if (this.msetAlphaModeMethod != null) {
            try {
                this.msetAlphaModeMethod.invoke(this.HwTransitionInstance, Boolean.valueOf(isUseAlpha));
            } catch (IllegalAccessException | InvocationTargetException e) {
                if (this.mIsBerr) {
                    this.mIsBerr = false;
                    Log.e(TAG, "Catch exception when call setAlphaMode(), error = " + e.getMessage());
                }
            }
        }
    }

    private void getHwTransitionClass(Context context) {
        try {
            this.clazz = new PathClassLoader(JAR_PAHT, context.getClassLoader()).loadClass(CLASS_NAME);
        } catch (ClassNotFoundException e) {
            if (this.mIsBerr) {
                this.mIsBerr = false;
                Log.e(TAG, "Catch exception when call getHwTransitionClass(), error = " + e.getMessage());
            }
        }
    }

    private void getHwTransitionInstance(View targetView, String type, int pageSpacing) {
        Class<?>[] mConstructorSignatures = {View.class, String.class, Integer.TYPE};
        if (this.clazz != null) {
            try {
                this.HwTransitionInstance = this.clazz.getConstructor(mConstructorSignatures).newInstance(targetView, type, Integer.valueOf(pageSpacing));
                Log.d("wangshx", "HwTransitionInstance = " + this.HwTransitionInstance);
            } catch (IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException e) {
                Log.e(TAG, "Exception happens when call getHwTransitionInstance(), error = " + e.getMessage());
            }
        }
    }

    private void initDeclaredMethods() {
        Class<?>[] setTransitionTypeArgs = {String.class};
        Class<?>[] animateDrawArgs = {Canvas.class};
        Class<?>[] startAnimationArgs = {View.class};
        Class<?>[] setCameraDistanceArgs = {Float.TYPE};
        Class<?>[] dispatchDrawArgs = {Canvas.class, Integer.TYPE, Boolean.TYPE};
        Class<?>[] setLayerTransparentArgs = {Boolean.TYPE};
        Class<?>[] setBackgroundArgs = {Bitmap.class};
        Class<?>[] setAlphaModeArgs = {Boolean.TYPE};
        Class<?>[] setAnimationFPSArgs = {Integer.TYPE};
        Class<?>[] setInterpolatorArgs = {TimeInterpolator.class};
        if (this.clazz != null) {
            try {
                this.mSetTransitionTypeMethod = this.clazz.getDeclaredMethod("setTransitionType", setTransitionTypeArgs);
                this.mAnimateDrawMethod = this.clazz.getDeclaredMethod("animateDraw", animateDrawArgs);
                this.mStartAnimationMethod = this.clazz.getDeclaredMethod("startAnimation", startAnimationArgs);
                this.mSetCameraDistanceMethod = this.clazz.getDeclaredMethod("setCameraDistance", setCameraDistanceArgs);
                this.mIs3DAnimationMethod = this.clazz.getDeclaredMethod("is3DAnimation", new Class[0]);
                this.mAnimateDispatchDrawMethod = this.clazz.getDeclaredMethod("animateDispatchDraw", dispatchDrawArgs);
                this.mSetLayerTransparentMethod = this.clazz.getDeclaredMethod("setLayerTransparent", setLayerTransparentArgs);
                this.mSetBackgroundMethod = this.clazz.getDeclaredMethod("setBackground", setBackgroundArgs);
                this.msetAlphaModeMethod = this.clazz.getDeclaredMethod("setAlphaMode", setAlphaModeArgs);
                this.mSetAnimationFPSMethod = this.clazz.getDeclaredMethod("setAnimationFPS", setAnimationFPSArgs);
                this.mStartViewAnimationMethod = this.clazz.getDeclaredMethod("startViewAnimation", startAnimationArgs);
                this.mSetInterpolatorMethod = this.clazz.getDeclaredMethod("setInterpolator", setInterpolatorArgs);
            } catch (NoSuchMethodException e) {
                Log.e(TAG, "Exception happens when call initDeclaredMethods(), error = " + e.getMessage());
            }
        }
    }
}
