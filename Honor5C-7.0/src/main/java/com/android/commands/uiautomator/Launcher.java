package com.android.commands.uiautomator;

import android.os.Process;
import java.util.Arrays;

public class Launcher {
    private static Command[] COMMANDS;
    private static Command HELP_COMMAND;

    public static abstract class Command {
        private String mName;

        public abstract String detailedOptions();

        public abstract void run(String[] strArr);

        public abstract String shortHelp();

        public Command(String name) {
            this.mName = name;
        }

        public String name() {
            return this.mName;
        }
    }

    /* renamed from: com.android.commands.uiautomator.Launcher.1 */
    static class AnonymousClass1 extends Command {
        AnonymousClass1(String $anonymous0) {
            super($anonymous0);
        }

        public void run(String[] args) {
            System.err.println("Usage: uiautomator <subcommand> [options]\n");
            System.err.println("Available subcommands:\n");
            for (Command command : Launcher.COMMANDS) {
                String shortHelp = command.shortHelp();
                String detailedOptions = command.detailedOptions();
                if (shortHelp == null) {
                    shortHelp = "";
                }
                if (detailedOptions == null) {
                    detailedOptions = "";
                }
                System.err.println(String.format("%s: %s", new Object[]{command.name(), shortHelp}));
                System.err.println(detailedOptions);
            }
        }

        public String detailedOptions() {
            return null;
        }

        public String shortHelp() {
            return "displays help message";
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.commands.uiautomator.Launcher.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.commands.uiautomator.Launcher.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.commands.uiautomator.Launcher.<clinit>():void");
    }

    public static void main(String[] args) {
        Process.setArgV0("uiautomator");
        if (args.length >= 1) {
            Command command = findCommand(args[0]);
            if (command != null) {
                String[] args2 = new String[0];
                if (args.length > 1) {
                    args2 = (String[]) Arrays.copyOfRange(args, 1, args.length);
                }
                command.run(args2);
                return;
            }
        }
        HELP_COMMAND.run(args);
    }

    private static Command findCommand(String name) {
        for (Command command : COMMANDS) {
            if (command.name().equals(name)) {
                return command;
            }
        }
        return null;
    }
}
