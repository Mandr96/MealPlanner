package com.mealplanner.demo.Model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Cascade;

import java.util.Date;
import java.util.Set;
import java.util.TreeSet;

@Getter
@Setter
@NoArgsConstructor
@Entity(name = "plan")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class MealPlan {
    @Id
    @SequenceGenerator(
            name = "plan_sequence",
            sequenceName = "plan_sequence",
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "plan_sequence"
    )
    private  Long id;
    private Float dailyKcal;
    private Date startDate;
    private int nDays;
    private String dietType;
    private float[] foodPrefs;
    @JsonIgnore
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "plan")
    private Set<Meal> mealList;
    @ManyToOne
    private Utente owner;

    public MealPlan(Float dailyKcal, Date startDate, int nDays, String dietType, float[] foodPrefs) {
        this.dailyKcal = dailyKcal;
        this.startDate = startDate;
        this.nDays = nDays;
        this.dietType = dietType;
        mealList = new TreeSet<Meal>(Meal.getComparator());
        this.foodPrefs = foodPrefs;
    }

    public MealPlan(Float dailyKcal, Date startDate, int nDays, String dietType, Utente owner) {
        this.dailyKcal = dailyKcal;
        this.startDate = startDate;
        this.nDays = nDays;
        this.dietType = dietType;
        this.owner = owner;
        mealList = new TreeSet<Meal>(Meal.getComparator());
        foodPrefs = setDefaultFoodPrefs();
    }

    private float[] setDefaultFoodPrefs() {
        return new float[] {
                25F,    // Pasta, cereali e derivati
                5F,     // Patate
                17.5F,  // Verdure
                15F,    // Frutta
                12.5F,  // Legumi
                7.5F,   // Pesce e prodotti della pesca
                7.5F,   // Carne bianca
                2.5F,   // Carne rossa e salumi
                2.5F,   // Latte e derivati
                5F      // Dolci e desserts
        };
    }
    public Float getDailyKcal() {
            return dailyKcal;
        }

    public float[] getFoodPrefs() {
            return foodPrefs;
        }

    public void addMeal(Meal meal) {
            mealList.add(meal);
        }

    public int getNDays() {
            return nDays;
        }

    public Set<Meal> getMealList() {
            return mealList;
        }

    public void print() {
        for(Meal m : mealList) {
            m.print();
            System.out.println("\n");
        }
    }
}
