package com.android.internal.widget;

import android.app.Notification;
import android.app.Person;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Icon;
import android.net.wifi.WifiEnterpriseConfig;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.RemotableViewMethod;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.Interpolator;
import android.view.animation.PathInterpolator;
import android.widget.FrameLayout;
import android.widget.RemoteViews;
import android.widget.TextView;
import com.android.internal.R;
import com.android.internal.graphics.ColorUtils;
import com.android.internal.util.ContrastColorUtil;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Pattern;

@RemoteViews.RemoteView
public class MessagingLayout extends FrameLayout implements ImageMessageConsumer {
    private static final float COLOR_SHIFT_AMOUNT = 60.0f;
    public static final Interpolator FAST_OUT_LINEAR_IN = new PathInterpolator(0.4f, 0.0f, 1.0f, 1.0f);
    public static final Interpolator FAST_OUT_SLOW_IN = new PathInterpolator(0.4f, 0.0f, 0.2f, 1.0f);
    private static final Pattern IGNORABLE_CHAR_PATTERN = Pattern.compile("[\\p{C}\\p{Z}]");
    public static final Interpolator LINEAR_OUT_SLOW_IN = new PathInterpolator(0.0f, 0.0f, 0.2f, 1.0f);
    public static final View.OnLayoutChangeListener MESSAGING_PROPERTY_ANIMATOR = new MessagingPropertyAnimator();
    private static final Consumer<MessagingMessage> REMOVE_MESSAGE = $$Lambda$DKD2sNhLnyRFoBkFvfwKyxoEx10.INSTANCE;
    private static final Pattern SPECIAL_CHAR_PATTERN = Pattern.compile("[!@#$%&*()_+=|<>?{}\\[\\]~-]");
    private ArrayList<MessagingGroup> mAddedGroups = new ArrayList<>();
    private Icon mAvatarReplacement;
    private int mAvatarSize;
    private CharSequence mConversationTitle;
    private boolean mDisplayImagesAtEnd;
    private ArrayList<MessagingGroup> mGroups = new ArrayList<>();
    private List<MessagingMessage> mHistoricMessages = new ArrayList();
    private ImageResolver mImageResolver;
    private boolean mIsOneToOne;
    private int mLayoutColor;
    private int mMessageTextColor;
    private List<MessagingMessage> mMessages = new ArrayList();
    private MessagingLinearLayout mMessagingLinearLayout;
    private CharSequence mNameReplacement;
    private Paint mPaint = new Paint(1);
    private int mSenderTextColor;
    private boolean mShowHistoricMessages;
    private Paint mTextPaint = new Paint();
    private TextView mTitleView;
    private Person mUser;

    public MessagingLayout(Context context) {
        super(context);
    }

    public MessagingLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MessagingLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public MessagingLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onFinishInflate() {
        super.onFinishInflate();
        this.mMessagingLinearLayout = (MessagingLinearLayout) findViewById(R.id.notification_messaging);
        this.mMessagingLinearLayout.setMessagingLayout(this);
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        int size = Math.max(displayMetrics.widthPixels, displayMetrics.heightPixels);
        this.mMessagingLinearLayout.setClipBounds(new Rect(0, 0, size, size));
        this.mTitleView = (TextView) findViewById(16908310);
        this.mAvatarSize = getResources().getDimensionPixelSize(R.dimen.messaging_avatar_size);
        this.mTextPaint.setTextAlign(Paint.Align.CENTER);
        this.mTextPaint.setAntiAlias(true);
    }

    @RemotableViewMethod
    public void setAvatarReplacement(Icon icon) {
        this.mAvatarReplacement = icon;
    }

    @RemotableViewMethod
    public void setNameReplacement(CharSequence nameReplacement) {
        this.mNameReplacement = nameReplacement;
    }

    @RemotableViewMethod
    public void setDisplayImagesAtEnd(boolean atEnd) {
        this.mDisplayImagesAtEnd = atEnd;
    }

