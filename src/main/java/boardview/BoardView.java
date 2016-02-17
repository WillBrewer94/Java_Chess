package boardview;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import gamecontrol.ChessController;
import gamecontrol.GameController;
import gamecontrol.GameState;
import gamecontrol.NetworkedChessController;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import model.Move;
import model.IllegalMoveException;
import model.Position;
import model.Side;
import model.PieceType;
import model.Piece;
import model.chess.ChessPiece;

/**
 * A class for a view for a chess board. This class must have a reference
 * to a GameController for chess playing chess
 * @author Gustavo
 * @date Oct 20, 2015
 */
public class BoardView {

    /* You may add more instance data if you need it */
    protected GameController controller;
    private GridPane gridPane;
    private Tile[][] tiles;
    private Text sideStatus;
    private Text state;
    private Set<Move> moves;
    private Move lastMove;
    private Tile currentTile;
    private boolean isFirst = false;
    private boolean isRotated;

    /**
     * Construct a BoardView with an instance of a GameController
     * and a couple of Text object for displaying info to the user
     * @param controller The controller for the chess game
     * @param state A Text object used to display state to the user
     * @param sideStatus A Text object used to display whose turn it is
     */
    public BoardView(GameController controller, Text state, Text sideStatus) {
        this.controller = controller;
        this.state = state;
        this.sideStatus = sideStatus;
        tiles = new Tile[8][8];
        gridPane = new GridPane();
        gridPane.setStyle("-fx-background-color : goldenrod;");
        reset(controller);
    }

    /**
     * Listener for clicks on a tile
     *
     * @param tile The tile attached to this listener
     * @return The event handler for all tiles.
     */
    private EventHandler<? super MouseEvent> tileListener(Tile tile) {
        return event -> {
            if (controller instanceof NetworkedChessController
                    && controller.getCurrentSide()
                    != ((NetworkedChessController) controller).getLocalSide()) {
                //not your turn!
                return;
            }

            // Don't change the code above this :)
            if (!isFirst) {
                firstClick(tile);
            } else {
                secondClick(tile);
            }
        };
    }

    /**
     * Perform the first click functions, like displaying
     * which are the valid moves for the piece you clicked.
     * @param tile The TileView that was clicked
     */
    private void firstClick(Tile tile) {
        System.out.println("First Click");
        if (tile.getSymbol().equals("")
                || controller.getMovesForPieceAt(tile.getPosition())
                .isEmpty()) {
            return;
        } else {
            currentTile = tile;
            tile.highlight(Color.LIGHTGREEN);
            tile.getRootNode();
            moves = controller.getMovesForPieceAt(tile.getPosition());

            for (Move m : moves) {
                Position sPos = m.getStart();
                Position dPos = m.getDestination();

                Tile td = tiles[dPos.getRow()][dPos.getCol()];
                Tile ts = tiles[sPos.getRow()][sPos.getCol()];

                ts.highlight(Color.GREEN);

                if (controller.moveResultsInCapture(m)) {
                    td.highlight(Color.SALMON);
                } else if (td.getSymbol().equals("")) {
                    td.highlight(Color.LIGHTGREEN);
                }
            }
            isFirst = true;
        }
    }

    /**
     * Perform the second click functions, like
     * sending moves to the controller but also
     * checking that the user clicked on a valid position.
     * If they click on the same piece they clicked on for the first click
     * then you should reset to click state back to the first click and clear
     * the highlighting effected on the board.
     *
     * @param tile the TileView at which the second click occurred
     */
    private void secondClick(Tile tile) {
        System.out.println("Second Click");
        Move move = new Move(currentTile.getPosition(), tile.getPosition());

        if (moves.contains(move)) {
            System.out.println("Legal Move");
            try {
                lastMove = move;
                controller.makeMove(move);
                controller.endTurn();
                controller.beginTurn();
                isFirst = false;

            } catch (IllegalMoveException e) {
                System.out.println(e.getMessage());
            }

        } else if (currentTile.getPosition() == tile.getPosition()) {
            currentTile.clear();
            isFirst = false;
            for (Move m : controller
                    .getMovesForPieceAt(currentTile.getPosition())) {
                this.getTileAt(m.getDestination()).clear();
            }

        } else {
            System.out.println("Illegal Move");
        }
    }

