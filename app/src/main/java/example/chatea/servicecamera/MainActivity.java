package example.chatea.servicecamera;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;


public class MainActivity extends Activity {

    private boolean mRecording;

    private Button bt_recordingButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bt_recordingButton = (Button) findViewById(R.id.recording_button);
        bt_recordingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tryToRecording();
            }
        });
    }

    private void tryToRecording() {
        if (mRecording) {
            Toast.makeText(this, "Already recording...", Toast.LENGTH_SHORT).show();
            return;
        }

        startRecording();
    }

    private void startRecording() {
        setRecording(true);

        Intent intent = new Intent(this, CameraService.class);
        ResultReceiver receiver = new ResultReceiver(new Handler()) {
            @Override
            protected void onReceiveResult(int resultCode, Bundle resultData) {
                handleRecordingResult(resultCode, resultData);
            }
        };
        intent.putExtra(CameraService.RESULT_RECEIVER, receiver);
        startService(intent);

        Toast.makeText(this, "Start recording...", Toast.LENGTH_SHORT).show();
    }

    private void setRecording(boolean recording) {
        mRecording = recording;
    }

    private void handleRecordingResult(int resultCode, Bundle resultData) {
        setRecording(false);
        if (resultCode == CameraService.RECORD_RESULT_OK) {
            String videoPath = resultData.getString(CameraService.VIDEO_PATH);
            Toast.makeText(this, "Record succeed, file saved in " + videoPath,
                    Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Recording failed...", Toast.LENGTH_SHORT).show();
        }
    }
}
