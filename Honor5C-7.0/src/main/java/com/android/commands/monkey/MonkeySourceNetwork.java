package com.android.commands.monkey;

import android.os.IPowerManager.Stub;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.util.Log;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.StringTokenizer;

public class MonkeySourceNetwork implements MonkeyEventSource {
    private static final Map<String, MonkeyCommand> COMMAND_MAP = null;
    private static final String DONE = "done";
    public static final MonkeyCommandReturn EARG = null;
    public static final MonkeyCommandReturn ERROR = null;
    private static final String ERROR_STR = "ERROR";
    public static final int MONKEY_NETWORK_VERSION = 2;
    public static final MonkeyCommandReturn OK = null;
    private static final String OK_STR = "OK";
    private static final String QUIT = "quit";
    private static final String TAG = "MonkeyStub";
    private static DeferredReturn deferredReturn;
    private Socket clientSocket;
    private final CommandQueueImpl commandQueue;
    private BufferedReader input;
    private PrintWriter output;
    private ServerSocket serverSocket;
    private boolean started;

    public interface CommandQueue {
        void enqueueEvent(MonkeyEvent monkeyEvent);
    }

    private static class CommandQueueImpl implements CommandQueue {
        private final Queue<MonkeyEvent> queuedEvents;

        private CommandQueueImpl() {
            this.queuedEvents = new LinkedList();
        }

        public void enqueueEvent(MonkeyEvent e) {
            this.queuedEvents.offer(e);
        }

        public MonkeyEvent getNextQueuedEvent() {
            return (MonkeyEvent) this.queuedEvents.poll();
        }
    }

    public interface MonkeyCommand {
        MonkeyCommandReturn translateCommand(List<String> list, CommandQueue commandQueue);
    }

    private static class DeferReturnCommand implements MonkeyCommand {
        private DeferReturnCommand() {
        }

        public MonkeyCommandReturn translateCommand(List<String> command, CommandQueue queue) {
            if (command.size() > 3) {
                if (!((String) command.get(1)).equals("screenchange")) {
                    return MonkeySourceNetwork.EARG;
                }
                long timeout = Long.parseLong((String) command.get(MonkeySourceNetwork.MONKEY_NETWORK_VERSION));
                MonkeyCommand deferredCommand = (MonkeyCommand) MonkeySourceNetwork.COMMAND_MAP.get(command.get(3));
                if (deferredCommand != null) {
                    MonkeySourceNetwork.deferredReturn = new DeferredReturn(1, deferredCommand.translateCommand(command.subList(3, command.size()), queue), timeout);
                    return MonkeySourceNetwork.OK;
                }
            }
            return MonkeySourceNetwork.EARG;
        }
    }

    private static class DeferredReturn {
        public static final int ON_WINDOW_STATE_CHANGE = 1;
        private MonkeyCommandReturn deferredReturn;
        private int event;
        private long timeout;

        public DeferredReturn(int event, MonkeyCommandReturn deferredReturn, long timeout) {
            this.event = event;
            this.deferredReturn = deferredReturn;
            this.timeout = timeout;
        }

        public MonkeyCommandReturn waitForEvent() {
            switch (this.event) {
                case ON_WINDOW_STATE_CHANGE /*1*/:
                    try {
                        synchronized (MonkeySourceNetworkViews.class) {
                            MonkeySourceNetworkViews.class.wait(this.timeout);
                            break;
                        }
                        break;
                    } catch (InterruptedException e) {
                        Log.d(MonkeySourceNetwork.TAG, "Deferral interrupted: " + e.getMessage());
                        break;
                    }
            }
            return this.deferredReturn;
        }
    }

    private static class FlipCommand implements MonkeyCommand {
        private FlipCommand() {
        }

        public MonkeyCommandReturn translateCommand(List<String> command, CommandQueue queue) {
            if (command.size() > 1) {
                String direction = (String) command.get(1);
                if ("open".equals(direction)) {
                    queue.enqueueEvent(new MonkeyFlipEvent(true));
                    return MonkeySourceNetwork.OK;
                } else if ("close".equals(direction)) {
                    queue.enqueueEvent(new MonkeyFlipEvent(false));
                    return MonkeySourceNetwork.OK;
                }
            }
            return MonkeySourceNetwork.EARG;
        }
    }

