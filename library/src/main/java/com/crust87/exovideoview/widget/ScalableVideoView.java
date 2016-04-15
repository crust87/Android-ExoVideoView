package com.crust87.exovideoview.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Matrix;
import android.util.AttributeSet;

import com.crust87.exovideoview.R;
import com.crust87.exovideoview.player.ExoMediaPlayer;

/**
 * Created by mabi on 2016. 3. 2..
 */
public class ScalableVideoView extends ExoVideoPlayerView {

    // Attributes
    private ScaleType mScaleType;
    private int mVideoWidth;
    private int mVideoHeight;

    private static final ScaleType[] sScaleTypeArray = {
            ScaleType.MATRIX,
            ScaleType.FIT_XY,
            ScaleType.FIT_START,
            ScaleType.FIT_CENTER,
            ScaleType.FIT_END,
            ScaleType.CENTER,
            ScaleType.CENTER_CROP,
            ScaleType.CENTER_INSIDE
    };

    public enum ScaleType {
        MATRIX(0),
        FIT_XY(1),
        FIT_START(2),
        FIT_CENTER(3),
        FIT_END(4),
        CENTER(5),
        CENTER_CROP(6),
        CENTER_INSIDE(7);

        ScaleType(int ni) {
            nativeInt = ni;
        }

        final int nativeInt;
    }

    public ScalableVideoView(Context context) {
        super(context);

        mVideoHeight = 0;
        mVideoWidth = 0;
        addListener(mListener);

        setScaleType(ScaleType.FIT_CENTER);
    }

    public ScalableVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mVideoHeight = 0;
        mVideoWidth = 0;
        addListener(mListener);

