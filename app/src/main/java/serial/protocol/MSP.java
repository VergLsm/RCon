package serial.protocol;

/**
 * Created by verg on 15/5/24.
 */
public class MSP extends SP {

    public MSP(byte cmdMSP) {
        super(cmdMSP);
    }

    public MSP(byte cmdMSP, byte[] data) {
        super(cmdMSP,data);
    }

    public MSP(byte cmdMSP, byte[] data, boolean isCycle, int interval) {
        super(cmdMSP, data, isCycle, interval);
    }

    @Override
    protected byte[] header() {
        byte[] header = {'$','M','<'};
        return header;
    }
}
