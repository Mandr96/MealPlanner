package com.mealplanner.demo.RestController;

import com.mealplanner.demo.DataAccess.MealRepository;
import com.mealplanner.demo.DataAccess.PlanRepository;
import com.mealplanner.demo.DataAccess.RecipeDAO;
import com.mealplanner.demo.DataAccess.UtentiRepository;
import com.mealplanner.demo.Model.Meal;
import com.mealplanner.demo.Model.MealPlan;
import com.mealplanner.demo.Model.MealType;
import com.mealplanner.demo.Model.Recipe;
import com.mealplanner.demo.RecipeFuzzyService.RecipeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(path = "meal")
public class MealRestController {
    private final MealRepository mealRepo;
    private final PlanRepository planRepo;

    @Autowired
    public MealRestController(MealRepository mealRepo, PlanRepository planRepo) {
        this.mealRepo = mealRepo;
        this.planRepo = planRepo;
    }

    @PostMapping(path = "/replace/{id}")
    public Meal generateReplacingMeal(@PathVariable("id") Long mealID) {
        Optional<Meal> result = mealRepo.findById(mealID);
        if(result.isPresent()) {
            Meal meal = result.get();
            MealPlan plan = meal.getPlan();

            List<Recipe> recipes = null;
            try {recipes = RecipeDAO.getDAO().getValidRecipesForPlan(plan);} catch (SQLException e) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
            }
            int day = meal.getDay();
            MealType type = meal.getType();
            Float kcal = meal.getTotalKcal();

            mealRepo.delete(meal);
            RecipeService recService = new RecipeService(plan);
            Meal newMeal = recService.generateMeal(recipes, day, type, kcal);
            mealRepo.save(newMeal);

            return newMeal;
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }

    @PostMapping(path = "/replace/recipe/{meal_id}/{recipe_id}")
    public Recipe generateReplacingRecipe(@PathVariable("meal_id") Long mealID,
                                          @PathVariable("recipe_id") Long recipeID) {
        Optional<Meal> result = mealRepo.findById(mealID);
        if(result.isPresent()) {
            Meal meal = result.get();
            Recipe rec;
            try {
                rec = RecipeDAO.getDAO().getRecipe(recipeID);
                Float kcal = rec.getKcal();
                RecipeService recService = new RecipeService(meal.getPlan());

                Recipe newRecipe;
                if(rec.hasTags(RecipeService.sideDishTags))
                    newRecipe = recService.generateRecipeWithTags(RecipeDAO.getDAO().getValidRecipesForPlan(meal.getPlan()), kcal, RecipeService.sideDishTags);
                else if (rec.hasTags(RecipeService.mainDishTags))
                    newRecipe = recService.generateRecipeWithTags(RecipeDAO.getDAO().getValidRecipesForPlan(meal.getPlan()), kcal, RecipeService.mainDishTags);
                else
                    newRecipe = recService.generateRecipeWithTags(RecipeDAO.getDAO().getValidRecipesForPlan(meal.getPlan()), kcal, RecipeService.breakfastTags);
                meal.getPortate().remove(rec);
                meal.addRecipe(newRecipe);
                mealRepo.save(meal);
                return newRecipe;
            } catch (SQLException e) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }
}