    @RemotableViewMethod
    public void setData(Bundle extras) {
        List<Notification.MessagingStyle.Message> newMessages = Notification.MessagingStyle.Message.getMessagesFromBundleArray(extras.getParcelableArray(Notification.EXTRA_MESSAGES));
        List<Notification.MessagingStyle.Message> newHistoricMessages = Notification.MessagingStyle.Message.getMessagesFromBundleArray(extras.getParcelableArray(Notification.EXTRA_HISTORIC_MESSAGES));
        setUser((Person) extras.getParcelable(Notification.EXTRA_MESSAGING_PERSON));
        this.mConversationTitle = null;
        TextView headerText = (TextView) findViewById(R.id.header_text);
        if (headerText != null) {
            this.mConversationTitle = headerText.getText();
        }
        addRemoteInputHistoryToMessages(newMessages, extras.getCharSequenceArray(Notification.EXTRA_REMOTE_INPUT_HISTORY));
        bind(newMessages, newHistoricMessages, extras.getBoolean(Notification.EXTRA_SHOW_REMOTE_INPUT_SPINNER, false));
    }

    @Override // com.android.internal.widget.ImageMessageConsumer
    public void setImageResolver(ImageResolver resolver) {
        this.mImageResolver = resolver;
    }

    private void addRemoteInputHistoryToMessages(List<Notification.MessagingStyle.Message> newMessages, CharSequence[] remoteInputHistory) {
        if (!(remoteInputHistory == null || remoteInputHistory.length == 0)) {
            for (int i = remoteInputHistory.length - 1; i >= 0; i--) {
                newMessages.add(new Notification.MessagingStyle.Message(remoteInputHistory[i], 0, null, true));
            }
        }
    }

    private void bind(List<Notification.MessagingStyle.Message> newMessages, List<Notification.MessagingStyle.Message> newHistoricMessages, boolean showSpinner) {
        List<MessagingMessage> historicMessages = createMessages(newHistoricMessages, true);
        List<MessagingMessage> messages = createMessages(newMessages, false);
        ArrayList<MessagingGroup> oldGroups = new ArrayList<>(this.mGroups);
        addMessagesToGroups(historicMessages, messages, showSpinner);
        removeGroups(oldGroups);
        this.mMessages.forEach(REMOVE_MESSAGE);
        this.mHistoricMessages.forEach(REMOVE_MESSAGE);
        this.mMessages = messages;
        this.mHistoricMessages = historicMessages;
        updateHistoricMessageVisibility();
        updateTitleAndNamesDisplay();
    }

    private void removeGroups(ArrayList<MessagingGroup> oldGroups) {
        int size = oldGroups.size();
        for (int i = 0; i < size; i++) {
            MessagingGroup group = oldGroups.get(i);
            if (!this.mGroups.contains(group)) {
                List<MessagingMessage> messages = group.getMessages();
                Runnable endRunnable = new Runnable(group) {
                    /* class com.android.internal.widget.$$Lambda$MessagingLayout$AR_BLYGwVbm8HbmaOhECHwnOBBg */
                    private final /* synthetic */ MessagingGroup f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final void run() {
                        MessagingLayout.this.lambda$removeGroups$0$MessagingLayout(this.f$1);
                    }
                };
                boolean wasShown = group.isShown();
                this.mMessagingLinearLayout.removeView(group);
                if (!wasShown || MessagingLinearLayout.isGone(group)) {
                    endRunnable.run();
                } else {
                    this.mMessagingLinearLayout.addTransientView(group, 0);
                    group.removeGroupAnimated(endRunnable);
                }
                this.mMessages.removeAll(messages);
                this.mHistoricMessages.removeAll(messages);
            }
        }
    }

    public /* synthetic */ void lambda$removeGroups$0$MessagingLayout(MessagingGroup group) {
        this.mMessagingLinearLayout.removeTransientView(group);
        group.recycle();
    }

