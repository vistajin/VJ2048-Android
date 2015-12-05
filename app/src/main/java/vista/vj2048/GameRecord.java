package vista.vj2048;

/**
 * Created by Vista on 2014/8/23.
 */
public class GameRecord {
    public static final String RECORD_SEPARATOR = "#";
    public static final String PARAM_SEPARATOR = ":";
    public static final String CELL_VAL_SEPARATOR = ",";
    //{game name}:1,2,3...15,16:{score}:{undo times}:{undoable}# next record
    private String gameName;
    private int[][] cellValues;
    private long score;
    private int undoTimes;
    private int undoable;

    public GameRecord() {

    }

    public GameRecord(String recordString) {
        String params[] = recordString.split(PARAM_SEPARATOR);
        gameName = params[0];
        score = Long.parseLong(params[2]);
        undoTimes = Integer.parseInt(params[3]);
        undoable = Integer.parseInt(params[4]);
        String values[] = params[1].split(CELL_VAL_SEPARATOR);
        int index = 0;
        cellValues = new int[4][4];
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                cellValues[i][j] = Integer.parseInt(values[index++]);
            }
        }
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(gameName);
        sb.append(PARAM_SEPARATOR);
        for (int i = 0; i < 4; i++)  {
            for (int j = 0; j < 4; j++) {
                sb.append(cellValues[i][j]);
                sb.append(CELL_VAL_SEPARATOR);
            }
        }
        sb.deleteCharAt(sb.length() - 1);
        sb.append(PARAM_SEPARATOR);
        sb.append(score);
        sb.append(PARAM_SEPARATOR);
        sb.append(undoTimes);
        sb.append(PARAM_SEPARATOR);
        sb.append(undoable);
        return sb.toString();
    }

    public String getGameName() {
        return gameName;
    }

    public void setGameName(String gameName) {
        this.gameName = gameName;
    }

    public int[][] getCellValues() {
        return cellValues;
    }

    public void setCellValues(int[][] cellValues) {
        this.cellValues = cellValues;
    }

    public long getScore() {
        return score;
    }

    public void setScore(long score) {
        this.score = score;
    }

    public int getUndoTimes() {
        return undoTimes;
    }

    public void setUndoTimes(int undoTimes) {
        this.undoTimes = undoTimes;
    }

    public int getUndoable() {
        return undoable;
    }

    public void setUndoable(int undoable) {
        this.undoable = undoable;
    }
}
