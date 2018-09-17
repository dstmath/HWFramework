package com.android.commands.svc;

public class Svc {
    public static final Command[] COMMANDS = new Command[]{COMMAND_HELP, new PowerCommand(), new DataCommand(), new WifiCommand(), new UsbCommand(), new NfcCommand()};
    public static final Command COMMAND_HELP = new Command("help") {
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
    };

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
