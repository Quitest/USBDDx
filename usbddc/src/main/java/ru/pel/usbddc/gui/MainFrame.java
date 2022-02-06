package ru.pel.usbddc.gui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.pel.usbddc.entity.SystemInfo;
import ru.pel.usbddc.entity.USBDevice;
import ru.pel.usbddc.service.SystemInfoCollector;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.regex.PatternSyntaxException;

public class MainFrame extends JFrame {
    private static final Logger logger = LoggerFactory.getLogger(MainFrame.class);
    private final TableRowSorter<DefaultTableModel> sorter;
    private JTable devicesTable;
    private JButton collectInfoButton;
    private JButton showOsInfoButton;
    private JPanel mainPanel;
    private JPanel buttonsPanel;
    private JScrollPane devicesInfoScrollPane;
    private JTextField filterField;
    private JButton exportButton;

    private SystemInfo systemInfo;

    public MainFrame() {
        super("USBDDc");

        this.setContentPane(mainPanel);
        devicesTable.setOpaque(true);

        DefaultTableModel tableModel = new DefaultTableModel(0, 0);
        String[] header = new String[]{"№", "Serial", "Generated", "Friendly name", "PID", "VID",
                "Product name", "Vendor name", "Volume name", "Revision", "First install",
                "User accounts list", "GUID"};
        tableModel.setColumnIdentifiers(header);


        sorter = new TableRowSorter<>(tableModel);
        devicesTable.setModel(tableModel);
        devicesTable.setRowSorter(sorter);
        devicesTable.setPreferredScrollableViewportSize(new Dimension(500, 100));
        devicesTable.setFillsViewportHeight(true);


        collectInfoButton.addActionListener(actionEvent -> {
            long startTime = System.currentTimeMillis();
            tableModel.setRowCount(0);
            collectInfoButton.setEnabled(false);
            exportButton.setEnabled(false);
            showOsInfoButton.setEnabled(false);
            new Thread(() -> {
                systemInfo = new SystemInfoCollector().collectSystemInfo().getSystemInfo();
                Map<String, USBDevice> usbDeviceMap = systemInfo.getUsbDeviceMap();

                SwingUtilities.invokeLater(() -> {
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
                    resizeColumnWidth(devicesTable);
                    collectInfoButton.setEnabled(true);
                    exportButton.setEnabled(true);
                    showOsInfoButton.setEnabled(true);
                });
            }).start();
            logger.trace("Общее время выполнения анализа {}", System.currentTimeMillis() - startTime);
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
        exportButton.addActionListener(new DeviceTableExporter(devicesTable));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MainFrame::createAndShowGUI);
    }

    private static void createAndShowGUI() {
        MainFrame frame = new MainFrame();
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        frame.pack();
        frame.setVisible(true);
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

    public void resizeColumnWidth(JTable table) {
        final TableColumnModel columnModel = table.getColumnModel();
        for (int column = 0; column < table.getColumnCount(); column++) {
            int width = 50; // Min width
            for (int row = 0; row < table.getRowCount(); row++) {
                TableCellRenderer renderer = table.getCellRenderer(row, column);
                Component comp = table.prepareRenderer(renderer, row, column);
                width = Math.max(comp.getPreferredSize().width + 1, width);
            }
            if (width > 400)
                width = 400;
            columnModel.getColumn(column).setPreferredWidth(width);
        }
    }
}
