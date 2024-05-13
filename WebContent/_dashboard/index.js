let api_url = "../api/dashboard"
// Bind the form submit event using jQuery
$("#newStarForm").submit(function(event) {
    event.preventDefault(); // Prevent the default form submission

    let formData = $(this).serialize(); // Serialize the form data using jQuery

    $.ajax({
        dataType: "json", // Expect a JSON response from the server
        method: "POST", // Use "POST" for data modification
        url: api_url + window.location.search, // Update this to your servlet's URL
        data: formData,
        success: handleAddStarResult,
        error: function(jqXHR, textStatus, errorThrown) {
            // Handle any errors
            $("#addStarResult")
                .text("AJAX error: " + textStatus + ' : ' + errorThrown)
                .removeClass("alert-success")
                .addClass("alert-danger")
                .show();
        }
    });
});

function handleAddStarResult(resultData) {
    let messageDiv = $("#addStarResult"); // Use jQuery to select the message div
    console.log(resultData);
    if (resultData.success) {
        messageDiv.text("Star added successfully! " + resultData.message);
        messageDiv.removeClass("alert-danger").addClass("alert-success");
    } else {
        messageDiv.text("Failed to add star: " + resultData.message);
        messageDiv.removeClass("alert-success").addClass("alert-danger");
    }
    messageDiv.show();
}

$("#newMovieForm").submit(function(event) {
    event.preventDefault(); // Prevent the default form submission

    let formData = $(this).serialize(); // Serialize the form data using jQuery

    $.ajax({
        dataType: "json", // Expect a JSON response from the server
        method: "POST", // Use "POST" for data modification
        url: api_url + window.location.search, // Update this to your servlet's URL
        data: formData,
        success: handleAddMovieResult,
        error: function(jqXHR, textStatus, errorThrown) {
            // Handle any errors
            $("#addMovieResult")
                .text("AJAX error: " + textStatus + ' : ' + errorThrown)
                .removeClass("alert-success")
                .addClass("alert-danger")
                .show();
        }
    });
});

function handleAddMovieResult(resultData) {
    let messageDiv = $("#addMovieResult"); // Use jQuery to select the message div
    console.log(resultData);
    if (resultData.success) {
        messageDiv.text("Movie added successfully! " + resultData.message);
        messageDiv.removeClass("alert-danger").addClass("alert-success");
    } else {
        messageDiv.text("Failed to add movie: " + resultData.message);
        messageDiv.removeClass("alert-success").addClass("alert-danger");
    }
    messageDiv.show();
}