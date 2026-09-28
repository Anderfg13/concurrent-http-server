function greet() {
    const name = document.getElementById("name").value;
    const result = document.getElementById("greet-result");

    fetch("/hello?name=" + encodeURIComponent(name))
        .then(response => response.text())
        .then(message => {
            result.textContent = message;
        })
        .catch(error => {
            result.textContent = "Error: " + error.message;
        });
}

function getPi() {
    const result = document.getElementById("pi-result");

    fetch("/pi")
        .then(response => response.text())
        .then(value => {
            result.textContent = value;
        })
        .catch(error => {
            result.textContent = "Error: " + error.message;
        });
}

function calculateSquare() {
    const number = document.getElementById("number").value;
    const result = document.getElementById("square-result");

    fetch("/square?number=" + encodeURIComponent(number))
        .then(async response => {
            const data = await response.json();
            if (!response.ok) {
                throw new Error(data.error);
            }
            result.textContent = "Square of " + data.number + " is " + data.square;
        })
        .catch(error => {
            result.textContent = "Error: " + error.message;
        });
}

function getServerTime() {
    const result = document.getElementById("time-result");

    fetch("/server-time")
        .then(response => response.json())
        .then(data => {
            result.textContent = data.serverTime;
        })
        .catch(error => {
            result.textContent = "Error: " + error.message;
        });
}

document.getElementById("greet-button").addEventListener("click", greet);
document.getElementById("pi-button").addEventListener("click", getPi);
document.getElementById("square-button").addEventListener("click", calculateSquare);
document.getElementById("time-button").addEventListener("click", getServerTime);
