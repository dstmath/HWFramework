package com.huawei.hwtransition.interfaces;

import android.animation.TimeInterpolator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.Log;
import android.view.View;
import dalvik.system.PathClassLoader;
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
    private boolean bErr;
    private Class<?> clazz;
    private Method mAnimateDispatchDrawMethod;
    private Method mAnimateDrawMethod;
    private Method mIs3DAnimationMethod;
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
        this.bErr = false;
        getHwTransitionClass(targetView.getContext());
        getHwTransitionInstance(targetView, type, pageSpacing);
        initDeclaredMethods();
    }

    public void setTransitionType(String type) {
        if (this.mSetTransitionTypeMethod != null) {
            try {
                this.mSetTransitionTypeMethod.invoke(this.HwTransitionInstance, new Object[]{type});
            } catch (Exception e) {
                if (this.bErr) {
                    this.bErr = false;
                    Log.e(TAG, e.toString());
                }
            }
        }
    }

    public void setCameraDistance(float z) {
        if (this.mSetCameraDistanceMethod != null) {
            try {
                this.mSetCameraDistanceMethod.invoke(this.HwTransitionInstance, new Object[]{Float.valueOf(z)});
            } catch (Exception e) {
                if (this.bErr) {
                    this.bErr = false;
                    Log.e(TAG, e.toString());
                }
            }
        }
    }

    public boolean startAnimation(View view) {
        boolean rtn = false;
        if (this.mStartAnimationMethod == null) {
            return rtn;
        }
        try {
            return ((Boolean) this.mStartAnimationMethod.invoke(this.HwTransitionInstance, new Object[]{view})).booleanValue();
        } catch (Exception e) {
            if (!this.bErr) {
                return rtn;
            }
            this.bErr = false;
            Log.e(TAG, e.toString());
            return rtn;
        }
    }

    public boolean startViewAnimation(View view) {
        boolean rtn = false;
        if (this.mStartViewAnimationMethod == null) {
            return rtn;
        }
        try {
            return ((Boolean) this.mStartViewAnimationMethod.invoke(this.HwTransitionInstance, new Object[]{view})).booleanValue();
        } catch (Exception e) {
            if (!this.bErr) {
                return rtn;
            }
            this.bErr = false;
            Log.e(TAG, e.toString());
            return rtn;
        }
    }

    public boolean animateDraw(Canvas canvas) {
        boolean rtn = false;
        if (this.mAnimateDrawMethod == null) {
            return rtn;
        }
        try {
            return ((Boolean) this.mAnimateDrawMethod.invoke(this.HwTransitionInstance, new Object[]{canvas})).booleanValue();
        } catch (Exception e) {
            if (!this.bErr) {
                return rtn;
            }
            this.bErr = false;
            Log.e(TAG, e.toString());
            return rtn;
        }
    }

    public boolean is3DAnimation() {
        boolean rtn = false;
        if (this.mIs3DAnimationMethod == null) {
            return rtn;
        }
        try {
            return ((Boolean) this.mIs3DAnimationMethod.invoke(this.HwTransitionInstance, new Object[0])).booleanValue();
        } catch (Exception e) {
            if (!this.bErr) {
                return rtn;
            }
            this.bErr = false;
            Log.e(TAG, e.toString());
            return rtn;
        }
    }

    public boolean animateDispatchDraw(Canvas canvas, int transitonX, boolean isScrolling) {
        boolean rtn = false;
        if (this.mAnimateDispatchDrawMethod == null) {
            return rtn;
        }
        try {
            return ((Boolean) this.mAnimateDispatchDrawMethod.invoke(this.HwTransitionInstance, new Object[]{canvas, Integer.valueOf(transitonX), Boolean.valueOf(isScrolling)})).booleanValue();
        } catch (Exception e) {
            if (!this.bErr) {
                return rtn;
            }
            this.bErr = false;
            Log.e(TAG, e.toString());
            return rtn;
        }
    }

    public void setAnimationFPS(int minfps) {
        if (minfps > 0 && this.mSetAnimationFPSMethod != null) {
            try {
                this.mSetAnimationFPSMethod.invoke(this.HwTransitionInstance, new Object[]{Integer.valueOf(minfps)});
            } catch (Exception e) {
                if (this.bErr) {
                    this.bErr = false;
                    Log.e(TAG, e.toString());
                }
            }
        }
    }

    public void setInterpolator(TimeInterpolator interpolator) {
        if (this.mSetInterpolatorMethod != null) {
            try {
                this.mSetInterpolatorMethod.invoke(this.HwTransitionInstance, new Object[]{interpolator});
            } catch (Exception e) {
                if (this.bErr) {
                    this.bErr = false;
                    Log.e(TAG, e.toString());
                }
            }
        }
    }

    public void setLayerTransparent(boolean isTransparent) {
        if (this.mSetLayerTransparentMethod != null) {
            try {
                this.mSetLayerTransparentMethod.invoke(this.HwTransitionInstance, new Object[]{Boolean.valueOf(isTransparent)});
            } catch (Exception e) {
                if (this.bErr) {
                    this.bErr = false;
                    Log.e(TAG, e.toString());
                }
            }
        }
    }

    public void setBackground(Bitmap background) {
        if (this.mSetBackgroundMethod != null) {
            try {
                this.mSetBackgroundMethod.invoke(this.HwTransitionInstance, new Object[]{background});
            } catch (Exception e) {
                if (this.bErr) {
                    this.bErr = false;
                    Log.e(TAG, e.toString());
                }
            }
        }
    }

    public void setAlphaMode(boolean useAlpha) {
        if (this.msetAlphaModeMethod != null) {
            try {
                this.msetAlphaModeMethod.invoke(this.HwTransitionInstance, new Object[]{Boolean.valueOf(useAlpha)});
            } catch (Exception e) {
                if (this.bErr) {
                    this.bErr = false;
                    Log.e(TAG, e.toString());
                }
            }
        }
    }

    private void getHwTransitionClass(Context context) {
        try {
            this.clazz = new PathClassLoader(JAR_PAHT, context.getClassLoader()).loadClass(CLASS_NAME);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void getHwTransitionInstance(View targetView, String type, int pageSpacing) {
        Class[] clsArr = {View.class, String.class, Integer.TYPE};
        if (this.clazz != null) {
            try {
                this.HwTransitionInstance = this.clazz.getConstructor(clsArr).newInstance(new Object[]{targetView, type, Integer.valueOf(pageSpacing)});
                Log.d("wangshx", "HwTransitionInstance  = " + this.HwTransitionInstance);
            } catch (Exception e) {
                Log.e(TAG, "Error inflating class HwTransition Exception is " + e.getMessage());
            }
        }
    }

    private void initDeclaredMethods() {
        Class[] clsArr = {String.class};
        Class[] clsArr2 = {Canvas.class};
        Class[] clsArr3 = {View.class};
        Class<?>[] setCameraDistanceArgs = {Float.TYPE};
        Class[] clsArr4 = {Canvas.class, Integer.TYPE, Boolean.TYPE};
        Class<?>[] setLayerTransparentArgs = {Boolean.TYPE};
        Class[] clsArr5 = {Bitmap.class};
        Class<?>[] setAlphaModeArgs = {Boolean.TYPE};
        Class<?>[] setAnimationFPSArgs = {Integer.TYPE};
        Class[] clsArr6 = {TimeInterpolator.class};
        if (this.clazz != null) {
            try {
                this.mSetTransitionTypeMethod = this.clazz.getDeclaredMethod("setTransitionType", clsArr);
                this.mAnimateDrawMethod = this.clazz.getDeclaredMethod("animateDraw", clsArr2);
                this.mStartAnimationMethod = this.clazz.getDeclaredMethod("startAnimation", clsArr3);
                this.mSetCameraDistanceMethod = this.clazz.getDeclaredMethod("setCameraDistance", setCameraDistanceArgs);
                this.mIs3DAnimationMethod = this.clazz.getDeclaredMethod("is3DAnimation", new Class[0]);
                this.mAnimateDispatchDrawMethod = this.clazz.getDeclaredMethod("animateDispatchDraw", clsArr4);
                this.mSetLayerTransparentMethod = this.clazz.getDeclaredMethod("setLayerTransparent", setLayerTransparentArgs);
                this.mSetBackgroundMethod = this.clazz.getDeclaredMethod("setBackground", clsArr5);
                this.msetAlphaModeMethod = this.clazz.getDeclaredMethod("setAlphaMode", setAlphaModeArgs);
                this.mSetAnimationFPSMethod = this.clazz.getDeclaredMethod("setAnimationFPS", setAnimationFPSArgs);
                this.mStartViewAnimationMethod = this.clazz.getDeclaredMethod("startViewAnimation", clsArr3);
                this.mSetInterpolatorMethod = this.clazz.getDeclaredMethod("setInterpolator", clsArr6);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
    }
}
