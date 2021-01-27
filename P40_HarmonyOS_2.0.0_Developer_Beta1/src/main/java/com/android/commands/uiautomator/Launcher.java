package com.android.commands.uiautomator;

import android.os.Process;
import java.util.Arrays;

public class Launcher {
    private static Command[] COMMANDS = {HELP_COMMAND, new RunTestCommand(), new DumpCommand(), new EventsCommand()};
    private static Command HELP_COMMAND = new Command("help") {
        /* class com.android.commands.uiautomator.Launcher.AnonymousClass1 */

        @Override // com.android.commands.uiautomator.Launcher.Command
        public void run(String[] args) {
            System.err.println("Usage: uiautomator <subcommand> [options]\n");
            System.err.println("Available subcommands:\n");
            Command[] commandArr = Launcher.COMMANDS;
            for (Command command : commandArr) {
                String shortHelp = command.shortHelp();
                String detailedOptions = command.detailedOptions();
                if (shortHelp == null) {
                    shortHelp = "";
                }
                if (detailedOptions == null) {
                    detailedOptions = "";
                }
                System.err.println(String.format("%s: %s", command.name(), shortHelp));
                System.err.println(detailedOptions);
            }
        }

        @Override // com.android.commands.uiautomator.Launcher.Command
        public String detailedOptions() {
            return null;
        }

        @Override // com.android.commands.uiautomator.Launcher.Command
        public String shortHelp() {
            return "displays help message";
        }
    };

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

    public static void main(String[] args) {
        Command command;
        Process.setArgV0("uiautomator");
        if (args.length < 1 || (command = findCommand(args[0])) == null) {
            HELP_COMMAND.run(args);
            return;
        }
        String[] args2 = new String[0];
        if (args.length > 1) {
            args2 = (String[]) Arrays.copyOfRange(args, 1, args.length);
        }
        command.run(args2);
    }

    private static Command findCommand(String name) {
        Command[] commandArr = COMMANDS;
        for (Command command : commandArr) {
            if (command.name().equals(name)) {
                return command;
            }
        }
        return null;
    }
}
