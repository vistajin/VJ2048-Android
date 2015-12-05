package vista.vj2048;

/**
 * Created by Vista on 2014/8/23.
 */
public class State {
    private int[][] cellValue = new int[4][4];
    private int score;

    public int[][] getCellValue() {
        return cellValue;
    }

    public void setCellValue(int[][] cellValue) {
        this.cellValue = cellValue;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }
}
