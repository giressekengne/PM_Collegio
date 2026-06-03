package PM;

import java.awt.Color;
import java.text.SimpleDateFormat;
import java.awt.HeadlessException;
import java.awt.event.KeyEvent;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;


 // @author gigatore

public class checkIn extends javax.swing.JFrame {

    public checkIn() {
        initComponents();
        date();
        RoomFields();
    }

    public void date() {
        SimpleDateFormat dat = new SimpleDateFormat("yyyy-MM-dd ");
        Date d = new Date();
        cidTextField.setText(dat.format(d));
        cidTextField.setEditable(false);
        surnameTextField.requestFocus();
    }

    // validazione telefono
    private static final Pattern PATTERN_TELEFONO = Pattern.compile("\\d{10}");
    public static boolean validaNumeroTelefono(String numero) {
        return PATTERN_TELEFONO.matcher(numero).matches();
    }

    // per la validazione della mail
    private boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$";
        return Pattern.matches(emailRegex, email);
    }

    public int convInt(String num) {
        return Integer.parseInt(num.substring(1));
    }

    public String convAlfa(int num) {
        return "R0" + String.format("%03d", num);
    }

    public String convAlfaR(int num) {
        return "P0" + String.format("%03d", num);
    }

    public void RoomFields() {
        try (Connection c = DBConfig.getConnection()) {
            ResultSet rs;
            PreparedStatement p = c.prepareStatement("select room_id from Room where stato=?");
            p.setString(1, "disponibile");
            rs = p.executeQuery();
            while (rs.next()) {
                rnComboBox.addItem(convAlfa(((Number) rs.getObject(1)).intValue()));
            }

            if (rnComboBox.getItemCount() > 0) {
                int id_int = convInt(rnComboBox.getItemAt(rnComboBox.getSelectedIndex()));
                p = c.prepareStatement("select prezzo,letto_tipo,tipo from Room where room_id = ?");
                p.setInt(1, id_int);
                rs = p.executeQuery();
                if (rs.next()) {
                    rtComboBox.setSelectedItem(rs.getString("tipo"));
                    btComboBox.setSelectedItem(rs.getString("letto_tipo"));
                    priceTextField.setText(rs.getString("prezzo"));
                }
            } else {
                JOptionPane.showMessageDialog(this, "Nessuna camera disponibile al momento");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Errore caricamento camere: " + e.getMessage());
        }
    }

    public void userFields() {
        try (Connection c = DBConfig.getConnection();
             PreparedStatement p = c.prepareStatement(
                 "select U.cognome,U.nome,U.telefono,U.genere,I.via " +
                 "from User U join indirizzo I on U.indirizzo_id = I.indirizzo_id " +
                 "where email = ?")) {
            p.setString(1, emailTextField.getText());
            ResultSet rs = p.executeQuery();
            if (rs.next()) {
                surnameTextField.setText(rs.getString("cognome"));
                nameTextField.setText(rs.getString("nome"));
                genderComboBox.setSelectedItem(rs.getString("genere"));
                addressComboBox.setSelectedItem(rs.getString("via"));
                mobileTextField.setText(rs.getString("telefono"));
                surnameTextField.setEditable(false);
                nameTextField.setEditable(false);
                genderComboBox.setEditable(false);
                addressComboBox.setEditable(false);
                mobileTextField.setEditable(false);
            } else {
                JOptionPane.showMessageDialog(this, "User Not Found");
                emailTextField.setText("");
            }
        } catch (SQLException ex) {
            // silent as before
        }
    }

    public void prenota() {
        if (emailTextField.getText().equals("")) {
            JOptionPane.showMessageDialog(this, "email field  Requied");
            surnameTextField.requestFocus();
        } else if (surnameTextField.getText().equals("")) {
            JOptionPane.showMessageDialog(this, "surname field  Requied");
            nameTextField.requestFocus();
        } else if (nameTextField.getText().equals("")) {
            JOptionPane.showMessageDialog(this, "name field  Requied");
            mobileTextField.requestFocus();
        } else if (genderComboBox.getSelectedItem().equals("")) {
            JOptionPane.showMessageDialog(this, "gender field  Requied");
            addressComboBox.requestFocus();
        } else if (addressComboBox.getSelectedItem().equals("")) {
            JOptionPane.showMessageDialog(this, "address field  Requied");
            mobileTextField.requestFocus();
        } else if (mobileTextField.getText().length() != 10) {
            JOptionPane.showMessageDialog(this, "Mobile Number Should be 10 Digit");
            mobileTextField.requestFocus();
        } else if (cidTextField.getText().equals("")) {
            JOptionPane.showMessageDialog(this, " date field requied");
            rnComboBox.requestFocus();
        } else if (rnComboBox.getSelectedItem().equals("")) {
            JOptionPane.showMessageDialog(this, "Sorry, This Room Not avaible Select another room");
        } else if (rtComboBox.getSelectedItem().equals("")) {
            JOptionPane.showMessageDialog(this, "field requied");
        } else if (btComboBox.getSelectedItem().equals("")) {
            JOptionPane.showMessageDialog(this, "field requied");
        } else if (priceTextField.getText().equals("")) {
            JOptionPane.showMessageDialog(this, "field requied");
        } else {
            try (Connection c = DBConfig.getConnection()) {
                c.setAutoCommit(false);

                String userCounter = "";
                String reservationSt;
                int roomId = convInt(rnComboBox.getItemAt(rnComboBox.getSelectedIndex()));
                int reservationInt = 0;
                int committente = 1;
                ResultSet rs;
                PreparedStatement p;

                p = c.prepareStatement("select Max(reservation_id) Rid from Reservation");
                rs = p.executeQuery();
                if (rs.next() && rs.getObject("Rid") != null) {
                    reservationInt = rs.getInt("Rid") + 1;
                } else {
                    reservationInt = 1;
                }
                reservationSt = convAlfaR(reservationInt);

                p = c.prepareStatement("select user_counter from User where email=?");
                p.setString(1, emailTextField.getText());
                rs = p.executeQuery();
                if (rs.next()) {
                    userCounter = rs.getString("user_counter");
                } else {
                    JOptionPane.showMessageDialog(this, "User non valido");
                    c.rollback();
                    return;
                }

                p = c.prepareStatement(
                    "insert into Reservation(reservation_id,user_id,committente_id,room_id,check_in,status,note)" +
                    "values(?,?,?,?,?,?,?)");
                p.setInt(1, reservationInt);
                p.setString(2, userCounter);
                p.setInt(3, committente);
                p.setInt(4, roomId);
                p.setString(5, cidTextField.getText());
                p.setString(6, "attiva");
                p.setNull(7, java.sql.Types.VARCHAR);
                p.executeUpdate();

                p = c.prepareStatement(
                    "insert into Fattura(reservation_id, importo, data_emissione, stato) values(?,?,?,?)");
                p.setInt(1, reservationInt);
                p.setDouble(2, 0);
                p.setString(3, new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date()));
                p.setString(4, "in attesa");
                p.executeUpdate();

                p = c.prepareStatement("update Room set stato=? where room_id=?");
                p.setString(1, "occupata");
                p.setInt(2, roomId);
                p.executeUpdate();

                c.commit();
                JOptionPane.showMessageDialog(this, "Room Alloted! Con Allote_ID: " + reservationSt);

                // aggiorna combo camere disponibili dopo il commit
                p = c.prepareStatement("select room_id from Room where stato=?");
                p.setString(1, "disponibile");
                rs = p.executeQuery();
                rnComboBox.removeAllItems();
                while (rs.next()) {
                    rnComboBox.addItem(convAlfa(((Number) rs.getObject(1)).intValue()));
                }
                if (rnComboBox.getItemCount() > 0) {
                    int id_int = convInt(rnComboBox.getItemAt(rnComboBox.getSelectedIndex()));
                    p = c.prepareStatement("select prezzo,letto_tipo,tipo from Room where room_id = ?");
                    p.setInt(1, id_int);
                    rs = p.executeQuery();
                    if (rs.next()) {
                        rtComboBox.setSelectedItem(rs.getString("tipo"));
                        btComboBox.setSelectedItem(rs.getString("letto_tipo"));
                        priceTextField.setText(rs.getString("prezzo"));
                    }
                }

                // pulisce i campi utente per la prossima prenotazione
                emailTextField.setText("");
                surnameTextField.setText("");
                nameTextField.setText("");
                mobileTextField.setText("");
                surnameTextField.setEditable(true);
                nameTextField.setEditable(true);
                mobileTextField.setEditable(true);
                genderComboBox.setSelectedIndex(0);
                addressComboBox.setSelectedIndex(0);

            } catch (SQLException e) {
                // il try-with-resources chiude la connessione → MySQL esegue rollback automatico
                JOptionPane.showMessageDialog(this, "Errore durante la prenotazione");
                e.printStackTrace();
            }
        }
    }

    public void priceItemChange() {
        double price;
        try {
            price = Double.parseDouble(priceTextField.getText());
        } catch (NumberFormatException e) {
            return;
        }
        try (Connection c = DBConfig.getConnection()) {
            PreparedStatement p = c.prepareStatement(
                "select room_id, letto_tipo,tipo from Room where prezzo=? and stato=?");
            p.setDouble(1, price);
            p.setString(2, "disponibile");
            ResultSet rs = p.executeQuery();
            rnComboBox.removeAllItems();
            rtComboBox.removeAllItems();
            btComboBox.removeAllItems();
            while (rs.next()) {
                rnComboBox.addItem(convAlfa(((Number) rs.getObject(1)).intValue()));
                rtComboBox.addItem(rs.getString("tipo"));
                btComboBox.addItem(rs.getString("letto_tipo"));
            }
            if (rnComboBox.getItemCount() > 0) {
                int id_int = convInt(rnComboBox.getItemAt(rnComboBox.getSelectedIndex()));
                p = c.prepareStatement("select prezzo,letto_tipo,tipo from Room where room_id = ?");
                p.setInt(1, id_int);
                rs = p.executeQuery();
                if (rs.next()) {
                    rtComboBox.setSelectedItem(rs.getString("tipo"));
                    btComboBox.setSelectedItem(rs.getString("letto_tipo"));
                    priceTextField.setText(rs.getString("prezzo"));
                }
            }
        } catch (Exception e) {
            //
        }
    }

    public void rtItemChange() {
        try (Connection c = DBConfig.getConnection()) {
            PreparedStatement p = c.prepareStatement("select room_id from Room where stato=?");
            p.setString(1, "disponibile");
            ResultSet rs = p.executeQuery();
            rnComboBox.removeAllItems();
            while (rs.next()) {
                rnComboBox.addItem(convAlfa(((Number) rs.getObject(1)).intValue()));
            }
            if (rnComboBox.getItemCount() > 0) {
                int id_int = convInt(rnComboBox.getItemAt(rnComboBox.getSelectedIndex()));
                p = c.prepareStatement("select prezzo,letto_tipo,tipo from Room where room_id = ?");
                p.setInt(1, id_int);
                rs = p.executeQuery();
                if (rs.next()) {
                    rtComboBox.setSelectedItem(rs.getString("tipo"));
                    btComboBox.setSelectedItem(rs.getString("letto_tipo"));
                    priceTextField.setText(rs.getString("prezzo"));
                }
            }
        } catch (Exception e) {
            //
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
        logoLabel = new javax.swing.JLabel();
        surnameLabel = new javax.swing.JLabel();
        surnameTextField = new javax.swing.JTextField();
        emailLabel = new javax.swing.JLabel();
        emailTextField = new javax.swing.JTextField();
        genderLabel = new javax.swing.JLabel();
        genderComboBox = new javax.swing.JComboBox<>();
        addressLabel = new javax.swing.JLabel();
        addressComboBox = new javax.swing.JComboBox<>();
        cidLabel = new javax.swing.JLabel();
        cidTextField = new javax.swing.JTextField();
        rnLabel = new javax.swing.JLabel();
        rtLabel = new javax.swing.JLabel();
        rtComboBox = new javax.swing.JComboBox<>();
        btLabel = new javax.swing.JLabel();
        btComboBox = new javax.swing.JComboBox<>();
        priceLabel = new javax.swing.JLabel();
        priceTextField = new javax.swing.JTextField();
        mobileLabel = new javax.swing.JLabel();
        mobileTextField = new javax.swing.JTextField();
        alloteButton = new javax.swing.JButton();
        clearButton = new javax.swing.JButton();
        nameLabel = new javax.swing.JLabel();
        nameTextField = new javax.swing.JTextField();
        rnComboBox = new javax.swing.JComboBox<>();
        searchButton = new javax.swing.JButton();
        bgLabel = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setUndecorated(true);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        closeButton.setFont(new java.awt.Font("Lucida Grande", 3, 14)); // NOI18N
        closeButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/image/close.png"))); // NOI18N
        closeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeButtonActionPerformed(evt);
            }
        });
        getContentPane().add(closeButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(1100, 0, -1, -1));

        logoLabel.setFont(new java.awt.Font(".SF NS Text", 3, 14)); // NOI18N
        logoLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/image/Customer Registration & Check IN.png"))); // NOI18N
        logoLabel.setText(" Checkin");
        getContentPane().add(logoLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(62, 6, 184, 71));

        surnameLabel.setFont(new java.awt.Font("Lucida Grande", 3, 14)); // NOI18N
        surnameLabel.setText("Surname");
        getContentPane().add(surnameLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 140, 261, 27));

        surnameTextField.setFont(new java.awt.Font("Lucida Grande", 3, 14)); // NOI18N
        getContentPane().add(surnameTextField, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 190, 261, 34));

        emailLabel.setFont(new java.awt.Font("Lucida Grande", 3, 14)); // NOI18N
        emailLabel.setText("Email");
        getContentPane().add(emailLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(290, 30, 90, 40));

        emailTextField.setFont(new java.awt.Font("Lucida Grande", 3, 14)); // NOI18N
        emailTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                emailTextFieldActionPerformed(evt);
            }
        });
        emailTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                emailTextFieldKeyReleased(evt);
            }
        });
        getContentPane().add(emailTextField, new org.netbeans.lib.awtextra.AbsoluteConstraints(390, 30, 261, 40));

        genderLabel.setFont(new java.awt.Font("Lucida Grande", 3, 14)); // NOI18N
        genderLabel.setText("Gender");
        getContentPane().add(genderLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 370, 253, 27));

        genderComboBox.setFont(new java.awt.Font("Lucida Grande", 3, 14)); // NOI18N
        genderComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "ND", "Maschio", "Femmina" }));
        getContentPane().add(genderComboBox, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 420, 260, 36));

        addressLabel.setFont(new java.awt.Font("Lucida Grande", 3, 14)); // NOI18N
        addressLabel.setText("Address");
        getContentPane().add(addressLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 490, 298, 24));

        addressComboBox.setFont(new java.awt.Font("Lucida Grande", 3, 14)); // NOI18N
        addressComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "GOLGI1", "GOLGI2", "CARDANO", "VOLTA", "GHISLIERI", "MAINO", "CAMPUS", "BORROMEO", "CAIROLI", "SPALLA", "DON BOSCO", "FRACCARO", "SAN SIRO", "ROGEREDO", "MALPENXA", "COLOSSEO", "TERMINE", "GASTALDI", "MESTRE", "EI (DE)", "EI (FR)", "EI (BE)", "EI (ES)", "EI (PRT)", "EI (GB)", "EE (USA)", "EE (CAN)", "EE (MEX)", "EE (BR)", "EE (ARG)", "EE (CN)", "EE (JPN)", "EE (KOR)", "EE (IND)", "EE (MA)", "EE (CMR)", "EE (SEN)", "EE (KEN)", "EE (ZA)" }));
        getContentPane().add(addressComboBox, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 540, 260, 37));

        cidLabel.setFont(new java.awt.Font("Lucida Grande", 3, 14)); // NOI18N
        cidLabel.setText("CkeckIn Date");
        getContentPane().add(cidLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(677, 132, 263, 24));

        cidTextField.setFont(new java.awt.Font("Lucida Grande", 3, 14)); // NOI18N
        getContentPane().add(cidTextField, new org.netbeans.lib.awtextra.AbsoluteConstraints(677, 189, 263, 34));

        rnLabel.setFont(new java.awt.Font("Lucida Grande", 3, 14)); // NOI18N
        rnLabel.setText("Room Number");
        getContentPane().add(rnLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(677, 260, 263, 27));

        rtLabel.setFont(new java.awt.Font("Lucida Grande", 3, 14)); // NOI18N
        rtLabel.setText("Room Type");
        getContentPane().add(rtLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(677, 368, 263, 26));

        rtComboBox.setFont(new java.awt.Font("Lucida Grande", 3, 14)); // NOI18N
        rtComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "singola", "doppia", "suite" }));
        getContentPane().add(rtComboBox, new org.netbeans.lib.awtextra.AbsoluteConstraints(677, 421, 263, 34));

        btLabel.setFont(new java.awt.Font("Lucida Grande", 3, 14)); // NOI18N
        btLabel.setText("Bed Type");
        getContentPane().add(btLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(677, 483, 263, 27));

        btComboBox.setFont(new java.awt.Font("Lucida Grande", 3, 14)); // NOI18N
        btComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "matrimoniale", "singolo", "king-size", " " }));
        getContentPane().add(btComboBox, new org.netbeans.lib.awtextra.AbsoluteConstraints(677, 537, 263, 36));

        priceLabel.setFont(new java.awt.Font("Lucida Grande", 3, 14)); // NOI18N
        priceLabel.setText("Price");
        getContentPane().add(priceLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(677, 596, 263, 27));

        priceTextField.setFont(new java.awt.Font("Lucida Grande", 3, 14)); // NOI18N
        priceTextField.addInputMethodListener(new java.awt.event.InputMethodListener() {
            public void caretPositionChanged(java.awt.event.InputMethodEvent evt) {
            }
            public void inputMethodTextChanged(java.awt.event.InputMethodEvent evt) {
                priceTextFieldInputMethodTextChanged(evt);
            }
        });
        getContentPane().add(priceTextField, new org.netbeans.lib.awtextra.AbsoluteConstraints(677, 643, 252, 34));

        mobileLabel.setFont(new java.awt.Font("Lucida Grande", 3, 14)); // NOI18N
        mobileLabel.setText("Mobile");
        getContentPane().add(mobileLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 600, 251, 27));

        mobileTextField.setFont(new java.awt.Font("Lucida Grande", 3, 14)); // NOI18N
        mobileTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                mobileTextFieldKeyReleased(evt);
            }
        });
        getContentPane().add(mobileTextField, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 650, 251, 34));

        alloteButton.setBackground(new java.awt.Color(0, 204, 153));
        alloteButton.setFont(new java.awt.Font("Lucida Grande", 3, 14)); // NOI18N
        alloteButton.setForeground(new java.awt.Color(153, 0, 0));
        alloteButton.setText("Allote");
        alloteButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                alloteButtonActionPerformed(evt);
            }
        });
        getContentPane().add(alloteButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(640, 940, 142, 40));

        clearButton.setBackground(new java.awt.Color(0, 204, 153));
        clearButton.setFont(new java.awt.Font("Lucida Grande", 3, 14)); // NOI18N
        clearButton.setForeground(new java.awt.Color(153, 0, 0));
        clearButton.setText("Clear");
        clearButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearButtonActionPerformed(evt);
            }
        });
        getContentPane().add(clearButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(840, 940, 128, 40));

        nameLabel.setFont(new java.awt.Font("Lucida Grande", 3, 14)); // NOI18N
        nameLabel.setText("Name");
        getContentPane().add(nameLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 260, 261, 27));

        nameTextField.setFont(new java.awt.Font("Lucida Grande", 3, 14)); // NOI18N
        getContentPane().add(nameTextField, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 310, 261, 34));

        rnComboBox.setFont(new java.awt.Font("Lucida Grande", 3, 14)); // NOI18N
        rnComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                rnComboBoxItemStateChanged(evt);
            }
        });
        getContentPane().add(rnComboBox, new org.netbeans.lib.awtextra.AbsoluteConstraints(677, 305, 263, 36));

        searchButton.setBackground(new java.awt.Color(0, 204, 153));
        searchButton.setFont(new java.awt.Font(".SF NS Text", 3, 14)); // NOI18N
        searchButton.setForeground(new java.awt.Color(153, 0, 0));
        searchButton.setText("Search");
        searchButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                searchButtonActionPerformed(evt);
            }
        });
        getContentPane().add(searchButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(680, 30, 100, 40));

        bgLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/image/background.png"))); // NOI18N
        bgLabel.setText("jLabel1");
        getContentPane().add(bgLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 1130, 1020));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void emailTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_emailTextFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_emailTextFieldActionPerformed

    private void searchButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchButtonActionPerformed
        // TODO add your handling code here:

        userFields();
    }//GEN-LAST:event_searchButtonActionPerformed

    private void clearButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearButtonActionPerformed
        emailTextField.setText("");
        surnameTextField.setText("");
        nameTextField.setText("");
        mobileTextField.setText("");
        surnameTextField.setEditable(true);
        nameTextField.setEditable(true);
        mobileTextField.setEditable(true);
        genderComboBox.setSelectedIndex(0);
        addressComboBox.setSelectedIndex(0);
        priceTextField.setText("");
    }//GEN-LAST:event_clearButtonActionPerformed

    private void alloteButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_alloteButtonActionPerformed
        // TODO add your handling code here:
        prenota();
    }//GEN-LAST:event_alloteButtonActionPerformed

    private void emailTextFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_emailTextFieldKeyReleased
        // TODO add your handling code here:
        emailTextField.setText(emailTextField.getText().toLowerCase());

        String email = emailTextField.getText();
        if (isValidEmail(email)) {
            emailLabel.setText("Email valida ✅");
            emailLabel.setForeground(Color.GREEN);
        } else {
            emailLabel.setText("Email non valida ❌");
            emailLabel.setForeground(Color.RED);
        }
        if(emailTextField.getText().equals("")){

            emailLabel.setText("");
        }
    }//GEN-LAST:event_emailTextFieldKeyReleased

    private void mobileTextFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_mobileTextFieldKeyReleased
        // TODO add your handling code here:


        String mobile = mobileTextField.getText();
        if (validaNumeroTelefono(mobile)) {
            mobileLabel.setText("Mobile valido ✅");
            mobileLabel.setForeground(Color.GREEN);
        } else {
            mobileLabel.setText("Mobile non valida ❌");
            mobileLabel.setForeground(Color.RED);
        }


    }//GEN-LAST:event_mobileTextFieldKeyReleased

    private void rnComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_rnComboBoxItemStateChanged
        if (evt.getStateChange() != java.awt.event.ItemEvent.SELECTED) return;
        if (rnComboBox.getSelectedIndex() < 0 || rnComboBox.getSelectedItem() == null) return;
        int id_int = convInt(rnComboBox.getItemAt(rnComboBox.getSelectedIndex()));
        try (Connection c = DBConfig.getConnection();
             PreparedStatement p = c.prepareStatement(
                 "select prezzo,letto_tipo,tipo from Room where room_id = ?")) {
            p.setInt(1, id_int);
            ResultSet rs = p.executeQuery();
            if (rs.next()) {
                rtComboBox.setSelectedItem(rs.getString("tipo"));
                btComboBox.setSelectedItem(rs.getString("letto_tipo"));
                priceTextField.setText(rs.getString("prezzo"));
            }
        } catch (SQLException e) {
            //
        }
    }//GEN-LAST:event_rnComboBoxItemStateChanged

    private void priceTextFieldInputMethodTextChanged(java.awt.event.InputMethodEvent evt) {//GEN-FIRST:event_priceTextFieldInputMethodTextChanged
        // TODO add your handling code here:
        priceItemChange();
    }//GEN-LAST:event_priceTextFieldInputMethodTextChanged

    private void closeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeButtonActionPerformed
        // TODO add your handling code here:
         dispose();
    }//GEN-LAST:event_closeButtonActionPerformed

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
            java.util.logging.Logger.getLogger(checkIn.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(checkIn.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(checkIn.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(checkIn.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new checkIn().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox<String> addressComboBox;
    private javax.swing.JLabel addressLabel;
    private javax.swing.JButton alloteButton;
    private javax.swing.JLabel bgLabel;
    private javax.swing.JComboBox<String> btComboBox;
    private javax.swing.JLabel btLabel;
    private javax.swing.JLabel cidLabel;
    private javax.swing.JTextField cidTextField;
    private javax.swing.JButton clearButton;
    private javax.swing.JButton closeButton;
    private javax.swing.JLabel emailLabel;
    private javax.swing.JTextField emailTextField;
    private javax.swing.JComboBox<String> genderComboBox;
    private javax.swing.JLabel genderLabel;
    private javax.swing.JLabel logoLabel;
    private javax.swing.JLabel mobileLabel;
    private javax.swing.JTextField mobileTextField;
    private javax.swing.JLabel nameLabel;
    private javax.swing.JTextField nameTextField;
    private javax.swing.JLabel priceLabel;
    private javax.swing.JTextField priceTextField;
    private javax.swing.JComboBox<String> rnComboBox;
    private javax.swing.JLabel rnLabel;
    private javax.swing.JComboBox<String> rtComboBox;
    private javax.swing.JLabel rtLabel;
    private javax.swing.JButton searchButton;
    private javax.swing.JLabel surnameLabel;
    private javax.swing.JTextField surnameTextField;
    // End of variables declaration//GEN-END:variables
}
