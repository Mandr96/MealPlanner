package com.mealplanner.demo.DataAccess;

import com.mealplanner.demo.Model.Recipe;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.SneakyThrows;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;

@Converter
public class RecipeListConverter implements AttributeConverter<List<Recipe>, String> {
    private static final String SPLIT_CHAR = ",";
    @Override
    public String convertToDatabaseColumn(List<Recipe> attribute) {
        if(attribute.isEmpty())
            return "";
        List<String> list = attribute.stream().map(recipe -> recipe.getId().toString()).toList();
        return String.join(SPLIT_CHAR, list);
    }

    @SneakyThrows
    @Override
    public List<Recipe> convertToEntityAttribute(String dbData) {
        RecipeDAO dao = RecipeDAO.getDAO();
        List<Recipe> result = new ArrayList<>();
        if(dbData.isBlank())
            return result;
        List<Long> idList = Arrays.stream(dbData.split(SPLIT_CHAR)).map(Long::parseLong).toList();
        for(Long id : idList) {
            result.add(dao.getRecipe(id));
        }
        return result;
    }
}
