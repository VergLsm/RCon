package serial.protocol;

/**
 * Created by verg on 15/5/24.
 */
public class VSP_CMD {
    public static final byte VSP = (byte)255;

    public static final byte VSP_REPORT = (byte) 100; //
    public static final byte VSP_STATUS = (byte) 101;
    public static final byte VSP_ANALOG = (byte) 110; //
    public static final byte VSP_SET_RAW_RC = (byte) 200; // in message 8 rc
    // chan;
}
