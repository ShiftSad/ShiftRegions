package codes.shiftmc.regions.service;

import codes.shiftmc.regions.model.Region;
import codes.shiftmc.regions.repository.RegionRepository;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import reactor.core.publisher.Mono;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class RegionService {

    private final RegionRepository regionRepository;
    private final Cache<UUID, Region> regionCache;

    public RegionService(RegionRepository regionRepository) {
        this.regionRepository = regionRepository;
        this.regionCache = Caffeine.newBuilder()
                .maximumSize(10_000)
                .expireAfterAccess(10, TimeUnit.MINUTES)
                .build();

        createTable().subscribe();
    }

    public Mono<Void> createTable() {
        return regionRepository.createTable();
    }

    /**
     * Cache region by its UUID
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
     * You could also cache by owner if you have a heavy usage scenario
     */
    public Mono<Region> findByOwner(UUID ownerUuid) {
        // For owner, we either do a separate cache or skip caching here.
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