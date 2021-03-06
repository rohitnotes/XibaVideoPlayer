package com.axiba.xibavideoplayer.sample.simpleDemo;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.axiba.xibavideoplayer.eventCallback.XibaFullScreenEventCallback;
import com.axiba.xibavideoplayer.XibaVideoPlayer;
import com.axiba.xibavideoplayer.eventCallback.XibaPlayerActionEventCallback;
import com.axiba.xibavideoplayer.eventCallback.XibaTinyScreenEventCallback;
import com.axiba.xibavideoplayer.eventCallback.XibaVideoPlayerEventCallback;
import com.axiba.xibavideoplayer.sample.R;
import com.axiba.xibavideoplayer.sample.recyclerViewDemo.RecyclerViewDemoActivity;
import com.axiba.xibavideoplayer.sample.view.FullScreenContainer;
import com.axiba.xibavideoplayer.utils.XibaUtil;

/**
 * Created by xiba on 2016/11/26.
 */
public class SimpleDemoActivity extends Activity implements XibaVideoPlayerEventCallback,
        XibaFullScreenEventCallback, XibaTinyScreenEventCallback, XibaPlayerActionEventCallback{

    public static final String TAG = SimpleDemoActivity.class.getSimpleName();

    private XibaVideoPlayer xibaVP;
    private Button play;
    private TextView currentTimeTV;
    private TextView totalTimeTV;
    private SeekBar demoSeek;
    private TextView positionChangingInfoTV;
    private SeekBar volumeBrightSeek;
    private Button fullScreenBN;
    private Button tinyScreenBN;
    private ProgressBar loadingPB;

    private Button backToNormalBN;
    private ViewGroup fullScreenContainer;

    private Button lockScreenBN;
    private RelativeLayout fullScreenBottomContainerRL;

    private StartButtonListener mStartButtonListener;
    private SeekProgressListener mSeekProgressListener;

    private StartFullScreenListener mStartFullScreenListener;   //全屏按钮监听
//    private BackToNormalListener mBackToNormalListener; //退出全屏监听

    private boolean isTrackingTouchSeekBar = false;     //是否正在控制SeekBar


    private Button verticalFeatureNoneBN;
    private Button verticalFeatureOnlyBrightnessBN;
    private Button verticalFeatureOnlyVolumeBN;
    private Button verticalFeatureLeftBrightnessBN;
    private Button verticalFeatureLeftVolumeBN;

    private VerticalFeatureListener mVerticalFeatureListener;

    private Button horizontalFeatureNoneBN;
    private Button horizontalFeatureChangePositionBN;

    private HorizontalFeatureListener mHorizontalFeatureListener;

    String url = "http://baobab.kaiyanapp.com/api/v1/playUrl?vid=11086&editionType=default";

    //全屏容器
    private FullScreenContainer mFullScreenContainer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_demo);

        xibaVP = (XibaVideoPlayer) findViewById(R.id.demo_xibaVP);
        play = (Button) findViewById(R.id.demo_play);
        currentTimeTV = (TextView) findViewById(R.id.current_time);
        totalTimeTV = (TextView) findViewById(R.id.total_time);
        demoSeek = (SeekBar) findViewById(R.id.demo_seek);
        positionChangingInfoTV = (TextView) findViewById(R.id.position_changing_info_TV);
        volumeBrightSeek = (SeekBar) findViewById(R.id.volume_bright_seek);
        fullScreenBN = (Button) findViewById(R.id.full_screen_BN);
        tinyScreenBN = (Button) findViewById(R.id.tiny_screen_BN);
        loadingPB = (ProgressBar) findViewById(R.id.loading_PB);

        verticalFeatureNoneBN = (Button) findViewById(R.id.demo_vertical_feature_none);
        verticalFeatureOnlyBrightnessBN = (Button) findViewById(R.id.demo_vertical_feature_only_brightness);
        verticalFeatureOnlyVolumeBN = (Button) findViewById(R.id.demo_vertical_feature_only_volume);
        verticalFeatureLeftBrightnessBN = (Button) findViewById(R.id.demo_vertical_feature_left_brightness);
        verticalFeatureLeftVolumeBN = (Button) findViewById(R.id.demo_vertical_feature_left_volume);

        horizontalFeatureNoneBN = (Button) findViewById(R.id.demo_horizontal_feature_none);
        horizontalFeatureChangePositionBN = (Button) findViewById(R.id.demo_horizontal_feature_change_position);

