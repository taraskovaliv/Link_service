package dev.kovaliv.data.repo;

import dev.kovaliv.data.entity.Header;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HeaderRepo extends JpaRepository<Header, Long> {
}
