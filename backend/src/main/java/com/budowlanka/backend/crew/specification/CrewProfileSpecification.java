package com.budowlanka.backend.crew.specification;

import com.budowlanka.backend.crew.entity.CrewProfile;
import com.budowlanka.backend.crew.enums.Voivodeship;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

public final class CrewProfileSpecification {

  private CrewProfileSpecification() {}

  public static Specification<CrewProfile> isVisible() {
    return (root, query, cb) -> cb.isTrue(root.get("visible"));
  }

  public static Specification<CrewProfile> hasCity(String city) {
    return (root, query, cb) -> cb.equal(cb.lower(root.get("city")), city.toLowerCase());
  }

  public static Specification<CrewProfile> hasVoivodeship(Voivodeship voivodeship) {
    return (root, query, cb) -> cb.equal(root.get("voivodeship"), voivodeship);
  }

  public static Specification<CrewProfile> isNotBlocked() {
    return (root, query, cb) -> cb.isFalse(root.get("blocked"));
  }

  public static Specification<CrewProfile> isBlocked(boolean blocked) {
    return (root, query, cb) -> cb.equal(root.get("blocked"), blocked);
  }

  public static Specification<CrewProfile> hasCategory(UUID categoryId) {
    return (root, query, cb) -> {
      query.distinct(true);
      return cb.equal(root.join("serviceCategories").get("id"), categoryId);
    };
  }
}
