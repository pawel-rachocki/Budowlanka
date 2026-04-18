package com.budowlanka.backend.crew.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.budowlanka.backend.crew.dto.ServiceCategoryResponse;
import com.budowlanka.backend.crew.entity.ServiceCategory;
import com.budowlanka.backend.crew.repository.ServiceCategoryRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ServiceCategoryServiceTest {

  @Mock private ServiceCategoryRepository serviceCategoryRepository;

  @InjectMocks private ServiceCategoryService serviceCategoryService;

  @Test
  void should_returnAllCategoriesMappedToResponse_when_called() {
    ServiceCategory cat1 = ServiceCategory.builder().name("Malowanie").slug("malowanie").build();
    ServiceCategory cat2 = ServiceCategory.builder().name("Tynkowanie").slug("tynkowanie").build();
    when(serviceCategoryRepository.findAllByOrderByNameAsc()).thenReturn(List.of(cat1, cat2));

    List<ServiceCategoryResponse> result = serviceCategoryService.getAll();

    assertThat(result).hasSize(2);
    assertThat(result.get(0).name()).isEqualTo("Malowanie");
    assertThat(result.get(0).slug()).isEqualTo("malowanie");
    assertThat(result.get(1).name()).isEqualTo("Tynkowanie");
  }

  @Test
  void should_returnEmptyList_when_noCategories() {
    when(serviceCategoryRepository.findAllByOrderByNameAsc()).thenReturn(List.of());

    List<ServiceCategoryResponse> result = serviceCategoryService.getAll();

    assertThat(result).isEmpty();
  }
}
