/**
 * This example is following frontend and backend separation.
 *
 * Before this .js is loaded, the html skeleton is created.
 *
 * This .js performs three steps:
 *      1. Get parameter from request URL so it know which id to look for
 *      2. Use jQuery to talk to backend API to get the json data.
 *      3. Populate the data to correct html elements.
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
 * Retrieve parameter from request URL, matching by parameter name
 * @param target String
 * @returns {*}
 */
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
let movieId = getParameterByName('id');
/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */

function handleResult(resultData) {
    console.log("handleResult: populating star info from resultData");
    // populate the star info h3
    // find the empty h3 body by id "star_info"
    let starInfoElement = jQuery("#movie_info");

    // append two html <p> created to the h3 body, which will refresh the page
    starInfoElement.append("<p>Title: " + resultData["title"] + "</p>" +
        "<p>Rating: " + resultData["rating"] + "</p>" +
        "<p>Year: " + resultData["year"] + "</p>" +
        "<p>Director: " + resultData["director"] + "</p>");


    let cart_add = jQuery("#cart-add")
    cart_add.on("click", () => Order("increase", movieId, resultData["title"]));

    let genreTableBodyElement = jQuery("#genre_table_body");
    const genres = JSON.parse(resultData["genres"]);
    // Concatenate the html tags with resultData jsonObject to create table rows
    for (let i = 0; i < genres.length; i++) {
        let rowHTML = "";
        rowHTML += "<tr>";
        rowHTML += "<th>" + genres[i]["name"] + "</th>";
        rowHTML += "</tr>";

        // Append the row created to the table body, which will refresh the page
        genreTableBodyElement.append(rowHTML);
    }



    let starsTableBodyElement = jQuery("#stars_table_body");
    const stars = JSON.parse(resultData["stars"]);
    // Concatenate the html tags with resultData jsonObject to create table rows
    for (let i = 0; i < stars.length; i++) {
        let rowHTML = "";
        rowHTML += "<tr><th>";
        rowHTML += "<a href='single-star.html?id=" + stars[i]["starId"] + "'>" + stars[i]["name"] + "</a>";
        rowHTML += "</th></tr>";

        // Append the row created to the table body, which will refresh the page
        starsTableBodyElement.append(rowHTML);
    }

}

/**
 * Once this .js is loaded, following scripts will be executed by the browser\
 */

// Get id from URL
//console.log(movieId);
// Makes the HTTP GET request and registers on success callback function handleResult
jQuery.ajax({
    dataType: "json",  // Setting return data type
    method: "GET",// Setting request method
    url: "api/movie?id=" + movieId,
    success: (resultData) => handleResult(resultData) // Setting callback function to handle data returned successfully by the SingleStarServlet
});