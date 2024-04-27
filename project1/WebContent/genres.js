


function handleResult(data){
    let genres = data["genres"]
    //console.log(genres)

    let table = jQuery("#genre_body");
    for (let i = 0; i < genres.length; i++){
        let rowHTML = "";
        rowHTML += "<tr><th>";
        rowHTML += "<a href='./genre.html?genre=" + genres[i] + "'>" + genres[i] + "</a>";
        rowHTML += "</th></tr>";

        table.append(rowHTML);
        //console.log(rowHTML);
    }
}


jQuery.ajax({
    dataType: "json",  // Setting return data type
    method: "GET",// Setting request method
    url: "api/genres",
    success: (resultData) => handleResult(resultData) // Setting callback function to handle data returned successfully by the SingleStarServlet
});