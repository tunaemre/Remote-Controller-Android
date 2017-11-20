package com.tunaemre.remotecontroller.fragment;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.tunaemre.remotecontroller.ControllerActivity;
import com.tunaemre.remotecontroller.R;
import com.tunaemre.remotecontroller.listener.ScrollListener;
import com.tunaemre.remotecontroller.network.AsyncSocketConnection;

import org.json.JSONObject;

public class ControllerMousePadFragment extends Fragment {

    private ControllerActivity activity = null;

    private RelativeLayout layout = null;

    private int touchpadXSize = 0, touchpadYSize = 0;

    private boolean isMoving = false;
    private boolean isClicking = false;

    private int lastXPosition = -1;
    private int lastYPosition = -1;

    public boolean isScreenRecognized = false;

    private View touchpadLayout, scrollLayout, btnLeft, btnRight, btnScroll;
    private RecyclerView verticalRecycler, horizontalRecycler;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        activity = (ControllerActivity) getActivity();
        layout = (RelativeLayout) inflater.inflate(R.layout.layout_controller_mousepad, null);

        prepareFragment(layout);
        return layout;
    }

    private void prepareFragment(RelativeLayout layout) {

        touchpadLayout = layout.findViewById(R.id.touchpadLayout);
        scrollLayout = layout.findViewById(R.id.scrollLayout);
        btnLeft = layout.findViewById(R.id.btnLeftButton);
        btnRight = layout.findViewById(R.id.btnRightButton);
        btnScroll = layout.findViewById(R.id.btnScrollButton);

        verticalRecycler = (RecyclerView) layout.findViewById(R.id.scrollVerticalRecycler);
        horizontalRecycler = (RecyclerView) layout.findViewById(R.id.scrollHorizontalRecycler);

        verticalRecycler.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        horizontalRecycler.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        verticalRecycler.setAdapter(new ScrollRecyclerAdapter());
        horizontalRecycler.setAdapter(new ScrollRecyclerAdapter());

        new GetTouchpadSizeService(layout.findViewById(R.id.touchpadLayout)).execute();
    }

    private void touchPadListener()
    {
        View.OnTouchListener touchListener = new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                switch (event.getAction())
                {
                    case MotionEvent.ACTION_DOWN:
                        return false;

                    case MotionEvent.ACTION_MOVE:
                        isMoving = true;
//					    lastTouchMilisecond = 0;

//					    if ((new Date()).getTime() - lastTouchMilisecond > 25)
//					    {
//					    	SendMoveAction((int) event.getX(), (int) event.getY());
//					    	lastTouchMilisecond = (new Date()).getTime();
//					    }
                        if (event.getPressure() < 0.7F)
                            sendRelativeMoveAction((int) event.getX(), (int) event.getY());
                        else
                            sendRelativeDragAction((int) event.getX(), (int) event.getY());
                        return true;

                    case MotionEvent.ACTION_UP:
                        isMoving = false;
                        lastXPosition = -1;
                        lastYPosition = -1;
                        return false;
                }
                return false;
            }
        };

        View.OnClickListener clickListener = new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (!isMoving)
                    sendButtonAction("Left");
            }
        };

        View.OnLongClickListener longClickListener = new View.OnLongClickListener()
        {
            @Override
            public boolean onLongClick(View v)
            {
                if (!isMoving)
                    sendButtonAction("Right");
                return true;
            }
        };

        touchpadLayout.setOnTouchListener(touchListener);
        //((RelativeLayout) findViewById(R.id.touchpadLayout)).setOnClickListener(clickListener);
        //((RelativeLayout) findViewById(R.id.touchpadLayout)).setOnLongClickListener(longClickListener);

        btnLeft.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                sendButtonAction("Left");
            }
        });

        btnRight.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                sendButtonAction("Right");
            }
        });

        btnScroll.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (touchpadLayout.getVisibility() == View.VISIBLE) {
                    verticalRecycler.scrollToPosition(Integer.MAX_VALUE / 2);
                    horizontalRecycler.scrollToPosition(Integer.MAX_VALUE / 2);

                    touchpadLayout.setVisibility(View.GONE);
                    scrollLayout.clearAnimation();
                    scrollLayout.setAnimation(AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_in));
                    scrollLayout.setVisibility(View.VISIBLE);
                }
                else
                {
                    scrollLayout.setVisibility(View.GONE);
                    touchpadLayout.clearAnimation();
                    touchpadLayout.setAnimation(AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_in));
                    touchpadLayout.setVisibility(View.VISIBLE);
                }
            }
        });

        verticalRecycler.setOnScrollListener(new ScrollListener(new ScrollListener.ScrollingListener() {
            @Override
            public void onScrolledHorizontal(int dx) {}

            @Override
            public void onScrolledVertical(int dy) {
                sendScrollAction("Vertical", dy);
            }
        }));

        horizontalRecycler.setOnScrollListener(new ScrollListener(new ScrollListener.ScrollingListener() {
            @Override
            public void onScrolledHorizontal(int dx) {
                sendScrollAction("Horizontal", dx);
            }

            @Override
            public void onScrolledVertical(int dy) { }
        }));

        if (activity.isConnected)
            sendScreenRecognizer();
    }

    private void sendScreenRecognizer()
    {
        try {
            JSONObject object = new JSONObject();
            object.put("Action", "ScreenRecognize");
            object.put("X", touchpadXSize);
            object.put("Y", touchpadYSize);
            AsyncSocketConnection.getInstance(getContext()).runSocketConnection(activity.ipNumber, activity.portNumber, object.toString(), new AsyncSocketConnection.ResultListener() {

                @Override
                public void onStart() {}

                @Override
                public void onResult(AsyncSocketConnection.SocketConnectionResult result) {
                    isScreenRecognized = result == AsyncSocketConnection.SocketConnectionResult.Success;
                }
            });
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendRelativeDragAction(int x, int y)
    {
        if (!activity.isConnected)
            return;

        if (!isScreenRecognized) {
            sendScreenRecognizer();
            return;
        }

        if (lastXPosition == -1 || lastYPosition == -1) {
            lastXPosition = x;
            lastYPosition = y;
            return;
        }

        try {
            JSONObject object = new JSONObject();
            object.put("Action", "MouseDragRelative");
            object.put("Button", "Left");
            object.put("X", x - lastXPosition);
            object.put("Y", y - lastYPosition);

            lastXPosition = x;
            lastYPosition = y;

            AsyncSocketConnection.getInstance(getContext()).runSocketConnection(activity.ipNumber, activity.portNumber, object.toString(), new AsyncSocketConnection.ResultListener() {
                @Override
                public void onStart() {
                    activity.showCommandIndicator();
                }

                @Override
                public void onResult(AsyncSocketConnection.SocketConnectionResult result) {
                    activity.hideCommandIndicator();
                }
            });
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void sendRelativeMoveAction(int x, int y)
    {
        if (!activity.isConnected)
            return;

        if (!isScreenRecognized) {
            sendScreenRecognizer();
            return;
        }

        if (lastXPosition == -1 || lastYPosition == -1) {
            lastXPosition = x;
            lastYPosition = y;
            return;
        }

        try {
            JSONObject object = new JSONObject();
            object.put("Action", "MouseMoveRelative");
            object.put("X", x - lastXPosition);
            object.put("Y",  y - lastYPosition);

            lastXPosition = x;
            lastYPosition = y;

            AsyncSocketConnection.getInstance(getContext()).runSocketConnection(activity.ipNumber, activity.portNumber, object.toString(), new AsyncSocketConnection.ResultListener() {
                @Override
                public void onStart() {
                    activity.showCommandIndicator();
                }

                @Override
                public void onResult(AsyncSocketConnection.SocketConnectionResult result) {
                    activity.hideCommandIndicator();
                }
            });
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void sendButtonAction(String button)
    {
        if (!activity.isConnected)
            return;

        if (!isScreenRecognized) {
            sendScreenRecognizer();
            return;
        }

        try
        {
            JSONObject object = new JSONObject();
            object.put("Action", "MouseClick");
            object.put("Button", button);

            AsyncSocketConnection.getInstance(getContext()).runSocketConnection(activity.ipNumber, activity.portNumber, object.toString(), new AsyncSocketConnection.ResultListener() {
                @Override
                public void onStart() {
                    activity.showCommandIndicator();
                }

                @Override
                public void onResult(AsyncSocketConnection.SocketConnectionResult result) {
                    activity.hideCommandIndicator();
                }
            });
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendScrollAction(String direction, int amount)
    {
        if (!activity.isConnected)
            return;

        if (!isScreenRecognized) {
            sendScreenRecognizer();
            return;
        }

        try
        {
            JSONObject object = new JSONObject();
            object.put("Action", "Scroll");
            object.put("Direction", direction);
            object.put("Amount", amount);

            AsyncSocketConnection.getInstance(getContext()).runSocketConnection(activity.ipNumber, activity.portNumber, object.toString(), new AsyncSocketConnection.ResultListener() {
                @Override
                public void onStart() {
                    activity.showCommandIndicator();
                }

                @Override
                public void onResult(AsyncSocketConnection.SocketConnectionResult result) {
                    activity.hideCommandIndicator();
                }
            });
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class GetTouchpadSizeService
    {
        private View mTouchpad;

        GetTouchpadSizeService(View touchpad) {
            this.mTouchpad = touchpad;
        }

        public void execute()
        {
            new touchpadSizeTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }

        private class touchpadSizeTask extends AsyncTask<Void, Void, Boolean>
        {
            protected Boolean doInBackground(Void... params)
            {
                ViewTreeObserver viewTree = mTouchpad.getViewTreeObserver();
                viewTree.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener()
                {
                    @Override
                    public boolean onPreDraw()
                    {
                        touchpadXSize = mTouchpad.getMeasuredHeight();
                        touchpadYSize = mTouchpad.getMeasuredWidth();

                        return (touchpadXSize != 0 && touchpadYSize != 0);
                    }
                });

                return false;
            }

            protected void onPostExecute(Boolean result)
            {
                if (touchpadXSize == 0 || touchpadYSize == 0)
                    execute();
                else
                    touchPadListener();
            }
        }
    }

    private class ScrollRecyclerAdapter extends RecyclerView.Adapter<ScrollRecyclerAdapter.ViewHolder> {

        public class ViewHolder extends RecyclerView.ViewHolder {
            public ImageView imageView;

            public ViewHolder(View itemView) {
                super(itemView);
                imageView = (ImageView) itemView.findViewById(R.id.imageView);
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            RelativeLayout layout = (RelativeLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recycler_scroll, parent, false);

            ViewHolder viewHolder = new ViewHolder(layout);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {

        }

        @Override
        public int getItemCount() {
            return Integer.MAX_VALUE;
        }
    }
}
