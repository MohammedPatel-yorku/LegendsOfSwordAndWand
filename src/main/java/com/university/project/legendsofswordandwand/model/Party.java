package com.university.project.legendsofswordandwand.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import lombok.*;

@Entity
@Table(name = "parties")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Party {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @Setter
  @JoinColumn(name = "owner_id")
  private User owner;

  @OneToMany(mappedBy = "party", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  private List<Hero> heroes = new ArrayList<>();
}
