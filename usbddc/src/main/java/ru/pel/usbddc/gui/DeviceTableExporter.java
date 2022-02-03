package ru.pel.usbddc.gui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

public class DeviceTableExporter implements ActionListener {
    private final JTable devicesTable;
    private final Logger logger = LoggerFactory.getLogger(DeviceTableExporter.class);

    public DeviceTableExporter(JTable devicesTable) {
        this.devicesTable = devicesTable;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        final String COLUMN_SEPARATOR = "\t";
        JFileChooser fileChooser = new JFileChooser();
        int returnVal = fileChooser.showDialog(null, "Save");
        fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            try (PrintWriter printWriter = new PrintWriter(fileChooser.getSelectedFile(), StandardCharsets.UTF_8)) {
                for (int col = 0; col < devicesTable.getColumnCount(); col++) {
                    printWriter.print(devicesTable.getColumnName(col) + COLUMN_SEPARATOR);
                }
                printWriter.println();
                for (int row = 0; row < devicesTable.getRowCount(); row++) {
                    for (int col = 0; col < devicesTable.getColumnCount(); col++) {
                        printWriter.print(devicesTable.getValueAt(row, col) + COLUMN_SEPARATOR);

                        if (logger.isTraceEnabled()) {
                            logger.trace(devicesTable.getValueAt(row, col) + COLUMN_SEPARATOR);
                        }
                    }
                    printWriter.println();
                }
            } catch (IOException ex) {
                logger.info("An I/O error occurs while opening or creating the file. {}", ex);
            }
        }

    }
}
