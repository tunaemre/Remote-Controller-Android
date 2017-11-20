package com.tunaemre.remotecontroller.view;

import android.animation.Animator;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewTreeObserver;

import com.tunaemre.remotecontroller.R;

public abstract class CircularRevealActivity extends ExtendedAppCombatActivity {

    public static String REVEAL_POSITION_X = "REVEAL_POSITION_X";
    public static String REVEAL_POSITION_Y = "REVEAL_POSITION_Y";

    private int revealPositionX = -1, revealPositionY = -1;

    public enum RevealPosition
    {
        TOP_RIGHT,
        CENTER,
        BOTTOM
    }

    private RevealPosition selectedRevealPosition = RevealPosition.CENTER;

    private Bundle savedInstanceState = null;
    protected View rootLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.savedInstanceState = savedInstanceState;

        Bundle bundle = getIntent().getExtras();
        if (bundle != null && bundle.containsKey(REVEAL_POSITION_X) && bundle.containsKey(REVEAL_POSITION_Y))
        {
            revealPositionX = bundle.getInt(REVEAL_POSITION_X);
            revealPositionY = bundle.getInt(REVEAL_POSITION_Y);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            overridePendingTransition(R.anim.anim_nothing, R.anim.anim_nothing);
    }

    protected void setRevealPosition(RevealPosition position)
    {
        this.selectedRevealPosition = position;
    }

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        super.setContentView(layoutResID);

        rootLayout = this.findViewById(android.R.id.content);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (savedInstanceState == null) {
                rootLayout.setVisibility(View.INVISIBLE);

                ViewTreeObserver viewTreeObserver = rootLayout.getViewTreeObserver();
                if (viewTreeObserver.isAlive()) {
                    viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                        @Override
                        public void onGlobalLayout() {
                            circularRevealActivity();
                            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                                rootLayout.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                            } else {
                                rootLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                            }
                        }
                    });
                }
            }
        }
    }

    @Override
    public void onBackPressed() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            int cx = 0;
            int cy = 0;

            if (revealPositionX != -1 && revealPositionY != -1) {
                cx = revealPositionX;
                cy = revealPositionY;
            }
            else {
                if (selectedRevealPosition == RevealPosition.TOP_RIGHT) {
                    cx = rootLayout.getWidth();
                    cy = 0;
                }
                else if (selectedRevealPosition == RevealPosition.BOTTOM) {
                    cx = rootLayout.getWidth() / 2;
                    cy = rootLayout.getHeight();
                }
                else if (selectedRevealPosition == RevealPosition.CENTER) {
                    cx = rootLayout.getWidth() / 2;
                    cy = rootLayout.getHeight() / 2;
                }
            }

            float finalRadius = Math.max(rootLayout.getWidth(), rootLayout.getHeight());
            Animator circularReveal = ViewAnimationUtils.createCircularReveal(rootLayout, cx, cy, finalRadius, 0);

            circularReveal.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {

                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    rootLayout.setVisibility(View.INVISIBLE);
                    finish();
                    overridePendingTransition(R.anim.anim_nothing, R.anim.anim_nothing);
                }

                @Override
                public void onAnimationCancel(Animator animator) {

                }

                @Override
                public void onAnimationRepeat(Animator animator) {

                }
            });
            circularReveal.setDuration(400);
            circularReveal.start();
        }
        else
        {
            super.onBackPressed();
        }
    }

    private void circularRevealActivity() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            int cx = 0;
            int cy = 0;

            if (revealPositionX != -1 && revealPositionY != -1) {
                cx = revealPositionX;
                cy = revealPositionY;
            }
            else {
                if (selectedRevealPosition == RevealPosition.TOP_RIGHT) {
                    cx = rootLayout.getWidth();
                    cy = 0;
                }
                else if (selectedRevealPosition == RevealPosition.BOTTOM) {
                    cx = rootLayout.getWidth() / 2;
                    cy = rootLayout.getHeight();
                }
                else if (selectedRevealPosition == RevealPosition.CENTER) {
                    cx = rootLayout.getWidth() / 2;
                    cy = rootLayout.getHeight() / 2;
                }
            }

            double finalRadius = Math.sqrt(Math.pow(rootLayout.getWidth(), 2) +  Math.pow(rootLayout.getHeight(), 2));

            // create the animator for this view (the start radius is zero)
            Animator circularReveal = ViewAnimationUtils.createCircularReveal(rootLayout, cx, cy, 0, (float) finalRadius);
            circularReveal.setDuration(400);

            rootLayout.setVisibility(View.VISIBLE);
            circularReveal.start();
        }
    }
}
