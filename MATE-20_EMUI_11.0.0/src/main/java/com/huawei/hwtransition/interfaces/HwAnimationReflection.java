package com.huawei.hwtransition.interfaces;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.Rect;
import android.util.Log;
import android.view.View;
import dalvik.system.PathClassLoader;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class HwAnimationReflection {
    private static final String DELETE_ANIM_CLASS_NAME = "com.huawei.hwtransition.anim.GarbageCanDelAnim";
    private static final String DELETE_LISTENER_CLASS_NAME = "com.huawei.hwtransition.anim.GarbageCanDelAnim$DeleteAnimListerner";
    private static final String ERROR_EFFECT_CLASS_NAME = "com.huawei.hwtransition.anim.HintErrorEffect";
    private static final String JAR_PAHT = "/system/framework/hwtransition.jar";
    private static final String TAG = "HwAnimationReflection";
    PathClassLoader loader;
    private Class<?> mDeleteAnimClazz;
    private Class<?> mDeleteListernerClazz;
    private Object mDeletingAnimInstance;
    private DeletingListerner mDeletingListerner;
    private Method mDrawErrorEffectMethod;
    private Class<?> mErrorEffectClazz;
    private Object mErrorEffectInstance;
    private Method mGetPathMethod;
    private Method mGetRectMethod;
    private boolean mIsBerr;
    private Method mSetDeleteAnimListernerMethod;
    private Method mShowErrEffectMethod;
    private Method mStartDeleteAnimMethod;

    public interface DeletingListerner {
        void onAnimStart();

        void onDelete();
    }

    public HwAnimationReflection(Context context) {
        this(context, null);
    }

    public HwAnimationReflection(Context context, View errorEffectView) {
        this.mDeleteAnimClazz = null;
        this.mDeleteListernerClazz = null;
        this.mDeletingAnimInstance = null;
        this.mErrorEffectClazz = null;
        this.mErrorEffectInstance = null;
        this.mDrawErrorEffectMethod = null;
        this.mShowErrEffectMethod = null;
        this.mGetPathMethod = null;
        this.mGetRectMethod = null;
        this.mStartDeleteAnimMethod = null;
        this.mSetDeleteAnimListernerMethod = null;
        this.mIsBerr = false;
        getAnimClass(context);
        getAnimInstance(errorEffectView);
        initDeclaredMethods();
    }

    public void startDeleteAnim(View currentPage, View nextPage, float x, float y, int duration) {
        if (this.mStartDeleteAnimMethod != null) {
            try {
                this.mStartDeleteAnimMethod.invoke(this.mDeletingAnimInstance, currentPage, nextPage, Float.valueOf(x), Float.valueOf(y), Integer.valueOf(duration));
            } catch (IllegalAccessException | InvocationTargetException e) {
                if (this.mIsBerr) {
                    this.mIsBerr = false;
                    Log.e(TAG, "Catch exception when call startDeleteAnim(), error msg = " + e.getMessage());
                }
            }
        }
    }

    private void setDeletingListerner(DeletingListerner listerner) {
        this.mDeletingListerner = listerner;
    }

    public void setDeleteAnimListerner(DeletingListerner listerner) {
        setDeletingListerner(listerner);
        if (this.mDeleteListernerClazz != null) {
            Object deleteListenerProxyInstance = Proxy.newProxyInstance(this.loader, new Class[]{this.mDeleteListernerClazz}, new InvocationHandler() {
                /* class com.huawei.hwtransition.interfaces.HwAnimationReflection.AnonymousClass1 */

                @Override // java.lang.reflect.InvocationHandler
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    if (method.getName().equals("onAnimStart")) {
                        HwAnimationReflection.this.mDeletingListerner.onAnimStart();
                        return null;
                    } else if (!method.getName().equals("onDelete")) {
                        return method.invoke(proxy, args);
                    } else {
                        HwAnimationReflection.this.mDeletingListerner.onDelete();
                        return null;
                    }
                }
            });
            if (this.mSetDeleteAnimListernerMethod != null) {
                try {
                    this.mSetDeleteAnimListernerMethod.invoke(this.mDeletingAnimInstance, deleteListenerProxyInstance);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    if (this.mIsBerr) {
                        this.mIsBerr = false;
                        Log.e(TAG, "Catch exception when call setDeleteAnimListerner(), error msg = " + e.getMessage());
                    }
                }
            }
        }
    }

    public void drawErrorEffect(Canvas canvas) {
        if (this.mDrawErrorEffectMethod != null) {
            try {
                this.mDrawErrorEffectMethod.invoke(this.mErrorEffectInstance, canvas);
            } catch (IllegalAccessException | InvocationTargetException e) {
                if (this.mIsBerr) {
                    this.mIsBerr = false;
                    Log.e(TAG, "Catch exception when call drawErrorEffect(), error msg = " + e.getMessage());
                }
            }
        }
    }

    public Path getPath() {
        if (this.mGetPathMethod == null) {
            return null;
        }
        try {
            return (Path) this.mGetPathMethod.invoke(this.mErrorEffectInstance, new Object[0]);
        } catch (IllegalAccessException | InvocationTargetException e) {
            if (!this.mIsBerr) {
                return null;
            }
            this.mIsBerr = false;
            Log.e(TAG, "Catch exception when call getPath(), error msg = " + e.getMessage());
            return null;
        }
    }

    public Rect getRect() {
        if (this.mGetRectMethod == null) {
            return null;
        }
        try {
            return (Rect) this.mGetRectMethod.invoke(this.mErrorEffectInstance, new Object[0]);
        } catch (IllegalAccessException | InvocationTargetException e) {
            if (!this.mIsBerr) {
                return null;
            }
            this.mIsBerr = false;
            Log.e(TAG, "Catch exception when call getRect(), error msg = " + e.getMessage());
            return null;
        }
    }

    public void showErrEffect(boolean isShow) {
        if (this.mShowErrEffectMethod != null) {
            try {
                this.mShowErrEffectMethod.invoke(this.mErrorEffectInstance, Boolean.valueOf(isShow));
            } catch (IllegalAccessException | InvocationTargetException e) {
                if (this.mIsBerr) {
                    this.mIsBerr = false;
                    Log.e(TAG, "Catch exception when call showErrEffect(), error msg = " + e.getMessage());
                }
            }
        }
    }

    private void getAnimClass(Context context) {
        this.loader = new PathClassLoader(JAR_PAHT, context.getClassLoader());
        try {
            this.mDeleteAnimClazz = this.loader.loadClass(DELETE_ANIM_CLASS_NAME);
            this.mDeleteListernerClazz = this.loader.loadClass(DELETE_LISTENER_CLASS_NAME);
            this.mErrorEffectClazz = this.loader.loadClass(ERROR_EFFECT_CLASS_NAME);
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "Catch exception when call getAnimClass()");
        }
        Log.i(TAG, "the errorEffect class is " + this.mErrorEffectClazz);
        Log.i(TAG, "the deleteAnim class is " + this.mDeleteAnimClazz);
    }

    private void getAnimInstance(View errorEffectView) {
        if (this.mDeleteAnimClazz != null) {
            try {
                this.mDeletingAnimInstance = this.mDeleteAnimClazz.getConstructor(new Class[0]).newInstance(new Object[0]);
            } catch (IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException e) {
                Log.e(TAG, "Catch exception when call getAnimClass(), error = " + e.getMessage());
            }
        }
        if (this.mErrorEffectClazz != null) {
            try {
                this.mErrorEffectInstance = this.mErrorEffectClazz.getConstructor(View.class).newInstance(errorEffectView);
            } catch (IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException e2) {
                Log.e(TAG, "Catch exception when call getAnimClass(), error = " + e2.getMessage());
            }
        }
    }

    private void initDeclaredMethods() {
        if (this.mDeleteListernerClazz != null) {
            try {
                this.mSetDeleteAnimListernerMethod = this.mDeleteAnimClazz.getDeclaredMethod("setDeleteAnimListerner", this.mDeleteListernerClazz);
            } catch (NoSuchMethodException e) {
                Log.e(TAG, "initDeclaredMethods first error = " + e.getMessage());
            }
        }
        if (this.mDeleteAnimClazz != null) {
            try {
                this.mStartDeleteAnimMethod = this.mDeleteAnimClazz.getDeclaredMethod("startDeleteAnim", View.class, View.class, Float.TYPE, Float.TYPE, Integer.TYPE);
            } catch (NoSuchMethodException e2) {
                Log.e(TAG, "initDeclaredMethods second error = " + e2.getMessage());
            }
        }
        if (this.mErrorEffectClazz != null) {
            Class<?>[] drawErrorEffectArgs = {Canvas.class};
            Class<?>[] showErrEffectArgs = {Boolean.TYPE};
            try {
                this.mDrawErrorEffectMethod = this.mErrorEffectClazz.getDeclaredMethod("drawErrorEffect", drawErrorEffectArgs);
                this.mGetPathMethod = this.mErrorEffectClazz.getDeclaredMethod("getPath", new Class[0]);
                this.mGetRectMethod = this.mErrorEffectClazz.getDeclaredMethod("getRect", new Class[0]);
                this.mShowErrEffectMethod = this.mErrorEffectClazz.getDeclaredMethod("showErrEffect", showErrEffectArgs);
            } catch (NoSuchMethodException e3) {
                Log.e(TAG, "initDeclaredMethods third error = " + e3.getMessage());
            }
        }
    }
}
