package dev.kovaliv.data.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

import static jakarta.persistence.GenerationType.SEQUENCE;

@Builder
@Setter
@Getter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "links")
public class Link {

    @Id
    @GeneratedValue(strategy = SEQUENCE, generator = "link_generator")
    @SequenceGenerator(name = "link_generator", sequenceName = "link_id_seq", allocationSize = 1)
    @Column(updatable = false, nullable = false, unique = true)
    private Long id;

    @Column(name = "name", updatable = false, nullable = false)
    private String name;

    @Column(name = "responsible_email", updatable = false, nullable = false)
    private String responsibleEmail;

    @Column(name = "url", updatable = false, nullable = false)
    private String url;

    @Column(name = "description", length = 3000)
    private String description;

    @Column(name = "count_visits", columnDefinition = "BIGINT DEFAULT 0")
    private long countVisits;

    @CreationTimestamp
    @Column(updatable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime created;
}
