package pl.appnode.timeboxer;

import android.support.v7.app.ActionBar;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;

import com.github.paolorotolo.appintro.AppIntro2;
import com.github.paolorotolo.appintro.AppIntroFragment;

public class AppIntroActivity extends AppIntro2 {

    @Override
    public void init(Bundle savedInstanceState) {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
            // actionBar.setIcon(R.drawable.ic_launcher);
        }
        // Slides: title, description, image, background colour
        addSlide(AppIntroFragment.newInstance(getString(R.string.introduction_slide00_title),
                getString(R.string.introduction_slide00_description),
                R.drawable.intro_0, Color.parseColor("#8BC34A")));
        addSlide(AppIntroFragment.newInstance(getString(R.string.introduction_slide01_title),
                getString(R.string.introduction_slide01_description),
                R.drawable.intro_a, Color.parseColor("#8BC34A")));
        addSlide(AppIntroFragment.newInstance(getString(R.string.introduction_slide02_title),
                getString(R.string.introduction_slide02_description),
                R.drawable.intro_b, Color.parseColor("#8BC34A")));
        addSlide(AppIntroFragment.newInstance(getString(R.string.introduction_slide03_title),
                getString(R.string.introduction_slide03_description),
                R.drawable.intro_d, Color.parseColor("#8BC34A")));
        showDoneButton(true);
    }

    @Override
    public void onDonePressed() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
