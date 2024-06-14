package dev.kovaliv.data.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

import static jakarta.persistence.FetchType.EAGER;
import static jakarta.persistence.GenerationType.SEQUENCE;

@Builder
@Setter
@Getter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "visit")
public class Visit {

    @Id
    @GeneratedValue(strategy = SEQUENCE, generator = "visit_generator")
    @SequenceGenerator(name = "visit_generator", sequenceName = "visit_id_seq", allocationSize = 1)
    @Column(updatable = false, nullable = false, unique = true)
    private Long id;

    private String ip;

    private String platform;

    private String browser;

    private String device;

    private String country;

    private String region;

    private String city;

    private String language;

    private String campaign;

    private String source;

    private boolean mobile;

    @Column(name = "is_bot", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean isBot;

    @ManyToOne(fetch = EAGER)
    @JoinColumn(name = "link_id", nullable = false)
    private Link link;

    @OneToMany(mappedBy = "visit", fetch = EAGER, cascade = {CascadeType.PERSIST, CascadeType.REMOVE})
    private List<Header> headers;

    @CreationTimestamp
    @Column(updatable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime created;
}
