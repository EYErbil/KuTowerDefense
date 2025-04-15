package towerdefense.view.screens;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Options screen for Tower Defense.
 * Allows configuration of game settings.
 */
public class OptionsScreen extends JFrame {

    private static final long serialVersionUID = 1L;

    // UI Components
    private JSlider musicVolumeSlider;
    private JSlider sfxVolumeSlider;
    private JCheckBox fullscreenCheckbox;
    private JComboBox<String> difficultyComboBox;

    /**
     * Constructor for OptionsScreen.
     */
    public OptionsScreen() {
        // Basic frame setup
        this.setTitle("Tower Defense - Options");
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setMinimumSize(new Dimension(600, 500));
        this.setLocationRelativeTo(null);
    }

    /**
     * Initialize the options screen.
     */
    public void initialize() {
        // Set up the layout
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Create options panel with grid layout
        JPanel optionsPanel = new JPanel(new GridLayout(0, 1, 0, 20));
        optionsPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));

        // Add audio options
        JPanel audioPanel = new JPanel(new GridLayout(0, 1, 0, 10));
        audioPanel.setBorder(BorderFactory.createTitledBorder("Audio Settings"));

        // Music volume
        JPanel musicPanel = new JPanel(new BorderLayout());
        JLabel musicLabel = new JLabel("Music Volume:");
        musicVolumeSlider = new JSlider(0, 100, 75);
        musicVolumeSlider.setMajorTickSpacing(20);
        musicVolumeSlider.setMinorTickSpacing(5);
        musicVolumeSlider.setPaintTicks(true);
        musicVolumeSlider.setPaintLabels(true);
        final JLabel musicValueLabel = new JLabel("75%");

        musicVolumeSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                int value = musicVolumeSlider.getValue();
                musicValueLabel.setText(value + "%");
            }
        });

        musicPanel.add(musicLabel, BorderLayout.WEST);
        musicPanel.add(musicVolumeSlider, BorderLayout.CENTER);
        musicPanel.add(musicValueLabel, BorderLayout.EAST);

        // SFX volume
        JPanel sfxPanel = new JPanel(new BorderLayout());
        JLabel sfxLabel = new JLabel("SFX Volume:");
        sfxVolumeSlider = new JSlider(0, 100, 80);
        sfxVolumeSlider.setMajorTickSpacing(20);
        sfxVolumeSlider.setMinorTickSpacing(5);
        sfxVolumeSlider.setPaintTicks(true);
        sfxVolumeSlider.setPaintLabels(true);
        final JLabel sfxValueLabel = new JLabel("80%");

        sfxVolumeSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                int value = sfxVolumeSlider.getValue();
                sfxValueLabel.setText(value + "%");
            }
        });

        sfxPanel.add(sfxLabel, BorderLayout.WEST);
        sfxPanel.add(sfxVolumeSlider, BorderLayout.CENTER);
        sfxPanel.add(sfxValueLabel, BorderLayout.EAST);

        audioPanel.add(musicPanel);
        audioPanel.add(sfxPanel);

        // Add video options
        JPanel videoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        videoPanel.setBorder(BorderFactory.createTitledBorder("Video Settings"));

        fullscreenCheckbox = new JCheckBox("Fullscreen");
        videoPanel.add(fullscreenCheckbox);

        // Add gameplay options
        JPanel gameplayPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        gameplayPanel.setBorder(BorderFactory.createTitledBorder("Gameplay Settings"));

        JLabel difficultyLabel = new JLabel("Difficulty: ");
        difficultyLabel.setFont(new Font("Arial", Font.PLAIN, 14));

        String[] difficulties = { "Easy", "Normal", "Hard" };
        difficultyComboBox = new JComboBox<>(difficulties);
        difficultyComboBox.setSelectedIndex(1); // Default to Normal

        gameplayPanel.add(difficultyLabel);
        gameplayPanel.add(difficultyComboBox);

        // Add panels to options panel
        optionsPanel.add(audioPanel);
        optionsPanel.add(videoPanel);
        optionsPanel.add(gameplayPanel);

        mainPanel.add(optionsPanel, BorderLayout.CENTER);

        // Add buttons panel at the bottom
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");
        JButton defaultsButton = new JButton("Restore Defaults");

        // Add action listeners
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveOptions();
                dispose(); // Close this screen
            }
        });

        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose(); // Close this screen
            }
        });

        defaultsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                resetToDefaults();
            }
        });

        // Add buttons to panel
        buttonsPanel.add(defaultsButton);
        buttonsPanel.add(saveButton);
        buttonsPanel.add(cancelButton);

        mainPanel.add(buttonsPanel, BorderLayout.SOUTH);

        // Add panel to frame
        this.add(mainPanel);
        this.pack();
    }

    /**
     * Save current options.
     */
    private void saveOptions() {
        System.out.println("Saving options...");
        System.out.println("Music Volume: " + musicVolumeSlider.getValue() + "%");
        System.out.println("SFX Volume: " + sfxVolumeSlider.getValue() + "%");
        System.out.println("Fullscreen: " + fullscreenCheckbox.isSelected());
        System.out.println("Difficulty: " + difficultyComboBox.getSelectedItem());
        // This would call the options controller to save the options
    }

    /**
     * Reset options to default values.
     */
    private void resetToDefaults() {
        System.out.println("Resetting to defaults...");
        musicVolumeSlider.setValue(75);
        sfxVolumeSlider.setValue(80);
        fullscreenCheckbox.setSelected(false);
        difficultyComboBox.setSelectedIndex(1); // Normal
    }
}