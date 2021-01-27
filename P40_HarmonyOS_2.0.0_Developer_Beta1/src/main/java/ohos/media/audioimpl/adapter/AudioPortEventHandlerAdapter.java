package ohos.media.audioimpl.adapter;

import android.util.ArrayMap;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import ohos.media.audio.AudioDeviceDescriptor;
import ohos.media.utils.log.Logger;
import ohos.media.utils.log.LoggerFactory;

public class AudioPortEventHandlerAdapter {
    private static final String CLAZZ_PATH = "android.media.AudioPortEventHandler";
    private static final String FIELD_NAME_AUDIO_PORT_EVENT_HANDLER = "sAudioPortEventHandler";
    private static final String INTERFACE_PATH = "android.media.AudioManager$OnAudioPortUpdateListener";
    private static final Logger LOGGER = LoggerFactory.getAudioLogger(AudioPortEventHandlerAdapter.class);
    private static final String METHOD_NAME_REGISTER_LISTENER = "registerListener";
    private static final String METHOD_NAME_UNREGISTER_LISTENER = "unregisterListener";
    private static Object audioPortEventHandler;
    private static Class onAudioPortUpdateListener;
    private static Method registerListener;
    private static Method unregisterListener;
    private final ArrayMap<AudioDevicePortChangeObserver, Object> observerToListenerMap = new ArrayMap<>();

    public interface AudioDevicePortChangeObserver {
        void onAudioDevicePortListChange(AudioDeviceDescriptor[] audioDeviceDescriptorArr);
    }

    public AudioPortEventHandlerAdapter() {
        Class<?> classAndNewInstance = getClassAndNewInstance();
        if (classAndNewInstance == null) {
            LOGGER.error("AudioPortEventHandlerAdapter: fail to getClassAndNewInstance.", new Object[0]);
            return;
        }
        initMethods(classAndNewInstance);
        initAndroidSideAudioPortEventHandler(classAndNewInstance);
    }

    private void initAndroidSideAudioPortEventHandler(Class<?> cls) {
        try {
            Method declaredMethod = cls.getDeclaredMethod("init", new Class[0]);
            try {
                declaredMethod.setAccessible(true);
                declaredMethod.invoke(audioPortEventHandler, new Object[0]);
            } catch (IllegalAccessException | InvocationTargetException e) {
                LOGGER.error("initAndroidSideAudioPortEventHandler-invoke exception: %{public}s", e.toString());
                e.printStackTrace();
            }
        } catch (NoSuchMethodException e2) {
            LOGGER.error("initAndroidSideAudioPortEventHandler-getDeclaredMethod exception: %{public}s", e2.toString());
            e2.printStackTrace();
        }
    }

    private Class<?> getClassAndNewInstance() {
        try {
            Class<?> cls = Class.forName(CLAZZ_PATH);
            try {
                onAudioPortUpdateListener = Class.forName(INTERFACE_PATH);
                try {
                    try {
                        Field declaredField = Class.forName("android.media.AudioManager").getDeclaredField(FIELD_NAME_AUDIO_PORT_EVENT_HANDLER);
                        declaredField.setAccessible(true);
                        try {
                            audioPortEventHandler = declaredField.get(null);
                            return cls;
                        } catch (IllegalAccessException e) {
                            LOGGER.error("audioPortEventHandler-get exception: %{public}s", e.toString());
                            e.printStackTrace();
                            return null;
                        }
                    } catch (NoSuchFieldException e2) {
                        LOGGER.error("clazzAudioManager-getDeclaredField exception: %{public}s", e2.toString());
                        e2.printStackTrace();
                        return null;
                    }
                } catch (ClassNotFoundException e3) {
                    LOGGER.error("clazzAudioManager-forName exception: %{public}s", e3.toString());
                    e3.printStackTrace();
                    return null;
                }
            } catch (ClassNotFoundException e4) {
                LOGGER.error("onAudioPortUpdateListener-forName exception: %{public}s", e4.toString());
                e4.printStackTrace();
                return null;
            }
        } catch (ClassNotFoundException e5) {
            LOGGER.error("clazz-forName exception: %{public}s", e5.toString());
            e5.printStackTrace();
            return null;
        }
    }

