package com.tunaemre.remotecontroller;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.tunaemre.remotecontroller.cache.Cache;
import com.tunaemre.remotecontroller.fragment.MainIPFragment;
import com.tunaemre.remotecontroller.fragment.MainQRReaderFragment;
import com.tunaemre.remotecontroller.network.NetworkChangeReceiver;
import com.tunaemre.remotecontroller.operator.PermissionOperator;
import com.tunaemre.remotecontroller.view.ExtendedAppCombatActivity;
import com.tunaemre.remotecontroller.view.IExtendedAppCombatActivity;

import java.util.ArrayList;
import java.util.List;

@IExtendedAppCombatActivity(theme = IExtendedAppCombatActivity.ActivityTheme.LIGHT, customToolBar = R.id.toolbar, titleRes = R.string.title_connect)
public class MainActivity extends ExtendedAppCombatActivity
{
    public static int CONNECTION_REQUEST_CODE = 99;

    private CoordinatorLayout coordinatorLayout;
    private ViewPager viewPager;
    private ViewPagerAdapter viewPagerAdapter;
    private BottomNavigationView navigation;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_ip:
                    viewPager.setCurrentItem(0, true);
                    return true;
                case R.id.navigation_qrreader:
                    viewPager.setCurrentItem(1, true);
                    return true;
            }
            return false;
        }
    };

    private static String pendingDataToSend = null;

    private static void setPendingDataToSend(String data) {
        pendingDataToSend = data;
    }

    public static boolean isPendingDataToSend() {
        return pendingDataToSend != null;
    }

    public static String getPendingDataToSend() {
        String temp = pendingDataToSend;
        pendingDataToSend = null;
        return temp;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinator);
        viewPager = (ViewPager) findViewById(R.id.viewPager);

        navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        prepareActivity();

        if (Cache.getInstance(this).getLastConnection() != null)
            startActivity(new Intent(this, QuickConnectActivity.class));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CONNECTION_REQUEST_CODE && requestCode == Activity.RESULT_OK) {
            if (Cache.getInstance(this).getLastConnection() != null)
                startActivity(new Intent(this, QuickConnectActivity.class));
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (getIntent().hasExtra("data")) {
            setPendingDataToSend(getIntent().getExtras().getString("data"));
            Snackbar.make(coordinatorLayout, "Connect before send to PC.", Snackbar.LENGTH_LONG).show();
            getIntent().removeExtra("data");
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_calibration:
                startActivity(new Intent(MainActivity.this, CalibrationActivity.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void prepareActivity() {
        final ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        viewPagerAdapter.addFragment(new MainIPFragment());
        viewPagerAdapter.addFragment(new MainQRReaderFragment());
        viewPager.setAdapter(viewPagerAdapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) { }

            @Override
            public void onPageSelected(int position) {
                switch (position)
                {
                    case 0:
                        navigation.setSelectedItemId(R.id.navigation_ip);
                        break;
                    case 1:
                        ((MainQRReaderFragment)viewPagerAdapter.getFragmentList().get(position)).prepareFragment();
                        navigation.setSelectedItemId(R.id.navigation_qrreader);
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {}
        });

        if (!NetworkChangeReceiver.checkWifiConnection(getApplicationContext())) {
            final Snackbar snackbar = Snackbar.make(coordinatorLayout, "Please check your wifi connection.", Snackbar.LENGTH_INDEFINITE);
            snackbar.setAction("OK", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    snackbar.dismiss();
                }
            });
            snackbar.show();
        }

        if (Cache.getInstance(this).getTouchCalibration() == -1)
            startActivity(new Intent(MainActivity.this, CalibrationActivity.class));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        for (Fragment fragment : viewPagerAdapter.getFragmentList())
            fragment.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private class ViewPagerAdapter extends FragmentPagerAdapter
    {
        private final List<Fragment> mFragmentList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager)
        {
            super(manager);
        }

        @Override
        public Fragment getItem(int position)
        {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount()
        {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment)
        {
            mFragmentList.add(fragment);
        }

        public List<Fragment> getFragmentList() {
            return mFragmentList;
        }
    }
}