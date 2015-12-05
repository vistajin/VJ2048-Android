package vista.vj2048;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Vista on 2014/8/24.
 */
public class DBHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "2048.db";
    private static final int DB_VERSION = 1;

    public DBHelper(Context ctx) {
        super(ctx, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("create table if not exists saved_game " +
            "(id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "gameName VARCHAR, cellValues VARCHAR, score INTEGER, undoTimes INTEGER)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {

    }
}
