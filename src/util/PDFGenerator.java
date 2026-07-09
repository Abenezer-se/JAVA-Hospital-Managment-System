package util;

import javax.swing.*;
import java.io.*;

public class PDFGenerator {
    public static void generateReceipt(String patient, String doctor,
            String consult, String lab, String medicine,
            String total, String status, String date) {
        String filename = "receipt_" + patient.replaceAll(" ", "_") + "_" + date + ".txt";
        try (PrintWriter pw = new PrintWriter(new FileWriter(filename))) {
            pw.println("============================================");
            pw.println("        HOSPITAL MANAGEMENT SYSTEM         ");
            pw.println("              PAYMENT RECEIPT              ");
            pw.println("============================================");
            pw.println("Date              : " + date);
            pw.println("Patient           : " + patient);
            pw.println("Doctor            : " + doctor);
            pw.println("--------------------------------------------");
            pw.println("Consultation Fee  : ETB " + consult);
            pw.println("Laboratory Fee    : ETB " + lab);
            pw.println("Medicine Fee      : ETB " + medicine);
            pw.println("--------------------------------------------");
            pw.println("TOTAL AMOUNT      : ETB " + total);
            pw.println("Payment Status    : " + status);
            pw.println("============================================");
            pw.println("      Thank you for choosing our hospital!  ");
            pw.println("============================================");
            JOptionPane.showMessageDialog(null, "Receipt saved as: " + filename);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage());
        }
    }
}