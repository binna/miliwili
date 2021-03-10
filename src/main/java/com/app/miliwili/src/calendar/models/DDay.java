package com.app.miliwili.src.calendar.models;

import lombok.*;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@EqualsAndHashCode(callSuper = false)
@Data
@Entity
@Table(name = "dDay")
public class DDay {
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "distinction", nullable = false, length = 10)
    private String distinction;

    @Column(name = "title", nullable = false, length = 30)
    private String title;

    @Column(name = "subtitle", nullable = false, length = 60)
    private String subtitle;

    @Column(name = "startDate", nullable = false)
    private LocalDate startDate;

    @Column(name = "endDate", nullable = false)
    private LocalDate endDate;

    @Column(name = "link", nullable = false, length = 1)
    @Builder.Default
    private String link = "N";

    @Column(name = "choiceCalendar", nullable = false, length = 1)
    @Builder.Default
    private String choiceCalendar = "N";

    @Column(name = "placeLat", precision = 16, scale = 14)
    private BigDecimal placeLat;

    @Column(name = "placeLon", precision = 17, scale = 14)
    private BigDecimal placeLon;

    @OneToMany(mappedBy = "dDay", orphanRemoval = true, cascade = CascadeType.ALL)
    private List<PreparationMaterial> preparationMaterials;
}