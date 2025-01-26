package codes.shiftmc.regions.math;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.util.Transformation;
import org.joml.Vector3d;

public class Line {

    private Vector3d start;
    private Vector3d end;
    private BlockDisplay blockDisplay;

    public Line(Vector3d start, Vector3d end) {
        this.start = start;
        this.end = end;
    }

    public void render(World world) {
        blockDisplay = world.spawn(MathUtils.toLocation(start, world), BlockDisplay.class, CreatureSpawnEvent.SpawnReason.COMMAND);
        blockDisplay.setBlock(Material.RED_STAINED_GLASS.createBlockData());
        blockDisplay.setGlowing(true);

        teleportEntity(world);
    }

    public void teleportEntity(World world) {
        double dx = end.x() - start.x();
        double dy = end.y() - start.y();
        double dz = end.z() - start.z();
        double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
        Transformation transformation = blockDisplay.getTransformation();
        transformation.getScale().set(0.25, 0.25, distance);
        blockDisplay.setTransformation(transformation);

        double squareRoot = Math.sqrt(dx * dx + dz * dz);
        float pitch = (float) -Math.toDegrees(Math.atan2(dy, squareRoot));
        float yaw = (float) (Math.toDegrees(Math.atan2(dz, dx)) - 90);
        blockDisplay.teleport(MathUtils.toLocation(start, world).addRotation(yaw, pitch));
    }

    public void setEnd(Vector3d vector3f) {
        this.end = vector3f;
        teleportEntity(blockDisplay.getWorld());
    }

    public void setStart(Vector3d vector3f) {
        this.start = vector3f;
        teleportEntity(blockDisplay.getWorld());
    }

    /**
     * Removes the line from the world
     */
    public void dispose() {
        blockDisplay.remove();
        blockDisplay = null;
    }
}