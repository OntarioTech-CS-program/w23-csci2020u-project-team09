let ws;


var network = function (websocket) {
    return {
        initialize: function() {
            var url = 'ws://localhost:8080/WSChatServerDemo-1.0-SNAPSHOT/ws/hello';
            websocket = new WebSocket(url);
            // websocket.name = APP.id;
            websocket.onopen = function(evt) {
                //alert('onopen');
            };
            websocket.onerror = function(evt) {
                alert('onerror');
            };
            websocket.onmessage = function (evt) {
                //alert('onmessage');
                var command = JSON.parse(evt.data);
                console.log(command)
                if (command.type == "pause") {
                    APP.pauseVideo();
                } else if (command.type == "play") {
                    APP.playVideo();
                } else if (command.type == "seeked") {
                    APP.seekVideo(command.currentTime);
                } else {
                    alert("Unknown command " + command);
                }
            };
            websocket.onclose = function()
            {
                alert('onclose');
            };
        },
        send: function(command) {
            console.log(" GOING To send the command " + command + " to server")
            websocket.send(command);
        }
    }
};

var APP = {
    id: Math.floor(Math.random() * 10000),

    network: network(null),

    // Cannot use 'this' here after updating window.onload (see below)
    initialize: function () {
        APP.network.initialize();
        var video = APP.getVideo();
        video.addEventListener('play',
            function (event) {
                console.log(" Video is supposed to be played")
                var command = { type: "play" };
                APP.network.send(JSON.stringify(command));
            },
            false);
        video.addEventListener('pause',
            function (event) {
                console.log(" Video is supposed to be paused")
                var command = { type: "pause" };
                APP.network.send(JSON.stringify(command));
            },
            false);
        video.addEventListener('seeked',
            function (event) {
                console.log(" Video is supposed to be seeked")

                var command = { type: "seeked",
                    currentTime: APP.getVideo().currentTime };
                APP.network.send(JSON.stringify(command));
            },
            false);
    },

    getVideo: function () {
        console.log(document.getElementsByTagName("video")[0])
        return document.getElementsByTagName("video")[0];
    },

    pauseVideo: function () {
        var video = this.getVideo();
        video.pause();
    },

    playVideo: function () {
        var video = this.getVideo();
        video.play();
    },

    seekVideo: function (currentTime) {
        var video = this.getVideo();
        video.currentTime = currentTime;
    }

};

function enterRoom() {
    let code = document.getElementById("room-code").value;
    ws = new WebSocket("ws://localhost:8080/WSChatServerDemo-1.0-SNAPSHOT/ws/" + code);

    ws.onmessage = function (event) {
        console.log(event.data);
        let message = JSON.parse(event.data);
        document.getElementById("log").value += "[" + timestamp() + "] " + message.message + "\n";
    }
}
document.getElementById("input").addEventListener("keyup", function (event) {
    if (event.keyCode === 13) {
        let request = {"type":"chat", "msg":event.target.value};
        ws.send(JSON.stringify(request));
        event.target.value = "";
    }
});

window.onload = APP.initialize;


function timestamp() {
    var d = new Date(), minutes = d.getMinutes();
    if (minutes < 10) minutes = '0' + minutes;
    return d.getHours() + ':' + minutes;
}