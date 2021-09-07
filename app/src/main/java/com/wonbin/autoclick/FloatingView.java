package com.wonbin.autoclick;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by wilburnLee on 2019/4/22.
 */
public class FloatingView extends FrameLayout implements View.OnClickListener {
    private final Context mContext;
    private View mView;
    private FloatingManager mWindowManager;
    private List<View> anchorListView = new ArrayList<>();
    private String mCurState;

    public FloatingView(Context context) {
        super(context);
        mContext = context.getApplicationContext();
        LayoutInflater mLayoutInflater = LayoutInflater.from(context);
        mView = mLayoutInflater.inflate(R.layout.floating_view, null);

        ImageView mPlayView = (ImageView)mView.findViewById(R.id.play);
        ImageView mStopView = (ImageView)mView.findViewById(R.id.stop);
        ImageView mCloseView = (ImageView)mView.findViewById(R.id.close);
        ImageView mAddAnchorView = (ImageView)mView.findViewById(R.id.ivAddAnchor);
        mPlayView.setOnClickListener(this);
        mStopView.setOnClickListener(this);
        mCloseView.setOnClickListener(this);
        mAddAnchorView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                addAnchor();
            }
        });

        mView.setOnTouchListener(initTouchListener());
        mWindowManager = FloatingManager.getInstance(mContext);
    }

    public void show() {
        WindowManager.LayoutParams mParams = new WindowManager.LayoutParams();
        mParams.gravity = Gravity.CENTER;
        //        mParams.x = 0;
        //        mParams.y = 300;
        //总是出现在应用程序窗口之上
        if (Build.VERSION.SDK_INT >= 26) {
            mParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            mParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        }
        //设置图片格式，效果为背景透明
        mParams.format = PixelFormat.RGBA_8888;
        mParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
            | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
            | WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR |
            WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH;
        mParams.width = LayoutParams.WRAP_CONTENT;
        mParams.height = LayoutParams.WRAP_CONTENT;
        boolean result = mWindowManager.addView(mView, mParams);
        Toast.makeText(getContext(), "浮窗显示:" + result, Toast.LENGTH_LONG).show();
        //逐帧动画
        //        AnimationDrawable animationDrawable=(AnimationDrawable)mImageView.getDrawable();
        //        animationDrawable.start();
    }

    public void hide() {
        mWindowManager.removeView(mView);
    }

    private OnTouchListener initTouchListener() {
        return new OnTouchListener() {
            private int mTouchStartX, mTouchStartY;//手指按下时坐标
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mTouchStartX = (int)event.getRawX();
                        mTouchStartY = (int)event.getRawY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (!AutoService.PLAY.equals(mCurState)) {
                            WindowManager.LayoutParams param = (WindowManager.LayoutParams)view
                                .getLayoutParams();
                            param.x += (int)event.getRawX() - mTouchStartX;
                            param.y += (int)event.getRawY() - mTouchStartY;//相对于屏幕左上角的位置
                            mWindowManager.updateView(view, param);
                            mTouchStartX = (int)event.getRawX();
                            mTouchStartY = (int)event.getRawY();
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        break;
                }
                return true;
            }
        };
    }

    @Override
    public void onClick(View view) {
        Intent intent = new Intent(getContext(), AutoService.class);
        switch (view.getId()) {
            case R.id.play:
                mCurState = AutoService.PLAY;
                int[] locationX = new int[anchorListView.size()];
                int[] locationY = new int[anchorListView.size()];
                for (int i = 0 ; i < anchorListView.size(); i++) {
                    View currView = anchorListView.get(i);
                    int[] location = new int[2];
                    currView.getLocationOnScreen(location);
                    locationX[i]= location[0] - 1;
                    locationY[i]= location[1] - 1;
                }
                intent.putExtra(AutoService.ACTION, AutoService.PLAY);
                intent.putExtra("x", locationX);
                intent.putExtra("y", locationY);
                break;
            case R.id.stop:
                mCurState = AutoService.STOP;
                intent.putExtra(AutoService.ACTION, AutoService.STOP);
                break;
            case R.id.close:
                intent.putExtra(AutoService.ACTION, AutoService.HIDE);
                Intent appMain = new Intent(getContext(), MainActivity.class);
                getContext().startActivity(appMain);
                break;
        }
        getContext().startService(intent);
    }

    private void addAnchor() {
        LayoutInflater mLayoutInflater = LayoutInflater.from(mContext);
        View anchorView = mLayoutInflater.inflate(R.layout.floating_anchor, null);
        anchorView.setOnTouchListener(initTouchListener());
        TextView tvAnchor = anchorView.findViewById(R.id.tv_anchor);
        tvAnchor.setText(""+anchorListView.size());


        WindowManager.LayoutParams anchorParam = new WindowManager.LayoutParams();
        anchorParam.gravity = Gravity.CENTER;
        //总是出现在应用程序窗口之上
        if (Build.VERSION.SDK_INT >= 26) {
            anchorParam.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            anchorParam.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        }
        //设置图片格式，效果为背景透明
        anchorParam.format = PixelFormat.RGBA_8888;
        anchorParam.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
            | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
            | WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR |
            WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH;
        anchorParam.width = LayoutParams.WRAP_CONTENT;
        anchorParam.height = LayoutParams.WRAP_CONTENT;

        boolean result = mWindowManager.addView(anchorView, anchorParam);
        anchorListView.add(anchorView);

        Toast.makeText(getContext(), "Anchor Added:" + result, Toast.LENGTH_LONG).show();
    }
}
