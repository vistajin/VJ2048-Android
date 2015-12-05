package vista.vj2048;

/**
 * Created by Vista on 2014/8/23.
 */
public class SavedGameRecord {
    public static final String RECORD_SEPARATOR = "#";

    private static final String PARAM_SEPARATOR = ":";
    private static final String CELL_VAL_SEPARATOR = ",";
    //{game name2}:1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16:{score2}:{undo times}

    private int id;
    private String gameName;
    private int[][] cellValues;
    private long score;
    private long undoTimes;

    public SavedGameRecord() {

    }

    public SavedGameRecord(String recordString) {
        String params[] = recordString.split(PARAM_SEPARATOR);
        gameName = params[0];
        score = Long.parseLong(params[2]);
        undoTimes = Long.parseLong(params[3]);
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
        return sb.toString();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public long getUndoTimes() {
        return undoTimes;
    }

    public void setUndoTimes(long undoTimes) {
        this.undoTimes = undoTimes;
    }
}
