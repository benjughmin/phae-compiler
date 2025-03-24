import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

public class CompilerGUI extends JFrame {
    private JTextArea codeEditor;
    private JTextArea outputArea;
    private JButton runButton;

    public CompilerGUI() {
        // Set up the frame
        super("Phae Compiler");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Create the code editor
        codeEditor = new JTextArea();
        codeEditor.setFont(new Font("Monospaced", Font.PLAIN, 14));
        JScrollPane codeScrollPane = new JScrollPane(codeEditor);

        // Create the output area
        outputArea = new JTextArea();
        outputArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        outputArea.setEditable(false);
        JScrollPane outputScrollPane = new JScrollPane(outputArea);

        // Create the run button
        runButton = new JButton("Run");
        runButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                runCode();
            }
        });

        // Create panels to organize components
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(runButton);

        // Create a split pane to divide the editor and output areas
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, codeScrollPane, outputScrollPane);
        splitPane.setResizeWeight(0.7); // Give more space to the editor

        // Add components to the frame
        add(splitPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        // Set visible
        setVisible(true);
    }

    private void runCode() {
        try {
            // Get the code from the editor
            String code = codeEditor.getText();

            // Clear the output area
            outputArea.setText("");

            // Save the original System.out
            PrintStream originalOut = System.out;

            // Create a stream to capture output
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            PrintStream captureStream = new PrintStream(outputStream);

            // Redirect System.out to our capture stream
            System.setOut(captureStream);

            // Run the compiler with the input redirected
            InputStream originalIn = System.in;
            ByteArrayInputStream inputStream = new ByteArrayInputStream((code + "\nEND\n").getBytes());
            System.setIn(inputStream);

            // Call the main method of your compiler
            PhaeCompiler.main(new String[0]);

            // Restore the original streams
            System.setOut(originalOut);
            System.setIn(originalIn);

            // Get the captured output
            String output = outputStream.toString().trim();

            // Display the output
            outputArea.setText(output);

        } catch (Exception ex) {
            // Display any errors
            outputArea.setText("Error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        // Create the GUI on the Event Dispatch Thread
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new CompilerGUI();
            }
        });
    }
}