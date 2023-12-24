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

    public VocabularyQuiz() {
        loadWordsFromJson();

        wordLabel = new JLabel();
        inputField = new JTextField();
        submitButton = new JButton("Submit");
        resultLabel = new JLabel();

        submitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                checkAnswer();
            }
        });

        setNextQuestion();

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(4, 1, 10, 10));
        panel.add(wordLabel);
        panel.add(inputField);
        panel.add(submitButton);
        panel.add(resultLabel);

        add(panel);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 400);
        setTitle("Vocabulary Quiz");
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void loadWordsFromJson() {
        try {
            java.lang.reflect.Type listType = TypeToken.getParameterized(List.class, Word.class).getType();
            URL url = new URL("https://raw.githubusercontent.com/AppPeterPan/TaiwanSchoolEnglishVocabulary/main/1%E7%B4%9A.json");
            InputStreamReader reader = new InputStreamReader(url.openStream());
            words = new Gson().fromJson(reader, listType);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void setNextQuestion() {
        if (currentIndex < words.size()) {
            Word currentWord = words.get(currentIndex);
            wordLabel.setText("<html><div style='text-align: center;'>" + currentWord.getDefinitions().get(0).getPartOfSpeech() + ": " + currentWord.getDefinitions().get(0).getText() + "</div></html>");
            inputField.setText("");
            submitButton.setText("Submit");
            resultLabel.setText("");
    
            // 移除先前的 ActionListener
            for (ActionListener listener : submitButton.getActionListeners()) {
                submitButton.removeActionListener(listener);
            }
    
            submitButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    checkAnswer(); // 在切換到下一題時再次檢查答案
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
            // 使用者尚未輸入答案
            resultLabel.setText("<html><font color='red'>Please enter an answer.</font></html>");
        } else {
            if (userAnswer.equals(correctAnswer)) {
                resultLabel.setText("<html><font color='green'>Correct! Pronunciation: " + currentWord.getPronunciation() + "</font></html>");
                submitButton.setText("Next Question");
    
                // 移除先前的 ActionListener，以免重複執行
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
        Timer timer = new Timer(100, new ActionListener() {
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
