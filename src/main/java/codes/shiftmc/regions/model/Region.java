package codes.shiftmc.regions.model;

import codes.shiftmc.regions.math.BlockVector;
import it.unimi.dsi.fastutil.Pair;

import java.util.List;
import java.util.UUID;

public class Region {

    protected UUID id;

    protected BlockVector min;
    protected BlockVector max;

    protected UUID owner;
    protected List<Pair<UUID, Integer>> members; // UUID, flags

    protected int flags;

    public Region(UUID id, BlockVector min, BlockVector max, UUID owner, List<Pair<UUID, Integer>> members, int flags) {
        this.id = id;
        this.owner = owner;
        this.members = members;
        this.flags = flags;

        setMinMaxPoints(Pair.of(min, max));
    }

    protected void setMinMaxPoints(Pair<BlockVector, BlockVector> points) {
        int minX = Math.min(points.left().x(), points.right().x());
        int minY = Math.min(points.left().y(), points.right().y());
        int minZ = Math.min(points.left().z(), points.right().z());
        int maxX = Math.max(points.left().x(), points.right().x());
        int maxY = Math.max(points.left().y(), points.right().y());
        int maxZ = Math.max(points.left().z(), points.right().z());

        min = new BlockVector(minX, minY, minZ);
        max = new BlockVector(maxX, maxY, maxZ);
    }

    protected void addFlag(Flag flag) {
        flags |= flag.getBit();
    }

    protected void removeFlag(Flag flag) {
        flags &= ~flag.getBit();
    }

    protected boolean hasFlag(Flag flag) {
        return (flags & flag.getBit()) != 0;
    }

    // I miss lombok :(

    public int flags() {
        return flags;
    }

    public UUID id() {
        return id;
    }

    public Region setId(UUID id) {
        this.id = id;
        return this;
    }

    public BlockVector min() {
        return min;
    }

    public Region setMin(BlockVector min) {
        setMinMaxPoints(Pair.of(min, max));
        return this;
    }

    public BlockVector max() {
        return max;
    }

    public Region setMax(BlockVector max) {
        setMinMaxPoints(Pair.of(min, max));
        return this;
    }

    public UUID owner() {
        return owner;
    }

    public Region setOwner(UUID owner) {
        this.owner = owner;
        return this;
    }

    public List<Pair<UUID, Integer>> members() {
        return members;
    }

    public Region setMembers(List<Pair<UUID, Integer>> members) {
        this.members = members;
        return this;
    }
}