//        xibaVP.setUp(url, 0, new Object[]{});
        xibaVP.setUp(url, 0);
        xibaVP.setEventCallback(this);
        xibaVP.setAutoRotate(true);

        xibaVP.setFullScreenEventCallback(this);
        xibaVP.setTinyScreenEventCallback(this);
        xibaVP.setPlayerActionEventCallback(this);

        //初始化监听
        mStartButtonListener = new StartButtonListener();
        mSeekProgressListener = new SeekProgressListener();
        mStartFullScreenListener = new StartFullScreenListener();
        mVerticalFeatureListener = new VerticalFeatureListener();
        mHorizontalFeatureListener = new HorizontalFeatureListener();

        setListeners();
        demoSeek.setEnabled(false);

        //设置全屏按钮监听
        fullScreenBN.setOnClickListener(mStartFullScreenListener);

        //设置小屏按钮监听
        tinyScreenBN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (xibaVP.getCurrentScreen() == XibaVideoPlayer.SCREEN_WINDOW_TINY) {
                    xibaVP.quitTinyScreen();
                } else {
                    xibaVP.startTinyScreen(new Point(500, 300), 600, 1400, true);
                }
            }
        });

        verticalFeatureNoneBN.setOnClickListener(mVerticalFeatureListener);
        verticalFeatureOnlyBrightnessBN.setOnClickListener(mVerticalFeatureListener);
        verticalFeatureOnlyVolumeBN.setOnClickListener(mVerticalFeatureListener);
        verticalFeatureLeftBrightnessBN .setOnClickListener(mVerticalFeatureListener);
        verticalFeatureLeftVolumeBN.setOnClickListener(mVerticalFeatureListener);

        horizontalFeatureNoneBN.setOnClickListener(mHorizontalFeatureListener);
        horizontalFeatureChangePositionBN.setOnClickListener(mHorizontalFeatureListener);
    }

//    /**
//     * 根据屏幕状态，重新加载控件
//     * @param isNormalScreen true 正常屏幕; false 全屏
//     */
//    private void initUIByScreenType(boolean isNormalScreen){
//
//        //保存控件状态
//        String tempPlayBNState = play.getText().toString();
//        String tempCurrentTime = currentTimeTV.getText().toString();
//        String tempTotalTime = totalTimeTV.getText().toString();
//        int progress = demoSeek.getProgress();
//
//        if (isNormalScreen) {
//            initNormalScreenUI();
////        } else {
////            initFullScreenUI();
//        }
//
//        //恢复控件状态
//        play.setText(tempPlayBNState);
//        currentTimeTV.setText(tempCurrentTime);
//        totalTimeTV.setText(tempTotalTime);
//        demoSeek.setProgress(progress);
//
//        //重新添加监听
//        setListeners();
//    }

    /**
     * 初始化正常屏幕控件
     */
    private void initNormalScreenUI(){
        //重新初始化控件
        play = (Button) findViewById(R.id.demo_play);
        currentTimeTV = (TextView) findViewById(R.id.current_time);
        totalTimeTV = (TextView) findViewById(R.id.total_time);
        demoSeek = (SeekBar) findViewById(R.id.demo_seek);
        positionChangingInfoTV = (TextView) findViewById(R.id.position_changing_info_TV);
    }

