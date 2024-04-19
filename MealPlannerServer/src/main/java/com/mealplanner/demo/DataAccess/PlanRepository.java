package com.mealplanner.demo.DataAccess;

import com.mealplanner.demo.Model.MealPlan;
import jakarta.persistence.Transient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlanRepository extends JpaRepository<MealPlan, Long> {

    @Query(value = "SELECT p.id, p.start_date, p.diet_type, p.n_days, p.daily_kcal, p.food_prefs, p.owner_email " +
            "FROM plan p WHERE p.owner_email = ?1",
            nativeQuery = true)
    public List<MealPlan> getUserPlan(String email);
}
