package codes.shiftmc.regions.service;

import codes.shiftmc.regions.math.BlockVector;
import codes.shiftmc.regions.model.Region;
import codes.shiftmc.regions.repository.RegionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

public class RegionService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RegionService.class);

    private final RegionRepository regionRepository;
    private final HashMap<UUID, Region> regionCache;


    public RegionService(RegionRepository regionRepository) {
        this.regionRepository = regionRepository;
        this.regionCache = new HashMap<>();

        createTable().block();

        // Load all regions into the cache
        regionRepository.findAll().all(region -> {
            regionCache.put(region.id(), region);
            return true;
        }).then().doOnTerminate(() -> LOGGER.info("Region cache initialized")).block();
    }

    private Mono<Void> createTable() {
        return regionRepository.createTable();
    }

    public Optional<Region> findByUUID(UUID uuid) {
        return Optional.ofNullable(regionCache.get(uuid));
    }

    public void deleteRegion(UUID uuid) {
        regionRepository.delete(uuid).subscribe();
        regionCache.remove(uuid);
    }

    public Optional<Region> findByLocation(int x, int y, int z) {
        System.out.println("Cached Regions: " + regionCache.keySet());

        for (Region region : regionCache.values()) {
            if (isPointInsideRegion(region, x, y, z)) {
                return Optional.of(region);
            }
        }

        return Optional.empty();
    }

    private boolean isPointInsideRegion(Region region, int x, int y, int z) {
        return isRegionIntersecting(region, new BlockVector(x, y, z), new BlockVector(x, y, z));
    }

    private boolean isRegionIntersecting(Region region, BlockVector first, BlockVector second) {
        int minX = Math.min(first.x(), second.x());
        int minY = Math.min(first.y(), second.y());
        int minZ = Math.min(first.z(), second.z());
        int maxX = Math.max(first.x(), second.x());
        int maxY = Math.max(first.y(), second.y());
        int maxZ = Math.max(first.z(), second.z());

        BlockVector regionMin = region.min();
        BlockVector regionMax = region.max();

        System.out.printf(
                "Checking region [%s] with bounds (%d,%d,%d) -> (%d,%d,%d) against point (%d,%d,%d)\n",
                region.id(), regionMin.x(), regionMin.y(), regionMin.z(),
                regionMax.x(), regionMax.y(), regionMax.z(),
                first.x(), first.y(), first.z()
        );

        boolean intersects = regionMin.x() <= maxX && regionMax.x() >= minX
                && regionMin.y() <= maxY && regionMax.y() >= minY
                && regionMin.z() <= maxZ && regionMax.z() >= minZ;

        System.out.println("Intersection result: " + intersects);
        return intersects;
    }

    public Optional<Region> findByOwner(UUID ownerUuid) {
        return regionCache.values().stream()
                .filter(region -> region.owner().equals(ownerUuid))
                .findFirst();
    }

    public Mono<Region> save(Region region, boolean cacheOnly) {
        if (cacheOnly) {
            regionCache.put(region.id(), region);
            return Mono.just(region);
        }
        return regionRepository.save(region)
                .thenReturn(region)
                .doOnNext(saved -> regionCache.put(saved.id(), saved));
    }

    public Mono<Region> updateRegion(Region region) {
        return regionRepository.delete(region.id())
                .then(regionRepository.save(region))
                .thenReturn(region)
                .doOnNext(updated -> regionCache.put(updated.id(), updated));
    }

    public Mono<Void> saveAllRegions() {
        return regionRepository.saveAll(new ArrayList<>(regionCache.values()));
    }

    public void invalidateCacheFor(UUID regionId) {
        regionCache.remove(regionId);
    }
}