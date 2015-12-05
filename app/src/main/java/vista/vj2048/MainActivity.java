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
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;


public class MainActivity extends Activity {
    private static int blockSize;
    private GameRecord currentRecord;
    private long highestScore = 0;

    // for save game
    private SharedPreferences vj2048File;
    private EditText saveNameText;
    private GameRecord savedGameRecords[];
    //private GameRecord loadGameRecord;
    private int loadGameIndex;

    private static final String SHARE_VJ2048_DATA = "vista.vj2048.score";
    private static final String KEY_HIGHEST_SCORE = "HIGHEST_SCORE2";
    private static final String KEY_SAVED_GAME = "SAVED_GAMES";
    private static final String KEY_PAUSED_GAME = "PAUSED_GAME";

    // for undo
    private static final int MAX_UNDO = 99;
    private long lastScore;
    private int[][] lastCellValue = new int[4][4];
    private MenuItem undoMenuItem;

    // layout view
    TextView[][] cells = new TextView[4][4];
    private GridLayout gridLayout;
    private TextView scoreText;
    private TextView highestScoreText;

    // for animation
    private Animation newCellAnimation;
    private int newCellPosX;
    private int newCellPosY;

    // Constants
    private static final Map<String, Integer> VAL_COLOR_MAP = new HashMap<String, Integer>();
    private static final int MOVE_RIGHT = 1;
    private static final int MOVE_LEFT = 2;
    private static final int MOVE_UP = 3;
    private static final int MOVE_DOWN = 4;
    //private boolean isOutDate = false;

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

        // init block size by screen size
        Point size = new Point();
        getWindowManager().getDefaultDisplay().getSize(size);
        blockSize = (size.x / 4);

        // init control view
        scoreText = (TextView) findViewById(R.id.textScore);
        highestScoreText = (TextView) findViewById(R.id.textHighestScore);
        gridLayout = (GridLayout) findViewById(R.id.root);

