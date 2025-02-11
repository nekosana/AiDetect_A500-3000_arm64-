function Player(video,flvPlayer){
    this.video=video;
    this.flvPlayer=flvPlayer;
    this.init=function(){
        this.flvPlayer.attachMediaElement(this.video);
        console.log("开始播放");
        this.flvPlayer.load();
        this.video.addEventListener("progress", () => {
            /*console.log("里面："+flvPlayer);
            console.log(flvPlayer);*/
            var end = this.flvPlayer.buffered.end(0); //获取当前buffered值(缓冲区末尾)
            var delta = end - this.flvPlayer.currentTime; //获取buffered与当前播放位置的差值

            // 延迟过大，通过跳帧的方式更新视频
            if (delta > 5 || delta < 0)  {
                this.flvPlayer.currentTime = this.flvPlayer.buffered.end(0) - 1;
                return;
            }

            // 追帧
            if (delta > 2) {
                // console.log("1.1倍速播放");
                this.video.playbackRate = 1.1;
            } else {
                // console.log("原速度播放");
                this.video.playbackRate = 1;
            }
        });
        // 网页重新激活后，更新视频
        window.onfocus = () => {
            let end = this.flvPlayer.buffered.end(0) - 1;
            this.flvPlayer.currentTime = end;
        };
    };
}






function startVideo(streams,ip){
        var Players = new Array(9);
        var port = 1935;
        var app = 'myapp';
        if (flvjs.isSupported()) {
            var videoElements = document.getElementsByTagName('video');
            for (var i=0;i<streams.length&&i<videoElements.length;i++){
                var stream = streams[i];
                var videoElement = videoElements[i];
                Players[i] =new Player(videoElement,generateFlvPlayer(ip,port, app, stream, videoElement));
                var player = Players[i];
                /*console.log("外面："+flvPlayer);
                console.log(flvPlayer);*/
                player.init(port,app,stream);

            }




        }
    }

    function generateFlvPlayer(ip,port, app, stream, videoElement){
        var player = createFlvPlayer(ip,port, app, stream, videoElement);
        //断流重连
        player.on(flvjs.Events.ERROR, (errorType, errorDetail, errorInfo) => {
            console.log("errorType:", errorType);
            console.log("errorDetail:", errorDetail);
            console.log("errorInfo:", errorInfo);
            //视频出错后销毁重新创建
            if (player) {
                player.pause();
                player.unload();
                player.detachMediaElement();
                player.destroy();
                console.log("error");
                player = generateFlvPlayer(ip,port, app, stream,videoElement);
            }
        }

    );

        /*player.on("statistics_info", function (res) {
            if (this.lastDecodedFrame == 0) {
                this.lastDecodedFrame = res.decodedFrames;
                return;
            }
            if (this.lastDecodedFrame != res.decodedFrames) {
                this.lastDecodedFrame = res.decodedFrames;
            } else {
                this.lastDecodedFrame = 0;
                if (player) {
                    player.pause();
                    player.unload();
                    player.detachMediaElement();
                    player.destroy();
                    console.log("error2");
                    flvPlayer = generateFlvPlayer(port, app, stream,videoElement);
                }
            }
        });*/
        return player;
    }

    function createFlvPlayer(ip,port, app, stream,videoElement){
        console.log("create");
        var flvPlayer = flvjs.createPlayer({
        type: 'flv',
        url: 'http://'+ip+':9000/live?port='+port+'&app='+app+'&stream='+stream,
        enableWorker: true, // 启用分离的线程进行转换
        //enableStashBuffer: false, // 关闭IO隐藏缓冲区
        stashInitialSize: 200, // 减少首帧显示等待时长
        lazyLoadMaxDuration: 24*60*60,
        hasAudio: false
    });
    /*flvPlayer.attachMediaElement(videoElement);
    flvPlayer.load();*/
    return flvPlayer;
}

