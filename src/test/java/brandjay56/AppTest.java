package brandjay56;

import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import com.theokanning.openai.service.OpenAiService;
import com.theokanning.openai.image.CreateImageRequest;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.stage.*;
import javafx.scene.*;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.image.Image;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;

import javax.swing.*;
import javax.imageio.*;

import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.List;
import java.awt.*;

public class AppTest extends Application{

    String prompt;
    String mainUrl;
    String finalResponse;

    String token;
    OpenAiService service;
    
    Stage stage;
    Scene scene;
    VBox root;
    HBox searchRow;
    HBox textRow;
    HBox buttonRow;
    HBox imageBox;
    VBox progressBox;
    Button genButton;
    Button regenButton;
    Button webButton;
    Button nextButton;
    Button lastButton;
    ImageView imageView;
    TextField searchField;
    Label textLabel;
    ProgressBar progressBar;

    List<GenImage> images;
    Integer currentPos;


    public AppTest () {

        this.prompt = "";
        this.mainUrl = "";
        this.finalResponse = "";

        this.token = "INSERT TOKEN";
        this.service = new OpenAiService(token);

        this.root = new VBox(8);
        this.searchRow = new HBox(8);
        this.textRow = new HBox(8);
        this.buttonRow = new HBox(8);
        this.imageBox = new HBox(8);
        this.progressBox = new VBox(8);
        this.genButton = new Button("Generate");
        this.regenButton = new Button("Regenerate");
        this.webButton = new Button("Open in Web");
        this.nextButton = new Button("Next Image");
        this.lastButton = new Button("Previous Image");
        this.imageView = new ImageView();
        this.textLabel = new Label("");
        this.searchField = new TextField("Enter a word or phrase");
        this.progressBar = new ProgressBar(0.0);

        this.images = new ArrayList<>();
        this.currentPos = null;

    }

    /** {@inheritDoc} */
    @Override
    public void init() {
        HBox.setHgrow(this.searchField, Priority.ALWAYS);
        HBox.setHgrow(this.progressBar, Priority.ALWAYS);
        VBox.setVgrow(root, Priority.ALWAYS);

        textLabel.setWrapText(true);

        this.imageBox.getChildren().add(this.imageView);
        this.progressBox.getChildren().add(this.progressBar);

        this.searchRow.getChildren().addAll(this.searchField, this.genButton, this.progressBox);
        this.textRow.getChildren().add(this.textLabel);
        this.buttonRow.getChildren().addAll(this.regenButton, this.webButton, this.lastButton, this.nextButton);

        this.progressBox.setAlignment(Pos.CENTER);
        this.imageBox.setAlignment(Pos.CENTER);
        this.buttonRow.setAlignment(Pos.CENTER);
        
        this.root.getChildren().addAll(this.searchRow);
        root.setPadding(new Insets(10));


    }

