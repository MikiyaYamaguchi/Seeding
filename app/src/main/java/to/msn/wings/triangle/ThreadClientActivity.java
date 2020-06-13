package to.msn.wings.triangle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Adapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Locale;

import java.util.ArrayList;
import java.util.List;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;

import android.graphics.Paint;
import android.graphics.PorterDuffXfermode;
import android.graphics.PorterDuff;

public class ThreadClientActivity extends FragmentActivity {

    static class Thread {

        private String m_strThreadName;
        private String m_strThreadCategory;

        public Thread (String strThreadName, String strThreadCategory){
            m_strThreadName = strThreadName;
            m_strThreadCategory = strThreadCategory;
        }

        public String getThreadName (){
            return m_strThreadName;
        }

        public String getThreadCategory (){
            return m_strThreadCategory;
        }

    }

    private String m_strName = "";
    private Socket m_socket;

    //定数
    public static final String EXTRA_NAME = "NAME";
    private static final String URI_SERVER = "https://triangleapps.herokuapp.com/";

    private static String ThreadName;
    private static String ThreadCategory;
    private boolean thread_on = false;

    private List<String> itemThreds = new ArrayList<String>();
    private List<String> itemThreadsCategorys = new ArrayList<String>();

    private RecyclerView.Adapter rAdapter;
    private RecyclerView.LayoutManager rLayoutManager;
    private RecyclerView recyclerView;

