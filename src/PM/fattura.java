package PM;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;

// @author gigatore

public class fattura extends javax.swing.JFrame {

    Connection con;
    PreparedStatement pst;
    int fatturaId;

    public fattura(int fatturaId) {
        this.fatturaId = fatturaId;
        initComponents();
        Connect();
        loadFattura();
    }

    public void Connect() {
        try {
            con = DBConfig.getConnection();
        } catch (SQLException ex) {
            Logger.getLogger(fattura.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void loadFattura() {
        try {
            pst = con.prepareStatement(
                "SELECT F.fattura_id, F.reservation_id, U.nome, R.numero_stanza, " +
                "F.importo, F.data_emissione, F.stato, Res.status " +
                "FROM Fattura F " +
                "JOIN Reservation Res ON Res.reservation_id = F.reservation_id " +
                "JOIN User U ON U.user_counter = Res.user_id " +
                "JOIN Room R ON R.room_id = Res.room_id " +
                "WHERE F.fattura_id = ?");
            pst.setInt(1, fatturaId);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                idValueLabel.setText("# " + rs.getInt(1));
                resValueLabel.setText("P0" + String.format("%03d", rs.getInt(2)));
                clienteValueLabel.setText(rs.getString(3));
                cameraValueLabel.setText(String.valueOf(rs.getInt(4)));
                importoValueLabel.setText(String.format("€ %.2f", rs.getDouble(5)));
                dataValueLabel.setText(rs.getString(6));
                statoValueLabel.setText(rs.getString(7));
                aggiornaBotoniPerStato(rs.getString(7), rs.getString(8));
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Errore caricamento fattura");
        }
    }

    private void aggiornaBotoniPerStato(String stato, String reservationStato) {
        boolean fatturaPagabile = "in attesa".equals(stato);
        boolean reservationPagabile = isReservationPagabile(reservationStato);
        boolean pagabile = fatturaPagabile && reservationPagabile;
        pagaButton.setEnabled(pagabile);
        annullaButton.setEnabled(pagabile);
    }

    /** Una fattura e' pagabile solo se la prenotazione e' chiusa (completata o cancellata). */
    private static boolean isReservationPagabile(String reservationStato) {
        return "completata".equalsIgnoreCase(reservationStato)
            || "cancellata".equalsIgnoreCase(reservationStato);
    }

    public void paga() {
        try {
            pst = con.prepareStatement("SELECT metodo_id, nome FROM MetodoPagamento");
            ResultSet rs = pst.executeQuery();
            Map<String, Integer> metodiMap = new LinkedHashMap<>();
            DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
            while (rs.next()) {
                String nome = rs.getString("nome");
                metodiMap.put(nome, rs.getInt("metodo_id"));
                model.addElement(nome);
            }
            if (model.getSize() == 0) {
                JOptionPane.showMessageDialog(this, "Nessun metodo di pagamento disponibile nel DB.");
                return;
            }

            JComboBox<String> combo = new JComboBox<>(model);
            int result = JOptionPane.showConfirmDialog(this, combo,
                "Seleziona metodo di pagamento", JOptionPane.OK_CANCEL_OPTION);
            if (result != JOptionPane.OK_OPTION) return;

            String metodoNome = (String) combo.getSelectedItem();
            int metodoId = metodiMap.get(metodoNome);

            pst = con.prepareStatement("SELECT importo FROM Fattura WHERE fattura_id=?");
            pst.setInt(1, fatturaId);
            rs = pst.executeQuery();
            double importo = rs.next() ? rs.getDouble(1) : 0;

            pst = con.prepareStatement("SELECT MAX(pagamento_id) FROM Pagamento");
            rs = pst.executeQuery();
            int pagamentoId = (rs.next() && rs.getObject(1) != null) ? rs.getInt(1) + 1 : 1;

            pst = con.prepareStatement(
                "INSERT INTO Pagamento(pagamento_id, fattura_id, metodo_id, importo, data_pagamento) VALUES(?,?,?,?,?)");
            pst.setInt(1, pagamentoId);
            pst.setInt(2, fatturaId);
            pst.setInt(3, metodoId);
            pst.setDouble(4, importo);
            pst.setString(5, new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date()));
            pst.executeUpdate();

            pst = con.prepareStatement("UPDATE Fattura SET stato='pagato' WHERE fattura_id=?");
            pst.setInt(1, fatturaId);
            pst.executeUpdate();

            JOptionPane.showMessageDialog(this, "Pagamento registrato con successo!");
            dispose();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Errore durante il pagamento.");
            ex.printStackTrace();
        }
    }

    public void annulla() {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Annullare la fattura? Lo stato diventerà 'non pagato'.",
            "Conferma annullamento", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;
        try {
            pst = con.prepareStatement("UPDATE Fattura SET stato='non pagato' WHERE fattura_id=?");
            pst.setInt(1, fatturaId);
            pst.executeUpdate();
            JOptionPane.showMessageDialog(this, "Fattura annullata.");
            dispose();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Errore annullamento fattura.");
        }
    }

    private void initComponents() {
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setUndecorated(true);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        // Close button
        closeButton = new javax.swing.JButton();
        closeButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/image/close.png")));
        closeButton.addActionListener(e -> dispose());
        getContentPane().add(closeButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(760, 0, -1, -1));

        // Logo
        logoLabel = new javax.swing.JLabel("Fattura");
        logoLabel.setFont(new java.awt.Font("Lucida Grande", java.awt.Font.BOLD, 20));
        logoLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/image/manage.png")));
        getContentPane().add(logoLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 20, 250, 50));

        // --- Row: ID fattura ---
        idValueLabel = new javax.swing.JLabel("# ---");
        idValueLabel.setFont(new java.awt.Font("Lucida Grande", java.awt.Font.BOLD, 22));
        getContentPane().add(idValueLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 90, 300, 35));

        // --- Row: Prenotazione ---
        javax.swing.JLabel resLabel = new javax.swing.JLabel("Prenotazione:");
        resLabel.setFont(new java.awt.Font("Lucida Grande", java.awt.Font.BOLD, 13));
        getContentPane().add(resLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 145, 150, 28));
        resValueLabel = new javax.swing.JLabel("---");
        resValueLabel.setFont(new java.awt.Font("Lucida Grande", 0, 13));
        getContentPane().add(resValueLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(185, 145, 200, 28));

