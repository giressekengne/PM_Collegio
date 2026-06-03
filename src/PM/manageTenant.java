/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package PM;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

// @author gigatore

public class manageTenant extends javax.swing.JFrame {

    Connection con;
    PreparedStatement pst;

    public manageTenant() {
        initComponents();
        Connect();
        initComboBoxes();
        autoID();
        loadTable();
        updateButton.setEnabled(false);
        tenantTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int row = tenantTable.getSelectedRow();
                if (row >= 0) {
                    int codCommittente = (int) ((DefaultTableModel) tenantTable.getModel()).getValueAt(row, 0);
                    loadFields(codCommittente);
                    updateButton.setEnabled(true);
                    addButton.setEnabled(false);
                }
            }
        });
    }

    public void Connect() {
        try {
            con = DBConfig.getConnection();
        } catch (SQLException ex) {
            Logger.getLogger(manageTenant.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void autoID() {
        try {
            pst = con.prepareStatement("SELECT MAX(codCommittente) FROM Committente");
            ResultSet rs = pst.executeQuery();
            if (rs.next() && rs.getObject(1) != null) {
                idTextField.setText(String.valueOf(rs.getInt(1) + 1));
            } else {
                idTextField.setText("1");
            }
        } catch (SQLException ex) {
            idTextField.setText("1");
        }
    }

    public void initComboBoxes() {
        try {
            // adminComboBox: email degli utenti non-clienti (role_id != 4)
            adminComboBox.removeAllItems();
            pst = con.prepareStatement("SELECT email FROM User WHERE ruolo != 4 AND stato = 'attivo'");
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                adminComboBox.addItem(rs.getString("email"));
            }

            // addressComboBox: indirizzi dalla tabella Indirizzo
            addressComboBox.removeAllItems();
            pst = con.prepareStatement("SELECT via FROM indirizzo ORDER BY via");
            rs = pst.executeQuery();
            while (rs.next()) {
                addressComboBox.addItem(rs.getString("via"));
            }
        } catch (SQLException ex) {
            Logger.getLogger(manageTenant.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void loadTable() {
        try {
            DefaultTableModel model = (DefaultTableModel) tenantTable.getModel();
            model.setRowCount(0);
            pst = con.prepareStatement(
                "SELECT C.codCommittente, C.ragione_Sociale, U.email, C.email " +
                "FROM Committente C " +
                "LEFT JOIN User U ON U.user_counter = C.gestore");
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt(1),
                    rs.getString(2),
                    rs.getString(3),
                    rs.getString(4)
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Errore caricamento committenti");
        }
    }

    public void loadFields(int codCommittente) {
        try {
            pst = con.prepareStatement(
                "SELECT C.codCommittente, C.ragione_Sociale, U.email, C.email, C.telefono, I.via " +
                "FROM Committente C " +
                "LEFT JOIN User U ON U.user_counter = C.gestore " +
                "LEFT JOIN indirizzo I ON I.indirizzo_id = C.indirizzo_id " +
                "WHERE C.codCommittente = ?");
            pst.setInt(1, codCommittente);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                idTextField.setText(String.valueOf(rs.getInt(1)));
                idTextField.setEditable(false);
                rsTextField.setText(rs.getString(2));
                adminComboBox.setSelectedItem(rs.getString(3));
                emailTextField.setText(rs.getString(4));
                telTextField.setText(rs.getString(5));
                addressComboBox.setSelectedItem(rs.getString(6));
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Errore caricamento dati committente");
        }
    }

    public void clearFields() {
        autoID();
        idTextField.setEditable(true);
        rsTextField.setText("");
        emailTextField.setText("");
        telTextField.setText("");
        if (adminComboBox.getItemCount() > 0) adminComboBox.setSelectedIndex(0);
        if (addressComboBox.getItemCount() > 0) addressComboBox.setSelectedIndex(0);
        tenantTable.clearSelection();
        addButton.setEnabled(true);
        updateButton.setEnabled(false);
    }

    private String resolveGestoreCounter(String email) throws SQLException {
        if (email == null || email.isEmpty()) return null;
        pst = con.prepareStatement("SELECT user_counter FROM User WHERE email=?");
        pst.setString(1, email);
        ResultSet rs = pst.executeQuery();
        return rs.next() ? rs.getString("user_counter") : null;
    }

    private int resolveIndirizzoId(String via) throws SQLException {
        if (via == null) return 1;
        pst = con.prepareStatement("SELECT indirizzo_id FROM indirizzo WHERE via=?");
        pst.setString(1, via);
        ResultSet rs = pst.executeQuery();
        return rs.next() ? rs.getInt(1) : 1;
    }

    public void addCommittente() {
        if (rsTextField.getText().equals("") || emailTextField.getText().equals("")) {
            JOptionPane.showMessageDialog(this, "Ragione Sociale ed Email sono obbligatori");
            return;
        }
        try {
            String gestoreCounter = resolveGestoreCounter((String) adminComboBox.getSelectedItem());
            int indirizzoId = resolveIndirizzoId((String) addressComboBox.getSelectedItem());
            int codCommittente = Integer.parseInt(idTextField.getText());

            pst = con.prepareStatement(
                "INSERT INTO Committente(codCommittente, ragione_Sociale, gestore, email, telefono, indirizzo_id) VALUES(?,?,?,?,?,?)");
            pst.setInt(1, codCommittente);
            pst.setString(2, rsTextField.getText());
            pst.setString(3, gestoreCounter);
            pst.setString(4, emailTextField.getText());
            pst.setString(5, telTextField.getText());
            pst.setInt(6, indirizzoId);
            pst.executeUpdate();
            JOptionPane.showMessageDialog(this, "Committente aggiunto con successo");
            loadTable();
            clearFields();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Errore inserimento committente");
        }
    }

    public void updateCommittente() {
        if (idTextField.getText().equals("")) {
            JOptionPane.showMessageDialog(this, "Selezionare prima un committente dalla tabella");
            return;
        }
        try {
            String gestoreCounter = resolveGestoreCounter((String) adminComboBox.getSelectedItem());
            int indirizzoId = resolveIndirizzoId((String) addressComboBox.getSelectedItem());
            int codCommittente = Integer.parseInt(idTextField.getText());

            pst = con.prepareStatement(
                "UPDATE Committente SET ragione_Sociale=?, gestore=?, email=?, telefono=?, indirizzo_id=? WHERE codCommittente=?");
            pst.setString(1, rsTextField.getText());
            pst.setString(2, gestoreCounter);
            pst.setString(3, emailTextField.getText());
            pst.setString(4, telTextField.getText());
            pst.setInt(5, indirizzoId);
            pst.setInt(6, codCommittente);
            pst.executeUpdate();
            JOptionPane.showMessageDialog(this, "Committente aggiornato con successo");
            loadTable();
            clearFields();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Errore aggiornamento committente");
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

        logoLabel = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tenantTable = new javax.swing.JTable();
        closeButton = new javax.swing.JButton();
        idLabel = new javax.swing.JLabel();
        rsLabel = new javax.swing.JLabel();
        adminLabel = new javax.swing.JLabel();
        emailLabel = new javax.swing.JLabel();
        telLabel = new javax.swing.JLabel();
        addressLabel = new javax.swing.JLabel();
        addButton = new javax.swing.JButton();
        updateButton = new javax.swing.JButton();
        clearButton = new javax.swing.JButton();
        idTextField = new javax.swing.JTextField();
        rsTextField = new javax.swing.JTextField();
        adminComboBox = new javax.swing.JComboBox<>();
        emailTextField = new javax.swing.JTextField();
        telTextField = new javax.swing.JTextField();
        addressComboBox = new javax.swing.JComboBox<>();
        bgLabel = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setUndecorated(true);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        logoLabel.setFont(new java.awt.Font(".SF NS Text", 3, 14)); // NOI18N
        logoLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/image/manage.png"))); // NOI18N
        logoLabel.setText("Manage Tenant");
        getContentPane().add(logoLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(21, 38, 193, -1));

        tenantTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Committente", "Ragione Sociale", "Admin", "Email"
            }
        ));
        jScrollPane1.setViewportView(tenantTable);

        getContentPane().add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(21, 126, 1034, 366));

        closeButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/image/close.png"))); // NOI18N
        closeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeButtonActionPerformed(evt);
            }
        });
        getContentPane().add(closeButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(1069, 6, -1, -1));

        idLabel.setText("Cod Committente");
        getContentPane().add(idLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(21, 553, 119, 32));

        rsLabel.setText("Ragione Sociale");
        getContentPane().add(rsLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(186, 553, 125, 32));

        adminLabel.setText("Gestore");
        getContentPane().add(adminLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(370, 553, 148, 32));

        emailLabel.setText("Email");
        getContentPane().add(emailLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(578, 553, 139, 31));

        telLabel.setText("Telefono");
        getContentPane().add(telLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(762, 554, 126, 28));

        addressLabel.setText("Indirizzo");
        getContentPane().add(addressLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(915, 554, 140, 28));

        addButton.setBackground(new java.awt.Color(0, 204, 153));
        addButton.setFont(new java.awt.Font(".SF NS Text", 3, 14)); // NOI18N
        addButton.setForeground(new java.awt.Color(153, 0, 0));
        addButton.setText("Add");
        addButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addButtonActionPerformed(evt);
            }
        });
        getContentPane().add(addButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(619, 706, 98, 31));

        updateButton.setBackground(new java.awt.Color(0, 204, 153));
        updateButton.setFont(new java.awt.Font(".SF NS Text", 3, 14)); // NOI18N
        updateButton.setForeground(new java.awt.Color(153, 0, 0));
        updateButton.setText("Update");
        updateButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                updateButtonActionPerformed(evt);
            }
        });
        getContentPane().add(updateButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(798, 704, 90, 34));

        clearButton.setBackground(new java.awt.Color(0, 204, 153));
        clearButton.setFont(new java.awt.Font(".SF NS Text", 3, 14)); // NOI18N
        clearButton.setForeground(new java.awt.Color(153, 0, 0));
        clearButton.setText("Clear");
        clearButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearButtonActionPerformed(evt);
            }
        });
        getContentPane().add(clearButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(963, 704, 92, 34));
        getContentPane().add(idTextField, new org.netbeans.lib.awtextra.AbsoluteConstraints(21, 603, 119, 33));
        getContentPane().add(rsTextField, new org.netbeans.lib.awtextra.AbsoluteConstraints(186, 603, 125, 33));

        adminComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        getContentPane().add(adminComboBox, new org.netbeans.lib.awtextra.AbsoluteConstraints(370, 600, 135, 33));
        getContentPane().add(emailTextField, new org.netbeans.lib.awtextra.AbsoluteConstraints(570, 600, 130, 30));
        getContentPane().add(telTextField, new org.netbeans.lib.awtextra.AbsoluteConstraints(760, 600, 126, 30));

        addressComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        getContentPane().add(addressComboBox, new org.netbeans.lib.awtextra.AbsoluteConstraints(920, 600, 130, 28));

        bgLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/image/all pages background.png"))); // NOI18N
        getContentPane().add(bgLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 1100, 760));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void addButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addButtonActionPerformed
        addCommittente();
    }//GEN-LAST:event_addButtonActionPerformed

    private void closeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeButtonActionPerformed
        dispose();
    }//GEN-LAST:event_closeButtonActionPerformed

    private void updateButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_updateButtonActionPerformed
        updateCommittente();
    }//GEN-LAST:event_updateButtonActionPerformed

    private void clearButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearButtonActionPerformed
        clearFields();
    }//GEN-LAST:event_clearButtonActionPerformed

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
            java.util.logging.Logger.getLogger(manageTenant.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(manageTenant.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(manageTenant.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(manageTenant.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new manageTenant().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.JComboBox<String> addressComboBox;
    private javax.swing.JLabel addressLabel;
    private javax.swing.JComboBox<String> adminComboBox;
    private javax.swing.JLabel adminLabel;
    private javax.swing.JLabel bgLabel;
    private javax.swing.JButton clearButton;
    private javax.swing.JButton closeButton;
    private javax.swing.JLabel emailLabel;
    private javax.swing.JTextField emailTextField;
    private javax.swing.JLabel idLabel;
    private javax.swing.JTextField idTextField;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel logoLabel;
    private javax.swing.JLabel rsLabel;
    private javax.swing.JTextField rsTextField;
    private javax.swing.JLabel telLabel;
    private javax.swing.JTextField telTextField;
    private javax.swing.JTable tenantTable;
    private javax.swing.JButton updateButton;
    // End of variables declaration//GEN-END:variables
}
