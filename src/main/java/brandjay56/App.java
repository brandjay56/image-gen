package brandjay56;

import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import com.theokanning.openai.service.OpenAiService;
import com.theokanning.openai.image.CreateImageRequest;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.ObservableList;
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
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.awt.*;

public class App extends Application{

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
    Button saveButton;
    Button nextButton;
    Button lastButton;
    ImageView imageView;
    TextField searchField;
    Label textLabel;
    Label errorLabel;
    ProgressBar progressBar;

    List<GenImage> images;
    Integer currentPos;


    public App () {

        this.prompt = "";
        this.mainUrl = "";
        this.finalResponse = "";

        this.token = "ENTER YOUR OPENAI API KEY HERE";
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
        this.saveButton = new Button("Save Image");
        this.nextButton = new Button("Next Image");
        this.lastButton = new Button("Previous Image");
        this.imageView = new ImageView();
        this.textLabel = new Label("");
        this.errorLabel = new Label("");
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
        this.buttonRow.getChildren().addAll(this.regenButton, this.webButton, this.saveButton, this.lastButton, this.nextButton);

        this.progressBox.setAlignment(Pos.CENTER);
        this.imageBox.setAlignment(Pos.CENTER);
        this.buttonRow.setAlignment(Pos.CENTER);
        this.textLabel.setAlignment(Pos.CENTER);
        
        this.root.getChildren().addAll(this.searchRow, this.errorLabel);
        root.setPadding(new Insets(10));


    }

