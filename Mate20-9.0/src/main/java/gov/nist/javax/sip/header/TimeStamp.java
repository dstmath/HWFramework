package gov.nist.javax.sip.header;

import gov.nist.core.Separators;
import javax.sip.InvalidArgumentException;
import javax.sip.header.TimeStampHeader;

public class TimeStamp extends SIPHeader implements TimeStampHeader {
    private static final long serialVersionUID = -3711322366481232720L;
    protected int delay;
    protected float delayFloat;
    protected long timeStamp;
    private float timeStampFloat;

    public TimeStamp() {
        super("Timestamp");
        this.timeStamp = -1;
        this.delay = -1;
        this.delayFloat = -1.0f;
        this.timeStampFloat = -1.0f;
        this.delay = -1;
    }

    private String getTimeStampAsString() {
        if (this.timeStamp == -1 && this.timeStampFloat == -1.0f) {
            return "";
        }
        if (this.timeStamp != -1) {
            return Long.toString(this.timeStamp);
        }
        return Float.toString(this.timeStampFloat);
    }

    private String getDelayAsString() {
        if (this.delay == -1 && this.delayFloat == -1.0f) {
            return "";
        }
        if (this.delay != -1) {
            return Integer.toString(this.delay);
        }
        return Float.toString(this.delayFloat);
    }

    public String encodeBody() {
        StringBuffer retval = new StringBuffer();
        String s1 = getTimeStampAsString();
        String s2 = getDelayAsString();
        if (s1.equals("") && s2.equals("")) {
            return "";
        }
        if (!s1.equals("")) {
            retval.append(s1);
        }
        if (!s2.equals("")) {
            retval.append(Separators.SP);
            retval.append(s2);
        }
        return retval.toString();
    }

    public boolean hasDelay() {
        return this.delay != -1;
    }

    public void removeDelay() {
        this.delay = -1;
    }

    public void setTimeStamp(float timeStamp2) throws InvalidArgumentException {
        if (timeStamp2 >= 0.0f) {
            this.timeStamp = -1;
            this.timeStampFloat = timeStamp2;
            return;
        }
        throw new InvalidArgumentException("JAIN-SIP Exception, TimeStamp, setTimeStamp(), the timeStamp parameter is <0");
    }

    public float getTimeStamp() {
        if (this.timeStampFloat == -1.0f) {
            return Float.valueOf((float) this.timeStamp).floatValue();
        }
        return this.timeStampFloat;
    }

    public float getDelay() {
        return this.delayFloat == -1.0f ? Float.valueOf((float) this.delay).floatValue() : this.delayFloat;
    }

    public void setDelay(float delay2) throws InvalidArgumentException {
        if (delay2 >= 0.0f || delay2 == -1.0f) {
            this.delayFloat = delay2;
            this.delay = -1;
            return;
        }
        throw new InvalidArgumentException("JAIN-SIP Exception, TimeStamp, setDelay(), the delay parameter is <0");
    }

    public long getTime() {
        return this.timeStamp == -1 ? (long) this.timeStampFloat : this.timeStamp;
    }

    public int getTimeDelay() {
        return this.delay == -1 ? (int) this.delayFloat : this.delay;
    }

    public void setTime(long timeStamp2) throws InvalidArgumentException {
        if (timeStamp2 >= -1) {
            this.timeStamp = timeStamp2;
            this.timeStampFloat = -1.0f;
            return;
        }
        throw new InvalidArgumentException("Illegal timestamp");
    }

    public void setTimeDelay(int delay2) throws InvalidArgumentException {
        if (delay2 >= -1) {
            this.delay = delay2;
            this.delayFloat = -1.0f;
            return;
        }
        throw new InvalidArgumentException("Value out of range " + delay2);
    }
}
