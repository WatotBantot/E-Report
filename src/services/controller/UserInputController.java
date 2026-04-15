package services.controller;

import javax.swing.*;

import services.validation.ValidationUtil;

public class UserInputController {

    public static boolean validateAndSubmit(String email, String contact, int age) {
        if (!ValidationUtil.isValidEmail(email)) {
            JOptionPane.showMessageDialog(null, "Invalid email format!");
            return false;
        }

        if (!ValidationUtil.isValidContact(contact)) {
            JOptionPane.showMessageDialog(null, "Invalid contact number!");
            return false;
        }

        if (!ValidationUtil.isValidAge(age)) {
            JOptionPane.showMessageDialog(null, "Invalid age!");
            return false;
        }

        // Proceed with submission if all are valid
        System.out.println("All inputs valid. Ready to submit!");
        return true;
    }
}