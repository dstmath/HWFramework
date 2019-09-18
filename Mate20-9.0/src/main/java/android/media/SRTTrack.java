package android.media;

import android.media.SubtitleTrack;
import android.os.Handler;
import android.os.Parcel;
import android.util.Log;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

/* compiled from: SRTRenderer */
class SRTTrack extends WebVttTrack {
    private static final int KEY_LOCAL_SETTING = 102;
    private static final int KEY_START_TIME = 7;
    private static final int KEY_STRUCT_TEXT = 16;
    private static final int MEDIA_TIMED_TEXT = 99;
    private static final String TAG = "SRTTrack";
    private final Handler mEventHandler;

    SRTTrack(WebVttRenderingWidget renderingWidget, MediaFormat format) {
        super(renderingWidget, format);
        this.mEventHandler = null;
    }

    SRTTrack(Handler eventHandler, MediaFormat format) {
        super(null, format);
        this.mEventHandler = eventHandler;
    }

    /* access modifiers changed from: protected */
    public void onData(SubtitleData data) {
        try {
            TextTrackCue cue = new TextTrackCue();
            cue.mStartTimeMs = data.getStartTimeUs() / 1000;
            cue.mEndTimeMs = (data.getStartTimeUs() + data.getDurationUs()) / 1000;
            String[] lines = new String(data.getData(), "UTF-8").split("\\r?\\n");
            cue.mLines = new TextTrackCueSpan[lines.length][];
            int length = lines.length;
            int i = 0;
            int i2 = 0;
            while (i2 < length) {
                cue.mLines[i] = new TextTrackCueSpan[]{new TextTrackCueSpan(lines[i2], -1)};
                i2++;
                i++;
            }
            addCue(cue);
        } catch (UnsupportedEncodingException e) {
            Log.w(TAG, "subtitle data is not UTF-8 encoded: " + e);
        }
    }

    public void onData(byte[] data, boolean eos, long runID) {
        try {
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(data), "UTF-8"));
                while (true) {
                    String readLine = br.readLine();
                    String str = readLine;
                    if (readLine == null) {
                        break;
                    }
                    String header = br.readLine();
                    if (header == null) {
                        break;
                    }
                    TextTrackCue cue = new TextTrackCue();
                    String[] startEnd = header.split("-->");
                    cue.mStartTimeMs = parseMs(startEnd[0]);
                    int i = 1;
                    cue.mEndTimeMs = parseMs(startEnd[1]);
                    List<String> paragraph = new ArrayList<>();
                    while (true) {
                        String readLine2 = br.readLine();
                        String s = readLine2;
                        if (readLine2 == null || s.trim().equals("")) {
                            int i2 = 0;
                            cue.mLines = new TextTrackCueSpan[paragraph.size()][];
                            cue.mStrings = (String[]) paragraph.toArray(new String[0]);
                        } else {
                            paragraph.add(s);
                        }
                    }
                    int i22 = 0;
                    cue.mLines = new TextTrackCueSpan[paragraph.size()][];
                    cue.mStrings = (String[]) paragraph.toArray(new String[0]);
                    for (String line : paragraph) {
                        TextTrackCueSpan[] textTrackCueSpanArr = new TextTrackCueSpan[i];
                        List<String> paragraph2 = paragraph;
                        textTrackCueSpanArr[0] = new TextTrackCueSpan(line, -1);
                        TextTrackCueSpan[] span = textTrackCueSpanArr;
                        cue.mStrings[i22] = line;
                        int i3 = i22 + 1;
                        cue.mLines[i22] = span;
                        i22 = i3;
                        paragraph = paragraph2;
                        i = 1;
                    }
                    try {
                        addCue(cue);
                    } catch (UnsupportedEncodingException e) {
                        e = e;
                    } catch (IOException e2) {
                        ioe = e2;
                        Log.e(TAG, ioe.getMessage(), ioe);
                    }
                }
            } catch (UnsupportedEncodingException e3) {
                e = e3;
                Log.w(TAG, "subtitle data is not UTF-8 encoded: " + e);
            } catch (IOException e4) {
                ioe = e4;
                Log.e(TAG, ioe.getMessage(), ioe);
            }
        } catch (UnsupportedEncodingException e5) {
            e = e5;
            byte[] bArr = data;
            Log.w(TAG, "subtitle data is not UTF-8 encoded: " + e);
        } catch (IOException e6) {
            ioe = e6;
            byte[] bArr2 = data;
            Log.e(TAG, ioe.getMessage(), ioe);
        }
    }

    public void updateView(Vector<SubtitleTrack.Cue> activeCues) {
        if (getRenderingWidget() != null) {
            super.updateView(activeCues);
        } else if (this.mEventHandler != null) {
            Iterator<SubtitleTrack.Cue> it = activeCues.iterator();
            while (it.hasNext()) {
                SubtitleTrack.Cue cue = it.next();
                Parcel parcel = Parcel.obtain();
                parcel.writeInt(102);
                parcel.writeInt(7);
                parcel.writeInt((int) cue.mStartTimeMs);
                parcel.writeInt(16);
                StringBuilder sb = new StringBuilder();
                for (String line : ((TextTrackCue) cue).mStrings) {
                    sb.append(line);
                    sb.append(10);
                }
                byte[] buf = sb.toString().getBytes();
                parcel.writeInt(buf.length);
                parcel.writeByteArray(buf);
                this.mEventHandler.sendMessage(this.mEventHandler.obtainMessage(99, 0, 0, parcel));
            }
            activeCues.clear();
        }
    }

    private static long parseMs(String in) {
        return (Long.parseLong(in.split(":")[0].trim()) * 60 * 60 * 1000) + (60 * Long.parseLong(in.split(":")[1].trim()) * 1000) + (1000 * Long.parseLong(in.split(":")[2].split(",")[0].trim())) + Long.parseLong(in.split(":")[2].split(",")[1].trim());
    }
}
