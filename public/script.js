

const greetingForm = document.getElementById("greeting-form");
// Busco los 3 elementos de esta seccion UNA SOLA VEZ, fuera de la funcion,
// asi try/catch/finally los pueden usar todos sin problemas de "scope".
const greetingLoading = document.getElementById("greeting-loading");
const greetingResult = document.getElementById("greeting-result");
const greetingError = document.getElementById("greeting-error");

greetingForm.addEventListener("submit", async function (event) {
    event.preventDefault(); // Evita que el formulario se envíe de la manera tradicional
    const name = document.getElementById("greeting-name").value; //Obtengo lo que se envió del campo de texto.
    const url = "/greeting?name=" + encodeURIComponent(name); //Armo la url para enviarsela al backend.

    // Antes de empezar: muestro "cargando" y oculto cualquier resultado/error de un intento anterior.
    greetingLoading.hidden = false;
    greetingResult.hidden = true;
    greetingError.hidden = true;

    try {
        const response = await fetch(url, { method: "GET" }); //Hago la peticion al backend.
        const data = await response.json(); //Obtengo la respuesta en formato JSON (exito o error, ambos son JSON).

        if (!response.ok) {
            throw new Error(data.error); //Error HTTP (400): lo mando al catch con el mensaje real del servidor.
        }

        greetingResult.textContent = data.greeting;
        greetingResult.hidden = false; //Muestro el resultado en la pagina.
    } catch (error) {
        // Aqui caen TANTO los errores HTTP (400, con mensaje real) COMO los fallos de red (servidor caido).
        greetingError.textContent = error.message;
        greetingError.hidden = false;
        console.error("Error en la solicitud:", error);
    } finally {
        // Esto se ejecuta SIEMPRE, haya habido exito o error: el "cargando" debe desaparecer en ambos casos.
        greetingLoading.hidden = true;
    }
})

const squareForm = document.getElementById("square-form");
const squareLoading = document.getElementById("square-loading");
const squareError = document.getElementById("square-error");
const squareResult = document.getElementById("square-result");


squareForm.addEventListener("submit", async function(event){
    event.preventDefault(); // Evita que el formulario se envíe de la manera tradicional
    const number = document.getElementById("square-number").value;
    const url = "/square?number=" + encodeURIComponent(number);
    console.log("Submit interceptado, ya no se recarga la pagina");

    squareLoading.hidden = false;
    squareResult.hidden = true;
    squareError.hidden = true;

    try{
        const response = await fetch(url, { method: "GET"});
        const data = await response.json();

        if(!response.ok){
            throw new Error(data.error);
        }

        squareResult.textContent = data.square;
        squareResult.hidden = false;
    } catch (error){
        squareError.textContent = error.message;
        squareError.hidden = false;
        console.error("Error en la solicitud:", error);
    } finally {
        squareLoading.hidden = true;
    }
})

const timeButton = document.getElementById("server-time-button");
const timeLoading = document.getElementById("server-time-loading");
const timeResult = document.getElementById("server-time-result");
const timeHidden = document.getElementById("server-time-error");


timeButton.addEventListener("click", async function (event) {

    event.preventDefault();
    const url = "/server-time";

    timeLoading.hidden = false;
    timeResult.hidden = true;
    timeHidden.hidden = true;

    try{
        const response = await fetch (url, { method: "GET"});
        const data = await response.json();

        if(!response.ok){
            throw new Error(data.error);
        }

        timeResult.textContent = data.serverTime;
        timeResult.hidden = false;
    } catch (error){
        timeHidden.textContent = error.message;
        timeHidden.hidden = false;
        console.error("Error en la solicitud:", error);

    } finally {

        timeLoading.hidden = true;
    }

    
    console.log("Boton de hora presionado");
});
