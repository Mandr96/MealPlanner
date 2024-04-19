package com.mealplanner.demo.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mealplanner.demo.DataAccess.MealRepository;
import com.mealplanner.demo.DataAccess.PlanRepository;
import com.mealplanner.demo.DataAccess.RecipeDAO;
import com.mealplanner.demo.DataAccess.UtentiRepository;
import com.mealplanner.demo.Model.Meal;
import com.mealplanner.demo.Model.MealPlan;
import com.mealplanner.demo.Model.Utente;
import com.mealplanner.demo.RecipeFuzzyService.RecipeService;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(path = "plan")
public class PlanRestController {
    private final MealRepository mealRepo;
    private final PlanRepository planRepo;
    private final UtentiRepository userRepo;

    @Autowired
    public PlanRestController(MealRepository mealRepo, PlanRepository planRepo, UtentiRepository userRepo) {
        this.mealRepo = mealRepo;
        this.planRepo = planRepo;
        this.userRepo = userRepo;
    }

    @PostMapping(path = "/create")
    public MealPlan createPlan(@RequestBody String body) {
        MealPlan plan;
        try {
            plan = new ObjectMapper().readValue(body, MealPlan.class);
        } catch (JsonProcessingException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        JSONObject jsObj = new JSONObject(body);
        Utente user = userRepo.findById(jsObj.getString("user")).get();
        plan.setOwner(user);
        planRepo.save(plan);
        return plan;
    }

    @PostMapping(path = "/generate/all/{id}")
    public void generateMealPlan(@PathVariable("id") Long planID) {
        MealPlan plan = planRepo.findById(planID).get();
        RecipeService recService = new RecipeService(plan);
        try {recService.generateMealPlan(RecipeDAO.getDAO().getValidRecipesForPlan(plan), 1);} catch (SQLException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Recipe database error");
        }
        plan.print();
        planRepo.save(plan);
    }

    @PostMapping(path = "/generate/update/{id}/{start_day}/{n_days}")
    public void generateUpdateMealPlan(@PathVariable("id") Long planID,
                                       @PathVariable("start_day") Integer startDay,
                                       @PathVariable("n_days") Integer nDays) {
        MealPlan plan = planRepo.findById(planID).get();
        plan.setNDays(plan.getNDays()+nDays);
        RecipeService recipeService = new RecipeService(plan);
        try {recipeService.generateMealPlan(RecipeDAO.getDAO().getValidRecipesForPlan(plan), startDay);} catch (SQLException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Recipe database error");
        }
        plan.print();
        planRepo.save(plan);
    }

    @GetMapping(path = "/{id}")
    public MealPlan getPlanById(@PathVariable("id") Long id) {
        Optional<MealPlan> result = planRepo.findById(id);
        if(result.isPresent())
            return result.get();
        throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }

    @GetMapping(path = "/week/{id}/{from_day}")
    public List<Meal> getPlanWeek(@PathVariable("id") Long id,
                                  @PathVariable("from_day") int fromDay) {
        List<Meal> result = mealRepo.findMealByDays(id, fromDay, fromDay+6);
        if(!result.isEmpty())
            return result;
        throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }

    @GetMapping(path = "/user/{email}")
    public List<MealPlan> getUserPlan(@PathVariable("email") String email) {
        return planRepo.getUserPlan(email);
    }
}
