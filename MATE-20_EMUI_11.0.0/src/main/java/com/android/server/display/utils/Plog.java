package com.android.server.display.utils;

import android.util.Slog;

public abstract class Plog {
    private long mId;

    /* access modifiers changed from: protected */
    public abstract void emit(String str);

    public static Plog createSystemPlog(String tag) {
        return new SystemPlog(tag);
    }

    public Plog start(String title) {
        this.mId = System.currentTimeMillis();
        write(formatTitle(title));
        return this;
    }

    public Plog logPoint(String name, float x, float y) {
        write(formatPoint(name, x, y));
        return this;
    }

    public Plog logCurve(String name, float[] xs, float[] ys) {
        write(formatCurve(name, xs, ys));
        return this;
    }

    private String formatTitle(String title) {
        return "title: " + title;
    }

    private String formatPoint(String name, float x, float y) {
        return "point: " + name + ": (" + x + "," + y + ")";
    }

    private String formatCurve(String name, float[] xs, float[] ys) {
        StringBuilder sb = new StringBuilder();
        sb.append("curve: " + name + ": [");
        int n = xs.length <= ys.length ? xs.length : ys.length;
        for (int i = 0; i < n; i++) {
            sb.append("(" + xs[i] + "," + ys[i] + "),");
        }
        sb.append("]");
        return sb.toString();
    }

    private void write(String message) {
        emit("[PLOG " + this.mId + "] " + message);
    }

    public static class SystemPlog extends Plog {
        private final String mTag;

        public SystemPlog(String tag) {
            this.mTag = tag;
        }

        /* access modifiers changed from: protected */
        @Override // com.android.server.display.utils.Plog
        public void emit(String message) {
            Slog.d(this.mTag, message);
        }
    }
}