    /** {@inheritDoc} */
    @Override
    public void start(Stage stage) {
        this.stage = stage;
        this.scene = new Scene(root, 550, 660);
        this.stage.setScene(this.scene);
        this.stage.sizeToScene();
        this.stage.setTitle("Image Generator");
        this.stage.show();

        this.genButton.setOnAction(event -> this.runNow(() -> {
            try {
                prompt = chatPrompt(searchField.getText());
                Image img = createImage(prompt);
                Platform.runLater(() -> updateImages(img));
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }));

        this.searchField.setOnKeyPressed(event -> {
        if (event.getCode() == KeyCode.ENTER) {
            this.runNow(() -> {
                try {
                    prompt = chatPrompt(searchField.getText());
                    Image img = createImage(prompt);
                    Platform.runLater(() -> updateImages(img));
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
        });
        }});

        this.regenButton.setOnAction(event -> this.runNow(() -> {
            try {
                Image img = createImage(prompt);
                Platform.runLater(() -> updateImages(img));
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }));

        this.webButton.setOnAction(event -> this.runNow(() -> {
            try {
                openWebPage(mainUrl);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }));

        this.nextButton.setOnAction(event -> this.runNow(() -> {
            try {
                Platform.runLater(() -> nextImage());
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }));

        this.lastButton.setOnAction(event -> this.runNow(() -> {
            try {
                Platform.runLater(() -> lastImage());
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }));
        

        ContextMenu contextMenu = new ContextMenu();

        MenuItem copyItem = new MenuItem("Copy");
        copyItem.setOnAction(e -> {
            Clipboard clipboard = Clipboard.getSystemClipboard();
            ClipboardContent content = new ClipboardContent();
            content.putString(textLabel.getText());
            clipboard.setContent(content);
        });

        contextMenu.getItems().add(copyItem);
        textLabel.setContextMenu(contextMenu);
    }


    public String chatPrompt(String query) {

        Platform.runLater(() -> progressBar.setProgress(ProgressBar.INDETERMINATE_PROGRESS));

        final List<ChatMessage> messages = new ArrayList<>();
        final ChatMessage systemMessage = new ChatMessage(ChatMessageRole.SYSTEM.value(), "Generate a prompt for an AI Image Generator based on the phrase " + query + ". The generator understands you want to create an image, so you don't need to include anything about creating an image.  For the prompt please include 2 unique additions and an art style. You don't need to preclude the prompt with anything. Following this sentence please respond with the prompt.");
        messages.add(systemMessage);
        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest
                .builder()
                .model("gpt-3.5-turbo")
                .messages(messages)
                .n(1)
                .maxTokens(100)
                .logitBias(new HashMap<>())
                .build();
                
                StringBuilder response = new StringBuilder();
                service.streamChatCompletion(chatCompletionRequest)
                    .doOnError(Throwable::printStackTrace)
                    .blockingForEach(result -> response.append(result.getChoices().get(0).getMessage().getContent()));
                finalResponse = response.toString().substring(4, response.toString().length() - 4);

                messages.clear();
                return finalResponse;

    }


    /**
     * 
     * @param query 
     * @param frame
     * @throws IOException
     */
    public Image createImage(String prompt) throws IOException {
        
        Platform.runLater(() -> progressBar.setProgress(ProgressBar.INDETERMINATE_PROGRESS));

        CreateImageRequest request = CreateImageRequest.builder()
                .prompt(prompt)
                .build();

        mainUrl = service.createImage(request).getData().get(0).getUrl();
        Image image = null;

        image = new Image(mainUrl, 500, 500, true, true);

        images.add(new GenImage(mainUrl, prompt));
        currentPos = images.size() - 1;

        return image;
    }

    public void updateImages(Image image) {
        //Reloads to frame
        
        imageView.setImage(image);
        textLabel.setText(finalResponse);

        root.getChildren().clear();

        root.getChildren().addAll(searchRow, imageBox, textRow, buttonRow);

        progressBar.setProgress(0.0);
    } 

    public void lastImage() {
        //Reloads to frame
        if (currentPos > 0) {
            currentPos--;
            imageView.setImage(new Image(images.get(currentPos).getUrl(), 400, 400, true, true));
            textLabel.setText(images.get(currentPos).getPrompt());
        }
    } 

    public void nextImage() {
        //Reloads to frame
        if (currentPos < images.size() - 1) {
            currentPos++;
            imageView.setImage(new Image(images.get(currentPos).getUrl(), 400, 400, true, true));
            textLabel.setText(images.get(currentPos).getPrompt());
        }
    } 

    public void openWebPage(String url) throws IOException {
        URI uri = null;
        try {
            uri = new URI(url);
        } catch (URISyntaxException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Desktop.getDesktop().browse(uri);
    }

        /**
    * Runs in a new thread.
    *
    * @param target is the method to run in a new thread
    */
    public void runNow (Runnable target) {
        Thread t = new Thread(target);
        t.setDaemon(true);
        t.start();
    } //runNow
}
