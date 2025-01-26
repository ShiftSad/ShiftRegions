package codes.shiftmc.regions.model;

import java.util.UUID;

public class UserData {

    private final UUID id;
    private int blockClaims;
    private int maxBlockClaims;

    public UserData(UUID id, int blockClaims, int maxBlockClaims) {
        this.id = id;
        this.blockClaims = blockClaims;
        this.maxBlockClaims = maxBlockClaims;
    }

    // I miss lombok :(

    public UUID id() {
        return id;
    }

    public int blockClaims() {
        return blockClaims;
    }

    public UserData setBlockClaims(int blockClaims) {
        this.blockClaims = blockClaims;
        return this;
    }

    public int maxBlockClaims() {
        return maxBlockClaims;
    }

    public UserData setMaxBlockClaims(int maxBlockClaims) {
        this.maxBlockClaims = maxBlockClaims;
        return this;
    }
}