    /**
     * This method should be called any time a move is made on the back end.
     * It should update the tiles' highlighting and symbols to reflect the
     * change in the board state.
     *
     * @param moveMade the move to show on the view
     * @param capturedPositions a list of positions where pieces were captured
     *
     */
    public void updateView(Move moveMade, List<Position> capturedPositions) {
        //clears previous highlights
        for (Tile[] tile : tiles) {
            for (Tile t : tile) {
                t.clear();
            }
        }

        //gets positions of tiles
        Position sPos = moveMade.getStart();
        Position dPos = moveMade.getDestination();
        Tile start = this.getTileAt(sPos);
        Tile end = this.getTileAt(dPos);

        //updates symbols
        start.setSymbol(controller.getSymbolForPieceAt(sPos));
        end.setSymbol(controller.getSymbolForPieceAt(dPos));

        //updates highlights for last move made
        this.getTileAt(moveMade.getStart()).highlight(Color.SEAGREEN);
        this.getTileAt(moveMade.getDestination()).highlight(Color.SEAGREEN);

        //eliminates captured pieces
        for (Position p : capturedPositions) {
            Tile cap = tiles[p.getRow()][p.getCol()];
            cap.setSymbol(controller.getSymbolForPieceAt(p));
        }

        //keeps track of last move made
        lastMove = moveMade;
    }

    /**
     * Asks the user which PieceType they want to promote to
     * (suggest using Alert). Then it returns the Piecetype user selected.
     *
     * @return  the PieceType that the user wants to promote their piece to
     */
    private PieceType handlePromotion() {
        //promotion type buttons
        ButtonType queen = new ButtonType("Queen");
        ButtonType rook = new ButtonType("Rook");
        ButtonType bishop = new ButtonType("Bishop");
        ButtonType knight = new ButtonType("Knight");

        //alert settings
        Alert d = new Alert(Alert.AlertType.CONFIRMATION);
        d.setTitle("Promotion");
        d.setHeaderText("Piece promoted!");
        d.setContentText("Choose piece to promote to: ");
        d.getButtonTypes().setAll(queen, rook, bishop, knight);

        //button events
        Optional<ButtonType> result = d.showAndWait();
        if (result.get() == queen) {
            return ChessPiece.ChessPieceType.QUEEN;
        } else if (result.get() == rook) {
            return ChessPiece.ChessPieceType.ROOK;
        } else if (result.get() == bishop) {
            return ChessPiece.ChessPieceType.BISHOP;
        } else {
            return ChessPiece.ChessPieceType.KNIGHT;
        }
    }

    /**
     * Handles a change in the GameState (ie someone in check or stalemate).
     * If the game is over, it should open an Alert and ask to keep
     * playing or exit.
     *
     * @param s The new Game State
     */
    public void handleGameStateChange(GameState s) {
        //updates game state text
        state.setText(controller.getCurrentState().toString());
        if (s.isGameOver()) {
            //buttons
            ButtonType play = new ButtonType("Play Again");
            ButtonType quit = new ButtonType("Quit");

            //alert settings
            Alert d = new Alert(Alert.AlertType.CONFIRMATION);
            d.setTitle("Game Over");
            d.setHeaderText("Game Over!");
            d.setContentText("Play another or quit?");
            d.getButtonTypes().setAll(play, quit);

            //button events
            Optional<ButtonType> result = d.showAndWait();
            if (result.get() == play) {
                reset(new ChessController());
            } else if (result.get() == quit) {
                Platform.exit();
            }
        }
    }

    /**
     * Updates UI that depends upon which Side's turn it is
     *
     * @param s The new Side whose turn it currently is
     */
    public void handleSideChange(Side s) {
        String side = s.toString();
        this.sideStatus.setText(side + "'s Turn");
    }

