package com.mealplanner.demo.Model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.mealplanner.demo.DataAccess.RecipeListConverter;
import com.mealplanner.demo.Model.MealType;
import com.mealplanner.demo.Model.Recipe;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CollectionId;
import org.hibernate.annotations.CollectionType;
import org.hibernate.annotations.Type;
import org.hibernate.type.ListType;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Entity(name = "meal")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class Meal {

    @Id
    @SequenceGenerator(
            name = "meal_sequence",
            sequenceName = "meal_sequence",
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "meal_sequence"
    )
    private Long id;
    private Integer day;
    private MealType type;
    @Convert(converter = RecipeListConverter.class)
    private List<Recipe> portate;
    @ManyToOne
    @JsonIgnore
    private MealPlan plan;

    public Meal(Integer day, MealType type, MealPlan plan) {
        this.day = day;
        this.type = type;
        this.plan = plan;
        portate = new ArrayList<>();
    }

    public void addRecipe(Recipe recipe) {
        portate.add(recipe);
    }

    public static Comparator<Meal> getComparator() {
        return (m1, m2) -> {
            int result = m1.getDay().compareTo(m2.getDay());
            if (result == 0) {
                return m1.getType().ordinal() - m2.getType().ordinal();
            }
            return result;
        };
    }

    public void print() {
        System.out.println(toString());
        for (Recipe rec : portate) {
            System.out.println("- " + rec);
        }
    }

    @Override
    public String toString() {
        return "DAY " + day + ": " + type.toString();
    }

    public Float getTotalKcal() {
        Float sum = 0F;
        for(Recipe rec : portate) {
            sum += rec.getKcal();
        }
        return sum;
    }
}
