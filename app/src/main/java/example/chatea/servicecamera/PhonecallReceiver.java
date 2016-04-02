package example.chatea.servicecamera;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import java.util.Date;

public class PhonecallReceiver extends BroadcastReceiver {

    //The receiver will be recreated whenever android feels like it.  We need a static variable to remember data between instantiations

    private static int lastState = TelephonyManager.CALL_STATE_IDLE;
    private static Date callStartTime;
    private static boolean isIncoming;
    private static String savedNumber;  //because the passed incoming is only valid in ringing
    private boolean mRecording;
    private static String TAG = "PhonecallReceiver";


    @Override
    public void onReceive(Context context, Intent intent) {

        //We listen to two intents.  The new outgoing call only tells us of an outgoing call.  We use it to get the number.
        if (intent.getAction().equals("android.intent.action.NEW_OUTGOING_CALL")) {
            savedNumber = intent.getExtras().getString("android.intent.extra.PHONE_NUMBER");
            Log.d(TAG, "NEW_OUTGOING_CALL");
            tryToRecording(context);   //tryToRecording when dialer to someone
        } else {
            String stateStr = intent.getExtras().getString(TelephonyManager.EXTRA_STATE);
            String number = intent.getExtras().getString(TelephonyManager.EXTRA_INCOMING_NUMBER);
            Log.d(TAG, stateStr + "with " + number);
            int state = 0;
            if (stateStr.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
                state = TelephonyManager.CALL_STATE_IDLE;
            } else if (stateStr.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
                state = TelephonyManager.CALL_STATE_OFFHOOK;
            } else if (stateStr.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
                state = TelephonyManager.CALL_STATE_RINGING;
                tryToRecording(context);   //tryToRecording when get someone incoming call
            }

            onCallStateChanged(context, state, number);

        }
    }

    //Derived classes should override these to respond to specific events of interest
    protected void onIncomingCallStarted(Context ctx, String number, Date start) {
    }

    protected void onOutgoingCallStarted(Context ctx, String number, Date start) {
    }

    protected void onIncomingCallEnded(Context ctx, String number, Date start, Date end) {
    }

    protected void onOutgoingCallEnded(Context ctx, String number, Date start, Date end) {
    }

    protected void onMissedCall(Context ctx, String number, Date start) {
    }

    //Deals with actual events

    //Incoming call-  goes from IDLE to RINGING when it rings, to OFFHOOK when it's answered, to IDLE when its hung up
    //Outgoing call-  goes from IDLE to OFFHOOK when it dials out, to IDLE when hung up
    public void onCallStateChanged(Context context, int state, String number) {
        if (lastState == state) {
            //No change, debounce extras
            return;
        }
        switch (state) {
            case TelephonyManager.CALL_STATE_RINGING:
                isIncoming = true;
                callStartTime = new Date();
                savedNumber = number;
                onIncomingCallStarted(context, number, callStartTime);
                Log.d(TAG, "CALL_STATE_RINGING");
                break;
            case TelephonyManager.CALL_STATE_OFFHOOK:
                //Transition of ringing->offhook are pickups of incoming calls.  Nothing done on them
                if (lastState != TelephonyManager.CALL_STATE_RINGING) {
                    isIncoming = false;
                    callStartTime = new Date();
                    onOutgoingCallStarted(context, savedNumber, callStartTime);
                    Log.d(TAG, "CALL_STATE_OFFHOOK");
                }
                break;
            case TelephonyManager.CALL_STATE_IDLE:
                Log.d(TAG, "CALL_STATE_IDLE");
                //Went to idle-  this is the end of a call.  What type depends on previous state(s)
                if (lastState == TelephonyManager.CALL_STATE_RINGING) {
                    //Ring but no pickup-  a miss
                    onMissedCall(context, savedNumber, callStartTime);
                } else if (isIncoming) {
                    onIncomingCallEnded(context, savedNumber, callStartTime, new Date());
                } else {
                    onOutgoingCallEnded(context, savedNumber, callStartTime, new Date());
                }
                break;
        }
        lastState = state;
    }

    private void startRecording(Context c) {
        setRecording(true);

        Intent intent = new Intent(c, CameraService.class);
        ResultReceiver receiver = new ResultReceiver(new Handler()) {
            @Override
            protected void onReceiveResult(int resultCode, Bundle resultData) {
                handleRecordingResult(resultCode, resultData);
            }
        };
        intent.putExtra(CameraService.RESULT_RECEIVER, receiver);
        c.startService(intent);

        Toast.makeText(c, "Start recording...", Toast.LENGTH_SHORT).show();
    }

    private void tryToRecording(Context c) {
        if (mRecording) {
            Toast.makeText(c, "Already recording...", Toast.LENGTH_SHORT).show();
            return;
        }

        startRecording(c);
    }

    private void setRecording(boolean recording) {
        mRecording = recording;
    }

    private void handleRecordingResult(int resultCode, Bundle resultData) {
        setRecording(false);
        if (resultCode == CameraService.RECORD_RESULT_OK) {
            String videoPath = resultData.getString(CameraService.VIDEO_PATH);
            Log.d(TAG, "videoPath =" + videoPath);
//            Toast.makeText(c, "Record succeed, file saved in " + videoPath,
//                    Toast.LENGTH_LONG).show();
        } else {
            Log.d(TAG, "Recording failed.");
//            Toast.makeText(this, "Recording failed...", Toast.LENGTH_SHORT).show();
        }
    }
}