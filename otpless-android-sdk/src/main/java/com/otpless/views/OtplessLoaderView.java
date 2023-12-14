package com.otpless.views;

import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.otpless.R;

import org.json.JSONException;
import org.json.JSONObject;


public class OtplessLoaderView extends FrameLayout {

    private OtplessLoaderCallback mOtplessLoaderCallback;
    private TextView mInfoTv, mCloseTv;
    private ProgressBar mOtplessProgress;
    private Button mRetryButton;
    private FrameLayout mContainerFl;

    @Nullable
    private JSONObject mColorConfig;

    public OtplessLoaderView(@NonNull Context context) {
        super(context);
        initView(context, null);
    }

    public OtplessLoaderView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context, attrs);
    }

    public OtplessLoaderView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context, attrs);
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public OtplessLoaderView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView(context, attrs);
    }

    private void initView(final Context context, @Nullable AttributeSet attrs) {
        final View otplessLoaderView = LayoutInflater.from(context).inflate(R.layout.otpless_loader_view, this, false);
        addView(otplessLoaderView);
        // finding and assigning the close button
        // close button will always be visible
        mCloseTv = otplessLoaderView.findViewById(R.id.otpless_close_tv);
        mCloseTv.setOnClickListener(v -> {
            if (mOtplessLoaderCallback != null) {
                mOtplessLoaderCallback.onOtplessLoaderEvent(OtplessLoaderEvent.CLOSE);
            }
        });
        // finding and assigning the retry button
        mRetryButton = otplessLoaderView.findViewById(R.id.otpless_retry_btn);
        mRetryButton.setOnClickListener(v -> {
            if (mOtplessLoaderCallback != null) {
                mOtplessLoaderCallback.onOtplessLoaderEvent(OtplessLoaderEvent.RETRY);
            }
        });
        // assigning the progress bar and error text view
        mOtplessProgress = otplessLoaderView.findViewById(R.id.otpless_progress_bar);
        mInfoTv = otplessLoaderView.findViewById(R.id.otpless_info_tv);
        mContainerFl = otplessLoaderView.findViewById(R.id.otpless_container_fl);
    }

    void setOtplessLoaderCallback(final OtplessLoaderCallback callback) {
        this.mOtplessLoaderCallback = callback;
    }

    void show() {
        this.mOtplessProgress.setVisibility(View.VISIBLE);
        this.mInfoTv.setVisibility(View.GONE);
        this.mRetryButton.setVisibility(View.GONE);
        this.mCloseTv.setVisibility(View.GONE);
        this.setVisibility(View.VISIBLE);
    }

    void showRetry(@NonNull final String errorText) {
        this.mInfoTv.setVisibility(View.VISIBLE);
        this.mInfoTv.setText(errorText);
        this.mRetryButton.setVisibility(View.VISIBLE);
        this.mOtplessProgress.setVisibility(View.GONE);
        this.mCloseTv.setVisibility(View.VISIBLE);
    }

    void hide() {
        this.setVisibility(View.GONE);
    }

    void setConfiguration(final JSONObject extras) {
        this.mColorConfig = extras;
        resetConfiguration();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        resetConfiguration();
    }

    private void resetConfiguration() {
        if (this.mColorConfig == null) return;
        // parse primary color and set to retry button
        ColorUtils.parseColor(this.mColorConfig.optString("primaryColor"), (primaryColor) -> {
            //region ==== creating color state list
            int[][] states = new int[][]{
                    new int[]{android.R.attr.state_enabled}, // enabled
                    new int[]{-android.R.attr.state_enabled}, // disabled
                    new int[]{android.R.attr.state_pressed}  // pressed
            };
            int[] colors = new int[]{
                    primaryColor,
                    primaryColor,
                    primaryColor
            };
            final ColorStateList colorStateList = new ColorStateList(states, colors);
            //endregion
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                this.mRetryButton.setBackgroundTintList(colorStateList);
            } else {
                this.mRetryButton.setBackgroundColor(primaryColor);
            }
        });
        // parse close button text color
        ColorUtils.parseColor(this.mColorConfig.optString("closeButtonColor"), (closeButtonColor) -> {
            this.mCloseTv.setTextColor(closeButtonColor);
        });
        // parse loader color and set to progress bar
        ColorUtils.parseColor(this.mColorConfig.optString("loaderColor"), (loaderColor) -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mOtplessProgress.setIndeterminateTintList(ColorStateList.valueOf(loaderColor));
            }
        });
        // parse text color and set it info text and retry button text
        ColorUtils.parseColor(this.mColorConfig.optString("textColor"), (textColor) -> {
            this.mInfoTv.setTextColor(textColor);
            this.mRetryButton.setTextColor(textColor);
        });
        // checking parsing for alpha for loader background
        final String alphaString = this.mColorConfig.optString("loaderAlpha");
        if (!alphaString.isEmpty()) {
            try {
                final float alpha = Float.parseFloat(alphaString);
                mContainerFl.setAlpha(alpha);
            } catch (Exception ignore){}
        }
    }
}
