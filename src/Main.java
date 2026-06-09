import db.DBConnection;
import ui.MainFrame;

import javax.swing.*;

/**
 * Application entry point.
 *
 * ▶ IntelliJ Run Configuration:
 *   - Main class: Main
 *   - Add lib/ JARs to classpath (File > Project Structure > Libraries > + > Java > select all JARs in lib/)
 */
public class Main {

    public static void main(String[] args) {
        // ── 1. Set FlatDark-style look and feel ────────────────────────────
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) {}

        // Apply global dark UI defaults
        applyGlobalDarkDefaults();

        // ── 2. Init DB (creates schema if missing) ─────────────────────────
        try {
            DBConnection.getInstance();
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(null,
                "❌ Failed to connect to MySQL.\n\nDetails: " + ex.getCause().getMessage()
                + "\n\nMake sure:\n  • MySQL server is running\n  • Credentials in DBConnection.java are correct\n  • MySQL Connector JAR is in lib/",
                "Database Connection Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        // ── 3. Launch UI on Event Dispatch Thread ──────────────────────────
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            // Graceful shutdown: close DB connection on window close
            frame.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosing(java.awt.event.WindowEvent e) {
                    DBConnection.getInstance().close();
                }
            });
        });
    }

    /** Applies consistent dark colors to standard Swing components globally. */
    private static void applyGlobalDarkDefaults() {
        java.awt.Color bg      = new java.awt.Color(30, 30, 46);
        java.awt.Color cardBg  = new java.awt.Color(36, 36, 54);
        java.awt.Color text    = new java.awt.Color(205, 214, 244);
        java.awt.Color subtext = new java.awt.Color(148, 163, 184);
        java.awt.Color select  = new java.awt.Color(88, 91, 112);
        java.awt.Color inputBg = new java.awt.Color(24, 24, 37);

        UIManager.put("Panel.background",              bg);
        UIManager.put("Frame.background",              bg);
        UIManager.put("OptionPane.background",         cardBg);
        UIManager.put("OptionPane.messageForeground",  text);
        UIManager.put("Button.background",             cardBg);
        UIManager.put("Button.foreground",             text);
        UIManager.put("Label.foreground",              text);
        UIManager.put("TextField.background",          inputBg);
        UIManager.put("TextField.foreground",          text);
        UIManager.put("TextField.caretForeground",     text);
        UIManager.put("ComboBox.background",           inputBg);
        UIManager.put("ComboBox.foreground",           text);
        UIManager.put("ComboBox.selectionBackground",  select);
        UIManager.put("Table.background",              cardBg);
        UIManager.put("Table.foreground",              text);
        UIManager.put("Table.selectionBackground",     select);
        UIManager.put("Table.selectionForeground",     java.awt.Color.WHITE);
        UIManager.put("TableHeader.background",        new java.awt.Color(20, 20, 35));
        UIManager.put("TableHeader.foreground",        new java.awt.Color(137, 180, 250));
        UIManager.put("ScrollPane.background",         bg);
        UIManager.put("ScrollBar.background",          new java.awt.Color(24, 24, 37));
        UIManager.put("ScrollBar.thumb",               new java.awt.Color(60, 60, 90));
        UIManager.put("ScrollBar.track",               new java.awt.Color(24, 24, 37));
        UIManager.put("Viewport.background",           bg);
        UIManager.put("ToolTip.background",            cardBg);
        UIManager.put("ToolTip.foreground",            text);
    }
}
