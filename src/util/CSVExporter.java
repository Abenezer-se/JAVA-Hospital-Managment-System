package util;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.io.*;

public class CSVExporter {
    public static void exportTable(JTable table, String filename) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(filename))) {
            DefaultTableModel model = (DefaultTableModel) table.getModel();
            for (int i = 0; i < model.getColumnCount(); i++) {
                pw.print(model.getColumnName(i));
                if (i < model.getColumnCount() - 1)
                    pw.print(",");
            }
            pw.println();
            for (int r = 0; r < model.getRowCount(); r++) {
                for (int c = 0; c < model.getColumnCount(); c++) {
                    Object val = model.getValueAt(r, c);
                    pw.print(val != null ? val.toString() : "");
                    if (c < model.getColumnCount() - 1)
                        pw.print(",");
                }
                pw.println();
            }
            JOptionPane.showMessageDialog(null, "Exported to " + filename);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Export error: " + e.getMessage());
        }
    }
}