package android.filterpacks.performance;

import android.filterfw.core.Filter;
import android.filterfw.core.FilterContext;
import android.filterfw.core.Frame;
import android.filterfw.core.FrameFormat;
import android.filterfw.core.GenerateFieldPort;
import android.filterfw.format.ObjectFormat;
import android.os.SystemClock;

public class ThroughputFilter extends Filter {
    private long mLastTime = 0;
    private FrameFormat mOutputFormat;
    @GenerateFieldPort(hasDefault = true, name = "period")
    private int mPeriod = 5;
    private int mPeriodFrameCount = 0;
    private int mTotalFrameCount = 0;

    public ThroughputFilter(String name) {
        super(name);
    }

    public void setupPorts() {
        addInputPort("frame");
        this.mOutputFormat = ObjectFormat.fromClass(Throughput.class, 1);
        addOutputBasedOnInput("frame", "frame");
        addOutputPort("throughput", this.mOutputFormat);
    }

    public FrameFormat getOutputFormat(String portName, FrameFormat inputFormat) {
        return inputFormat;
    }

    public void open(FilterContext env) {
        this.mTotalFrameCount = 0;
        this.mPeriodFrameCount = 0;
        this.mLastTime = 0;
    }

    public void process(FilterContext context) {
        Frame input = pullInput("frame");
        pushOutput("frame", input);
        this.mTotalFrameCount++;
        this.mPeriodFrameCount++;
        if (this.mLastTime == 0) {
            this.mLastTime = SystemClock.elapsedRealtime();
        }
        long curTime = SystemClock.elapsedRealtime();
        if (curTime - this.mLastTime >= ((long) (this.mPeriod * 1000))) {
            FrameFormat inputFormat = input.getFormat();
            Throughput throughput = new Throughput(this.mTotalFrameCount, this.mPeriodFrameCount, this.mPeriod, inputFormat.getWidth() * inputFormat.getHeight());
            Frame throughputFrame = context.getFrameManager().newFrame(this.mOutputFormat);
            throughputFrame.setObjectValue(throughput);
            pushOutput("throughput", throughputFrame);
            this.mLastTime = curTime;
            this.mPeriodFrameCount = 0;
        }
    }
}
