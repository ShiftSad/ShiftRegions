package codes.shiftmc.regions.service;

import codes.shiftmc.regions.model.UserData;
import codes.shiftmc.regions.repository.UserRepository;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import reactor.core.publisher.Mono;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class UserService {

    private final UserRepository userRepository;
    private final Cache<UUID, UserData> userCache;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
        // Configure your cache as needed: size limit, expiration, etc.
        this.userCache = Caffeine.newBuilder()
                .maximumSize(10_000)
                .expireAfterAccess(2, TimeUnit.HOURS)
                .build();

        createTable().subscribe();
    }

    public Mono<Void> createTable() {
        // You can optionally run repository table creation from the service
        return userRepository.createTable();
    }

    /**
     * Tries to get the user from cache; if missing, fetch from DB and cache the result.
     */
    public Mono<UserData> findByUUID(UUID uuid) {
        UserData cachedUser = userCache.getIfPresent(uuid);
        if (cachedUser != null) {
            return Mono.just(cachedUser);
        }

        return userRepository.findByUUID(uuid)
                .doOnNext(user -> {
                    if (user != null) {
                        userCache.put(uuid, user);
                    }
                });
    }

    /**
     * Save user in the DB, then cache it.
     */
    public Mono<UserData> save(UserData user) {
        return userRepository.save(user)
                .doOnNext(savedUser -> userCache.put(savedUser.id(), savedUser));
    }

    /**
     * Update user in the DB, then update the cache.
     */
    public Mono<UserData> updateUser(UserData user) {
        return userRepository.updateUser(user)
                .doOnNext(updatedUser -> userCache.put(updatedUser.id(), updatedUser));
    }

    /**
     * Optional method to clear the cache if needed.
     */
    public void invalidateCacheFor(UUID userId) {
        userCache.invalidate(userId);
    }
}