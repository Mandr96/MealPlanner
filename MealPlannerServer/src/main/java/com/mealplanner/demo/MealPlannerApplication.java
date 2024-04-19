package com.mealplanner.demo;

import com.mealplanner.demo.DataAccess.MealRepository;
import com.mealplanner.demo.DataAccess.PlanRepository;
import com.mealplanner.demo.DataAccess.RecipeDAO;
import com.mealplanner.demo.DataAccess.UtentiRepository;
import com.mealplanner.demo.Model.MealPlan;
import com.mealplanner.demo.Model.Utente;
import com.mealplanner.demo.RecipeFuzzyService.RecipeService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;

@SpringBootApplication
public class MealPlannerApplication {
	public static void main(String[] args) {
		SpringApplication.run(MealPlannerApplication.class, args);
	}
		@Bean
		CommandLineRunner commandLineRunner(PlanRepository planRep, MealRepository mealRep, UtentiRepository userRep) {
			return args -> {
				Utente user = new Utente("prova", "1234", "Mario", new Date(), 175, 80F, 'M', 1, new ArrayList<>());
				userRep.save(user);

				MealPlan plan1 = new MealPlan(1750F, Date.from(Instant.now()), 7, "Onnivora", user);
				planRep.save(plan1);
				MealPlan plan2 = new MealPlan(2000F, Date.from(Instant.now()), 14, "Onnivora", user);
				planRep.save(plan2);
				MealPlan plan3 = new MealPlan(2250F, Date.from(Instant.now()), 21, "Onnivora", user);
				new RecipeService(plan3).generateMealPlan(RecipeDAO.getDAO().getLimitedRecipes(), 1);
				planRep.save(plan3);
				MealPlan plan4 = new MealPlan(2000F, Date.from(Instant.now()), 21, "Onnivora", user);
				planRep.save(plan4);
				MealPlan plan5 = new MealPlan(1750F, Date.from(Instant.now()), 21, "Onnivora", user);
				planRep.save(plan5);
				//new PlanController(mealRep, planRep).generateMealPlan(1L);

			};
	}

}
