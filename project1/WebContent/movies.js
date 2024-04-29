/**
 * This example is following frontend and backend separation.
 *
 * Before this .js is loaded, the html skeleton is created.
 *
 * This .js performs two steps:
 *      1. Use jQuery to talk to backend API to get the json data.
 *      2. Populate the data to correct html elements.
 */

function Order(action, id, title){
    let t = title.replace(' ', '%20');
    orderData = "action=" + action + "&id=" + id + "&title=" + title;
    //console.log(orderData)

    jQuery.ajax("api/cart", {
        method: "POST",
        data: orderData,
        success: resultData => {
           //console.log(resultData);
           alert(title + " has been added to cart")
        }
    });
}

/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */
function handleMovieResult(resultData) {
    console.log(resultData)
    let moviesTableBodyElement = jQuery("#movies_table_body");
    moviesTableBodyElement.empty();
    // Iterate through resultData, no more than 10 entries
    // if (resultData.length === 0 && currentPage > 1){
    //     location.href = ""
    // }
    for (let i = 0; i < resultData.length; i++) {
        // Concatenate the html tags with resultData jsonObject
        let rowHTML = "";
        rowHTML += "<tr>";
        rowHTML +=
            "<th>" +
            // Add a link to single-star.html with id passed with GET url parameter
            '<a href="movie.html?id=' + resultData[i]['id'] + '">'
            + resultData[i]["title"] +     // display star_name for the link text
            '</a>' +
            "</th>";
        rowHTML += "<th>" + resultData[i]["year"] + "</th>";
        rowHTML += "<th>" + resultData[i]["director"] + "</th>";
        rowHTML += "<th>" + resultData[i]["genres"] + "</th>";
        let stars = resultData[i]["stars"].split(", ");
        let stars_id = resultData[i]["stars_id"].split(", ");
        let stars_info = build_stars(stars, stars_id);

        rowHTML += "<th>"
        for (let i = 0; i < stars.length; i++){
            if (i !== 0){
                rowHTML += "; "
            }
            rowHTML += '<a href="single-star.html?id=' + stars_info[stars[i]] + '">'
                + stars[i] + '</a>'
        }
        rowHTML += "</th>"

        rowHTML += "<th>" + resultData[i]["rating"] + "</th>";

        rowHTML += "<th><button onclick=\"Order(\'increase\', \'" + resultData[i]['id'] + "\', \'" + resultData[i]['title'] + "\')\">Add to Cart</button></th>"

        rowHTML += "</tr>";
        // Append the row created to the table body, which will refresh the page
        moviesTableBodyElement.append(rowHTML);
    }
}


function build_stars(stars, stars_id) {
    let stars_dict = {};
    for (let i = 0; i < stars.length; i++) stars_dict[stars[i]] = stars_id[i];
    return stars_dict;
}


// function getParameterByName(target) {
//     // Get request URL
//     let url = window.location.href;
//     // Encode target parameter name to url encoding
//     target = target.replace(/[\[\]]/g, "\\$&");
//
//     // Ues regular expression to find matched parameter value
//     let regex = new RegExp("[?&]" + target + "(=([^&#]*)|&|#|$)"),
//         results = regex.exec(url);
//     if (!results) return null;
//     if (!results[2]) return '';
//
//     // Return the decoded parameter value
//     return decodeURIComponent(results[2].replace(/\+/g, " "));
// }

function handleStarts(){
    let table = jQuery("#starts-body")
    const starts = "0123456789abcdefghijklmnopqrstuvwxyz*"

    for (let i = 0; i < starts.length; i++){
        let rowHTML = "";
        rowHTML += "<tr><th>";
        rowHTML += "<a href='./movies.html?startsWith=" + starts[i] + "'>" + starts[i] + "</a>";
        rowHTML += "</th></tr>";
        table.append(rowHTML);
    }
}

