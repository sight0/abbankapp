import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Objects;


public class CustomerForm extends JFrame {
    private JPanel panel;
    private JLabel welcome;
    private JButton requestAccount;
    private JButton requestLoan;
    private JButton eStatement;
    private JButton transferButton;
    private JButton debts;
    private JButton payUtilitiesButton;
    private JButton editProfileButton;
    private JButton logoutButton;
    private JButton notificationsButton;
    private JLabel lastLogin;
    private JLabel todayDate;
    private JPanel accountsPanel;
    private JButton left;
    private JButton right;

    public CustomerForm() {
        super("ABBank Online Banking");
        ImageIcon backgroundImage = new ImageIcon(Objects.requireNonNull(getImage("customer_background.png")));
        this.setContentPane(new JLabel(backgroundImage));
        this.setLayout(new GridBagLayout());
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(995, 588);
        panel.setOpaque(false);
        this.add(panel);
        this.setResizable(false);
        this.setVisible(true);
        APIHandle apiHandle = APIHandle.getInstance();
        apiHandle.refreshInformation();
        DateFormat Date = DateFormat.getDateInstance();
        Calendar cals = Calendar.getInstance();
        String currentDate = Date.format(cals.getTime());
        this.todayDate.setText(currentDate);
        accountsPanel.setLayout(new CardLayout(3, 3));
        accountsPanel.setOpaque(true);
        //accountsPanel.setMaximumSize(new Dimension(800, 400));
        accountsPanel.setBackground(Color.decode("#b1b2b5"));
        apiHandle.refreshInformation();
        apiHandle.refreshNotifications();
        updateInformation(apiHandle);
        Thread t1 = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                apiHandle.refreshNotifications();
                apiHandle.refreshInformation();
                updateInformation(apiHandle);
                System.out.println("Refreshed API!");
            }
        });
        t1.start();

        logoutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                System.exit(0);
            }
        });
        requestAccount.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                String[] options = {"current", "saving"};
                int type = JOptionPane.showOptionDialog(null, "Choose the type of account you would like to open and a banker will review your request:", "Request to open an account",
                        JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
                String response = apiHandle.requestAccount(options[type]);
                JOptionPane.showMessageDialog(null, response, "Request Submitted!", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        transferButton.addActionListener(new ActionListener() {
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
                    String option = String.format("%s - %s", type, number);
                    options[i[0]] = option;
                    i[0]++;
                });
                int from = JOptionPane.showOptionDialog(null, "Select the account that you want to transfer from:", "Choose account",
                        JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
                String accountNumber = options[from].split("-")[1].stripLeading();
                String receiverNumber = JOptionPane.showInputDialog("Enter the receiver account number:");
                String amount = JOptionPane.showInputDialog("Enter the amount:");
                if (!isNumeric(amount) || !isNumeric(accountNumber) || !isNumeric(receiverNumber)) {
                    JOptionPane.showMessageDialog(null, "Invalid input!", "ERROR", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                String response = apiHandle.transfer(accountNumber, receiverNumber, amount);
                JOptionPane.showMessageDialog(null, response, "Request response", JOptionPane.INFORMATION_MESSAGE);
                apiHandle.refreshInformation();
                updateInformation(apiHandle);
            }
        });
        payUtilitiesButton.addActionListener(new ActionListener() {
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
                    String option = String.format("%s - %s", type, number);
                    options[i[0]] = option;
                    i[0]++;
                });
                int from = JOptionPane.showOptionDialog(null, "Select the account that you want to pay bill with:", "Choose account",
                        JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
                String accountNumber = options[from].split("-")[1].stripLeading();
                String[] utilities = {"Etisalat", "DU", "DEWA"};
                String[] utilitiesAccounts = {"43211234916526", "43211234108674", "43211234788888"};
                int to = JOptionPane.showOptionDialog(null, "Select the utility you want to pay:", "Select Utility",
                        JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, utilities, utilities[0]);
                String receiverNumber = utilitiesAccounts[to];
                String amount = JOptionPane.showInputDialog("Enter the bill amount:");
                if (!isNumeric(amount) || !isNumeric(accountNumber)) {
                    JOptionPane.showMessageDialog(null, "Invalid input!", "ERROR", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                String response = apiHandle.transfer(accountNumber, receiverNumber, amount);
                JOptionPane.showMessageDialog(null, response, "Request response", JOptionPane.INFORMATION_MESSAGE);
                apiHandle.refreshInformation();
                updateInformation(apiHandle);
            }
        });
        debts.addActionListener(new ActionListener() {
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
                    String debt = account.get("accountDebt");
                    String option = String.format("%s - %s - %s", type, number, debt);
                    options[i[0]] = option;
                    i[0]++;
                });
                String totalDebts = apiHandle.getDebts();
                Number r = getNumber(totalDebts);
                if (r.doubleValue() == 0) {
                    JOptionPane.showMessageDialog(null, "You do not have an debts!", "ERROR", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                String sentence = String.format("Your total debt is %s \n Select the account that you want to pay its debts: ", totalDebts);
                int from = JOptionPane.showOptionDialog(null, sentence, "Choose account",
                        JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
                String accountNumber = options[from].split("-")[1].stripLeading();
                double accountDebt = getNumber(options[from].split("-")[2].stripLeading()).doubleValue();
                String amount = JOptionPane.showInputDialog("Enter the amount you would like to pay back:");
                if (!isNumeric(amount) || (Double.parseDouble(amount) > (accountDebt))) {
                    JOptionPane.showMessageDialog(null, "Invalid input!", "ERROR", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                String response = apiHandle.payDebt(accountNumber, amount);
                JOptionPane.showMessageDialog(null, response, "Request response", JOptionPane.INFORMATION_MESSAGE);
                apiHandle.refreshInformation();
                updateInformation(apiHandle);
            }
        });
        requestLoan.addActionListener(new ActionListener() {
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
                int from = JOptionPane.showOptionDialog(null, "Select the account that you want to request a loan for:", "Choose account",
                        JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
                String accountNumber = options[from].split(",")[1].strip();
                String amount = JOptionPane.showInputDialog("Enter the loan amount:");
                if (!isNumeric(amount) || !isNumeric(accountNumber)) {
                    JOptionPane.showMessageDialog(null, "Invalid input!", "ERROR", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                String response = apiHandle.requestLoan(accountNumber, amount);
                JOptionPane.showMessageDialog(null, response, "Request response", JOptionPane.INFORMATION_MESSAGE);
                apiHandle.refreshInformation();
                updateInformation(apiHandle);
            }
        });
        eStatement.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                new StatementForm();
            }
        });
        left.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                CardLayout cardLayout = (CardLayout) accountsPanel.getLayout();
                cardLayout.next(accountsPanel);
            }
        });
        right.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                CardLayout cardLayout = (CardLayout) accountsPanel.getLayout();
                cardLayout.previous(accountsPanel);
            }
        });
        editProfileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                new EditProfile();
            }
        });
        notificationsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                new NotificationPanel();
            }
        });
    }

    public Number getNumber(String totalDebts) {
        String formatted = totalDebts.split("\\s+")[0];
        DecimalFormat df = new DecimalFormat("#,##0.00");
        Number r = null;
        try {
            r = df.parse(formatted);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return r;
    }

    public boolean isNumeric(String number) {
        if (number == null) {
            return false;
        }
        try {
            double d = Double.parseDouble(number);
            if (d <= 0) {
                return false;
            }
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }


    private void updateInformation(APIHandle handle) {
        String firstName = handle.getFirstName();
        welcome.setText("Welcome ".concat(firstName).concat(","));
        notificationsButton.setText("`" + handle.getNotificationsNum() + "` Notifications");
        if (handle.getAccountsNum() == 0) return;
        accountsPanel.removeAll();
        handle.getAccounts().forEach((account) ->
                {
                    AccountPanel accountPanel = new AccountPanel(account.get("accountStatus"), account.get("accountDebt"), account.get("accountBalance"), account.get("accountNumber"), account.get("accountType"));
                    accountPanel.setBackground(Color.decode("#b1b2b5"));
                    accountsPanel.setBackground(Color.decode("#b1b2b5"));
                    accountsPanel.add(account.get("accountNumber"), accountPanel.getPanel());
                }
        );
    }

    private Image getImage(String filename) {
        try {
            return ImageIO.read(Objects.requireNonNull(getClass().getResourceAsStream(
                    "/" + filename)));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
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
        panel.setLayout(new GridBagLayout());
        welcome = new JLabel();
        welcome.setForeground(new Color(-2829100));
        welcome.setText("Welcome ,");
        GridBagConstraints gbc;
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(40, 0, 0, 0);
        panel.add(welcome, gbc);
        requestAccount = new JButton();
        requestAccount.setBackground(new Color(-10525075));
        requestAccount.setForeground(new Color(-1));
        requestAccount.setText("Request Account");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 0, 10, 0);
        panel.add(requestAccount, gbc);
        requestLoan = new JButton();
        requestLoan.setBackground(new Color(-10525075));
        requestLoan.setForeground(new Color(-1));
        requestLoan.setText("Request Loan");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(15, 0, 10, 0);
        panel.add(requestLoan, gbc);
        eStatement = new JButton();
        eStatement.setBackground(new Color(-10525075));
        eStatement.setForeground(new Color(-1));
        eStatement.setText("My eStatement");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(15, 0, 10, 0);
        panel.add(eStatement, gbc);
        transferButton = new JButton();
        transferButton.setBackground(new Color(-10525075));
        transferButton.setForeground(new Color(-1));
        transferButton.setText("Transfer");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(15, 0, 10, 0);
        panel.add(transferButton, gbc);
        debts = new JButton();
        debts.setBackground(new Color(-10525075));
        debts.setForeground(new Color(-1));
        debts.setText("Pay Debts");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(15, 0, 10, 0);
        panel.add(debts, gbc);
        payUtilitiesButton = new JButton();
        payUtilitiesButton.setBackground(new Color(-10525075));
        payUtilitiesButton.setForeground(new Color(-1));
        payUtilitiesButton.setText("Pay Utilities");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 8;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(15, 0, 10, 0);
        panel.add(payUtilitiesButton, gbc);
        editProfileButton = new JButton();
        editProfileButton.setBackground(new Color(-10525075));
        editProfileButton.setForeground(new Color(-1));
        editProfileButton.setText("Edit Profile");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 9;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(15, 0, 30, 0);
        panel.add(editProfileButton, gbc);
        logoutButton = new JButton();
        logoutButton.setBackground(new Color(-2209206));
        logoutButton.setForeground(new Color(-1));
        logoutButton.setText("Logout");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 11;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 0, 0, 0);
        panel.add(logoutButton, gbc);
        notificationsButton = new JButton();
        notificationsButton.setBackground(new Color(-16440538));
        notificationsButton.setForeground(new Color(-394241));
        notificationsButton.setText("`0` Notifications");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(15, 0, 30, 0);
        panel.add(notificationsButton, gbc);
        accountsPanel = new JPanel();
        accountsPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
        accountsPanel.setBackground(new Color(-5131595));
        accountsPanel.setMaximumSize(new Dimension(600, 400));
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 1;
        gbc.gridwidth = 3;
        gbc.gridheight = 11;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(15, 0, 0, 0);
        panel.add(accountsPanel, gbc);
        lastLogin = new JLabel();
        lastLogin.setText("Reliable and trustworthy. Your money is safe with us.");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(30, 100, 0, 0);
        panel.add(lastLogin, gbc);
        todayDate = new JLabel();
        todayDate.setText("Sunday, 11th November 2022");
        gbc = new GridBagConstraints();
        gbc.gridx = 4;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(20, 30, 0, 0);
        panel.add(todayDate, gbc);
        left = new JButton();
        left.setBackground(new Color(-10525075));
        left.setForeground(new Color(-1));
        left.setText("<");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 6;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 80, 0, 0);
        panel.add(left, gbc);
        right = new JButton();
        right.setBackground(new Color(-10525075));
        right.setForeground(new Color(-1));
        right.setText(">");
        gbc = new GridBagConstraints();
        gbc.gridx = 5;
        gbc.gridy = 6;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 0, 60);
        panel.add(right, gbc);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return panel;
    }

}
