package info.liuqy.adc.speakingenglish;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.RatingBar;

public class AboutActivity extends Activity {
    public static final String RATING = "rating";
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about);
        
        RatingBar rb = (RatingBar)this.findViewById(R.id.rating);
        rb.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            public void onRatingChanged(RatingBar ratingBar, float rating,
                    boolean fromUser) {
                Intent data = new Intent();
                data.putExtra(RATING, rating); //2 ways: putExtra(k, v) or putExtras(Bundle)
                AboutActivity.this.setResult(RESULT_OK, data);
                finish();
            }
        });
    }
}
