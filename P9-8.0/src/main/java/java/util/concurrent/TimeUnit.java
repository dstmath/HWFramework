package java.util.concurrent;

public enum TimeUnit {
    NANOSECONDS {
        public long toNanos(long d) {
            return d;
        }

        public long toMicros(long d) {
            return d / TimeUnit.C1;
        }

        public long toMillis(long d) {
            return d / TimeUnit.C2;
        }

        public long toSeconds(long d) {
            return d / TimeUnit.C3;
        }

        public long toMinutes(long d) {
            return d / TimeUnit.C4;
        }

        public long toHours(long d) {
            return d / TimeUnit.C5;
        }

        public long toDays(long d) {
            return d / TimeUnit.C6;
        }

        public long convert(long d, TimeUnit u) {
            return u.toNanos(d);
        }

        int excessNanos(long d, long m) {
            return (int) (d - (TimeUnit.C2 * m));
        }
    },
    MICROSECONDS {
        public long toNanos(long d) {
            return TimeUnit.x(d, TimeUnit.C1, 9223372036854775L);
        }

        public long toMicros(long d) {
            return d;
        }

        public long toMillis(long d) {
            return d / TimeUnit.C1;
        }

        public long toSeconds(long d) {
            return d / TimeUnit.C2;
        }

        public long toMinutes(long d) {
            return d / 60000000;
        }

        public long toHours(long d) {
            return d / 3600000000L;
        }

        public long toDays(long d) {
            return d / 86400000000L;
        }

        public long convert(long d, TimeUnit u) {
            return u.toMicros(d);
        }

        int excessNanos(long d, long m) {
            return (int) ((TimeUnit.C1 * d) - (TimeUnit.C2 * m));
        }
    },
    MILLISECONDS {
        public long toNanos(long d) {
            return TimeUnit.x(d, TimeUnit.C2, 9223372036854L);
        }

        public long toMicros(long d) {
            return TimeUnit.x(d, TimeUnit.C1, 9223372036854775L);
        }

        public long toMillis(long d) {
            return d;
        }

        public long toSeconds(long d) {
            return d / TimeUnit.C1;
        }

        public long toMinutes(long d) {
            return d / 60000;
        }

        public long toHours(long d) {
            return d / 3600000;
        }

        public long toDays(long d) {
            return d / 86400000;
        }

        public long convert(long d, TimeUnit u) {
            return u.toMillis(d);
        }

        int excessNanos(long d, long m) {
            return 0;
        }
    },
    SECONDS {
        public long toNanos(long d) {
            return TimeUnit.x(d, TimeUnit.C3, 9223372036L);
        }

        public long toMicros(long d) {
            return TimeUnit.x(d, TimeUnit.C2, 9223372036854L);
        }

        public long toMillis(long d) {
            return TimeUnit.x(d, TimeUnit.C1, 9223372036854775L);
        }

        public long toSeconds(long d) {
            return d;
        }

        public long toMinutes(long d) {
            return d / 60;
        }

        public long toHours(long d) {
            return d / 3600;
        }

        public long toDays(long d) {
            return d / 86400;
        }

        public long convert(long d, TimeUnit u) {
            return u.toSeconds(d);
        }

        int excessNanos(long d, long m) {
            return 0;
        }
    },
    MINUTES {
        public long toNanos(long d) {
            return TimeUnit.x(d, TimeUnit.C4, 153722867);
        }

        public long toMicros(long d) {
            return TimeUnit.x(d, 60000000, 153722867280L);
        }

        public long toMillis(long d) {
            return TimeUnit.x(d, 60000, 153722867280912L);
        }

        public long toSeconds(long d) {
            return TimeUnit.x(d, 60, 153722867280912930L);
        }

        public long toMinutes(long d) {
            return d;
        }

        public long toHours(long d) {
            return d / 60;
        }

        public long toDays(long d) {
            return d / 1440;
        }

        public long convert(long d, TimeUnit u) {
            return u.toMinutes(d);
        }

        int excessNanos(long d, long m) {
            return 0;
        }
    },
    HOURS {
        public long toNanos(long d) {
            return TimeUnit.x(d, TimeUnit.C5, 2562047);
        }

        public long toMicros(long d) {
            return TimeUnit.x(d, 3600000000L, 2562047788L);
        }

        public long toMillis(long d) {
            return TimeUnit.x(d, 3600000, 2562047788015L);
        }

        public long toSeconds(long d) {
            return TimeUnit.x(d, 3600, 2562047788015215L);
        }

        public long toMinutes(long d) {
            return TimeUnit.x(d, 60, 153722867280912930L);
        }

        public long toHours(long d) {
            return d;
        }

        public long toDays(long d) {
            return d / 24;
        }

        public long convert(long d, TimeUnit u) {
            return u.toHours(d);
        }

        int excessNanos(long d, long m) {
            return 0;
        }
    },
    DAYS {
        public long toNanos(long d) {
            return TimeUnit.x(d, TimeUnit.C6, 106751);
        }

        public long toMicros(long d) {
            return TimeUnit.x(d, 86400000000L, 106751991);
        }

        public long toMillis(long d) {
            return TimeUnit.x(d, 86400000, 106751991167L);
        }

        public long toSeconds(long d) {
            return TimeUnit.x(d, 86400, 106751991167300L);
        }

        public long toMinutes(long d) {
            return TimeUnit.x(d, 1440, 6405119470038038L);
        }

        public long toHours(long d) {
            return TimeUnit.x(d, 24, 384307168202282325L);
        }

        public long toDays(long d) {
            return d;
        }

        public long convert(long d, TimeUnit u) {
            return u.toDays(d);
        }

        int excessNanos(long d, long m) {
            return 0;
        }
    };
    
    static final long C0 = 1;
    static final long C1 = 1000;
    static final long C2 = 1000000;
    static final long C3 = 1000000000;
    static final long C4 = 60000000000L;
    static final long C5 = 3600000000000L;
    static final long C6 = 86400000000000L;
    static final long MAX = Long.MAX_VALUE;

    abstract int excessNanos(long j, long j2);

    static long x(long d, long m, long over) {
        if (d > over) {
            return Long.MAX_VALUE;
        }
        if (d < (-over)) {
            return Long.MIN_VALUE;
        }
        return d * m;
    }

    public long convert(long sourceDuration, TimeUnit sourceUnit) {
        throw new AbstractMethodError();
    }

    public long toNanos(long duration) {
        throw new AbstractMethodError();
    }

    public long toMicros(long duration) {
        throw new AbstractMethodError();
    }

    public long toMillis(long duration) {
        throw new AbstractMethodError();
    }

    public long toSeconds(long duration) {
        throw new AbstractMethodError();
    }

    public long toMinutes(long duration) {
        throw new AbstractMethodError();
    }

    public long toHours(long duration) {
        throw new AbstractMethodError();
    }

    public long toDays(long duration) {
        throw new AbstractMethodError();
    }

    public void timedWait(Object obj, long timeout) throws InterruptedException {
        if (timeout > 0) {
            long ms = toMillis(timeout);
            obj.wait(ms, excessNanos(timeout, ms));
        }
    }

    public void timedJoin(Thread thread, long timeout) throws InterruptedException {
        if (timeout > 0) {
            long ms = toMillis(timeout);
            thread.join(ms, excessNanos(timeout, ms));
        }
    }

    public void sleep(long timeout) throws InterruptedException {
        if (timeout > 0) {
            long ms = toMillis(timeout);
            Thread.sleep(ms, excessNanos(timeout, ms));
        }
    }
}
