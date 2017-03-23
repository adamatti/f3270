package net.sf.f3270.impersonator;

class DataBlock {

    private int[] in_
    private int[] out

    DataBlock(int[] in_, int[] out) {
        this.in_ = in_
        this.out = out
    }

    int[] getIn() {
        in_
    }

    int[] getOut() {
        out
    }

    @Override
    String toString() {
        "{in:" + Arrays.toString(in_) + " out:" + Arrays.toString(out) + "}"
    }

}
