package jdk.net;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.DatagramSocket;
import java.net.MulticastSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketOption;
import java.net.StandardSocketOptions;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import sun.net.ExtendedOptionsImpl;

public class Sockets {
    private static Method dsiGetOption;
    private static Method dsiSetOption;
    private static final HashMap<Class<?>, Set<SocketOption<?>>> options = new HashMap<>();
    private static Method siGetOption;
    private static Method siSetOption;

    static {
        initOptionSets();
        AccessController.doPrivileged(new PrivilegedAction<Void>() {
            public Void run() {
                Sockets.initMethods();
                return null;
            }
        });
    }

    /* access modifiers changed from: private */
    public static void initMethods() {
        try {
            Class<?> clazz = Class.forName("java.net.SocketSecrets");
            siSetOption = clazz.getDeclaredMethod("setOption", Object.class, SocketOption.class, Object.class);
            siSetOption.setAccessible(true);
            siGetOption = clazz.getDeclaredMethod("getOption", Object.class, SocketOption.class);
            siGetOption.setAccessible(true);
            dsiSetOption = clazz.getDeclaredMethod("setOption", DatagramSocket.class, SocketOption.class, Object.class);
            dsiSetOption.setAccessible(true);
            dsiGetOption = clazz.getDeclaredMethod("getOption", DatagramSocket.class, SocketOption.class);
            dsiGetOption.setAccessible(true);
        } catch (ReflectiveOperationException e) {
            throw new InternalError((Throwable) e);
        }
    }

    private static <T> void invokeSet(Method method, Object socket, SocketOption<T> option, T value) throws IOException {
        try {
            method.invoke(null, socket, option, value);
        } catch (Exception e) {
            if (e instanceof InvocationTargetException) {
                Throwable t = ((InvocationTargetException) e).getTargetException();
                if (t instanceof IOException) {
                    throw ((IOException) t);
                } else if (t instanceof RuntimeException) {
                    throw ((RuntimeException) t);
                }
            }
            throw new RuntimeException((Throwable) e);
        }
    }

    private static <T> T invokeGet(Method method, Object socket, SocketOption<T> option) throws IOException {
        try {
            return method.invoke(null, socket, option);
        } catch (Exception e) {
            if (e instanceof InvocationTargetException) {
                Throwable t = ((InvocationTargetException) e).getTargetException();
                if (t instanceof IOException) {
                    throw ((IOException) t);
                } else if (t instanceof RuntimeException) {
                    throw ((RuntimeException) t);
                }
            }
            throw new RuntimeException((Throwable) e);
        }
    }

    private Sockets() {
    }

    public static <T> void setOption(Socket s, SocketOption<T> name, T value) throws IOException {
        if (isSupported(Socket.class, name)) {
            invokeSet(siSetOption, s, name, value);
            return;
        }
        throw new UnsupportedOperationException(name.name());
    }

    public static <T> T getOption(Socket s, SocketOption<T> name) throws IOException {
        if (isSupported(Socket.class, name)) {
            return invokeGet(siGetOption, s, name);
        }
        throw new UnsupportedOperationException(name.name());
    }

    public static <T> void setOption(ServerSocket s, SocketOption<T> name, T value) throws IOException {
        if (isSupported(ServerSocket.class, name)) {
            invokeSet(siSetOption, s, name, value);
            return;
        }
        throw new UnsupportedOperationException(name.name());
    }

    public static <T> T getOption(ServerSocket s, SocketOption<T> name) throws IOException {
        if (isSupported(ServerSocket.class, name)) {
            return invokeGet(siGetOption, s, name);
        }
        throw new UnsupportedOperationException(name.name());
    }

    public static <T> void setOption(DatagramSocket s, SocketOption<T> name, T value) throws IOException {
        if (isSupported(s.getClass(), name)) {
            invokeSet(dsiSetOption, s, name, value);
            return;
        }
        throw new UnsupportedOperationException(name.name());
    }

    public static <T> T getOption(DatagramSocket s, SocketOption<T> name) throws IOException {
        if (isSupported(s.getClass(), name)) {
            return invokeGet(dsiGetOption, s, name);
        }
        throw new UnsupportedOperationException(name.name());
    }

    public static Set<SocketOption<?>> supportedOptions(Class<?> socketType) {
        Set<SocketOption<?>> set = options.get(socketType);
        if (set != null) {
            return set;
        }
        throw new IllegalArgumentException("unknown socket type");
    }

    private static boolean isSupported(Class<?> type, SocketOption<?> option) {
        return supportedOptions(type).contains(option);
    }

    private static void initOptionSets() {
        boolean flowsupported = ExtendedOptionsImpl.flowSupported();
        Set<SocketOption<?>> set = new HashSet<>();
        set.add(StandardSocketOptions.SO_KEEPALIVE);
        set.add(StandardSocketOptions.SO_SNDBUF);
        set.add(StandardSocketOptions.SO_RCVBUF);
        set.add(StandardSocketOptions.SO_REUSEADDR);
        set.add(StandardSocketOptions.SO_LINGER);
        set.add(StandardSocketOptions.IP_TOS);
        set.add(StandardSocketOptions.TCP_NODELAY);
        if (flowsupported) {
            set.add(ExtendedSocketOptions.SO_FLOW_SLA);
        }
        options.put(Socket.class, Collections.unmodifiableSet(set));
        Set<SocketOption<?>> set2 = new HashSet<>();
        set2.add(StandardSocketOptions.SO_RCVBUF);
        set2.add(StandardSocketOptions.SO_REUSEADDR);
        set2.add(StandardSocketOptions.IP_TOS);
        options.put(ServerSocket.class, Collections.unmodifiableSet(set2));
        Set<SocketOption<?>> set3 = new HashSet<>();
        set3.add(StandardSocketOptions.SO_SNDBUF);
        set3.add(StandardSocketOptions.SO_RCVBUF);
        set3.add(StandardSocketOptions.SO_REUSEADDR);
        set3.add(StandardSocketOptions.IP_TOS);
        if (flowsupported) {
            set3.add(ExtendedSocketOptions.SO_FLOW_SLA);
        }
        options.put(DatagramSocket.class, Collections.unmodifiableSet(set3));
        Set<SocketOption<?>> set4 = new HashSet<>();
        set4.add(StandardSocketOptions.SO_SNDBUF);
        set4.add(StandardSocketOptions.SO_RCVBUF);
        set4.add(StandardSocketOptions.SO_REUSEADDR);
        set4.add(StandardSocketOptions.IP_TOS);
        set4.add(StandardSocketOptions.IP_MULTICAST_IF);
        set4.add(StandardSocketOptions.IP_MULTICAST_TTL);
        set4.add(StandardSocketOptions.IP_MULTICAST_LOOP);
        if (flowsupported) {
            set4.add(ExtendedSocketOptions.SO_FLOW_SLA);
        }
        options.put(MulticastSocket.class, Collections.unmodifiableSet(set4));
    }
}
