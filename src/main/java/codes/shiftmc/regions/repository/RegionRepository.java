package codes.shiftmc.regions.repository;

import codes.shiftmc.regions.math.BlockVector;
import codes.shiftmc.regions.model.Region;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

public interface RegionRepository {

    Mono<Void> createTable();
    Flux<Region> findAll();
    Mono<Void> save(Region region);
    Mono<Void> delete(UUID uuid);
    Mono<Void> saveAll(List<Region> regions);
}
