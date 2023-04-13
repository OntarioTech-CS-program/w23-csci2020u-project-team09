let ws;
let UserName = "";
let CurrentRoom = "";
let VideoNameInputtedVal = "";
let ListOfRooms = [];
let IndexPageAreaToUpdateRef = document.getElementById("main");
let ListOfRoomsTemplateRef = document.getElementById("ListOfRoomsTemplate");
let MessageInputAreaFormRef = "";
let LeaveRoomBtnRef = document.getElementById("LeaveRoomBtn");
let CreateAndJoinChatFormTemplateRef = document.getElementById(
  "CreateAndJoinChatFormTemplate"
);
let AlertUsernameHasNotBeenCreatedYetRef = document.createElement("div");
AlertUsernameHasNotBeenCreatedYetRef.innerHTML = `<div class="alert alert-danger" role="alert">
  Please create a username first
</div>`;
let AlertUserHasNotLeftTheRoomYetRef = document.createElement("div");
AlertUserHasNotLeftTheRoomYetRef.innerHTML = `<div class="alert alert-danger" role="alert">
  Please use the 'Leave Room Button'
</div>`;

let lastSeekFromServer = null; // NEW CODE

var addAlertToDom = (givenParentDOMElem, givenDOMElem) =>
  givenParentDOMElem.insertAdjacentElement("afterbegin", givenDOMElem);

function RightBubbleMessageHTML(
  givenUsername,
  givenMessage,
  capturedTimeStamp
) {
  return `<div class="message right-message">
              <div
                class="message-img"
                style="
                  background-image: url(https://avatars.githubusercontent.com/u/90413603?v=4);
                "
              ></div>

              <div class="message-bubble">
                <div class="message-info">
                  <div class="message-info-name">${givenUsername}</div>
                  <div class="message-info-time">${capturedTimeStamp}</div>
                </div>

                <div class="message-text">${givenMessage}</div>
              </div>
            </div>`;
}

function LeftBubbleMessageHTML(givenUsername, givenMessage, capturedTimeStamp) {
  return `<div class="message left-message">
              <div
                class="message-img"
                style="
                  background-image: url(https://avatars.githubusercontent.com/u/59520945?v=4);
                "
              ></div>

              <div class="message-bubble">
                <div class="message-info">
                  <div class="message-info-name">${givenUsername}</div>
                  <div class="message-info-time">${capturedTimeStamp}</div>
                </div>

                <div class="message-text">${givenMessage}</div>
              </div>
            </div>`;
}

function setUserName() {
  //access myForm using document object
  UserName = document.getElementById("usernameInput").value;
  renderHomeTemplate();
}

function renderHomeTemplate() {
  IndexPageAreaToUpdateRef.innerHTML = ``;
  IndexPageAreaToUpdateRef.innerHTML = `  <div class="content" id="area-to-update">
<div style="display: flex; flex-direction: column;justify-content: center">
    <div id="LeftRoomAlert" style="visibility: hidden;" class="alert alert-success" role="alert">
      Success! You've left ${CurrentRoom}
    </div>
    <div class="jumbotron">
      <h1 class="display-4">Welcome, ${UserName}</h1>
      <p class="lead"> Please use the side bar to join any room you wish to chat in. </p>
      <hr class="my-4">
      <p>Alternatively, use the 'Create button' to create your own chat room!</p>
      <p class="lead">
          <a class="btn btn-primary btn-lg" href="about.html" role="button">About us</a>
      </p>
      </div>
  </div>
  </div>
    `;
  CreateAndJoinChatFormTemplateRef.style.visibility = "visible";
  CreateAndJoinChatFormTemplateRef.style.height = "auto";
}

function renderVideo() {
  const videoWrapper = document.createElement("div");
  videoWrapper.classList.add("w-75");
  videoWrapper.innerHTML = `<video autoplay id="videoStreamed" muted controls>
                                      <source src="resources/videos/${VideoNameInputtedVal}.mp4" type="video/mp4">
                                      Your browser does not support the video tag.
                                  </video>
                                    `;
  let ChatAreaToUpdateRef = document.getElementById("chat-area-to-update");
  ChatAreaToUpdateRef.insertAdjacentElement("afterbegin", videoWrapper);
  let VideoStreamedRef = document.getElementById("videoStreamed");

  VideoStreamedRef.addEventListener(
    "play",
    function (event) {
      console.log(" Video is supposed to be played");
      var command = {
        type: "play",
        msg: "Tell clients to play video",
      };
      ws.send(JSON.stringify(command));
    },
    false
  );
  VideoStreamedRef.addEventListener(
    "pause",
    function (event) {
      console.log(" Video is supposed to be paused");
      var command = {
        type: "pause",
        msg: "Tell clients to pause video",
      };
      ws.send(JSON.stringify(command));
    },
    false
  );
  VideoStreamedRef.addEventListener(
    "seeked",
    function (event) {
      console.log(" Video is supposed to be seeked");
      var timestamp = document.getElementById("videoStreamed").currentTime;
      if (timestamp !== lastSeekFromServer) {
        var command = {
          type: "seeked",
          currentTime: document.getElementById("videoStreamed").currentTime,
          msg: "Tell clients to sync video",
        };
        ws.send(JSON.stringify(command));
      }
    },
    false
  );
}