        // --- Row: Cliente ---
        javax.swing.JLabel clienteLabel = new javax.swing.JLabel("Cliente:");
        clienteLabel.setFont(new java.awt.Font("Lucida Grande", java.awt.Font.BOLD, 13));
        getContentPane().add(clienteLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 183, 150, 28));
        clienteValueLabel = new javax.swing.JLabel("---");
        clienteValueLabel.setFont(new java.awt.Font("Lucida Grande", 0, 13));
        getContentPane().add(clienteValueLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(185, 183, 300, 28));

        // --- Row: Camera ---
        javax.swing.JLabel cameraLabel = new javax.swing.JLabel("Camera:");
        cameraLabel.setFont(new java.awt.Font("Lucida Grande", java.awt.Font.BOLD, 13));
        getContentPane().add(cameraLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 221, 150, 28));
        cameraValueLabel = new javax.swing.JLabel("---");
        cameraValueLabel.setFont(new java.awt.Font("Lucida Grande", 0, 13));
        getContentPane().add(cameraValueLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(185, 221, 200, 28));

        // --- Row: Data ---
        javax.swing.JLabel dataLabel = new javax.swing.JLabel("Data emissione:");
        dataLabel.setFont(new java.awt.Font("Lucida Grande", java.awt.Font.BOLD, 13));
        getContentPane().add(dataLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 259, 150, 28));
        dataValueLabel = new javax.swing.JLabel("---");
        dataValueLabel.setFont(new java.awt.Font("Lucida Grande", 0, 13));
        getContentPane().add(dataValueLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(185, 259, 250, 28));

        // --- Row: Stato ---
        javax.swing.JLabel statoLabel = new javax.swing.JLabel("Stato:");
        statoLabel.setFont(new java.awt.Font("Lucida Grande", java.awt.Font.BOLD, 13));
        getContentPane().add(statoLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 297, 150, 28));
        statoValueLabel = new javax.swing.JLabel("---");
        statoValueLabel.setFont(new java.awt.Font("Lucida Grande", java.awt.Font.BOLD, 13));
        getContentPane().add(statoValueLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(185, 297, 200, 28));

        // --- Importo (grande, evidenziato) ---
        javax.swing.JLabel importoLabel = new javax.swing.JLabel("Totale:");
        importoLabel.setFont(new java.awt.Font("Lucida Grande", java.awt.Font.BOLD, 16));
        getContentPane().add(importoLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 345, 150, 40));
        importoValueLabel = new javax.swing.JLabel("€ 0.00");
        importoValueLabel.setFont(new java.awt.Font("Lucida Grande", java.awt.Font.BOLD, 26));
        importoValueLabel.setForeground(new java.awt.Color(0, 120, 60));
        getContentPane().add(importoValueLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(185, 338, 300, 50));

        // Separator line (visual trick: thin label)
        javax.swing.JLabel sep = new javax.swing.JLabel();
        sep.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(200, 200, 200)));
        getContentPane().add(sep, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 405, 740, 2));

        // Paga button
        pagaButton = new javax.swing.JButton("Paga");
        pagaButton.setBackground(new java.awt.Color(0, 204, 153));
        pagaButton.setFont(new java.awt.Font("Lucida Grande", java.awt.Font.BOLD, 16));
        pagaButton.setForeground(new java.awt.Color(0, 51, 0));
        pagaButton.addActionListener(e -> paga());
        getContentPane().add(pagaButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 425, 170, 50));

        // Annulla button
        annullaButton = new javax.swing.JButton("Annulla");
        annullaButton.setBackground(new java.awt.Color(204, 0, 0));
        annullaButton.setFont(new java.awt.Font("Lucida Grande", java.awt.Font.BOLD, 16));
        annullaButton.setForeground(java.awt.Color.WHITE);
        annullaButton.addActionListener(e -> annulla());
        getContentPane().add(annullaButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(220, 425, 170, 50));

        // Background (aggiunto per ULTIMO → finisce dietro a tutto)
        bgLabel = new javax.swing.JLabel();
        bgLabel.setBackground(new java.awt.Color(250, 250, 252));
        bgLabel.setOpaque(true);
        getContentPane().add(bgLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 800, 500));

        setSize(800, 500);
        setLocationRelativeTo(null);
    }

    private javax.swing.JButton closeButton;
    private javax.swing.JLabel logoLabel;
    private javax.swing.JLabel idValueLabel;
    private javax.swing.JLabel resValueLabel;
    private javax.swing.JLabel clienteValueLabel;
    private javax.swing.JLabel cameraValueLabel;
    private javax.swing.JLabel importoValueLabel;
    private javax.swing.JLabel dataValueLabel;
    private javax.swing.JLabel statoValueLabel;
    private javax.swing.JButton pagaButton;
    private javax.swing.JButton annullaButton;
    private javax.swing.JLabel bgLabel;
}
