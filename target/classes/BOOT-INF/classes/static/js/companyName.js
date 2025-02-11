$(function(){
	var name1="companyName1";
	var name2 = "companyName2";
	var cookie = document.cookie.split(";");
	for(var i=0; i<cookie.length; i++){
		var c = cookie[i].trim();
		if(c.indexOf(name1)==0){
			document.getElementById(name1).innerText=c.substring(name1.length+1,c.length);
		}
		else if(c.indexOf(name2)==0){
			document.getElementById(name2).innerText=c.substring(name2.length+1,c.length);
		}
	}
})