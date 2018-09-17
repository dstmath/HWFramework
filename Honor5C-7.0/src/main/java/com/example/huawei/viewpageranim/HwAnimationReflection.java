package com.example.huawei.viewpageranim;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.Rect;
import android.util.Log;
import android.view.View;
import dalvik.system.PathClassLoader;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class HwAnimationReflection {
    private static final String DELETE_ANIM_CLASS_NAME = "com.huawei.hwtransition.anim.GarbageCanDelAnim";
    private static final String DELETE_LISTENER_CLASS_NAME = "com.huawei.hwtransition.anim.GarbageCanDelAnim$DeleteAnimListerner";
    private static final String ERROR_EFFECT_CLASS_NAME = "com.huawei.hwtransition.anim.HintErrorEffect";
    private static final String JAR_PAHT = "/system/framework/hwtransition.jar";
    private static final String TAG = "HwAnimationReflection";
    private boolean bErr;
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
        this.bErr = false;
        getAnimClass(context);
        getAnimInstance(errorEffectView);
        initDeclaredMethods();
    }

    public void startDeleteAnim(View currentPage, View nextPage, float x, float y, int duration) {
        if (this.mStartDeleteAnimMethod != null) {
            try {
                this.mStartDeleteAnimMethod.invoke(this.mDeletingAnimInstance, new Object[]{currentPage, nextPage, Float.valueOf(x), Float.valueOf(y), Integer.valueOf(duration)});
            } catch (Exception e) {
                if (this.bErr) {
                    this.bErr = false;
                    Log.e(TAG, e.toString());
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
                    this.mSetDeleteAnimListernerMethod.invoke(this.mDeletingAnimInstance, new Object[]{deleteListenerProxyInstance});
                } catch (Exception e) {
                    if (this.bErr) {
                        this.bErr = false;
                        Log.e(TAG, e.toString());
                    }
                }
            }
        }
    }

    public void drawErrorEffect(Canvas canvas) {
        if (this.mDrawErrorEffectMethod != null) {
            try {
                this.mDrawErrorEffectMethod.invoke(this.mErrorEffectInstance, new Object[]{canvas});
            } catch (Exception e) {
                if (this.bErr) {
                    this.bErr = false;
                    Log.e(TAG, e.toString());
                }
            }
        }
    }

    public Path getPath() {
        Path path = null;
        if (this.mGetPathMethod == null) {
            return path;
        }
        try {
            return (Path) this.mGetPathMethod.invoke(this.mErrorEffectInstance, new Object[0]);
        } catch (Exception e) {
            if (!this.bErr) {
                return path;
            }
            this.bErr = false;
            Log.e(TAG, e.toString());
            return path;
        }
    }

    public Rect getRect() {
        Rect rect = null;
        if (this.mGetRectMethod == null) {
            return rect;
        }
        try {
            return (Rect) this.mGetRectMethod.invoke(this.mErrorEffectInstance, new Object[0]);
        } catch (Exception e) {
            if (!this.bErr) {
                return rect;
            }
            this.bErr = false;
            Log.e(TAG, e.toString());
            return rect;
        }
    }

    public void showErrEffect(boolean show) {
        if (this.mShowErrEffectMethod != null) {
            try {
                this.mShowErrEffectMethod.invoke(this.mErrorEffectInstance, new Object[]{Boolean.valueOf(show)});
            } catch (Exception e) {
                if (this.bErr) {
                    this.bErr = false;
                    Log.e(TAG, e.toString());
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
            e.printStackTrace();
        }
        Log.i(TAG, "the errorEffect class is " + this.mErrorEffectClazz);
        Log.i(TAG, "the deleteAnim class is " + this.mDeleteAnimClazz);
    }

    private void getAnimInstance(View errorEffectView) {
        if (this.mDeleteAnimClazz != null) {
            try {
                this.mDeletingAnimInstance = this.mDeleteAnimClazz.getConstructor(new Class[0]).newInstance(new Object[0]);
            } catch (Exception e) {
                Log.e(TAG, "Error inflating class HwAnimation Exception is " + e.getMessage());
            }
        }
        if (this.mErrorEffectClazz != null) {
            try {
                this.mErrorEffectInstance = this.mErrorEffectClazz.getConstructor(new Class[]{View.class}).newInstance(new Object[]{errorEffectView});
            } catch (Exception e2) {
                Log.e(TAG, "Error inflating class HwAnimation Exception is " + e2.getMessage());
            }
        }
    }

    private void initDeclaredMethods() {
        if (this.mDeleteListernerClazz != null) {
            try {
                this.mSetDeleteAnimListernerMethod = this.mDeleteAnimClazz.getDeclaredMethod("setDeleteAnimListerner", new Class[]{this.mDeleteListernerClazz});
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
        if (this.mDeleteAnimClazz != null) {
            try {
                this.mStartDeleteAnimMethod = this.mDeleteAnimClazz.getDeclaredMethod("startDeleteAnim", new Class[]{View.class, View.class, Float.TYPE, Float.TYPE, Integer.TYPE});
            } catch (NoSuchMethodException e2) {
                e2.printStackTrace();
            }
        }
        if (this.mErrorEffectClazz != null) {
            Class<?>[] showErrEffectArgs = new Class[]{Boolean.TYPE};
            try {
                this.mDrawErrorEffectMethod = this.mErrorEffectClazz.getDeclaredMethod("drawErrorEffect", new Class[]{Canvas.class});
                this.mGetPathMethod = this.mErrorEffectClazz.getDeclaredMethod("getPath", new Class[0]);
                this.mGetRectMethod = this.mErrorEffectClazz.getDeclaredMethod("getRect", new Class[0]);
                this.mShowErrEffectMethod = this.mErrorEffectClazz.getDeclaredMethod("showErrEffect", showErrEffectArgs);
            } catch (NoSuchMethodException e22) {
                e22.printStackTrace();
            }
        }
    }
}