    private int pinCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thread);

        //呼び出し元からパラメータ取得
        Bundle extras = getIntent().getExtras();
        if(null != extras){
            m_strName = extras.getString(EXTRA_NAME);
        }

        //サーバーとの接続
        try{
            m_socket = IO.socket(URI_SERVER);
        } catch (URISyntaxException e){
            //IO.socketの失敗
            e.printStackTrace();
            Toast.makeText(this, "URI is invalid", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        m_socket.connect(); //接続

        //接続完了時の処理
        m_socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                runOnUiThread(new Runnable(){
                    @Override
                    public void run(){
                        Toast.makeText(ThreadClientActivity.this, "Connected", Toast.LENGTH_SHORT).show();
                        //サーバーに、イベント名"join"で名前を送信
                        m_socket.emit("join", m_strName);
                    }
                });
            }
        });

        //接続エラー時の処理
        m_socket.on(Socket.EVENT_CONNECT_ERROR, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(ThreadClientActivity.this, "Connection error", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }
                });
            }
        });

        //接続タイムアウト時の処理
        m_socket.on(Socket.EVENT_CONNECT_TIMEOUT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(ThreadClientActivity.this, "Connection timeout", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
            }
        });

        //接続時の処理
        m_socket.on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(ThreadClientActivity.this, "Disconnected", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        ImageView thread_add_btn = (ImageView)findViewById(R.id.thread_add);

        //スレッド作成ダイアログボック生成
        thread_add_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater inflater = LayoutInflater.from(ThreadClientActivity.this);
                AlertDialog.Builder thread_dialog = new AlertDialog.Builder(ThreadClientActivity.this);
                final View dialog_view = inflater.inflate(R.layout.thread_dialog,null);
                thread_dialog.setView(dialog_view);
                EditText thread_edit = dialog_view.findViewById(R.id.tread_name);
                thread_dialog.setPositiveButton("OK", new  DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int idx) {
                        EditText txt = (EditText)dialog_view.findViewById(R.id.tread_name);
                        Spinner categorys = (Spinner)dialog_view.findViewById(R.id.thread_category);
                        //スレッド名とスレッドカテゴリーをサーバーへ送信
                        m_socket.emit("thread category",categorys.getSelectedItem().toString());
                        m_socket.emit("new thread",txt.getText());
                    }});
                thread_dialog.show();
            }
        });

        recyclerView = findViewById(R.id.my_recycler_view);
        recyclerView.setHasFixedSize(true);
        rLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(rLayoutManager);
        rAdapter = new MyAdapter(itemThreds,itemThreadsCategorys,m_strName);
        recyclerView.setAdapter(rAdapter);

        //スレッド受信処理
        //サーバーから拡散されてきたスレッド情報をキャッチしてThreadクラスのインスタンス生成
        m_socket.on("spread Thread", new Emitter.Listener(){
            public void call( final Object... args){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String strThreadName = "";
                        String strThreadCategory = "";

                        JSONObject objThread = (JSONObject)args[0];
                        try{
                            strThreadName = objThread.getString("strThreadName");
                            strThreadCategory = objThread.getString("strThreadCategory");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        Thread thread = new Thread(strThreadName, strThreadCategory);
                        if(pinCount > 0){
                            itemThreds.add(pinCount,thread.m_strThreadName);
                            itemThreadsCategorys.add(pinCount,thread.m_strThreadCategory);
                            rAdapter.notifyItemInserted(pinCount);
                        } else {
                            itemThreds.add(0,thread.m_strThreadName);
                            itemThreadsCategorys.add(0,thread.m_strThreadCategory);
                            rAdapter.notifyItemInserted(0);
                        }
                    }
                });
            }
        });


        //スワイプ処理
        final Drawable deleteIcon = ContextCompat.getDrawable(this, R.drawable.ic_delete);
        final Drawable pinIcon = ContextCompat.getDrawable(this, R.drawable.ic_flag);

        //左スワイプ（削除）
        ItemTouchHelper.SimpleCallback callback = new ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
                int swipedPosition = viewHolder.getAdapterPosition();
                MyAdapter adapter = (MyAdapter) recyclerView.getAdapter();
                itemThreds.remove(swipedPosition);
                if(swipedPosition < pinCount){
                    pinCount--;
                    if(swipedPosition <= 0){
                        pinCount = 0;
                    }
                }
                itemThreadsCategorys.remove(swipedPosition);
                adapter.notifyItemRemoved(swipedPosition);
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive){
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                View itemView = viewHolder.itemView;

                //キャンセル処理
                if(dX == 0f && !isCurrentlyActive){
                    clearCanvas(c, itemView.getRight() + (int) dX, itemView.getTop(), itemView.getRight(), itemView.getBottom());
                    super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, false);
                    return;
                }

                //背景の設定
                ColorDrawable background = new ColorDrawable();
                background.setColor(Color.parseColor("#d1d1d1"));
                background.setBounds(itemView.getRight() + (int)dX, itemView.getTop(), itemView.getRight(), itemView.getBottom());
                background.draw(c);

                //背景に配置するアイコンの設定
                int deleteIconTop = itemView.getTop() + (itemView.getHeight() - deleteIcon.getIntrinsicHeight()) / 2;
                int deleteIconMargin = (itemView.getHeight() - deleteIcon.getIntrinsicHeight()) / 2;
                int deleteIconLeft = itemView.getRight() - deleteIconMargin - deleteIcon.getIntrinsicWidth();
                int deleteIconRight = itemView.getRight() - deleteIconMargin;
                int deleteIconBottom = deleteIconTop +  deleteIcon.getIntrinsicHeight();
                deleteIcon.setBounds(deleteIconLeft, deleteIconTop, deleteIconRight, deleteIconBottom);
                deleteIcon.draw(c);
            }
        };

        new ItemTouchHelper(callback).attachToRecyclerView(recyclerView);

        //右スワイプ（pin立て）
        ItemTouchHelper.SimpleCallback callback_right = new ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
                int swipedPosition = viewHolder.getAdapterPosition();
                MyAdapter adapter = (MyAdapter) recyclerView.getAdapter();
                String swipe_item = itemThreds.get(viewHolder.getAdapterPosition());
                if(swipedPosition < pinCount) {
                    itemThreds.add(pinCount, itemThreds.get(swipedPosition));
                    itemThreadsCategorys.add(pinCount, itemThreadsCategorys.get(swipedPosition));
                    itemThreds.remove(viewHolder.getLayoutPosition());
                    itemThreadsCategorys.remove(viewHolder.getLayoutPosition());
                    pinCount--;
                    if(swipedPosition <= 0){
                        pinCount = 0;
                    }
                } else if(pinCount == 0 || swipe_item != itemThreds.get(0)){
                    itemThreds.add(0, itemThreds.get(swipedPosition));
                    itemThreadsCategorys.add(0, itemThreadsCategorys.get(swipedPosition));
                    itemThreds.remove(viewHolder.getLayoutPosition() + 1);
                    itemThreadsCategorys.remove(viewHolder.getLayoutPosition() + 1);
                    pinCount++;
                }
                adapter.notifyItemRemoved(swipedPosition);
                adapter.notifyItemInserted(0);
                adapter.notifyItemRangeChanged(1, itemThreds.size());
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive){
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                View itemView = viewHolder.itemView;

                //キャンセル処理
                if(dX == 0f && !isCurrentlyActive){
                    clearCanvas(c, itemView.getLeft() + (int) dX, itemView.getTop(), itemView.getLeft(), itemView.getBottom());
                    super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, false);
                    return;
                }

                //背景の設定
                ColorDrawable background = new ColorDrawable();
                background.setColor(Color.parseColor("#7e7f80"));
                background.setBounds(0, itemView.getTop(), (int) (itemView.getLeft() + dX), itemView.getBottom());
                background.draw(c);

                //背景に配置するアイコンの設定
                int pinIconTop = itemView.getTop() + (itemView.getHeight() - deleteIcon.getIntrinsicHeight()) / 2;
                int pinIconMargin = (itemView.getHeight() - deleteIcon.getIntrinsicHeight()) / 2;
                int pinIconLeft = itemView.getLeft() + pinIconMargin;
                int pinIconRight = itemView.getLeft() + (pinIconMargin + deleteIcon.getIntrinsicWidth());
                int pinIconBottom = pinIconTop + deleteIcon.getIntrinsicHeight();

                pinIcon.setBounds(pinIconLeft, pinIconTop, pinIconRight, pinIconBottom);
                pinIcon.draw(c);
            }
        };

        new ItemTouchHelper(callback_right).attachToRecyclerView(recyclerView);

        if(thread_on == true){
            m_socket.emit("new thread",ThreadName);
            thread_on = false;
        }

    }

    private void clearCanvas(Canvas c, int left, int top, int right, int bottom) {
        Paint paint = new Paint();
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        c.drawRect(left, top, right, bottom, paint);
    }
}