package org.gymcrm.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true)
public class Trainee extends User {
    private Long userId;
    private LocalDate dateOfBirth;
    private String address;

    public Trainee(Long userId, String firstName, String lastName, String username, String password, boolean isActive, LocalDate dateOfBirth, String address) {
        super(firstName, lastName, username, password, isActive);
        this.userId = userId;
        this.dateOfBirth = dateOfBirth;
        this.address = address;
    }

}
