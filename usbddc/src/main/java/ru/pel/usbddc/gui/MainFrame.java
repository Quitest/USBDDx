package ru.pel.usbddc.gui;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.pel.usbddc.collector.SystemInfoCollector;
import ru.pel.usbddc.config.UsbddcConfig;
import ru.pel.usbddc.dto.SystemInfoDto;
import ru.pel.usbddc.entity.SystemInfo;
import ru.pel.usbddc.entity.USBDevice;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.regex.PatternSyntaxException;

public class MainFrame extends JFrame {
    private static final Logger logger = LoggerFactory.getLogger(MainFrame.class);
    private static final String SERIAL_TRASH = ".*[^\\w#{}&?\\-:]+";
    private final TableRowSorter<DefaultTableModel> sorter;
    private final boolean isSkipSerialTrash = UsbddcConfig.getInstance().isSkipSerialTrash();
    private JTable devicesTable;
    private JButton collectInfoButton;
    private JButton showOsInfoButton;
    private JPanel mainPanel;
    private JPanel buttonsPanel;
    private JScrollPane devicesInfoScrollPane;
    private JTextField filterField;
    private JButton exportButton;
    private JButton sendButton;
    private JPanel statusPanel;
    private JLabel localStatusLabel;
    private JLabel remoteStatusLabel;
    private JTextArea commentTextArea;
    private JLabel commentLabel;
    private SystemInfo systemInfo;
    private DefaultTableModel tableModel = new DefaultTableModel(0, 0);


    public MainFrame() {
        super("USBDDc");

        this.setContentPane(mainPanel);
        String[] header = new String[]{"№", "Serial", "Generated", "Friendly name", "PID", "VID",
                "Product name", "Product name by Registry", "Vendor name", "Vendor name by Registry", "Volume name", "Volume ID", "Revision", "First install",
                "User accounts list", "GUID"};
        tableModel.setColumnIdentifiers(header);
        devicesTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        devicesTable.setOpaque(true);
        sorter = new TableRowSorter<>(tableModel);
        devicesTable.setModel(tableModel);
        devicesTable.setRowSorter(sorter);
        devicesTable.setPreferredScrollableViewportSize(new Dimension(980, 500));
        devicesTable.setFillsViewportHeight(true);

        collectInfoButton.addActionListener(actionEvent -> fillDevTable());

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
        sendButton.addActionListener(actionEvent -> sendReport());
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

    private void fillDevTable() {
        long startTime = System.currentTimeMillis();
        tableModel.setRowCount(0);
        collectInfoButton.setEnabled(false);
        exportButton.setEnabled(false);
        showOsInfoButton.setEnabled(false);
        sendButton.setEnabled(false);
        localStatusLabel.setText("Анализ выполняется...");
        new Thread(() -> {
            systemInfo = new SystemInfoCollector().collectSystemInfo().getSystemInfo();
            Map<String, USBDevice> usbDeviceMap = systemInfo.getUsbDeviceMap();

            SwingUtilities.invokeLater(() -> {
                List<USBDevice> usbDeviceList = new ArrayList<>(usbDeviceMap.values());
                for (int count = 0; count < usbDeviceList.size(); count++) {
                    USBDevice device = usbDeviceList.get(count);
                    String serial = device.getSerial();
                    if (isSkipSerialTrash && serial.matches(SERIAL_TRASH)) {
                        continue;
                    }
                    Vector<Object> data = new Vector<>();
                    data.add(count + 1);
                    data.add(serial);
                    data.add(device.isSerialOSGenerated());
                    data.add(device.getFriendlyName());
                    data.add(device.getPid());
                    data.add(device.getVid());
                    data.add(device.getProductName());
                    data.add(device.getProductNameByRegistry());
                    data.add(device.getVendorName());
                    data.add(device.getVendorNameByRegistry());
                    data.add(device.getVolumeLabelList());
                    data.add(device.getVolumeIdList());
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
                sendButton.setEnabled(true);
                localStatusLabel.setText("Анализ выполнен.");
            });
        }).start();
        logger.trace("Общее время выполнения анализа {}", System.currentTimeMillis() - startTime);
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
            if (width > 400) {
                width = 400;
            }
            columnModel.getColumn(column).setPreferredWidth(width);
        }
    }

    private void saveComment() {
        systemInfo.setComment(commentTextArea.getText());
    }

    private void sendReport() {
        saveComment();
        SystemInfoDto dto = new SystemInfoDto(systemInfo);
        ObjectWriter ow = new ObjectMapper().findAndRegisterModules().writer().withDefaultPrettyPrinter();

        try {
            String jsonInputString = ow.writeValueAsString(dto);
            logger.debug("Сформирован JSON-запрос: {}", jsonInputString);
            URL postUrl = new URL(UsbddcConfig.getInstance().getUrlPostSystemInfo());
            HttpRequest request = HttpRequest.newBuilder()
                    .POST(HttpRequest.BodyPublishers.ofString(jsonInputString))
                    .uri(postUrl.toURI())
                    .header("Content-Type", "application/json; utf-8")
                    .header("Accept", "application/json")
                    .timeout(Duration.of(10, ChronoUnit.SECONDS))
                    .build();
            localStatusLabel.setText("Идет отправка отчета...");

            HttpResponse<String> response = HttpClient.newBuilder()
                    .build()
                    .send(request, HttpResponse.BodyHandlers.ofString());


            remoteStatusLabel.setText("Статус: " + response.statusCode());
            logger.info("Ответ сервера: {}", response.statusCode());
            localStatusLabel.setText("ГОТОВ");
        } catch (JsonProcessingException e) {
            logger.error("Ошибка генерации JSON: {}", e.getLocalizedMessage());
            showErrorMessage("Ошибка генерации JSON", e.getLocalizedMessage());
        } catch (MalformedURLException e) {
            logger.error("Ошибка в адресе сервера USBDDa: {}", e.getLocalizedMessage());
            showErrorMessage("Ошибка в адресе сервера USBDDa", e.getLocalizedMessage());
        } catch (URISyntaxException e) {
            logger.error("URL сервера USBDDa не соответствует RFC2390: {}", e.getLocalizedMessage());
            showErrorMessage("URL сервера USBDDa не соответствует RFC2390", e.getLocalizedMessage());
        } catch (InterruptedException e) {
            logger.error("Процесс отправки данных прерван: {}", e.getLocalizedMessage());
            showErrorMessage("Процесс отправки данных прерван", e.getLocalizedMessage());
            Thread.currentThread().interrupt();
        } catch (IOException e) {
            logger.error("При отправке произошла ошибка ввода/вывода: {}", e.getLocalizedMessage());
            showErrorMessage("При отправке произошла ошибка ввода/вывода", e.getLocalizedMessage());
        }
    }

    private void showErrorMessage(String title, String message) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE);
        String text = String.format("Отчет не отправлен: %s [%s]", title, message);
        localStatusLabel.setText(text);
    }
}
