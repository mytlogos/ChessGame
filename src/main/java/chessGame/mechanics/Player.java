package chessGame.mechanics;

import chessGame.figures.Figure;

import java.util.Collection;

/**
 *
 */
public enum Player {
    WHITE,
    BLACK,;

    private Collection<Figure> figures;

    public void setFigures(Collection<Figure> figures) {
        this.figures = figures;
    }

    public Collection<Figure> getFigures() {
        return figures;
    }
}
