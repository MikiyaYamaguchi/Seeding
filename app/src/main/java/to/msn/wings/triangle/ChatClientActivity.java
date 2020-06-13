package to.msn.wings.triangle;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.LinearLayout;

import java.net.URISyntaxException;
import java.util.ArrayList;

import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

import android.widget.AdapterView.OnItemClickListener;

import android.content.ClipboardManager;

import static android.view.Gravity.*;

public class ChatClientActivity extends AppCompatActivity  {

    private static boolean myMessage = false;
    private static String me = "";
    private static String sendName = "";
    private static boolean reply = false;

    static class Message
    {
        private String m_strName;
        private String m_strMessage;
        private String m_strDate;
        private boolean isMe;
        private String m_strReplyMessage;
        private String m_strReplyName;

        public Message( String strName, String strMessage, String strDate, boolean Mine, String strReplyMessage, String strReplyName )
        {
            m_strName = strName;
            m_strMessage = strMessage;
            m_strDate = strDate;
            isMe = Mine;
            sendName = strName;
            m_strReplyMessage = strReplyMessage;
            m_strReplyName = strReplyName;
        }

        public String getName()
        {
            return m_strName;
        }

        public String getMessage()
        {
            return m_strMessage;
        }

        public String getDate()
        {
            return m_strDate;
        }

        public boolean getIsme()
        {
            return isMe;
        }

        public String getReplyMessage(){ return m_strReplyMessage; }

        public String getReplyName(){ return m_strReplyName; }
    }

    static class MessageListAdapter extends BaseAdapter
    {
        private ArrayList<Message> m_listMessage;
        private LayoutInflater m_inflater;

        private final int MY_MESSAGE = 1;
        private final int NOMAL_MESSAGE = 0;

        public MessageListAdapter( Activity activity )
        {
            super();
            m_listMessage = new ArrayList<Message>();
            m_inflater = activity.getLayoutInflater();
        }

        // リストへの追加
        public void addMessage( Message message )
        {
            m_listMessage.add( m_listMessage.size(), message );    // 先頭に追加
            notifyDataSetChanged();    // ListViewの更新
        }

        // リストのクリア
        public void clear()
        {
            m_listMessage.clear();
            notifyDataSetChanged();    // ListViewの更新
        }

        @Override
        public int getCount()
        {
            return m_listMessage.size();
        }

        @Override
        public Object getItem( int position )
        {
            return m_listMessage.get( position );
        }

        @Override
        public long getItemId( int position )
        {
            return position;
        }

        static class ViewHolder
        {
            TextView textviewDate;
            TextView textviewNickname;
            TextView textviewMessage;
            LinearLayout textviewMessageWrap;
            LinearLayout content;
            TextView textviewReplyName;
            TextView textviewReplyMessage;
            LinearLayout listWrap;
            ImageView replyImageSub;
        }

        @Override
        public int getViewTypeCount() {
            return 2; // Viewのタイプの数
        }

        @Override
        public int getItemViewType(int position) {
            if(me.equals(sendName)){
                return MY_MESSAGE;
            } else {
                return NOMAL_MESSAGE;
            }
        }

        @Override
        public View getView( int position, View convertView, ViewGroup parent )
        {
            ViewHolder viewHolder;
            int type = getItemViewType(position);
            // General ListView optimization code.
            Message message;
                        if( null == convertView ) {
                            convertView = m_inflater.inflate(R.layout.listitem_message, parent, false);
                        viewHolder = new ViewHolder();
                        viewHolder.textviewDate = (TextView) convertView.findViewById(R.id.textview_date);
                        viewHolder.textviewNickname = (TextView) convertView.findViewById(R.id.textview_name);
                        viewHolder.textviewMessage = (TextView) convertView.findViewById(R.id.textview_message);
                        viewHolder.textviewMessageWrap = (LinearLayout) convertView.findViewById(R.id.textview_message_wrap);
                        viewHolder.content = (LinearLayout) convertView.findViewById(R.id.content);
                        viewHolder.textviewReplyName = (TextView) convertView.findViewById(R.id.reply_name);
                        viewHolder.textviewReplyMessage = (TextView) convertView.findViewById(R.id.reply_message);
                        viewHolder.listWrap = (LinearLayout) convertView.findViewById(R.id.list_wrap);
                        viewHolder.replyImageSub = (ImageView) convertView.findViewById(R.id.reply_image_sub);
                        convertView.setTag(viewHolder);
                        }else {
                            viewHolder = (ViewHolder)convertView.getTag();
                        }
                        message = m_listMessage.get( position );
                        boolean myMsg = message.getIsme();
            viewHolder.textviewDate.setText( message.getDate() );
            viewHolder.textviewNickname.setText( message.getName() );
            viewHolder.textviewMessage.setText( message.getMessage() );
            viewHolder.textviewReplyName.setText( message.getReplyName() );
            viewHolder.textviewReplyMessage.setText(message.getReplyMessage() );
                        setAlignment(viewHolder, myMsg);

            return convertView;
        }

