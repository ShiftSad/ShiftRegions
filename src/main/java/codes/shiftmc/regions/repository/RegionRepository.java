package codes.shiftmc.regions.repository;

import codes.shiftmc.regions.math.BlockVector;
import codes.shiftmc.regions.model.Region;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface RegionRepository {

    Mono<Void> createTable();
    Mono<Region> findByUUID(UUID uuid);
    Mono<Region> findByOwner(UUID uuid);
    Mono<Region> save(Region region);
    Mono<Region> updateRegion(Region region);
    Mono<Void> deleteRegion(UUID uuid);

    /**
     * Returns the first region that includes location (x, y, z),
     * or null if no region contains that location.
     */
    Mono<Region> findByLocation(int x, int y, int z);

    /**
     * Checks if any region's bounding box intersects with the
     * axis-aligned bounding box defined by two corners, 'first' and 'second'.
     */
    Mono<Boolean> checkCollision(BlockVector first, BlockVector second);
}
