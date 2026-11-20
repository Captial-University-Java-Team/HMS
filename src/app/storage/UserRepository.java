package app.storage;

import app.domain.User;
import java.util.List;
import java.util.Optional;

public interface UserRepository {
    Optional<User> findById(String id);
    List<User> findAll();
    List<User> searchByNameOrId(String query);
    void save(User user);
    void deleteById(String id);
}