    private static class KeyCommand implements MonkeyCommand {
        private KeyCommand() {
        }

        public MonkeyCommandReturn translateCommand(List<String> command, CommandQueue queue) {
            if (command.size() != 3) {
                return MonkeySourceNetwork.EARG;
            }
            int keyCode = MonkeySourceNetwork.getKeyCode((String) command.get(MonkeySourceNetwork.MONKEY_NETWORK_VERSION));
            if (keyCode < 0) {
                Log.e(MonkeySourceNetwork.TAG, "Can't find keyname: " + ((String) command.get(MonkeySourceNetwork.MONKEY_NETWORK_VERSION)));
                return MonkeySourceNetwork.EARG;
            }
            Log.d(MonkeySourceNetwork.TAG, "keycode: " + keyCode);
            int action = -1;
            if ("down".equals(command.get(1))) {
                action = 0;
            } else if ("up".equals(command.get(1))) {
                action = 1;
            }
            if (action == -1) {
                Log.e(MonkeySourceNetwork.TAG, "got unknown action.");
                return MonkeySourceNetwork.EARG;
            }
            queue.enqueueEvent(new MonkeyKeyEvent(action, keyCode));
            return MonkeySourceNetwork.OK;
        }
    }

    public static class MonkeyCommandReturn {
        private final String message;
        private final boolean success;

        public MonkeyCommandReturn(boolean success) {
            this.success = success;
            this.message = null;
        }

        public MonkeyCommandReturn(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        boolean hasMessage() {
            return this.message != null;
        }

        String getMessage() {
            return this.message;
        }

        boolean wasSuccessful() {
            return this.success;
        }
    }

    private static class PressCommand implements MonkeyCommand {
        private PressCommand() {
        }

        public MonkeyCommandReturn translateCommand(List<String> command, CommandQueue queue) {
            if (command.size() != MonkeySourceNetwork.MONKEY_NETWORK_VERSION) {
                return MonkeySourceNetwork.EARG;
            }
            int keyCode = MonkeySourceNetwork.getKeyCode((String) command.get(1));
            if (keyCode < 0) {
                Log.e(MonkeySourceNetwork.TAG, "Can't find keyname: " + ((String) command.get(1)));
                return MonkeySourceNetwork.EARG;
            }
            queue.enqueueEvent(new MonkeyKeyEvent(0, keyCode));
            queue.enqueueEvent(new MonkeyKeyEvent(1, keyCode));
            return MonkeySourceNetwork.OK;
        }
    }

    private static class SleepCommand implements MonkeyCommand {
        private SleepCommand() {
        }

        public MonkeyCommandReturn translateCommand(List<String> command, CommandQueue queue) {
            if (command.size() != MonkeySourceNetwork.MONKEY_NETWORK_VERSION) {
                return MonkeySourceNetwork.EARG;
            }
            String sleepStr = (String) command.get(1);
            try {
                queue.enqueueEvent(new MonkeyThrottleEvent((long) Integer.parseInt(sleepStr)));
                return MonkeySourceNetwork.OK;
            } catch (NumberFormatException e) {
                Log.e(MonkeySourceNetwork.TAG, "Not a number: " + sleepStr, e);
                return MonkeySourceNetwork.EARG;
            }
        }
    }

    private static class TapCommand implements MonkeyCommand {
        private TapCommand() {
        }

        public MonkeyCommandReturn translateCommand(List<String> command, CommandQueue queue) {
            if (command.size() != 3) {
                return MonkeySourceNetwork.EARG;
            }
            try {
                int x = Integer.parseInt((String) command.get(1));
                int y = Integer.parseInt((String) command.get(MonkeySourceNetwork.MONKEY_NETWORK_VERSION));
                queue.enqueueEvent(new MonkeyTouchEvent(0).addPointer(0, (float) x, (float) y));
                queue.enqueueEvent(new MonkeyTouchEvent(1).addPointer(0, (float) x, (float) y));
                return MonkeySourceNetwork.OK;
            } catch (NumberFormatException e) {
                Log.e(MonkeySourceNetwork.TAG, "Got something that wasn't a number", e);
                return MonkeySourceNetwork.EARG;
            }
        }
    }

