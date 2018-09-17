package com.android.commands.monkey;

import com.android.commands.monkey.MonkeySourceNetwork.CommandQueue;
import com.android.commands.monkey.MonkeySourceNetwork.MonkeyCommand;
import com.android.commands.monkey.MonkeySourceNetwork.MonkeyCommandReturn;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MonkeySourceNetworkVars {
    private static final Map<String, VarGetter> VAR_MAP = null;

    private interface VarGetter {
        String get();
    }

    public static class GetVarCommand implements MonkeyCommand {
        public MonkeyCommandReturn translateCommand(List<String> command, CommandQueue queue) {
            if (command.size() != 2) {
                return MonkeySourceNetwork.EARG;
            }
            VarGetter getter = (VarGetter) MonkeySourceNetworkVars.VAR_MAP.get(command.get(1));
            if (getter == null) {
                return new MonkeyCommandReturn(false, "unknown var");
            }
            return new MonkeyCommandReturn(true, getter.get());
        }
    }

    public static class ListVarCommand implements MonkeyCommand {
        public MonkeyCommandReturn translateCommand(List<String> list, CommandQueue queue) {
            Set<String> keys = MonkeySourceNetworkVars.VAR_MAP.keySet();
            StringBuffer sb = new StringBuffer();
            for (String key : keys) {
                sb.append(key).append(" ");
            }
            return new MonkeyCommandReturn(true, sb.toString());
        }
    }

    private static class StaticVarGetter implements VarGetter {
        private final String value;

        public StaticVarGetter(String value) {
            this.value = value;
        }

        public String get() {
            return this.value;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.commands.monkey.MonkeySourceNetworkVars.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.commands.monkey.MonkeySourceNetworkVars.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.commands.monkey.MonkeySourceNetworkVars.<clinit>():void");
    }
}
