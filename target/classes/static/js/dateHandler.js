$(document).ready(function(){
    var date = $('.myDate');
    var i=0;
    for(i=0; i<date.length; i++){
        date[i].innerText = date[i].innerText.replace('.0','');
    }
})