package com.android.server.biometrics;

import android.content.Context;
import android.hardware.biometrics.BiometricAuthenticator;
import android.hardware.biometrics.BiometricAuthenticator.Identifier;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Slog;
import android.util.Xml;
import com.android.internal.annotations.GuardedBy;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import libcore.io.IoUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public abstract class BiometricUserState<T extends BiometricAuthenticator.Identifier> {
    private static final String TAG = "UserState";
    @GuardedBy({"this"})
    protected final ArrayList<T> mBiometrics = new ArrayList<>();
    protected final Context mContext;
    protected final File mFile;
    private final Runnable mWriteStateRunnable = new Runnable() {
        /* class com.android.server.biometrics.BiometricUserState.AnonymousClass1 */

        @Override // java.lang.Runnable
        public void run() {
            BiometricUserState.this.doWriteState();
        }
    };

    /* access modifiers changed from: protected */
    public abstract void doWriteState();

    /* access modifiers changed from: protected */
    public abstract String getBiometricFile();

    /* access modifiers changed from: protected */
    public abstract String getBiometricsTag();

    /* access modifiers changed from: protected */
    public abstract ArrayList<T> getCopy(ArrayList<T> arrayList);

    /* access modifiers changed from: protected */
    public abstract int getNameTemplateResource();

    /* access modifiers changed from: protected */
    public abstract void parseBiometricsLocked(XmlPullParser xmlPullParser) throws IOException, XmlPullParserException;

    public BiometricUserState(Context context, int userId) {
        this.mFile = getFileForUser(userId);
        this.mContext = context;
        synchronized (this) {
            readStateSyncLocked();
        }
    }

    public BiometricUserState(Context context, int userId, int deviceIndex) {
        this.mFile = getFileForUser(userId, deviceIndex);
        this.mContext = context;
        synchronized (this) {
            readStateSyncLocked();
        }
    }

    public void addBiometric(T identifier) {
        synchronized (this) {
            this.mBiometrics.add(identifier);
            scheduleWriteStateLocked();
        }
    }

    public void removeBiometric(int biometricId) {
        synchronized (this) {
            int i = 0;
            while (true) {
                if (i >= this.mBiometrics.size()) {
                    break;
                } else if (this.mBiometrics.get(i).getBiometricId() == biometricId) {
                    this.mBiometrics.remove(i);
                    scheduleWriteStateLocked();
                    break;
                } else {
                    i++;
                }
            }
        }
    }

    public void renameBiometric(int biometricId, CharSequence name) {
        synchronized (this) {
            int i = 0;
            while (true) {
                if (i >= this.mBiometrics.size()) {
                    break;
                } else if (this.mBiometrics.get(i).getBiometricId() == biometricId) {
                    this.mBiometrics.get(i).setName(name);
                    scheduleWriteStateLocked();
                    break;
                } else {
                    i++;
                }
            }
        }
    }

    public boolean isBiometricExist(int biometricId) {
        synchronized (this) {
            int size = this.mBiometrics.size();
            for (int i = 0; i < size; i++) {
                if (this.mBiometrics.get(i).getBiometricId() == biometricId) {
                    return true;
                }
            }
            return false;
        }
    }

    public List<T> getBiometrics() {
        ArrayList<T> copy;
        synchronized (this) {
            copy = getCopy(this.mBiometrics);
        }
        return copy;
    }

    public String getUniqueName() {
        int guess = 1;
        while (true) {
            String name = this.mContext.getString(getNameTemplateResource(), Integer.valueOf(guess));
            if (isUnique(name)) {
                return name;
            }
            guess++;
        }
    }

    private boolean isUnique(String name) {
        Iterator<T> it = this.mBiometrics.iterator();
        while (it.hasNext()) {
            if (it.next().getName().equals(name)) {
                return false;
            }
        }
        return true;
    }

    private File getFileForUser(int userId) {
        return new File(Environment.getUserSystemDirectory(userId), getBiometricFile());
    }

    private File getFileForUser(int userId, int deviceIndex) {
        return new File(Environment.getUserSystemDirectory(userId), getBiometricFile(deviceIndex));
    }

    /* access modifiers changed from: protected */
    public String getBiometricFile(int deviceIndex) {
        return "";
    }

    private void scheduleWriteStateLocked() {
        AsyncTask.execute(this.mWriteStateRunnable);
    }

    @GuardedBy({"this"})
    private void readStateSyncLocked() {
        if (this.mFile.exists()) {
            try {
                FileInputStream in = new FileInputStream(this.mFile);
                try {
                    XmlPullParser parser = Xml.newPullParser();
                    parser.setInput(in, null);
                    parseStateLocked(parser);
                } catch (IOException | XmlPullParserException e) {
                    Slog.e(TAG, "Failed parsing settings fingerprint file");
                } catch (Throwable th) {
                    IoUtils.closeQuietly(in);
                    throw th;
                }
                IoUtils.closeQuietly(in);
            } catch (FileNotFoundException e2) {
                Slog.i(TAG, "No fingerprint state");
            }
        }
    }

    @GuardedBy({"this"})
    private void parseStateLocked(XmlPullParser parser) throws IOException, XmlPullParserException {
        int outerDepth = parser.getDepth();
        while (true) {
            int type = parser.next();
            if (type == 1) {
                return;
            }
            if (type == 3 && parser.getDepth() <= outerDepth) {
                return;
            }
            if (!(type == 3 || type == 4 || !parser.getName().equals(getBiometricsTag()))) {
                parseBiometricsLocked(parser);
            }
        }
    }
}
