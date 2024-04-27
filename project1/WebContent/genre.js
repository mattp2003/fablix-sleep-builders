
function getParameterByName(target) {
    // Get request URL
    let url = window.location.href;
    // Encode target parameter name to url encoding
    target = target.replace(/[\[\]]/g, "\\$&");

    // Ues regular expression to find matched parameter value
    let regex = new RegExp("[?&]" + target + "(=([^&#]*)|&|#|$)"),
        results = regex.exec(url);
    if (!results) return null;
    if (!results[2]) return '';

    // Return the decoded parameter value
    return decodeURIComponent(results[2].replace(/\+/g, " "));
}

const movie_table = jQuery("#movie_table")
function handleResult(data){
    let movies = data["movies"];
    let ids = data["ids"];

    for (let i = 0; i < movies.length; i++){
        let rowHTML = "";
        rowHTML += "<tr><th>";
        rowHTML += "<a href='./movie.html?id=" + ids[i] + "'>" + movies[i] + "</a>";
        rowHTML += "</th></tr>";

        movie_table.append(rowHTML);
        console.log(rowHTML);
    }
}


let genre = getParameterByName('genre');
console.log(genre);
// Makes the HTTP GET request and registers on success callback function handleResult
jQuery.ajax({
    dataType: "json",  // Setting return data type
    method: "GET",// Setting request method
    url: "api/genre?genre=" + genre,
    success: (resultData) => handleResult(resultData) // Setting callback function to handle data returned successfully by the SingleStarServlet
});