package com.mealplanner.demo.DataAccess;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mealplanner.demo.Model.MealPlan;
import com.mealplanner.demo.Model.Recipe;

public class RecipeDAO {
    private Connection conn;
    private static RecipeDAO dao;
    private final int CACHE_SIZE = 25000;
    private LoadingCache<Long, Recipe> recipeCache;
    private RecipeDAO() throws SQLException {
        String url = "jdbc:postgresql://localhost:5432/recipes_db";
        Properties props = new Properties();
        props.setProperty("user", "postgres");
        props.setProperty("password", "progettoDB");
        conn = DriverManager.getConnection(url, props);

        recipeCache = CacheBuilder.newBuilder().maximumSize(CACHE_SIZE).build(new CacheLoader<Long, Recipe>() {
            @Override
            public Recipe load(Long id) throws Exception {
                return findRecipeById(id);
            }
        });
    }

    public static RecipeDAO getDAO() throws SQLException {
        if(dao == null)
            dao = new RecipeDAO();
        return dao;
    }

    private List<Recipe> getVegetarianRecipes() throws SQLException {
        List<Recipe> recipeList = getLimitedRecipes();
        recipeList.removeIf(rec -> !rec.hasTags(List.of("vegetarian")));
        return recipeList;
    }

    private List<Recipe> getVenganRecipes() throws SQLException {
        List<Recipe> recipeList = getLimitedRecipes();
        recipeList.removeIf(rec -> !rec.hasTags(List.of("vegan")));
        return recipeList;
    }

    public Recipe getRecipe(Long recipeID) throws SQLException {
        if(recipeCache.asMap().isEmpty()){
            initCache();
        }
        try {
            return recipeCache.get(recipeID);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public Recipe findRecipeById(Long recipeID) throws SQLException {
        PreparedStatement query = conn.prepareStatement("SELECT * FROM recipes WHERE id = ?");
        query.setLong(1, recipeID);
        ResultSet result = query.executeQuery();
        if(result.next()) {
            return new Recipe(
                    result.getString(1),
                    result.getLong(2),
                    result.getInt(3),
                    result.getString(4),
                    result.getInt(5),
                    result.getString(6),
                    result.getString(7),
                    result.getString(8),
                    result.getInt(9),
                    result.getFloat(10),
                    result.getInt(11),
                    (Integer[]) result.getArray(12).getArray()
            );
        }
        return null;
    }

    public List<Recipe> getAllRecipes() throws SQLException {
        ArrayList<Recipe> recipeList = new ArrayList<>();
        PreparedStatement query = conn.prepareStatement("SELECT * FROM recipes");
        ResultSet result = query.executeQuery();
        while(result.next()) {
            recipeList.add(new Recipe(
                    result.getString(1),
                    result.getLong(2),
                    result.getInt(3),
                    result.getString(4),
                    result.getInt(5),
                    result.getString(6),
                    result.getString(7),
                    result.getString(8),
                    result.getInt(9),
                    result.getFloat(10),
                    result.getInt(11),
                    (Integer[]) result.getArray(12).getArray()
            ));
        }
        return recipeList;
    }

    public List<Recipe> getLimitedRecipes() throws SQLException {
        ArrayList<Recipe> recipeList = new ArrayList<>();
        String queryString = "SELECT * FROM recipes WHERE healthiness_pt >= 10 AND (SELECT SUM(s) FROM UNNEST(food_groups) s) > 0 AND minutes < 1000 ORDER BY healthiness_pt DESC";
        PreparedStatement query = conn.prepareStatement(queryString);
        ResultSet result = query.executeQuery();
        while(result.next()) {
            recipeList.add(new Recipe(
                    result.getString(1),
                    result.getLong(2),
                    result.getInt(3),
                    result.getString(4),
                    result.getInt(5),
                    result.getString(6),
                    result.getString(7),
                    result.getString(8),
                    result.getInt(9),
                    result.getFloat(10),
                    result.getInt(11),
                    (Integer[]) result.getArray(12).getArray()
            ));
        }
        return recipeList;
    }

    public List<Recipe> getValidRecipesForPlan(MealPlan plan) throws SQLException {
        RecipeDAO dao = RecipeDAO.getDAO();
        if(plan.getDietType().toLowerCase().equals("vegetarian"))
            return getVegetarianRecipes();
        if(plan.getDietType().toLowerCase().equals("vegan"))
            return getVenganRecipes();
        return getLimitedRecipes();
    }

    private void initCache() throws SQLException {
        List<Recipe> dbRecipes, cacheRecipes;
        dbRecipes = getLimitedRecipes();
        if(CACHE_SIZE < dbRecipes.size())
            cacheRecipes = getLimitedRecipes().subList(0, CACHE_SIZE-1);
        else
            cacheRecipes = dbRecipes;
        for(Recipe rec : cacheRecipes) {
            recipeCache.put(rec.getId(), rec);
        }
    }
}


