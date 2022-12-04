import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import com.toedter.calendar.JMonthChooser;

import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

public class StatementForm extends JFrame {
    private JPanel panel;
    private JPanel Controls;
    private JPanel DisplayPanel;
    private JRadioButton allAccountsRadioButton;
    private JRadioButton specificAccountRadioButton;
    private JButton allTimeButton;
    private JButton specificMonthButton;
    private JScrollBar scrollBar1;
    private JScrollPane statementPane;
    private JLabel criteria2;
    private JLabel criteria1;

    private String accountNumber = "";
    private String month = "";
    private boolean getAll = true;
    private boolean allTime = true;

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
        refreshCriteria();
    }

    public void setMonth(String month) {
        this.month = month;
        refreshCriteria();
    }

    public void setAllTime(boolean allTime) {
        this.allTime = allTime;
        refreshCriteria();
    }

    public void setGetAll(boolean getAll) {
        this.getAll = getAll;
        refreshCriteria();
    }

    public void refreshCriteria() {
        if (getAll) {
            criteria1.setText("All accounts");
        } else {
            criteria1.setText("Account: ".concat(accountNumber));
        }
        if (allTime) {
            criteria2.setText("All time");
        } else {
            criteria2.setText("Month: ".concat(new DateFormatSymbols().getMonths()[Integer.parseInt(month)]));
        }
    }

    public StatementForm() {
        super("ABBank myStatement");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(970, 600);
        this.setContentPane(panel);
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setResizable(false);
        this.setVisible(true);
        this.setLocationRelativeTo(null);
        APIHandle apiHandle = APIHandle.getInstance();
        apiHandle.refreshInformation();
        specificAccountRadioButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (apiHandle.getAccountsNum() == 0) {
                    JOptionPane.showMessageDialog(null, "You do not have an active account!", "ERROR", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                String[] options = new String[apiHandle.getAccountsNum()];
                final int[] i = {0};
                apiHandle.getAccounts().forEach((account) -> {
                    String number = account.get("accountNumber");
                    String type = account.get("accountType");
                    String option = String.format("%s , %s", type, number);
                    options[i[0]] = option;
                    i[0]++;
                });
                int from = JOptionPane.showOptionDialog(null, "Select the account that you want to view its statement:", "Choose account",
                        JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
                setGetAll(false);
                setAccountNumber(options[from].split(",")[1].stripLeading());
            }
        });
        allAccountsRadioButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                setGetAll(true);
            }
        });
        allTimeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (getAll) {
                    Map<String, List<Map<String, String>>> statements = apiHandle.getStatement();
                    showStatements(statements);
                } else {
                    Map<String, List<Map<String, String>>> statements = apiHandle.getStatement(accountNumber);
                    showStatements(statements);
                }
                setAllTime(true);
            }
        });
        specificMonthButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                Map<String, List<Map<String, String>>> statements;
                JMonthChooser monthChooser = new JMonthChooser();
                String message = "Choose a specific month:\n";
                Object[] params = {message, monthChooser};
                JOptionPane.showConfirmDialog(null, params, "Choose month", JOptionPane.DEFAULT_OPTION);
                String month = String.valueOf(((JMonthChooser) params[1]).getMonth());
                DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MM");
                setMonth(month);
                if (getAll) {
                    statements = apiHandle.getStatement();
                } else {
                    statements = apiHandle.getStatement(getAccountNumber());
                }
                Map<String, List<Map<String, String>>> filteredStatements = new HashMap<>();
                final int[] records = {0};
                statements.forEach((account, logs) -> {
                            List<Map<String, String>> filteredLog = new ArrayList<>();
                            logs.forEach((log) -> {
                                DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
                                Date date;
                                try {
                                    date = df.parse(log.get("logDate"));
                                } catch (ParseException e) {
                                    throw new RuntimeException(e);
                                }
                                Calendar cal = Calendar.getInstance();
                                cal.setTime(date);
                                int logMonth = cal.get(Calendar.MONTH);
                                if (logMonth == Integer.parseInt(month)) {
                                    filteredLog.add(log);
                                    records[0]++;
                                }
                            });
                            filteredStatements.put(account, filteredLog);
                        }
                );
                if (records[0] == 0) {
                    JLabel entries = new JLabel("No statement entries found at the specified month!");
                    entries.setHorizontalAlignment(JLabel.CENTER);
                    statementPane.setViewportView(entries);
                    return;
                }
                setAllTime(false);
                showStatements(filteredStatements);
            }
        });
    }

    public void showStatements(Map<String, List<Map<String, String>>> statements) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        statements.forEach((account, logs) -> logs.forEach((log) -> {
                    DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
                    Date date;
                    try {
                        date = df.parse(log.get("logDate"));
                    } catch (ParseException e) {
                        throw new RuntimeException(e);
                    }
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(date);
                    StatementPanel statementPanel = new StatementPanel(cal.getTime().toString(), log.get("logType"), log.get("logMessage"));
                    panel.add(statementPanel.getPanel());
                })
        );
        statementPane.setViewportView(panel);
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        panel = new JPanel();
        panel.setLayout(new BorderLayout(0, 0));
        Controls = new JPanel();
        Controls.setLayout(new GridLayoutManager(3, 7, new Insets(20, 20, 20, 20), -1, -1));
        Controls.setBackground(new Color(-14276018));
        panel.add(Controls, BorderLayout.NORTH);
        allAccountsRadioButton = new JRadioButton();
        allAccountsRadioButton.setBackground(new Color(-14276018));
        allAccountsRadioButton.setForeground(new Color(-1));
        allAccountsRadioButton.setText("All Accounts");
        Controls.add(allAccountsRadioButton, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        specificAccountRadioButton = new JRadioButton();
        specificAccountRadioButton.setBackground(new Color(-14276018));
        specificAccountRadioButton.setForeground(new Color(-1));
        specificAccountRadioButton.setText("Specific Account");
        Controls.add(specificAccountRadioButton, new GridConstraints(0, 5, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        allTimeButton = new JButton();
        allTimeButton.setBackground(new Color(-14276018));
        allTimeButton.setForeground(new Color(-1));
        allTimeButton.setText("All Time");
        Controls.add(allTimeButton, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        specificMonthButton = new JButton();
        specificMonthButton.setBackground(new Color(-14276018));
        specificMonthButton.setForeground(new Color(-1));
        specificMonthButton.setText("Specific Month");
        Controls.add(specificMonthButton, new GridConstraints(1, 5, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label1 = new JLabel();
        Font label1Font = this.$$$getFont$$$(null, Font.BOLD, 14, label1.getFont());
        if (label1Font != null) label1.setFont(label1Font);
        label1.setForeground(new Color(-1));
        label1.setText("Criteria");
        Controls.add(label1, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        criteria1 = new JLabel();
        Font criteria1Font = this.$$$getFont$$$(null, Font.ITALIC, 16, criteria1.getFont());
        if (criteria1Font != null) criteria1.setFont(criteria1Font);
        criteria1.setForeground(new Color(-1));
        criteria1.setText("All Accounts / Specific Account");
        Controls.add(criteria1, new GridConstraints(1, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        criteria2 = new JLabel();
        Font criteria2Font = this.$$$getFont$$$(null, Font.ITALIC, 16, criteria2.getFont());
        if (criteria2Font != null) criteria2.setFont(criteria2Font);
        criteria2.setForeground(new Color(-1));
        criteria2.setText("All Time / Specific Month");
        Controls.add(criteria2, new GridConstraints(2, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        Controls.add(spacer1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final Spacer spacer2 = new Spacer();
        Controls.add(spacer2, new GridConstraints(0, 6, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final Spacer spacer3 = new Spacer();
        Controls.add(spacer3, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final Spacer spacer4 = new Spacer();
        Controls.add(spacer4, new GridConstraints(1, 4, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        statementPane = new JScrollPane();
        panel.add(statementPane, BorderLayout.CENTER);
        scrollBar1 = new JScrollBar();
        panel.add(scrollBar1, BorderLayout.EAST);
        statementPane.setVerticalScrollBar(scrollBar1);
        ButtonGroup buttonGroup;
        buttonGroup = new ButtonGroup();
        buttonGroup.add(allAccountsRadioButton);
        buttonGroup.add(specificAccountRadioButton);
    }

    /**
     * @noinspection ALL
     */
    private Font $$$getFont$$$(String fontName, int style, int size, Font currentFont) {
        if (currentFont == null) return null;
        String resultName;
        if (fontName == null) {
            resultName = currentFont.getName();
        } else {
            Font testFont = new Font(fontName, Font.PLAIN, 10);
            if (testFont.canDisplay('a') && testFont.canDisplay('1')) {
                resultName = fontName;
            } else {
                resultName = currentFont.getName();
            }
        }
        Font font = new Font(resultName, style >= 0 ? style : currentFont.getStyle(), size >= 0 ? size : currentFont.getSize());
        boolean isMac = System.getProperty("os.name", "").toLowerCase(Locale.ENGLISH).startsWith("mac");
        Font fontWithFallback = isMac ? new Font(font.getFamily(), font.getStyle(), font.getSize()) : new StyleContext().getFont(font.getFamily(), font.getStyle(), font.getSize());
        return fontWithFallback instanceof FontUIResource ? fontWithFallback : new FontUIResource(fontWithFallback);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return panel;
    }

}
