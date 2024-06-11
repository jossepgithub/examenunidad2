package com.example.ticTacToe;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class TicTacToe extends JFrame implements ActionListener {
    private JButton[] boton = new JButton[9];
    private boolean ursa = true; 
    private JTextField jug1, jug2;
    private JLabel turnLabel;
    private String jugador1, jugador2;
    private Connection connection;

    public TicTacToe() {
        setTitle("Tic Tac Toe");
        setSize(450, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel boardPanel = new JPanel();
        boardPanel.setLayout(new GridLayout(3, 3));
        for (int i = 0; i < 9; i++) {
            boton[i] = new JButton("");
            boton[i].setFont(new Font("Arial", Font.PLAIN, 65));
            boton[i].setFocusPainted(false);
            boton[i].addActionListener(this);
            boton[i].setEnabled(false);
            boardPanel.add(boton[i]);
        }

        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new GridLayout(3, 2));

        jug1= new JTextField();
        jug2 = new JTextField();
        controlPanel.add(new JLabel("ID jugador 1:"));
        controlPanel.add(jug1);
        controlPanel.add(new JLabel("ID jugador 2:"));
        controlPanel.add(jug2);

        JButton startButton = new JButton("empezar");
        startButton.addActionListener(e -> startGame());
        controlPanel.add(startButton);

        JButton cancelButton = new JButton("cancelar");
        cancelButton.addActionListener(e -> resetGame());
        controlPanel.add(cancelButton);

        turnLabel = new JLabel("Turno: ");
        add(turnLabel, BorderLayout.SOUTH);
        add(boardPanel, BorderLayout.CENTER);
        add(controlPanel, BorderLayout.NORTH);

        try {
            connection = DriverManager.getConnection("jdbc:sqlite:tictactoe.db");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void startGame() {
        jugador1 = jug1.getText();
        jugador2 = jug2.getText();
        if (jugador1.isEmpty() || jugador2.isEmpty()) {
            JOptionPane.showMessageDialog(this, "ingrese los nombres de los dos jugadores.");
            return;
        }
        for (JButton button : boton) {
            button.setEnabled(true);
            button.setText("");
        }
        ursa = true;
        turnLabel.setText("Turno: " + jugador1 + " (X)");
    }

    private void resetGame() {
        for (JButton button : boton) {
            button.setText("");
            button.setEnabled(false);
        }
        turnLabel.setText("Turno: ");
    }

    private void endGame(String winner) {
        for (JButton button : boton) {
            button.setEnabled(false);
        }
        JOptionPane.showMessageDialog(this, "Vencedor: " + winner);
        saveResult(winner);
    }

    private void saveResult(String winner) {
        String sql = "INSERT INTO resultados(nombre_partida, nombre_jugador1, nombre_jugador2, ganador, punto, estado) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, "Partida " + System.currentTimeMillis());
            pstmt.setString(2, jugador1);
            pstmt.setString(3, jugador2);
            pstmt.setString(4, winner);
            pstmt.setInt(5, 1);
            pstmt.setString(6, "Terminado");
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private boolean checkWinner() {
        int[][] winPositions = {
            {0, 1, 2}, {3, 4, 5}, {6, 7, 8},
            {0, 3, 6}, {1, 4, 7}, {2, 5, 8},
            {0, 4, 8}, {2, 4, 6}
        };

        for (int[] pos : winPositions) {
            if (boton[pos[0]].getText().equals(boton[pos[1]].getText()) &&
                boton[pos[1]].getText().equals(boton[pos[2]].getText()) &&
                !boton[pos[0]].getText().equals("")) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        JButton buttonClicked = (JButton) e.getSource();
        buttonClicked.setText(ursa ? "X" : "O");
        buttonClicked.setEnabled(false);
        if (checkWinner()) {
            endGame(ursa ? jugador1 : jugador2);
        } else {
            ursa = !ursa;
            turnLabel.setText("Turno: " + (ursa ? jugador1 + " (X)" :jugador2 + " (O)"));
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            TicTacToe frame = new TicTacToe();
            frame.setVisible(true);
        });
    }
}
