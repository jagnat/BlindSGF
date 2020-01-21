package com.jagnat.blindsgf;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.ParcelFileDescriptor;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.jagnat.golib.GameNode;
import com.jagnat.golib.SgfReader;

import java.io.FileDescriptor;
import java.io.FileInputStream;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public GameNode rootNode = null;
    public GameNode currentNode = null;
    public int moveNo = -1;

    public class SgfLoadTask extends AsyncTask<Object, String, String> {

        @Override
        protected String doInBackground(Object... objects) {
            Uri path = (Uri) objects[0];
            try {
                ParcelFileDescriptor pfd = getContentResolver().openFileDescriptor(path, "r");
                FileDescriptor fd = pfd.getFileDescriptor();
                FileInputStream is = new FileInputStream(fd);
                SgfReader reader = new SgfReader();
                rootNode = reader.parseFromStream(is);
                currentNode = rootNode;
                moveNo = 0;
                DisplayCurrentNode();
            } catch (Exception e) {
                return null;
            }
            return "success";
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        findViewById(R.id.prevBtn).setOnClickListener(this);
        findViewById(R.id.nextBtn).setOnClickListener(this);

        if (getIntent().getAction().equals(Intent.ACTION_VIEW)) {
            Uri path = getIntent().getData() ;
            try {
                ParcelFileDescriptor pfd = getContentResolver().openFileDescriptor(path, "r");
                FileDescriptor fd = pfd.getFileDescriptor();
                FileInputStream is = new FileInputStream(fd);
                SgfReader reader = new SgfReader();
                rootNode = reader.parseFromStream(is);
                currentNode = rootNode;
                moveNo = 0;
                DisplayCurrentNode();
            } catch (Exception e) {

            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_open) {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("*/*");
            startActivityForResult(intent, 42);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        if (requestCode == 42 && resultCode == Activity.RESULT_OK)
        {
            Uri testUri = null;
            if (resultData != null) {
                testUri = resultData.getData();
                SgfLoadTask myLoadTask = new SgfLoadTask();
                myLoadTask.doInBackground(testUri);
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.prevBtn:
                if (currentNode != null && currentNode.parent != null) {
                    moveNo--;
                    currentNode = currentNode.parent;
                    DisplayCurrentNode();
                }
                break;
            case R.id.nextBtn:
                if (currentNode != null && currentNode.children.size() != 0) {
                    moveNo++;
                    currentNode = currentNode.children.get(0);
                    DisplayCurrentNode();
                }
                break;
            default: break;
        }
    }

    public void DisplayCurrentNode() {
        TextView moveView = findViewById(R.id.moveView);
        TextView commentView = findViewById(R.id.commentView);
        GobanView gameView = findViewById(R.id.gobanView);

        if (currentNode == null) {
            moveView.setText(" ");
            commentView.setText(" ");
        }

        gameView.setLastMove(-1, -1, GameNode.Color.BLACK);

        commentView.setText(currentNode.comment);
        String moveStr = moveNo >= 0? (String.valueOf(moveNo) + ": ") : "";
        String color = currentNode.moveColor == GameNode.Color.BLACK? "Black" : "White";
        switch(currentNode.type)
        {
            case NONE:
                break;
            case MOVE:
                moveStr += color + " " + currentNode.movePos.x + ", " + currentNode.movePos.y;
                gameView.setLastMove(currentNode.movePos.x, currentNode.movePos.y, currentNode.moveColor);
                break;
            case PASS:
                moveStr += color + " passes";
                break;
            case RESIGN:
                moveStr +=  color + " resigns";
                break;
        }
        moveView.setText(moveStr);
        gameView.invalidate();
    }
}
