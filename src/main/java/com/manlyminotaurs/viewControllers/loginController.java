package com.manlyminotaurs.viewControllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import com.manlyminotaurs.core.Main;

public class loginController {

    @FXML
    Button btnBack;

    @FXML
    Label lblTitle;

    @FXML
    Label lblUsername;

    @FXML
    TextField txtUser;

    @FXML
    Label lblPassword;

    @FXML
    PasswordField txtPassword;

    @FXML
    Button btnLogin;

    @FXML
    Label lblWarning;


    public void back(ActionEvent event) {
        Main.removePrompt(0);
    }


    public void login(ActionEvent event){


        txtUser.clear();
        txtPassword.clear();
        //if admin account

        Main.setScreen(4); //go to landing screen
    }

}