        // init swipe gesture
        findViewById(R.id.main).setOnTouchListener(new OnSwipeTouchListener(MainActivity.this) {
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

        // init animation parameters
        newCellAnimation = new ScaleAnimation(0.0f, 1.0f, 0.0f, 1.0f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        newCellAnimation.setDuration(200);

        // load sharedpreferences file
        vj2048File = getSharedPreferences(SHARE_VJ2048_DATA, MODE_PRIVATE);
        highestScore = Long.parseLong(vj2048File.getString(KEY_HIGHEST_SCORE, "0"));

        String pausedGameData = vj2048File.getString(KEY_PAUSED_GAME, null);
        if (pausedGameData == null) {
            restart();
        } else {
            loadGame(new GameRecord(pausedGameData));
        }


        /*
        // outdate control
        Date today = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        if (sdf.format(today).compareTo("2014-09-30") > 0) {
            isOutDate = true;
            AlertDialog.Builder alertDlgBuilder = new AlertDialog.Builder(MainActivity.this);
            alertDlgBuilder.setTitle("不好意思，该试用版已到期");
            alertDlgBuilder.setNegativeButton("如需继续使用，请联系528189@qq.com", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    restart();
                }
            });
            AlertDialog alertDialog = alertDlgBuilder.create();
            alertDialog.show();
        }
        */
    }

    private void initCells() {
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                GridLayout.LayoutParams params = new GridLayout.LayoutParams(GridLayout.spec(i), GridLayout.spec(j));
                params.width = blockSize - 10;
                params.height = blockSize - 10;
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
        //if (isOutDate) {
        //    return;
        //}
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                if (newCellPosX == i && newCellPosY == j) {
                    cells[i][j].startAnimation(newCellAnimation);
                }
                if (currentRecord.getCellValues()[i][j] > 8192) {
                    cells[i][j].setTextSize(32);
                } else {
                    cells[i][j].setTextSize(40);
                }
                cells[i][j].setText(currentRecord.getCellValues()[i][j] == 0 ?
                        "" : Integer.toString(currentRecord.getCellValues()[i][j]));
                cells[i][j].setBackgroundResource(
                        VAL_COLOR_MAP.get(Integer.toString(currentRecord.getCellValues()[i][j])));
            }
        }
        scoreText.setText("分数\n" + currentRecord.getScore());
        if (highestScore < currentRecord.getScore()) {
            highestScore = currentRecord.getScore();
            SharedPreferences.Editor editor = vj2048File.edit();
            editor.putString(KEY_HIGHEST_SCORE, Long.toString(highestScore));
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
            int curVal = currentRecord.getCellValues()[row][0];
            for (int col = 1; col < 4; col++) {
                if (currentRecord.getCellValues()[row][col] == curVal) {
                    return;
                }
                curVal = currentRecord.getCellValues()[row][col];
            }
        }

        for (int col = 0; col < 4; col++) {
            int curVal = currentRecord.getCellValues()[0][col];
            for (int row = 1; row < 4; row++) {
                if (currentRecord.getCellValues()[row][col] == curVal) {
                    return;
                }
                curVal = currentRecord.getCellValues()[row][col];
            }
        }

        AlertDialog.Builder alertDlgBuilder = new AlertDialog.Builder(MainActivity.this);
        alertDlgBuilder.setTitle("您输了！");
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
        currentRecord = new GameRecord();
        currentRecord.setCellValues(new int[4][4]);
        currentRecord.setUndoTimes(0);
        currentRecord.setScore(0);
        currentRecord.setUndoable(1);

        lastCellValue = new int[4][4];
        lastScore = 0;

        if (null != undoMenuItem) {
            undoMenuItem.setEnabled(true);
            undoMenuItem.setTitle("反悔: " + MAX_UNDO);
        }

        //backStep = 0;
        //cachedSteps = new State[MAX_BACK_STEP];

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
            currentRecord.getCellValues()[newCellPosX][newCellPosY] = gen2or4();
        }
    }

    private List<Integer> getAvailablePos() {
        List<Integer> availablePosList = new ArrayList<Integer>();
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                if (currentRecord.getCellValues()[i][j] == 0) {
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
                if (saveData[i][j] != currentRecord.getCellValues()[i][j]) {
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
                currentRecord.setScore(currentRecord.getScore() + ret[i]);
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
        int[][] savedData = copyArray(currentRecord.getCellValues());
        lastScore = currentRecord.getScore();

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

            if (MAX_UNDO != currentRecord.getUndoTimes()) {
                lastCellValue = copyArray(savedData);
                undoMenuItem.setEnabled(true);
                currentRecord.setUndoable(1);
            }
            genNextCell();
            refresh();
        }
    }

/*    private void updateCachedSteps() {

    }*/

    private void moveLeftRight(boolean isRight) {
        for (int row = 0; row < 4; row++) {
            currentRecord.getCellValues()[row] = zip(currentRecord.getCellValues()[row], isRight);
        }
    }

    private void moveUpDown(boolean isDown) {
        for (int col = 0; col < 4; col++) {
            int[] curCol = new int[4];
            for (int row = 0; row < 4; row++) {
                curCol[row] = currentRecord.getCellValues()[row][col];
            }
            curCol = zip(curCol, isDown);
            for (int row = 0; row < 4; row++) {
                currentRecord.getCellValues()[row][col] = curCol[row];
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        undoMenuItem = menu.findItem(R.id.action_undo);
        undoMenuItem.setTitle("反悔: " + (MAX_UNDO - currentRecord.getUndoTimes()));
        undoMenuItem.setEnabled(currentRecord.getUndoable() == 1);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        //int id = item.getItemId();
        return true;//id == R.id.action_settings || super.onOptionsItemSelected(item);
   }

    public void restartAction(MenuItem item) {
        AlertDialog.Builder alertDlgBuilder = new AlertDialog.Builder(MainActivity.this);
        alertDlgBuilder.setTitle("真的要重新开始？");
        alertDlgBuilder.setNegativeButton("按错了", new DialogInterface.OnClickListener() {
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
        intent.putExtra(Intent.EXTRA_TEXT, "我在玩Vista版2048，获得" + currentRecord.getScore() + "分，你也来试试？");
        startActivity(Intent.createChooser(intent, getTitle()));
    }

    public void shareAction(MenuItem item) {
        share();
    }

    public void undoAction(MenuItem item) {
        item.setEnabled(false);
        newCellPosX = -1;
        newCellPosY = -1;
        currentRecord.setCellValues(copyArray(lastCellValue));
        currentRecord.setScore(lastScore);
        refresh();
        currentRecord.setUndoTimes(currentRecord.getUndoTimes() + 1);
        currentRecord.setUndoable(0);
        item.setTitle("反悔: " + (MAX_UNDO - currentRecord.getUndoTimes()));
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
                if (saveName.trim().equals("")) {
                    Toast.makeText(MainActivity.this, "未输入名称！", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (saveName.contains(GameRecord.RECORD_SEPARATOR)
                        || saveName.contains(GameRecord.PARAM_SEPARATOR)
                        || saveName.contains(GameRecord.CELL_VAL_SEPARATOR)) {
                    Toast.makeText(MainActivity.this, "不能包含下列字符：#:,", Toast.LENGTH_SHORT).show();
                    return;
                }
                currentRecord.setGameName(saveName);
                String savedGameData = vj2048File.getString(KEY_SAVED_GAME, null);

                if (savedGameData == null || savedGameData.equals("")) {
                    savedGameData = currentRecord.toString();
                } else {
                    // check if same name exists
                    String []strSavedGameRecords = savedGameData.split(GameRecord.RECORD_SEPARATOR);
                    String []savedGameNames = new String[strSavedGameRecords.length];
                    savedGameRecords = new GameRecord[strSavedGameRecords.length];
                    for (int i = 0; i < savedGameRecords.length; i++) {
                        savedGameRecords[i] = new GameRecord(strSavedGameRecords[i]);
                        savedGameNames[i] = savedGameRecords[i].getGameName();
                        if (savedGameNames[i].equals(saveName)) {
                            Toast.makeText(MainActivity.this, saveName + "已存在！", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                    savedGameData = savedGameData + GameRecord.RECORD_SEPARATOR + currentRecord.toString();
                }
                SharedPreferences.Editor editor = vj2048File.edit();
                editor.putString(KEY_SAVED_GAME, savedGameData);
                editor.apply();

                Toast.makeText(MainActivity.this, "已保存" + saveName, Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });
        AlertDialog alertDialog = alertDlgBuilder.create();
        alertDialog.show();
    }

    public void loadGameAction(MenuItem item) {
        String savedGameData = vj2048File.getString(KEY_SAVED_GAME, null);
        if (null != savedGameData && !savedGameData.equals("")) {
            String []strSavedGameRecords = savedGameData.split(GameRecord.RECORD_SEPARATOR);
            String []savedGameNames = new String[strSavedGameRecords.length];
            savedGameRecords = new GameRecord[strSavedGameRecords.length];
            for (int i = 0; i < savedGameRecords.length; i++) {
                savedGameRecords[i] = new GameRecord(strSavedGameRecords[i]);
                savedGameNames[i] = savedGameRecords[i].getGameName();
            }
            loadGameIndex = 0; // default select the first record

            AlertDialog.Builder alertDlgBuilder = new AlertDialog.Builder(MainActivity.this);
            alertDlgBuilder.setTitle("请选择");
            alertDlgBuilder.setSingleChoiceItems(savedGameNames, 0, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    loadGameIndex = which;
                }
            });
            alertDlgBuilder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    GameRecord loadGameRecord = savedGameRecords[loadGameIndex];
                    loadGameRecord.setUndoable(0); // 读取记录第一次总是不能反悔
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
            alertDlgBuilder.setTitle("您还没有保存过游戏进度呢！");
            alertDlgBuilder.setNegativeButton("知道了", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            AlertDialog alertDialog = alertDlgBuilder.create();
            alertDialog.show();
        }
    }

    private void loadGame(GameRecord record) {
        currentRecord = record;
        if (undoMenuItem != null) {
            undoMenuItem.setTitle("反悔: " + (MAX_UNDO - currentRecord.getUndoTimes()));
            undoMenuItem.setEnabled(currentRecord.getUndoable() == 1);
        }
        newCellPosX = -1;
        newCellPosY = -1;
        refresh();
    }

    public void showInfoAction(MenuItem item) {
        AlertDialog.Builder alertDlgBuilder = new AlertDialog.Builder(this);
        alertDlgBuilder.setTitle("2048");
        alertDlgBuilder.setMessage("作者：\t\tVista JIN\r\n" +
                "联系：\t\tQQ528189\r\n" +
                "版本：\t\t1.0.0");
        alertDlgBuilder.setNegativeButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog alertDialog = alertDlgBuilder.create();
        alertDialog.show();
    }

    @Override
    public void onPause() {
        super.onPause();

        // save game record before leave
        GameRecord pausedGameRecord = currentRecord;
        pausedGameRecord.setGameName("PausedGame");

        SharedPreferences.Editor editor = vj2048File.edit();
        editor.putString(KEY_PAUSED_GAME, pausedGameRecord.toString());
        editor.apply();
    }

    public void maintainGameAction(MenuItem item) {
        String savedGameData = vj2048File.getString(KEY_SAVED_GAME, null);
        if (null != savedGameData && !"".equals(savedGameData)) {
            String []strSavedGameRecords = savedGameData.split(GameRecord.RECORD_SEPARATOR);
            String []savedGameNames = new String[strSavedGameRecords.length];
            savedGameRecords = new GameRecord[strSavedGameRecords.length];
            for (int i = 0; i < savedGameRecords.length; i++) {
                savedGameRecords[i] = new GameRecord(strSavedGameRecords[i]);
                savedGameNames[i] = savedGameRecords[i].getGameName();
            }

            loadGameIndex = 0; // default select the first record

            AlertDialog.Builder alertDlgBuilder = new AlertDialog.Builder(MainActivity.this);
            alertDlgBuilder.setTitle("请选择");
            alertDlgBuilder.setSingleChoiceItems(savedGameNames, 0, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    loadGameIndex = which;
                }
            });
            alertDlgBuilder.setPositiveButton("删除", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String savedGameData = "";
                    for (int i = 0; i < savedGameRecords.length; i++) {
                        if (i != loadGameIndex) {
                            if (savedGameData.equals("")) {
                                savedGameData = savedGameRecords[i].toString();
                            } else {
                                savedGameData += GameRecord.RECORD_SEPARATOR + savedGameRecords[i].toString();
                            }
                        }
                    }
                    SharedPreferences.Editor editor = vj2048File.edit();
                    editor.putString(KEY_SAVED_GAME, savedGameData);
                    editor.apply();
                    Toast.makeText(MainActivity.this, "已删除" + savedGameRecords[loadGameIndex].getGameName(), Toast.LENGTH_SHORT).show();
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
            alertDlgBuilder.setTitle("您还没有保存过游戏进度呢！");
            alertDlgBuilder.setNegativeButton("知道了", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            AlertDialog alertDialog = alertDlgBuilder.create();
            alertDialog.show();
        }
    }

}
