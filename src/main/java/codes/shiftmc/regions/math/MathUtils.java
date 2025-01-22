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
}
