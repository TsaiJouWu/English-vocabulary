package BackEnd;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
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
    private JButton showAnswerButton;
    private JButton nextButton;

    public VocabularyQuiz() {
        wordLabel = new JLabel();
        inputField = new JTextField();
        submitButton = new JButton("Submit");
        resultLabel = new JLabel();
        apiSelector = new JComboBox<>();
        mainMenu = new JComboBox<>();
        showAnswerButton = new JButton("Show Answer");
        nextButton = new JButton("Next");

        String[] apiOptions = {"CH1", "CH2", "CH3", "CH4", "CH5", "CH6"};
        apiSelector.setModel(new DefaultComboBoxModel<>(apiOptions));
        apiSelector.setSelectedIndex(0);

        String[] mainMenuOptions = {"Select Action", "View Vocabulary", "Take Quiz"};
        mainMenu.setModel(new DefaultComboBoxModel<>(mainMenuOptions));

        mainMenu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleMainMenuSelection();
            }
        });
        showAnswerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showAnswer();
            }
        });
        nextButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                currentIndex++;
                setNextQuestion();
            }
        });

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(6, 1, 10, 10));
        panel.add(apiSelector);
        panel.add(mainMenu);
        panel.add(wordLabel);
        panel.add(inputField);
        panel.add(submitButton);
        panel.add(resultLabel);
        panel.add(showAnswerButton);
        panel.add(nextButton);

        add(panel);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 500);
        setTitle("English Vocabulary");
        setLocationRelativeTo(null);
        setVisible(true);

        hideQuizElements();
    }

    private void handleMainMenuSelection() {
        String selectedAction = (String) mainMenu.getSelectedItem();

        if ("View Vocabulary".equals(selectedAction)) {
            loadWordsFromJson();
            showVocabulary();
            hideQuizElements();
        } else if ("Take Quiz".equals(selectedAction)) {
            currentIndex = 0;
            loadWordsFromJson();
            setNextQuestion();
            showQuizElements();
        }
    }

    private void hideQuizElements() {
        wordLabel.setText("");
        inputField.setText("");
        submitButton.setVisible(false);
        resultLabel.setText("");
        showAnswerButton.setVisible(false);
        nextButton.setVisible(false);
        }

    private void showQuizElements() {
        inputField.setText("");
        submitButton.setVisible(true);
        resultLabel.setText("");
        showAnswerButton.setVisible(true);
        nextButton.setVisible(true);
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
            case "CH1":
                return "https://raw.githubusercontent.com/AppPeterPan/TaiwanSchoolEnglishVocabulary/main/1%E7%B4%9A.json";
            case "CH2":
                return "https://raw.githubusercontent.com/AppPeterPan/TaiwanSchoolEnglishVocabulary/main/2%E7%B4%9A.json";
            case "CH3":
                return "https://raw.githubusercontent.com/AppPeterPan/TaiwanSchoolEnglishVocabulary/main/3%E7%B4%9A.json";
            case "CH4":
                return "https://raw.githubusercontent.com/AppPeterPan/TaiwanSchoolEnglishVocabulary/main/4%E7%B4%9A.json";
            case "CH5":
                return "https://raw.githubusercontent.com/AppPeterPan/TaiwanSchoolEnglishVocabulary/main/5%E7%B4%9A.json";
            case "CH6":
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
            showAnswerButton.setEnabled(true);
            nextButton.setEnabled(false);
    
        } else {
            wordLabel.setText("Quiz completed!");
            inputField.setEnabled(false);
            submitButton.setEnabled(false);
            showAnswerButton.setEnabled(false);
            nextButton.setEnabled(false);
        }
    }

    private void showAnswer() {
        Word currentWord = words.get(currentIndex);
        StringBuilder answerText = new StringBuilder("<html><b>English:</b> " + currentWord.getWord() + "<br>");
        
        for (Definition definition : currentWord.getDefinitions()) {
            answerText.append("<b>").append(definition.getPartOfSpeech()).append(":</b> ").append(definition.getText()).append("<br>");
        }
    
        answerText.append("</html>");
    
        JTextPane answerPane = new JTextPane();
        answerPane.setContentType("text/html");
        answerPane.setText(answerText.toString());
        answerPane.setEditable(false);
    
        JButton playPronunciationButton = new JButton("Play Pronunciation");
        playPronunciationButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                playPronunciation(currentWord.getPronunciation());
            }
        });
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(playPronunciationButton);
        buttonPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        buttonPanel.setAlignmentY(Component.BOTTOM_ALIGNMENT);

        JPanel dialogPanel = new JPanel();
        dialogPanel.setLayout(new BoxLayout(dialogPanel, BoxLayout.Y_AXIS));
        dialogPanel.add(answerPane);
        dialogPanel.add(Box.createVerticalStrut(10));
        dialogPanel.add(buttonPanel);

        JOptionPane.showMessageDialog(this, dialogPanel, "Answer", JOptionPane.PLAIN_MESSAGE);

        showAnswerButton.setEnabled(false);
        nextButton.setEnabled(true);
    }

    private void playPronunciation(String wordToPronounce) {
        try {
            String pronunciationURL = "https://translate.google.com/translate_tts?ie=UTF-8&client=tw-ob&q=" + wordToPronounce + "&tl=en";
            openBrowser(pronunciationURL);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openBrowser(String url) {
        try {
            Desktop desktop = Desktop.getDesktop();
            desktop.browse(new URI(url));
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
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
