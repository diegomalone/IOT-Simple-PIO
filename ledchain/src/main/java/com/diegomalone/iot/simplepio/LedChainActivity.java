package com.diegomalone.iot.simplepio;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.diegomalone.iot.common.utils.BoardUtils;
import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.GpioCallback;
import com.google.android.things.pio.PeripheralManagerService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Diego Malone on 26/07/17.
 */

public class LedChainActivity extends Activity {
    private static final String TAG = LedChainActivity.class.getSimpleName();
    private static final int INTERVAL_BETWEEN_BLINKS_MS = 300;

    private static final int LEDS_COUNT = 3;

    private Gpio mButton;
    private Gpio mLedIn;
    private List<Gpio> mLedList = new ArrayList<>();

    private volatile boolean mIsBlinking = false;

    private GpioCallback mButtonCallback = new GpioCallback() {
        @Override
        public boolean onGpioEdge(Gpio gpio) {
            mIsBlinking = !mIsBlinking;
            if (mIsBlinking) {
                startBlink();
            } else {
                stopBlink();
            }
            return true;
        }
    };

    private GpioCallback mSimpleInputCallback = new GpioCallback() {
        @Override
        public boolean onGpioEdge(Gpio gpio) {
            if (mIsBlinking) {
                startBlink();
            }
            return true;
        }
    };

    private Thread mBlinkThread = new Thread(new Runnable() {
        @Override
        public void run() {
            for (Gpio led : mLedList) {
                if (mIsBlinking) {
                    try {
                        led.setValue(true);
                        Thread.sleep(INTERVAL_BETWEEN_BLINKS_MS);
                        led.setValue(false);
                    } catch (IOException | InterruptedException e) {
                        logIOError(e);
                    }
                }
            }
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PeripheralManagerService service = new PeripheralManagerService();
        try {
            mButton = service.openGpio(BoardUtils.getGPIOForButton());
            mButton.setDirection(Gpio.DIRECTION_IN);
            mButton.setEdgeTriggerType(Gpio.EDGE_FALLING);
            mButton.registerGpioCallback(mButtonCallback);

            mLedIn = service.openGpio(BoardUtils.getGPIOForSimpleInput());
            mLedIn.setDirection(Gpio.DIRECTION_IN);
            mLedIn.setEdgeTriggerType(Gpio.EDGE_FALLING);
            mLedIn.registerGpioCallback(mSimpleInputCallback);

            List<String> ledPinList = BoardUtils.getGPIOForLED(LEDS_COUNT);

            for (String ledPin : ledPinList) {
                mLedList.add(service.openGpio(ledPin));
            }

            for (Gpio led : mLedList) {
                led.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
            }
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

        if (mLedIn != null) {
            mLedIn.unregisterGpioCallback(mSimpleInputCallback);
            try {
                mLedIn.close();
            } catch (IOException e) {
                logIOError(e);
            }
        }

        for (Gpio led : mLedList) {
            if (led != null) {
                try {
                    led.close();
                } catch (IOException e) {
                    logIOError(e);
                }
            }
        }
    }

    private synchronized void startBlink() {
        mIsBlinking = true;

        if (mBlinkThread.isAlive()) {
            mBlinkThread.interrupt();
        }

        stopBlink();

        mBlinkThread.start();
    }

    private void stopBlink() {
        for (Gpio led : mLedList) {
            if (led != null) {
                try {
                    led.setValue(false);
                } catch (IOException e) {
                    logIOError(e);
                }
            }
        }
    }

    private void logIOError(Exception e) {
        Log.e(TAG, "Error while manipulating IO", e);
    }
}
