package net.sf.f3270.impersonator

class DataByte {

    enum Direction {
        CLIENT_TO_SERVER, SERVER_TO_CLIENT
    }

    private Direction direction
    private int data

    DataByte(Direction direction, int data) {
        this.direction = direction
        this.data = data
    }

    Direction getDirection() {
        direction
    }

    int getData() {
        data
    }
}
