package serial.protocol;

/**
 * Created by verg on 15/5/24.
 */
public abstract class SP {

    protected byte cmdSP;
    protected final byte[] header;
    protected byte[] data;
    protected byte[] temp;
    protected boolean isCycle;
    protected int interval;

    /**
     * 默认无数据，不循环，间隔时间为0
     *
     * @param cmdSP
     */
    public SP(byte cmdSP) {
        this(cmdSP, new byte[0]);
    }

    public SP(byte cmdSP, byte[] data) {
        this(cmdSP, data, false, 0);
    }

    public SP(byte cmdSP, byte[] data, boolean isCycle, int interval) {
        this.cmdSP = cmdSP;
        this.data = data;
        this.isCycle = isCycle;
        this.interval = interval;
        header = header();
        buildSPString();
    }

    protected void buildSPString() {
        byte checkSum = 0;
        int i;
        temp = new byte[header.length + data.length + 3];
        for (i = 0; i < header.length; i++) {
            temp[i] = header[i];
        }
        temp[i] = (byte) (data.length & 0xff);
        checkSum ^= temp[i++];
        temp[i] = cmdSP;
        checkSum ^= temp[i++];
        for (int j = 0; j < data.length; j++) {
            temp[i] = data[j];
            checkSum ^= temp[i++];
        }
        // temp[i++] = getChecksum();
        temp[i] = checkSum;
    }

    abstract protected byte[] header();

    public byte[] getSPString() {
        return temp;
    }

    public void setData(byte[] data) {
        // this.dataSize = (byte) (data.length & 0xff);
        this.data = data;
        buildSPString();
    }

    public boolean isCyle() {
        return isCycle;
    }

    public int getInterval() {
        return interval;
    }

    /**
     * 计算校验码<br>
     * 三个参数分别为： 数据长度 ， 指令代码 ， 实际数据数组
     *
     * @author verg
     * @version 1.00
     */
	/*
	 * protected byte getChecksum(byte cmdMSP, byte data[]) { byte checksum = 0;
	 * checksum ^= (byte) (data.length & 0xff); checksum ^= cmdMSP; for (int i =
	 * 0; i < data.length; i++) { checksum ^= data[i]; } return checksum; }
	 * 
	 * protected byte getChecksum() { return getChecksum(cmdSP, data); }
	 */

    public String bytes2Hex(byte[] src) {
        char[] res = new char[src.length * 2];
        final char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8',
                '9', 'A', 'B', 'C', 'D', 'E', 'F' };
        for (int i = 0, j = 0; i < src.length; i++) {
            res[j++] = hexDigits[src[i] >>> 4 & 0x0f];
            res[j++] = hexDigits[src[i] & 0x0f];
        }

        return new String(res);
    }

}
