package com.android.commands.am;

import android.app.IActivityManager;
import android.app.IInstrumentationWatcher.Stub;
import android.app.IUiAutomationConnection;
import android.app.UiAutomationConnection;
import android.content.ComponentName;
import android.content.pm.IPackageManager;
import android.content.pm.InstrumentationInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.ServiceManager;
import android.util.AndroidException;
import android.util.proto.ProtoOutputStream;
import android.view.IWindowManager;
import com.android.commands.am.InstrumentationData.ResultsBundleEntry;
import com.android.commands.am.InstrumentationData.Session;
import com.android.commands.am.InstrumentationData.SessionStatus;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Instrument {
    public String abi = null;
    public Bundle args = new Bundle();
    public String componentNameArg;
    private final IActivityManager mAm;
    private final IPackageManager mPm;
    private final IWindowManager mWm;
    public boolean noWindowAnimation = false;
    public String profileFile = null;
    public boolean proto = false;
    public boolean rawMode = false;
    public int userId = -2;
    public boolean wait = false;

    private class InstrumentationWatcher extends Stub {
        private boolean mFinished = false;
        private final StatusReporter mReporter;

        public InstrumentationWatcher(StatusReporter reporter) {
            this.mReporter = reporter;
        }

        public void instrumentationStatus(ComponentName name, int resultCode, Bundle results) {
            synchronized (this) {
                this.mReporter.onInstrumentationStatusLocked(name, resultCode, results);
                notifyAll();
            }
        }

        public void instrumentationFinished(ComponentName name, int resultCode, Bundle results) {
            synchronized (this) {
                this.mReporter.onInstrumentationFinishedLocked(name, resultCode, results);
                this.mFinished = true;
                notifyAll();
            }
        }

        public boolean waitForFinish() {
            synchronized (this) {
                while (!this.mFinished) {
                    try {
                        if (Instrument.this.mAm.asBinder().pingBinder()) {
                            wait(1000);
                        } else {
                            return false;
                        }
                    } catch (InterruptedException e) {
                        throw new IllegalStateException(e);
                    }
                }
                return true;
            }
        }
    }

    private interface StatusReporter {
        void onError(String str, boolean z);

        void onInstrumentationFinishedLocked(ComponentName componentName, int i, Bundle bundle);

        void onInstrumentationStatusLocked(ComponentName componentName, int i, Bundle bundle);
    }

    private class ProtoStatusReporter implements StatusReporter {
        /* synthetic */ ProtoStatusReporter(Instrument this$0, ProtoStatusReporter -this1) {
            this();
        }

        private ProtoStatusReporter() {
        }

        public void onInstrumentationStatusLocked(ComponentName name, int resultCode, Bundle results) {
            ProtoOutputStream proto = new ProtoOutputStream();
            long token = proto.startRepeatedObject(2272037699585L);
            proto.writeSInt32(1129576398851L, resultCode);
            writeBundle(proto, 1172526071812L, results);
            proto.endRepeatedObject(token);
            writeProtoToStdout(proto);
        }

        public void onInstrumentationFinishedLocked(ComponentName name, int resultCode, Bundle results) {
            ProtoOutputStream proto = new ProtoOutputStream();
            long token = proto.startObject(Session.SESSION_STATUS);
            proto.writeEnum(SessionStatus.STATUS_CODE, 0);
            proto.writeSInt32(1129576398851L, resultCode);
            writeBundle(proto, 1172526071812L, results);
            proto.endObject(token);
            writeProtoToStdout(proto);
        }

        public void onError(String errorText, boolean commandError) {
            ProtoOutputStream proto = new ProtoOutputStream();
            long token = proto.startObject(Session.SESSION_STATUS);
            proto.writeEnum(SessionStatus.STATUS_CODE, 1);
            proto.writeString(1159641169922L, errorText);
            proto.endObject(token);
            writeProtoToStdout(proto);
        }

        private void writeBundle(ProtoOutputStream proto, long fieldId, Bundle bundle) {
            long bundleToken = proto.startObject(fieldId);
            for (String key : bundle.keySet()) {
                long entryToken = proto.startRepeatedObject(2272037699585L);
                proto.writeString(ResultsBundleEntry.KEY, key);
                Object val = bundle.get(key);
                if (val instanceof String) {
                    proto.writeString(1159641169922L, (String) val);
                } else if (val instanceof Byte) {
                    proto.writeSInt32(1129576398851L, ((Byte) val).intValue());
                } else if (val instanceof Double) {
                    proto.writeDouble(ResultsBundleEntry.VALUE_DOUBLE, ((Double) val).doubleValue());
                } else if (val instanceof Float) {
                    proto.writeFloat(ResultsBundleEntry.VALUE_FLOAT, ((Float) val).floatValue());
                } else if (val instanceof Integer) {
                    proto.writeSInt32(1129576398851L, ((Integer) val).intValue());
                } else if (val instanceof Long) {
                    proto.writeSInt64(ResultsBundleEntry.VALUE_LONG, ((Long) val).longValue());
                } else if (val instanceof Short) {
                    proto.writeSInt32(1129576398851L, ((Short) val).intValue());
                } else if (val instanceof Bundle) {
                    writeBundle(proto, ResultsBundleEntry.VALUE_BUNDLE, (Bundle) val);
                }
                proto.endRepeatedObject(entryToken);
            }
            proto.endObject(bundleToken);
        }

        private void writeProtoToStdout(ProtoOutputStream proto) {
            try {
                System.out.write(proto.getBytes());
                System.out.flush();
            } catch (IOException ex) {
                System.err.println("Error writing finished response: ");
                ex.printStackTrace(System.err);
            }
        }
    }

    private class TextStatusReporter implements StatusReporter {
        private boolean mRawMode;

        public TextStatusReporter(boolean rawMode) {
            this.mRawMode = rawMode;
        }

        public void onInstrumentationStatusLocked(ComponentName name, int resultCode, Bundle results) {
            String pretty = null;
            if (!(this.mRawMode || results == null)) {
                pretty = results.getString("stream");
            }
            if (pretty != null) {
                System.out.print(pretty);
                return;
            }
            if (results != null) {
                for (String key : results.keySet()) {
                    System.out.println("INSTRUMENTATION_STATUS: " + key + "=" + results.get(key));
                }
            }
            System.out.println("INSTRUMENTATION_STATUS_CODE: " + resultCode);
        }

        public void onInstrumentationFinishedLocked(ComponentName name, int resultCode, Bundle results) {
            String pretty = null;
            if (!(this.mRawMode || results == null)) {
                pretty = results.getString("stream");
            }
            if (pretty != null) {
                System.out.println(pretty);
                return;
            }
            if (results != null) {
                for (String key : results.keySet()) {
                    System.out.println("INSTRUMENTATION_RESULT: " + key + "=" + results.get(key));
                }
            }
            System.out.println("INSTRUMENTATION_CODE: " + resultCode);
        }

        public void onError(String errorText, boolean commandError) {
            if (!commandError) {
                System.out.println(errorText);
            }
        }
    }

    public Instrument(IActivityManager am, IPackageManager pm) {
        this.mAm = am;
        this.mPm = pm;
        this.mWm = IWindowManager.Stub.asInterface(ServiceManager.getService("window"));
    }

    private ComponentName parseComponentName(String cnArg) throws Exception {
        if (cnArg.contains("/")) {
            ComponentName cn = ComponentName.unflattenFromString(cnArg);
            if (cn != null) {
                return cn;
            }
            throw new IllegalArgumentException("Bad component name: " + cnArg);
        }
        int i;
        List<InstrumentationInfo> infos = this.mPm.queryInstrumentation(null, 0).getList();
        int numInfos = infos == null ? 0 : infos.size();
        ArrayList<ComponentName> cns = new ArrayList();
        for (i = 0; i < numInfos; i++) {
            InstrumentationInfo info = (InstrumentationInfo) infos.get(i);
            ComponentName c = new ComponentName(info.packageName, info.name);
            if (cnArg.equals(info.packageName)) {
                cns.add(c);
            }
        }
        if (cns.size() == 0) {
            throw new IllegalArgumentException("No instrumentation found for: " + cnArg);
        } else if (cns.size() == 1) {
            return (ComponentName) cns.get(0);
        } else {
            StringBuilder cnsStr = new StringBuilder();
            int numCns = cns.size();
            for (i = 0; i < numCns; i++) {
                cnsStr.append(((ComponentName) cns.get(i)).flattenToString());
                cnsStr.append(", ");
            }
            cnsStr.setLength(cnsStr.length() - 2);
            throw new IllegalArgumentException("Found multiple instrumentations: " + cnsStr.toString());
        }
    }

    public void run() throws Exception {
        StatusReporter reporter = null;
        float[] oldAnims = null;
        try {
            if (this.proto) {
                reporter = new ProtoStatusReporter(this, null);
            } else if (this.wait) {
                reporter = new TextStatusReporter(this.rawMode);
            }
            Object watcher = null;
            IUiAutomationConnection iUiAutomationConnection = null;
            if (reporter != null) {
                watcher = new InstrumentationWatcher(reporter);
                iUiAutomationConnection = new UiAutomationConnection();
            }
            if (this.noWindowAnimation) {
                oldAnims = this.mWm.getAnimationScales();
                this.mWm.setAnimationScale(0, 0.0f);
                this.mWm.setAnimationScale(1, 0.0f);
                this.mWm.setAnimationScale(2, 0.0f);
            }
            ComponentName cn = parseComponentName(this.componentNameArg);
            if (this.abi != null) {
                boolean matched = false;
                for (String supportedAbi : Build.SUPPORTED_ABIS) {
                    if (supportedAbi.equals(this.abi)) {
                        matched = true;
                        break;
                    }
                }
                if (!matched) {
                    throw new AndroidException("INSTRUMENTATION_FAILED: Unsupported instruction set " + this.abi);
                }
            }
            if (!this.mAm.startInstrumentation(cn, this.profileFile, 0, this.args, watcher, iUiAutomationConnection, this.userId, this.abi)) {
                throw new AndroidException("INSTRUMENTATION_FAILED: " + cn.flattenToString());
            } else if (watcher == null || watcher.waitForFinish()) {
                if (oldAnims != null) {
                    this.mWm.setAnimationScales(oldAnims);
                }
            } else {
                reporter.onError("INSTRUMENTATION_ABORTED: System has crashed.", false);
                if (oldAnims != null) {
                    this.mWm.setAnimationScales(oldAnims);
                }
            }
        } catch (Exception ex) {
            if (reporter != null) {
                reporter.onError(ex.getMessage(), true);
            }
            throw ex;
        } catch (Throwable th) {
            if (null != null) {
                this.mWm.setAnimationScales(null);
            }
        }
    }
}
