package com.android.server.trust;

import android.content.ComponentName;
import android.os.SystemClock;
import android.util.TimeUtils;
import java.io.PrintWriter;
import java.util.ArrayDeque;
import java.util.Iterator;

public class TrustArchive {
    private static final int HISTORY_LIMIT = 200;
    private static final int TYPE_AGENT_CONNECTED = 4;
    private static final int TYPE_AGENT_DIED = 3;
    private static final int TYPE_AGENT_STOPPED = 5;
    private static final int TYPE_GRANT_TRUST = 0;
    private static final int TYPE_MANAGING_TRUST = 6;
    private static final int TYPE_POLICY_CHANGED = 7;
    private static final int TYPE_REVOKE_TRUST = 1;
    private static final int TYPE_TRUST_TIMEOUT = 2;
    ArrayDeque<Event> mEvents = new ArrayDeque<>();

    private static class Event {
        final ComponentName agent;
        final long duration;
        final long elapsedTimestamp;
        final int flags;
        final boolean managingTrust;
        final String message;
        final int type;
        final int userId;

        private Event(int type2, int userId2, ComponentName agent2, String message2, long duration2, int flags2, boolean managingTrust2) {
            this.type = type2;
            this.userId = userId2;
            this.agent = agent2;
            this.elapsedTimestamp = SystemClock.elapsedRealtime();
            this.message = message2;
            this.duration = duration2;
            this.flags = flags2;
            this.managingTrust = managingTrust2;
        }
    }

    public void logGrantTrust(int userId, ComponentName agent, String message, long duration, int flags) {
        Event event = new Event(0, userId, agent, message, duration, flags, false);
        addEvent(event);
    }

    public void logRevokeTrust(int userId, ComponentName agent) {
        Event event = new Event(1, userId, agent, null, 0, 0, false);
        addEvent(event);
    }

    public void logTrustTimeout(int userId, ComponentName agent) {
        Event event = new Event(2, userId, agent, null, 0, 0, false);
        addEvent(event);
    }

    public void logAgentDied(int userId, ComponentName agent) {
        Event event = new Event(3, userId, agent, null, 0, 0, false);
        addEvent(event);
    }

    public void logAgentConnected(int userId, ComponentName agent) {
        Event event = new Event(4, userId, agent, null, 0, 0, false);
        addEvent(event);
    }

    public void logAgentStopped(int userId, ComponentName agent) {
        Event event = new Event(5, userId, agent, null, 0, 0, false);
        addEvent(event);
    }

    public void logManagingTrust(int userId, ComponentName agent, boolean managing) {
        Event event = new Event(6, userId, agent, null, 0, 0, managing);
        addEvent(event);
    }

    public void logDevicePolicyChanged() {
        Event event = new Event(7, -1, null, null, 0, 0, false);
        addEvent(event);
    }

    private void addEvent(Event e) {
        if (this.mEvents.size() >= 200) {
            this.mEvents.removeFirst();
        }
        this.mEvents.addLast(e);
    }

    public void dump(PrintWriter writer, int limit, int userId, String linePrefix, boolean duplicateSimpleNames) {
        PrintWriter printWriter = writer;
        int i = userId;
        int count = 0;
        Iterator<Event> iter = this.mEvents.descendingIterator();
        while (true) {
            if (iter.hasNext()) {
                if (count >= limit) {
                    break;
                }
                Event ev = iter.next();
                if (i == -1 || i == ev.userId || ev.userId == -1) {
                    printWriter.print(linePrefix);
                    printWriter.printf("#%-2d %s %s: ", new Object[]{Integer.valueOf(count), formatElapsed(ev.elapsedTimestamp), dumpType(ev.type)});
                    if (i == -1) {
                        printWriter.print("user=");
                        printWriter.print(ev.userId);
                        printWriter.print(", ");
                    }
                    if (ev.agent != null) {
                        printWriter.print("agent=");
                        if (duplicateSimpleNames) {
                            printWriter.print(ev.agent.flattenToShortString());
                        } else {
                            printWriter.print(getSimpleName(ev.agent));
                        }
                    }
                    int i2 = ev.type;
                    if (i2 == 0) {
                        printWriter.printf(", message=\"%s\", duration=%s, flags=%s", new Object[]{ev.message, formatDuration(ev.duration), dumpGrantFlags(ev.flags)});
                    } else if (i2 == 6) {
                        printWriter.printf(", managingTrust=" + ev.managingTrust, new Object[0]);
                    }
                    writer.println();
                    count++;
                }
            } else {
                int i3 = limit;
                break;
            }
        }
        String str = linePrefix;
    }

    public static String formatDuration(long duration) {
        StringBuilder sb = new StringBuilder();
        TimeUtils.formatDuration(duration, sb);
        return sb.toString();
    }

    private static String formatElapsed(long elapsed) {
        return TimeUtils.logTimeOfDay(System.currentTimeMillis() + (elapsed - SystemClock.elapsedRealtime()));
    }

    static String getSimpleName(ComponentName cn) {
        String name = cn.getClassName();
        int idx = name.lastIndexOf(46);
        if (idx >= name.length() || idx < 0) {
            return name;
        }
        return name.substring(idx + 1);
    }

    private String dumpType(int type) {
        switch (type) {
            case 0:
                return "GrantTrust";
            case 1:
                return "RevokeTrust";
            case 2:
                return "TrustTimeout";
            case 3:
                return "AgentDied";
            case 4:
                return "AgentConnected";
            case 5:
                return "AgentStopped";
            case 6:
                return "ManagingTrust";
            case 7:
                return "DevicePolicyChanged";
            default:
                return "Unknown(" + type + ")";
        }
    }

    private String dumpGrantFlags(int flags) {
        StringBuilder sb = new StringBuilder();
        if ((flags & 1) != 0) {
            if (sb.length() != 0) {
                sb.append('|');
            }
            sb.append("INITIATED_BY_USER");
        }
        if ((flags & 2) != 0) {
            if (sb.length() != 0) {
                sb.append('|');
            }
            sb.append("DISMISS_KEYGUARD");
        }
        if (sb.length() == 0) {
            sb.append('0');
        }
        return sb.toString();
    }
}