        private void setAlignment(ViewHolder viewHolder, boolean isMe){
            if(isMe == true){
                viewHolder.textviewMessage.setBackgroundResource(R.drawable.balloonright);
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams( // 親 View の LayoutParams を指定
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
                layoutParams.gravity = RIGHT;
                viewHolder.textviewMessageWrap.setLayoutParams(layoutParams);
                viewHolder.textviewNickname.setLayoutParams(layoutParams);
                viewHolder.textviewNickname.setVisibility(View.GONE);
                viewHolder.textviewDate.setLayoutParams(layoutParams);
                viewHolder.textviewReplyName.setLayoutParams(layoutParams);
                viewHolder.textviewReplyMessage.setLayoutParams(layoutParams);
                viewHolder.listWrap.setLayoutParams(layoutParams);
                if(((String) viewHolder.textviewReplyName.getText()).isEmpty()){
                    viewHolder.textviewReplyName.setVisibility(View.GONE);
                    viewHolder.listWrap.setBackgroundColor(0x000000);
                    viewHolder.listWrap.setPadding(0,0,0,0);
                    viewHolder.replyImageSub.setVisibility(View.GONE);
                } else {
                    viewHolder.textviewReplyName.setVisibility(View.VISIBLE);
                    viewHolder.listWrap.setBackgroundColor(Color.rgb(245,245,245));
                    viewHolder.listWrap.setPadding(50,10,0,0);
                    viewHolder.replyImageSub.setVisibility(View.VISIBLE);
                }
                if(((String) viewHolder.textviewReplyMessage.getText()).isEmpty()){
                    viewHolder.textviewReplyMessage.setVisibility(View.GONE);
                } else {
                    viewHolder.textviewReplyMessage.setVisibility(View.VISIBLE);
                }
            } else {
                viewHolder.textviewMessage.setBackgroundResource(R.drawable.balloonleft);
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams( // 親 View の LayoutParams を指定
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
                layoutParams.gravity = LEFT;
                viewHolder.textviewMessageWrap.setLayoutParams(layoutParams);
                viewHolder.textviewNickname.setLayoutParams(layoutParams);
                viewHolder.textviewNickname.setVisibility(View.VISIBLE);
                viewHolder.textviewDate.setLayoutParams(layoutParams);
                viewHolder.listWrap.setLayoutParams(layoutParams);
                viewHolder.textviewReplyName.setLayoutParams(layoutParams);
                viewHolder.textviewReplyMessage.setLayoutParams(layoutParams);
                if(((String) viewHolder.textviewReplyName.getText()).isEmpty()){
                    viewHolder.textviewReplyName.setVisibility(View.GONE);
                    viewHolder.listWrap.setBackgroundColor(0x000000);
                    viewHolder.listWrap.setPadding(0,0,0,0);
                    viewHolder.replyImageSub.setVisibility(View.GONE);
                } else {
                    viewHolder.textviewReplyName.setVisibility(View.VISIBLE);
                    viewHolder.listWrap.setBackgroundColor(Color.rgb(245,245,245));
                    viewHolder.listWrap.setPadding(0,10,50,0);
                    viewHolder.replyImageSub.setVisibility(View.VISIBLE);
                }
                if(((String) viewHolder.textviewReplyMessage.getText()).isEmpty()){
                    viewHolder.textviewReplyMessage.setVisibility(View.GONE);
                } else {
                    viewHolder.textviewReplyMessage.setVisibility(View.VISIBLE);
                }
            }
        }

    }


    //定数
    public static final String EXTRA_NAME = "NAME";
    private static final String URI_SERVER = "https://triangleapps.herokuapp.com/";


    //メンバー変数
    private String m_strName = "";
    private Socket m_socket;
    private EditText m_edittextMessage;
    MessageListAdapter m_messagelistadapter;

    private String m_replyMessage = "";
    private String m_replyName = "";

    private String thread_name = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_client);

        //リプライメッセージ欄を非表示
        final LinearLayout replyMessageWrap = (LinearLayout)findViewById(R.id.replyMessageWrap);
        replyMessageWrap.setVisibility(View.GONE);
        final TextView replyMessage = (TextView)findViewById(R.id.replyMessage);
        final TextView replyName = (TextView)findViewById(R.id.replyName);

