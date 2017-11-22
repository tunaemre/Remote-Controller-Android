package com.tunaemre.remotecontroller.fragment;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tunaemre.remotecontroller.ControllerActivity;
import com.tunaemre.remotecontroller.R;
import com.tunaemre.remotecontroller.cache.Cache;
import com.tunaemre.remotecontroller.hardware.Vibrate;
import com.tunaemre.remotecontroller.listener.ScrollListener;
import com.tunaemre.remotecontroller.model.AuthJSONObject;
import com.tunaemre.remotecontroller.network.AsyncSocketConnection;

public class ControllerMousePadFragment extends Fragment {

    private ControllerActivity activity = null;

    private RelativeLayout layout = null;

    private int touchpadWidth = 0, touchpadHeight = 0;

    private boolean isMoving = false;
    private boolean isHardPressed = false;
    private boolean isClicking = false;

    private int lastXPosition = -1;
    private int lastYPosition = -1;

    public boolean isScreenRecognized = false;

    private View touchpadLayout, scrollLayout, keyboardLayout, btnLeft, btnRight;
    private ImageButton btnScroll, btnKeyboard;
    private RecyclerView verticalRecycler, horizontalRecycler;
    private EditText editText;
    private FloatingActionButton fab;

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
        keyboardLayout = layout.findViewById(R.id.keyboardLayout);
        btnLeft = layout.findViewById(R.id.btnLeftButton);
        btnRight = layout.findViewById(R.id.btnRightButton);
        btnScroll = (ImageButton) layout.findViewById(R.id.btnScrollButton);
        btnKeyboard = (ImageButton) layout.findViewById(R.id.btnKeyboardButton);

        verticalRecycler = (RecyclerView) layout.findViewById(R.id.scrollVerticalRecycler);
        horizontalRecycler = (RecyclerView) layout.findViewById(R.id.scrollHorizontalRecycler);

        verticalRecycler.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        horizontalRecycler.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        verticalRecycler.setAdapter(new ScrollRecyclerAdapter());
        horizontalRecycler.setAdapter(new ScrollRecyclerAdapter());

        editText = (EditText) layout.findViewById(R.id.editText);
        fab = (FloatingActionButton) layout.findViewById(R.id.floatingActionButton);

        touchpadWidth = touchpadLayout.getMeasuredWidth();
        touchpadHeight= touchpadLayout.getMeasuredHeight();

