import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;

public class VocabularyQuiz extends JFrame {

    private List<Word> words;
    private int currentIndex;
    private JLabel wordLabel;
    private JTextField inputField;
    private JButton submitButton;
    private JLabel resultLabel;
    private JComboBox<String> apiSelector;
    private JComboBox<String> mainMenu;

    public VocabularyQuiz() {
        wordLabel = new JLabel();
        inputField = new JTextField();
        submitButton = new JButton("Submit");
        resultLabel = new JLabel();
        apiSelector = new JComboBox<>();
        mainMenu = new JComboBox<>();

        String[] apiOptions = {"API 1", "API 2", "API 3", "API 4", "API 5", "API 6"};
        apiSelector.setModel(new DefaultComboBoxModel<>(apiOptions));
        apiSelector.setSelectedIndex(0);

        String[] mainMenuOptions = {"Select Action","View Vocabulary", "Take Quiz"};
        mainMenu.setModel(new DefaultComboBoxModel<>(mainMenuOptions));

        mainMenu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleMainMenuSelection();
            }
        });

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(6, 1, 10, 10));
        panel.add(mainMenu);
        panel.add(apiSelector);
        panel.add(wordLabel);
        panel.add(inputField);
        panel.add(submitButton);
        panel.add(resultLabel);

        add(panel);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 400);
        setTitle("English Vocabulary");
        setLocationRelativeTo(null);
        setVisible(true);

        loadWordsFromJson();
        setNextQuestion();
    }

    private void handleMainMenuSelection() {
        String selectedAction = (String) mainMenu.getSelectedItem();

        if ("View Vocabulary".equals(selectedAction)) {
            loadWordsFromJson();
            showVocabulary(); 
        } else if ("Take Quiz".equals(selectedAction)) {
            currentIndex = 0;
            loadWordsFromJson();
            setNextQuestion();
        }
    }

    private void showVocabulary() {
        StringBuilder vocabularyText = new StringBuilder("<html><body>");

        for (Word word : words) {
            vocabularyText.append("<b>").append(word.getWord()).append("</b>: ");
            for (Definition definition : word.getDefinitions()) {
                vocabularyText.append(definition.getPartOfSpeech()).append(": ").append(definition.getText()).append("<br>");
            }
            vocabularyText.append("<br>");
        }

        vocabularyText.append("</body></html>");

        JTextPane textPane = new JTextPane();
        textPane.setContentType("text/html");
        textPane.setText(vocabularyText.toString());
        textPane.setEditable(false);

        JScrollPane scrollPane = new JScrollPane(textPane);
        scrollPane.setPreferredSize(new Dimension(400, 400));

        JOptionPane.showMessageDialog(this, scrollPane, "Vocabulary List", JOptionPane.PLAIN_MESSAGE);
    }

    private void loadWordsFromJson() {
        try {
            String selectedApi = (String) apiSelector.getSelectedItem();
            String apiEndpoint = getApiEndpoint(selectedApi);

            java.lang.reflect.Type listType = TypeToken.getParameterized(List.class, Word.class).getType();
            URL url = new URL(apiEndpoint);
            InputStreamReader reader = new InputStreamReader(url.openStream());
            words = new Gson().fromJson(reader, listType);

            currentIndex = 0;
            setNextQuestion();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getApiEndpoint(String apiName) {
        switch (apiName) {
            case "API 1":
                return "https://raw.githubusercontent.com/AppPeterPan/TaiwanSchoolEnglishVocabulary/main/1%E7%B4%9A.json";
            case "API 2":
                return "https://raw.githubusercontent.com/AppPeterPan/TaiwanSchoolEnglishVocabulary/main/2%E7%B4%9A.json";
            case "API 3":
                return "https://raw.githubusercontent.com/AppPeterPan/TaiwanSchoolEnglishVocabulary/main/3%E7%B4%9A.json";
            case "API 4":
                return "https://raw.githubusercontent.com/AppPeterPan/TaiwanSchoolEnglishVocabulary/main/4%E7%B4%9A.json";
            case "API 5":
                return "https://raw.githubusercontent.com/AppPeterPan/TaiwanSchoolEnglishVocabulary/main/5%E7%B4%9A.json";
            case "API 6":
                return "https://raw.githubusercontent.com/AppPeterPan/TaiwanSchoolEnglishVocabulary/main/6%E7%B4%9A.json";
            default:
                return "";
        }
    }

    private void setNextQuestion() {
        if (currentIndex < words.size()) {
            Word currentWord = words.get(currentIndex);
            wordLabel.setText("<html><div style='text-align: center;'>" + currentWord.getDefinitions().get(0).getPartOfSpeech() + ": " + currentWord.getDefinitions().get(0).getText() + "</div></html>");
            inputField.setText("");
            submitButton.setText("Submit");
            resultLabel.setText("");

            for (ActionListener listener : submitButton.getActionListeners()) {
                submitButton.removeActionListener(listener);
            }

            submitButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    checkAnswer();
                }
            });
        } else {
            wordLabel.setText("Quiz completed!");
            inputField.setEnabled(false);
            submitButton.setEnabled(false);
        }
    }

    private void checkAnswer() {
        Word currentWord = words.get(currentIndex);
        String userAnswer = inputField.getText().trim().toLowerCase();
        String correctAnswer = currentWord.getWord().toLowerCase();

        if (userAnswer.isEmpty()) {
            resultLabel.setText("<html><font color='red'>Please enter an answer.</font></html>");
        } else {
            if (userAnswer.equals(correctAnswer)) {
                resultLabel.setText("<html><font color='green'>Correct! Pronunciation: " + currentWord.getPronunciation() + "</font></html>");
                submitButton.setText("Next Question");

                for (ActionListener listener : submitButton.getActionListeners()) {
                    submitButton.removeActionListener(listener);
                }

                submitButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        currentIndex++;
                        setNextQuestion();
                    }
                });
            } else {
                resultLabel.setText("<html><font color='red'>Incorrect! Correct Answer: " + currentWord.getWord() + "</font></html>");
                shakeTextField();
            }
        }
    }

    private void shakeTextField() {
        final int numberOfShakes = 3;
        final int deltaX = 10;
        Timer timer = new Timer(500, new ActionListener() {
            int shakes = numberOfShakes;

            @Override
            public void actionPerformed(ActionEvent e) {
                if (shakes % 2 == 1) {
                    inputField.setLocation(inputField.getX() + deltaX, inputField.getY());
                } else {
                    inputField.setLocation(inputField.getX() - deltaX, inputField.getY());
                }
                shakes--;
                if (shakes == 0) {
                    ((Timer) e.getSource()).stop();
                    inputField.setLocation(inputField.getX() + deltaX, inputField.getY());
                    setNextQuestion();
                }
            }
        });
        timer.start();
    }

    private static class Word {
        private String word;
        private List<Definition> definitions;
        private String pronunciation;

        public String getWord() {
            return word;
        }

        public List<Definition> getDefinitions() {
            return definitions;
        }

        public String getPronunciation() {
            return pronunciation;
        }
    }

    private static class Definition {
        private String text;
        private String partOfSpeech;

        public String getText() {
            return text;
        }

        public String getPartOfSpeech() {
            return partOfSpeech;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new VocabularyQuiz();
            }
        });
    }
}
