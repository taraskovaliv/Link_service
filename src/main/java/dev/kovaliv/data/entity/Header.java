package dev.kovaliv.data.entity;

import jakarta.persistence.*;
import lombok.*;

import static jakarta.persistence.GenerationType.SEQUENCE;

@Builder
@Setter
@Getter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "header")
public class Header {

    @Id
    @GeneratedValue(strategy = SEQUENCE, generator = "header_generator")
    @SequenceGenerator(name = "header_generator", sequenceName = "header_id_seq", allocationSize = 1)
    @Column(updatable = false, nullable = false, unique = true)
    private Long id;

    private String name;

    @Column(length = 1000, columnDefinition = "VARCHAR(1000)")
    private String value;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "visit_id", nullable = false)
    private Visit visit;
}
