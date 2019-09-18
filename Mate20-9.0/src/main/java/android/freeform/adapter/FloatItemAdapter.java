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
import java.util.ArrayList;
import java.util.List;

public class FloatItemAdapter extends BaseAdapter implements View.OnClickListener {
    private static final float LCD_DENSITY = ((float) SystemProperties.getInt("ro.sf.lcd_density", 0));
    private static final float REAL_LCD_DENSITY = ((float) SystemProperties.getInt("ro.sf.real_lcd_density", SystemProperties.getInt("ro.sf.lcd_density", 0)));
    private Context mContext;
    private float mDeviceDefalutDensity = 0.0f;
    private float mDisplayDpi = 0.0f;
    private List<FloatItem> mSource = new ArrayList();

    private static class ViewHolder {
        /* access modifiers changed from: private */
        public ImageView image;
        /* access modifiers changed from: private */
        public Intent intent;
        /* access modifiers changed from: private */
        public TextView title;
        /* access modifiers changed from: private */
        public int userId;

        private ViewHolder() {
        }
    }

    public FloatItemAdapter(List<FloatItem> data, Context context) {
        this.mContext = context;
        this.mSource = data;
        this.mDisplayDpi = (float) SystemProperties.getInt("persist.sys.dpi", 0);
        this.mDeviceDefalutDensity = LCD_DENSITY / 160.0f;
    }

    public void onClick(View view) {
        if (view.getTag() instanceof ViewHolder) {
            ViewHolder vh = (ViewHolder) view.getTag();
            if (vh != null && vh.intent != null) {
                ActivityOptions opts = ActivityOptions.makeBasic();
                ActivityManager.RunningTaskInfo runningTaskInfo = getRunningTaskInfo();
                if (!(runningTaskInfo == null || runningTaskInfo.topActivity == null || runningTaskInfo.configuration.windowConfiguration.getWindowingMode() != 1)) {
                    String currentPkg = runningTaskInfo.topActivity.getPackageName();
                    Context context = view.getContext();
                    Flog.bdReport(context, 10065, "{ pkg1:" + currentPkg + ",pkg2:" + vh.intent.getPackage() + "}");
                }
                opts.setLaunchWindowingMode(5);
                this.mContext.startActivityAsUser(vh.intent.addFlags(268435456), opts.toBundle(), new UserHandle(vh.userId));
            }
        }
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v3, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v2, resolved type: android.freeform.adapter.FloatItemAdapter$ViewHolder} */
    /* JADX WARNING: Multi-variable type inference failed */
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(this.mContext).inflate(34013300, null);
            viewHolder = new ViewHolder();
            ImageView iv = (ImageView) convertView.findViewById(34603220);
            ViewGroup.LayoutParams ivlp = iv.getLayoutParams();
            ivlp.width = (int) ((((float) ivlp.width) * this.mDeviceDefalutDensity) / getDisplayDensity());
            ivlp.height = (int) ((((float) ivlp.height) * this.mDeviceDefalutDensity) / getDisplayDensity());
            ImageView unused = viewHolder.image = iv;
            TextView tv = (TextView) convertView.findViewById(34603221);
            tv.setTextSize(0, (tv.getTextSize() * this.mDeviceDefalutDensity) / getDisplayDensity());
            TextView unused2 = viewHolder.title = tv;
            convertView.setTag(viewHolder);
        } else {
            viewHolder = convertView.getTag();
        }
        FloatItem item = this.mSource.get(position);
        viewHolder.title.setText(item.getLabel());
        viewHolder.image.setImageDrawable(item.getIcon());
        Intent unused3 = viewHolder.intent = item.getIntent();
        int unused4 = viewHolder.userId = item.getUserId();
        convertView.setOnClickListener(this);
        return convertView;
    }

    public int getCount() {
        if (this.mSource != null) {
            return this.mSource.size();
        }
        return 0;
    }

    public Object getItem(int position) {
        return this.mSource.get(position);
    }

    public long getItemId(int position) {
        return (long) position;
    }

    public float getDisplayDensity() {
        if (this.mDisplayDpi == 0.0f) {
            return this.mDeviceDefalutDensity;
        }
        return ((LCD_DENSITY * this.mDisplayDpi) / REAL_LCD_DENSITY) / 160.0f;
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
