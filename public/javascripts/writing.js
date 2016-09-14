$(document).ready(function() {
    $('#article-list-id a').click(function(e) {
        e.preventDefault();
        var selectedInd = $(this).index();
        var articleContainers = $('#article-panel').children('.article-container');
        var currentActiveInd = $('#article-list-id .active').index();
        articleContainers.eq(currentActiveInd - 1).attr('style', 'display: none;');
        articleContainers.eq(selectedInd - 1).attr('style', '');

        $('#article-list-id').children().eq(currentActiveInd).attr('class', 'list-group-item');
        $('#article-list-id').children().eq(selectedInd).attr('class', 'list-group-item active');
    });
});