    private void updateTitleAndNamesDisplay() {
        Icon cachedIcon;
        ArrayMap<CharSequence, String> uniqueNames = new ArrayMap<>();
        ArrayMap<Character, CharSequence> uniqueCharacters = new ArrayMap<>();
        for (int i = 0; i < this.mGroups.size(); i++) {
            MessagingGroup group = this.mGroups.get(i);
            CharSequence senderName = group.getSenderName();
            if (group.needsGeneratedAvatar() && !TextUtils.isEmpty(senderName) && !uniqueNames.containsKey(senderName)) {
                String pureSenderName = IGNORABLE_CHAR_PATTERN.matcher(senderName).replaceAll("");
                char c = pureSenderName.charAt(0);
                if (uniqueCharacters.containsKey(Character.valueOf(c))) {
                    CharSequence existingName = uniqueCharacters.get(Character.valueOf(c));
                    if (existingName != null) {
                        uniqueNames.put(existingName, findNameSplit((String) existingName));
                        uniqueCharacters.put(Character.valueOf(c), null);
                    }
                    uniqueNames.put(senderName, findNameSplit((String) senderName));
                } else {
                    uniqueNames.put(senderName, Character.toString(c));
                    uniqueCharacters.put(Character.valueOf(c), pureSenderName);
                }
            }
        }
        ArrayMap<CharSequence, Icon> cachedAvatars = new ArrayMap<>();
        for (int i2 = 0; i2 < this.mGroups.size(); i2++) {
            MessagingGroup group2 = this.mGroups.get(i2);
            boolean isOwnMessage = group2.getSender() == this.mUser;
            CharSequence senderName2 = group2.getSenderName();
            if (group2.needsGeneratedAvatar() && !TextUtils.isEmpty(senderName2) && ((!this.mIsOneToOne || this.mAvatarReplacement == null || isOwnMessage) && (cachedIcon = group2.getAvatarSymbolIfMatching(senderName2, uniqueNames.get(senderName2), this.mLayoutColor)) != null)) {
                cachedAvatars.put(senderName2, cachedIcon);
            }
        }
        for (int i3 = 0; i3 < this.mGroups.size(); i3++) {
            MessagingGroup group3 = this.mGroups.get(i3);
            CharSequence senderName3 = group3.getSenderName();
            if (group3.needsGeneratedAvatar() && !TextUtils.isEmpty(senderName3)) {
                if (!this.mIsOneToOne || this.mAvatarReplacement == null || group3.getSender() == this.mUser) {
                    Icon cachedIcon2 = cachedAvatars.get(senderName3);
                    if (cachedIcon2 == null) {
                        cachedIcon2 = createAvatarSymbol(senderName3, uniqueNames.get(senderName3), this.mLayoutColor);
                        cachedAvatars.put(senderName3, cachedIcon2);
                    }
                    group3.setCreatedAvatar(cachedIcon2, senderName3, uniqueNames.get(senderName3), this.mLayoutColor);
                } else {
                    group3.setAvatar(this.mAvatarReplacement);
                }
            }
        }
    }

