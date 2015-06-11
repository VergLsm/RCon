package serial.protocol;

/**
 * Created by verg on 15/5/24.
 */
public class RC extends MSP {

    public static enum CHANNEL {
        ROLL, PITCH, YAW, THROTTLE, AUX1, AUX2, AUX3, AUX4
    }

    public RC(byte cmdVSP, int interval) {
        super(cmdVSP,
                new byte[] { (byte) 0xDC, (byte) 0x05, (byte) 0xDC,
                        (byte) 0x05, (byte) 0xDC, (byte) 0x05, (byte) 0xE8,
                        (byte) 0x03, (byte) 0xDC, (byte) 0x05, (byte) 0xDC,
                        (byte) 0x05, (byte) 0xDC, (byte) 0x05, (byte) 0xDC,
                        (byte) 0x05 }, true, interval);
    }

    public void setIntervalTime(int interval){
        this.interval = interval;
    }

    public void setRC(int channel, int val) {
        data[channel * 2] = (byte) (val & 0xff);
        data[channel * 2 + 1] = (byte) ((val >> 8) & 0xff);
        buildSPString();
    }

    public int getRC(int channel) {

        return (data[channel * 2] << 8) | (data[channel * 2 + 1]);
    }
}
