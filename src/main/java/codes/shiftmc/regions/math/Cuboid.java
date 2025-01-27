package codes.shiftmc.regions.math;

import it.unimi.dsi.fastutil.Pair;
import org.bukkit.World;
import org.joml.Vector3d;

public class Cuboid {

    private final Vector3d first;
    private final Vector3d second;

    private Line[] lines;

    public Cuboid(Pair<Vector3d, Vector3d> pair) {
        this.first = pair.first();
        this.second = pair.second();
    }

    public int getVolume() {
        return (int) ((second.x - first.x) * (second.y - first.y) * (second.z - first.z));
    }

    public Vector3d getFirst() {
        return first;
    }

    public Vector3d getSecond() {
        return second;
    }

    /**
     * Creates the 12 edges as Line objects from the initial min/max.
     * This is only called once, in the constructor.
     */
    private void initializeLines() {
        lines = new Line[12];
        lines[0] = new Line(
                new Vector3d(first),
                new Vector3d(first.x, first.y, second.z)
        );
        lines[1] = new Line(
                new Vector3d(first),
                new Vector3d(first.x, second.y, first.z)
        );
        lines[2] = new Line(
                new Vector3d(first),
                new Vector3d(second.x + 1, first.y, first.z)
        );
        lines[3] = new Line(
                new Vector3d(first.x, second.y, second.z),
                new Vector3d(first.x, first.y, second.z)
        );
        lines[4] = new Line(
                new Vector3d(first.x, second.y, second.z),
                new Vector3d(first.x, second.y, first.z)
        );
        lines[5] = new Line(
                new Vector3d(first.x, second.y, second.z),
                new Vector3d(second.x, second.y, second.z)
        );
        lines[6] = new Line(
                new Vector3d(second.x, first.y, second.z),
                new Vector3d(first.x, first.y, second.z)
        );
        lines[7] = new Line(
                new Vector3d(second.x, first.y, second.z),
                new Vector3d(second.x, first.y, first.z)
        );
        lines[8] = new Line(
                new Vector3d(second.x, first.y, second.z),
                new Vector3d(second.x, second.y, second.z)
        );
        lines[9] = new Line(
                new Vector3d(second.x, second.y, first.z),
                new Vector3d(first.x, second.y, first.z)
        );
        lines[10] = new Line(
                new Vector3d(second.x, second.y, first.z),
                new Vector3d(second.x, first.y, first.z)
        );
        lines[11] = new Line(
                new Vector3d(second.x, second.y, first.z),
                new Vector3d(second.x, second.y, second.z)
        );
    }

    /**
     * Whenever min or max changes, we need to re-set the start/end
     * for all twelve lines to reflect the new corners.
     */
    private void updateLines() {
        if (lines == null) {
            initializeLines();
            return;
        }

        // Bottom face (y = min.y)
        lines[0].setStart(new Vector3d(first.x, first.y, first.z)); // Line 1
        lines[0].setEnd(new Vector3d(first.x, first.y, second.z));   // From (min.x, min.y, min.z) -> (min.x, min.y, max.z)

        lines[1].setStart(new Vector3d(first.x, first.y, second.z)); // Line 2
        lines[1].setEnd(new Vector3d(second.x, first.y, second.z));   // From (min.x, min.y, max.z) -> (max.x, min.y, max.z)

        lines[2].setStart(new Vector3d(second.x, first.y, second.z)); // Line 3
        lines[2].setEnd(new Vector3d(second.x, first.y, first.z));   // From (max.x, min.y, max.z) -> (max.x, min.y, min.z)

        lines[3].setStart(new Vector3d(second.x, first.y, first.z)); // Line 4
        lines[3].setEnd(new Vector3d(first.x, first.y, first.z));   // From (max.x, min.y, min.z) -> (min.x, min.y, min.z)

        // Top face (y = max.y)
        lines[4].setStart(new Vector3d(first.x, second.y, first.z)); // Line 5
        lines[4].setEnd(new Vector3d(first.x, second.y, second.z));   // From (min.x, max.y, min.z) -> (min.x, max.y, max.z)

        lines[5].setStart(new Vector3d(first.x, second.y, second.z)); // Line 6
        lines[5].setEnd(new Vector3d(second.x, second.y, second.z));   // From (min.x, max.y, max.z) -> (max.x, max.y, max.z)

        lines[6].setStart(new Vector3d(second.x, second.y, second.z)); // Line 7
        lines[6].setEnd(new Vector3d(second.x, second.y, first.z));   // From (max.x, max.y, max.z) -> (max.x, max.y, min.z)

        lines[7].setStart(new Vector3d(second.x, second.y, first.z)); // Line 8
        lines[7].setEnd(new Vector3d(first.x, second.y, first.z));   // From (max.x, max.y, min.z) -> (min.x, max.y, min.z)

        // Vertical edges (connecting top and bottom faces)
        lines[8].setStart(new Vector3d(first.x, first.y, first.z)); // Line 9
        lines[8].setEnd(new Vector3d(first.x, second.y, first.z));   // From (min.x, min.y, min.z) -> (min.x, max.y, min.z)

        lines[9].setStart(new Vector3d(first.x, first.y, second.z)); // Line 10
        lines[9].setEnd(new Vector3d(first.x, second.y, second.z));   // From (min.x, min.y, max.z) -> (min.x, max.y, max.z)

        lines[10].setStart(new Vector3d(second.x, first.y, second.z)); // Line 11
        lines[10].setEnd(new Vector3d(second.x, second.y, second.z));   // From (max.x, min.y, max.z) -> (max.x, max.y, max.z)

        lines[11].setStart(new Vector3d(second.x, first.y, first.z)); // Line 12
        lines[11].setEnd(new Vector3d(second.x, second.y, first.z));   // From (max.x, min.y, min.z) -> (max.x, max.y, min.z)
    }

    /**
     * Spawns (or re-spawns) each line's display if needed and positions it.
     */
    public void render(World world) {
        updateLines();
        for (Line line : lines) {
            line.render(world);
        }
    }

    /**
     * Dispose of the lines, object can no longer be reused.
     */
    public void dispose() {
        for (Line line : lines) {
            line.dispose();
            this.lines = null;
        }
    }

    /**
     * Update our internal max corner, then re-set line endpoints.
     */
    public void setSecond(Vector3d newMax) {
        this.second.set(newMax);
        updateLines();
    }

    /**
     * Update our internal min corner, then re-set line endpoints.
     */
    public void setFirst(Vector3d newMin) {
        this.first.set(newMin);
        updateLines();
    }
}
