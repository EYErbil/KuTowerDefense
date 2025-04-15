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
import javax.swing.JToolBar;
import javax.swing.SwingConstants;

/**
 * Map editor screen for Tower Defense.
 * Allows creation and editing of game maps.
 */
public class MapEditorScreen extends JFrame {

    private static final long serialVersionUID = 1L;

    /**
     * Constructor for MapEditorScreen.
     */
    public MapEditorScreen() {
        // Basic frame setup
        this.setTitle("Tower Defense - Map Editor");
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setMinimumSize(new Dimension(1024, 768));
        this.setLocationRelativeTo(null);
    }

    /**
     * Initialize the map editor screen.
     */
    public void initialize() {
        // Set up the layout
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Add toolbar at the top
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);

        // Add toolbar buttons
        JButton newButton = new JButton("New Map");
        JButton openButton = new JButton("Open Map");
        JButton saveButton = new JButton("Save Map");
        JButton validateButton = new JButton("Validate Map");
        JButton exitButton = new JButton("Exit Editor");

        // Add action listener to exit button
        exitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose(); // Close this screen
            }
        });

        // Add buttons to toolbar
        toolBar.add(newButton);
        toolBar.addSeparator();
        toolBar.add(openButton);
        toolBar.addSeparator();
        toolBar.add(saveButton);
        toolBar.addSeparator();
        toolBar.add(validateButton);
        toolBar.addSeparator();
        toolBar.add(exitButton);

        mainPanel.add(toolBar, BorderLayout.NORTH);

        // Add map grid placeholder
        JPanel mapGridPanel = new JPanel();
        mapGridPanel.setBorder(BorderFactory.createTitledBorder("Map Grid"));

        // Add placeholder label
        JLabel placeholderLabel = new JLabel("Map Grid Placeholder", SwingConstants.CENTER);
        placeholderLabel.setFont(new Font("Arial", Font.BOLD, 18));
        mapGridPanel.add(placeholderLabel);

        mainPanel.add(mapGridPanel, BorderLayout.CENTER);

        // Add tile selector on the right
        JPanel tileSelectorPanel = new JPanel(new GridLayout(6, 1, 0, 10));
        tileSelectorPanel.setBorder(BorderFactory.createTitledBorder("Tile Types"));
        tileSelectorPanel.setPreferredSize(new Dimension(200, 500));

        // Add tile type buttons
        JButton pathButton = new JButton("Path Tile");
        JButton towerButton = new JButton("Tower Slot");
        JButton startButton = new JButton("Start Point");
        JButton endButton = new JButton("End Point");
        JButton obstacleButton = new JButton("Obstacle");
        JButton eraseButton = new JButton("Erase");

        // Add components to tile selector panel
        tileSelectorPanel.add(pathButton);
        tileSelectorPanel.add(towerButton);
        tileSelectorPanel.add(startButton);
        tileSelectorPanel.add(endButton);
        tileSelectorPanel.add(obstacleButton);
        tileSelectorPanel.add(eraseButton);

        mainPanel.add(tileSelectorPanel, BorderLayout.EAST);

        // Add status panel at the bottom
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBorder(BorderFactory.createTitledBorder("Status"));

        JLabel statusLabel = new JLabel("Ready to edit map", SwingConstants.LEFT);
        statusPanel.add(statusLabel, BorderLayout.WEST);

        JLabel positionLabel = new JLabel("Position: 0, 0", SwingConstants.RIGHT);
        statusPanel.add(positionLabel, BorderLayout.EAST);

        mainPanel.add(statusPanel, BorderLayout.SOUTH);

        // Add panel to frame
        this.add(mainPanel);
        this.pack();
    }
}