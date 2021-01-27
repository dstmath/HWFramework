package android.freeform.adapter;

import android.app.ActivityManager;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.util.Flog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.hwext.internal.R;
import com.huawei.android.statistical.StatisticalConstant;
import java.util.ArrayList;
import java.util.List;

public class FloatItemAdapter extends BaseAdapter implements View.OnClickListener {
    private static final int COLLECTION_CAPACITY_SHORT = 16;
    private static final int DEVICE_DEFALUT_DENSITY = 160;
    private static final float LCD_DENSITY = ((float) SystemProperties.getInt("ro.sf.lcd_density", 0));
    private static final float REAL_LCD_DENSITY = ((float) SystemProperties.getInt("ro.sf.real_lcd_density", SystemProperties.getInt("ro.sf.lcd_density", 0)));
    private Context mContext;
    private float mDeviceDefalutDensity = 0.0f;
    private float mDisplayDpi = 0.0f;
    private List<FloatItem> mFloatItemList = new ArrayList(16);

    public FloatItemAdapter(List<FloatItem> data, Context context) {
        this.mContext = context;
        this.mFloatItemList = data;
        this.mDisplayDpi = (float) SystemProperties.getInt("persist.sys.dpi", 0);
        this.mDeviceDefalutDensity = LCD_DENSITY / 160.0f;
    }

    private static class ViewHolder {
        private ImageView image;
        private Intent intent;
        private TextView title;
        private int userId;

        private ViewHolder() {
        }
    }

    @Override // android.view.View.OnClickListener
    public void onClick(View view) {
        ViewHolder vh;
        if ((view.getTag() instanceof ViewHolder) && (vh = (ViewHolder) view.getTag()) != null && vh.intent != null) {
            ActivityOptions opts = ActivityOptions.makeBasic();
            ActivityManager.RunningTaskInfo runningTaskInfo = getRunningTaskInfo();
            if (!(runningTaskInfo == null || runningTaskInfo.topActivity == null || runningTaskInfo.configuration.windowConfiguration.getWindowingMode() != 1)) {
                String currentPkg = runningTaskInfo.topActivity.getPackageName();
                Flog.bdReport((int) StatisticalConstant.TYPE_FREEFROM_START_FROM_TRIPLE_FINGERS_COUNT, "{pkg1:" + currentPkg + ",pkg2:" + vh.intent.getPackage() + "}");
            }
            opts.setLaunchWindowingMode(5);
            this.mContext.startActivityAsUser(vh.intent.addFlags(268435456), opts.toBundle(), new UserHandle(vh.userId));
        }
    }

    @Override // android.widget.Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if (convertView == null) {
            convertView = LayoutInflater.from(this.mContext).inflate(R.layout.hw_floatlist_item_layout, (ViewGroup) null);
            viewHolder = new ViewHolder();
            ImageView iv = (ImageView) convertView.findViewById(R.id.hw_floatlist_item_image);
            ViewGroup.LayoutParams ivlp = iv.getLayoutParams();
            ivlp.width = (int) ((((float) ivlp.width) * this.mDeviceDefalutDensity) / getDisplayDensity());
            ivlp.height = (int) ((((float) ivlp.height) * this.mDeviceDefalutDensity) / getDisplayDensity());
            viewHolder.image = iv;
            TextView tv = (TextView) convertView.findViewById(R.id.hw_floatlist_item_title);
            tv.setTextSize(0, (tv.getTextSize() * this.mDeviceDefalutDensity) / getDisplayDensity());
            viewHolder.title = tv;
            convertView.setTag(viewHolder);
        } else if (convertView.getTag() instanceof ViewHolder) {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        FloatItem item = this.mFloatItemList.get(position);
        if (!(viewHolder == null || item == null)) {
            viewHolder.title.setText(item.getLabel());
            viewHolder.image.setImageDrawable(item.getIcon());
            viewHolder.intent = item.getIntent();
            viewHolder.userId = item.getUserId();
        }
        convertView.setOnClickListener(this);
        return convertView;
    }

    @Override // android.widget.Adapter
    public int getCount() {
        List<FloatItem> list = this.mFloatItemList;
        if (list != null) {
            return list.size();
        }
        return 0;
    }

    @Override // android.widget.Adapter
    public Object getItem(int position) {
        return this.mFloatItemList.get(position);
    }

    @Override // android.widget.Adapter
    public long getItemId(int position) {
        return (long) position;
    }

    public float getDisplayDensity() {
        float f = this.mDisplayDpi;
        if (f == 0.0f) {
            return this.mDeviceDefalutDensity;
        }
        return ((LCD_DENSITY * f) / REAL_LCD_DENSITY) / 160.0f;
    }

    public float getDeviceDefalutDensity() {
        return this.mDeviceDefalutDensity;
    }

    private ActivityManager.RunningTaskInfo getRunningTaskInfo() {
        ActivityManager am = (ActivityManager) this.mContext.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> tasks = null;
        if (am != null) {
            tasks = am.getRunningTasks(1);
        }
        if (tasks == null || tasks.isEmpty()) {
            return null;
        }
        return tasks.get(0);
    }
}
