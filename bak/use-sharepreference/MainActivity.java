package vista.vj2048;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;


public class MainActivity extends Activity {

    // TODO： save game / switch cell

    // for save game
    private static final String SAVED_GAME = "SAVED_GAME";
    private EditText saveNameText;
    private SavedGameRecord savedGameRecords[];
    private SavedGameRecord loadGameRecord;

    private static final String SHARE_VJ2048_DATA = "vista.vj2048.score";
    private static final String HIGHEST_SCORE = "HIGHEST_SCORE";
    private long highestScore = 0;

    private int[][] cellValue = new int[4][4];
    TextView[][] cells = new TextView[4][4];
    private long score = 0;
    private GridLayout gridLayout;
    private TextView scoreText;
    private TextView highestScoreText;
    private static int BLOCK_SIZE;

    private SharedPreferences vj2048File;
    private Animation newCellAnimation;
    private int newCellPosX;
    private int newCellPosY;

    private static final int MOVE_RIGHT = 1;
    private static final int MOVE_LEFT = 2;
    private static final int MOVE_UP = 3;
    private static final int MOVE_DOWN = 4;

    private static final Map<String, Integer> VAL_COLOR_MAP = new HashMap<String, Integer>();

    // for undo
    private int MAX_BACK_STEP = 5;
    private State cachedSteps[] = new State[MAX_BACK_STEP];
    private int backStep = 0;

    private int[][] lastCellValue = new int[4][4];
    private long lastScore;

    private long undoTime = 0;
    private static final long MAX_UNDO = 99999;
    private MenuItem undoMenuItem;

    static {
        VAL_COLOR_MAP.put("0", R.drawable.textview_radius_0);
        VAL_COLOR_MAP.put("2", R.drawable.textview_radius_2);
        VAL_COLOR_MAP.put("4", R.drawable.textview_radius_4);
        VAL_COLOR_MAP.put("8", R.drawable.textview_radius_8);
        VAL_COLOR_MAP.put("16", R.drawable.textview_radius_16);
        VAL_COLOR_MAP.put("32", R.drawable.textview_radius_32);
        VAL_COLOR_MAP.put("64", R.drawable.textview_radius_64);
        VAL_COLOR_MAP.put("128", R.drawable.textview_radius_128);
        VAL_COLOR_MAP.put("256", R.drawable.textview_radius_256);
        VAL_COLOR_MAP.put("512", R.drawable.textview_radius_512);
        VAL_COLOR_MAP.put("1024", R.drawable.textview_radius_1024);
        VAL_COLOR_MAP.put("2048", R.drawable.textview_radius_2048);
        VAL_COLOR_MAP.put("4096", R.drawable.textview_radius_4096);
        VAL_COLOR_MAP.put("8192", R.drawable.textview_radius_8192);
        VAL_COLOR_MAP.put("16384", R.drawable.textview_radius_16384);
        VAL_COLOR_MAP.put("32768", R.drawable.textview_radius_32768);
        VAL_COLOR_MAP.put("65536", R.drawable.textview_radius_65536);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        newCellAnimation = new ScaleAnimation(0.0f, 1.0f, 0.0f, 1.0f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        newCellAnimation.setDuration(200);

        vj2048File = getSharedPreferences(SHARE_VJ2048_DATA, MODE_PRIVATE);
        highestScore = vj2048File.getInt(HIGHEST_SCORE, 0);

        Point size = new Point();
        getWindowManager().getDefaultDisplay().getSize(size);
        BLOCK_SIZE = (size.x / 4);

        scoreText = (TextView) findViewById(R.id.textScore);
        highestScoreText = (TextView) findViewById(R.id.textHighestScore);
        gridLayout = (GridLayout) findViewById(R.id.root);
        gridLayout.setOnTouchListener(new OnSwipeTouchListener(MainActivity.this) {
            @Override
            public void onSwipeTop() {
                //Toast.makeText(MainActivity.this, "top", Toast.LENGTH_SHORT).show();
                move(MOVE_UP);
            }

            @Override
            public void onSwipeRight() {
                move(MOVE_RIGHT);
            }

            @Override
            public void onSwipeLeft() {
                move(MOVE_LEFT);
            }

            @Override
            public void onSwipeBottom() {
                move(MOVE_DOWN);
            }

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return gestureDetector.onTouchEvent(event);
            }
        });
        initCells();
        restart();
    }