        if (touchpadWidth == 0 || touchpadWidth == 0)
        {
            ViewTreeObserver viewTree = touchpadLayout.getViewTreeObserver();
            viewTree.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener()
            {
                @Override
                public boolean onPreDraw()
                {
                    touchpadWidth = touchpadLayout.getMeasuredHeight();
                    touchpadHeight = touchpadLayout.getMeasuredWidth();

                    if (touchpadWidth > 0 && touchpadHeight > 0){
                        touchPadListener();
                        return true;
                    }
                    return false;
                }
            });
        }
        else
            touchPadListener();
    }

    private void touchPadListener()
    {
        final float hardPressPressure = Cache.getInstance(getContext()).getTouchCalibration();

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

                        boolean tempIsHardPressed = event.getSize() >= hardPressPressure;

                        if (!isHardPressed && tempIsHardPressed)
                            Vibrate.getInstance(getContext()).makeSingleHapticFeedback();

                        isHardPressed = tempIsHardPressed;
                        if (isHardPressed)
                            sendRelativeDragAction((int) event.getX(), (int) event.getY());
                        else
                            sendRelativeMoveAction((int) event.getX(), (int) event.getY());
                        return true;

                    case MotionEvent.ACTION_UP:
                        isMoving = false;
                        isHardPressed = false;
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
        touchpadLayout.setOnClickListener(clickListener);
        touchpadLayout.setOnLongClickListener(longClickListener);

        btnLeft.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                sendButtonAction("Left");
                Vibrate.getInstance(getContext()).makeSingleHapticFeedback();
            }
        });

        btnRight.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                sendButtonAction("Right");
                Vibrate.getInstance(getContext()).makeDoubleHapticFeedback();
            }
        });

        btnScroll.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (scrollLayout.getVisibility() == View.GONE) {
                    verticalRecycler.scrollToPosition(Integer.MAX_VALUE / 2);
                    horizontalRecycler.scrollToPosition(Integer.MAX_VALUE / 2);

                    touchpadLayout.setVisibility(View.GONE);
                    scrollLayout.clearAnimation();
                    scrollLayout.setAnimation(AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_in));
                    scrollLayout.setVisibility(View.VISIBLE);
                    btnScroll.setImageResource(R.drawable.ic_touch_app_white_24px);
                    btnKeyboard.setEnabled(false);
                }
                else
                {
                    scrollLayout.setVisibility(View.GONE);
                    touchpadLayout.clearAnimation();
                    touchpadLayout.setAnimation(AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_in));
                    touchpadLayout.setVisibility(View.VISIBLE);
                    btnScroll.setImageResource(R.drawable.ic_swap_vert_white_24px);
                    btnKeyboard.setEnabled(true);
                }
            }
        });

        btnKeyboard.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (keyboardLayout.getVisibility() == View.GONE) {

                    touchpadLayout.setVisibility(View.GONE);
                    keyboardLayout.clearAnimation();
                    keyboardLayout.setAnimation(AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_in));
                    keyboardLayout.setVisibility(View.VISIBLE);
                    btnKeyboard.setImageResource(R.drawable.ic_keyboard_hide_white_24px);
                    btnScroll.setEnabled(false);
                    ((InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE)).toggleSoftInput(InputMethodManager.SHOW_FORCED,0);
                    editText.requestFocus();
                }
                else
                {
                    keyboardLayout.setVisibility(View.GONE);
                    touchpadLayout.clearAnimation();
                    touchpadLayout.setAnimation(AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_in));
                    touchpadLayout.setVisibility(View.VISIBLE);
                    btnKeyboard.setImageResource(R.drawable.ic_keyboard_white_24px);
                    btnScroll.setEnabled(true);
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

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendTextAction(editText.getText().toString());
                editText.setText(null);
                btnKeyboard.performClick();
            }
        });

        editText.setOnEditorActionListener(new EditText.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                        if (actionId == EditorInfo.IME_ACTION_SEND) {
                            fab.performClick();
                            return true;
                        }
                        return false;
                    }
                });

        if (activity.isConnected)
            sendScreenRecognizer();
    }

    private void sendScreenRecognizer()
    {
        try {
            AuthJSONObject object = new AuthJSONObject(activity.connectionModel.getToken());
            object.put("Action", "ScreenRecognize");
            object.put("X", touchpadWidth);
            object.put("Y", touchpadHeight);
            AsyncSocketConnection.getInstance().runSocketConnection(activity.connectionModel.ip, activity.connectionModel.port, object.toString(), new AsyncSocketConnection.ResultListener() {

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
            AuthJSONObject object = new AuthJSONObject(activity.connectionModel.getToken());
            object.put("Action", "MouseDragRelative");
            object.put("Button", "Left");
            object.put("X", x - lastXPosition);
            object.put("Y", y - lastYPosition);

            lastXPosition = x;
            lastYPosition = y;

            AsyncSocketConnection.getInstance().runSocketConnection(activity.connectionModel.ip, activity.connectionModel.port, object.toString(), new AsyncSocketConnection.ResultListener() {
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
            AuthJSONObject object = new AuthJSONObject(activity.connectionModel.getToken());
            object.put("Action", "MouseMoveRelative");
            object.put("X", x - lastXPosition);
            object.put("Y",  y - lastYPosition);

            lastXPosition = x;
            lastYPosition = y;

            AsyncSocketConnection.getInstance().runSocketConnection(activity.connectionModel.ip, activity.connectionModel.port, object.toString(), new AsyncSocketConnection.ResultListener() {
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
            AuthJSONObject object = new AuthJSONObject(activity.connectionModel.getToken());
            object.put("Action", "MouseClick");
            object.put("Button", button);

            AsyncSocketConnection.getInstance().runSocketConnection(activity.connectionModel.ip, activity.connectionModel.port, object.toString(), new AsyncSocketConnection.ResultListener() {
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
            AuthJSONObject object = new AuthJSONObject(activity.connectionModel.getToken());
            object.put("Action", "Scroll");
            object.put("Direction", direction);
            object.put("Amount", amount);

            AsyncSocketConnection.getInstance().runSocketConnection(activity.connectionModel.ip, activity.connectionModel.port, object.toString(), new AsyncSocketConnection.ResultListener() {
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

    private void sendTextAction(String text)
    {
        if (!activity.isConnected)
            return;

        if (!isScreenRecognized) {
            sendScreenRecognizer();
            return;
        }

        try
        {
            AuthJSONObject object = new AuthJSONObject(activity.connectionModel.getToken());
            object.put("Action", "Text");
            object.put("Data", text);

            AsyncSocketConnection.getInstance().runSocketConnection(activity.connectionModel.ip, activity.connectionModel.port, object.toString(), new AsyncSocketConnection.ResultListener() {
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
