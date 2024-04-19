package com.mealplanner.demo.DataAccess;

import com.mealplanner.demo.Model.Utente;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UtentiRepository extends JpaRepository<Utente, String> {
}
