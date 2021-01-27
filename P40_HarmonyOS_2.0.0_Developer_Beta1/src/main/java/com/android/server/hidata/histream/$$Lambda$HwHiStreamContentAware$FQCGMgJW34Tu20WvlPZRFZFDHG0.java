package com.android.server.hidata.histream;

import android.media.AudioRecordingConfiguration;
import java.util.function.Predicate;

/* renamed from: com.android.server.hidata.histream.-$$Lambda$HwHiStreamContentAware$FQCGMgJW34Tu20WvlPZRFZFDHG0  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$HwHiStreamContentAware$FQCGMgJW34Tu20WvlPZRFZFDHG0 implements Predicate {
    public static final /* synthetic */ $$Lambda$HwHiStreamContentAware$FQCGMgJW34Tu20WvlPZRFZFDHG0 INSTANCE = new $$Lambda$HwHiStreamContentAware$FQCGMgJW34Tu20WvlPZRFZFDHG0();

    private /* synthetic */ $$Lambda$HwHiStreamContentAware$FQCGMgJW34Tu20WvlPZRFZFDHG0() {
    }

    @Override // java.util.function.Predicate
    public final boolean test(Object obj) {
        return HwHiStreamContentAware.lambda$isRecordingInVoip$0((AudioRecordingConfiguration) obj);
    }
}
