package com.example.awake;

public class DrowsyDetection {

    private static final long THRESHOLD_DURATION = 3000; // 3 seconds
    private static final String STATE_OPEN = "open";
    private static final String STATE_YAWN = "yawn";
    private static final String STATE_CLOSED = "closed";
    private static final String STATE_NO_YAWN = "no_yawn";

    private long openStartTime = 0;
    private long yawnStartTime = 0;
    private long closedStartTime = 0;
    private long noYawnStartTime = 0;
    private static String currentState = STATE_OPEN;

    public  void processState(String state) {
        if (state.equals(currentState)) {
            // The state remains the same, update the timer
            long elapsedTime = System.currentTimeMillis() - getStateStartTime();
            if (elapsedTime >= THRESHOLD_DURATION) {
                // The driver is drowsy or fatigued
                if (currentState.equals(STATE_CLOSED)) {
                    // Drowsy
                    handleDrowsy();
                } else if (currentState.equals(STATE_YAWN)) {
                    // Fatigued
                    handleFatigue();
                }
            }
        } else {
            // State has changed, update the current state
            currentState = state;
            setStateStartTime(System.currentTimeMillis());
        }
    }

    private void handleDrowsy() {
        // Driver is drowsy, take appropriate action
        // Example: Trigger an alarm or notification
    }

    private void handleFatigue() {
        // Driver is fatigued, take appropriate action
        // Example: Suggest taking a break
    }

    private long getStateStartTime() {
        switch (currentState) {
            case STATE_OPEN:
                return openStartTime;
            case STATE_YAWN:
                return yawnStartTime;
            case STATE_CLOSED:
                return closedStartTime;
            case STATE_NO_YAWN:
                return noYawnStartTime;
            default:
                return 0;
        }
    }

    private void setStateStartTime(long startTime) {
        switch (currentState) {
            case STATE_OPEN:
                openStartTime = startTime;
                break;
            case STATE_YAWN:
                yawnStartTime = startTime;
                break;
            case STATE_CLOSED:
                closedStartTime = startTime;
                break;
            case STATE_NO_YAWN:
                noYawnStartTime = startTime;
                break;
        }
    }
}
