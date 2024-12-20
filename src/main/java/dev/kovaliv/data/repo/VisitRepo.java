package dev.kovaliv.data.repo;

import dev.kovaliv.data.entity.Visit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface VisitRepo extends JpaRepository<Visit, Long> {
    @Query("SELECT v from Visit v where v.link.name = ?1 and not v.isBot")
    List<Visit> findByLinkNameAndNotBot(String name);

    @Query("SELECT v from Visit v where v.country is null")
    List<Visit> findAllByCountryIsNull();

    @Query("SELECT v from Visit v where v.city is null")
    List<Visit> findAllByCityIsNull();

    @Query("SELECT v from Visit v where v.region is null")
    List<Visit> findAllByRegionIsNull();

    @Query("SELECT v from Visit v where v.source is null")
    List<Visit> findAllBySourceIsNull();

    @Query("SELECT v from Visit v where not v.isBot and v.created between ?1 and ?2")
    List<Visit> findAllByCreatedDateBetweenAndNotBot(LocalDateTime start, LocalDateTime end);
}
