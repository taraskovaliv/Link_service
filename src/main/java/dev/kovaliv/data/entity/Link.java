package dev.kovaliv.data.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

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

    @Column(updatable = false, nullable = false, unique = true)
    private String name;

    @Column(name = "responsible_email", updatable = false, nullable = false)
    private String responsibleEmail;

    @Column(updatable = false, nullable = false)
    private String url;

    @Column(length = 3000)
    private String description;

    @OneToMany(mappedBy = "link", fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.REMOVE})
    private List<Visit> visits;

    @CreationTimestamp
    @Column(updatable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime created;
}
