package sun.security.util;

import java.net.NetPermission;
import java.net.SocketPermission;
import java.security.AllPermission;
import java.security.SecurityPermission;

public final class SecurityConstants {
    public static final AllPermission ALL_PERMISSION = null;
    public static final RuntimePermission CHECK_MEMBER_ACCESS_PERMISSION = null;
    public static final SecurityPermission CREATE_ACC_PERMISSION = null;
    public static final RuntimePermission CREATE_CLASSLOADER_PERMISSION = null;
    public static final String FILE_DELETE_ACTION = "delete";
    public static final String FILE_EXECUTE_ACTION = "execute";
    public static final String FILE_READLINK_ACTION = "readlink";
    public static final String FILE_READ_ACTION = "read";
    public static final String FILE_WRITE_ACTION = "write";
    public static final RuntimePermission GET_CLASSLOADER_PERMISSION = null;
    public static final SecurityPermission GET_COMBINER_PERMISSION = null;
    public static final NetPermission GET_COOKIEHANDLER_PERMISSION = null;
    public static final RuntimePermission GET_PD_PERMISSION = null;
    public static final SecurityPermission GET_POLICY_PERMISSION = null;
    public static final NetPermission GET_PROXYSELECTOR_PERMISSION = null;
    public static final NetPermission GET_RESPONSECACHE_PERMISSION = null;
    public static final RuntimePermission GET_STACK_TRACE_PERMISSION = null;
    public static final SocketPermission LOCAL_LISTEN_PERMISSION = null;
    public static final RuntimePermission MODIFY_THREADGROUP_PERMISSION = null;
    public static final RuntimePermission MODIFY_THREAD_PERMISSION = null;
    public static final String PROPERTY_READ_ACTION = "read";
    public static final String PROPERTY_RW_ACTION = "read,write";
    public static final String PROPERTY_WRITE_ACTION = "write";
    public static final NetPermission SET_COOKIEHANDLER_PERMISSION = null;
    public static final NetPermission SET_PROXYSELECTOR_PERMISSION = null;
    public static final NetPermission SET_RESPONSECACHE_PERMISSION = null;
    public static final String SOCKET_ACCEPT_ACTION = "accept";
    public static final String SOCKET_CONNECT_ACCEPT_ACTION = "connect,accept";
    public static final String SOCKET_CONNECT_ACTION = "connect";
    public static final String SOCKET_LISTEN_ACTION = "listen";
    public static final String SOCKET_RESOLVE_ACTION = "resolve";
    public static final NetPermission SPECIFY_HANDLER_PERMISSION = null;
    public static final RuntimePermission STOP_THREAD_PERMISSION = null;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: sun.security.util.SecurityConstants.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: sun.security.util.SecurityConstants.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: sun.security.util.SecurityConstants.<clinit>():void");
    }

    private SecurityConstants() {
    }
}