    private void initMethods(Class<?> cls) {
        try {
            registerListener = cls.getDeclaredMethod(METHOD_NAME_REGISTER_LISTENER, onAudioPortUpdateListener);
            registerListener.setAccessible(true);
        } catch (NoSuchMethodException e) {
            LOGGER.error("initMethods-getDeclaredMethod-registerListener exception: %{public}s", e.toString());
            e.printStackTrace();
        }
        try {
            unregisterListener = cls.getDeclaredMethod(METHOD_NAME_UNREGISTER_LISTENER, onAudioPortUpdateListener);
            unregisterListener.setAccessible(true);
        } catch (NoSuchMethodException e2) {
            LOGGER.error("initMethods-getDeclaredMethod-unregisterListener exception: %{public}s", e2.toString());
            e2.printStackTrace();
        }
    }

    public void registerListener(AudioDevicePortChangeObserver audioDevicePortChangeObserver) {
        synchronized (this.observerToListenerMap) {
            Object newProxyInstance = Proxy.newProxyInstance(getClass().getClassLoader(), new Class[]{onAudioPortUpdateListener}, new WorkHandler(audioDevicePortChangeObserver));
            this.observerToListenerMap.put(audioDevicePortChangeObserver, newProxyInstance);
            try {
                registerListener.invoke(audioPortEventHandler, newProxyInstance);
            } catch (IllegalAccessException | InvocationTargetException e) {
                LOGGER.error("registerListener-invoke exception: %{public}s", e.toString());
                e.printStackTrace();
            }
        }
    }

    /* access modifiers changed from: private */
    public class WorkHandler implements InvocationHandler {
        private AudioDevicePortChangeObserver observer;

        public WorkHandler(AudioDevicePortChangeObserver audioDevicePortChangeObserver) {
            this.observer = audioDevicePortChangeObserver;
        }

        /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
        @Override // java.lang.reflect.InvocationHandler
        public Object invoke(Object obj, Method method, Object[] objArr) throws Throwable {
            char c;
            String name = method.getName();
            switch (name.hashCode()) {
                case -1776922004:
                    if (name.equals("toString")) {
                        c = 4;
                        break;
                    }
                    c = 65535;
                    break;
                case -1449759526:
                    if (name.equals("onServiceDied")) {
                        c = 2;
                        break;
                    }
                    c = 65535;
                    break;
                case -1295482945:
                    if (name.equals("equals")) {
                        c = 3;
                        break;
                    }
                    c = 65535;
                    break;
                case -52004040:
                    if (name.equals("onAudioPatchListUpdate")) {
                        c = 1;
                        break;
                    }
                    c = 65535;
                    break;
                case 147696667:
                    if (name.equals("hashCode")) {
                        c = 5;
                        break;
                    }
                    c = 65535;
                    break;
                case 2125938559:
                    if (name.equals("onAudioPortListUpdate")) {
                        c = 0;
                        break;
                    }
                    c = 65535;
                    break;
                default:
                    c = 65535;
                    break;
            }
            if (c == 0) {
                this.observer.onAudioDevicePortListChange(null);
                return null;
            } else if (c == 1 || c == 2) {
                AudioPortEventHandlerAdapter.LOGGER.debug("WorkHandler.invoke: %{public}s called", method.getName());
                return null;
            } else if (c == 3) {
                return Boolean.valueOf(Proxy.getInvocationHandler(objArr[0]).equals(this));
            } else {
                if (c == 4) {
                    AudioPortEventHandlerAdapter.LOGGER.debug("WorkHandler.invoke: toString called", new Object[0]);
                    return toString();
                } else if (c != 5) {
                    AudioPortEventHandlerAdapter.LOGGER.error("WorkHandler.invoke: unknown method %{public}s", method.getName());
                    return null;
                } else {
                    AudioPortEventHandlerAdapter.LOGGER.debug("WorkHandler.invoke: hashCode called", new Object[0]);
                    return Integer.valueOf(hashCode());
                }
            }
        }
    }

    public void unregisterListener(AudioDevicePortChangeObserver audioDevicePortChangeObserver) {
        synchronized (this.observerToListenerMap) {
            try {
                unregisterListener.invoke(audioPortEventHandler, this.observerToListenerMap.get(audioDevicePortChangeObserver));
            } catch (IllegalAccessException | InvocationTargetException e) {
                LOGGER.error("unregisterListener-invoke exception: %{public}s", e.toString());
                e.printStackTrace();
            }
            this.observerToListenerMap.remove(audioDevicePortChangeObserver);
        }
    }
}
