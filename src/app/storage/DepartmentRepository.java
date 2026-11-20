package app.storage;

import app.domain.Department;
import java.util.List;
import java.util.Optional;

public interface DepartmentRepository {
    void save(Department department);
    Optional<Department> findById(String id);
    List<Department> findAll();
    List<Department> searchByNameOrId(String query);
    void deleteById(String id);
}

