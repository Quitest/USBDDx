package ru.pel.usbddc.gui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

public class DeviceTableExporter implements ActionListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(DeviceTableExporter.class);
    private final JTable devicesTable;

    public DeviceTableExporter(JTable devicesTable) {
        this.devicesTable = devicesTable;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        final String COLUMN_SEPARATOR = "\t";
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(new File(System.getProperty("user.dir")));

        int returnVal = fileChooser.showDialog(null, "Save");
        fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            try (PrintWriter printWriter = new PrintWriter(fileChooser.getSelectedFile() + ".csv", StandardCharsets.UTF_8)) {
                for (int col = 0; col < devicesTable.getColumnCount(); col++) {
                    printWriter.print(devicesTable.getColumnName(col) + COLUMN_SEPARATOR);
                }
                printWriter.println();
                for (int row = 0; row < devicesTable.getRowCount(); row++) {
                    for (int col = 0; col < devicesTable.getColumnCount(); col++) {
                        printWriter.print(devicesTable.getValueAt(row, col) + COLUMN_SEPARATOR);

                        LOGGER.trace("{} : {}", devicesTable.getColumnName(col), devicesTable.getValueAt(row, col));
                    }
                    printWriter.println();
                }
            } catch (IOException ex) {
                LOGGER.error("An I/O error occurs while opening or creating the file. {}", ex.getMessage());
                LOGGER.debug("An I/O error occurs while opening or creating the file.", ex);
            }
        }

    }
}
