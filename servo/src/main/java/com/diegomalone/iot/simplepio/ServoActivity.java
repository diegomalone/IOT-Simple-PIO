package com.diegomalone.iot.simplepio;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.diegomalone.iot.common.utils.BoardUtils;
import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.GpioCallback;
import com.google.android.things.pio.PeripheralManagerService;
import com.google.android.things.pio.Pwm;

import java.io.IOException;

/**
 * Created by Diego Malone on 26/07/17.
 */

public class ServoActivity extends Activity {
    private static final String TAG = ServoActivity.class.getSimpleName();

    private static final int SERVO_FREQUENCY = 50;
    private static final int SERVO_DUTY_CYCLE_90 = 2;
    private static final int SERVO_DUTY_CYCLE_180 = 20;

    private static final int SERVO_MOVEMENT_DURATION_MS = 500;

    private Gpio mButton;
    private Pwm mServo;

    private boolean mIsActive = false;
    private boolean mIs90 = false;

    private GpioCallback mButtonCallback = new GpioCallback() {
        @Override
        public boolean onGpioEdge(Gpio gpio) {
            mIs90 = !mIs90;

            if (!mIsActive) {
                startServo();
            }

            return true;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PeripheralManagerService service = new PeripheralManagerService();
        try {
            mButton = service.openGpio(BoardUtils.getGPIOForButton());
            mButton.setDirection(Gpio.DIRECTION_IN);
            mButton.setEdgeTriggerType(Gpio.EDGE_FALLING);
            mButton.registerGpioCallback(mButtonCallback);

            mServo = service.openPwm(BoardUtils.getGPIOForServo());
            mServo.setPwmFrequencyHz(SERVO_FREQUENCY);
            mServo.setPwmDutyCycle(SERVO_DUTY_CYCLE_90);
        } catch (IOException e) {
            logIOError(e);
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mButton != null) {
            mButton.unregisterGpioCallback(mButtonCallback);
            try {
                mButton.close();
            } catch (IOException e) {
                logIOError(e);
            }
        }
    }

    private synchronized void startServo() {
        mIsActive = true;

        updateDutyCycle();

        try {
            mServo.setEnabled(true);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(SERVO_MOVEMENT_DURATION_MS);
                    } catch (InterruptedException e) {
                        logIOError(e);
                    }
                    stopServo();
                }
            }).start();
        } catch (IOException e) {
            logIOError(e);
        }
    }

    private void updateDutyCycle() {
        try {
            if (mIs90) {
                mServo.setPwmDutyCycle(SERVO_DUTY_CYCLE_90);
            } else {
                mServo.setPwmDutyCycle(SERVO_DUTY_CYCLE_180);
            }
        } catch (IOException e) {
            logIOError(e);
        }
    }

    private void stopServo() {
        try {
            mServo.setEnabled(false);
        } catch (IOException e) {
            logIOError(e);
        }

        mIsActive = false;
    }

    private void logIOError(Exception e) {
        Log.e(TAG, "Error while manipulating IO", e);
    }
}
