package serial.protocol;


import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 命令生成器
 * Created by verg on 16/7/30.
 */
public abstract class AbstractSerialProtocol {

    /** @hide */
    @IntDef({READ, Sent})
    @Retention(RetentionPolicy.SOURCE)
    public @interface ReadOrSent {}

    public static final byte READ = '<';
    public static final byte Sent = '>';

    /**
     * $M< length cmd data checkSum
     * @param ReadOrSent
     * @param cmd
     * @param data
     * @return
     */
    public static byte[] build(@ReadOrSent byte ReadOrSent, byte cmd, byte... data){
        byte[] command;
        command = new byte[data.length  + 3];

        command[0] = '$';
        command[1] = 'M';
        command[2] = ReadOrSent;

        int i = 3;
        byte checkSum = 0;

        command[i] = (byte) (data.length & 0xff);
        checkSum ^= command[i++];

        command[i] = cmd;
        checkSum ^= command[i++];

        for (int j = 0; j < data.length; j++) {
            command[i] = data[j];
            checkSum ^= command[i++];
        }

        command[i] = checkSum;
        return command;
    }

    private static final byte IDLE = 0;
    private static final byte HEADER_START = 1;
    private static final byte HEADER_M = 2;
    private static final byte HEADER_ARROW = 3;
    private static final byte HEADER_SIZE = 4;
    private static final byte HEADER_CMD = 5;

    private static final int INBUF_SIZE = 32;
    private static byte[] inBuf = new byte[INBUF_SIZE];

    private static byte c_state = IDLE;
    private static byte dataSize;
    private static byte offset;
    private static byte checksum;
    private static byte indRX;
    protected static byte cmdSP;

    public void serialCom(byte[] recBuf) {
        for (int i = 0; i < recBuf.length; i++) {
            if (c_state == IDLE) {
                // c_state = (recBuf[i] == '@') ? HEADER_START : IDLE;
                if (recBuf[i] == '$') {
                    c_state = HEADER_START;
                } else {
                    c_state = IDLE;
                }
            } else if (c_state == HEADER_START) {
                c_state = (recBuf[i] == 'M') ? HEADER_M : IDLE;
            } else if (c_state == HEADER_M) {
                c_state = (recBuf[i] == '>') ? HEADER_ARROW : IDLE;
            } else if (c_state == HEADER_ARROW) {
                if (recBuf[i] > INBUF_SIZE) { // now we are expecting
                    // the
                    // payload size
                    c_state = IDLE;
                    continue;
                }
                dataSize = recBuf[i];
                offset = 0;
                checksum = 0;
                indRX = 0;
                checksum ^= recBuf[i];
                c_state = HEADER_SIZE; // the command is to follow
            } else if (c_state == HEADER_SIZE) {
                cmdSP = recBuf[i];
                checksum ^= recBuf[i];
                c_state = HEADER_CMD;
            } else if (c_state == HEADER_CMD && offset < dataSize) {
                checksum ^= recBuf[i];
                inBuf[offset++] = recBuf[i];
            } else if (c_state == HEADER_CMD && offset >= dataSize) {
                if (checksum == recBuf[i]) { // compare calculated and
                    // transferred checksum
                    evaluateCommand(); // we got a valid packet,
                    // evaluate it
                }
                c_state = IDLE;
            }
        }
    }

    protected static byte read8() {
        return (byte) (inBuf[indRX++] & 0xff);
    }

    protected static short read16() {
        short t = (short) (read8() & 0xff);
        t |= (short) ((read8() & 0xff) << 8);
        return t;
    }

    abstract void evaluateCommand();
}
