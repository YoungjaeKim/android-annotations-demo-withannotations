package com.egorand.aademo.withannotations;

import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.*;
import android.support.v4.app.Fragment;
import android.util.Log;
import com.googlecode.androidannotations.annotations.EFragment;

@EFragment
public class DataFragment extends Fragment {

    private Messenger serviceMessenger;
    private Messenger localMessenger = new Messenger(new IncomingHandler(this));

    private ProgressReceiver progressReceiver;

    private boolean isBound = false;

    private ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceMessenger = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            serviceMessenger = new Messenger(service);
            Message msg = Message.obtain(null, Constants.MSG_REGISTER_CLIENT);
            msg.replyTo = localMessenger;
            try {
                serviceMessenger.send(msg);
            } catch (RemoteException e) {
                Log.e(Constants.DEBUG_TAG, e.getLocalizedMessage(), e);
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        progressReceiver = (ProgressReceiver) getActivity();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopService();
    }

    public void startService() {
        if (!isBound) {
            getActivity().getApplicationContext().bindService(WorkerService_.intent(getActivity().getApplicationContext()).get(),
                    connection, Context.BIND_AUTO_CREATE);
            isBound = true;
        }
    }

    public void stopService() {
        if (isBound) {
            Message msg = Message.obtain(null, Constants.MSG_UNREGISTER_CLIENT);
            msg.replyTo = localMessenger;
            try {
                serviceMessenger.send(msg);
            } catch (RemoteException e) {
                Log.e(Constants.DEBUG_TAG, e.getLocalizedMessage(), e);
            }
            getActivity().getApplicationContext().unbindService(connection);
            isBound = false;
        }
    }

    private static class IncomingHandler extends Handler {

        private final DataFragment dataFragment;

        public IncomingHandler(DataFragment dataFragment) {
            this.dataFragment = dataFragment;
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.MSG_PROGRESS_UPDATE: {
                    dataFragment.progressReceiver.onProgressUpdate(msg.getData().getInt(
                            Constants.ARGUMENT_PROGRESS));
                }
                break;
                default: {
                    super.handleMessage(msg);
                }
            }
        }
    }

    public interface ProgressReceiver {
        void onProgressUpdate(int progress);
    }
}
