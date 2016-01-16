package www.geek.andfixdemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = (TextView) findViewById(R.id.tv_fix);

//        setTextFixBefore();

        setTextFixAfter();
    }

    /**
     * 需要打补丁，修复TextView显示的内容
     */
    private void setTextFixAfter() {
        textView.setText("Hello AndFix  after ++++++++++++");
    }

    /**
     * 使用AndFix之前的TextView 设置的内容
     */
    private void setTextFixBefore() {
        textView.setText("Hello AndFix before===========");
    }
}