    /**
     * Resets this BoardView with a new controller.
     * This moves the chess pieces back to their original configuration
     * and calls startGame() at the end of the method
     * @param newController The new controller for this BoardView
     */
    public void reset(GameController newController) {
        if (controller instanceof NetworkedChessController) {
            ((NetworkedChessController) controller).close();
        }
        controller = newController;
        isRotated = false;
        if (controller instanceof NetworkedChessController) {
            Side mySide
                = ((NetworkedChessController) controller).getLocalSide();
            if (mySide == Side.BLACK) {
                isRotated = true;
            }
        }
        sideStatus.setText(controller.getCurrentSide() + "'s Turn");

        // controller event handlers
        // We must force all of these to run on the UI thread
        controller.addMoveListener(
                (Move move, List<Position> capturePositions) ->
                Platform.runLater(
                    () -> updateView(move, capturePositions)));

        controller.addCurrentSideListener(
                (Side side) -> Platform.runLater(
                    () -> handleSideChange(side)));

        controller.addGameStateChangeListener(
                (GameState state) -> Platform.runLater(
                    () -> handleGameStateChange(state)));

        controller.setPromotionListener(() -> handlePromotion());


        addPieces();
        controller.startGame();
        if (isRotated) {
            setBoardRotation(180);
        } else {
            setBoardRotation(0);
        }
    }

    /**
     * Initializes the gridPane object with the pieces from the GameController.
     * This method should only be called once before starting the game.
     */
    private void addPieces() {
        gridPane.getChildren().clear();
        Map<Piece, Position> pieces = controller.getAllActivePiecesPositions();
        /* Add the tiles */
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Tile tile = new TileView(new Position(row, col));
                gridPane.add(tile.getRootNode(),
                        1 + tile.getPosition().getCol(),
                        1 + tile.getPosition().getRow());
                GridPane.setHgrow(tile.getRootNode(), Priority.ALWAYS);
                GridPane.setVgrow(tile.getRootNode(), Priority.ALWAYS);
                getTiles()[row][col] = tile;
                tile.getRootNode().setOnMouseClicked(
                        tileListener(tile));
                tile.clear();
                tile.setSymbol("");
            }
        }
        /* Add the pieces */
        for (Piece p : pieces.keySet()) {
            Position placeAt = pieces.get(p);
            getTileAt(placeAt).setSymbol(p.getType().getSymbol(p.getSide()));
        }
        /* Add the coordinates around the perimeter */
        for (int i = 1; i <= 8; i++) {
            Text coord1 = new Text((char) (64 + i) + "");
            GridPane.setHalignment(coord1, HPos.CENTER);
            gridPane.add(coord1, i, 0);

            Text coord2 = new Text((char) (64 + i) + "");
            GridPane.setHalignment(coord2, HPos.CENTER);
            gridPane.add(coord2, i, 9);

            Text coord3 = new Text(9 - i + "");
            GridPane.setHalignment(coord3, HPos.CENTER);
            gridPane.add(coord3, 0, i);

            Text coord4 = new Text(9 - i + "");
            GridPane.setHalignment(coord4, HPos.CENTER);
            gridPane.add(coord4, 9, i);
        }
    }

    private void setBoardRotation(int degrees) {
        gridPane.setRotate(degrees);
        for (Node n : gridPane.getChildren()) {
            n.setRotate(degrees);
        }
    }

    /**
     * Gets the view to add to the scene graph
     * @return A pane that is the node for the chess board
     */
    public Pane getView() {
        return gridPane;
    }

    /**
     * Gets the tiles that belong to this board view
     * @return A 2d array of TileView objects
     */
    public Tile[][] getTiles() {
        return tiles;
    }

    private Tile getTileAt(int row, int col) {
        return getTiles()[row][col];
    }

    private Tile getTileAt(Position p) {
        return getTileAt(p.getRow(), p.getCol());
    }

}
