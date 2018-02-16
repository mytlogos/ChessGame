package chessGame.engine;

import chessGame.mechanics.Figure;
import chessGame.mechanics.board.Board;
import chessGame.mechanics.move.PlayerMove;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
class SearchItem {
    private final Board<Figure> before;
    private final Board<Figure> after;
    private final PlayerMove move;
    private final List<SearchItem> children = new ArrayList<>();
    private final IntegerProperty depth = new SimpleIntegerProperty();
    private int level;
    private final int personalRating;
    private int maxChildRating;
    private SearchItem maxChild;
    private boolean searching;

    SearchItem(Board<Figure> before, Board<Figure> after, int level, PlayerMove move, int rating) {
        this.before = before;
        this.after = after;
        this.level = level;
        this.move = move;
        this.personalRating = rating;
    }

    public int getDepth() {
        return depth.get();
    }

    public ReadOnlyIntegerProperty depthProperty() {
        return depth;
    }

    public int getPersonalRating() {
        return personalRating;
    }

    public int getMaxChildRating() {
        return maxChildRating;
    }

    @Override
    public String toString() {
        return "SearchItem{" +
                "depth=" + depth.get() +
                ", level=" + level +
                ", move=" + move +
                ", personalRating=" + personalRating +
                ", maxChildRating=" + maxChildRating +
                ", maxChild=" + maxChild +
                ", searching=" + searching +
                '}';
    }

    void addChildren(SearchItem level) {
//        children.add(level);

        //if own level is even, the starting player´s enemy needs to minimize his rating,
        // on odd it needs to maximize its own
        if (this.level % 2 == 0) {
            if (maxChildRating > level.personalRating) {

                maxChildRating = level.personalRating;
                maxChild = level;
                if (depth.get() == 0) {
                    depth.bind(level.depth.add(1));
                }
            }

        } else {
            if (maxChildRating < level.personalRating) {

                maxChildRating = level.personalRating;
                maxChild = level;
                if (depth.get() == 0) {
                    depth.bind(level.depth.add(1));
                }
            }
        }
    }

    Board<Figure> getAfter() {
        return after;
    }

    Board<Figure> getBefore() {
        return before;
    }

    int getLevel() {
        return level;
    }

    PlayerMove getMove() {
        return move;
    }

    SearchItem getMaxChild() {
        return maxChild;
    }

    List<SearchItem> getChildren() {
        return children;
    }

    void resetLevel() {
        level = 0;
    }

    boolean isSearching() {
        return searching;
    }

    void setSearchingFinished() {
        searching = false;
    }

    int getTotalScore() {
        final int i = personalRating + maxChildRating;
        System.out.println("Score " + i + " for " + move);
        return i;
    }
}