    private static class TouchCommand implements MonkeyCommand {
        private TouchCommand() {
        }

        public MonkeyCommandReturn translateCommand(List<String> command, CommandQueue queue) {
            if (command.size() != 4) {
                return MonkeySourceNetwork.EARG;
            }
            String actionName = (String) command.get(1);
            try {
                int x = Integer.parseInt((String) command.get(MonkeySourceNetwork.MONKEY_NETWORK_VERSION));
                int y = Integer.parseInt((String) command.get(3));
                int action = -1;
                if ("down".equals(actionName)) {
                    action = 0;
                } else if ("up".equals(actionName)) {
                    action = 1;
                } else if ("move".equals(actionName)) {
                    action = MonkeySourceNetwork.MONKEY_NETWORK_VERSION;
                }
                if (action == -1) {
                    Log.e(MonkeySourceNetwork.TAG, "Got a bad action: " + actionName);
                    return MonkeySourceNetwork.EARG;
                }
                queue.enqueueEvent(new MonkeyTouchEvent(action).addPointer(0, (float) x, (float) y));
                return MonkeySourceNetwork.OK;
            } catch (NumberFormatException e) {
                Log.e(MonkeySourceNetwork.TAG, "Got something that wasn't a number", e);
                return MonkeySourceNetwork.EARG;
            }
        }
    }

    private static class TrackballCommand implements MonkeyCommand {
        private TrackballCommand() {
        }

        public MonkeyCommandReturn translateCommand(List<String> command, CommandQueue queue) {
            if (command.size() != 3) {
                return MonkeySourceNetwork.EARG;
            }
            try {
                queue.enqueueEvent(new MonkeyTrackballEvent(MonkeySourceNetwork.MONKEY_NETWORK_VERSION).addPointer(0, (float) Integer.parseInt((String) command.get(1)), (float) Integer.parseInt((String) command.get(MonkeySourceNetwork.MONKEY_NETWORK_VERSION))));
                return MonkeySourceNetwork.OK;
            } catch (NumberFormatException e) {
                Log.e(MonkeySourceNetwork.TAG, "Got something that wasn't a number", e);
                return MonkeySourceNetwork.EARG;
            }
        }
    }

    private static class TypeCommand implements MonkeyCommand {
        private TypeCommand() {
        }

        public MonkeyCommandReturn translateCommand(List<String> command, CommandQueue queue) {
            if (command.size() != MonkeySourceNetwork.MONKEY_NETWORK_VERSION) {
                return MonkeySourceNetwork.EARG;
            }
            for (KeyEvent event : KeyCharacterMap.load(-1).getEvents(((String) command.get(1)).toString().toCharArray())) {
                queue.enqueueEvent(new MonkeyKeyEvent(event));
            }
            return MonkeySourceNetwork.OK;
        }
    }

    private static class WakeCommand implements MonkeyCommand {
        private WakeCommand() {
        }

        public MonkeyCommandReturn translateCommand(List<String> list, CommandQueue queue) {
            if (MonkeySourceNetwork.wake()) {
                return MonkeySourceNetwork.OK;
            }
            return MonkeySourceNetwork.ERROR;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.commands.monkey.MonkeySourceNetwork.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.commands.monkey.MonkeySourceNetwork.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.commands.monkey.MonkeySourceNetwork.<clinit>():void");
    }

    private static int getKeyCode(String keyName) {
        int keyCode;
        try {
            keyCode = Integer.parseInt(keyName);
        } catch (NumberFormatException e) {
            keyCode = MonkeySourceRandom.getKeyCode(keyName);
            if (keyCode == 0) {
                keyCode = MonkeySourceRandom.getKeyCode("KEYCODE_" + keyName.toUpperCase());
                if (keyCode == 0) {
                    return -1;
                }
            }
        }
        return keyCode;
    }

    private static final boolean wake() {
        try {
            Stub.asInterface(ServiceManager.getService("power")).wakeUp(SystemClock.uptimeMillis(), "Monkey", null);
            return true;
        } catch (RemoteException e) {
            Log.e(TAG, "Got remote exception", e);
            return false;
        }
    }

