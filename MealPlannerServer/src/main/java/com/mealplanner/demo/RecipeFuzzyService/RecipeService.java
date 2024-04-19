package com.mealplanner.demo.RecipeFuzzyService;

import com.mealplanner.demo.Model.Meal;
import com.mealplanner.demo.Model.MealPlan;
import com.mealplanner.demo.Model.MealType;
import com.mealplanner.demo.Model.Recipe;
import nrc.fuzzy.*;

import java.util.*;
import java.util.stream.Stream;

public class RecipeService {
    private final MealPlan currentPlan;
    private Integer[] currentFoodVector;

    public static final List<String>
            breakfastTags = List.of("breakfast"),
            mainDishTags = List.of("main-dish"),
            sideDishTags = List.of("side-dish");

    public RecipeService(MealPlan plan) {
        currentPlan = plan;
        currentFoodVector = new Integer[plan.getFoodPrefs().length];
        currentFoodVector = getCurrentFoodVector();
    }

    public void generateMealPlan(List<Recipe> recipeList, Integer startDay) {
        currentPlan.getMealList().removeIf(meal -> {
            return meal.getDay() >= startDay;
        });
        Float kcalDebt = 0F;
        Float targetKcal;
        Meal currentMeal;
        for(int thisDay = startDay; thisDay < currentPlan.getNDays()+1; thisDay++) {
            targetKcal = currentPlan.getDailyKcal()*0.20F;
            currentMeal = generateMeal(recipeList, thisDay, MealType.COLAZIONE, targetKcal-kcalDebt);
            currentPlan.addMeal(currentMeal);
            kcalDebt += currentMeal.getTotalKcal() - targetKcal;

            targetKcal = currentPlan.getDailyKcal()*0.40F;
            currentMeal = generateMeal(recipeList, thisDay, MealType.PRANZO, targetKcal-kcalDebt);
            currentPlan.addMeal(currentMeal);
            kcalDebt += currentMeal.getTotalKcal() - targetKcal;

            targetKcal = currentPlan.getDailyKcal()*0.35F;
            currentMeal = generateMeal(recipeList, thisDay, MealType.CENA, targetKcal-kcalDebt);
            currentPlan.addMeal(currentMeal);
            kcalDebt += currentMeal.getTotalKcal() - targetKcal;
        }
        System.out.println("Kcal debt: "+kcalDebt);
    }

    public Meal generateMeal(List<Recipe> recipeList, Integer day, MealType type, Float targetKcal) {
        Meal meal = new Meal(day, type, currentPlan);
        if(type == MealType.COLAZIONE) {
            meal.addRecipe(generateRecipeWithTags(recipeList, targetKcal, List.of("breakfast")));
        }
        else {
            meal.addRecipe(generateRecipeWithTags(recipeList, targetKcal * 0.70F, List.of("main-dish")));
            meal.addRecipe(generateRecipeWithTags(recipeList, targetKcal * 0.30F, List.of("side-dish")));
        }
        return meal;
    }

    public Recipe generateRecipe(List<Recipe> recipeList, float targetKcal) {
        return generateRecipeWithTags(recipeList, targetKcal, new ArrayList<String>());
    }

    public Recipe generateRecipeWithTags(List<Recipe> recipeList, float targetKcal, List<String> tags) {
        RecipeFuzzySystem recipeEvaluator = RecipeFuzzySystem.getFuzzySystem();
        float kcalDiff;
        float foodVectDistance;
        double score;
        Map<Recipe, Double> result = new HashMap<>();

        for(Recipe rec : recipeList) {
            if(!rec.hasTags(tags))
                continue;
            try {
                kcalDiff = rec.getKcal()-targetKcal;
                Integer[] sum = sumFoodVector(currentFoodVector, rec.getFoodVector());
                float[] comp = getVectorComposition(sum);
                foodVectDistance = calculateCompositionDistance(
                        currentPlan.getFoodPrefs(),
                        comp
                );
                /*
                System.out.println("Differenza calorie: "+kcalDiff+"\n"+
                        "Health pt: "+rec.getHealth_pt()+"\n"+
                        "Vector Distance: "+foodVectDistance);
                */
                score = recipeEvaluator.eval(kcalDiff, rec.getHealth_pt(), foodVectDistance, rec.getMinutes());
                //System.out.println("Score: "+score+"\n");

            } catch (XValuesOutOfOrderException |
                     XValueOutsideUODException |
                     IncompatibleRuleInputsException |
                     InvalidDefuzzifyException | IncompatibleFuzzyValuesException e) {
                throw new RuntimeException("Errore input: "+rec.getMinutes());
            }
            result.put(rec, score);
        }

        Map<Recipe, Double> sortedResult = sortMapByValue(result);
        System.out.println(result.size()+" recipes found");

        int size = 21, i = 0;
        List<Recipe> bestResults = new ArrayList<>();
        for(Recipe rec : sortedResult.keySet()) {
            bestResults.add(rec);
            if(i >= size)
                break;
            i++;
        }

        Collections.shuffle(bestResults);
        Recipe rec = bestResults.get(0);
        currentFoodVector = sumFoodVector(currentFoodVector, rec.getFoodVector());
        return rec;
    }


    private Integer[] sumFoodVector(Integer[] v1, Integer[] v2) {
        if(v1.length != v2.length)
            throw new IllegalArgumentException("Different size vector");
        Integer[] result = new Integer[v1.length];
        for(int i = 0; i < v1.length; i++) {
            result[i] = v1[i]+v2[i];
        }
        return result;
    }

    private float[] getVectorComposition(Integer[] vec) {
        float[] result = new float[vec.length];
        int tot = Stream.of(vec).reduce(0, Integer::sum);
        if(tot > 0) {
            for (int i = 0; i < vec.length; i++) {
                result[i] = ((float) vec[i] / tot * 100);
            }
        }
        return result;
    }

    public float[] getCurrentPlanComposition() {
        return getVectorComposition(currentFoodVector);
    }

    public float getDistanceFromFoodPrefs() {
        return calculateCompositionDistance(currentPlan.getFoodPrefs(), getCurrentPlanComposition());
    }

    private float calculateCompositionDistance(float[] c1, float[] c2) {
        if(c1.length != c2.length)
            throw new IllegalArgumentException("Different size vector");
        float dist = 0;
        for(int i = 0; i < c1.length; i++) {
            dist += Math.abs(c1[i] - c2[i]);
        }
        return dist;
    }

    public static <K, V extends Comparable<V> > Map<K, V>
    sortMapByValue(final Map<K, V> map)
    {
        Comparator<K> valueComparator = new Comparator<K>() {
            public int compare(K k1, K k2)
            {
                return map.get(k1).compareTo(
                        map.get(k2));
            }
        };

        Map<K, V> sorted = new TreeMap<K, V>(valueComparator.reversed());
        sorted.putAll(map);
        return sorted;
    }

    public Integer[] getCurrentFoodVector() {
        Arrays.fill(currentFoodVector, 0);
        for(Meal m : currentPlan.getMealList()) {
            for(Recipe rec : m.getPortate()) {
                currentFoodVector = sumFoodVector(currentFoodVector, rec.getFoodVector());
            }
        }
        return currentFoodVector;
    }
}
