package boardview;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import gamecontrol.AIChessController;
import gamecontrol.ChessController;
import gamecontrol.GameController;
import gamecontrol.NetworkedChessController;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/**
 * Main class for the chess application
 * Sets up the top level of the GUI
 * @author Gustavo
 * @version
 */
public class ChessFX extends Application {

    private GameController controller;
    private BoardView board;
    private Text state;
    private Text sideStatus;
    private VBox root;
    private HBox topPanel;
    private HBox bottomPanel;

    @Override
    public void start(Stage primaryStage) {
        //UI elements

        //controller for the chess game logic
        controller = new ChessController();

        //displays the current state for the game
        state = new Text(controller.getCurrentState().toString());

        //displays who's turn it is
        sideStatus = new Text();

        //displays local host IP
        Text locIP = new Text();
        try {
            locIP.setText(InetAddress.getLocalHost().toString());
        } catch (UnknownHostException e) {
            locIP.setText(e.getMessage());
        }

        //text field for IP address
        TextField ip = new TextField("");

        //resets the board
        Button reset = new Button("Reset");
        reset.setOnAction(e -> {
                board.reset(new ChessController());
            });

        //sets opponent to AI
        Button playAI = new Button("Play AI");
        playAI.setOnAction(e -> {
                board.reset(new AIChessController());
            });

        //joins a game based on IP in textfield
        Button joinGame = new Button("Join");
        joinGame.setOnMouseClicked(makeJoinListener(ip));
        joinGame.disableProperty().bind(Bindings.isEmpty(ip.textProperty()));

        //opens and hosts a game to be joined by an opponent
        Button host = new Button("Host");
        host.setOnMouseClicked(makeHostListener());

        //UI partitions
        root = new VBox(10);
        topPanel = new HBox(30);
        bottomPanel = new HBox(30);
        board = new BoardView(controller, state, sideStatus);

        //adds elements to the two hboxes
        topPanel.getChildren().addAll(reset, playAI, state, sideStatus);
        bottomPanel.getChildren().addAll(host, locIP, ip, joinGame);

        //sets up the panel and scene
        root.getChildren().addAll(board.getView(), topPanel, bottomPanel);
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private EventHandler<? super MouseEvent> makeHostListener() {
        return event -> {
            board.reset(new NetworkedChessController());
        };
    }

    private EventHandler<? super MouseEvent> makeJoinListener(TextField input) {
        return event -> {
            try {
                InetAddress addr = InetAddress.getByName(input.getText());
                GameController newController
                    = new NetworkedChessController(addr);
                board.reset(newController);
            } catch (IOException e) {
                e.printStackTrace();
            }
        };
    }


    public static void main(String[] args) {
        launch(args);
    }
}
