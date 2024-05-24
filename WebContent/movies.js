let currentPage = 1;
let hasNext = false;

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

function handleMovieResult(data) {
    resultData = data["movies"];
    currentPage = data["page"];
    hasNext = data["hasNext"];


    console.log(resultData, currentPage, hasNext);

    let moviesTableBodyElement = jQuery("#movies_table_body");
    moviesTableBodyElement.empty();
    // Iterate through resultData, no more than 10 entries
    if (resultData.length === 0 && parseInt(getParameterByName("page") ) > 1){
        goToPreviousPage();
    }
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
        rowHTML += "<th>" + (resultData[i]["year"] === null ? "No Year Found" : resultData[i]["year"]) + "</th>";
        rowHTML += "<th>" + (resultData[i]["director"] === null ? "No Director Found" : resultData[i]["director"]) + "</th>";
        rowHTML += "<th>" + (resultData[i]["genres"] === null ? "No Genre Found" : resultData[i]["genres"]) + "</th>";
        if (resultData[i]["stars"] === null) {
            rowHTML += "<th>No Star Found</th>";
        } else {
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
        }
        rowHTML += "</th>"
        rowHTML += "<th>" + (resultData[i]["rating"] === null ? "No Rating Found" : resultData[i]["rating"]) + "</th>";
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


function getParameterByName(target) {
     // Get request URL
     let url = window.location.href;
//     // Encode target parameter name to url encoding
     target = target.replace(/[\[\]]/g, "\\$&");
//
//     // Ues regular expression to find matched parameter value
     let regex = new RegExp("[?&]" + target + "(=([^&#]*)|&|#|$)"),
     results = regex.exec(url);
     if (!results) return null;
     if (!results[2]) return '';
//
//     // Return the decoded parameter value
     return decodeURIComponent(results[2].replace(/\+/g, " "));
 }

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

/**
 * Once this .js is loaded, following scripts will be executed by the browser
 */

jQuery.ajax({
    dataType: "json", // Setting return data type
    method: "GET", // Setting request method
    // url: "/api/movies" + ,
    url: url + window.location.search,
    success: (resultData) => handleMovieResult(resultData) // Setting callback function to handle data returned successfully by the StarsServlet
});


var sortState = {
    title: "asc",
    rating: "desc"
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

let sortQuery = "";

function sortMovies(sortBy, sortOrder) {
    let params = null;
    if (sortBy === "rating"){
        params = new URLSearchParams({
         "sortBy": sortBy,
         "ratingOrder": sortOrder
        })
    }
    else{
        params = new URLSearchParams({
         "sortBy": sortBy,
         "titleOrder": sortOrder
        })
    }
    jQuery.ajax({
        dataType: "json", // Setting return data type
        method: "GET", // Setting request method
        // url: "/api/movies" + ,
        url: "api/movies?" + params.toString(),
        success: (resultData) => handleMovieResult(resultData) // Setting callback function to handle data returned successfully by the StarsServlet
    });
}

document.getElementById('recordsPerPage').addEventListener('change', function() {
    let moviesPerPage = parseInt(this.value); // Update the recordsPerPage with the selected value
    const params = new URLSearchParams({
        "page": 1,
        "n": moviesPerPage
    })
    jQuery.ajax({
        dataType: "json", // Setting return data type
        method: "GET", // Setting request method
        // url: "/api/movies" + ,
        url: "api/movies?" + params.toString(),
        success: (resultData) => handleMovieResult(resultData) // Setting callback function to handle data returned successfully by the StarsServlet
    });
});

function goToNextPage() {
    let newPage = currentPage + 1;
    if (hasNext){
        const params = new URLSearchParams({
            "page": newPage
        })
        jQuery.ajax({
            dataType: "json", // Setting return data type
            method: "GET", // Setting request method
            // url: "/api/movies" + ,
            url: "api/movies?" + params.toString(),
            success: (resultData) => handleMovieResult(resultData) // Setting callback function to handle data returned successfully by the StarsServlet
        });
    }
}

function goToPreviousPage() {
    let newPage = currentPage -1;
    if (newPage > 0){
        const params = new URLSearchParams({
            "page": newPage
        })
        jQuery.ajax({
            dataType: "json", // Setting return data type
            method: "GET", // Setting request method
            // url: "/api/movies" + ,
            url: "api/movies?" + params.toString(),
            success: (resultData) => handleMovieResult(resultData) // Setting callback function to handle data returned successfully by the StarsServlet
        });
    }
}
