package ru.pel.usbddc.gui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.pel.usbddc.entity.OSInfo;
import ru.pel.usbddc.entity.SystemInfo;
import ru.pel.usbddc.entity.USBDevice;
import ru.pel.usbddc.service.SystemInfoCollector;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.regex.PatternSyntaxException;

public class MainFrame extends JFrame {
    private final static Logger logger = LoggerFactory.getLogger(MainFrame.class);
    private final TableRowSorter<DefaultTableModel> sorter;
    private JTable devicesTable;
    private JButton collectInfoButton;
    private JButton showOsInfoButton;
    private JPanel mainPanel;
    private JPanel buttonsPanel;
    private JScrollPane devicesInfoScrollPane;
    private JTextField filterField;
    private JButton exportButton;

    public MainFrame() {
        super("USBDDc");

        this.setContentPane(mainPanel);
        devicesTable.setOpaque(true);

        DefaultTableModel tableModel = new DefaultTableModel(0, 0);
        String[] header = new String[]{"№", "serial", "isSerialOSGenerated", "friendlyName", "PID", "VID",
                "productName", "vendorName", "volumeName", "revision", "dateTimeFirstInstall",
                "userAccountsList", "GUID"};
        tableModel.setColumnIdentifiers(header);


        sorter = new TableRowSorter<>(tableModel);
        devicesTable.setModel(tableModel);
        devicesTable.setRowSorter(sorter);
        devicesTable.setPreferredScrollableViewportSize(new Dimension(500, 100));
        devicesTable.setFillsViewportHeight(true);

        collectInfoButton.addActionListener(actionEvent -> {
            tableModel.setRowCount(0);
            try {
                SystemInfo systemInfo = new SystemInfoCollector().collectSystemInfo().getSystemInfo();
                Map<String, USBDevice> usbDeviceMap = systemInfo.getUsbDeviceMap();
                OSInfo osInfo = systemInfo.getOsInfo();

                List<USBDevice> usbDeviceList = new ArrayList<>(usbDeviceMap.values());
                for (int count = 0; count < usbDeviceList.size(); count++) {
                    Vector<Object> data = new Vector<>();
                    data.add(count + 1);
                    USBDevice device = usbDeviceList.get(count);
                    data.add(device.getSerial());
                    data.add(device.isSerialOSGenerated());
                    data.add(device.getFriendlyName());
                    data.add(device.getPid());
                    data.add(device.getVid());
                    data.add(device.getProductName());
                    data.add(device.getVendorName());
                    data.add(device.getVolumeName());
                    data.add(device.getRevision());
                    data.add(device.getDateTimeFirstInstall());
                    data.add(device.getUserAccountsList());
                    data.add(device.getGuid());

                    tableModel.addRow(data);
                }

            } catch (IOException e) {
                logger.error("Analysis error. {}", e);
            }
        });

        filterField.getDocument().addDocumentListener(
                new DocumentListener() {
                    @Override
                    public void changedUpdate(DocumentEvent e) {
                        newFilter();
                    }

                    @Override
                    public void insertUpdate(DocumentEvent e) {
                        newFilter();
                    }

                    @Override
                    public void removeUpdate(DocumentEvent e) {
                        newFilter();
                    }
                }
        );
        exportButton.addActionListener(exportActionListener());
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MainFrame::createAndShowGUI);
    }

    private static void createAndShowGUI() {
        MainFrame frame = new MainFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.pack();
        frame.setVisible(true);
    }

    private ActionListener exportActionListener() {
        return e -> {
            final String COLUMN_SEPARATOR = ";";
            JFileChooser fileChooser = new JFileChooser();
            int returnVal = fileChooser.showDialog(MainFrame.this, "Save");
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

        };
    }

    /**
     * Update the row filter regular expression from the expression in
     * the text box.
     */
    private void newFilter() {
        RowFilter<DefaultTableModel, Object> rf;
        //If current expression doesn't parse, don't update.
        try {
            rf = RowFilter.regexFilter(filterField.getText());
        } catch (PatternSyntaxException e) {
            return;
        }
        sorter.setRowFilter(rf);
    }
}
