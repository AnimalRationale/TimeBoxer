package pl.appnode.timeboxer;


import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import com.github.paolorotolo.appintro.AppIntro;
import com.github.paolorotolo.appintro.AppIntroFragment;

public class AppIntroActivity extends AppIntro{

    @Override
    public void init(Bundle savedInstanceState) {

        // Instead of fragments, you can also use our default slide
        // Just set a title, description, background and image. AppIntro will do the rest
        addSlide(AppIntroFragment.newInstance("First slide", "Set timer", R.mipmap.ic_launcher, Color.parseColor("#8BC34A")));
        addSlide(AppIntroFragment.newInstance("Second slide", "Set ringtone and volume", R.mipmap.ic_launcher, Color.parseColor("#009688")));
        addSlide(AppIntroFragment.newInstance("Third slide", "Start/stop timer", R.mipmap.ic_launcher, Color.parseColor("#C5CAE9")));

        // OPTIONAL METHODS
        // Override bar/separator color
        setBarColor(Color.parseColor("#3F51B5"));
        setSeparatorColor(Color.parseColor("#2196F3"));

        // Hide Skip/Done button
        showSkipButton(false);
        showDoneButton(true);
    }

    @Override
    public void onSkipPressed() {
        // Do something when users tap on Skip button.
    }

    @Override
    public void onDonePressed() {
        Intent intent = new Intent(AppContextHelper.getContext(), MainActivity.class);
        startActivity(intent);
    }
}