    public Icon createAvatarSymbol(CharSequence senderName, String symbol, int layoutColor) {
        float f;
        float f2;
        if (symbol.isEmpty() || TextUtils.isDigitsOnly(symbol) || SPECIAL_CHAR_PATTERN.matcher(symbol).find()) {
            Icon avatarIcon = Icon.createWithResource(getContext(), (int) R.drawable.messaging_user);
            avatarIcon.setTint(findColor(senderName, layoutColor));
            return avatarIcon;
        }
        int i = this.mAvatarSize;
        Bitmap bitmap = Bitmap.createBitmap(i, i, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        float radius = ((float) this.mAvatarSize) / 2.0f;
        int color = findColor(senderName, layoutColor);
        this.mPaint.setColor(color);
        canvas.drawCircle(radius, radius, radius, this.mPaint);
        this.mTextPaint.setColor((ColorUtils.calculateLuminance(color) > 0.5d ? 1 : (ColorUtils.calculateLuminance(color) == 0.5d ? 0 : -1)) > 0 ? -16777216 : -1);
        Paint paint = this.mTextPaint;
        if (symbol.length() == 1) {
            f2 = (float) this.mAvatarSize;
            f = 0.5f;
        } else {
            f2 = (float) this.mAvatarSize;
            f = 0.3f;
        }
        paint.setTextSize(f2 * f);
        canvas.drawText(symbol, radius, (float) ((int) (radius - ((this.mTextPaint.descent() + this.mTextPaint.ascent()) / 2.0f))), this.mTextPaint);
        return Icon.createWithBitmap(bitmap);
    }

    private int findColor(CharSequence senderName, int layoutColor) {
        double luminance = ContrastColorUtil.calculateLuminance(layoutColor);
        return ContrastColorUtil.getShiftedColor(layoutColor, (int) (60.0f * ((float) (((double) ((float) (((double) ((((float) (Math.abs(senderName.hashCode()) % 5)) / 4.0f) - 0.5f)) + Math.max(0.30000001192092896d - luminance, 0.0d)))) - Math.max(0.30000001192092896d - (1.0d - luminance), 0.0d)))));
    }

    private String findNameSplit(String existingName) {
        String[] split = existingName.split(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
        if (split.length <= 1) {
            return existingName.substring(0, 1);
        }
        return Character.toString(split[0].charAt(0)) + Character.toString(split[1].charAt(0));
    }

    @RemotableViewMethod
    public void setLayoutColor(int color) {
        this.mLayoutColor = color;
    }

    @RemotableViewMethod
    public void setIsOneToOne(boolean oneToOne) {
        this.mIsOneToOne = oneToOne;
    }

    @RemotableViewMethod
    public void setSenderTextColor(int color) {
        this.mSenderTextColor = color;
    }

    @RemotableViewMethod
    public void setMessageTextColor(int color) {
        this.mMessageTextColor = color;
    }

    public void setUser(Person user) {
        this.mUser = user;
        if (this.mUser.getIcon() == null) {
            Icon userIcon = Icon.createWithResource(getContext(), (int) R.drawable.messaging_user);
            userIcon.setTint(this.mLayoutColor);
            this.mUser = this.mUser.toBuilder().setIcon(userIcon).build();
        }
    }

    private void addMessagesToGroups(List<MessagingMessage> historicMessages, List<MessagingMessage> messages, boolean showSpinner) {
        List<List<MessagingMessage>> groups = new ArrayList<>();
        List<Person> senders = new ArrayList<>();
        findGroups(historicMessages, messages, groups, senders);
        createGroupViews(groups, senders, showSpinner);
    }

    private void createGroupViews(List<List<MessagingMessage>> groups, List<Person> senders, boolean showSpinner) {
        this.mGroups.clear();
        for (int groupIndex = 0; groupIndex < groups.size(); groupIndex++) {
            List<MessagingMessage> group = groups.get(groupIndex);
            MessagingGroup newGroup = null;
            boolean z = true;
            for (int messageIndex = group.size() - 1; messageIndex >= 0; messageIndex--) {
                newGroup = group.get(messageIndex).getGroup();
                if (newGroup != null) {
                    break;
                }
            }
            if (newGroup == null) {
                newGroup = MessagingGroup.createGroup(this.mMessagingLinearLayout);
                this.mAddedGroups.add(newGroup);
            }
            newGroup.setDisplayImagesAtEnd(this.mDisplayImagesAtEnd);
            newGroup.setLayoutColor(this.mLayoutColor);
            newGroup.setTextColors(this.mSenderTextColor, this.mMessageTextColor);
            Person sender = senders.get(groupIndex);
            CharSequence nameOverride = null;
            if (!(sender == this.mUser || this.mNameReplacement == null)) {
                nameOverride = this.mNameReplacement;
            }
            newGroup.setSender(sender, nameOverride);
            if (groupIndex != groups.size() - 1 || !showSpinner) {
                z = false;
            }
            newGroup.setSending(z);
            this.mGroups.add(newGroup);
            if (this.mMessagingLinearLayout.indexOfChild(newGroup) != groupIndex) {
                this.mMessagingLinearLayout.removeView(newGroup);
                this.mMessagingLinearLayout.addView(newGroup, groupIndex);
            }
            newGroup.setMessages(group);
        }
    }

    private void findGroups(List<MessagingMessage> historicMessages, List<MessagingMessage> messages, List<List<MessagingMessage>> groups, List<Person> senders) {
        MessagingMessage message;
        CharSequence key;
        CharSequence currentSenderKey = null;
        List<MessagingMessage> currentGroup = null;
        int histSize = historicMessages.size();
        for (int i = 0; i < messages.size() + histSize; i++) {
            if (i < histSize) {
                message = historicMessages.get(i);
            } else {
                message = messages.get(i - histSize);
            }
            boolean isNewGroup = currentGroup == null;
            Person sender = message.getMessage().getSenderPerson();
            if (sender == null) {
                key = null;
            } else {
                key = sender.getKey() == null ? sender.getName() : sender.getKey();
            }
            if ((true ^ TextUtils.equals(key, currentSenderKey)) || isNewGroup) {
                currentGroup = new ArrayList<>();
                groups.add(currentGroup);
                if (sender == null) {
                    sender = this.mUser;
                }
                senders.add(sender);
                currentSenderKey = key;
            }
            currentGroup.add(message);
        }
    }

    private List<MessagingMessage> createMessages(List<Notification.MessagingStyle.Message> newMessages, boolean historic) {
        List<MessagingMessage> result = new ArrayList<>();
        for (int i = 0; i < newMessages.size(); i++) {
            Notification.MessagingStyle.Message m = newMessages.get(i);
            MessagingMessage message = findAndRemoveMatchingMessage(m);
            if (message == null) {
                message = MessagingMessage.createMessage(this, m, this.mImageResolver);
            }
            message.setIsHistoric(historic);
            result.add(message);
        }
        return result;
    }

    private MessagingMessage findAndRemoveMatchingMessage(Notification.MessagingStyle.Message m) {
        for (int i = 0; i < this.mMessages.size(); i++) {
            MessagingMessage existing = this.mMessages.get(i);
            if (existing.sameAs(m)) {
                this.mMessages.remove(i);
                return existing;
            }
        }
        for (int i2 = 0; i2 < this.mHistoricMessages.size(); i2++) {
            MessagingMessage existing2 = this.mHistoricMessages.get(i2);
            if (existing2.sameAs(m)) {
                this.mHistoricMessages.remove(i2);
                return existing2;
            }
        }
        return null;
    }

    public void showHistoricMessages(boolean show) {
        this.mShowHistoricMessages = show;
        updateHistoricMessageVisibility();
    }

    private void updateHistoricMessageVisibility() {
        int numHistoric = this.mHistoricMessages.size();
        int i = 0;
        while (true) {
            int i2 = 0;
            if (i >= numHistoric) {
                break;
            }
            MessagingMessage existing = this.mHistoricMessages.get(i);
            if (!this.mShowHistoricMessages) {
                i2 = 8;
            }
            existing.setVisibility(i2);
            i++;
        }
        int numGroups = this.mGroups.size();
        for (int i3 = 0; i3 < numGroups; i3++) {
            MessagingGroup group = this.mGroups.get(i3);
            int visibleChildren = 0;
            List<MessagingMessage> messages = group.getMessages();
            int numGroupMessages = messages.size();
            for (int j = 0; j < numGroupMessages; j++) {
                if (messages.get(j).getVisibility() != 8) {
                    visibleChildren++;
                }
            }
            if (visibleChildren > 0 && group.getVisibility() == 8) {
                group.setVisibility(0);
            } else if (visibleChildren == 0 && group.getVisibility() != 8) {
                group.setVisibility(8);
            }
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.FrameLayout, android.view.View, android.view.ViewGroup
    public void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (!this.mAddedGroups.isEmpty()) {
            getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                /* class com.android.internal.widget.MessagingLayout.AnonymousClass1 */

                @Override // android.view.ViewTreeObserver.OnPreDrawListener
                public boolean onPreDraw() {
                    Iterator it = MessagingLayout.this.mAddedGroups.iterator();
                    while (it.hasNext()) {
                        MessagingGroup group = (MessagingGroup) it.next();
                        if (group.isShown()) {
                            MessagingPropertyAnimator.fadeIn(group.getAvatar());
                            MessagingPropertyAnimator.fadeIn(group.getSenderView());
                            MessagingPropertyAnimator.startLocalTranslationFrom(group, group.getHeight(), MessagingLayout.LINEAR_OUT_SLOW_IN);
                        }
                    }
                    MessagingLayout.this.mAddedGroups.clear();
                    MessagingLayout.this.getViewTreeObserver().removeOnPreDrawListener(this);
                    return true;
                }
            });
        }
    }

    public MessagingLinearLayout getMessagingLinearLayout() {
        return this.mMessagingLinearLayout;
    }

    public ArrayList<MessagingGroup> getMessagingGroups() {
        return this.mGroups;
    }
}
