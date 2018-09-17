package huawei.com.android.server.policy;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import huawei.android.widget.RoundProgressBar;
import java.text.NumberFormat;

public class HwHotaView extends RelativeLayout {
    private RoundProgressBar mRoundProgressBar;
    private TextView mTextView;
    private TextView mTextView1;
    private View root;

    public void init() {
        this.root = findViewById(34603181);
        this.mRoundProgressBar = (RoundProgressBar) this.root.findViewById(34603182);
        this.mTextView = (TextView) this.root.findViewById(34603183);
        this.mTextView1 = (TextView) this.root.findViewById(34603077);
    }

    public HwHotaView(Context context) {
        this(context, null);
    }

    public HwHotaView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.root = null;
    }

    public void update(String str, int progress) {
        this.mTextView.setText(str);
        NumberFormat pnf = NumberFormat.getPercentInstance();
        this.mTextView.setText(str);
        if (progress > 0) {
            this.mTextView1.setText(pnf.format(((double) progress) / 100.0d));
        }
        this.mRoundProgressBar.setProgress(progress);
    }
}
