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
            outputArea.setText(""); // Clear the output
    
            // Capture user input before execution
            String modifiedCode = preprocessInput(code);
    
            // Save original System.out and System.in
            PrintStream originalOut = System.out;
            InputStream originalIn = System.in;
    
            // Create output capture stream
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            PrintStream captureStream = new PrintStream(outputStream);
            System.setOut(captureStream);
    
            // Set System.in with modified code
            ByteArrayInputStream inputStream = new ByteArrayInputStream((modifiedCode + "\nEND\n").getBytes());
            System.setIn(inputStream);
    
            // Run compiler
            PhaeCompiler.main(new String[0]);
    
            // Restore original System.out and System.in
            System.setOut(originalOut);
            System.setIn(originalIn);
    
            // Show output
            outputArea.setText(outputStream.toString().trim());
    
        } catch (Exception ex) {
            outputArea.setText("Error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private String preprocessInput(String code) {
        String[] lines = code.split("\n");
        StringBuilder modifiedCode = new StringBuilder();
    
        for (String line : lines) {
            if (line.trim().startsWith("input(") && line.trim().endsWith(");")) {
                // Extract variable name inside input()
                String varName = line.trim().substring(6, line.trim().length() - 2);
    
                // Show input dialog
                String userInput = JOptionPane.showInputDialog(this, 
                    "Enter value for " + varName + ":", 
                    "Input Required", JOptionPane.QUESTION_MESSAGE);
    
                if (userInput == null) userInput = ""; // Handle cancel
    
                // Replace input statement with direct assignment
                modifiedCode.append(varName).append(" = \"").append(userInput).append("\";\n");
            } else {
                modifiedCode.append(line).append("\n");
            }
        }
        return modifiedCode.toString();
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