        initAttributes(context, attrs, 0);
    }

    public ScalableVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mVideoHeight = 0;
        mVideoWidth = 0;
        addListener(mListener);

        initAttributes(context, attrs, defStyleAttr);
    }

    private void initAttributes(Context context, AttributeSet attrs, int defStyleAttr) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ExoVideoPlayerView, defStyleAttr, 0);

        final int index = typedArray.getInt(R.styleable.ExoVideoPlayerView_scaleType, -1);
        if (index >= 0) {
            setScaleType(sScaleTypeArray[index]);
        }
    }

    public void setScaleType(ScaleType scaleType) {
        if (scaleType == null) {
            throw new NullPointerException();
        }

        if (mScaleType != scaleType) {
            mScaleType = scaleType;

            setWillNotCacheDrawing(mScaleType == ScaleType.CENTER);

            requestLayout();
            invalidate();
            initVideo();
        }
    }

    private void initVideo() {
        // FIXME dirty code
        switch(mScaleType) {
            case MATRIX:
                matrix();
                break;
            case FIT_XY:
                fitXY();
                break;
            case FIT_START:
                fitStart();
                break;
            case FIT_CENTER:
                fitCenter();
                break;
            case FIT_END:
                fitEnd();
                break;
            case CENTER:
                center();
                break;
            case CENTER_CROP:
                centerCrop();
                break;
            case CENTER_INSIDE:
                centerInside();
                break;
        }
    }

    // FIXME dirty code
    private void matrix() {
        try {
            Matrix mMatrix = new Matrix();

            int viewWidth = getWidth();
            int viewHeight = getHeight();

            float mScaleX = (float) mVideoWidth / viewWidth;
            float mScaleY = (float) mVideoHeight / viewHeight;

            mMatrix.setScale(mScaleX, mScaleY);

            setTransform(mMatrix);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
    }

    // FIXME dirty code
    private void fitXY() {
        try {
            Matrix mMatrix = new Matrix();

            mMatrix.setScale(1f, 1f);

            setTransform(mMatrix);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
    }

    // FIXME dirty code
    private void center() {
        try {
            Matrix mMatrix = new Matrix();

            int viewWidth = getWidth();
            int viewHeight = getHeight();

            float mScaleX = (float) mVideoWidth / viewWidth;
            float mScaleY = (float) mVideoHeight / viewHeight;

            float mBoundX = viewWidth - mVideoWidth;
            float mBoundY = viewHeight - mVideoHeight;

            mMatrix.setScale(mScaleX, mScaleY);
            mMatrix.postTranslate(mBoundX / 2, mBoundY / 2);

            setTransform(mMatrix);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
    }

    // FIXME dirty code
    private void centerCrop() {
        try {
            Matrix mMatrix = new Matrix();

            int viewWidth = getWidth();
            int viewHeight = getHeight();

            float mScaleX = (float) mVideoWidth / viewWidth;
            float mScaleY = (float) mVideoHeight / viewHeight;

            float mBoundX = viewWidth - mVideoWidth / mScaleY;
            float mBoundY = viewHeight - mVideoHeight / mScaleX;

            if (mScaleX < mScaleY) {
                mScaleY = mScaleY * (1.0f / mScaleX);
                mScaleX = 1.0f;
                mBoundX = 0;
            } else {
                mScaleX = mScaleX * (1.0f / mScaleY);
                mScaleY = 1.0f;
                mBoundY = 0;
            }

            mMatrix.setScale(mScaleX, mScaleY);
            mMatrix.postTranslate(mBoundX / 2, mBoundY / 2);

            setTransform(mMatrix);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
    }

    // FIXME dirty code
    private void centerInside() {
        try {
            Matrix mMatrix = new Matrix();

            int viewWidth = getWidth();
            int viewHeight = getHeight();

            float mScaleX = (float) mVideoWidth / viewWidth;
            float mScaleY = (float) mVideoHeight / viewHeight;

            if(mScaleX > 1 || mScaleY > 1) {
                fitCenter();
            } else {
                float mBoundX = viewWidth - mVideoWidth;
                float mBoundY = viewHeight - mVideoHeight;

                mMatrix.setScale(mScaleX, mScaleY);
                mMatrix.postTranslate(mBoundX / 2, mBoundY / 2);

                setTransform(mMatrix);
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
    }

    // FIXME dirty code
    private void fitStart() {
        try {
            Matrix mMatrix = new Matrix();

            int viewWidth = getWidth();
            int viewHeight = getHeight();

            float mScaleX = (float) mVideoWidth / viewWidth;
            float mScaleY = (float) mVideoHeight / viewHeight;

            float mBoundX = 0;
            float mBoundY = 0;

            if (mScaleX > mScaleY) {
                mScaleY = mScaleY * (1.0f / mScaleX);
                mScaleX = 1.0f;
            } else {
                mScaleX = mScaleX * (1.0f / mScaleY);
                mScaleY = 1.0f;
            }

            mMatrix.setScale(mScaleX, mScaleY);
            mMatrix.postTranslate(mBoundX / 2, mBoundY / 2);

            setTransform(mMatrix);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
    }

    // FIXME dirty code
    private void fitCenter() {
        try {
            Matrix mMatrix = new Matrix();

            int viewWidth = getWidth();
            int viewHeight = getHeight();

            float mScaleX = (float) mVideoWidth / viewWidth;
            float mScaleY = (float) mVideoHeight / viewHeight;

            float mBoundX = viewWidth - mVideoWidth / mScaleY;
            float mBoundY = viewHeight - mVideoHeight / mScaleX;

            if (mScaleX > mScaleY) {
                mScaleY = mScaleY * (1.0f / mScaleX);
                mScaleX = 1.0f;
                mBoundX = 0;
            } else {
                mScaleX = mScaleX * (1.0f / mScaleY);
                mScaleY = 1.0f;
                mBoundY = 0;
            }

            mMatrix.setScale(mScaleX, mScaleY);
            mMatrix.postTranslate(mBoundX / 2, mBoundY / 2);

            setTransform(mMatrix);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
    }

    // FIXME dirty code
    private void fitEnd() {
        try {
            Matrix mMatrix = new Matrix();

            int viewWidth = getWidth();
            int viewHeight = getHeight();

            float mScaleX = (float) mVideoWidth / viewWidth;
            float mScaleY = (float) mVideoHeight / viewHeight;

            float mBoundX = viewWidth - mVideoWidth / mScaleY;
            float mBoundY = viewHeight - mVideoHeight / mScaleX;

            if (mScaleX > mScaleY) {
                mScaleY = mScaleY * (1.0f / mScaleX);
                mScaleX = 1.0f;
                mBoundX = 0;
            } else {
                mScaleX = mScaleX * (1.0f / mScaleY);
                mScaleY = 1.0f;
                mBoundY = 0;
            }

            mMatrix.setScale(mScaleX, mScaleY);
            mMatrix.postTranslate(mBoundX, mBoundY);

            setTransform(mMatrix);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
    }

    private ExoMediaPlayer.Listener mListener = new ExoMediaPlayer.Listener() {
        @Override
        public void onStateChanged(boolean playWhenReady, int playbackState) {
            if(playbackState == ExoMediaPlayer.STATE_READY) {
                initVideo();
            }
        }

        @Override
        public void onError(Exception e) {

        }

        @Override
        public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees, float pixelWidthHeightRatio) {
            mVideoWidth = width;
            mVideoHeight = height;
        }
    };
}
