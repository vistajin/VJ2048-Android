package vista.vj2048;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.sql.SQLException;

/**
 * Created by Vista on 2014/8/24.
 */
public class DBManager {

    private DBHelper helper;
    private SQLiteDatabase db;

    public DBManager(Context ctx) {
        helper = new DBHelper(ctx);
        db = helper.getWritableDatabase();
    }

    public void insertRecord(SavedGameRecord record) {
        db.beginTransaction();
        try {
            db.execSQL("insert into 2048.db values(NULL, ?, ?, ?, ?)",
                    new Object[] {
                            record.getGameName(),
                            record.getCellValues()
                            record.getScore(),
                            record.getUndoTimes()
                    });
            db.setTransactionSuccessful();
        } catch (SQLException e) {
            Log.e("DBManager", e.getMessage());
        } finally {
            db.endTransaction();
        }
    }

    public void updateRecord(String gameName, SavedGameRecord record) {
        ContentValues cv = new ContentValues();
        cv.put("cellValues", record.getCellValues());
        cv.put("score", record.getScore());
        cv.put("undoTimes", record.getUndoTimes());
        db.update("saved_game", cv, "gameName=?", new String[] {gameName});
    }
}
