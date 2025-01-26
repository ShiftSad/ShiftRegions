package codes.shiftmc.regions.math;

public record BlockVector(
    int x,
    int y,
    int z
) {
    public BlockVector(Integer[] coords) {
        this(coords[0], coords[1], coords[2]);
    }
}
