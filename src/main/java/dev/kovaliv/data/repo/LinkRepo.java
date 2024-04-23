package dev.kovaliv.data.repo;

import dev.kovaliv.data.entity.Link;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LinkRepo extends JpaRepository<Link, Long>{
    Optional<Link> findByName(String name);
}