function chatWindowTemplate() {
  IndexPageAreaToUpdateRef.innerHTML = ``;
  IndexPageAreaToUpdateRef.innerHTML = `<div id="chat-area-to-update" class="content">
    <section id="chat-messaging-section" class="messager flex-shrink-1 ">
      <header class="messager-header">
        <div
                id="chat-room-header-title"
                class="messager-header-title"
        ></div>
        <div class="messager-header-options">
          <span><i class="fas fa-cog"></i></span>
        </div>
      </header>

      <main id="messager-chat" class="messager-chat">
        

      </main>

      <form id="messager-inputarea-form" class="messager-inputarea">
        <input
                type="text"
                class="messager-input"
                id="messager-input"
                placeholder="Enter your message..."
        />
        <button type="submit" class="messager-send-btn">Send</button>
      </form>
    </section>
  </div>`;
  CreateAndJoinChatFormTemplateRef.style.visibility = "hidden";
  CreateAndJoinChatFormTemplateRef.style.height = "0";
  LeaveRoomBtnRef.style.visibility = "visible";
  MessageInputAreaFormRef = document.getElementById("messager-inputarea-form");
  MessageInputAreaFormRef.addEventListener("submit", ProcessMessageInputted);
}

function ProcessMessageInputted(event) {
  let messageInputtedVal = document.getElementById("messager-input").value;
  console.log(" messageInputtedVal " + messageInputtedVal);

  let request = {
    type: "chat",
    chatRoomCode: GetCurrentRoom(),
    msg: messageInputtedVal,
  };
  ws.send(JSON.stringify(request));
  document.getElementById("messager-input").value = "";
  event.preventDefault();
}

function GetListOfRooms() {
  ListOfRoomsTemplateRef.innerHTML = ``;
  // calling the ChatServlet to retrieve a new room ID
  let callURL =
    "http://localhost:8080/Zocial-1.0-SNAPSHOT/zocial-servlet/rooms";
  fetch(callURL, {
    method: "GET",
    headers: {
      Accept: "text/plain",
    },
  })
    .then((response) => response.text())
    .then((response) => {
      console.log(response);
      console.log(JSON.parse(response));
      ListOfRooms = JSON.parse(response);
      if (ListOfRooms.length != 0) {
        for (room in ListOfRooms) {
          const tempLI = document.createElement("li");
          tempLI.classList.add("nav-item");
          tempLI.style.display = "block";
          tempLI.style.width = "80%";
          tempLI.style.marginInline = "auto";
          tempLI.style.textAlign = "center";
          tempLI.innerHTML = `<a class="nav-link" href="#" onClick="enterRoom('${ListOfRooms[room]}')">${ListOfRooms[room]}</a>`;
          ListOfRoomsTemplateRef.insertAdjacentElement("beforeend", tempLI);
        }
      }
    });
}
function newRoom() {
  // calling the ChatServlet to retrieve a new room ID
  // this is a new change
  let RoomNameInputtedVal = document.getElementById("room-name-input").value;
  VideoNameInputtedVal = document.getElementById("video-to-stream-input").value;

  if (VideoNameInputtedVal.length == 0) {
    VideoNameInputtedVal = "9876543";
  }
  let callURL = "http://localhost:8080/Zocial-1.0-SNAPSHOT/zocial-servlet";

  fetch(callURL, {
    method: "POST",
    headers: {
      Accept: "application/json, text/plain, */*",
      "Content-Type": "application/json",
    },
    body: JSON.stringify({ owner: UserName, roomCode: RoomNameInputtedVal }),
  })
    .then((res) => res.text())
    .then((res) => {
      GetListOfRooms();
      enterRoom(res);
      document.getElementById("room-name-input").value = "";
      document.getElementById("video-to-stream-input").value = "";
    });
}

function leaveRoom() {
  console.log("Closing ws ", ws);
  ws.close();
  LeaveRoomBtnRef.style.visibility = "hidden";
  renderHomeTemplate();
  let LeftRoomAlertRef = document.getElementById("LeftRoomAlert");
  LeftRoomAlertRef.style.visibility = "visible";
  VideoNameInputtedVal = "";
  CurrentRoom = "";
}

function SetCurrentRoom(givenCode) {
  CurrentRoom = givenCode;
}
function GetCurrentRoom() {
  return CurrentRoom;
}

