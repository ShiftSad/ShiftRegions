package codes.shiftmc.regions.math;

import org.bukkit.Location;
import org.bukkit.World;
import org.joml.Vector3d;

public final class MathUtils {

    public static Location toLocation(BlockVector vector, World world) {
        return new Location(world, vector.x(), vector.y(), vector.z());
    }

    public static Location toLocation(Vector3d vector, World world) {
        return new Location(world, vector.x(), vector.y(), vector.z());
    }

    public static Vector3d toVector(Location location) {
        return new Vector3d(location.getX(), location.getY(), location.getZ());
    }

    public static BlockVector toBlockVector(Location location) {
        return new BlockVector(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    public static Vector3d toVector(BlockVector blockVector) {
        return new Vector3d(blockVector.x(), blockVector.y(), blockVector.z());
    }

    public static BlockVector toBlockVector(Vector3d vector) {
        return new BlockVector((int) vector.x(), (int) vector.y(), (int) vector.z());
    }
}
