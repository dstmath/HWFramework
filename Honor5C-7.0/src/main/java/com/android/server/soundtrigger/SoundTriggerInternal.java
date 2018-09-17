package com.android.server.soundtrigger;

import android.hardware.soundtrigger.IRecognitionStatusCallback;
import android.hardware.soundtrigger.SoundTrigger.KeyphraseSoundModel;
import android.hardware.soundtrigger.SoundTrigger.ModuleProperties;
import android.hardware.soundtrigger.SoundTrigger.RecognitionConfig;
import java.io.FileDescriptor;
import java.io.PrintWriter;

public abstract class SoundTriggerInternal {
    public static final int STATUS_ERROR = Integer.MIN_VALUE;
    public static final int STATUS_OK = 0;

    public abstract void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr);

    public abstract ModuleProperties getModuleProperties();

    public abstract int startRecognition(int i, KeyphraseSoundModel keyphraseSoundModel, IRecognitionStatusCallback iRecognitionStatusCallback, RecognitionConfig recognitionConfig);

    public abstract int stopRecognition(int i, IRecognitionStatusCallback iRecognitionStatusCallback);

    public abstract int unloadKeyphraseModel(int i);
}
