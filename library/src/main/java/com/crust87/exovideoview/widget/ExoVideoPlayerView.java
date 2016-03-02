package com.crust87.exovideoview.widget;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.widget.Toast;

import com.crust87.exovideoview.R;
import com.crust87.exovideoview.player.DashRendererBuilder;
import com.crust87.exovideoview.player.ExoMediaPlayer;
import com.crust87.exovideoview.player.ExtractorRendererBuilder;
import com.crust87.exovideoview.player.HlsRendererBuilder;
import com.crust87.exovideoview.player.SmoothStreamingRendererBuilder;
import com.crust87.exovideoview.uril.EventLogger;
import com.crust87.exovideoview.widget.callback.SmoothStreamingTestMediaDrmCallback;
import com.crust87.exovideoview.widget.callback.WidevineTestMediaDrmCallback;
import com.google.android.exoplayer.ExoPlaybackException;
import com.google.android.exoplayer.ExoPlayer;
import com.google.android.exoplayer.MediaCodecTrackRenderer.DecoderInitializationException;
import com.google.android.exoplayer.MediaCodecUtil;
import com.google.android.exoplayer.audio.AudioCapabilities;
import com.google.android.exoplayer.audio.AudioCapabilitiesReceiver;
import com.google.android.exoplayer.drm.UnsupportedDrmException;
import com.google.android.exoplayer.metadata.id3.GeobFrame;
import com.google.android.exoplayer.metadata.id3.Id3Frame;
import com.google.android.exoplayer.metadata.id3.PrivFrame;
import com.google.android.exoplayer.metadata.id3.TxxxFrame;
import com.google.android.exoplayer.util.Util;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.List;

import static com.google.android.exoplayer.drm.UnsupportedDrmException.REASON_UNSUPPORTED_SCHEME;

