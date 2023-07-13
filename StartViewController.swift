//
//  StartViewController.swift
//  saluki
//
//  Created by zhl on 2017/4/7.
//  Copyright © 2017年 zhl. All rights reserved.
//

import UIKit
import SwiftyTimer
import SwifterSwift
import AVFoundation
import MediaPlayer

class StartViewController: BaseViewController {
    let startBtnWidth = 156 * ASPECT_RATIO
    let smallBtnWidth = 85 * ASPECT_RATIO
    var isBegin = false
    var isPause = false
    var isEnd = false
    var beginTime:Date?
    var endTime:Date?
    var durationTime: Int = 0
    var nextAudioTime: Int = 3600
    var playAudio = ZManager.instance.play_audio

    var musicPlayer = MPMusicPlayerController.systemMusicPlayer
    var minuteQueue = Queue<Int>()
    var plan: ActivityPlan?

    var currentSecond = 0.0

    lazy var backView = UIImageView().then{
        $0.image = UIImage(named: "start_activity_back")
    }
    lazy var audioPlayer: ZAudioPlayer = {
        let player = ZAudioPlayer()
        return player
    }()
    lazy var backAudioPlayer: ZAudioPlayer = {
        let player = ZAudioPlayer()
        return player
    }()
    lazy var startBtn: RoundPicButton = {
        let btn = RoundPicButton(image: UIImage(named: "activity_big_btn")!)
        btn.titleLabel.text = "开始"
        btn.titleLabel.font = UIFont.systemFont(ofSize: 24 * ASPECT_RATIO)
        return btn
    }()
    lazy var cancelBtn: RoundPicButton = {
        let btn = RoundPicButton(image: UIImage(named: "activity_small_btn")!)
        btn.titleLabel.text = "取消"
        return btn
    }()
    lazy var endBtn: RoundPicButton = {
        let btn = RoundPicButton(image: UIImage(named: "activity_small_btn")!)
        btn.titleLabel.text = "结束"
        return btn
    }()
    lazy var timeTitleLabel = UILabel().then{
        $0.font = UIFont.boldSystemFont(ofSize: 12 * ASPECT_RATIO)
        $0.textColor = UIColor.white
        $0.text = "持续时间"
    }
    lazy var timeLabel = UILabel().then{
        $0.font = UIFont.boldSystemFont(ofSize: 45 * ASPECT_RATIO)
        $0.textColor = UIColor.white
    }
    lazy var countDownView = CountDownView().then{ _ in

    }
    lazy var settingBtn = UIButton().then{
        $0.setImage(UIImage(named: "icon_setting"), for: UIControlState.normal)
    }
    lazy var musicBtn = UIButton().then{
        $0.setImage(UIImage(named: "music_picker"), for: UIControlState.normal)
    }
    lazy var audioTipIcon = UIImageView().then{
        $0.image = UIImage(named: "activity_audo")
    }
    lazy var audioTipLabel = UILabel().then{
        $0.text = "语音提示已关闭"
        $0.font = UIFont.boldSystemFont(ofSize: 12 * ASPECT_RATIO)
        $0.textColor = UIColor.white
    }
    lazy var musicView = MusicPlayerView().then{_ in

    }

    var timer0: Timer!
    var timer1: Timer!
    var mainTimer: Timer!

