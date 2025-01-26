package codes.shiftmc.regions.repository;

import codes.shiftmc.regions.model.Region;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface RegionRepository {

    Mono<Region> findByUUID(UUID uuid);
    Mono<Region> findByOwner(UUID uuid);
    Mono<Region> save(Region region);
    Mono<Region> updateRegion(Region region);

}
