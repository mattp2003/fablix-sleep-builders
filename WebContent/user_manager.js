function display() {
    const isEmployee = sessionStorage.getItem('isEmployee') === 'true'; // sessionStorage stores everything as string

    if (isEmployee) {
        $('#dashboardLink').show();
        $('#addStarLink').show();
        $('#addMovieLink').show();
    } else {
        $('#dashboardLink').hide();
        $('#addStarLink').hide();
        $('#addMovieLink').hide();
    }
}

document.addEventListener('DOMContentLoaded', display);