        //呼び出し元からパラメータ取得
        Bundle extras = getIntent().getExtras();
        if(null != extras){
            String[] list = extras.getStringArray(EXTRA_NAME);
            m_strName = list[0];
            thread_name = list[1];
            TextView ThreadNameArea = (TextView)findViewById(R.id.thread_name);
            ThreadNameArea.setText(thread_name);
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
                        Toast.makeText(ChatClientActivity.this, "Connected", Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(ChatClientActivity.this, "Connection error", Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(ChatClientActivity.this, "Connection timeout", Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(ChatClientActivity.this, "Disconnected", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        //「send」ボタンを押した時の処理
        m_edittextMessage = (EditText)findViewById(R.id.edittext_message);
        Button buttonSend = (Button)findViewById(R.id.button_send);
        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myMessage = true;
                String strMessage = m_edittextMessage.getText().toString();
                if(!strMessage.isEmpty()){
                    //サーバーに、イベント名"new message"で入力テキストを送信
                    m_socket.emit("reply name", m_replyName);
                    m_socket.emit("reply message", m_replyMessage);
                    m_socket.emit("new message", strMessage);
                    m_edittextMessage.setText(""); //テキストボックスを空に
                    replyMessageWrap.setVisibility(View.GONE);
                }
            }
        });

        // メッセージを受信したときの処理
        // ・サーバー側のメッセージ拡散時の「io.emit( 'spread message', strMessage );」に対する処理
        m_messagelistadapter = new MessageListAdapter( this ); // ビューアダプターの初期化
        final ListView listView = (ListView)findViewById( R.id.listview_messagelist );    // リストビューの取得
        listView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        m_socket.on( "spread message", new Emitter.Listener()
        {
            @Override
            public void call( final Object... args )
            {
                runOnUiThread( new Runnable()
                {
                    @Override
                    public void run()
                    {
                        String strName = "";
                        String strMessage = "";
                        String strDate = "";
                        String strReplyName = "";
                        String strReplyMessage = "";

                        JSONObject objMessage = (JSONObject)args[0];
                        try
                        {
                            strName = objMessage.getString( "strNickname" );
                            strMessage = objMessage.getString( "strMessage" );
                            strDate = objMessage.getString( "strDate" );
                            strReplyName = objMessage.getString("strReplyName");
                            strReplyMessage = objMessage.getString("strReplyMessage");
                        }
                        catch( JSONException e )
                        {
                            e.printStackTrace();
                        }

                        // 拡散されたメッセージをメッセージリストに追加
                        Message message = new Message( strName, strMessage, strDate, myMessage, strReplyMessage, strReplyName);
                        m_replyMessage = "";
                        m_replyName = "";
                        myMessage = false;
                        reply = false;
                        m_messagelistadapter.addMessage( message );
                        int last = listView.getCount() - 1;
                        listView.setSelection(last);

                    }
                } );
            }
        } );
        listView.setAdapter( m_messagelistadapter );    // リストビューにビューアダプターをセット

        //タップしたビューを長押しでテキストをクリップボードへコピー
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener(){
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id){
                Context context = getApplicationContext();
                Toast toast = Toast.makeText(context , "Copied", Toast.LENGTH_SHORT);
                toast.setGravity(TOP, 0, 0);
                toast.show();
                TextView txtView = (TextView)view.findViewById(R.id.textview_message);
                String txt = (String) txtView.getText();
                ClipboardManager clipboardManager = (ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
                clipboardManager.setPrimaryClip(ClipData.newPlainText("label", txt));
                return false;
            }
        });

        //listviewダブルタップ（リプライ機能）
        listView.setOnItemClickListener(new DoubleClickListener(){
            @Override
            public void onSingleClick(View v) { //シングルタップでは何も起きない
            }
            @Override
            public void onDoubleClick(View v) {
                TextView txtView = (TextView)v.findViewById(R.id.textview_message);
                TextView nameView = (TextView)v.findViewById(R.id.textview_name);
                String txt = (String) txtView.getText();
                String name = (String) nameView.getText();
                replyMessageWrap.setVisibility(View.VISIBLE);
                replyMessage.setText(txt);
                replyName.setText(name);
                m_replyMessage = txt;
                m_replyName = name;
                reply = true;
            }
        });

        //リプライ解除
        ImageView replyClear = (ImageView)findViewById(R.id.reply_clear);
        replyClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                m_replyMessage = "";
                m_replyName = "";
                reply = false;
                replyMessageWrap.setVisibility(View.GONE);
            }
        });

        //スレッド一覧画面に戻る処理
        ImageView back_btn = (ImageView)findViewById(R.id.back_btn);
        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    //ダブルタップ処理
    public abstract class DoubleClickListener implements OnItemClickListener {
        private static final long DOUBLE_CLICK_TIME_DELTA = 700;//milliseconds
        long lastClickTime = 0;
        public void onItemClick(AdapterView<?> v, View iv, int pos, long id) {
            long clickTime = System.currentTimeMillis();
            if(clickTime - lastClickTime < DOUBLE_CLICK_TIME_DELTA){
                onDoubleClick(iv);
            } else {
                onSingleClick(iv);
            }
            lastClickTime = clickTime;
        }
        public abstract void onSingleClick(View v);
        public abstract void onDoubleClick(View v);
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        m_socket.disconnect(); //切断
    }
}