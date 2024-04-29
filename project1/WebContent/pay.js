let form = $("#payment-form")

function handleSessionData(data){
    console.log(data);
    const cart_data = data["cart"];
    let total = 0;
    for (let i = 0; i < cart_data.length; i++){
        const movie_data = cart_data[i];
        total += movie_data["price"] * movie_data["quantity"];
    }
    $("#total").text(total);
}

function handleSubmitData(data){
    if (data["status"] === "success"){
        location.href = "./confirmation.html";
    }
    else{
        let warning = $("#warning");
        warning.text(data["message"]);
    }
}

$.ajax("api/cart", {
    dataType: "json",
    method: "GET",
    success: handleSessionData
});

function handleFormSubmit(event){
    $.ajax("api/pay", {
        method: "POST",
        dataType: "json",
        data: form.serialize(),
        success: resultDataString => {
            handleSubmitData(resultDataString);
        }
    });
    event.preventDefault();
}

form.submit(handleFormSubmit)