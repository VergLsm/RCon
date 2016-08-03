package serial.protocol;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by verg on 16/7/30.
 */

public class SerialProtocol extends AbstractSerialProtocol {

    public static final byte MSP_IDENT                =(byte) 100;    //out message         multitype + multiwii version + protocol version + capability variable
    public static final byte MSP_STATUS               =(byte) 101;    //out message         cycletime & errors_count & sensor present & box activation & current setting number
    public static final byte MSP_RAW_IMU              =(byte) 102;    //out message         9 DOF
    public static final byte MSP_SERVO                =(byte) 103;    //out message         8 servos
    public static final byte MSP_MOTOR                =(byte) 104;    //out message         8 motors
    public static final byte MSP_RC                   =(byte) 105;    //out message         8 rc chan and more
    public static final byte MSP_RAW_GPS              =(byte) 106;    //out message         fix, numsat, lat, lon, alt, speed, ground course

    @Override
    void evaluateCommand() {
        switch (cmdSP) {
            case MSP_IDENT: {

                break;
            }
            case MSP_STATUS: {
                break;
            }
            case MSP_RAW_IMU: {
                break;
            }
        }
    }

}
