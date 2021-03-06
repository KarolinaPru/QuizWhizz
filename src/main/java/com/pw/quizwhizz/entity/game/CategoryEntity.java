package com.pw.quizwhizz.entity.game;

import com.pw.quizwhizz.annotation.UniqueCategory;
import lombok.Data;
import lombok.NoArgsConstructor;
import javax.persistence.*;

/**
 * Encja Category
 * @author Michał Nowiński
 */
@Entity
@Data
@NoArgsConstructor
@Table(name = "category")
public class CategoryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "category_name", nullable = false)
    @UniqueCategory
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private String urlImage = "";
}
