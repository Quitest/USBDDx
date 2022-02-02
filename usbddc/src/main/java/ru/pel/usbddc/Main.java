package ru.pel.usbddc;
/**
 * USBDDC - USB devices data collector
 */

import ru.pel.usbddc.gui.MainFrame;

import javax.swing.*;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        //возможно в будущем будет использоваться. В настоящее время функционал тестируется JUpiter'ом.
        MainFrame mainFrame = new MainFrame();
        mainFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        mainFrame.pack();
        mainFrame.setVisible(true);
    }
}
