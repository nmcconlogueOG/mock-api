package net.mcfarb.mockapi.service;

import net.mcfarb.mockapi.model.User;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class UserService {

	private final ConcurrentHashMap<Long, User> userStore = new ConcurrentHashMap<>();
	private final AtomicLong idGenerator = new AtomicLong(1);

	public UserService() {
		userStore.put(1L, new User(1L, "Alice", "alice@example.com"));
		userStore.put(2L, new User(2L, "Bob", "bob@example.com"));
		idGenerator.set(3L);
	}

	public Mono<User> getUserById(Long id) {
		return Mono.justOrEmpty(userStore.get(id));
	}

	public Flux<User> getAllUsers() {
		return Flux.fromIterable(new ArrayList<>(userStore.values()));
	}

	public Mono<User> createUser(User user) {
		if (user.getId() == null) {
			user.setId(idGenerator.getAndIncrement());
		}
		userStore.put(user.getId(), user);
		return Mono.just(user);
	}

	public Mono<User> updateUser(Long id, User user) {
		return Mono.justOrEmpty(userStore.get(id))
				.flatMap(existingUser -> {
					user.setId(id);
					userStore.put(id, user);
					return Mono.just(user);
				});
	}

	public Mono<Void> deleteUser(Long id) {
		return Mono.justOrEmpty(userStore.remove(id))
				.then();
	}
}
