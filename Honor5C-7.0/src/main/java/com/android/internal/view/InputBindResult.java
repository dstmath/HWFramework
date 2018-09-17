package com.android.internal.view;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.view.InputChannel;
import com.android.internal.view.IInputMethodSession.Stub;

public final class InputBindResult implements Parcelable {
    public static final Creator<InputBindResult> CREATOR = null;
    static final String TAG = "InputBindResult";
    public final InputChannel channel;
    public final String id;
    public final IInputMethodSession method;
    public final int sequence;
    public final int userActionNotificationSequenceNumber;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.view.InputBindResult.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.view.InputBindResult.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.view.InputBindResult.<clinit>():void");
    }

    public InputBindResult(IInputMethodSession _method, InputChannel _channel, String _id, int _sequence, int _userActionNotificationSequenceNumber) {
        this.method = _method;
        this.channel = _channel;
        this.id = _id;
        this.sequence = _sequence;
        this.userActionNotificationSequenceNumber = _userActionNotificationSequenceNumber;
    }

    InputBindResult(Parcel source) {
        this.method = Stub.asInterface(source.readStrongBinder());
        if (source.readInt() != 0) {
            this.channel = (InputChannel) InputChannel.CREATOR.createFromParcel(source);
        } else {
            this.channel = null;
        }
        this.id = source.readString();
        this.sequence = source.readInt();
        this.userActionNotificationSequenceNumber = source.readInt();
    }

    public String toString() {
        return "InputBindResult{" + this.method + " " + this.id + " sequence:" + this.sequence + " userActionNotificationSequenceNumber:" + this.userActionNotificationSequenceNumber + "}";
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStrongInterface(this.method);
        if (this.channel != null) {
            dest.writeInt(1);
            this.channel.writeToParcel(dest, flags);
        } else {
            dest.writeInt(0);
        }
        dest.writeString(this.id);
        dest.writeInt(this.sequence);
        dest.writeInt(this.userActionNotificationSequenceNumber);
    }

    public int describeContents() {
        return this.channel != null ? this.channel.describeContents() : 0;
    }
}
