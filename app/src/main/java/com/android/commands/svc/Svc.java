package com.android.commands.svc;

public class Svc {
    public static final Command[] COMMANDS = null;
    public static final Command COMMAND_HELP = null;

    public static abstract class Command {
        private String mName;

        public abstract String longHelp();

        public abstract void run(String[] strArr);

        public abstract String shortHelp();

        public Command(String name) {
            this.mName = name;
        }

        public String name() {
            return this.mName;
        }
    }

    /* renamed from: com.android.commands.svc.Svc.1 */
    static class AnonymousClass1 extends Command {
        AnonymousClass1(String $anonymous0) {
            super($anonymous0);
        }

        public String shortHelp() {
            return "Show information about the subcommands";
        }

        public String longHelp() {
            return shortHelp();
        }

        public void run(String[] args) {
            Command c;
            if (args.length == 2) {
                c = Svc.lookupCommand(args[1]);
                if (c != null) {
                    System.err.println(c.longHelp());
                    return;
                }
            }
            System.err.println("Available commands:");
            int maxlen = 0;
            for (Command c2 : Svc.COMMANDS) {
                int len = c2.name().length();
                if (maxlen < len) {
                    maxlen = len;
                }
            }
            String format = "    %-" + maxlen + "s    %s";
            for (Command c22 : Svc.COMMANDS) {
                System.err.println(String.format(format, new Object[]{c22.name(), c22.shortHelp()}));
            }
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.commands.svc.Svc.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.commands.svc.Svc.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.commands.svc.Svc.<clinit>():void");
    }

    public static void main(String[] args) {
        if (args.length >= 1) {
            Command c = lookupCommand(args[0]);
            if (c != null) {
                c.run(args);
                return;
            }
        }
        COMMAND_HELP.run(args);
    }

    private static Command lookupCommand(String name) {
        for (Command c : COMMANDS) {
            if (c.name().equals(name)) {
                return c;
            }
        }
        return null;
    }
}
