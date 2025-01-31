package codes.shiftmc.regions.model;

public enum Flag {
    NONE(0, false),
    PVP(1, false),
    BLOCK_BREAK(2, true),
    BLOCK_PLACE(4, true),
    INTERACT(8, true);

    private final int bit;

    Flag(int bit, boolean perPlayer) {
        this.bit = bit;
    }

    public int getBit() {
        return bit;
    }

    public static int join(Flag... flags) {
        int joined = 0;
        for (Flag flag : flags) {
            joined |= flag.getBit();
        }
        return joined;
    }

    public static boolean hasFlag(int flags, Flag flag) {
        return (flags & flag.getBit()) != 0;
    }
}
