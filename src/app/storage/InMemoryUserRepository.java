package app.storage;

import app.domain.User;
import java.util.*;

public class InMemoryUserRepository implements UserRepository {
    private final Map<String, User> users = new LinkedHashMap<>();

    @Override
    public Optional<User> findById(String id) {
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public List<User> findAll() {
        return new ArrayList<>(users.values());
    }

    @Override
    public List<User> searchByNameOrId(String query) {
        String q = query.toLowerCase().trim();
        return users.values().stream()
                .filter(u -> u.getId().toLowerCase().contains(q) || u.getName().toLowerCase().contains(q))
                .toList();
    }

    @Override
    public void save(User user) {
        users.put(user.getId(), user);
    }

    @Override
    public void deleteById(String id) {
        users.remove(id);
    }
}

