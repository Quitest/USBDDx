package ru.pel.usbddc.gui;

import ru.pel.usbddc.entity.OSInfo;
import ru.pel.usbddc.entity.SystemInfo;
import ru.pel.usbddc.entity.USBDevice;
import ru.pel.usbddc.service.SystemInfoCollector;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

public class MainFrame extends JFrame {
    private JTable devicesTable;
    private JButton collectInfoButton;
    private JButton showOsInfoButton;
    private JPanel mainPanel;
    private JPanel buttonsPanel;
    private JScrollPane devicesInfoScrollPane;

    public MainFrame() {
        super("USBDDc");
        collectInfoButton.addActionListener(actionEvent -> {
            try {
                SystemInfo systemInfo = new SystemInfoCollector().collectSystemInfo().getSystemInfo();
                Map<String, USBDevice> usbDeviceMap = systemInfo.getUsbDeviceMap();
                OSInfo osInfo = systemInfo.getOsInfo();

                DefaultTableModel dm = new DefaultTableModel(0, 0);
                String[] header = new String[]{"№", "serial", "isSerialOSGenerated", "friendlyName", "PID", "VID",
                        "productName", "vendorName", "volumeName", "revision", "dateTimeFirstInstall",
                        "userAccountsList", "GUID"};


                dm.setColumnIdentifiers(header);
                devicesTable.setModel(dm);

                List<USBDevice> usbDeviceList = new ArrayList<>(usbDeviceMap.values());
                for (int count = 0; count < usbDeviceList.size(); count++) {
                    Vector<Object> data = new Vector<>();
//                        int finalCount = count;
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

                    dm.addRow(data);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainFrame().createAndShowGUI());
    }

    private void createAndShowGUI() {
        //Create and set up the window.
        MainFrame frame = new MainFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Create and set up the content pane.
        frame.setContentPane(mainPanel);
        devicesTable.setOpaque(true);

        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }
}
