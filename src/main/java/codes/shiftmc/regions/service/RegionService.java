package codes.shiftmc.regions.service;

import codes.shiftmc.regions.math.BlockVector;
import codes.shiftmc.regions.model.Region;
import codes.shiftmc.regions.repository.RegionRepository;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.UUID;

public class RegionService {

    private final RegionRepository regionRepository;
    private final HashMap<UUID, Region> regionCache;

    public RegionService(RegionRepository regionRepository) {
        this.regionRepository = regionRepository;
        this.regionCache = new HashMap<>();
        createTable().subscribe();
    }

    public Mono<Void> createTable() {
        return regionRepository.createTable();
    }

    /**
     * Fetch a region by its UUID, using the region cache.
     */
    public Mono<Region> findByUUID(UUID uuid) {
        Region cached = regionCache.getIfPresent(uuid);
        if (cached != null) {
            return Mono.just(cached);
        }
        return regionRepository.findByUUID(uuid)
                .doOnNext(region -> {
                    if (region != null) {
                        regionCache.put(uuid, region);
                    }
                });
    }

    /**
     * Delete a region by its UUID.
     * the region is not deleted immediately, but invalidated in the cache.
     *
     * @param uuid the UUID of the region to delete
     */
    public void deleteRegion(UUID uuid) {
        regionRepository.deleteRegion(uuid).subscribe();
        regionCache.invalidate(uuid);
    }

    /**
     * Find a region by a specific location (x, y, z).
     * Check cached regions first; if no match, query the database and update the cache.
     */
    public Mono<Region> findByLocation(int x, int y, int z) {
        System.out.println("Cached Regions: " + regionCache.asMap().keySet());
        for (Region region : regionCache.asMap().values()) {
            if (isPointInsideRegion(region, x, y, z)) return Mono.just(region);
        }

        return regionRepository.findByLocation(x, y, z)
                .doOnNext(region -> {
                    if (region != null) regionCache.put(region.id(), region); // Cache the region
                });
    }

    /**
     * Check if a point (x, y, z) is inside the given region.
     */
    private boolean isPointInsideRegion(Region region, int x, int y, int z) {
        return isRegionIntersecting(region, new BlockVector(x, y, z), new BlockVector(x, y, z));
    }

    /**
     * Check if a region collides with a bounding box defined by 'first' and 'second'.
     */
    private boolean isRegionIntersecting(Region region, BlockVector first, BlockVector second) {
        int minX = Math.min(first.x(), second.x());
        int minY = Math.min(first.y(), second.y());
        int minZ = Math.min(first.z(), second.z());
        int maxX = Math.max(first.x(), second.x());
        int maxY = Math.max(first.y(), second.y());
        int maxZ = Math.max(first.z(), second.z());

        BlockVector regionMin = region.min();
        BlockVector regionMax = region.max();

        System.out.printf("Checking region [%s] with bounds (%d,%d,%d) -> (%d,%d,%d) against point (%d,%d,%d)\n",
                region.id(), regionMin.x(), regionMin.y(), regionMin.z(),
                regionMax.x(), regionMax.y(), regionMax.z(),
                first.x(), first.y(), first.z());

        boolean intersects = regionMin.x() <= maxX && regionMax.x() >= minX
                && regionMin.y() <= maxY && regionMax.y() >= minY
                && regionMin.z() <= maxZ && regionMax.z() >= minZ;

        System.out.println("Intersection result: " + intersects);
        return intersects;
    }

    public Mono<Region> findByOwner(UUID ownerUuid) {
        return regionRepository.findByOwner(ownerUuid);
    }

    public Mono<Region> save(Region region) {
        return regionRepository.save(region)
                .doOnNext(saved -> regionCache.put(saved.id(), saved));
    }

    public Mono<Region> updateRegion(Region region) {
        return regionRepository.updateRegion(region)
                .doOnNext(updated -> regionCache.put(updated.id(), updated));
    }

    public void invalidateCacheFor(UUID regionId) {
        regionCache.invalidate(regionId);
    }
}