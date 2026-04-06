package com.budowlanka.backend.crew.entity;

import jakarta.persistence.*;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name = "service_categories")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "slug")
public class ServiceCategory {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Setter(AccessLevel.NONE)
  private UUID id;

  @Column(nullable = false, unique = true, length = 100)
  private String name;

  @Column(nullable = false, unique = true, length = 100)
  private String slug;
}
