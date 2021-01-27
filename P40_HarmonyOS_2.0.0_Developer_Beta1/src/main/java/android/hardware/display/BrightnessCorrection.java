package android.hardware.display;

import android.annotation.SystemApi;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.MathUtils;
import com.android.internal.util.XmlUtils;
import java.io.IOException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

@SystemApi
public final class BrightnessCorrection implements Parcelable {
    public static final Parcelable.Creator<BrightnessCorrection> CREATOR = new Parcelable.Creator<BrightnessCorrection>() {
        /* class android.hardware.display.BrightnessCorrection.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public BrightnessCorrection createFromParcel(Parcel in) {
            if (in.readInt() != 1) {
                return null;
            }
            return ScaleAndTranslateLog.readFromParcel(in);
        }

        @Override // android.os.Parcelable.Creator
        public BrightnessCorrection[] newArray(int size) {
            return new BrightnessCorrection[size];
        }
    };
    private static final int SCALE_AND_TRANSLATE_LOG = 1;
    private static final String TAG_SCALE_AND_TRANSLATE_LOG = "scale-and-translate-log";
    private BrightnessCorrectionImplementation mImplementation;

    /* access modifiers changed from: private */
    public interface BrightnessCorrectionImplementation {
        float apply(float f);

        void saveToXml(XmlSerializer xmlSerializer) throws IOException;

        String toString();

        void writeToParcel(Parcel parcel);
    }

    private BrightnessCorrection(BrightnessCorrectionImplementation implementation) {
        this.mImplementation = implementation;
    }

    public static BrightnessCorrection createScaleAndTranslateLog(float scale, float translate) {
        return new BrightnessCorrection(new ScaleAndTranslateLog(scale, translate));
    }

    public float apply(float brightness) {
        return this.mImplementation.apply(brightness);
    }

    public String toString() {
        return this.mImplementation.toString();
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof BrightnessCorrection)) {
            return false;
        }
        return ((BrightnessCorrection) o).mImplementation.equals(this.mImplementation);
    }

    public int hashCode() {
        return this.mImplementation.hashCode();
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        this.mImplementation.writeToParcel(dest);
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    public void saveToXml(XmlSerializer serializer) throws IOException {
        this.mImplementation.saveToXml(serializer);
    }

    public static BrightnessCorrection loadFromXml(XmlPullParser parser) throws IOException, XmlPullParserException {
        int depth = parser.getDepth();
        while (XmlUtils.nextElementWithin(parser, depth)) {
            if (TAG_SCALE_AND_TRANSLATE_LOG.equals(parser.getName())) {
                return ScaleAndTranslateLog.loadFromXml(parser);
            }
        }
        return null;
    }

    /* access modifiers changed from: private */
    public static float loadFloatFromXml(XmlPullParser parser, String attribute) {
        try {
            return Float.parseFloat(parser.getAttributeValue(null, attribute));
        } catch (NullPointerException | NumberFormatException e) {
            return Float.NaN;
        }
    }

    /* access modifiers changed from: private */
    public static class ScaleAndTranslateLog implements BrightnessCorrectionImplementation {
        private static final String ATTR_SCALE = "scale";
        private static final String ATTR_TRANSLATE = "translate";
        private static final float MAX_SCALE = 2.0f;
        private static final float MAX_TRANSLATE = 0.7f;
        private static final float MIN_SCALE = 0.5f;
        private static final float MIN_TRANSLATE = -0.6f;
        private final float mScale;
        private final float mTranslate;

        ScaleAndTranslateLog(float scale, float translate) {
            if (Float.isNaN(scale) || Float.isNaN(translate)) {
                throw new IllegalArgumentException("scale and translate must be numbers");
            }
            this.mScale = MathUtils.constrain(scale, (float) MIN_SCALE, 2.0f);
            this.mTranslate = MathUtils.constrain(translate, (float) MIN_TRANSLATE, (float) MAX_TRANSLATE);
        }

        @Override // android.hardware.display.BrightnessCorrection.BrightnessCorrectionImplementation
        public float apply(float brightness) {
            return MathUtils.exp((this.mScale * MathUtils.log(brightness)) + this.mTranslate);
        }

        @Override // android.hardware.display.BrightnessCorrection.BrightnessCorrectionImplementation
        public String toString() {
            return "ScaleAndTranslateLog(" + this.mScale + ", " + this.mTranslate + ")";
        }

        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            if (!(o instanceof ScaleAndTranslateLog)) {
                return false;
            }
            ScaleAndTranslateLog other = (ScaleAndTranslateLog) o;
            if (other.mScale == this.mScale && other.mTranslate == this.mTranslate) {
                return true;
            }
            return false;
        }

        public int hashCode() {
            return (((1 * 31) + Float.hashCode(this.mScale)) * 31) + Float.hashCode(this.mTranslate);
        }

        @Override // android.hardware.display.BrightnessCorrection.BrightnessCorrectionImplementation
        public void writeToParcel(Parcel dest) {
            dest.writeInt(1);
            dest.writeFloat(this.mScale);
            dest.writeFloat(this.mTranslate);
        }

        @Override // android.hardware.display.BrightnessCorrection.BrightnessCorrectionImplementation
        public void saveToXml(XmlSerializer serializer) throws IOException {
            serializer.startTag(null, BrightnessCorrection.TAG_SCALE_AND_TRANSLATE_LOG);
            serializer.attribute(null, "scale", Float.toString(this.mScale));
            serializer.attribute(null, ATTR_TRANSLATE, Float.toString(this.mTranslate));
            serializer.endTag(null, BrightnessCorrection.TAG_SCALE_AND_TRANSLATE_LOG);
        }

        static BrightnessCorrection readFromParcel(Parcel in) {
            return BrightnessCorrection.createScaleAndTranslateLog(in.readFloat(), in.readFloat());
        }

        static BrightnessCorrection loadFromXml(XmlPullParser parser) throws IOException, XmlPullParserException {
            return BrightnessCorrection.createScaleAndTranslateLog(BrightnessCorrection.loadFloatFromXml(parser, "scale"), BrightnessCorrection.loadFloatFromXml(parser, ATTR_TRANSLATE));
        }
    }
}
