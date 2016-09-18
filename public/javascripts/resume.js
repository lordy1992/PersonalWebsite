function topLevelInfoCallback() {
    $('#top-level-info-list li').each(function(index) {
        var newValue = $(this).find('input').val();
        $(this).find('span.locked-mode').text(newValue);
    });
}

function summaryCallback() {
    var newValue = $('#summary-info-form').find('textarea').val();
    $('#summary-readable-text').html(newValue);
}

$(document).ready(function() {
    var editLinks = [
        {edit: '#top-level-info-edit-link', form: '#top-level-info-form', successCallback: topLevelInfoCallback},
        {edit: '#summary-edit-link', form: '#summary-info-form', successCallback: summaryCallback},
        {edit: '#research-interests-edit-link', form: '#research-interests-info-form'},
        {edit: '#past-experience-edit-link', form: '#past-experience-info-form'},
        {edit: '#expertise-edit-link', form: '#expertise-info-form'},
        {edit: '#language-tech-edit-link', form: '#language-tech-info-form'},
        {edit: '#education-edit-link', form: '#education-info-form'}];

    for (let i = 0; i < editLinks.length; i++) {
        $(editLinks[i].form).find('.editable-mode').hide();

        $(editLinks[i].edit).click(function() {
            var link = editLinks[i];
            $(link.form).find('.locked-mode').hide();
            $(link.form).find('.editable-mode').show();
            $(link.edit).hide();
            $(link.form).find("[type='submit']").parent().show();
        });

        $(editLinks[i].form).submit(function(e) {
            e.preventDefault();

            var form = $(this);
            var actionUrl = form.attr('action');
            var postData = {};

            form.find('input,textarea').each(function(index) {
                var elName = $(this).attr('name');
                postData[elName] = $(this).val();
            });

            $.ajax({
                url: actionUrl,
                dataType: 'json',
                contentType: 'application/json; charset=UTF-8',
                data: JSON.stringify(postData),
                type: 'POST',
                success: function() {
                    var link = editLinks[i];
                    $(link.form).find('.locked-mode').show();
                    $(link.form).find('.editable-mode').hide();
                    $(link.edit).show();
                    $(link.form).find("[type='submit']").parent().hide();

                    if (link.successCallback !== undefined) {
                        link.successCallback.call();
                    }
                },
                error: function() {
                    alert("Ajax fail");
                }
            });
        });
    }
});