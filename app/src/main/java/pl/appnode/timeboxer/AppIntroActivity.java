package pl.appnode.timeboxer;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import com.github.paolorotolo.appintro.AppIntro2;
import com.github.paolorotolo.appintro.AppIntroFragment;

public class AppIntroActivity extends AppIntro2 {

    @Override
    public void init(Bundle savedInstanceState) {

        // Instead of fragments, you can also use our default slide
        // Just set a title, description, background and image. AppIntro will do the rest
        addSlide(AppIntroFragment.newInstance("First slide", "Set timer", R.mipmap.ic_launcher, Color.parseColor("#8BC34A")));
        addSlide(AppIntroFragment.newInstance("Second slide", "Set ringtone and volume", R.mipmap.ic_launcher, Color.parseColor("#009688")));
        addSlide(AppIntroFragment.newInstance("Third slide", "Start/stop timer", R.mipmap.ic_launcher, Color.parseColor("#FFC107")));
        showDoneButton(true);
    }

    @Override
    public void onDonePressed() {
        Intent intent = new Intent(AppContextHelper.getContext(), MainActivity.class);
        startActivity(intent);
    }
}
