package example.chatea.servicecamera;

import android.app.Activity;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.session.MediaController;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.VideoView;

import java.io.IOException;


public class MainActivity extends Activity {

    private Button bt_recordingButton, bt_playingVideoButton;

    private VideoView mVideoView;
    private String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mVideoView = (VideoView) findViewById(R.id.videoview);
        bt_playingVideoButton = (Button) findViewById(R.id.play_video_button);

        bt_recordingButton = (Button) findViewById(R.id.recording_button);
        bt_recordingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startRecording();
            }
        });

        bt_playingVideoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String DataPath = CameraService.DataPath;
                Log.d(TAG, DataPath);

                mVideoView.setVideoPath(DataPath);
//                mVideoView.setMediaController(new MediaController(this)); //API 21
                mVideoView.requestFocus();
                mVideoView.start();
            }
        });
    }

    private void startRecording() {
        Intent intent = new Intent(this, CameraService.class);
        startService(intent);
    }
}