public class ExoVideoPlayerView
        extends TextureView
        implements TextureView.SurfaceTextureListener,
        ExoMediaPlayer.Listener,
        ExoMediaPlayer.Id3MetadataListener,
        AudioCapabilitiesReceiver.Listener {

    // Constants
    private static String TAG = ExoVideoPlayerView.class.getSimpleName();
    private static final CookieManager defaultCookieManager;
    static {
        defaultCookieManager = new CookieManager();
        defaultCookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ORIGINAL_SERVER);
    }

    // Components
    private Surface mSurface;
    private Context mContext;
    private EventLogger mEventLogger;
    private ExoMediaPlayer mMediaPlayer;
    private AudioCapabilitiesReceiver mAudioCapabilitiesReceiver;

    // Attributes
    private boolean mPlayerNeedsPrepare;
    private long mPlayerPosition;
    private Uri mContentUri;
    private int mContentType;
    private String mContentId;
    private String mProvider;

    // Constructor
    public ExoVideoPlayerView(Context context) {
        super(context);

        mContext = context;
        init();
    }

    public ExoVideoPlayerView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mContext = context;
        init();
    }

    public ExoVideoPlayerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mContext = context;
        init();
    }

    /*
    External methods
     */
    public void setContent(Uri contentUri) {
        setContent(contentUri, Util.TYPE_OTHER);
    }

    public void setContent(Uri contentUri, int contentType) {
        setContent(contentUri, contentType, "", "");
    }

    public void setContent(Uri contentUri, int contentType, String contentId, String provider) {
        mContentUri = contentUri;
        mContentType = contentType;
        mContentId = contentId;
        mProvider = provider;

        if (mMediaPlayer == null) {
            preparePlayer(true);
        } else {
            mMediaPlayer.setBackgrounded(false);
        }
    }

    public void destroy() {
        // Using when background play on pause
        // player.setBackgrounded(true);

        mAudioCapabilitiesReceiver.unregister();
        releasePlayer();
    }

    /*
    Internal methods
     */
    private void init() {
        setSurfaceTextureListener(this);

        CookieHandler currentHandler = CookieHandler.getDefault();
        if (currentHandler != defaultCookieManager) {
            CookieHandler.setDefault(defaultCookieManager);
        }

        mAudioCapabilitiesReceiver = new AudioCapabilitiesReceiver(mContext, this);
        mAudioCapabilitiesReceiver.register();
    }

    private ExoMediaPlayer.RendererBuilder getRendererBuilder() {
        String userAgent = Util.getUserAgent(mContext, "ExoVideoPlayer");

        switch (mContentType) {
            case Util.TYPE_SS:
                return new SmoothStreamingRendererBuilder(mContext, userAgent, mContentUri.toString(), new SmoothStreamingTestMediaDrmCallback());
            case Util.TYPE_DASH:
                return new DashRendererBuilder(mContext, userAgent, mContentUri.toString(), new WidevineTestMediaDrmCallback(mContentId, mProvider));
            case Util.TYPE_HLS:
                return new HlsRendererBuilder(mContext, userAgent, mContentUri.toString());
            case Util.TYPE_OTHER:
                return new ExtractorRendererBuilder(mContext, userAgent, mContentUri);
            default:
                throw new IllegalStateException("Unsupported type: " + mContentType);
        }
    }

    private void preparePlayer(boolean playWhenReady) {
        if (mMediaPlayer == null) {
            mMediaPlayer = new ExoMediaPlayer(getRendererBuilder());
            mMediaPlayer.addListener(this);
            mMediaPlayer.setMetadataListener(this);
            mMediaPlayer.seekTo(mPlayerPosition);
            mPlayerNeedsPrepare = true;
            mEventLogger = new EventLogger();
            mEventLogger.startSession();
            mMediaPlayer.addListener(mEventLogger);
            mMediaPlayer.setInfoListener(mEventLogger);
            mMediaPlayer.setInternalErrorListener(mEventLogger);
        }

        if (mPlayerNeedsPrepare) {
            mMediaPlayer.prepare();
            mPlayerNeedsPrepare = false;
        }

        mMediaPlayer.setSurface(mSurface);
        mMediaPlayer.setPlayWhenReady(playWhenReady);
    }

    private void releasePlayer() {
        if (mMediaPlayer != null) {
            mPlayerPosition = mMediaPlayer.getCurrentPosition();
            mMediaPlayer.release();
            mMediaPlayer = null;
            mEventLogger.endSession();
            mEventLogger = null;
        }
    }

    /*
    TextureView.SurfaceTextureListener
     */
    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        mSurface = new Surface(surface);

        if (mMediaPlayer != null) {
            mMediaPlayer.setSurface(mSurface);
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        // Do nothing.
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        if (mMediaPlayer != null) {
            mMediaPlayer.blockingClearSurface();
        }

        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        // TODO Something
    }

    /*
    ExoMediaPlayer.Listener
     */
    @Override
    public void onStateChanged(boolean playWhenReady, int playbackState) {
        String text = "playWhenReady=" + playWhenReady + ", playbackState=";
        switch (playbackState) {
            case ExoPlayer.STATE_BUFFERING:
                text += "buffering";
                break;
            case ExoPlayer.STATE_ENDED:
                text += "ended";
                break;
            case ExoPlayer.STATE_IDLE:
                text += "idle";
                break;
            case ExoPlayer.STATE_PREPARING:
                text += "preparing";
                break;
            case ExoPlayer.STATE_READY:
                text += "ready";
                break;
            default:
                text += "unknown";
                break;
        }
    }

    @Override
    public void onError(Exception e) {
        String errorString = null;
        if (e instanceof UnsupportedDrmException) {
            // Special case DRM failures.
            UnsupportedDrmException unsupportedDrmException = (UnsupportedDrmException) e;
            errorString = mContext.getString(Util.SDK_INT < 18 ? R.string.error_drm_not_supported : unsupportedDrmException.reason == REASON_UNSUPPORTED_SCHEME ? R.string.error_drm_unsupported_scheme : R.string.error_drm_unknown);
        } else if (e instanceof ExoPlaybackException && e.getCause() instanceof DecoderInitializationException) {
            // Special case for decoder initialization failures.
            DecoderInitializationException decoderInitializationException = (DecoderInitializationException) e.getCause();
            if (decoderInitializationException.decoderName == null) {
                if (decoderInitializationException.getCause() instanceof MediaCodecUtil.DecoderQueryException) {
                    errorString = mContext.getString(R.string.error_querying_decoders);
                } else if (decoderInitializationException.secureDecoderRequired) {
                    errorString = mContext.getString(R.string.error_no_secure_decoder, decoderInitializationException.mimeType);
                } else {
                    errorString = mContext.getString(R.string.error_no_decoder, decoderInitializationException.mimeType);
                }
            } else {
                errorString = mContext.getString(R.string.error_instantiating_decoder, decoderInitializationException.decoderName);
            }
        }

        if (errorString != null) {
            // TODO 콜백으로 만들어 주면 좋을 듯
            Toast.makeText(getContext(), errorString, Toast.LENGTH_LONG).show();
        }

        mPlayerNeedsPrepare = true;
    }

    @Override
    public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees, float pixelWidthHeightRatio) {
        // shutterView.setVisibility(View.GONE);
        // videoFrame.setAspectRatio(height == 0 ? 1 : (width * pixelWidthAspectRatio) / height);
        // TODO 좋은 알고리즘?
    }

    /*
    ExoMediaPlayer.Id3MetadataListener
     */
    @Override
    public void onId3Metadata(List<Id3Frame> id3Frames) {
        for (Id3Frame id3Frame : id3Frames) {
            if (id3Frame instanceof TxxxFrame) {
                TxxxFrame txxxFrame = (TxxxFrame) id3Frame;
                Log.i(TAG, String.format("ID3 TimedMetadata %s: description=%s, value=%s", txxxFrame.id, txxxFrame.description, txxxFrame.value));
            } else if (id3Frame instanceof PrivFrame) {
                PrivFrame privFrame = (PrivFrame) id3Frame;
                Log.i(TAG, String.format("ID3 TimedMetadata %s: owner=%s", privFrame.id, privFrame.owner));
            } else if (id3Frame instanceof GeobFrame) {
                GeobFrame geobFrame = (GeobFrame) id3Frame;
                Log.i(TAG, String.format("ID3 TimedMetadata %s: mimeType=%s, filename=%s, description=%s", geobFrame.id, geobFrame.mimeType, geobFrame.filename, geobFrame.description));
            } else {
                Log.i(TAG, String.format("ID3 TimedMetadata %s", id3Frame.id));
            }
        }
    }

    /*
    AudioCapabilitiesReceiver.Listener
     */
    @Override
    public void onAudioCapabilitiesChanged(AudioCapabilities audioCapabilities) {
        if (mMediaPlayer == null) {
            return;
        }

        boolean backgrounded = mMediaPlayer.getBackgrounded();
        boolean playWhenReady = mMediaPlayer.getPlayWhenReady();
        releasePlayer();
        preparePlayer(playWhenReady);
        mMediaPlayer.setBackgrounded(backgrounded);
    }
}