function enterRoom(code) {
  // refresh the list of rooms

  if (UserName == "") {
    addAlertToDom(
      document.getElementById("area-to-update"),
      AlertUsernameHasNotBeenCreatedYetRef
    );
  } else if (CurrentRoom != "") {
    addAlertToDom(
      document.getElementById("chat-messaging-section"),
      AlertUserHasNotLeftTheRoomYetRef
    );
  } else {
    chatWindowTemplate();
    var ChatRoomTitleRef = document.getElementById("chat-room-header-title");
    ChatRoomTitleRef.innerHTML = `${code}`;
    SetCurrentRoom(code);

    let ListOfMessagesAreaRef = document.getElementById("messager-chat");

    // create the web socket
    ws = new WebSocket("ws://localhost:8080/Zocial-1.0-SNAPSHOT/ws/" + code);

    if (UserName == "") {
      console.error(
        "Something went wrong. Cannot send username to the server. Please restart application"
      );
      throw new UserException(
        "Something went wrong. Cannot send username to the server. Please restart application"
      );
    }

    let request = {
      type: "setUserName",
      msg: UserName,
    };

    ws.onopen = () => {
      ws.send(JSON.stringify(request));
      console.log("Page is refreshing --");

      if (VideoNameInputtedVal != "") {
        let broadcastVideo = {
          type: "broadcastVideo",
          msg: VideoNameInputtedVal,
        };
        ws.send(JSON.stringify(broadcastVideo));
      } else {
        let streamVideo = {
          type: "streamVideo",
          msg: "new user has joined the room. User is not the owner",
        };
        ws.send(JSON.stringify(streamVideo));
      }
    };

    // parse messages received from the server and update the UI accordingly
    ws.onmessage = function (event) {
      console.log(event.data);
      // parsing the server's message as json
      let message = JSON.parse(event.data);
      const wrapperDiv = document.createElement("div");
      wrapperDiv.style.marginBottom = "10px";
      // handle message
      var capturedTimeStamp = new Date().toLocaleTimeString();
      if (message.username == UserName) {
        wrapperDiv.innerHTML = RightBubbleMessageHTML(
          UserName,
          message.message,
          capturedTimeStamp
        );
        ListOfMessagesAreaRef.insertAdjacentElement("beforeend", wrapperDiv);
      } else if (
        message.username == "Server" &&
        message.type == "BroadcastVideo"
      ) {
        if (VideoNameInputtedVal == "") VideoNameInputtedVal = message.message;
        renderVideo();
        SetVideoTimeStampForEveryone();
        setInterval(SetVideoTimeStampForEveryone, 3 * 1000);
      } else if (
        message.username == "Server" &&
        message.type == "StreamVideo" &&
        VideoNameInputtedVal == ""
      ) {
        VideoNameInputtedVal = message.message;
        renderVideo();
        syncVideo(message.currentTime);
      } else if (message.username == "Server" && message.type == "pause") {
        pauseVideo();
      } else if (message.username == "Server" && message.type == "play") {
        playVideo();
      } else if (message.username == "Server" && message.type == "seeked") {
        seekVideo(message.currentTime);
      } else if (
        message.username == "Server" &&
        (message.type == "SetUserName" || message.type == "Close")
      ) {
        wrapperDiv.classList.add("form-tex");
        wrapperDiv.classList.add("text-muted");
        wrapperDiv.style.marginBlock = "1.25rem";
        wrapperDiv.style.textAlign = "center";
        wrapperDiv.innerHTML = message.message;
        ListOfMessagesAreaRef.insertAdjacentElement("beforeend", wrapperDiv);
      } else if (message.type == "chat") {
        wrapperDiv.innerHTML = LeftBubbleMessageHTML(
          message.username,
          message.message,
          capturedTimeStamp
        );
        ListOfMessagesAreaRef.insertAdjacentElement("beforeend", wrapperDiv);
      }
    };
  }
}

function syncVideo(givenCurrentTime) {
  console.log(givenCurrentTime);
  let VideoStreamedRef = document.getElementById("videoStreamed");
  lastSeekFromServer = parseFloat(givenCurrentTime); // setting the timestamp from the sever
  VideoStreamedRef.currentTime = parseFloat(givenCurrentTime);
}

function pauseVideo() {
  let VideoStreamedRef = document.getElementById("videoStreamed");
  VideoStreamedRef.pause();
}
function playVideo() {
  let VideoStreamedRef = document.getElementById("videoStreamed");
  VideoStreamedRef.play();
}
function seekVideo(givenCurrentTime) {
  console.log(givenCurrentTime);
  let VideoStreamedRef = document.getElementById("videoStreamed");
  lastSeekFromServer = parseFloat(givenCurrentTime); // setting the timestamp from the sever
  VideoStreamedRef.currentTime = parseFloat(givenCurrentTime);
}

function SetVideoTimeStampForEveryone() {
  var timestamp = document.getElementById("videoStreamed").currentTime;
  let request = {
    type: "updateVideoTimeStamp",
    currentTime: timestamp,
    msg: "Owner emiting timestamp to keep everyone in sync. ",
  };
  ws.send(JSON.stringify(request));
}

(function () {})();

GetListOfRooms();
setInterval(GetListOfRooms, 100 * 1000);

window.onbeforeunload = function () {
  ws.close();
  ws.onclose = function () {}; // disable onclose handler first
  clearInterval(setInterval);
};
