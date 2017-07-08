package android_maps_conflict_avoidance.com.google.common.graphics.android;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android_maps_conflict_avoidance.com.google.common.graphics.GoogleImage;

public class AndroidAshmemImageFactory extends AndroidImageFactory {

    private static class AndroidAshmemImage extends AndroidImage {
        private static final Options options = null;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android_maps_conflict_avoidance.com.google.common.graphics.android.AndroidAshmemImageFactory.AndroidAshmemImage.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android_maps_conflict_avoidance.com.google.common.graphics.android.AndroidAshmemImageFactory.AndroidAshmemImage.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00eb
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android_maps_conflict_avoidance.com.google.common.graphics.android.AndroidAshmemImageFactory.AndroidAshmemImage.<clinit>():void");
        }

        public AndroidAshmemImage(byte[] imageData, int imageOffset, int imageLength) {
            super(BitmapFactory.decodeByteArray(imageData, imageOffset, imageLength, options));
        }
    }

    public AndroidAshmemImageFactory(Context context) {
        super(context);
    }

    public GoogleImage createImage(byte[] imageData, int imageOffset, int imageLength) {
        return new AndroidAshmemImage(imageData, imageOffset, imageLength);
    }
}
