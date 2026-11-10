package com.stockmanager.stock_manager.service;

import com.stockmanager.stock_manager.entity.Category;
import com.stockmanager.stock_manager.repository.CategoryRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class CategoryService {

	private final CategoryRepository categoryRepository;

	public CategoryService(CategoryRepository categoryRepository) {
	    this.categoryRepository = categoryRepository;
	}

    public List<Category> findAll() {
        return categoryRepository.findAll();
    }

    public Category findById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Catégorie introuvable avec l'id : " + id));
    }

    public List<Category> search(String name) {
        return categoryRepository.findByNameContainingIgnoreCase(name);
    }

    @Transactional
    public Category create(Category category) {
        if (categoryRepository.existsByNameIgnoreCase(category.getName())) {
            throw new IllegalArgumentException("Une catégorie avec ce nom existe déjà.");
        }
        return categoryRepository.save(category);
    }

    @Transactional
    public Category update(Long id, Category updated) {
        Category existing = findById(id);
        existing.setName(updated.getName());
        existing.setDescription(updated.getDescription());
        return categoryRepository.save(existing);
    }

    @Transactional
    public void delete(Long id) {
        Category category = findById(id);
        if (category.getProducts() != null && !category.getProducts().isEmpty()) {
            throw new IllegalStateException("Impossible de supprimer une catégorie qui contient des produits.");
        }
        categoryRepository.deleteById(id);
    }
}