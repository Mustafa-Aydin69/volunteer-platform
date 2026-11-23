package com.imece.config;

import com.imece.entity.Category;
import com.imece.repository.CategoryRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

// Fills the initial categories on startup. The count check makes it safe to run on
// every start: once categories exist (seeded or added by the admin), nothing is touched.
@Component
public class CategorySeeder implements CommandLineRunner {

    private static final List<String> DEFAULT_CATEGORIES = List.of(
            "Çevre",
            "Eğitim",
            "Sağlık",
            "Hayvanlar",
            "Sosyal Yardım",
            "Kültür-Sanat",
            "Afet",
            "Yaşlı ve Engelli Desteği"
    );

    private final CategoryRepository categoryRepository;

    public CategorySeeder(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public void run(String... args) {
        if (categoryRepository.count() > 0) {
            return;
        }
        categoryRepository.saveAll(DEFAULT_CATEGORIES.stream().map(Category::new).toList());
    }
}
