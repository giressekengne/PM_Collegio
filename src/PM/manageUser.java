package PM;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;


 // @author gigatore

public class manageUser extends javax.swing.JFrame {

    /**
     * Creates new form logout
     */
    public manageUser() {
        initComponents();
        Connect();
        initComboBoxes();
        loadTable();

        searchButton.addActionListener(e -> userFields());
        RefreshButton.addActionListener(e -> loadTable());
        clearButton.addActionListener(e -> clearFields());
        updateButton.addActionListener(e -> updateUser());
        addButton.addActionListener(e -> addUser());

        updateButton.setEnabled(false);
        addButton.setEnabled(true);

        jTable1.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int row = jTable1.getSelectedRow();
                if (row < 0) {
                    return;
                }
                String email = (String) ((DefaultTableModel) jTable1.getModel()).getValueAt(row, 1);
                if (email == null || email.isEmpty()) {
                    return;
                }
                emailTextField.setText(email);
                userFields();
                addButton.setEnabled(false);
                updateButton.setEnabled(true);
            }
        });

        if ("U".equalsIgnoreCase(Session.roleType)) {
            searchButton.setVisible(false);
            RefreshButton.setVisible(false);
            addButton.setVisible(false);
            jScrollPane1.setVisible(false);
            emailTextField.setText(Session.email);
            emailTextField.setEditable(false);
            userFields();
            updateButton.setEnabled(true);
        }
    }
    
    Connection con;
    PreparedStatement pst;
    
    //validazione telefono
    private static final Pattern PATTERN_TELEFONO = Pattern.compile("\\d{10}"); // Regex precompilata
    public static boolean validaNumeroTelefono(String numero) {
        Matcher matcher = PATTERN_TELEFONO.matcher(numero);
        return matcher.matches();
    }
    
    // per la validazione della mail
    private boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$";
        return Pattern.matches(emailRegex, email);
    }
    
    public void Connect()
    {
        try{
            con = DBConfig.getConnection();
        } catch (SQLException ex) {
            Logger.getLogger(manageRoom.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
    
    public void userFields() {
        ResultSet rs;
        try {
            pst = con.prepareStatement(
                "SELECT U.cognome, U.nome, U.telefono, U.genere, U.access, U.stato, U.recupero, U.response, I.via " +
                "FROM User U JOIN indirizzo I ON U.indirizzo_id = I.indirizzo_id WHERE U.email = ?");
            pst.setString(1, emailTextField.getText());
            rs = pst.executeQuery();
            if (rs.next()) {
                surnameTextField.setText(rs.getString("cognome"));
                nameTextField.setText(rs.getString("nome"));
                mobileTextField.setText(rs.getString("telefono"));
                pwTextField.setText(rs.getString("access"));
                responseTextField.setText(rs.getString("response"));
                genderComboBox.setSelectedItem(rs.getString("genere"));
                addressComboBox.setSelectedItem(rs.getString("via"));
                statusComboBox.setSelectedItem(rs.getString("stato"));
                qstComboBox.setSelectedItem(rs.getString("recupero"));
            } else {
                JOptionPane.showMessageDialog(this, "Utente non trovato");
                emailTextField.setText("");
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Errore ricerca utente");
        }
    }

    public void initComboBoxes() {
        statusComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[]{"attivo", "disattivato", "attesa"}));
        genderComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[]{"ND", "Maschio", "Femmina"}));
        qstComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[]{"giocatore preferito?", "disciplina preferita?", "cantante preferito?", "libro preferita?", "programma televisivo preferito?"}));
        addressComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[]{"GOLGI1", "GOLGI2", "CARDANO", "VOLTA", "GHISLIERI", "MAINO", "CAMPUS", "BORROMEO", "CAIROLI", "SPALLA", "DON BOSCO", "FRACCARO", "SAN SIRO", "ROGEREDO", "MALPENXA", "COLOSSEO", "TERMINE", "GASTALDI", "MESTRE", "EI (DE)", "EI (FR)", "EI (BE)", "EI (ES)", "EI (PRT)", "EI (GB)", "EE (USA)", "EE (CAN)", "EE (MEX)", "EE (BR)", "EE (ARG)", "EE (CN)", "EE (JPN)", "EE (KOR)", "EE (IND)", "EE (MA)", "EE (CMR)", "EE (SEN)", "EE (KEN)", "EE (ZA)"}));
    }

    public void loadTable() {
        try {
            DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
            model.setRowCount(0);
            if ("U".equalsIgnoreCase(Session.roleType)) {
                pst = con.prepareStatement("SELECT nome, email, access, response, stato FROM User WHERE user_counter=?");
                pst.setString(1, Session.userCounter);
            } else {
                pst = con.prepareStatement("SELECT nome, email, access, response, stato FROM User");
            }
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getString("nome"),
                    rs.getString("email"),
                    rs.getString("access"),
                    rs.getString("response"),
                    rs.getString("stato")
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Errore caricamento utenti");
        }
    }

    public void clearFields() {
        nameTextField.setText("");
        surnameTextField.setText("");
        if (!"U".equalsIgnoreCase(Session.roleType)) {
            emailTextField.setText("");
        }
        pwTextField.setText("");
        mobileTextField.setText("");
        responseTextField.setText("");
        statusComboBox.setSelectedIndex(0);
        addressComboBox.setSelectedIndex(0);
        qstComboBox.setSelectedIndex(0);
        genderComboBox.setSelectedIndex(0);
        jTable1.clearSelection();
        if (!"U".equalsIgnoreCase(Session.roleType)) {
            addButton.setEnabled(true);
            updateButton.setEnabled(false);
        }
    }

    public void updateUser() {
        if (emailTextField.getText().equals("")) {
            JOptionPane.showMessageDialog(this, "Cercare prima un utente per email");
            return;
        }
        try {
            pst = con.prepareStatement(
                "UPDATE User SET nome=?, cognome=?, access=?, telefono=?, stato=?, recupero=?, response=?, genere=? WHERE email=?");
            pst.setString(1, nameTextField.getText());
            pst.setString(2, surnameTextField.getText());
            pst.setString(3, pwTextField.getText());
            pst.setString(4, mobileTextField.getText());
            pst.setString(5, statusComboBox.getItemAt(statusComboBox.getSelectedIndex()));
            pst.setString(6, qstComboBox.getItemAt(qstComboBox.getSelectedIndex()));
            pst.setString(7, responseTextField.getText());
            pst.setString(8, genderComboBox.getItemAt(genderComboBox.getSelectedIndex()));
            pst.setString(9, emailTextField.getText());
            pst.executeUpdate();
            JOptionPane.showMessageDialog(this, "Utente aggiornato con successo");
            loadTable();
            clearFields();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Errore aggiornamento utente");
        }
    }

    public void addUser() {
        if (nameTextField.getText().equals("") || emailTextField.getText().equals("") || pwTextField.getText().equals("")) {
            JOptionPane.showMessageDialog(this, "Nome, Email e Password sono obbligatori");
            return;
        }
        try {
            pst = con.prepareStatement("SELECT * FROM User WHERE email=?");
            pst.setString(1, emailTextField.getText());
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                JOptionPane.showMessageDialog(this, "Email già in uso");
                return;
            }
            int indirizzoId = 1;
            pst = con.prepareStatement("SELECT indirizzo_id FROM indirizzo WHERE via=?");
            pst.setString(1, addressComboBox.getItemAt(addressComboBox.getSelectedIndex()));
            rs = pst.executeQuery();
            if (rs.next()) {
                indirizzoId = rs.getInt("indirizzo_id");
            }
            pst = con.prepareStatement(
                "INSERT INTO User(user_counter,nome,cognome,email,access,ruolo,committente_id,stato,telefono,indirizzo_id,recupero,response,genere) " +
                "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?)");
            pst.setString(1, "XXX" + nameTextField.getText().toUpperCase());
            pst.setString(2, nameTextField.getText());
            pst.setString(3, surnameTextField.getText());
            pst.setString(4, emailTextField.getText().toLowerCase());
            pst.setString(5, pwTextField.getText());
            pst.setInt(6, 4);
            pst.setInt(7, 1);
            pst.setString(8, statusComboBox.getItemAt(statusComboBox.getSelectedIndex()));
            pst.setString(9, mobileTextField.getText());
            pst.setInt(10, indirizzoId);
            pst.setString(11, qstComboBox.getItemAt(qstComboBox.getSelectedIndex()));
            pst.setString(12, responseTextField.getText());
            pst.setString(13, genderComboBox.getItemAt(genderComboBox.getSelectedIndex()));
            pst.executeUpdate();
            JOptionPane.showMessageDialog(this, "Utente aggiunto con successo");
            loadTable();
            clearFields();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Errore inserimento utente");
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        closeButton = new javax.swing.JButton();
        emailLabel = new javax.swing.JLabel();
        emailTextField = new javax.swing.JTextField();
        searchButton = new javax.swing.JButton();
        RefreshButton = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        logoLabel = new javax.swing.JLabel();
        nameLabel = new javax.swing.JLabel();
        surnameLabel = new javax.swing.JLabel();
        pwLabel = new javax.swing.JLabel();
        statusLabel = new javax.swing.JLabel();
        mobileLabel = new javax.swing.JLabel();
        addressLabel = new javax.swing.JLabel();
        recuperoLabel = new javax.swing.JLabel();
        responseLabel = new javax.swing.JLabel();
        genderLabel = new javax.swing.JLabel();
        addButton = new javax.swing.JButton();
        updateButton = new javax.swing.JButton();
        clearButton = new javax.swing.JButton();
        nameTextField = new javax.swing.JTextField();
        surnameTextField = new javax.swing.JTextField();
        pwTextField = new javax.swing.JTextField();
        statusComboBox = new javax.swing.JComboBox<>();
        mobileTextField = new javax.swing.JTextField();
        addressComboBox = new javax.swing.JComboBox<>();
        qstComboBox = new javax.swing.JComboBox<>();
        responseTextField = new javax.swing.JTextField();
        genderComboBox = new javax.swing.JComboBox<>();
        bgLabel = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setUndecorated(true);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        closeButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/image/close.png"))); // NOI18N
        closeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeButtonActionPerformed(evt);
            }
        });
        getContentPane().add(closeButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(1170, 0, -1, -1));

        emailLabel.setFont(new java.awt.Font("Lucida Grande", 3, 14)); // NOI18N
        emailLabel.setText("Search by Email");
        getContentPane().add(emailLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(340, 50, 120, 27));

        emailTextField.setFont(new java.awt.Font("Lucida Grande", 3, 14)); // NOI18N
        getContentPane().add(emailTextField, new org.netbeans.lib.awtextra.AbsoluteConstraints(520, 50, 180, 36));

        searchButton.setBackground(new java.awt.Color(0, 204, 153));
        searchButton.setFont(new java.awt.Font("Lucida Grande", 3, 14)); // NOI18N
        searchButton.setForeground(new java.awt.Color(153, 0, 0));
        searchButton.setText("Search");
        getContentPane().add(searchButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(740, 50, 100, 36));

        RefreshButton.setBackground(new java.awt.Color(0, 204, 153));
        RefreshButton.setFont(new java.awt.Font("Lucida Grande", 3, 14)); // NOI18N
        RefreshButton.setForeground(new java.awt.Color(153, 0, 0));
        RefreshButton.setText("Refresh");
        getContentPane().add(RefreshButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(880, 50, 100, 40));

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null}
            },
            new String [] {
                "Name", "Email", "Password", "Answer", "Status"
            }
        ));
        jScrollPane1.setViewportView(jTable1);

        getContentPane().add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 110, 1140, 530));

        logoLabel.setFont(new java.awt.Font(".SF NS Text", 3, 14)); // NOI18N
        logoLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/image/manage.png"))); // NOI18N
        logoLabel.setText("Manage User");
        getContentPane().add(logoLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 20, 190, 70));

        nameLabel.setFont(new java.awt.Font(".SF NS Text", 3, 14)); // NOI18N
        nameLabel.setText("Name");
        getContentPane().add(nameLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 680, 120, 30));

        surnameLabel.setFont(new java.awt.Font(".SF NS Text", 3, 14)); // NOI18N
        surnameLabel.setText("Surname");
        getContentPane().add(surnameLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(340, 680, 100, 30));

        pwLabel.setFont(new java.awt.Font(".SF NS Text", 3, 14)); // NOI18N
        pwLabel.setText("Password");
        getContentPane().add(pwLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(670, 680, 100, 30));

        statusLabel.setFont(new java.awt.Font(".SF NS Text", 3, 14)); // NOI18N
        statusLabel.setText("Status");
        getContentPane().add(statusLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 780, 110, 30));

        mobileLabel.setFont(new java.awt.Font(".SF NS Text", 3, 14)); // NOI18N
        mobileLabel.setText("Mobile");
        getContentPane().add(mobileLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(330, 780, 110, 30));

        addressLabel.setFont(new java.awt.Font(".SF NS Text", 3, 14)); // NOI18N
        addressLabel.setText("Address");
        getContentPane().add(addressLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(670, 770, 90, 30));

        recuperoLabel.setFont(new java.awt.Font(".SF NS Text", 3, 14)); // NOI18N
        recuperoLabel.setText("Recupero");
        getContentPane().add(recuperoLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 890, 120, 30));

        responseLabel.setFont(new java.awt.Font(".SF NS Text", 3, 14)); // NOI18N
        responseLabel.setText("Response");
        getContentPane().add(responseLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(330, 890, 100, 30));

        genderLabel.setFont(new java.awt.Font(".SF NS Text", 3, 14)); // NOI18N
        genderLabel.setText("Gender");
        getContentPane().add(genderLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(670, 880, 90, 30));

        addButton.setBackground(new java.awt.Color(0, 204, 153));
        addButton.setFont(new java.awt.Font(".SF NS Text", 3, 14)); // NOI18N
        addButton.setForeground(new java.awt.Color(153, 0, 0));
        addButton.setText("Add");
        getContentPane().add(addButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(1030, 710, 120, 40));

        updateButton.setBackground(new java.awt.Color(0, 204, 153));
        updateButton.setFont(new java.awt.Font(".SF NS Text", 3, 14)); // NOI18N
        updateButton.setForeground(new java.awt.Color(153, 0, 0));
        updateButton.setText("Update");
        getContentPane().add(updateButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(1030, 800, 120, 40));

        clearButton.setBackground(new java.awt.Color(0, 204, 153));
        clearButton.setFont(new java.awt.Font(".SF NS Text", 3, 14)); // NOI18N
        clearButton.setForeground(new java.awt.Color(153, 0, 0));
        clearButton.setText("Clear");
        getContentPane().add(clearButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(1030, 890, 120, 40));

        nameTextField.setFont(new java.awt.Font(".SF NS Text", 3, 14)); // NOI18N
        nameTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nameTextFieldActionPerformed(evt);
            }
        });
        getContentPane().add(nameTextField, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 720, 160, 30));

        surnameTextField.setFont(new java.awt.Font(".SF NS Text", 3, 14)); // NOI18N
        getContentPane().add(surnameTextField, new org.netbeans.lib.awtextra.AbsoluteConstraints(330, 720, 150, 30));

        pwTextField.setFont(new java.awt.Font(".SF NS Text", 3, 14)); // NOI18N
        getContentPane().add(pwTextField, new org.netbeans.lib.awtextra.AbsoluteConstraints(670, 720, 160, 30));

        statusComboBox.setFont(new java.awt.Font(".SF NS Text", 3, 14)); // NOI18N
        statusComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        getContentPane().add(statusComboBox, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 830, 160, 30));

        mobileTextField.setFont(new java.awt.Font(".SF NS Text", 3, 14)); // NOI18N
        getContentPane().add(mobileTextField, new org.netbeans.lib.awtextra.AbsoluteConstraints(330, 830, 150, 30));

        addressComboBox.setFont(new java.awt.Font(".SF NS Text", 3, 14)); // NOI18N
        addressComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        getContentPane().add(addressComboBox, new org.netbeans.lib.awtextra.AbsoluteConstraints(670, 820, 160, 30));

        qstComboBox.setFont(new java.awt.Font(".SF NS Text", 3, 14)); // NOI18N
        qstComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        getContentPane().add(qstComboBox, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 930, 160, 30));

        responseTextField.setFont(new java.awt.Font(".SF NS Text", 3, 14)); // NOI18N
        getContentPane().add(responseTextField, new org.netbeans.lib.awtextra.AbsoluteConstraints(330, 930, 150, 30));

        genderComboBox.setFont(new java.awt.Font(".SF NS Text", 3, 14)); // NOI18N
        genderComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        getContentPane().add(genderComboBox, new org.netbeans.lib.awtextra.AbsoluteConstraints(670, 920, 160, 30));

        bgLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/image/all pages background.png"))); // NOI18N
        getContentPane().add(bgLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 1200, 1010));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void closeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeButtonActionPerformed
        // TODO add your handling code here:
        dispose();
    }//GEN-LAST:event_closeButtonActionPerformed

    private void nameTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nameTextFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_nameTextFieldActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(manageUser.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(manageUser.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(manageUser.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(manageUser.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new manageUser().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton RefreshButton;
    private javax.swing.JButton addButton;
    private javax.swing.JComboBox<String> addressComboBox;
    private javax.swing.JLabel addressLabel;
    private javax.swing.JLabel bgLabel;
    private javax.swing.JButton clearButton;
    private javax.swing.JButton closeButton;
    private javax.swing.JLabel emailLabel;
    private javax.swing.JTextField emailTextField;
    private javax.swing.JComboBox<String> genderComboBox;
    private javax.swing.JLabel genderLabel;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JLabel logoLabel;
    private javax.swing.JLabel mobileLabel;
    private javax.swing.JTextField mobileTextField;
    private javax.swing.JLabel nameLabel;
    private javax.swing.JTextField nameTextField;
    private javax.swing.JLabel pwLabel;
    private javax.swing.JTextField pwTextField;
    private javax.swing.JComboBox<String> qstComboBox;
    private javax.swing.JLabel recuperoLabel;
    private javax.swing.JLabel responseLabel;
    private javax.swing.JTextField responseTextField;
    private javax.swing.JButton searchButton;
    private javax.swing.JComboBox<String> statusComboBox;
    private javax.swing.JLabel statusLabel;
    private javax.swing.JLabel surnameLabel;
    private javax.swing.JTextField surnameTextField;
    private javax.swing.JButton updateButton;
    // End of variables declaration//GEN-END:variables
}
