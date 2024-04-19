package com.mealplanner.demo.RestController;

import com.mealplanner.demo.DataAccess.UtentiRepository;
import com.mealplanner.demo.Model.Utente;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@RestController
@RequestMapping(path = "user")
public class UserRestController {

    private UtentiRepository userRepo;

    @Autowired
    public UserRestController(UtentiRepository rep) {
        userRepo = rep;
    }

    @PostMapping(path = "/signup")
    public void signup(@RequestBody Utente user) {
        if(userRepo.existsById(user.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT);
        }
        userRepo.save(user);
    }

    @PostMapping(path = "/login")
    public Utente login(@RequestBody Utente user) {
        Optional<Utente> retrieved = userRepo.findById(user.getEmail());
        if(retrieved.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        Utente result = retrieved.get();
        if(result.getPassword().equals(user.getPassword())) {
            return result;
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    }

    @PostMapping(path = "/update_info")
    public void update(@RequestBody Utente user) {
        Utente old = userRepo.findById(user.getEmail()).get();
        old.setName(user.getName());
        old.setAltezza(user.getAltezza());
        old.setBirthdate(user.getBirthdate());
        old.setPeso(user.getPeso());
        old.setSex(user.getSex());
        old.setLv_attivita_fisica(user.getLv_attivita_fisica());
        userRepo.save(old);
    }
}
