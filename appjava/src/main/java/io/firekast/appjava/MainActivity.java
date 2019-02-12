package io.firekast.appjava;

import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView mBottomNavigationView;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            return navigateTo(item.getItemId());
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBottomNavigationView = findViewById(R.id.navigation);
        mBottomNavigationView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        navigateTo(mBottomNavigationView.getSelectedItemId());
    }

    private boolean navigateTo(@IdRes int navigationItemId) {
        if (mBottomNavigationView.getSelectedItemId() == navigationItemId
                && !getSupportFragmentManager().getFragments().isEmpty()) {
            return false;
        }
        switch (navigationItemId) {
            case R.id.navigation_streamer:
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container_main, new StreamerFragment())
                        .commit();
                return true;
            case R.id.navigation_player:
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container_main, new PlayerFragment())
                        .commit();
                return true;
        }
        return false;
    }

}
