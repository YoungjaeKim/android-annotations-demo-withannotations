package com.egorand.aademo.withannotations;

import android.support.v4.app.FragmentActivity;
import android.widget.Button;
import android.widget.ProgressBar;
import com.googlecode.androidannotations.annotations.*;

@EActivity(R.layout.content_layout)
@OptionsMenu(R.menu.content_menu)
public class MainActivity extends FragmentActivity implements DataFragment.ProgressReceiver {

    @ViewById
    Button btnStart, btnStop;

    @ViewById
    ProgressBar progressBar;

    @FragmentByTag(Constants.DATA_FRAGMENT_TAG)
    DataFragment dataFragment;

    @InstanceState
    boolean startBtnEnabled = true, stopBtnEnabled = false;

    @AfterViews
    void initButtons() {
        btnStart.setEnabled(startBtnEnabled);
        btnStop.setEnabled(stopBtnEnabled);
    }

    @AfterViews
    void initFragment() {
        if (dataFragment == null) {
            dataFragment = new DataFragment_();
            getSupportFragmentManager().beginTransaction()
                    .add(dataFragment, Constants.DATA_FRAGMENT_TAG).commit();
        }
    }

    @Click(R.id.btnStart)
    void startService() {
        dataFragment.startService();
        startBtnEnabled = false;
        stopBtnEnabled = true;
        initButtons();
    }

    @Click(R.id.btnStop)
    void stopService() {
        dataFragment.stopService();
        startBtnEnabled = true;
        stopBtnEnabled = false;
        initButtons();
    }

    @OptionsItem(R.id.clear)
    void clear() {
        progressBar.setProgress(0);
    }

    @Override
    public void onProgressUpdate(int progress) {
        progressBar.setProgress(progress);
    }
}
