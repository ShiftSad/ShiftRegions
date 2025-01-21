package codes.shiftmc.regions.model;

public enum Flag {
    PVP(1),
    BLOCK_BREAK(2),
    BLOCK_PLACE(4),
    INTERACT(8);

    private final int bit;

    Flag(int bit) {
        this.bit = bit;
    }

    public int getBit() {
        return bit;
    }
}
