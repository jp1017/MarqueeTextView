package uk.co.deanwild.marqueetextviewsample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import uk.co.deanwild.marqueetextview.MarqueeTextView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final MarqueeTextView tv = (MarqueeTextView) findViewById(R.id.tv1);

        Button buttonChangeColour = (Button) findViewById(R.id.button_change_colour);
        buttonChangeColour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tv.setBackgroundResource(R.color.green);
                tv.setEdgeEffectColorRes(R.color.green);
            }
        });

        Button buttonToggleEdgeEffect = (Button) findViewById(R.id.toggleEdgeEffect);
        buttonToggleEdgeEffect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tv.setEdgeEffectEnabled(!tv.isEdgeEffectEnabled());
            }
        });


    }

}
