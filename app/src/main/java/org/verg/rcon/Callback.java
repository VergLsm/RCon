package org.verg.rcon;

/**
 * verg 服务主动更新UI
 * Created by verg on 15/5/24.
 */
public class Callback {
    /**
     * @param isEnable
     */
    void localStatusChanged(boolean isEnable) {
    }

    /**
     * @param hasTager
     */
    void tagerStatus(boolean hasTager) {
    }

    /**
     * @param isConnected
     */
    void connectStatusChanged(boolean isConnected) {
    }

    /**
     * @param cycleTime
     * @param millivolt
     */
    void relayStatus(short cycleTime, short millivolt) {
    }

    /**
     * @param hasRF
     */
    void relayHardware(byte hasRF) {
    }

    void RCDataReturn(byte cmd, int arg1, int arg2) {
    }
}