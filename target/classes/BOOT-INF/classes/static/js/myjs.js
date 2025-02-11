function change(str1, str2){
    let s1=document.getElementById(str1).value;
    s1=s1.replace(/T/,' ');
    if(s1.indexOf("00.0")<0){
        s1=s1+':00.0';
    }
    document.getElementById(str2).value=s1;
}
function download(url, name){
    let link = document.createElement("a");
    link.preventDefault();
    link.href = url;
    link.download = name;
    link.click();
}
function readIconFromCookie(objImg){
    let icon = document.cookie.split(";");
    for(var i=0;i<icon.length;i++){
        var ic = icon[i].trim();
        if(ic.indexOf("iconPath")==0) {
            objImg.src=ic.replace('iconPath=','/iconPath/');
            break;
        }
    }
}
/*function countTime(time, id) {
                var date = new Date();
                var now = date.getTime();               
                var endDate = new Date(time);//设置截止时间
                var end = endDate.getTime();
                var leftTime = end - now; //时间差                              
                var d, h, m, s, ms;
                if(leftTime >= 0) {
                    d = Math.floor(leftTime / 1000 / 60 / 60 / 24);
                    h = Math.floor(leftTime / 1000 / 60 / 60 % 24);
                    m = Math.floor(leftTime / 1000 / 60 % 60);
                    s = Math.floor(leftTime / 1000 % 60);
                    ms = Math.floor(leftTime % 1000);
                    if(ms < 100) {
                        ms = "0" + ms;
                    }
                    if(s < 10) {
                        s = "0" + s;
                    }
                    if(m < 10) {
                        m = "0" + m;
                    }
                    if(h < 10) {
                        h = "0" + h;
                    }
                    //将倒计时赋值到div中
                document.getElementById(id).innerHTML = d + "天" + h + "时" + m + "分" + s + "秒"+  ms + "毫秒";
                } else {
                    console.log('已截止')
                    //将倒计时赋值到div中
                    document.getElementById(id).innerHTML = "已开抢"
                }
                //递归每秒调用countTime方法，显示动态时间效果
               return leftTime
            }
            //调用方式  需要传入  结束时间   和 倒计时内容所在的id名称
            var s = setInterval(function (){
                   var dates =  countTime("2019-7-2 10:32:00", "go");
                    if(dates<=0) {
                        clearInterval(s)
                    }
            },50)*/
         function countTime(time, id) {
                var date = new Date();
                var now = date.getTime();               
                var endDate = new Date(time);//设置截止时间
                var end = endDate.getTime();
                var leftTime = end - now; //时间差                              
                var s;
                if(leftTime >= 0) {
                    s = Math.floor(leftTime / 1000 % 60);
                    if(s < 10) {
                        s = "0" + s;
                    }
                    //将倒计时赋值到div中
                document.getElementById(id).innerHTML =  s +"s";
                } else {
                    console.log('已截止')
                    //将倒计时赋值到div中
                    document.getElementById(id).innerHTML = ""
                }
                //递归每秒调用countTime方法，显示动态时间效果
               return leftTime
            }       