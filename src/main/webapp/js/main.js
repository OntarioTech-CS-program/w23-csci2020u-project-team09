let ws;
let UserName = "";
let CurrentRoom = "";
let ListOfRooms = [];
let IndexPageAreaToUpdateRef = document.getElementById("area-to-update");
let ListOfRoomsTemplateRef = document.getElementById("ListOfRoomsTemplate");
let MessageInputAreaFormRef = document.getElementById(
  "messager-inputarea-form"
);
let LeaveRoomBtnRef = document.getElementById("LeaveRoomBtn");

let ListOfMessagesAreaRef = document.getElementById("messager-chat");

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

let CreateAndJoinChatRoomBtnRef = document.getElementById(
  "CreateAndJoinChatRoomBtn"
);

function indexMainFunc() {
  //newRoom();
  CreateAndJoinChatRoomBtnRef.style.visibility = "hidden";
  GetListOfRooms();
}

function setUserName() {
  //access myForm using document object
  UserName = document.getElementById("usernameInput").value;
  IndexPageAreaToUpdateRef.innerHTML = `<div style="display: flex; flex-direction: column;justify-content: center">
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
    `;
  CreateAndJoinChatRoomBtnRef.style.visibility = "visible";
}

MessageInputAreaFormRef.addEventListener("submit", ProcessMessageInputted);

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
    "http://localhost:8080/WSChatServer-1.0-SNAPSHOT/chat-servlet/rooms";
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
          tempLI.innerHTML = `<a class="nav-item nav-link" onClick="enterRoom(${ListOfRooms[room]})">${ListOfRooms[room]}</a>`;
          ListOfRoomsTemplateRef.insertAdjacentElement("beforeend", tempLI);
        }
      }
    });
}
function newRoom() {
  // calling the ChatServlet to retrieve a new room ID
  // this is a new change

  let callURL = "http://localhost:8080/WSChatServer-1.0-SNAPSHOT/chat-servlet";
  fetch(callURL, {
    method: "GET",
    headers: {
      Accept: "text/plain",
    },
  })
    .then((response) => response.text())
    .then((response) => {
      var ChatRoomTitleRef = document.getElementById("chat-room-header-title");
      ChatRoomTitleRef.innerHTML = `${response}`;
      SetCurrentRoom(response);
      return response;
    })
    .then((response) => {
      GetListOfRooms();
      enterRoom(response);
    }); // enter the room with the code

  IndexPageAreaToUpdateRef.remove();
  CreateAndJoinChatRoomBtnRef.style.visibility = "hidden";
  LeaveRoomBtnRef.style.visibility = "visible";
}

function leaveRoom() {
  let LeftRoomAlertRef = document.getElementById("LeftRoomAlert");
  LeftRoomAlertRef.style.visibility = "visible";
}

function SetCurrentRoom(givenCode) {
  CurrentRoom = givenCode;
}
function GetCurrentRoom() {
  return CurrentRoom;
}

function enterRoom(code) {
  // refresh the list of rooms

  // create the web socket
  ws = new WebSocket(
    "ws://localhost:8080/WSChatServer-1.0-SNAPSHOT/ws/" + code
  );

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

  console.log(JSON.stringify(request));

  ws.onopen = () => ws.send(JSON.stringify(request));

  // parse messages received from the server and update the UI accordingly
  ws.onmessage = function (event) {
    console.log(event.data);
    // parsing the server's message as json
    let message = JSON.parse(event.data);
    console.log(" Message " + message.message);
    console.log(" Username " + message.username);
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
    } else if (message.username == "Server") {
      wrapperDiv.classList.add("form-tex");
      wrapperDiv.classList.add("text-muted");
      wrapperDiv.style.marginBlock = "1.25rem";
      wrapperDiv.style.textAlign = "center";
      wrapperDiv.innerHTML = message.message;
      ListOfMessagesAreaRef.insertAdjacentElement("beforeend", wrapperDiv);
    } else {
      wrapperDiv.innerHTML = LeftBubbleMessageHTML(
        message.username,
        message.message,
        capturedTimeStamp
      );
      ListOfMessagesAreaRef.insertAdjacentElement("beforeend", wrapperDiv);
    }
  };
}

(function () {
  indexMainFunc();
})();
