package com.android.commands.monkey;

import android.os.IPowerManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.util.Log;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import com.android.commands.monkey.MonkeySourceNetworkVars;
import com.android.commands.monkey.MonkeySourceNetworkViews;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.StringTokenizer;

public class MonkeySourceNetwork implements MonkeyEventSource {
    /* access modifiers changed from: private */
    public static final Map<String, MonkeyCommand> COMMAND_MAP = new HashMap();
    private static final String DONE = "done";
    public static final MonkeyCommandReturn EARG = new MonkeyCommandReturn(false, "Invalid Argument");
    public static final MonkeyCommandReturn ERROR = new MonkeyCommandReturn(false);
    private static final String ERROR_STR = "ERROR";
    public static final int MONKEY_NETWORK_VERSION = 2;
    public static final MonkeyCommandReturn OK = new MonkeyCommandReturn(true);
    private static final String OK_STR = "OK";
    private static final String QUIT = "quit";
    private static final String TAG = "MonkeyStub";
    /* access modifiers changed from: private */
    public static DeferredReturn deferredReturn;
    private Socket clientSocket;
    private final CommandQueueImpl commandQueue = new CommandQueueImpl();
    private BufferedReader input;
    private PrintWriter output;
    private ServerSocket serverSocket;
    private boolean started = false;

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
            return this.queuedEvents.poll();
        }
    }

    private static class DeferReturnCommand implements MonkeyCommand {
        private DeferReturnCommand() {
        }

        public MonkeyCommandReturn translateCommand(List<String> command, CommandQueue queue) {
            if (command.size() > 3) {
                if (!command.get(1).equals("screenchange")) {
                    return MonkeySourceNetwork.EARG;
                }
                long timeout = Long.parseLong(command.get(2));
                MonkeyCommand deferredCommand = (MonkeyCommand) MonkeySourceNetwork.COMMAND_MAP.get(command.get(3));
                if (deferredCommand != null) {
                    DeferredReturn unused = MonkeySourceNetwork.deferredReturn = new DeferredReturn(1, deferredCommand.translateCommand(command.subList(3, command.size()), queue), timeout);
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

        public DeferredReturn(int event2, MonkeyCommandReturn deferredReturn2, long timeout2) {
            this.event = event2;
            this.deferredReturn = deferredReturn2;
            this.timeout = timeout2;
        }

        public MonkeyCommandReturn waitForEvent() {
            if (this.event == 1) {
                try {
                    synchronized (MonkeySourceNetworkViews.class) {
                        MonkeySourceNetworkViews.class.wait(this.timeout);
                    }
                } catch (InterruptedException e) {
                    Log.d(MonkeySourceNetwork.TAG, "Deferral interrupted: " + e.getMessage());
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
                String direction = command.get(1);
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
            int keyCode = MonkeySourceNetwork.getKeyCode(command.get(2));
            if (keyCode < 0) {
                Log.e(MonkeySourceNetwork.TAG, "Can't find keyname: " + command.get(2));
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

    public interface MonkeyCommand {
        MonkeyCommandReturn translateCommand(List<String> list, CommandQueue commandQueue);
    }

    public static class MonkeyCommandReturn {
        private final String message;
        private final boolean success;

        public MonkeyCommandReturn(boolean success2) {
            this.success = success2;
            this.message = null;
        }

        public MonkeyCommandReturn(boolean success2, String message2) {
            this.success = success2;
            this.message = message2;
        }

        /* access modifiers changed from: package-private */
        public boolean hasMessage() {
            return this.message != null;
        }

        /* access modifiers changed from: package-private */
        public String getMessage() {
            return this.message;
        }

        /* access modifiers changed from: package-private */
        public boolean wasSuccessful() {
            return this.success;
        }
    }

    private static class PressCommand implements MonkeyCommand {
        private PressCommand() {
        }

        public MonkeyCommandReturn translateCommand(List<String> command, CommandQueue queue) {
            if (command.size() != 2) {
                return MonkeySourceNetwork.EARG;
            }
            int keyCode = MonkeySourceNetwork.getKeyCode(command.get(1));
            if (keyCode < 0) {
                Log.e(MonkeySourceNetwork.TAG, "Can't find keyname: " + command.get(1));
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
            if (command.size() != 2) {
                return MonkeySourceNetwork.EARG;
            }
            String sleepStr = command.get(1);
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
                int x = Integer.parseInt(command.get(1));
                int y = Integer.parseInt(command.get(2));
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
            String actionName = command.get(1);
            try {
                int x = Integer.parseInt(command.get(2));
                int y = Integer.parseInt(command.get(3));
                int action = -1;
                if ("down".equals(actionName)) {
                    action = 0;
                } else if ("up".equals(actionName)) {
                    action = 1;
                } else if ("move".equals(actionName)) {
                    action = 2;
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
                int dx = Integer.parseInt(command.get(1));
                queue.enqueueEvent(new MonkeyTrackballEvent(2).addPointer(0, (float) dx, (float) Integer.parseInt(command.get(2))));
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
            if (command.size() != 2) {
                return MonkeySourceNetwork.EARG;
            }
            for (KeyEvent event : KeyCharacterMap.load(-1).getEvents(command.get(1).toString().toCharArray())) {
                queue.enqueueEvent(new MonkeyKeyEvent(event));
            }
            return MonkeySourceNetwork.OK;
        }
    }

    private static class WakeCommand implements MonkeyCommand {
        private WakeCommand() {
        }

        public MonkeyCommandReturn translateCommand(List<String> list, CommandQueue queue) {
            if (!MonkeySourceNetwork.wake()) {
                return MonkeySourceNetwork.ERROR;
            }
            return MonkeySourceNetwork.OK;
        }
    }

    static {
        COMMAND_MAP.put("flip", new FlipCommand());
        COMMAND_MAP.put("touch", new TouchCommand());
        COMMAND_MAP.put("trackball", new TrackballCommand());
        COMMAND_MAP.put("key", new KeyCommand());
        COMMAND_MAP.put("sleep", new SleepCommand());
        COMMAND_MAP.put("wake", new WakeCommand());
        COMMAND_MAP.put("tap", new TapCommand());
        COMMAND_MAP.put("press", new PressCommand());
        COMMAND_MAP.put("type", new TypeCommand());
        COMMAND_MAP.put("listvar", new MonkeySourceNetworkVars.ListVarCommand());
        COMMAND_MAP.put("getvar", new MonkeySourceNetworkVars.GetVarCommand());
        COMMAND_MAP.put("listviews", new MonkeySourceNetworkViews.ListViewsCommand());
        COMMAND_MAP.put("queryview", new MonkeySourceNetworkViews.QueryViewCommand());
        COMMAND_MAP.put("getrootview", new MonkeySourceNetworkViews.GetRootViewCommand());
        COMMAND_MAP.put("getviewswithtext", new MonkeySourceNetworkViews.GetViewsWithTextCommand());
        COMMAND_MAP.put("deferreturn", new DeferReturnCommand());
    }

    /* access modifiers changed from: private */
    public static int getKeyCode(String keyName) {
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

    /* access modifiers changed from: private */
    public static final boolean wake() {
        try {
            IPowerManager.Stub.asInterface(ServiceManager.getService("power")).wakeUp(SystemClock.uptimeMillis(), "Monkey", null);
            return true;
        } catch (RemoteException e) {
            Log.e(TAG, "Got remote exception", e);
            return false;
        }
    }

    public MonkeySourceNetwork(int port) throws IOException {
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

    private static String replaceQuotedChars(String input2) {
        return input2.replace("\\\"", "\"");
    }

    private static List<String> commandLineSplit(String line) {
        ArrayList<String> result = new ArrayList<>();
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
                quotedWord.append(" ");
                quotedWord.append(replaceQuotedChars(cur));
                String word = quotedWord.toString();
                result.add(word.substring(1, word.length() - 1));
            } else {
                quotedWord.append(" ");
                quotedWord.append(replaceQuotedChars(cur));
            }
        }
        return result;
    }

    private void translateCommand(String commandLine) {
        Log.d(TAG, "translateCommand: " + commandLine);
        List<String> parts = commandLineSplit(commandLine);
        if (parts.size() > 0) {
            MonkeyCommand command = COMMAND_MAP.get(parts.get(0));
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
            } catch (IOException e3) {
                Log.e(TAG, "Exception: ", e3);
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
