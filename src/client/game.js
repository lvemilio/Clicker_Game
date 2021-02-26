// Change this to any username you'd like to start a new game
var username = "bitl-student";

var socket = io.connect("http://localhost:8080/", {transports: ['websocket']});
setupSocket();


function setupSocket() {
    // This function is called whenever a new game state is received from the server
    socket.on('gameState', function (jsonGameState) {
        console.log(jsonGameState);
        var gameState = JSON.parse(jsonGameState)
        document.getElementById("gold").innerHTML = "Gold: " + Math.floor(gameState["gold"])
        document.getElementById("shovel").innerHTML = "Buy Shovel <br> Cost: " + Math.ceil(gameState["equipment"]["shovel"]["cost"]) + " Current: " + gameState["equipment"]["shovel"]["numberOwned"]
        document.getElementById("excavator").innerHTML = "Buy Excavator <br> Cost: " + Math.ceil(gameState["equipment"]["excavator"]["cost"]) + " Current: " + gameState["equipment"]["excavator"]["numberOwned"]
        document.getElementById("mine").innerHTML = "Buy Gold Mine <br> Cost: " + Math.ceil(gameState["equipment"]["mine"]["cost"]) + " Current: " + gameState["equipment"]["mine"]["numberOwned"]
    });
}


function initializeGame() {
    socket.emit("register", username);
    document.getElementById("username").innerHTML = "Username: " + username;
    document.getElementById("gold").innerHTML = "Gold: 0"
    document.getElementById("shovel").innerHTML = "Buy Shovel <br> Cost: 0 Current: 0"
    document.getElementById("excavator").innerHTML = "Buy Excavator <br> Cost: 0 Current: 0"
    document.getElementById("mine").innerHTML = "Buy Gold Mine <br> Cost: 0 Current: 0"
}


function clickGold() {
    socket.emit("clickGold");
}


function buyEquipment(equipmentID) {
    socket.emit("buy", equipmentID);
}