//    /**
//     * 初始化全屏控件
//     */
//    private void initFullScreenUI(){
//        //重新初始化控件
//        play = (Button) fullScreenContainer.findViewById(R.id.demo_play);
//        currentTimeTV = (TextView) fullScreenContainer.findViewById(R.id.current_time);
//        totalTimeTV = (TextView) fullScreenContainer.findViewById(R.id.total_time);
//        demoSeek = (SeekBar) fullScreenContainer.findViewById(R.id.demo_seek);
//        positionChangingInfoTV = (TextView) fullScreenContainer.findViewById(R.id.position_changing_info_TV);
//    }

    /**
     * 设置监听
     */
    private void setListeners(){
        play.setOnClickListener(mStartButtonListener);
        demoSeek.setOnSeekBarChangeListener(mSeekProgressListener);
    }

    @Override
    protected void onPause() {
        xibaVP.pausePlayer();
        super.onPause();
    }

    @Override
    protected void onResume() {
        xibaVP.resumePlayer();
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        if (xibaVP.onBackPress()) {
            return;
        }
        super.onBackPressed();
    }

    /**
     * ↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓ --XibaVideoPlayerEventCallback override methods start-- ↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓
     */
    @Override
    public void onPlayerPrepare() {
        demoSeek.setEnabled(true);
        play.setText("暂停");

        xibaVP.setHorizontalSlopInfluenceValue(4);
        Log.e(TAG, "onPlayerPrepare: xibaVP.getDuration()=" + xibaVP.getDuration());
    }

    @Override
    public void onPlayerProgressUpdate(int progress, int secProgress, long currentTime, long totalTime) {

//        currentTimeTV.setText("currentTime=" + XibaUtil.stringForTime(currentTime) + " : secProgress=" + secProgress);
//        totalTimeTV.setText("totalTime=" +  XibaUtil.stringForTime(totalTime));

        currentTimeTV.setText(XibaUtil.stringForTime(currentTime));
        totalTimeTV.setText(XibaUtil.stringForTime(totalTime));

        if (!isTrackingTouchSeekBar) {
            demoSeek.setProgress(progress);
        }
        demoSeek.setSecondaryProgress(secProgress);

        if (!demoSeek.isEnabled()) {
            demoSeek.setEnabled(true);
        }


        if (loadingPB.getVisibility() == View.VISIBLE) {
            loadingPB.setVisibility(View.GONE);
        }
    }

    @Override
    public void onPlayerPause() {
        play.setText("播放");
    }

    @Override
    public void onPlayerResume() {
        play.setText("暂停");
    }

    @Override
    public void onPlayerComplete() {
        play.setText("播放");
    }

    @Override
    public void onPlayerAutoComplete() {
        play.setText("播放");
    }

    @Override
    public void onChangingPosition(long originPosition, long seekTimePosition, long totalTimeDuration) {
        if (positionChangingInfoTV.getVisibility() != View.VISIBLE) {
            positionChangingInfoTV.setVisibility(View.VISIBLE);
        }

        long seekPosition = seekTimePosition - originPosition;
        StringBuilder sb = new StringBuilder();
        if (seekPosition > 0) {
            sb.append("+");
        } else if(seekPosition < 0){
            sb.append("-");
        }
        sb.append(XibaUtil.stringForTime(Math.abs(seekTimePosition - originPosition)));
        positionChangingInfoTV.setText(sb.toString());

        int progress = (int) (seekTimePosition * 100 / (totalTimeDuration == 0 ? 1 : totalTimeDuration));   //播放进度
        demoSeek.setProgress(progress);
    }

    @Override
    public void onChangingPositionEnd() {
        if (positionChangingInfoTV.getVisibility() != View.GONE) {
            positionChangingInfoTV.setVisibility(View.GONE);
        }
    }

    @Override
    public void onChangingVolume(int percent) {
        if (volumeBrightSeek.getVisibility() != View.VISIBLE) {
            volumeBrightSeek.setVisibility(View.VISIBLE);
        }

        volumeBrightSeek.setProgress(percent);
    }

    @Override
    public void onChangingVolumeEnd() {
        if (volumeBrightSeek.getVisibility() != View.GONE) {
            volumeBrightSeek.setVisibility(View.GONE);
        }
    }

    @Override
    public void onChangingBrightness(int percent) {
        if (volumeBrightSeek.getVisibility() != View.VISIBLE) {
            volumeBrightSeek.setVisibility(View.VISIBLE);
        }

        volumeBrightSeek.setProgress(percent);
    }

    @Override
    public void onChangingBrightnessEnd() {
        if (volumeBrightSeek.getVisibility() != View.GONE) {
            volumeBrightSeek.setVisibility(View.GONE);
        }
    }

    @Override
    public void onPlayerError(int what, int extra) {

    }

    @Override
    public ViewGroup onEnterFullScreen() {
//        ViewGroup contentView = (ViewGroup) SimpleDemoActivity.this.findViewById(Window.ID_ANDROID_CONTENT);
//
//        fullScreenContainer = (ViewGroup) getLayoutInflater()
//                .inflate(R.layout.activity_simple_demo_fullscreen, contentView, false);
//
//        //初始化全屏控件
//        initUIByScreenType(false);
//
//        fullScreenBottomContainerRL = (RelativeLayout) fullScreenContainer.findViewById(R.id.full_screen_bottom_container_RL);
//
//        //退出全屏按钮
//        backToNormalBN = (Button) fullScreenContainer.findViewById(R.id.back_to_normal_BN);
//        backToNormalBN.setOnClickListener(getBackToNormalListener());
//
//        //锁屏
//        lockScreenBN = (Button) fullScreenContainer.findViewById(R.id.lock_screen_BN);
//        lockScreenBN.setOnClickListener(new LockScreenListener());
//
//        return fullScreenContainer;

        mFullScreenContainer = new FullScreenContainer(SimpleDemoActivity.this);

        //初始化全屏控件
//            initFullScreenUI();
        mFullScreenContainer.initUI(xibaVP);

        xibaVP.setEventCallback(mFullScreenContainer.getFullScreenEventCallback());
        xibaVP.setPlayerActionEventCallback(mFullScreenContainer.getFullScreenEventCallback());

        //全屏状态下，垂直滑动左侧改变亮度，右侧改变声音
        xibaVP.setFullScreenVerticalFeature(XibaVideoPlayer.SLIDING_VERTICAL_LEFT_BRIGHTNESS);

        //全屏状态下，水平滑动改变播放位置
        xibaVP.setFullScreenHorizontalFeature(XibaVideoPlayer.SLIDING_HORIZONTAL_CHANGE_POSITION);

        //全屏状态下，水平滑动改变位置的总量为屏幕的 1/4
        xibaVP.setHorizontalSlopInfluenceValue(4);
        return mFullScreenContainer;
    }

    @Override
    public void onQuitFullScreen() {
        mFullScreenContainer.releaseFullScreenUI();

        //初始化普通屏控件
//        initUIByScreenType(true);
        resetUI();

        //绑定List的eventCallback
        xibaVP.setEventCallback(SimpleDemoActivity.this);
        xibaVP.setPlayerActionEventCallback(SimpleDemoActivity.this);
    }

    @Override
    public void onEnterTinyScreen() {
        tinyScreenBN.setText("退出小屏");
    }

    @Override
    public void onQuitTinyScreen() {
        tinyScreenBN.setText("小屏");
    }

    @Override
    public void onSingleTap() {
//        if (xibaVP.getCurrentScreen() == XibaVideoPlayer.SCREEN_WINDOW_FULLSCREEN) {
//            toggleShowHideFullScreenUI();
//        }
    }

    @Override
    public void onDoubleTap() {
        xibaVP.togglePlayPause();
    }

    @Override
    public void onTouchLockedScreen() {
        //显示或隐藏锁屏按钮
//        toggleShowHideLockBN();
    }

    @Override
    public void onStartLoading() {
        if (loadingPB.getVisibility() != View.VISIBLE) {
            loadingPB.setVisibility(View.VISIBLE);
        }
    }
    //↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑ --XibaVideoPlayerEventCallback methods end-- ↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑

    private void resetUI() {
        //设置播放按钮状态
        if (xibaVP.getCurrentState() == XibaVideoPlayer.STATE_PLAYING) {
            play.setText("暂停");
        } else {
            play.setText("播放");
        }

        //如果视频未加载，进度条不可用
        if (xibaVP.getCurrentState() == XibaVideoPlayer.STATE_NORMAL
                || xibaVP.getCurrentState() == XibaVideoPlayer.STATE_ERROR) {

            demoSeek.setEnabled(false);
        } else {

            demoSeek.setEnabled(true);

            long totalTimeDuration = xibaVP.getDuration();
            long currentTimePosition = xibaVP.getCurrentPositionWhenPlaying();

            //设置视频总时长和当前播放位置
            currentTimeTV.setText(XibaUtil.stringForTime(currentTimePosition));
            totalTimeTV.setText(XibaUtil.stringForTime(totalTimeDuration));

            int progress = (int) (currentTimePosition * 100 / (totalTimeDuration == 0 ? 1 : totalTimeDuration));   //播放进度

            //设置进度条位置
            demoSeek.setProgress(progress);
        }
    }

    /**
     * ↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓ --StartButtonOnClickListener start-- ↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓
     */
    private class StartButtonListener implements View.OnClickListener{

        @Override
        public void onClick(View v) {
            xibaVP.togglePlayPause();
        }
    }
    // ↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑ --StartButtonOnClickListener end-- ↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑

    /**
     * ↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓ --SeekProgressListener start-- ↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓
     */
    private class SeekProgressListener implements SeekBar.OnSeekBarChangeListener{

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            isTrackingTouchSeekBar = true;
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            xibaVP.seekTo(seekBar.getProgress());
            isTrackingTouchSeekBar = false;
        }
    }
    // ↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑ --SeekProgressListener end-- ↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑

    /**
     * ↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓ --BackToNormalListener start-- ↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓
     */
