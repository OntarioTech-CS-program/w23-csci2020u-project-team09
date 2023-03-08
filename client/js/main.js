
let apiCallURL = `http://localhost:8080/Zocial-1.0-SNAPSHOT/api/rooms/data`;

function requestData(callURL) {
  fetch(callURL, {
    method: "GET",
    headers: {
      Accept: "application/json",
    },
  })
    .then((response) => response.json())
    .then((response) => addDataToBody("main", response))
    .catch((e) => console.error("Error : ", e));
}

function addDataToBody(givenID, data) {
  console.log("data sent from the server: ", data);
  let paragraphRef = document.getElementById(givenID);
  console.log(paragraphRef, "ID : ", givenID)
  paragraphRef.innerText = (JSON.stringify(data));

}

(function () {
  requestData(apiCallURL);
})();