    private void initCells() {
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                GridLayout.LayoutParams params = new GridLayout.LayoutParams(GridLayout.spec(i), GridLayout.spec(j));
                params.width = BLOCK_SIZE - 10;
                params.height = BLOCK_SIZE - 10;
                params.setMargins(4, 4, 4, 4);

                cells[i][j] = new TextView(this);
                cells[i][j].setLayoutParams(params);
                cells[i][j].setGravity(Gravity.CENTER);
                cells[i][j].setBackgroundResource(R.drawable.textview_radius_0);
                cells[i][j].setTextSize(40);
                gridLayout.addView(cells[i][j], params);
            }

        }
    }

    private void refresh() {
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                if (newCellPosX == i && newCellPosY == j) {
                    cells[i][j].startAnimation(newCellAnimation);
                }
                if (cellValue[i][j] > 8192) {
                    cells[i][j].setTextSize(32);
                } else {
                    cells[i][j].setTextSize(40);
                }
                cells[i][j].setText(cellValue[i][j] == 0 ? "" : Integer.toString(cellValue[i][j]));
                cells[i][j].setBackgroundResource(VAL_COLOR_MAP.get(Integer.toString(cellValue[i][j])));
            }
        }
        scoreText.setText("分数\n" + score);
        if (highestScore < score) {
            highestScore = score;
            SharedPreferences.Editor editor = vj2048File.edit();
            editor.putLong(HIGHEST_SCORE, highestScore);
            editor.apply();
        }

        highestScoreText.setText("最高分\n" + highestScore);
        checkGameOver();
    }

    private void checkGameOver() {
        if (!getAvailablePos().isEmpty()) {
            return;
        }
        for (int row = 0; row < 4; row++) {
            int curVal = cellValue[row][0];
            for (int col = 1; col < 4; col++) {
                if (cellValue[row][col] == curVal) {
                    return;
                }
                curVal = cellValue[row][col];
            }
        }

        for (int col = 0; col < 4; col++) {
            int curVal = cellValue[0][col];
            for (int row = 1; row < 4; row++) {
                if (cellValue[row][col] == curVal) {
                    return;
                }
                curVal = cellValue[row][col];
            }
        }

        AlertDialog.Builder alertDlgBuilder = new AlertDialog.Builder(MainActivity.this);
        alertDlgBuilder.setTitle("亲，您输了！");
        alertDlgBuilder.setNegativeButton("重新挑战", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                restart();
            }
        });
        alertDlgBuilder.setPositiveButton("分享", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                share();
            }
        });
        AlertDialog alertDialog = alertDlgBuilder.create();
        alertDialog.show();
    }

    private void restart() {
        cellValue = new int[4][4];
        lastCellValue = new int[4][4];
        score = 0;
        lastScore = 0;
        undoTime = 0;
        if (null != undoMenuItem) {
            undoMenuItem.setEnabled(true);
            undoMenuItem.setTitle("反悔(" + MAX_UNDO + ")");
        }

        backStep = 0;
        cachedSteps = new State[MAX_BACK_STEP];

        genNextCell();
        genNextCell();
        refresh();
    }

    private void genNextCell() {
        List<Integer> availablePosList = getAvailablePos();
        int availablePosNum = availablePosList.size();
        if (availablePosNum != 0) {
            int pos = availablePosList.get(new Random().nextInt(availablePosNum));
            newCellPosX = (int) Math.floor(pos / 4);
            newCellPosY = pos % 4;
            cellValue[newCellPosX][newCellPosY] = gen2or4();
        }
    }

    private List<Integer> getAvailablePos() {
        List<Integer> availablePosList = new ArrayList<Integer>();
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                if (cellValue[i][j] == 0) {
                    availablePosList.add(i * 4 + j);
                }
            }
        }
        return availablePosList;
    }

    private int gen2or4() {
        return new Random().nextInt(100) < 70 ? 2 : 4;
    }

    private int[][] copyArray(int [][]org) {
        int[][] copiedData = new int[4][4];
        for (int i = 0; i < 4; i++) {
            System.arraycopy(org[i], 0, copiedData[i], 0, 4);
        }
        return copiedData;
    }

    private boolean isDataChanged(int[][] saveData) {
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                if (saveData[i][j] != cellValue[i][j]) {
                    return true;
                }
            }
        }
        return false;
    }

    private int[] trimBlank(int[] in) {
        int[] ret = new int[4];
        int count = 0;
        for (int i = 0; i < 4; i++) {
            if (in[i] != 0) {
                ret[count++] = in[i];
            }
        }
        return ret;
    }

    private int[] revert(int[] in) {
        int[] ret = new int[4];
        int count = 0;
        for (int i = 3; i >= 0; i--) {
            ret[count++] = in[i];
        }
        return ret;
    }

    private int[] zip(int[] in, boolean rev) {
        int[] ret = trimBlank(in);
        if (rev) {
            ret = revert(ret);
        }
        for (int i = 0; i < 3; i++) {
            if (ret[i] == ret[i + 1]) {
                ret[i] += ret[i];
                score += ret[i];
                ret[++i] = 0;
            }
        }
        ret = trimBlank(ret);
        if (rev) {
            ret = revert(ret);
        }
        return ret;
    }

    private void move(int direction) {
        int[][] savedData = copyArray(cellValue);
        lastScore = score;

        if (direction == MOVE_RIGHT) {
            moveLeftRight(true);
        } else if (direction == MOVE_LEFT) {
            moveLeftRight(false);
        } else if (direction == MOVE_UP) {
            moveUpDown(false);
        } else if (direction == MOVE_DOWN) {
            moveUpDown(true);
        }

        if (isDataChanged(savedData)) {

            if (MAX_UNDO != undoTime) {
                lastCellValue = copyArray(savedData);
                undoMenuItem.setEnabled(true);
            }
            genNextCell();
            refresh();
        }
    }

    private void updateCachedSteps() {

    }

    private void moveLeftRight(boolean isRight) {
        for (int row = 0; row < 4; row++) {
            cellValue[row] = zip(cellValue[row], isRight);
        }
    }

    private void moveUpDown(boolean isDown) {
        for (int col = 0; col < 4; col++) {
            int[] curCol = new int[4];
            for (int row = 0; row < 4; row++) {
                curCol[row] = cellValue[row][col];
            }
            curCol = zip(curCol, isDown);
            for (int row = 0; row < 4; row++) {
                cellValue[row][col] = curCol[row];
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        undoMenuItem = menu.findItem(R.id.action_undo);
        undoMenuItem.setTitle("反悔(" + (MAX_UNDO - undoTime) + ")");

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        return true;//id == R.id.action_settings || super.onOptionsItemSelected(item);
   }

    public void restartAction(MenuItem item) {
        AlertDialog.Builder alertDlgBuilder = new AlertDialog.Builder(MainActivity.this);
        alertDlgBuilder.setTitle("亲，您真的要重新开始吗？");
        alertDlgBuilder.setNegativeButton("不好意思，人家不小心按错了", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alertDlgBuilder.setPositiveButton("重新挑战", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                restart();
            }
        });
        AlertDialog alertDialog = alertDlgBuilder.create();
        alertDialog.show();
    }

    private void share() {
        Intent intent=new Intent(Intent.ACTION_SEND);

        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, "分享");
        intent.putExtra(Intent.EXTRA_TEXT, "我在玩Vista版2048，获得" + score + "分，你也来试试？");
        startActivity(Intent.createChooser(intent, getTitle()));
    }

    public void shareAction(MenuItem item) {
        share();
    }

    public void undoAction(MenuItem item) {
        item.setEnabled(false);
        undo();
        undoTime++;
        item.setTitle("反悔(" + (MAX_UNDO - undoTime) + ")");
    }

    private void undo() {
        newCellPosX = -1;
        newCellPosY = -1;
        cellValue = copyArray(lastCellValue);
        score = lastScore;
        refresh();
    }

    public void saveGameAction(MenuItem item) {
        saveNameText = new EditText(MainActivity.this);
        AlertDialog.Builder alertDlgBuilder = new AlertDialog.Builder(MainActivity.this);
        alertDlgBuilder.setTitle("将当前状态保存为：");
        alertDlgBuilder.setView(saveNameText);
        alertDlgBuilder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String saveName = saveNameText.getText().toString();
                SavedGameRecord record = new SavedGameRecord();
                record.setGameName(saveName);
                record.setCellValues(cellValue);
                record.setScore(score);
                record.setUndoTimes(undoTime);
                //{game name2}:1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16:{score2}:{undo time}
                String savedGameData = vj2048File.getString(SAVED_GAME, null);

                if (savedGameData == null) {
                    savedGameData = record.toString();
                } else {
                    // check if same name exists

                    savedGameData = savedGameData + SavedGameRecord.RECORD_SEPARATOR + record.toString();
                }

                SharedPreferences.Editor editor = vj2048File.edit();
                editor.putString(SAVED_GAME, savedGameData);
                editor.apply();

                //Toast.makeText(MainActivity.this, "已保存" + saveName, Toast.LENGTH_SHORT);

                dialog.dismiss();
            }
        });
        AlertDialog alertDialog = alertDlgBuilder.create();
        alertDialog.show();
    }

    public void loadGameAction(MenuItem item) {
        String savedGameData = vj2048File.getString(SAVED_GAME, null);
        if (null != savedGameData) {
            String []strSavedGameRecords = savedGameData.split(SavedGameRecord.RECORD_SEPARATOR);
            String []savedGameNames = new String[strSavedGameRecords.length];
            savedGameRecords = new SavedGameRecord[strSavedGameRecords.length];
            for (int i = 0; i < savedGameRecords.length; i++) {
                savedGameRecords[i] = new SavedGameRecord(strSavedGameRecords[i]);
                savedGameNames[i] = savedGameRecords[i].getGameName();
            }

            AlertDialog.Builder alertDlgBuilder = new AlertDialog.Builder(MainActivity.this);
            alertDlgBuilder.setTitle("请选择");
            alertDlgBuilder.setSingleChoiceItems(savedGameNames, 0, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    loadGameRecord = savedGameRecords[which];
                }
            });
            alertDlgBuilder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    loadGame(loadGameRecord);
                    dialog.dismiss();
                }
            });
            alertDlgBuilder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            AlertDialog alertDialog = alertDlgBuilder.create();
            alertDialog.show();

        } else {
            AlertDialog.Builder alertDlgBuilder = new AlertDialog.Builder(MainActivity.this);
            alertDlgBuilder.setTitle("亲，您还没有保存过游戏进度呢！");
            alertDlgBuilder.setNegativeButton("知道了！", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            AlertDialog alertDialog = alertDlgBuilder.create();
            alertDialog.show();
        }
    }

    private void loadGame(SavedGameRecord record) {
        cellValue = record.getCellValues();
        score = record.getScore();
        undoTime = record.getUndoTimes();
        refresh();
    }
}
