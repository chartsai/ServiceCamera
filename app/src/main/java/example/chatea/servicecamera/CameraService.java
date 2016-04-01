package example.chatea.servicecamera;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class CameraService extends Service {

    public static final String RESULT_RECEIVER = "resultReceiver";
    public static final String VIDEO_PATH = "recordedVideoPath";

    public static final int RECORD_RESULT_OK = 0;
    public static final int RECORD_RESULT_DEVICE_NO_CAMERA= 1;
    public static final int RECORD_RESULT_GET_CAMERA_FAILED = 2;

    private Camera mCamera;
    private MediaRecorder mMediaRecorder;

    public CameraService() {
    }

    /**
     * Used to take picture.
     */
    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            File pictureFile = Util.getOutputMediaFile(Util.MEDIA_TYPE_IMAGE);

            if (pictureFile == null) {
                return;
            }

            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                fos.close();
            } catch (FileNotFoundException e) {
            } catch (IOException e) {
            }
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.d("TAG", "======= service in onStartCommand");

        final ResultReceiver resultReceiver = intent.getParcelableExtra(RESULT_RECEIVER);

        if (Util.checkCameraHardware(this)) {
            mCamera = Util.getCameraInstance();
            if (mCamera != null) {
                SurfaceView sv = new SurfaceView(this);

                WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
                WindowManager.LayoutParams params = new WindowManager.LayoutParams(1, 1,
                        WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
                        WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                        PixelFormat.TRANSLUCENT);


                SurfaceHolder sh = sv.getHolder();

                sv.setZOrderOnTop(true);
                sh.setFormat(PixelFormat.TRANSPARENT);


                sh.addCallback(new SurfaceHolder.Callback() {
                    @Override
                    public void surfaceCreated(SurfaceHolder holder) {
                        Camera.Parameters params = mCamera.getParameters();
                        mCamera.setParameters(params);
                        Camera.Parameters p = mCamera.getParameters();

                        List<Camera.Size> listSize;

                        listSize = p.getSupportedPreviewSizes();
                        Camera.Size mPreviewSize = listSize.get(2);
                        Log.v("TAG", "preview width = " + mPreviewSize.width
                                + " preview height = " + mPreviewSize.height);
                        p.setPreviewSize(mPreviewSize.width, mPreviewSize.height);

                        listSize = p.getSupportedPictureSizes();
                        Camera.Size mPictureSize = listSize.get(2);
                        Log.v("TAG", "capture width = " + mPictureSize.width
                                + " capture height = " + mPictureSize.height);
                        p.setPictureSize(mPictureSize.width, mPictureSize.height);
                        mCamera.setParameters(p);

                        try {
                            mCamera.setPreviewDisplay(holder);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        mCamera.startPreview();
//                    mCamera.takePicture(null, null, mPicture); // used to takePicture.

                        mCamera.unlock();

                        mMediaRecorder = new MediaRecorder();
                        mMediaRecorder.setCamera(mCamera);

                        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

                        mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));

                        final String recordedVideoPath = Util.getOutputMediaFile(Util.MEDIA_TYPE_VIDEO).getPath();
                        mMediaRecorder.setOutputFile(recordedVideoPath);

                        mMediaRecorder.setPreviewDisplay(holder.getSurface());

                        try {
                            mMediaRecorder.prepare();
                        } catch (IllegalStateException e) {
                            Log.d("TAG", "====== IllegalStateException preparing MediaRecorder: " + e.getMessage());
                        } catch (IOException e) {
                            Log.d("TAG", "====== IOException preparing MediaRecorder: " + e.getMessage());
                        }
                        mMediaRecorder.start();
                        Log.d("TAG", "========= recording start");

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mMediaRecorder.stop();
                                mMediaRecorder.release();
                                mCamera.stopPreview();
                                mCamera.release();

                                Bundle b = new Bundle();
                                b.putString(VIDEO_PATH, recordedVideoPath);
                                resultReceiver.send(RECORD_RESULT_OK, b);
                                Log.d("TAG", "========== recording finished.");
                            }
                        }, 10000);
                    }

                    @Override
                    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                    }

                    @Override
                    public void surfaceDestroyed(SurfaceHolder holder) {
                    }
                });


                wm.addView(sv, params);

            } else {
                Log.d("TAG", "==== get Camera from service failed");
                resultReceiver.send(RECORD_RESULT_GET_CAMERA_FAILED, null);
            }
        } else {
            Log.d("TAG", "==== There is no camera hardware on device.");
            resultReceiver.send(RECORD_RESULT_DEVICE_NO_CAMERA, null);
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
