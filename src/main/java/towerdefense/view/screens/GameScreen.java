package towerdefense.view.screens;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 * Game screen for Tower Defense.
 * Displays the game board, towers, enemies, and game controls.
 */
public class GameScreen extends JFrame {

    private static final long serialVersionUID = 1L;

    /**
     * Constructor for GameScreen.
     */
    public GameScreen() {
        // Basic frame setup
        this.setTitle("Tower Defense - Game");
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setMinimumSize(new Dimension(1024, 768));
        this.setLocationRelativeTo(null);
    }

    /**
     * Initialize the game screen.
     */
    public void initialize() {
        // Set up the layout
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Add placeholder for game board
        JPanel gameBoardPanel = new JPanel();
        gameBoardPanel.setBorder(BorderFactory.createTitledBorder("Game Board"));
        gameBoardPanel.setPreferredSize(new Dimension(600, 500));

        // Add placeholder label
        JLabel placeholderLabel = new JLabel("Game Board Placeholder", SwingConstants.CENTER);
        placeholderLabel.setFont(new Font("Arial", Font.BOLD, 18));
        gameBoardPanel.add(placeholderLabel);

        mainPanel.add(gameBoardPanel, BorderLayout.CENTER);

        // Add control panel on the right
        JPanel controlPanel = new JPanel(new GridLayout(6, 1, 0, 10));
        controlPanel.setBorder(BorderFactory.createTitledBorder("Controls"));
        controlPanel.setPreferredSize(new Dimension(200, 500));

        // Add game information
        JLabel waveLabel = new JLabel("Wave: 1/10");
        JLabel goldLabel = new JLabel("Gold: 100");
        JLabel livesLabel = new JLabel("Lives: 20");

        // Add buttons
        JButton pauseButton = new JButton("Pause");
        JButton optionsButton = new JButton("Options");
        JButton quitButton = new JButton("Quit to Main Menu");

        // Add action listener to quit button
        quitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose(); // Close this screen
            }
        });

        // Add components to control panel
        controlPanel.add(waveLabel);
        controlPanel.add(goldLabel);
        controlPanel.add(livesLabel);
        controlPanel.add(pauseButton);
        controlPanel.add(optionsButton);
        controlPanel.add(quitButton);

        mainPanel.add(controlPanel, BorderLayout.EAST);

        // Add placeholder for tower selection at the bottom
        JPanel towerPanel = new JPanel(new GridLayout(1, 5, 10, 0));
        towerPanel.setBorder(BorderFactory.createTitledBorder("Towers"));
        towerPanel.setPreferredSize(new Dimension(600, 100));

        // Add placeholder tower buttons
        for (int i = 1; i <= 5; i++) {
            JButton towerButton = new JButton("Tower " + i);
            towerPanel.add(towerButton);
        }

        mainPanel.add(towerPanel, BorderLayout.SOUTH);

        // Add panel to frame
        this.add(mainPanel);
        this.pack();
    }
}