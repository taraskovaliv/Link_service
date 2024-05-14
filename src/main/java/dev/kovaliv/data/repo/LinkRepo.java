package dev.kovaliv.data.repo;

import dev.kovaliv.data.dto.StatisticDto;
import dev.kovaliv.data.entity.Link;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LinkRepo extends JpaRepository<Link, Long> {
    Optional<Link> findByName(String name);

    @Query("SELECT new dev.kovaliv.data.dto.StatisticDto(l.name, l.description, COUNT(v)) " +
            "FROM Link l inner JOIN l.visits v GROUP BY l.name, l.description " +
            "ORDER BY COUNT(v) DESC")
    List<StatisticDto> getStatisticByEmail(String email);
}
