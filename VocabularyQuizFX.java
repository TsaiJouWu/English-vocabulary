import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.scene.web.WebView;


import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

public class VocabularyQuizFX extends Application {

    private List<Word> words;
    private int currentIndex;
    private Label wordLabel;
    private TextField inputField;
    private Button submitButton;
    private Label resultLabel;
    private ComboBox<String> apiSelector;
    private ComboBox<String> mainMenu;
    private Button showAnswerButton;
    private Button nextButton;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        wordLabel = new Label();
        inputField = new TextField();
        submitButton = new Button("Submit");
        resultLabel = new Label();
        apiSelector = new ComboBox<>();
        mainMenu = new ComboBox<>();
        showAnswerButton = new Button("Show Answer");
        nextButton = new Button("Next");

        String[] apiOptions = {"CH1", "CH2", "CH3", "CH4", "CH5", "CH6"};
        apiSelector.getItems().addAll(apiOptions);
        apiSelector.getSelectionModel().select(0);

        String[] mainMenuOptions = {"Select Action", "View Vocabulary", "Take Quiz"};
        mainMenu.getItems().addAll(mainMenuOptions);

        mainMenu.setOnAction(e -> handleMainMenuSelection());
        showAnswerButton.setOnAction(e -> showAnswer());
        nextButton.setOnAction(e -> {
            currentIndex++;
            setNextQuestion();
        });

        GridPane gridPane = new GridPane();
        gridPane.setAlignment(Pos.CENTER);
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        gridPane.setPadding(new Insets(20, 20, 20, 20));

        gridPane.add(apiSelector, 0, 0);
        gridPane.add(mainMenu, 1, 0);
        gridPane.add(wordLabel, 0, 1, 2, 1);
        gridPane.add(inputField, 0, 2, 2, 1);
        gridPane.add(submitButton, 0, 3, 2, 1);
        gridPane.add(resultLabel, 0, 4, 2, 1);
        gridPane.add(showAnswerButton, 0, 5, 2, 1);
        gridPane.add(nextButton, 0, 6, 2, 1);

        Scene scene = new Scene(gridPane, 500, 500);
        scene.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());

        primaryStage.setScene(scene);
        primaryStage.setTitle("English Vocabulary");
        primaryStage.show();

        hideQuizElements();
    }

    private void handleMainMenuSelection() {
        String selectedAction = mainMenu.getValue();

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

        WebView webView = new WebView();
        webView.getEngine().loadContent(vocabularyText.toString());

        ScrollPane scrollPane = new ScrollPane(webView);
        scrollPane.setFitToHeight(true);
        scrollPane.setFitToWidth(true);

        Stage vocabularyStage = new Stage();
        vocabularyStage.setTitle("Vocabulary List");
        vocabularyStage.setScene(new Scene(scrollPane, 400, 400));
        vocabularyStage.show();
    }

    private void loadWordsFromJson() {
        try {
            String selectedApi = apiSelector.getValue();
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

            submitButton.setOnAction(e -> checkAnswer());
            showAnswerButton.setDisable(false);
            nextButton.setDisable(true);
        } else {
            wordLabel.setText("Quiz completed!");
            inputField.setDisable(true);
            submitButton.setDisable(true);
            showAnswerButton.setDisable(true);
            nextButton.setDisable(true);
        }
    }

    private void showAnswer() {
        Word currentWord = words.get(currentIndex);
        StringBuilder answerText = new StringBuilder("<html><b>English:</b> " + currentWord.getWord() + "<br>");

        for (Definition definition : currentWord.getDefinitions()) {
            answerText.append("<b>").append(definition.getPartOfSpeech()).append(":</b> ").append(definition.getText()).append("<br>");
        }

        answerText.append("</html>");

        Label answerLabel = new Label(answerText.toString());

        Button playPronunciationButton = new Button("Play Pronunciation");
        playPronunciationButton.setOnAction(e -> playPronunciation(currentWord.getWord()));
        HBox buttonBox = new HBox(playPronunciationButton);
        buttonBox.setAlignment(Pos.BOTTOM_CENTER);

        VBox answerBox = new VBox(answerLabel, buttonBox);
        answerBox.setSpacing(10);
        answerBox.setAlignment(Pos.CENTER);

        Stage answerStage = new Stage();
        answerStage.setTitle("Answer");
        answerStage.setScene(new Scene(answerBox, 300, 200));
        answerStage.show();

        showAnswerButton.setDisable(true);
        nextButton.setDisable(false);
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
            java.awt.Desktop.getDesktop().browse(new URI(url));
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

                submitButton.setOnAction(e -> {
                    currentIndex++;
                    setNextQuestion();
                });
            } else {
                resultLabel.setText("<html><font color='red'>Incorrect! Correct Answer: " + currentWord.getWord() + "</font></html>");
                shakeTextField();
            }
        }
    }

private void shakeTextField() {
    int[] numberOfShakes = {3};
    final int deltaX = 10;

    javafx.animation.Timeline timeline = new javafx.animation.Timeline(
            new javafx.animation.KeyFrame(javafx.util.Duration.millis(500), e -> {
                if (numberOfShakes[0] % 2 == 1) {
                    inputField.setTranslateX(inputField.getTranslateX() + deltaX);
                } else {
                    inputField.setTranslateX(inputField.getTranslateX() - deltaX);
                }
                numberOfShakes[0]--;
                if (numberOfShakes[0] == 0) {
                    inputField.setTranslateX(inputField.getTranslateX() + deltaX);
                    setNextQuestion();
                }
            })
    );
    timeline.play();
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
}
