package com.francis.taratulong.category;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    @Nested
    @DisplayName("normalizeName")
    class NormalizeName {

        @Test
        @DisplayName("should convert to lowercase, replace spaces with hyphens, and remove special characters")
        void shouldNormalizeProperly() {
            assertEquals("flood-relief", categoryService.normalizeName(" Flood Relief "));
            assertEquals("covid-19-aid", categoryService.normalizeName("COVID-19 AID!"));
            assertEquals("medical-mission", categoryService.normalizeName("Medical---Mission"));
            assertEquals("a-b", categoryService.normalizeName("-A B-"));
        }
    }

    @Nested
    @DisplayName("findOrCreate")
    class FindOrCreate {

        @Test
        @DisplayName("should return existing category if found")
        void shouldReturnExisting() {
            Category existing = new Category();
            existing.setId(1L);
            existing.setName("food-drive");

            when(categoryRepository.findByName("food-drive")).thenReturn(Optional.of(existing));

            Category result = categoryService.findOrCreate("Food Drive");

            assertEquals(existing, result);
            verify(categoryRepository, never()).save(any());
        }

        @Test
        @DisplayName("should create and return new category if not found")
        void shouldCreateNew() {
            when(categoryRepository.findByName("food-drive")).thenReturn(Optional.empty());
            when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> {
                Category cat = invocation.getArgument(0);
                cat.setId(2L);
                return cat;
            });

            Category result = categoryService.findOrCreate("Food Drive");

            assertNotNull(result);
            assertEquals("food-drive", result.getName());
            assertEquals(2L, result.getId());
            verify(categoryRepository, times(1)).save(any(Category.class));
        }
    }

    @Nested
    @DisplayName("resolveCategories")
    class ResolveCategories {

        @Test
        @DisplayName("should return empty set when input is null or empty")
        void shouldReturnEmpty() {
            assertTrue(categoryService.resolveCategories(null).isEmpty());
            assertTrue(categoryService.resolveCategories(Set.of()).isEmpty());
        }

        @Test
        @DisplayName("should resolve existing and create missing categories")
        void shouldResolveAndCreate() {
            Category existing = new Category();
            existing.setId(1L);
            existing.setName("existing-tag");

            when(categoryRepository.findByNameIn(Set.of("existing-tag", "new-tag")))
                    .thenReturn(List.of(existing));

            when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> {
                Category cat = invocation.getArgument(0);
                cat.setId(2L);
                return cat;
            });

            Set<Category> result = categoryService.resolveCategories(Set.of("Existing Tag", "New Tag"));

            assertEquals(2, result.size());
            assertTrue(result.stream().anyMatch(c -> c.getName().equals("existing-tag")));
            assertTrue(result.stream().anyMatch(c -> c.getName().equals("new-tag")));

            verify(categoryRepository, times(1)).save(argThat(cat -> cat.getName().equals("new-tag")));
        }
    }
}
