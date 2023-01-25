package com.otpless.views;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.OnLifecycleEvent;

import com.otpless.R;
import com.otpless.dto.OtplessResponse;
import com.otpless.main.OtplessResultContract;
import com.otpless.network.ApiCallback;
import com.otpless.network.ApiManager;
import com.otpless.utils.Utility;

import org.json.JSONObject;

public class WhatsappLoginButton extends androidx.appcompat.widget.AppCompatButton implements View.OnClickListener, LifecycleObserver {

    private static final String WHATSAPP_PACKAGE = "com.whatsapp";
    private static final String WHATSAPP_BUSINESS_PACKAGE = "com.whatsapp.w4b";

    private Paint paint;
    private RectF rectF;
    private Drawable icon;
    private int backgroundColor;
    private String otplessLink = null;

    private OtplessUserDetailCallback mUserCallback;

    private ActivityResultLauncher<Uri> launcher;

    public WhatsappLoginButton(Context context) {
        super(context);
        init(null);
    }

    public WhatsappLoginButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public WhatsappLoginButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int desiredWidth = 100;
        int desiredHeight = 100;

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int width;
        int height;

        //Measure Width
        if (widthMode == MeasureSpec.EXACTLY) {
            //Must be this size
            width = widthSize;
        } else if (widthMode == MeasureSpec.AT_MOST) {
            //Can't be bigger than...
            width = (int) (((float) widthSize) * 0.8);
        } else {
            //Be whatever you want
            width = desiredWidth;
        }

        //Measure Height
        if (heightMode == MeasureSpec.EXACTLY) {
            //Must be this size
            height = heightSize;
        } else if (heightMode == MeasureSpec.AT_MOST) {
            //Can't be bigger than...
            height = (int) (0.18 * ((float) width));
        } else {
            //Be whatever you want
            height = desiredHeight;
        }

        //MUST CALL THIS
        setMeasuredDimension(width, height);
    }

    private void init(AttributeSet attrs) {
        // parsing otpless link attribute
        if (attrs != null) {
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.WhatsappLoginButton);
            this.otplessLink = a.getString(R.styleable.WhatsappLoginButton_otpless_link);
            // parsing host name
            try {
                final Uri uri = Uri.parse(this.otplessLink);
                // base url created
                ApiManager.getInstance().baseUrl = uri.getScheme() + "://" +uri.getHost() + "/";
            } catch (Exception ignore){}
            a.recycle();
        }

        // setting background and style
        backgroundColor = Color.rgb(35, 211, 102);
        paint = new Paint();
        paint.setColor(backgroundColor);
        paint.setAntiAlias(true);
        rectF = new RectF();
        icon = ContextCompat.getDrawable(getContext(), R.drawable.icons8_whatsapp_96); // The image you want to use
        final String text = "Continue with WhatsApp";
        setText(text);
        final int horPad = getResources().getDimensionPixelSize(R.dimen.button_padding_horizontal);
        final int verPad = getResources().getDimensionPixelSize(R.dimen.button_padding_vertical);
        setPadding(horPad, verPad, horPad, verPad);
        setTextColor(Color.WHITE);
        setTypeface(getTypeface(), Typeface.BOLD);
        setTextSize(20);
        this.setAllCaps(false);
        this.setOnClickListener(this);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float cornerRadius = 20.0f;
        rectF.set(0, 0, this.getWidth(), this.getHeight());
        canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, paint);
        int iconHeight = getHeight() / 2;
        int x = iconHeight / 3;
        int y = (this.getHeight() - iconHeight) / 2;
        icon.setBounds(x, y, x + iconHeight, y + iconHeight);
        icon.draw(canvas);
        super.onDraw(canvas);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        // check if whatsapp package are not installed then mark the view gone
        PackageManager pm = getContext().getPackageManager();
        if (!Utility.isAppInstalled(pm, WHATSAPP_PACKAGE) && !Utility.isAppInstalled(pm, WHATSAPP_BUSINESS_PACKAGE)) {
            this.setVisibility(View.GONE);
            return;
        }

        if (getContext() instanceof FragmentActivity) {
            FragmentActivity activity = (FragmentActivity) getContext();
            launcher = activity.registerForActivityResult(new OtplessResultContract(), this::onOtplessResult);
        }
        // if context is instance of lifecycle
        if (getContext() instanceof LifecycleOwner) {
            final LifecycleOwner owner = (LifecycleOwner) getContext();
            owner.getLifecycle().addObserver(this);
        }
        checkForWaid();
    }

    private void checkForWaid() {
        final String waid = getContext().getSharedPreferences("otpless_storage_manager", Context.MODE_PRIVATE).getString("otpless_waid", null);
        if (waid == null) {
            Toast.makeText(getContext(), "Button loaded without waID", Toast.LENGTH_SHORT).show();
            return;
        }
        ApiManager.getInstance().verifyWaId(
                waid, new ApiCallback<JSONObject>() {
                    @Override
                    public void onSuccess(JSONObject data) {
                        final String userNumber = Utility.parseUserNumber(data);
                        if (Utility.isNotEmpty(userNumber)) {
                            setText(userNumber);
                        }
                    }

                    @Override
                    public void onError(Exception exception) {
                        exception.printStackTrace();
                        Utility.deleteWaId(getContext());
                    }
                }
        );
    }

    private void onOtplessResult(@Nullable OtplessResponse userDetail) {
        if (this.mUserCallback != null) {
            this.mUserCallback.onOtplessUserDetail(userDetail);
        }
        // set userNumber on button
        if (userDetail != null && userDetail.getUserNumber() != null) {
            setText(userDetail.getUserNumber());
        }
    }

    @Override
    public void onClick(View v) {
        if (otplessLink != null && otplessLink.length() > 0) {
            try {
                final Uri uri = Uri.parse(otplessLink);
                launcher.launch(uri);
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
    }

    public final void setResultCallback(final OtplessUserDetailCallback callback) {
        this.mUserCallback = callback;
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    public void onDestroyed() {
    }
}
