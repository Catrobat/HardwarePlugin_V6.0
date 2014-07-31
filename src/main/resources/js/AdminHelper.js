AJS.$.ajax({
    type: "POST",
    url: "#github"
}).
    done(function(html){
        alert("blub: " + html);
    }).
    fail(function(html) {
        alert("fail: " + html);
    });

AJS.$(document).ready(function(){
    alert("blahblub");
});