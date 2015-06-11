package serial.protocol;

/**
 * 设置常量类
 * Created by verg on 15/5/24.
 */
public class MSP_CMD {

    public static final byte MSP_IDENT                =(byte) 100;    //out message         multitype + multiwii version + protocol version + capability variable
    public static final byte MSP_STATUS               =(byte) 101;    //out message         cycletime & errors_count & sensor present & box activation & current setting number
    public static final byte MSP_RAW_IMU              =(byte) 102;    //out message         9 DOF
    public static final byte MSP_SERVO                =(byte) 103;    //out message         8 servos
    public static final byte MSP_MOTOR                =(byte) 104;    //out message         8 motors
    public static final byte MSP_RC                   =(byte) 105;    //out message         8 rc chan and more
    public static final byte MSP_RAW_GPS              =(byte) 106;    //out message         fix, numsat, lat, lon, alt, speed, ground course
    public static final byte MSP_COMP_GPS             =(byte) 107;    //out message         distance home, direction home
    public static final byte MSP_ATTITUDE             =(byte) 108;    //out message         2 angles 1 heading
    public static final byte MSP_ALTITUDE             =(byte) 109;    //out message         altitude, variometer
    public static final byte MSP_ANALOG               =(byte) 110;    //out message         vbat, powermetersum, rssi if available on RX
    public static final byte MSP_RC_TUNING            =(byte) 111;    //out message         rc rate, rc expo, rollpitch rate, yaw rate, dyn throttle PID
    public static final byte MSP_PID                  =(byte) 112;    //out message         P I D coeff (9 are used currently)
    public static final byte MSP_BOX                  =(byte) 113;    //out message         BOX setup (number is dependant of your setup)
    public static final byte MSP_MISC                 =(byte) 114;    //out message         powermeter trig
    public static final byte MSP_MOTOR_PINS           =(byte) 115;    //out message         which pins are in use for motors & servos, for GUI
    public static final byte MSP_BOXNAMES             =(byte) 116;    //out message         the aux switch names
    public static final byte MSP_PIDNAMES             =(byte) 117;    //out message         the PID names
    public static final byte MSP_WP                   =(byte) 118;    //out message         get a WP, WP# is in the payload, returns (WP#, lat, lon, alt, flags) WP#0-home, WP#16-poshold
    public static final byte MSP_BOXIDS               =(byte) 119;    //out message         get the permanent IDs associated to BOXes
    public static final byte MSP_SERVO_CONF           =(byte) 120;    //out message         Servo settings
    public static final byte MSP_NAV_STATUS           =(byte) 121;    //out message         Returns navigation status
    public static final byte MSP_NAV_CONFIG           =(byte) 122;    //out message         Returns navigation parameters

    public static final byte MSP_SET_RAW_RC           =(byte) 200;    //in message          8 rc chan
    public static final byte MSP_SET_RAW_GPS          =(byte) 201;    //in message          fix, numsat, lat, lon, alt, speed
    public static final byte MSP_SET_PID              =(byte) 202;    //in message          P I D coeff (9 are used currently)
    public static final byte MSP_SET_BOX              =(byte) 203;    //in message          BOX setup (number is dependant of your setup)
    public static final byte MSP_SET_RC_TUNING        =(byte) 204;    //in message          rc rate, rc expo, rollpitch rate, yaw rate, dyn throttle PID
    public static final byte MSP_ACC_CALIBRATION      =(byte) 205;    //in message          no param
    public static final byte MSP_MAG_CALIBRATION      =(byte) 206;    //in message          no param
    public static final byte MSP_SET_MISC             =(byte) 207;    //in message          powermeter trig + 8 free for future use
    public static final byte MSP_RESET_CONF           =(byte) 208;    //in message          no param
    public static final byte MSP_SET_WP               =(byte) 209;    //in message          sets a given WP (WP#,lat, lon, alt, flags)
    public static final byte MSP_SELECT_SETTING       =(byte) 210;    //in message          Select Setting Number (0-2)
    public static final byte MSP_SET_HEAD             =(byte) 211;    //in message          define a new heading hold direction
    public static final byte MSP_SET_SERVO_CONF       =(byte) 212;    //in message          Servo settings
    public static final byte MSP_SET_MOTOR            =(byte) 214;    //in message          PropBalance function
    public static final byte MSP_SET_NAV_CONFIG       =(byte) 215;    //in message          Sets nav config parameters - write to the eeprom

    // public static final byte MSP_BIND                 240    //in message          no param

    public static final byte MSP_EEPROM_WRITE         =(byte) 250;    //in message          no param

    public static final byte MSP_DEBUGMSG             =(byte) 253;    //out message         debug string buffer
    public static final byte MSP_DEBUG                =(byte) 254;    //out message         debug1,debug2,debug3,debug4

    // Additional commands that are not compatible with MultiWii
    public static final byte MSP_UID                  =(byte) 160;    //out message         Unique device ID
    public static final byte MSP_ACC_TRIM             =(byte) 240;    //out message         get acc angle trim values
    public static final byte MSP_SET_ACC_TRIM         =(byte) 239;    //in message          set acc angle trim values
    public static final byte MSP_GPSSVINFO            =(byte) 164;    //out message         get Signal Strength (only U-Blox)

    // Additional private MSP for baseflight configurator
    public static final byte MSP_RCMAP                =(byte) 64;     //out message         get channel map (also returns number of channels total)
    public static final byte MSP_SET_RCMAP            =(byte) 65;     //in message          set rc map, numchannels to set comes from MSP_RCMAP
    public static final byte MSP_CONFIG               =(byte) 66;     //out message         baseflight-specific settings that aren't covered elsewhere
    public static final byte MSP_SET_CONFIG           =(byte) 67;     //in message          baseflight-specific settings save
    public static final byte MSP_REBOOT               =(byte) 68;     //in message          reboot settings
    public static final byte MSP_BUILDINFO            =(byte) 69;     //out message         build date as well as some space for future expansion

}