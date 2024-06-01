$(document).ready(function(){
    // Get current page URL
    var currentPageUrl = window.location.href;
    
    // Loop through all nav links
    $('.navbar-nav .nav-link').each(function(){
        // Check if current URL matches nav link
        if(this.href === currentPageUrl){
            // Add 'active' class to the nav link
            $(this).addClass('active');
        }
    });
});