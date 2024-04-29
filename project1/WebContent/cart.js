let cart_table = $("#cart-table")

function Order(action, id, title){
    let t = title.replace(' ', '%20');
    orderData = "action=" + action + "&id=" + id + "&title=" + title;
    //console.log(orderData)

    jQuery.ajax("api/cart", {
        method: "POST",
        dataType: "json",
        data: orderData,
        success: resultData => {
           handleSessionData(resultData);
        }
    });
}


function handleSessionData(data){
    console.log(data);
    const cart_data = data["cart"];
    cart_table.empty();
    let total = 0;
    for (let i = 0; i < cart_data.length; i++){
        const movie_data = cart_data[i];
        let row = "<tr>"
        row += "<th><a href='./movie.html?id=" + movie_data["movieId"] + "'>" + movie_data["title"] + "<a/></th>";
        row += "<th>" + movie_data["price"] + "</th>";
        row += "<th>" + movie_data["quantity"] + "</th>";
        row += "<th>" + movie_data["price"] * movie_data["quantity"] + "</th>";
        row += "<th><button onclick=\"Order(\'increase\', \'" + movie_data['movieId'] + "\', \'" + movie_data['title'] + "\')\">Add</button></th>";
        row += "<th><button onclick=\"Order(\'decrease\', \'" + movie_data['movieId'] + "\', \'" + movie_data['title'] + "\')\">Subtract</button></th>";
        row += "<th><button onclick=\"Order(\'delete\', \'" + movie_data['movieId'] + "\', \'" + movie_data['title'] + "\')\">Delete</button></th>";

        row += "</tr>";

        //console.log(row)
        total += movie_data["price"] * movie_data["quantity"];
        cart_table.append(row);
    }
    $("#total").text(total);
}


$.ajax("api/cart", {
    dataType: "json",
    method: "GET",
    success: handleSessionData
});