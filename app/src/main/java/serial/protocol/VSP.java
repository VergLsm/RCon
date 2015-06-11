package serial.protocol;

/**
 * Created by verg on 15/5/24.
 */
public class VSP extends SP {
    public VSP(byte cmdVSP) {
        super(cmdVSP);
    }

    public VSP(byte cmdVSP, byte[] data) {
        super(cmdVSP, data);
    }

    public VSP(byte cmdVSP, byte[] data, boolean isCycle, int interval) {
        super(cmdVSP, data, isCycle, interval);
    }

    @Override
    protected byte[] header() {
        byte[] header = { '@', 'M', '<' };
        return header;
    }

}
