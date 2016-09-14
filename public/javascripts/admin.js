function enableNextPage(pageId) {
    var messageRows = $('#message-list').children('div');
    var numMessages = messageRows.size();
    messageRows.attr('style', 'display: none;');

    for (i = (pageId - 1) * 10; i < pageId * 10; i++) {
        if (i >= numMessages) {
            break;
        }

        messageRows.eq(i).attr('style', '');
    }

    var pagesNav = $('#pages-nav').children('li');
    pagesNav.attr('class', '');
    pagesNav.eq(pageId).attr('class', 'active');

    if (pageId <= 1) {
        pagesNav.eq(0).attr('class', 'disabled');
    }

    if (pageId >= pagesNav.size() - 2) {
        pagesNav.eq(pagesNav.size() - 1).attr('class', 'disabled');
    }
}

function previousPage() {
    var currentPage = $('#pages-nav').children('.active').index();
    if (currentPage - 1 >= 1) {
        enableNextPage(currentPage - 1);
    }
}

function nextPage() {
    var currentPage = $('#pages-nav').children('.active').index();
    var numMessages = $('#message-list').children('div').size();
    if (currentPage + 1 <= 1 + Math.floor((numMessages - 1) / 10)) {
        enableNextPage(currentPage + 1);
    }
}

$(document).ready(function() {
    $('#confirm-delete-modal').on('show.bs.modal', function(event) {
        var removeLink = $(event.relatedTarget);
        var message = removeLink.data('messageid');
        var modal = $(this);

        modal.find('[name="messageid"]').val(message);
    });
});