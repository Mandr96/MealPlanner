package com.mealplanner.demo.DataAccess;

import com.mealplanner.demo.Model.Meal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MealRepository extends JpaRepository<Meal, Long> {

    @Query(value = "SELECT m.* FROM meal m " +
           "WHERE m.plan_id = ?1 AND m.day >= ?2 AND m.day <= ?3 ORDER BY day",
            nativeQuery = true)
    public List<Meal> findMealByDays(Long planID, int fromDay, int toDay);
}
