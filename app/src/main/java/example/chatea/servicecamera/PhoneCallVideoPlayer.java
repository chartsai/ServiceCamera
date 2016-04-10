package example.chatea.servicecamera;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.MediaController;
import android.widget.VideoView;

public class PhoneCallVideoPlayer extends ActionBarActivity {

    private VideoView mVideoView;
    private static final String TAG = PhoneCallVideoPlayer.class.getSimpleName();;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.phone_call_video_player);
        mVideoView = (VideoView) findViewById(R.id.my_videoview);
        Intent intent = this.getIntent();
        String mRecordingPath = intent.getStringExtra(CameraService.VIDEO_PATH);
        Log.i(TAG, mRecordingPath);
        mVideoView.setVideoURI(Uri.parse(mRecordingPath));
        mVideoView.setMediaController(new MediaController(this));
        mVideoView.requestFocus();
        mVideoView.start();
    }
}
