package controller;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.FlowPane;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import model.WordSelector;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.List;

public class MainController {

    @FXML
    private Label mainWordLabel;

    @FXML
    private FlowPane clickableWordsPane;

    @FXML
    private FlowPane dropTargetPane;

    @FXML
    private Button startGameButton;

    @FXML
    private Button helpButton;

    @FXML
    private Button aboutButton;

    private String mainWord;
    private WordSelector wordSelector;

    @FXML
    public void initialize() {
        // Назначаем обработчики
        startGameButton.setOnAction(event -> onStartGame());
        helpButton.setOnAction(event -> onHelpButtonClicked());
        aboutButton.setOnAction(event -> onAboutButtonClicked());
    }

    @FXML
    public void onStartGame() {
        wordSelector = new WordSelector();
        List<String> result = getSelectedWordSequence();
        mainWordLabel.setText(result.getFirst());
        mainWord = result.getFirst();
        result.removeFirst();

        Collections.shuffle(result);

        clickableWordsPane.getChildren().clear();
        dropTargetPane.getChildren().clear();

        // Создаем кнопки в clickableWordsPane
        for (String word : result) {
            Button wordButton = createDraggableButton(word, clickableWordsPane, dropTargetPane);
            clickableWordsPane.getChildren().add(wordButton);
        }

        // Настроить зону сброса для dropTargetPane (прием слов из clickableWordsPane)
        setupDropTargetPane(dropTargetPane, clickableWordsPane);

        // Настроить зону сброса для clickableWordsPane (прием слов из dropTargetPane)
        setupDropTargetPane(clickableWordsPane, dropTargetPane);

        // Разрешить сброс обратно в список кликабельных слов
        clickableWordsPane.setOnDragOver(event -> {
            if (event.getGestureSource() != clickableWordsPane && event.getDragboard().hasString()) {
                event.acceptTransferModes(TransferMode.MOVE);
            }
            event.consume();
        });

        clickableWordsPane.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasString()) {
                String word = db.getString();
                Button wordButton = new Button(word);
                setupDraggable(wordButton); // Повторно сделать кнопку перетаскиваемой
                clickableWordsPane.getChildren().add(wordButton);
                success = true;

                // Удалить из dropTargetPane
                removeButtonWithText(dropTargetPane, word);
            }
            event.setDropCompleted(success);
            event.consume();
        });
    }

    @FXML
    public void onCheckGameEnd() {
        String mainWord = mainWordLabel.getText().toUpperCase();

        boolean hasCorrectInClickable = false;
        boolean hasIncorrectInDrop = false;

        for (Node node : clickableWordsPane.getChildren()) {
            if (node instanceof Button button) {
                String word = button.getText().toUpperCase();
                if (wordSelector.canFormWord(mainWord, word)) {
                    hasCorrectInClickable = true;
                    break; // можно остановиться, если нашли хотя бы одно
                }
            }
        }

        for (Node node : dropTargetPane.getChildren()) {
            if (node instanceof Button button) {
                String word = button.getText().toUpperCase();
                if (!wordSelector.canFormWord(mainWord, word)) {
                    hasIncorrectInDrop = true;
                    break; // можно остановиться, если нашли хотя бы одно
                }
            }
        }

        if (!hasCorrectInClickable && !hasIncorrectInDrop) {
            showAlert("Поздравляем!", "Вы успешно завершили игру!");
        } else {
            showAlert("Проверьте еще раз", """
                Некоторые слова не на месте:
                - Остались слова, которые можно было бы использовать?
                - Есть неверные слова среди выбранных?
                """);
        }
    }

    private void setupDraggable(Button wordButton) {
        wordButton.setOnDragDetected(event -> {
            Dragboard db = wordButton.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            content.putString(wordButton.getText());
            db.setContent(content);
            event.consume();
        });
    }

    private void removeButtonWithText(Pane pane, String text) {
        Node toRemove = null;
        for (Node node : pane.getChildren()) {
            if (node instanceof Button button && button.getText().equals(text)) {
                toRemove = node;
                break;
            }
        }
        if (toRemove != null) {
            pane.getChildren().remove(toRemove);
        }
    }


    private Button createDraggableButton(String word, FlowPane sourcePane, FlowPane targetPane) {
        Button button = new Button(word);

        button.setOnDragDetected(event -> {
            Dragboard db = button.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            content.putString(button.getText());
            db.setContent(content);
            event.consume();
        });

        return button;
    }

    private void setupDropTargetPane(FlowPane dropPane, FlowPane sourcePane) {
        dropPane.setOnDragOver(event -> {
            if (event.getGestureSource() != dropPane && event.getDragboard().hasString()) {
                event.acceptTransferModes(TransferMode.MOVE);
            }
            event.consume();
        });

        dropPane.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;

            if (db.hasString()) {
                String word = db.getString();

                // Найдем кнопку-источник
                Button sourceButton = (Button) event.getGestureSource();

                // Удалим из предыдущей панели
                sourcePane.getChildren().remove(sourceButton);

                // Создадим новую кнопку для новой панели (чтобы не было проблем с иерархией)
                Button newButton = createDraggableButton(word, dropPane, sourcePane);

                dropPane.getChildren().add(newButton);

                success = true;
            }

            event.setDropCompleted(success);
            event.consume();
        });
    }

    @FXML
    private void onHelpButtonClicked() {
        String helpText = "Цель игры:\n" +
                "Составлять новые слова из букв первого слова в списке. Проверяй свою внимательность, словарный запас и находчивость!\n\n" +
                "Правила игры:\n" +
                "• Нажми кнопку Начать игру\n" +
                "• У тебя есть слово-образец(в оранжевом поле). Например: ПАСКАЛЬ\n" +
                "• Твоя задача — найти и выбрать все слова, которые можно составить из букв этого слова. Примеры правильных слов: ЛАК, ЛАСКА, СКАЛА\n" +
                "• Можно использовать только те буквы, которые есть в слове-образце. Например, если в слове-образце только одна буква А, то в твоём слове может быть только одна А. Если в образце две буквы С, ты можешь использовать до двух С в новом слове.\n\n" +
                "Что засчитывается:\n" +
                "• Слово использует только буквы из слова-образца.\n" +
                "• Каждая буква используется не чаще, чем в оригинальном слове.\n\n" +
                "Что не засчитывается:\n" +
                "• Слово содержит лишние буквы, которых нет в образце.\n" +
                "• Буква повторяется чаще, чем она есть в образце.\n\n" +
                "Интерфейс:\n" +
                "• Просто перетащи слова вниз в синюю область.\n" +
                "• Если хочешь вернуть слово, то перетащи его вверх в белую область\n" +
                "• Нажми кнопку «Проверить», и программа покажет, какие из них правильные.\n";
        showAutoSizedAlert("Справка", helpText);
    }

    @FXML
    private void onAboutButtonClicked() {
        String aboutText = "Информация о разработчике:\nАвтор: Величайший геймдизайнер современности\nВерсия: 1.0\n2025 год.";
        showAutoSizedAlert("О разработчике", aboutText);
    }

    private void showAutoSizedAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);

        Label label = new Label(content);
        label.setWrapText(true);  // Чтобы текст переносился

        // Ограничим ширину, чтобы окно не было слишком широким, но текст переносился
        label.setMaxWidth(400);

        // Обернем в VBox для отступов
        VBox dialogPaneContent = new VBox();
        dialogPaneContent.setPadding(new Insets(10));
        dialogPaneContent.getChildren().add(label);

        alert.getDialogPane().setContent(dialogPaneContent);

        // Позволяем окну подстроить размер под содержимое
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);

        alert.showAndWait();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private List<String> getSelectedWordSequence() {
        WordSelector selector = new WordSelector();

        try (InputStream is = WordSelector.class.getClassLoader().getResourceAsStream("dictionary/dictionary.txt")) {
            if (is == null) {
                System.err.println("Файл dictionary.txt не найден в ресурсах!");
                return Collections.emptyList(); // Возвращаем пустой список
            }

            List<String> words;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                words = reader.lines().toList();
            }

            return selector.selectSequence(words); // Возвращаем найденную последовательность

        } catch (IOException e) {
            e.printStackTrace();
            return Collections.emptyList(); // В случае ошибки — пустой список
        }
    }

}