    override func viewDidLoad() {
        super.viewDidLoad()
        self.navigationItem.title = "开始站桩"
        self.view.addSubview(backView)
        self.view.addSubview(startBtn)
        self.view.addSubview(endBtn)
        self.view.addSubview(cancelBtn)
        backView.snp.makeConstraints{
            $0.top.equalTo(0)
            $0.bottom.equalTo(0)
            $0.left.equalTo(0)
            $0.right.equalTo(0)
        }
        endBtn.isHidden = true
        

        endBtn.addTarget(self, action: #selector(drawEndBtnClockwise), for: UIControlEvents.touchDown)
        endBtn.addTarget(self, action: #selector(drawEndBtnReverse), for: UIControlEvents.touchUpInside)
        cancelBtn.addTarget(self, action: #selector(cancelBtnClick), for: UIControlEvents.touchUpInside)
        startBtn.addTarget(self, action: #selector(toggleActivity), for: UIControlEvents.touchUpInside)

        timeLabel.text = "00 : 00 : 00"
        self.view.addSubview(timeLabel)
        timeLabel.snp.makeConstraints{
            $0.centerX.equalTo(self.view)
            $0.centerY.equalTo(self.view).offset(-125*ASPECT_RATIO)
        }
        self.view.addSubview(timeTitleLabel)
        timeTitleLabel.snp.makeConstraints{
            $0.centerX.equalTo(self.view)
            $0.bottom.equalTo(timeLabel.snp.top).offset(-22)
        }
        countDownView.delegate = self
        countDownView.playAudio = self.playAudio
        self.view.addSubview(countDownView)
        countDownView.snp.makeConstraints{
            $0.center.equalTo(self.view)
            $0.width.equalTo(200)
            $0.width.equalTo(200)
        }
        self.view.addSubview(settingBtn)
        settingBtn.addTarget(self, action: #selector(showSettingView), for: UIControlEvents.touchUpInside)
        settingBtn.snp.makeConstraints{
            $0.right.equalTo( -20)
            $0.top.equalTo(STATUS_BAR_HEIGHT + 5)
            $0.width.equalTo(30*ASPECT_RATIO)
            $0.height.equalTo(30*ASPECT_RATIO)
        }
        self.view.addSubview(musicBtn)
        musicBtn.snp.makeConstraints{
            $0.right.equalTo(settingBtn.snp.left).offset(-15)
            $0.centerY.equalTo(settingBtn)
            $0.width.equalTo(settingBtn)
            $0.height.equalTo(settingBtn)
        }
        musicBtn.addTarget(self, action: #selector(showMusicPicker), for: UIControlEvents.touchUpInside)
        self.view.addSubview(audioTipIcon)
        audioTipIcon.snp.makeConstraints{
            $0.centerX.equalTo(self.view)
            $0.centerY.equalTo(self.view).offset(-20)
            $0.width.equalTo(45 * ASPECT_RATIO)
            $0.height.equalTo(45 * ASPECT_RATIO)
        }
        changeAudioTipLabel()
        self.view.addSubview(audioTipLabel)
        audioTipLabel.snp.makeConstraints{
            $0.centerX.equalTo(self.view)
            $0.top.equalTo(audioTipIcon.snp.bottom).offset(10)
        }
        do {
//            try AVAudioSession.sharedInstance().setCategory(AVAudioSessionCategoryPlayback)
            try AVAudioSession.sharedInstance().setCategory(AVAudioSessionCategoryPlayback, with: AVAudioSessionCategoryOptions.mixWithOthers)
            try AVAudioSession.sharedInstance().setActive(true)
        } catch {
            vlog(error)
        }
//        UIApplication.shared.beginReceivingRemoteControlEvents()
        self.becomeFirstResponder()
//        BackendTaskQueue.instance.addOperation {
//            self.backAudioPlayer.playBlank()
//        }
        self.backAudioPlayer.playBlank()

        musicView.delegate = self
        self.view.addSubview(musicView)
        musicView.snp.makeConstraints{
            $0.centerX.equalTo(self.view)
            $0.top.equalTo(audioTipLabel.snp.bottom).offset(10)
            $0.width.equalTo(self.view).multipliedBy(3.0/5)
            $0.height.equalTo(40)
        }

        if musicPlayer.playbackState == .playing {
            musicView.setPlayStatus(true)
        }
        startBtn.snp.makeConstraints{
            $0.centerX.equalTo(self.view)
            $0.top.equalTo(musicView.snp.bottom).offset(20)
            $0.width.equalTo(startBtnWidth)
            $0.height.equalTo(startBtnWidth)
        }
        endBtn.snp.makeConstraints{
            $0.centerY.equalTo(startBtn)
            $0.right.equalTo(-(SCREEN_WIDTH - startBtnWidth) / 4 + smallBtnWidth / 2)
            $0.width.equalTo(smallBtnWidth)
            $0.height.equalTo(smallBtnWidth)
        }
        cancelBtn.snp.makeConstraints{
            $0.centerY.equalTo(startBtn)
            $0.left.equalTo((SCREEN_WIDTH - startBtnWidth) / 4 - smallBtnWidth / 2)
            $0.width.equalTo(smallBtnWidth)
            $0.height.equalTo(smallBtnWidth)
        }
    }
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        ZHandlerEnterBackground().addObserver { (timeInterval) in
//            self.durationTime = self.durationTime + Int(timeInterval)
            print(self.durationTime)
//            self.backAudioPlayer.playBlank()
        }
        getSelectPlan()
    }
    override func viewWillDisappear(_ animated: Bool) {
        super.viewWillAppear(animated)
        ZHandlerEnterBackground().removeNotificationObserver(observer: self)
//        self.backAudioPlayer.audioPlayer?.stop()
    }
    func endActivity() {

        let activity = Activity()
        activity.beginTime = self.beginTime
        activity.endTime = self.endTime
        activity.duration = self.durationTime
        let endViewController = EndViewController()
        endViewController.delegate = self
        endViewController.activity = activity
        self.show(endViewController, sender: nil)
    }
    func getSelectPlan() {
        webApi.getSelectedPlan(){
            debugPrint($0)
            self.plan = $0
            self.minuteQueue = $0.getTimeQueue()
            self.nextAudioTime = self.minuteQueue.dequeue()!
            if self.isBegin && self.nextAudioTime < self.durationTime {
                repeat {
                    let next = self.minuteQueue.dequeue()
                    if next != nil {
                        self.nextAudioTime = next!
                        if self.minuteQueue.count < 50 && (self.plan?.isSimple)!{
                            self.minuteQueue.enqueue(self.minuteQueue.last! + (self.plan?.getLastTime())!)
                        }
                        if  !(self.plan?.isSimple)! && (self.plan?.lastRepeat)! &&  self.minuteQueue.count < 50{
                            self.minuteQueue.enqueue(self.minuteQueue.last! + (self.plan?.getLastTime())!)
                        }
                    }
                } while(self.nextAudioTime < self.durationTime)
            }
        }
    }

}

//事件
extension StartViewController {
    /**
     改变audioTipLabel的显示
    */
    func changeAudioTipLabel() {
        if self.playAudio {
            audioTipLabel.text = "语音提示已开启"
        } else {
            audioTipLabel.text = "语音提示已关闭"
        }
    }
    /**
     顺时针画圆，结束后结束本次计时，进入下一个页面
    */
    @objc func drawEndBtnClockwise() {
        self.mainTimer.invalidate()
        self.backAudioPlayer.audioPlayer?.stop()
        self.isEnd = true
        self.isBegin = false
        self.isPause = false
        self.endTime = Date()
        if self.playAudio {
            let audioArr = ZStringUtil.generateTimeAudioArr(self.durationTime, preHeader: "w_end")
            self.audioPlayer.playSequence(audioArr)
        }
        vlog("站桩结束，一共\(self.durationTime)秒")
        self.endActivity()
    }

    /**
     逆时针画圆
    */
    @objc func drawEndBtnReverse() {
        if !isEnd {
            vlog("drawEndBtnReverse")
            timer0.invalidate()
        }
    }


    @objc func cancelBtnClick() {
        if !isBegin {
            self.dismiss(animated: true) {}
        }
    }
    //更新计时器label的显示
    func updateTitleLabel() {
        durationTime += 1
        self.timeLabel.text = ZStringUtil.formatSeconds(seconds: durationTime)
        if durationTime > 0 && durationTime == self.nextAudioTime && self.playAudio && self.plan != nil{
            let audioArr = ZStringUtil.generateTimeAudioArr(self.durationTime, preHeader: nil)
            self.audioPlayer.playSequence(audioArr)
            let next = self.minuteQueue.dequeue()
            if next != nil {
                self.nextAudioTime = next!
                if self.minuteQueue.count < 50 && (self.plan?.isSimple)!{
                    self.minuteQueue.enqueue(self.minuteQueue.last! + (self.plan?.getLastTime())!)
                }
                if  !(self.plan?.isSimple)! && (self.plan?.lastRepeat)! &&  self.minuteQueue.count < 50{
                    self.minuteQueue.enqueue(self.minuteQueue.last! + (self.plan?.getLastTime())!)
                }
            }
            
        }
    }
    //切换主计时器的状态
    @objc func toggleActivity() {
        if !isBegin {
            cancelBtn.isHidden = true
            startBtn.isEnabled = false
            countDownView.startCountDown()
        } else {
            if isPause {
                isPause = false
                startBtn.titleLabel.text = "暂停"
                mainTimer.fireDate = Date()
                if self.playAudio {
                    audioPlayer.playOne("w_resume")
                }
            } else {
                isPause = true
                startBtn.titleLabel.text = "继续"
                var date = Date()
                date.add(.day, value: 4)
                mainTimer.fireDate = date
                if self.playAudio {
                    audioPlayer.playOne("w_pause")
                }
            }

        }
    }
    @objc func showMusicPicker() {
        let authorizationStatus = MPMediaLibrary.authorizationStatus();
        switch authorizationStatus {
        case .authorized:
            self.realShowMusicPicker()
            break
        case .notDetermined:
            MPMediaLibrary.requestAuthorization({(newPermissionStatus: MPMediaLibraryAuthorizationStatus) in
                if newPermissionStatus == .authorized {
                    self.realShowMusicPicker()
                } else {
                    self.showMusicAlert()
                }
            })
            break
        case .denied, .restricted:
            self.showMusicAlert()
            break
        }
    }
    func showMusicAlert(){
        let alert = UIAlertController(title: "提示" , message: "需要权限才能使用该功能？",  preferredStyle: .alert)
        let action = UIAlertAction(title: "取消", style: .cancel,  handler: nil)
        let okaction = UIAlertAction(title: "设置", style: .default) { _ in
            self.openMusicSetting()
        }
        alert.addAction(action)
        alert.addAction(okaction)
        present(alert, animated: true, completion: nil)
    }
    @objc func openMusicSetting() {
        if #available(iOS 10, *) {
            UIApplication.shared.open(URL.init(string: UIApplicationOpenSettingsURLString)!, options: [:],
                                      completionHandler: {
                                        (success) in
            })
        } else {
            UIApplication.shared.openURL(URL.init(string: UIApplicationOpenSettingsURLString)!)
        }
    }
    @objc func realShowMusicPicker() {
        let picker = MusicPickerController()
        picker.delegate = self
        
//        self.addChildViewController(picker)
//        self.view.addSubview(picker.view)
//        picker.didMove(toParentViewController: self)
//        self.present(picker, animated: true, completion: nil)
        self.show(picker, sender: nil)
    }
    @objc func showSettingView() {
        let settingView = ActivitySettingController()
        settingView.delegate = self
        settingView.playAudio = self.playAudio
        self.show(settingView, sender: nil)
    }

}
extension StartViewController: ActivityDelegate {
    func cancel() {
        self.dismiss(animated: false) {}
    }

    func saved() {
        self.dismiss(animated: false) {}
    }
}
extension StartViewController: CountDownDelegate {
    func countFinish() {
        startBtn.isEnabled = true
        self.durationTime = 0
        isBegin = true
        endBtn.isHidden = false
        startBtn.titleLabel.text = "暂停"
        beginTime = Date()
        mainTimer = Timer.new(every: 1.second){
            self.updateTitleLabel()
        }
        mainTimer.start()
    }
}

extension StartViewController: ActivitySettingDelegate {
    func didAudioSettingChange(result: Bool) {
        self.playAudio = result
        self.countDownView.playAudio = result
        changeAudioTipLabel()
    }

    func didAudioIntervalChange(val: Int) {
        print("当前频率:\(val)")
    }
}

extension StartViewController: MusicPickerDelegate {
    func musicPickerDidCancel(_ mediaPicker: MPMediaPickerController) {
        musicView.setPlayStatus(musicPlayer.playbackState == .playing)
    }
    func musicPicker(_ mediaPicker: MPMediaPickerController, didPickMediaItems mediaItemCollection: MPMediaItemCollection) {
        musicView.selectSong = mediaItemCollection
        musicPlayer.setQueue(with: musicView.selectSong!)
        musicPlayer.shuffleMode = .off
        musicPlayer.repeatMode = .all
        musicPlayer.play()
        musicView.setPlayStatus(true)
        musicPlayer.beginGeneratingPlaybackNotifications()
    }

}

extension StartViewController: MusicDelegate {
    func didPlayBtnClick() {
        musicPlayer.play()
        musicView.setPlayStatus(true)
    }
    func didPauseClick() {
        musicPlayer.pause()
    }
    func didNextBtnClick() {
        musicPlayer.skipToNextItem()
    }
    func didPreBtnClick() {
        if musicPlayer.indexOfNowPlayingItem == 0 {
            musicPlayer.skipToBeginning()
        } else {
            musicPlayer.skipToPreviousItem()
        }
    }
}
