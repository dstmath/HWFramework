package com.android.server;

import com.google.android.collect.Lists;
import java.io.FileDescriptor;
import java.util.ArrayList;

public class NativeDaemonEvent {
    public static final String SENSITIVE_MARKER = "{{sensitive}}";
    private final int mCmdNumber;
    private final int mCode;
    private FileDescriptor[] mFdList;
    private final String mLogMessage;
    private final String mMessage;
    private String[] mParsed;
    private final String mRawEvent;

    private NativeDaemonEvent(int cmdNumber, int code, String message, String rawEvent, String logMessage, FileDescriptor[] fdList) {
        this.mCmdNumber = cmdNumber;
        this.mCode = code;
        this.mMessage = message;
        this.mRawEvent = rawEvent;
        this.mLogMessage = logMessage;
        this.mParsed = null;
        this.mFdList = fdList;
    }

    public int getCmdNumber() {
        return this.mCmdNumber;
    }

    public int getCode() {
        return this.mCode;
    }

    public String getMessage() {
        return this.mMessage;
    }

    public FileDescriptor[] getFileDescriptors() {
        return this.mFdList;
    }

    @Deprecated
    public String getRawEvent() {
        return this.mRawEvent;
    }

    public String toString() {
        return this.mLogMessage;
    }

    public boolean isClassContinue() {
        return this.mCode >= 100 && this.mCode < 200;
    }

    public boolean isClassOk() {
        return this.mCode >= 200 && this.mCode < 300;
    }

    public boolean isClassServerError() {
        return this.mCode >= 400 && this.mCode < SystemService.PHASE_SYSTEM_SERVICES_READY;
    }

    public boolean isClassClientError() {
        return this.mCode >= SystemService.PHASE_SYSTEM_SERVICES_READY && this.mCode < NetdResponseCode.InterfaceChange;
    }

    public boolean isClassUnsolicited() {
        return isClassUnsolicited(this.mCode);
    }

    private static boolean isClassUnsolicited(int code) {
        if (code < NetdResponseCode.InterfaceChange || code >= 700) {
            return code >= 800 && code < 900;
        } else {
            return true;
        }
    }

    public void checkCode(int code) {
        if (this.mCode != code) {
            throw new IllegalStateException("Expected " + code + " but was: " + this);
        }
    }

    public static NativeDaemonEvent parseRawEvent(String rawEvent, FileDescriptor[] fdList) {
        String[] parsed = rawEvent.split(" ");
        if (parsed.length < 2) {
            throw new IllegalArgumentException("Insufficient arguments");
        }
        try {
            int code = Integer.parseInt(parsed[0]);
            int skiplength = parsed[0].length() + 1;
            int cmdNumber = -1;
            if (!isClassUnsolicited(code)) {
                if (parsed.length < 3) {
                    throw new IllegalArgumentException("Insufficient arguemnts");
                }
                try {
                    cmdNumber = Integer.parseInt(parsed[1]);
                    skiplength += parsed[1].length() + 1;
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("problem parsing cmdNumber", e);
                }
            }
            String logMessage = rawEvent;
            if (parsed.length > 2 && parsed[2].equals(SENSITIVE_MARKER)) {
                skiplength += parsed[2].length() + 1;
                logMessage = parsed[0] + " " + parsed[1] + " {}";
            }
            return new NativeDaemonEvent(cmdNumber, code, rawEvent.substring(skiplength), rawEvent, logMessage, fdList);
        } catch (NumberFormatException e2) {
            throw new IllegalArgumentException("problem parsing code", e2);
        }
    }

    public static String[] filterMessageList(NativeDaemonEvent[] events, int matchCode) {
        ArrayList<String> result = Lists.newArrayList();
        for (NativeDaemonEvent event : events) {
            if (event.getCode() == matchCode) {
                result.add(event.getMessage());
            }
        }
        return (String[]) result.toArray(new String[result.size()]);
    }

    public String getField(int n) {
        if (this.mParsed == null) {
            this.mParsed = unescapeArgs(this.mRawEvent);
        }
        n += 2;
        if (n > this.mParsed.length) {
            return null;
        }
        return this.mParsed[n];
    }

    public static String[] unescapeArgs(String rawEvent) {
        String LOGTAG = "unescapeArgs";
        ArrayList<String> parsed = new ArrayList();
        int length = rawEvent.length();
        int current = 0;
        boolean quoted = false;
        if (rawEvent.charAt(0) == '\"') {
            quoted = true;
            current = 1;
        }
        while (current < length) {
            char terminator = quoted ? '\"' : ' ';
            int wordEnd = current;
            while (wordEnd < length && rawEvent.charAt(wordEnd) != terminator) {
                if (rawEvent.charAt(wordEnd) == '\\') {
                    wordEnd++;
                }
                wordEnd++;
            }
            if (wordEnd > length) {
                wordEnd = length;
            }
            String word = rawEvent.substring(current, wordEnd);
            current += word.length();
            if (quoted) {
                current++;
            } else {
                word = word.trim();
            }
            parsed.add(word.replace("\\\\", "\\").replace("\\\"", "\""));
            int nextSpace = rawEvent.indexOf(32, current);
            int nextQuote = rawEvent.indexOf(" \"", current);
            if (nextQuote <= -1 || nextQuote > nextSpace) {
                quoted = false;
                if (nextSpace > -1) {
                    current = nextSpace + 1;
                }
            } else {
                quoted = true;
                current = nextQuote + 2;
            }
        }
        return (String[]) parsed.toArray(new String[parsed.size()]);
    }
}
