package com.manlyminotaurs.viewControllers;

import com.manlyminotaurs.core.Kiosk;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import com.manlyminotaurs.core.Main;

import java.net.URL;
import java.util.ResourceBundle;

public class loginController implements Initializable{

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


    public void login(ActionEvent event) {


       if ((txtUser.getText().equals("user")) && (txtPassword.getText().equals("password"))){
           Main.removePrompt(0);
           Main.removeScreen(1);
           Main.removeAction(Main.getAction());
           txtUser.clear();
           txtPassword.clear();
           new Kiosk().setUser("user");
           Main.setScreen(3);
       }
       else if ((txtUser.getText().equals( "admin")) && (txtPassword.getText().equals("password"))){
           Main.removePrompt(0);
           Main.removeScreen(1);
           Main.removeAction(Main.getAction());
           txtUser.clear();
           txtPassword.clear();
           new Kiosk().setUser("admin");
           Main.setScreen(2);
       }
       else{
           lblWarning.setText("Incorrect UserName or Password Try Again");
           txtPassword.clear();
       }

    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        new Kiosk().setUser("user");
    }
}
