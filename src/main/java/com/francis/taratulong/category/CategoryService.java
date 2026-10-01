package com.francis.taratulong.category;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;

    /**
     * Normalizes a category name to lowercase, alphanumeric + hyphens only.
     * Spaces are converted to hyphens, consecutive hyphens are collapsed,
     * and leading/trailing hyphens are trimmed.
     */
    public String normalizeName(String rawName) {
        return rawName.trim()
                .toLowerCase()
                .replaceAll("[^a-z0-9\\-\\s]", "")
                .replaceAll("[\\s]+", "-")
                .replaceAll("-{2,}", "-")
                .replaceAll("^-|-$", "");
    }

    /**
     * Finds or creates a single category by its raw name.
     * The name is normalized before lookup/creation.
     */
    public Category findOrCreate(String rawName) {
        String normalized = normalizeName(rawName);
        log.debug("Finding or creating category: raw='{}', normalized='{}'", rawName, normalized);
        return categoryRepository.findByName(normalized)
                .orElseGet(() -> {
                    Category category = new Category();
                    category.setName(normalized);
                    log.info("Created new category: '{}'", normalized);
                    return categoryRepository.save(category);
                });
    }

    /**
     * Resolves a set of raw category names into Category entities.
     * Existing categories are reused; new ones are auto-created.
     */
    public Set<Category> resolveCategories(Set<String> rawNames) {
        if (rawNames == null || rawNames.isEmpty()) {
            return new HashSet<>();
        }

        Set<String> normalizedNames = rawNames.stream()
                .map(this::normalizeName)
                .filter(name -> !name.isEmpty())
                .collect(Collectors.toSet());

        // Bulk-fetch existing categories
        List<Category> existing = categoryRepository.findByNameIn(normalizedNames);
        Map<String, Category> existingMap = existing.stream()
                .collect(Collectors.toMap(Category::getName, c -> c));

        Set<Category> result = new HashSet<>(existing);

        // Create any missing categories
        for (String name : normalizedNames) {
            if (!existingMap.containsKey(name)) {
                Category newCategory = new Category();
                newCategory.setName(name);
                result.add(categoryRepository.save(newCategory));
                log.info("Created new category: '{}'", name);
            }
        }

        return result;
    }

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }
}
