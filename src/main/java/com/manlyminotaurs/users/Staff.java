package com.manlyminotaurs.users;

import java.util.List;

public class Staff extends User {
    public Staff(String userID, String firstName, String middleInitial, String lastName, List<String> languages, String userType) {
        super(userID, firstName, middleInitial, lastName, languages, userType);
    }
}
