package com.diegomalone.iot.common.utils;

import android.os.Build;

import com.google.android.things.pio.PeripheralManagerService;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by diegomalone on 22/07/17.
 */

@SuppressWarnings("WeakerAccess")
public class BoardUtils {
    private static final String DEVICE_EDISON_ARDUINO = "edison_arduino";
    private static final String DEVICE_EDISON = "edison";
    private static final String DEVICE_JOULE = "joule";
    private static final String DEVICE_RPI3 = "rpi3";
    private static final String DEVICE_IMX6UL_PICO = "imx6ul_pico";
    private static final String DEVICE_IMX6UL_VVDN = "imx6ul_iopb";
    private static final String DEVICE_IMX7D_PICO = "imx7d_pico";
    private static String sBoardVariant = "";

    /**
     * Return the GPIO pin that the LED is connected on.
     * For example, on Intel Edison Arduino breakout, pin "IO13" is connected to an onboard LED
     * that turns on when the GPIO pin is HIGH, and off when low.
     */
    public static String getGPIOForLED() {
        switch (getBoardVariant()) {
            case DEVICE_EDISON_ARDUINO:
                return "IO13";
            case DEVICE_EDISON:
                return "GP45";
            case DEVICE_JOULE:
                return "J6_25";
            case DEVICE_RPI3:
                return "BCM6";
            case DEVICE_IMX6UL_PICO:
                return "GPIO4_IO20";
            case DEVICE_IMX6UL_VVDN:
                return "GPIO3_IO06";
            case DEVICE_IMX7D_PICO:
                return "GPIO_34";
            default:
                throw new IllegalStateException("Unknown Build.DEVICE " + Build.DEVICE);
        }
    }

    public static List<String> getGPIOForLED(int numberOfLEDs) {
        List<String> pinList = new ArrayList<>();

        if (numberOfLEDs >= 1) {
            pinList.add(getGPIOForLED());
        }

        if (numberOfLEDs >= 2) {
            pinList.add("BCM5");
        }

        if (numberOfLEDs >= 3) {
            pinList.add("BCM12");
        }

        if (numberOfLEDs >= 4) {
            pinList.add("BCM22");
        }

        if (numberOfLEDs >= 5) {
            pinList.add("BCM23");
        }

        if (numberOfLEDs >= 6) {
            pinList.add("BCM24");
        }

        if (numberOfLEDs >= 7) {
            pinList.add("BCM25");
        }

        return pinList;
    }

    /**
     * Return the GPIO pin that the Button is connected on.
     */
    public static String getGPIOForButton() {
        switch (getBoardVariant()) {
            case DEVICE_EDISON_ARDUINO:
                return "IO12";
            case DEVICE_EDISON:
                return "GP44";
            case DEVICE_JOULE:
                return "J7_71";
            case DEVICE_RPI3:
                return "BCM21";
            case DEVICE_IMX6UL_PICO:
                return "GPIO4_IO20";
            case DEVICE_IMX6UL_VVDN:
                return "GPIO3_IO01";
            case DEVICE_IMX7D_PICO:
                return "GPIO_174";
            default:
                throw new IllegalStateException("Unknown Build.DEVICE " + Build.DEVICE);
        }
    }

    public static String getGPIOForSimpleInput() {
        return "BCM26";
    }

    public static String getGPIOForServo() {
        return "PWM0";
    }

    private static String getBoardVariant() {
        if (!sBoardVariant.isEmpty()) {
            return sBoardVariant;
        }
        sBoardVariant = Build.DEVICE;
        // For the edison check the pin prefix
        // to always return Edison Breakout pin name when applicable.
        if (sBoardVariant.equals(DEVICE_EDISON)) {
            PeripheralManagerService pioService = new PeripheralManagerService();
            List<String> gpioList = pioService.getGpioList();
            if (gpioList.size() != 0) {
                String pin = gpioList.get(0);
                if (pin.startsWith("IO")) {
                    sBoardVariant = DEVICE_EDISON_ARDUINO;
                }
            }
        }
        return sBoardVariant;
    }
}
