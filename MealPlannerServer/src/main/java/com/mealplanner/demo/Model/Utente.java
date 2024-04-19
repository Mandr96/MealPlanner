package com.mealplanner.demo.Model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "utente")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "email")
public class Utente {
    @Id
    String email;
    String password;
    String name;
    Date birthdate;
    Integer altezza;
    Float peso;
    char sex;
    int lv_attivita_fisica;
    @JsonIgnore
    @OneToMany(mappedBy = "owner")
    List<MealPlan> mealPlans;
}