    public MonkeySourceNetwork(int port) throws IOException {
        this.commandQueue = new CommandQueueImpl();
        this.started = false;
        this.serverSocket = new ServerSocket(port, 0, InetAddress.getLocalHost());
    }

    private void startServer() throws IOException {
        this.clientSocket = this.serverSocket.accept();
        MonkeySourceNetworkViews.setup();
        wake();
        this.input = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));
        this.output = new PrintWriter(this.clientSocket.getOutputStream(), true);
    }

    private void stopServer() throws IOException {
        this.clientSocket.close();
        MonkeySourceNetworkViews.teardown();
        this.input.close();
        this.output.close();
        this.started = false;
    }

    private static String replaceQuotedChars(String input) {
        return input.replace("\\\"", "\"");
    }

    private static List<String> commandLineSplit(String line) {
        ArrayList<String> result = new ArrayList();
        StringTokenizer tok = new StringTokenizer(line);
        boolean insideQuote = false;
        StringBuffer quotedWord = new StringBuffer();
        while (tok.hasMoreTokens()) {
            String cur = tok.nextToken();
            if (!insideQuote && cur.startsWith("\"")) {
                quotedWord.append(replaceQuotedChars(cur));
                insideQuote = true;
            } else if (!insideQuote) {
                result.add(replaceQuotedChars(cur));
            } else if (cur.endsWith("\"")) {
                insideQuote = false;
                quotedWord.append(" ").append(replaceQuotedChars(cur));
                String word = quotedWord.toString();
                result.add(word.substring(1, word.length() - 1));
            } else {
                quotedWord.append(" ").append(replaceQuotedChars(cur));
            }
        }
        return result;
    }

    private void translateCommand(String commandLine) {
        Log.d(TAG, "translateCommand: " + commandLine);
        List<String> parts = commandLineSplit(commandLine);
        if (parts.size() > 0) {
            MonkeyCommand command = (MonkeyCommand) COMMAND_MAP.get(parts.get(0));
            if (command != null) {
                handleReturn(command.translateCommand(parts, this.commandQueue));
            }
        }
    }

    private void handleReturn(MonkeyCommandReturn ret) {
        if (ret.wasSuccessful()) {
            if (ret.hasMessage()) {
                returnOk(ret.getMessage());
            } else {
                returnOk();
            }
        } else if (ret.hasMessage()) {
            returnError(ret.getMessage());
        } else {
            returnError();
        }
    }

    public MonkeyEvent getNextEvent() {
        if (!this.started) {
            try {
                startServer();
                this.started = true;
            } catch (IOException e) {
                Log.e(TAG, "Got IOException from server", e);
                return null;
            }
        }
        while (true) {
            try {
                MonkeyEvent queuedEvent = this.commandQueue.getNextQueuedEvent();
                if (queuedEvent != null) {
                    return queuedEvent;
                }
                if (deferredReturn != null) {
                    Log.d(TAG, "Waiting for event");
                    MonkeyCommandReturn ret = deferredReturn.waitForEvent();
                    deferredReturn = null;
                    handleReturn(ret);
                }
                String command = this.input.readLine();
                if (command == null) {
                    Log.d(TAG, "Connection dropped.");
                    command = DONE;
                }
                if (DONE.equals(command)) {
                    try {
                        stopServer();
                        return new MonkeyNoopEvent();
                    } catch (IOException e2) {
                        Log.e(TAG, "Got IOException shutting down!", e2);
                        return null;
                    }
                } else if (QUIT.equals(command)) {
                    Log.d(TAG, "Quit requested");
                    returnOk();
                    return null;
                } else if (!command.startsWith("#")) {
                    translateCommand(command);
                }
            } catch (IOException e22) {
                Log.e(TAG, "Exception: ", e22);
                return null;
            }
        }
    }

    private void returnError() {
        this.output.println(ERROR_STR);
    }

    private void returnError(String msg) {
        this.output.print(ERROR_STR);
        this.output.print(":");
        this.output.println(msg);
    }

    private void returnOk() {
        this.output.println(OK_STR);
    }

    private void returnOk(String returnValue) {
        this.output.print(OK_STR);
        this.output.print(":");
        this.output.println(returnValue);
    }

    public void setVerbose(int verbose) {
    }

    public boolean validate() {
        return true;
    }
}
