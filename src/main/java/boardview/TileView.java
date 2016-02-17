package boardview;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.text.Font;
import javafx.scene.control.Label;
import javafx.scene.shape.Rectangle;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import model.Position;

/**
 * View class for a tile on a chess board
 * A tile should be able to display a chess piece
 * as well as highlight itself during the game.
 *
 * @author Will Brewer
 */
public class TileView implements Tile {
    private Position p;
    private StackPane pane;
    private Color highlight;
    private Color background;
    private Label label = new Label("");
    private static boolean offSet = false;
    private static int count = 0;

    /**
     * Creates a TileView with a specified position
     * @param p
     */
    public TileView(Position p) {
        this.p = p;
        pane = new StackPane();
        highlight = Color.TRANSPARENT;

        if (count == 8) {
            count = 0;
        } else {
            offSet = !offSet;
        }

        if (offSet) {
            background = Color.WHITE;
        } else {
            background = Color.GREY;
        }

        pane.setBackground(new Background(new BackgroundFill(background
            , new CornerRadii(0), new Insets(0))));
        count++;
    }


    @Override
    public Position getPosition() {
        return p;
    }


    @Override
    public Node getRootNode() {
        pane.getChildren().clear();
        Rectangle rect = new Rectangle(80, 80, highlight);
        pane.getChildren().addAll(rect, label);
        return pane;
    }

    @Override
    public void setSymbol(String symbol) {
        label.setText(symbol);
        label.setFont(new Font(50));
    }


    @Override
    public String getSymbol() {
        return label.getText();
    }

    @Override
    public void highlight(Color color) {
        highlight = color;
        getRootNode();
    }

    @Override
    public void clear() {
        highlight = Color.TRANSPARENT;
        getRootNode();
    }
}