//    private class BackToNormalListener implements View.OnClickListener{
//        @Override
//        public void onClick(View v) {
////            //初始化全屏控件
////            initUIByScreenType(true);
//
//            xibaVP.quitFullScreen();
//        }
//    }
//
//    private BackToNormalListener getBackToNormalListener(){
//        if (mBackToNormalListener == null) {
//            mBackToNormalListener = new BackToNormalListener();
//        }
//        return mBackToNormalListener;
//    }
    // ↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑ --BackToNormalListener end-- ↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑

    /**
     * ↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓ --StartFullScreenListener start-- ↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓
     */
    private class StartFullScreenListener implements View.OnClickListener{

        @Override
        public void onClick(View v) {
//            startFullScreen();

            xibaVP.startFullScreen(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            xibaVP.setAutoRotate(false);
            xibaVP.setFullScreenVerticalFeature(XibaVideoPlayer.SLIDING_VERTICAL_LEFT_BRIGHTNESS);
            xibaVP.setFullScreenHorizontalFeature(XibaVideoPlayer.SLIDING_HORIZONTAL_CHANGE_POSITION);
        }
    }

//    private void startFullScreen(){
//        ViewGroup contentView = (ViewGroup) SimpleDemoActivity.this.findViewById(Window.ID_ANDROID_CONTENT);
//
//        fullScreenContainer = (ViewGroup) getLayoutInflater()
//                .inflate(R.layout.activity_simple_demo_fullscreen, contentView, false);
//
//        //初始化全屏控件
//        initUIByScreenType(false);
//
//        fullScreenBottomContainerRL = (RelativeLayout) fullScreenContainer.findViewById(R.id.full_screen_bottom_container_RL);
//
//        //退出全屏按钮
//        backToNormalBN = (Button) fullScreenContainer.findViewById(R.id.back_to_normal_BN);
//        backToNormalBN.setOnClickListener(getBackToNormalListener());
//
//        //锁屏
//        lockScreenBN = (Button) fullScreenContainer.findViewById(R.id.lock_screen_BN);
//        lockScreenBN.setOnClickListener(new LockScreenListener());
//
//        boolean hasActionBar = false;
//        if(getSupportActionBar() != null) hasActionBar = true;
//
//        xibaVP.startFullScreen(fullScreenContainer, hasActionBar, true);
//    }
    // ↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑ --StartFullScreenListener end-- ↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑


    private class VerticalFeatureListener implements View.OnClickListener{

        @Override
        public void onClick(View v) {
            int feature = XibaVideoPlayer.SLIDING_VERTICAL_NONE;

            switch (v.getId()) {
                case R.id.demo_vertical_feature_none:
                    feature = XibaVideoPlayer.SLIDING_VERTICAL_NONE;
                    break;
                case R.id.demo_vertical_feature_only_brightness:
                    feature = XibaVideoPlayer.SLIDING_VERTICAL_ONLY_BRIGHTNESS;
                    break;
                case R.id.demo_vertical_feature_only_volume:
                    feature = XibaVideoPlayer.SLIDING_VERTICAL_ONLY_VOLUME;
                    break;
                case R.id.demo_vertical_feature_left_brightness:
                    feature = XibaVideoPlayer.SLIDING_VERTICAL_LEFT_BRIGHTNESS;
                    break;
                case R.id.demo_vertical_feature_left_volume:
                    feature = XibaVideoPlayer.SLIDING_VERTICAL_LEFT_VOLUME;
                    break;
            }

            xibaVP.setNormalScreenVerticalFeature(feature);
        }
    }

    private class HorizontalFeatureListener implements View.OnClickListener{

        @Override
        public void onClick(View v) {
            int feature = XibaVideoPlayer.SLIDING_HORIZONTAL_NONE;

            switch (v.getId()) {
                case R.id.demo_horizontal_feature_none:
                    feature = XibaVideoPlayer.SLIDING_HORIZONTAL_NONE;
                    break;
                case R.id.demo_horizontal_feature_change_position:
                    feature = XibaVideoPlayer.SLIDING_HORIZONTAL_CHANGE_POSITION;
                    break;
            }

            xibaVP.setNormalScreenHorizontalFeature(feature);
        }
    }

//    /**
//     * 锁屏按钮监听
//     */
//    private class LockScreenListener implements View.OnClickListener{
//
//        @Override
//        public void onClick(View v) {
//            if (xibaVP.isScreenLock()) {
//                xibaVP.setScreenLock(false);
//                lockScreenBN.setText("锁屏");
//                showFullScreenUI();     //显示全部控件
//            } else {
//                xibaVP.setScreenLock(true);
//                lockScreenBN.setText("解锁");
//                dismissFullScreenUI();  //隐藏全部控件
//            }
//        }
//    }
//
//    /**
//     * 显示全部控件
//     */
//    private void showFullScreenUI(){
//        fullScreenBottomContainerRL.setVisibility(View.VISIBLE);
//        backToNormalBN.setVisibility(View.VISIBLE);
//        play.setVisibility(View.VISIBLE);
//        lockScreenBN.setVisibility(View.VISIBLE);
//    }
//
//    /**
//     * 隐藏全部控件
//     */
//    private void dismissFullScreenUI(){
//        fullScreenBottomContainerRL.setVisibility(View.GONE);
//        backToNormalBN.setVisibility(View.GONE);
//        play.setVisibility(View.GONE);
//        lockScreenBN.setVisibility(View.GONE);
//    }
//
//    /**
//     * 显示或隐藏全屏UI控件
//     */
//    private void toggleShowHideFullScreenUI(){
//        if (fullScreenBottomContainerRL != null) {
//            if (fullScreenBottomContainerRL.getVisibility() == View.VISIBLE) {
//                dismissFullScreenUI();
//            } else {
//                showFullScreenUI();
//            }
//        }
//    }
//
//    /**
//     * 显示或隐藏锁屏按钮
//     */
//    private void toggleShowHideLockBN(){
//        if (lockScreenBN != null) {
//            if (lockScreenBN.getVisibility() == View.VISIBLE) {
//                lockScreenBN.setVisibility(View.GONE);
//            } else {
//                lockScreenBN.setVisibility(View.VISIBLE);
//            }
//        }
//    }
}
