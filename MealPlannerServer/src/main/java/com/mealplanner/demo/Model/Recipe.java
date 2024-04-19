package com.mealplanner.demo.Model;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Id;
import lombok.Data;

import java.util.Arrays;
import java.util.List;

@Embeddable
@Data
public class Recipe {
    Long id;
    String name;
    int minutes;
    String tags;
    int n_steps;
    String steps;
    String description;
    String ingredients;
    int n_ingredients;
    Float kcal;
    int health_pt;
    Integer[] foodVector;

    public Recipe(String name, Long id, int minutes, String tags, int n_steps, String steps, String description, String ingredients, int n_ingredients, Float kcal, int health_pt, Integer[] foodVector) {
        this.name = name;
        this.id = id;
        this.minutes = minutes;
        this.tags = tags;
        this.n_steps = n_steps;
        this.steps = steps;
        this.description = description;
        this.ingredients = ingredients;
        this.n_ingredients = n_ingredients;
        this.kcal = kcal;
        this.health_pt = health_pt;
        this.foodVector = foodVector;
    }

    @Override
    public String toString() {
        return name+" ["+minutes+"min] - "+health_pt+" pt, "+ Arrays.toString(foodVector)+"\n" +
                "   "+description+"\n"+
                "   "+kcal+"kcal - "+tags+"\n"+
                "   "+steps;
    }

    public boolean hasTags(List<String> tags) {
        for(String tag : tags) {
            if(!this.tags.contains(tag)) {
                return false;
            }
        }
        return true;
    }
}