    /** {@inheritDoc} */
    @Override
    public void start(Stage stage) {
        this.stage = stage;
        this.scene = new Scene(root, 570, 680);
        String cssFile = getClass().getResource("/style.css").toExternalForm();
        scene.getStylesheets().add(cssFile);

        
        this.genButton.setId("gen-button");

        
        this.stage.setScene(this.scene);
        this.stage.sizeToScene();
        this.stage.setTitle("Image Generator");
        this.stage.show();





        this.genButton.setOnAction(event -> this.runNow(() -> {
            try {
                if (this.searchField.getText().trim().isEmpty()) {
                    Platform.runLater(() -> this.errorLabel.setText("Error: Please enter a word or phrase."));
                } else {
                Image img = createImage(prompt);
                Platform.runLater(() -> updateImages(img));
                }
            } catch (Exception e1) {
                e1.printStackTrace();
                Platform.runLater(() -> {
                    progressBar.setProgress(0);
                    genButton.setDisable(false);
                    regenButton.setDisable(false);
                    errorLabel.setText("Error: Unable to generate image. Please try again.");
                });

            }
        }));

        this.searchField.setOnKeyPressed(event -> {
        if (event.getCode() == KeyCode.ENTER) {
            this.runNow(() -> {
                try {
                    if (this.searchField.getText().trim().isEmpty()) {
                        Platform.runLater(() -> this.errorLabel.setText("Error: Please enter a word or phrase."));
                    } else {
                    prompt = chatPrompt(searchField.getText());
                    Image img = createImage(prompt);
                    Platform.runLater(() -> updateImages(img));
                    }
                } catch (Exception e1) {
                    e1.printStackTrace();
                    Platform.runLater(() -> {
                        progressBar.setProgress(0);
                        genButton.setDisable(false);
                        regenButton.setDisable(false);
                        errorLabel.setText("Error: Unable to generate image. Please try again.");
                    });
                }
        });
        }});

        this.regenButton.setOnAction(event -> this.runNow(() -> {
            try {
                Image img = createImage(prompt);
                Platform.runLater(() -> updateImages(img));
            } catch (Exception e1) {
                e1.printStackTrace();
                Platform.runLater(() -> {
                    progressBar.setProgress(0);
                    genButton.setDisable(false);
                    regenButton.setDisable(false);
                    errorLabel.setText("Error: Unable to regenerate. Please try again.");
                });
            }
        }));

        this.webButton.setOnAction(event -> this.runNow(() -> {
            try {
                openWebPage(images.get(currentPos).getUrl());
            } catch (Exception e1) {
                e1.printStackTrace();
                Platform.runLater(() -> {
                    progressBar.setProgress(0);
                    genButton.setDisable(false);
                    regenButton.setDisable(false);
                    errorLabel.setText("Error: Unable to open in web");
                });
            }
        }));

        this.saveButton.setOnAction(event -> this.runNow(() -> {
            try {
                saveImage(images.get(currentPos).getUrl());
            } catch (Exception e1) {
                e1.printStackTrace();
                Platform.runLater(() -> {
                    progressBar.setProgress(0);
                    genButton.setDisable(false);
                    regenButton.setDisable(false);
                    errorLabel.setText("Error: Unable to save image");
                });
            }
        }));

        this.nextButton.setOnAction(event -> this.runNow(() -> {
            try {
                Platform.runLater(() -> nextImage());
            } catch (Exception e1) {
                e1.printStackTrace();
                Platform.runLater(() -> {
                    progressBar.setProgress(0);
                    genButton.setDisable(false);
                    regenButton.setDisable(false);
                    errorLabel.setText("Error: No next image");
                });
            }
        }));

        this.lastButton.setOnAction(event -> this.runNow(() -> {
            try {
                Platform.runLater(() -> lastImage());
            } catch (Exception e1) {
                e1.printStackTrace();
                Platform.runLater(() -> {
                    progressBar.setProgress(0);
                    genButton.setDisable(false);
                    regenButton.setDisable(false);
                    errorLabel.setText("Error: No previous image");
                });
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

        Platform.runLater(() -> {
            progressBar.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
            genButton.setDisable(true);
            regenButton.setDisable(true);
        });

        final List<ChatMessage> messages = new ArrayList<>();
        final ChatMessage systemMessage = new ChatMessage(ChatMessageRole.SYSTEM.value(), "Generate a prompt for an AI Image Generator based on the phrase " + query + ". The generator understands you want to create an image, so you don't need to include anything about creating an image. For the prompt please include up to two unique additions and an art style or artist to base the style on. You don't need to preclude the prompt with anything. Following this sentence please respond with the prompt.");
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

        Platform.runLater(() -> {
            progressBar.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
            genButton.setDisable(true);
            regenButton.setDisable(true);
        });

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

        root.getChildren().addAll(searchRow, imageBox, textRow, buttonRow, errorLabel);

        progressBar.setProgress(0.0);

        genButton.setDisable(false);
        regenButton.setDisable(false);
        errorLabel.setText("");
    } 

    public void lastImage() {
        //Reloads to frame
        if (currentPos > 0) {
            currentPos--;
            imageView.setImage(new Image(images.get(currentPos).getUrl(), 500, 500, true, true));
            textLabel.setText(images.get(currentPos).getPrompt());
        }
    } 

    public void nextImage() {
        //Reloads to frame
        if (currentPos < images.size() - 1) {
            currentPos++;
            imageView.setImage(new Image(images.get(currentPos).getUrl(), 500, 500, true, true));
            textLabel.setText(images.get(currentPos).getPrompt());
        }
    } 

    public void saveImage(String url) {
        
        URL url2 = null;
        BufferedImage img = null;

        try {
            url2 = new URL(mainUrl);
            img = ImageIO.read(url2);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        String pattern = "yyyyMMddHHmmss";
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        String fileName = "image-" + sdf.format(new Date()) + ".png";
        String fileNameText = "image-" + sdf.format(new Date()) + ".txt";


        File dir = new File("images/");
        if (!dir.exists()) {
            boolean created = dir.mkdirs();
            if (!created) {
                // Handle error
            }
        }

        File file = new File("images/" + fileName);
        File fileText = new File("images/" + fileNameText);

        try (FileWriter writer = new FileWriter(fileText)) {
            writer.write(this.images.get(currentPos).getPrompt());
            writer.close();
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
            try {
            ImageIO.write(img, "png", file);
            file.createNewFile();
            fileText.createNewFile();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
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

    public void styleProgram () {
        this.root.getStyleClass().add("root");
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
