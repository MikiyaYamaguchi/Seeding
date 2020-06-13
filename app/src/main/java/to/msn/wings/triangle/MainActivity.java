package to.msn.wings.triangle;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //チャットクライアントアクティビティへ遷移するためのボタン
        Button buttonJoin = (Button)findViewById(R.id.button_Join);
        buttonJoin.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                String strName = ((EditText)findViewById(R.id.edittext_name)).getText().toString();
                if(strName.isEmpty()){
                    Toast.makeText(MainActivity.this, "What's your name?", Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent = new Intent(MainActivity.this, ThreadClientActivity.class);
                    intent.putExtra(ThreadClientActivity.EXTRA_NAME,strName);

                    startActivity(intent);
                }

            }
        });

    }
}