function handleGenres(data){
    let genres = data["genres"]
    //console.log(genres)

    let table = jQuery("#genre_body");
    for (let i = 0; i < genres.length; i++){
        let rowHTML = "";
        rowHTML += "<tr><th>";
        rowHTML += "<a href='./movies.html?genre=" + genres[i] + "'>" + genres[i] + "</a>";
        rowHTML += "</th></tr>";

        table.append(rowHTML);
        //console.log(rowHTML);
    }
}

handleStarts()

jQuery.ajax({
    dataType: "json",  // Setting return data type
    method: "GET",// Setting request method
    url: "api/genres",
    success: (resultData) => handleGenres(resultData) // Setting callback function to handle data returned successfully by the SingleStarServlet
});


let url = "api/movies"
let some_url = url + window.location.search;

console.log("This is the url", some_url);
/**
 * Once this .js is loaded, following scripts will be executed by the browser
 */

//has no parameters, load parameters from session history and reload site
function handleHistory(data){
    console.log(data);
    if (data.length > 0){
        location.href = "./movies.html" + "?" + data;
    }
    else{
        jQuery.ajax({
            dataType: "json", // Setting return data type
            method: "GET", // Setting request method
            // url: "/api/movies" + ,
            url: url + window.location.search,
            success: (resultData) => handleMovieResult(resultData) // Setting callback function to handle data returned successfully by the StarsServlet
        });
       }
}
console.log(some_url);
if (some_url === url){
    jQuery.ajax({
        dataType: "text", // Setting return data type
        method: "GET", // Setting request method
        // url: "/api/movies" + ,
        url: "api/movieHistory",
        success: (resultData) => handleHistory(resultData)
    });
}
else{
    // Makes the HTTP GET request and registers on success callback function handleStarResult
    jQuery.ajax({
        dataType: "json", // Setting return data type
        method: "GET", // Setting request method
        // url: "/api/movies" + ,
        url: url + window.location.search,
        success: (resultData) => handleMovieResult(resultData) // Setting callback function to handle data returned successfully by the StarsServlet
    });

}
var sortState = {
    title: "asc",
    rating: "asc"
};

$("#sort_title").on("click", function() {
    // Toggle sort order for title
    sortState.title = sortState.title === "asc" ? "desc" : "asc";
    sortMovies("title", sortState.title);
});

$("#sort_rating").on("click", function() {
    // Toggle sort order for rating
    sortState.rating = sortState.rating === "asc" ? "desc" : "asc";
    sortMovies("rating", sortState.rating);
});

function sortMovies(sortBy, sortOrder) {
    // Construct the query with the sorting parameters
    let query = "";
    if (window.location.search.length === 0) {
        query = `api/movies?&sortBy=${sortBy}&sortOrder=${sortOrder}`;
    }
    else {
        query = "api/movies" + window.location.search + `&sortBy=${sortBy}&sortOrder=${sortOrder}`;
    }

    // Make the AJAX call to the MoviesServlet
    jQuery.ajax({
        dataType: "json",
        method: "GET",
        url: query,
        success: (resultData) => handleMovieResult(resultData)
    });
}

let currentPage = 1;
let moviesPerPage = 10;

document.getElementById('recordsPerPage').addEventListener('change', function() {
    moviesPerPage = parseInt(this.value); // Update the recordsPerPage with the selected value
    currentPage = 1;
    fetchNumMovies(); // Fetch the first page of movies with the new limit
});

function fetchNumMovies(diff) {
    let query = url;
    let params = new URLSearchParams(window.location.search);
    params.set('n', moviesPerPage);
    params.set('page', currentPage);
    query = `${url}?${params.toString()}`;

    console.log("./movies.html?" + params.toString());
    page_url = "./movies.html?" + params.toString();
    location.href = "./movies.html?" + params.toString()

}

function goToNextPage() {
    fetchNumMovies(1);
}

function goToPreviousPage() {
    fetchNumMovies(-1);
}