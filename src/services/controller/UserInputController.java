package services.controller;

import services.middleware.checkValidation;
import javax.swing.*;

public class UserInputController {

    public static boolean validateAndSubmit(String email, String contact, int age) {
        if (!checkValidation.isValidEmail(email)) {
            JOptionPane.showMessageDialog(null, "Invalid email format!");
            return false;
        }

        if (!checkValidation.isValidContact(contact)) {
            JOptionPane.showMessageDialog(null, "Invalid contact number!");
            return false;
        }

        if (!checkValidation.isValidAge(age)) {
            JOptionPane.showMessageDialog(null, "Invalid age!");
            return false;
        }

        // Proceed with submission if all are valid
        System.out.println("All inputs valid. Ready to submit!");
        return true;
    }
}