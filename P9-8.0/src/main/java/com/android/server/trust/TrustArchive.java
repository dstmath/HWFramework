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
    ArrayDeque<Event> mEvents = new ArrayDeque();

    private static class Event {
        final ComponentName agent;
        final long duration;
        final long elapsedTimestamp;
        final int flags;
        final boolean managingTrust;
        final String message;
        final int type;
        final int userId;

        /* synthetic */ Event(int type, int userId, ComponentName agent, String message, long duration, int flags, boolean managingTrust, Event -this7) {
            this(type, userId, agent, message, duration, flags, managingTrust);
        }

        private Event(int type, int userId, ComponentName agent, String message, long duration, int flags, boolean managingTrust) {
            this.type = type;
            this.userId = userId;
            this.agent = agent;
            this.elapsedTimestamp = SystemClock.elapsedRealtime();
            this.message = message;
            this.duration = duration;
            this.flags = flags;
            this.managingTrust = managingTrust;
        }
    }

    public void logGrantTrust(int userId, ComponentName agent, String message, long duration, int flags) {
        addEvent(new Event(0, userId, agent, message, duration, flags, false, null));
    }

    public void logRevokeTrust(int userId, ComponentName agent) {
        addEvent(new Event(1, userId, agent, null, 0, 0, false, null));
    }

    public void logTrustTimeout(int userId, ComponentName agent) {
        addEvent(new Event(2, userId, agent, null, 0, 0, false, null));
    }

    public void logAgentDied(int userId, ComponentName agent) {
        addEvent(new Event(3, userId, agent, null, 0, 0, false, null));
    }

    public void logAgentConnected(int userId, ComponentName agent) {
        addEvent(new Event(4, userId, agent, null, 0, 0, false, null));
    }

    public void logAgentStopped(int userId, ComponentName agent) {
        addEvent(new Event(5, userId, agent, null, 0, 0, false, null));
    }

    public void logManagingTrust(int userId, ComponentName agent, boolean managing) {
        addEvent(new Event(6, userId, agent, null, 0, 0, managing, null));
    }

    public void logDevicePolicyChanged() {
        addEvent(new Event(7, -1, null, null, 0, 0, false, null));
    }

    private void addEvent(Event e) {
        if (this.mEvents.size() >= 200) {
            this.mEvents.removeFirst();
        }
        this.mEvents.addLast(e);
    }

    public void dump(PrintWriter writer, int limit, int userId, String linePrefix, boolean duplicateSimpleNames) {
        int count = 0;
        Iterator<Event> iter = this.mEvents.descendingIterator();
        while (iter.hasNext() && count < limit) {
            Event ev = (Event) iter.next();
            if (userId == -1 || userId == ev.userId || ev.userId == -1) {
                writer.print(linePrefix);
                writer.printf("#%-2d %s %s: ", new Object[]{Integer.valueOf(count), formatElapsed(ev.elapsedTimestamp), dumpType(ev.type)});
                if (userId == -1) {
                    writer.print("user=");
                    writer.print(ev.userId);
                    writer.print(", ");
                }
                if (ev.agent != null) {
                    writer.print("agent=");
                    if (duplicateSimpleNames) {
                        writer.print(ev.agent.flattenToShortString());
                    } else {
                        writer.print(getSimpleName(ev.agent));
                    }
                }
                switch (ev.type) {
                    case 0:
                        writer.printf(", message=\"%s\", duration=%s, flags=%s", new Object[]{ev.message, formatDuration(ev.duration), dumpGrantFlags(ev.flags)});
                        break;
                    case 6:
                        writer.printf(", managingTrust=" + ev.managingTrust, new Object[0]);
                        break;
                }
                writer.println();
                count++;
            }
        }
    }

    public static String formatDuration(long duration) {
        StringBuilder sb = new StringBuilder();
        TimeUtils.formatDuration(duration, sb);
        return sb.toString();
    }

    private static String formatElapsed(long elapsed) {
        return TimeUtils.logTimeOfDay((elapsed - SystemClock.elapsedRealtime()) + System.currentTimeMillis());
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
