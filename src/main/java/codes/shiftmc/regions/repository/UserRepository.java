package codes.shiftmc.regions.repository;

import codes.shiftmc.regions.model.UserData;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface UserRepository {

    Mono<UserData> findByUUID(UUID uuid);
    Mono<UserData> save(UserData region);
    Mono<UserData> updateUser(UserData region);

}
